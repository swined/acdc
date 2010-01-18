package dcpp;

import hub.HubConnection;
import hub.IHubEventHandler;
import hub.SearchResult;
import java.io.OutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import logger.ILogger;
import peer.IPeerEventHandler;
import peer.PeerConnection;
import util.ISelectable;

public class DownloadManager implements IHubEventHandler, IPeerEventHandler {

    private final int searchPeriod = 60000;
    private final int selectTimeout = 10 * 1000;
    private final String nick = generateNick();
    private final ILogger logger;
    private final String tth;
    private final OutputStream out;
    private boolean hubConnected = false;
    private List<PeerConnection> peers = new LinkedList();
    private Set<PeerConnection> busyPeers = new HashSet();
    private Selector selector = Selector.open();
    private DownloadScheduler scheduler = null;

    public DownloadManager(ILogger logger, OutputStream out, String tth) throws Exception {
        this.logger = logger;
        this.out = out;
        this.tth = tth;
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

    private void select() throws Exception {
        if (selector.select(selectTimeout) > 0) {
            try {
                processSelected();
            } finally {
                selector.selectedKeys().clear();
            }
        }
    }

    private void requestChunks() throws Exception {
        while (peers.size() > 0) {
            long offset = scheduler.getChunk();
            if (offset < 0)
                return;
            scheduler.markAsLoading(offset);
            PeerConnection peer = peers.get(0);
            peers.remove(peer);
            busyPeers.add(peer);
            peer.adcGet(tth, offset, scheduler.getChunkLength(offset));
        }
    }

    public void download(String host, int port) throws Exception {
        HubConnection hub = new HubConnection(this, logger, host, port, nick);
        hub.register(selector);
        long lastSearch = 0;
        while (scheduler == null || !scheduler.isDone()) {
            select();
            if (scheduler != null)
                requestChunks();
            int numPeers = peers.size() + busyPeers.size();
            if (System.currentTimeMillis() - lastSearch > searchPeriod * (numPeers + 1) && hubConnected) {
                if (lastSearch != 0 && numPeers == 0)
                    throw new Exception("search timed out");
                lastSearch = System.currentTimeMillis();
                logger.info("looking for peers");
                hub.search(tth);
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

    public void onHubConnected(HubConnection hub) {
        logger.info("connected to hub");
        hubConnected = true;
    }

    public void onSearchResult(HubConnection hub, SearchResult r) {
        if (r.getFreeSlots() < 1) {
            logger.warn("file found, but no free slots");
            return;
        }
        if (scheduler == null)
            scheduler = new DownloadScheduler(r.getLength());
        try {
            hub.requestPeerConnection(r.getNick());
        } catch (Exception e) {
            logger.warn("connection request failed: " + e.getMessage());
        }
    }

    public void onPeerConnectionRequested(HubConnection hub, String ip, int port) {
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

    public void onHandShakeDone(PeerConnection peer) {
        peers.add(peer);
    }

    public void onNoFreeSlots(PeerConnection peer) throws Exception {
        throw new Exception("no free slots");
    }

    public void onPeerError(PeerConnection peer, String err) throws Exception {
        throw new Exception(err);
    }

    private void status() {
        double committed = Math.round(1000.0 * scheduler.committedProgress()) / 10.0;
        double total = Math.round(1000.0 * scheduler.totalProgress()) / 10.0;
        logger.info("" + committed + "% (" + total + "%) done");
    }

    public void onPeerData(PeerConnection peer, long offset, byte[] data, int start, int length) throws Exception {
        scheduler.setData(offset, data, start, length);
        scheduler.dump(out);
        busyPeers.remove(peer);
        peers.add(peer);
        status();
    }

    public void onSupportsReceived(PeerConnection peer, String[] features) throws Exception {
        boolean adcGet = false;
        for (String feature : features)
            if (feature.equalsIgnoreCase("ADCGet"))
                adcGet = true;
        if (!adcGet)
            throw new Exception("peer does not support adcget");
        String supports = "";
        for (String feature : features) {
            supports += feature + " ";
        }
        logger.debug("peer supports features: " + supports);
    }
}
