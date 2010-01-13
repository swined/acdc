package dcpp;

import hub.HubConnection;
import hub.IHubEventHandler;
import hub.SearchResult;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import logger.ILogger;
import peer.IPeerEventHandler;
import peer.PeerConnection;

public class DownloadManager implements IHubEventHandler, IPeerEventHandler {

    private final int searchPeriod = 60000;
    private final int chunkSize = 1024 * 1024;
    private final int chunkTimeout = 3600 * 1000;

    private final int maxChunks = 100 * 1024 * 1024 / chunkSize;
    private final String nick = generateNick();
    private final ILogger logger;
    private final String tth;
    private final OutputStream out;

    private Integer toRead = null;
    private int length = 0;
    private boolean hubConnected = false;
    private Set<Chunk> chunks;
    private Set<PeerConnection> connecting;
    private Set<PeerConnection> peers;

    public DownloadManager(ILogger logger, OutputStream out, String tth) {
        this.logger = logger;
        this.out = out;
        this.tth = tth;
    }

    private static void runPeers(Set<PeerConnection> peers, ILogger logger) throws Exception {
        Set<PeerConnection> delete = new HashSet();
        Set<PeerConnection> p = new HashSet(peers);
        for (PeerConnection peer : p) {
            try {
                peer.run();
            } catch (Exception e) {
                logger.warn("peer error: " + e.getMessage());
                delete.add(peer);
            }
        }
        for (PeerConnection peer : delete)
            peers.remove(peer);
    }

    private void expireChunks() {
        for (Chunk chunk : chunks)
            if (chunk.getData() == null)
                if (new Date().getTime() - chunk.getCTime() > chunkTimeout / (peers.size() + 1)) {
                    logger.warn("download timed out, dropping peer");
                    peers.remove(chunk.getPeer());
                }
    }

    private void cleanChunks() {
        Set<Chunk> delete = new HashSet();
        for (Chunk chunk : chunks)
            if (chunk.getData() == null)
                if (!peers.contains(chunk.getPeer()))
                    delete.add(chunk);
        for (Chunk chunk : delete)
            chunks.remove(chunk);
    }

    private void dumpChunks() throws Exception {
        while (chunks.size() > 0) {
            Chunk chunk = null;
            for (Chunk c : chunks)
                if (c.getStart() == (length - toRead))
                    if (c.getData() != null)
                        chunk = c;
            if (chunk == null)
                break;
            out.write(chunk.getData());
            toRead -= chunk.getData().length;
            chunks.remove(chunk);
        }
    }

    private boolean isPeerBusy(PeerConnection peer) throws Exception {
        for (Chunk chunk : chunks)
            if (chunk.getData() == null)
                if (chunk.getPeer().equals(peer))
                    return true;
        return false;
    }

    private PeerConnection getPeer() throws Exception {
        for (PeerConnection peer : peers)
            if (!isPeerBusy(peer))
                return peer;
        return null;
    }

    private int getNextChunk() {
        Set<Integer> used = new HashSet();
        for (Chunk c : chunks)
            used.add(c.getStart());
        for (int i = length - toRead; i < length; i += chunkSize)
            if (!used.contains(i))
                return i;
        return -1;
    }

    private void requestChunks() throws Exception {
        while (chunks.size() < maxChunks) {
            int next = getNextChunk();
            if (next == -1)
                return;
            int len = (next + chunkSize > length) ? (length - next) : chunkSize;
            PeerConnection peer = getPeer();
            if (peer == null)
                return;
            Chunk chunk = new Chunk(peer, next, len);
            chunks.add(chunk);
            logger.debug("requesting " + chunk);
            peer.adcGet(tth, next, len);
        }
    }

    private void status() {
        long size = length - toRead;
        long real = size;
        for (Chunk chunk : chunks)
            if (chunk.getData() != null)
                real += chunk.getLength();
        double progress = Math.round(1000.0 * size / length) / 10.0;
        double rp = Math.round(1000.0 * real / length) / 10.0;
        logger.info("" + progress + "% (" + rp + "%) done, " + peers.size() + " peers");
    }

    public void download(String host, int port) throws Exception {
        HubConnection hub = new HubConnection(this, logger, host, port, nick);
        chunks = new HashSet();
        peers = new HashSet();
        connecting = new HashSet();
        Date lastSearch = new Date(0);
        while (toRead == null || toRead != 0) {
            hub.run();
            runPeers(peers, logger);
            runPeers(connecting, logger);
            expireChunks();
            cleanChunks();
            dumpChunks();
            if (toRead != null)
                requestChunks();
            if (new Date().getTime() - lastSearch.getTime() > searchPeriod * (peers.size() + 1) && hubConnected) {
                lastSearch = new Date();
                logger.info("looking for peers");
                hub.search(tth);
            }
            if (toRead != null && toRead < 0)
                throw new Exception("shit happened: need to download " + toRead + " bytes, which is a negative value");
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
            if (length != r.getLength())
                throw new Exception("peer lied about length");
        }
        hub.requestPeerConnection(r.getNick());
    }

    public void onPeerConnectionRequested(HubConnection hub, String ip, int port) throws Exception {
        try {
            logger.info("connecting to " + ip + ":" + port);
            connecting.add(new PeerConnection(logger, this, ip, port));
        } catch (Exception e) {
            logger.warn("connection failed: " + e.getMessage());
        }
    }

    public void onPeerConnected(PeerConnection peer) throws Exception {
        peer.handshake(nick);
    }

    public void onHandShakeDone(PeerConnection peer) throws Exception {
        peers.add(peer);
        connecting.remove(peer);
    }

    public void onNoFreeSlots(PeerConnection peer) throws Exception {
        throw new Exception("no free slots");
    }

    public void onPeerError(PeerConnection peer, String err) throws Exception {
        throw new Exception(err);
    }

    public void onPeerData(PeerConnection peer, byte[] data) throws Exception {
        logger.debug("got " + data.length + " bytes, " + toRead + " of " + length + " bytes left");
        for (Chunk chunk : chunks)
            if (chunk.getPeer() == peer)
                if (chunk.getData() == null) {
                    chunk.setData(data);
                    status();
                    return;
                }
        throw new Exception("unexpected data from peer");
    }

    public void onSupportsReceived(PeerConnection peer, String[] features) throws Exception {
        boolean adcGet = false;
        for (String feature : features)
            if (feature.equalsIgnoreCase("ADCGet"))
                adcGet = true;
        if (!adcGet)
            throw new Exception("peer does not support adcget");
        String supports = "";
        for (String feature : features)
            supports += feature + " ";
        logger.debug("peer supports features: " + supports);
    }

}
