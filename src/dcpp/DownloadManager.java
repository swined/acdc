package dcpp;

import hub.HubConnection;
import hub.IHubEventHandler;
import hub.SearchResult;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import logger.ILogger;
import peer.IPeerEventHandler;
import peer.PeerConnection;
import util.ISelectable;

public class DownloadManager implements IHubEventHandler, IPeerEventHandler {

    private final int searchPeriod = 60000;
    private final int chunkSize = 1024 * 1024;
    private final int selectTimeout = 10 * 1000;
    private final int slowpoke = 10;
    private final int maxChunks = 100 * 1024 * 1024 / chunkSize;
    private final String nick = generateNick();
    private final ILogger logger;
    private final String tth;
    private final OutputStream out;
    private Integer toRead = null;
    private int length = 0;
    private boolean hubConnected = false;
    private Set<Chunk> chunks;
    private Set<PeerConnection> peers;
    private HashMap<PeerConnection, Long> speed = new HashMap();
    private Selector selector = Selector.open();

    public DownloadManager(ILogger logger, OutputStream out, String tth) throws Exception {
        this.logger = logger;
        this.out = out;
        this.tth = tth;
    }

    private long getSpeed(PeerConnection peer) throws Exception {
        Long spd = speed.get(peer);
        return spd == null ? Long.MAX_VALUE : spd;
    }

