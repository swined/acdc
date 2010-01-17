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
    private Chunk[] chunks = new Chunk[maxChunks];
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
            if (chunk != null)
            if (chunk.getData() != null) {
                c++;
            }
        }
        return c;
    }

    private boolean isPeerBusy(PeerConnection peer) {
        for (Chunk chunk : chunks)
            if (chunk != null)
                if (chunk.getPeer() == peer)
                    return true;
        return false;
    }

    private PeerConnection bestPeer() throws Exception {
        PeerConnection fastest = null;
        for (PeerConnection peer : peers) {
            if (isPeerBusy(peer))
                continue;
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

    private int activeChunks() {
        int r = 0;
        for (Chunk chunk : chunks)
            if (chunk != null)
                r++;
        return r;
    }

    private void expireChunks() throws Exception {
        if (activeChunks() >= maxChunks) {
            if (activeChunks() >= readyChunksCount() + 1) {
                if (chunks[0] == null) {
                    return;
                }
                if (chunks[0].getData() != null) {
                    return;
                }
                PeerConnection best = bestPeer();
                if (best == null) {
                    return;
                }
                if (best == chunks[0].getPeer()) {
                    return;
                }
                if (getSpeed(best) * slowpoke < getSpeed(chunks[0].getPeer())) {
                    logger.warn("dropping slow peer");
                    peers.remove(chunks[0].getPeer());
                    cleanChunks();
                }
            }
        }
    }

    private void cleanChunks() {
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] != null)
            if (chunks[i].getData() == null) {
                if (!peers.contains(chunks[i].getPeer())) {
                    chunks[i] = null;
                }
            }
        }
    }

    private void dumpChunks() throws Exception {
        while (chunks[0] != null) {
            if (chunks[0].getData() == null)
                return;
            out.write(chunks[0].getData());
            toRead -= chunks[0].getData().length;
            logger.debug("dumping " + chunks[0]);
            chunks[0] = null;
            for (int i = 0; i < chunks.length - 1; i++)
                chunks[i] = chunks[i+1];
            chunks[chunks.length - 1] = null;
        }
    }

    private int getNextChunk() {
        for (int i = 0; i < chunks.length; i++)
            if (chunks[i] == null)
                if (i * chunkSize < toRead)
                    return i;
        return -1;
    }

    private void requestChunks() throws Exception {
        int next = 0;
        while ((next = getNextChunk()) != -1) {
            int len = ((next + 1) * chunkSize > toRead) ? length % chunkSize : chunkSize;
            PeerConnection peer = bestPeer();
            if (peer == null) {
                return;
            }
            Chunk chunk = new Chunk(peer, length - toRead + next * chunkSize, len);
            chunks[next] = chunk;
            logger.debug("requesting " + chunk);
            peer.adcGet(tth, next * chunkSize, len);
        }
    }

    private void status() {
        long size = length - toRead;
        long real = size;
        for (Chunk chunk : chunks) {
            if (chunk != null)
            if (chunk.getData() != null) {
                real += chunk.getLength();
            }
        }
        double progress = Math.round(1000.0 * size / length) / 10.0;
        double rp = Math.round(1000.0 * real / length) / 10.0;
        logger.info("" + progress + "% (" + rp + "%) done, " + peers.size() + " peers, " + readyChunksCount() + "/" + activeChunks() + "/" + maxChunks + " chunks");
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
            if (chunk != null)
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