    private void processSelected() throws Exception {
        for (SelectionKey k : selector.selectedKeys()) {
            if (k.attachment() instanceof ISelectable) {
                ISelectable selectable = (ISelectable) k.attachment();
                try {
                    selectable.update();
                } catch (Exception e) {
                    if (selectable instanceof PeerConnection) {
                        logger.error("peer error: " + e.getMessage());
                        peers.remove(selectable);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private int readyChunksCount() {
        int c = 0;
        for (Chunk chunk : chunks) {
            if (chunk.getData() != null) {
                c++;
            }
        }
        return c;
    }

    private PeerConnection bestPeer() throws Exception {
        PeerConnection fastest = null;
        HashSet<PeerConnection> p = new HashSet(peers);
        for (Chunk chunk : chunks)
            if (chunk.getData() == null)
                p.remove(chunk.getPeer());
        for (PeerConnection peer : p) {
            if (fastest == null) {
                fastest = peer;
                continue;
            }
            if (getSpeed(fastest) > getSpeed(peer)) {
                fastest = peer;
            }
        }
        return fastest;
    }

    private void expireChunks() throws Exception {
        if (chunks.size() >= maxChunks) {
            if (chunks.size() >= readyChunksCount() + 1) {
                Chunk chunk = getFirstChunk();
                if (chunk == null) {
                    return;
                }
                if (chunk.getData() != null) {
                    return;
                }
                PeerConnection best = bestPeer();
                if (best == null) {
                    return;
                }
                if (best == chunk.getPeer()) {
                    return;
                }
                if (getSpeed(best) * slowpoke < getSpeed(chunk.getPeer())) {
                    logger.warn("dropping slow peer");
                    peers.remove(chunk.getPeer());
                    cleanChunks();
                }
            }
        }
    }

    private void cleanChunks() {
        Set<Chunk> delete = new HashSet();
        for (Chunk chunk : chunks) {
            if (chunk.getData() == null) {
                if (!peers.contains(chunk.getPeer())) {
                    delete.add(chunk);
                }
            }
        }
        for (Chunk chunk : delete) {
            chunks.remove(chunk);
        }
    }

    private void dumpChunks() throws Exception {
        while (chunks.size() > 0) {
            Chunk chunk = getFirstChunk();
            if (chunk == null) {
                break;
            }
            if (chunk.getData() == null) {
                return;
            }
            out.write(chunk.getData());
            toRead -= chunk.getData().length;
            chunks.remove(chunk);
            logger.debug("dumping " + chunk);
        }
    }

    private Chunk getFirstChunk() {
        for (Chunk c : chunks) {
            if (c.getStart() == (length - toRead)) {
                return c;
            }
        }
        return null;
    }

    private int getNextChunk() {
        Set<Integer> used = new HashSet();
        for (Chunk c : chunks) {
            used.add(c.getStart());
        }
        for (int i = length - toRead; i < length; i += chunkSize) {
            if (!used.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    private void requestChunks() throws Exception {
        while (chunks.size() < maxChunks) {
            int next = getNextChunk();
            if (next == -1) {
                return;
            }
            int len = (next + chunkSize > length) ? (length - next) : chunkSize;
            PeerConnection peer = bestPeer();
            if (peer == null) {
                return;
            }
            Chunk chunk = new Chunk(peer, next, len);
            chunks.add(chunk);
            logger.debug("requesting " + chunk);
            peer.adcGet(tth, next, len);
        }
    }

    private void status() {
        long size = length - toRead;
        long real = size;
        for (Chunk chunk : chunks) {
            if (chunk.getData() != null) {
                real += chunk.getLength();
            }
        }
        double progress = Math.round(1000.0 * size / length) / 10.0;
        double rp = Math.round(1000.0 * real / length) / 10.0;
        logger.info("" + progress + "% (" + rp + "%) done, " + peers.size() + " peers, " + readyChunksCount() + "/" + chunks.size() + "/" + maxChunks + " chunks");
    }

    private void select() throws Exception {
        if (selector.select(selectTimeout) > 0) {
            try {
                processSelected();
            } finally {
                selector.selectedKeys().clear();
            }
        }
    }

    public void download(String host, int port) throws Exception {
        HubConnection hub = new HubConnection(this, logger, host, port, nick);
        hub.register(selector);
        chunks = new HashSet();
        peers = new HashSet();
        Date lastSearch = new Date(0);
        while (toRead == null || toRead != 0) {
            select();
            expireChunks();
            if (toRead != null) {
                requestChunks();
            }
            if (new Date().getTime() - lastSearch.getTime() > searchPeriod * (peers.size() + 1) && hubConnected) {
                lastSearch = new Date();
                logger.info("looking for peers");
                hub.search(tth);
            }
            if (toRead != null && toRead < 0) {
                throw new Exception("shit happened: need to download " + toRead + " bytes, which is a negative value");
            }
        }
    }

    private String generateNick() {
        Random rand = new Random();
        byte[] bytes = new byte[8];
        rand.nextBytes(bytes);
        String r = "";
        for (byte b : bytes) {
            r += Integer.toHexString(b > 0 ? b : b + 0xFF);
        }
        return r;
    }

    public void onHubConnected(HubConnection hub) throws Exception {
        logger.info("connected to hub");
        hubConnected = true;
    }

    public void onSearchResult(HubConnection hub, SearchResult r) throws Exception {
        if (r.getFreeSlots() < 1) {
            logger.warn("file found, but no free slots");
            return;
        }
        if (toRead == null) {
            length = r.getLength();
            toRead = r.getLength();
        } else {
            if (length != r.getLength()) {
                throw new Exception("peer lied about length");
            }
        }
        hub.requestPeerConnection(r.getNick());
    }

    public void onPeerConnectionRequested(HubConnection hub, String ip, int port) throws Exception {
        try {
            logger.info("connecting to " + ip + ":" + port);
            new PeerConnection(logger, this, ip, port).register(selector);
        } catch (Exception e) {
            logger.warn("connection failed: " + e.getMessage());
        }
    }

    public void onPeerConnected(PeerConnection peer) throws Exception {
        peer.handshake(nick);
    }

    public void onHandShakeDone(PeerConnection peer) throws Exception {
        peers.add(peer);
    }

    public void onNoFreeSlots(PeerConnection peer) throws Exception {
        throw new Exception("no free slots");
    }

    public void onPeerError(PeerConnection peer, String err) throws Exception {
        throw new Exception(err);
    }

    public void onPeerData(PeerConnection peer, byte[] data) throws Exception {
        logger.debug("got " + data.length + " bytes, " + toRead + " of " + length + " bytes left");
        for (Chunk chunk : chunks) {
            if (chunk.getPeer() == peer) {
                if (chunk.getData() == null) {
                    chunk.setData(data);
                    speed.put(peer, new Date().getTime() - chunk.getCTime());
                    dumpChunks();
                    status();
                    return;
                }
            }
        }
        throw new Exception("unexpected data from peer");
    }

    public void onSupportsReceived(PeerConnection peer, String[] features) throws Exception {
        boolean adcGet = false;
        for (String feature : features) {
            if (feature.equalsIgnoreCase("ADCGet")) {
                adcGet = true;
            }
        }
        if (!adcGet) {
            throw new Exception("peer does not support adcget");
        }
        String supports = "";
        for (String feature : features) {
            supports += feature + " ";
        }
        logger.debug("peer supports features: " + supports);
    }
}
