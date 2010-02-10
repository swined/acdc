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

    private final int timeout = 30 * 1000;
    private final int searchPeriod = 60 * 1000;
    private final int selectTimeout = 1 * 1000;
    private final String nick = generateNick();
    private final ILogger logger;
    private final String tth;
    private final OutputStream out;
    private boolean hubConnected = false;
    private List<PeerConnection> peers = new LinkedList<PeerConnection>();
    private Set<PeerConnection> busyPeers = new HashSet<PeerConnection>();
    private Selector selector = Selector.open();
    private DownloadScheduler scheduler = null;
    private long lastActivity = 0;

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
                        selectable.close();
                        peers.remove(selectable);
                        busyPeers.remove(selectable);
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
        lastActivity = System.currentTimeMillis();
        logger.info("downlading TTH/" + tth);
        while (scheduler == null || !scheduler.isDone()) {
            select();
            if (scheduler != null)
                requestChunks();
	    if (System.currentTimeMillis() - lastActivity > timeout) {
		if (busyPeers.size() == 1) {
		    for (PeerConnection peer : busyPeers)
			peer.close();
		    busyPeers.clear();
		} else
		    throw new Exception("timed out");
	    }
            int numPeers = peers.size() + busyPeers.size();
            if (System.currentTimeMillis() - lastSearch > searchPeriod * (numPeers + 1) && hubConnected) {
                lastSearch = System.currentTimeMillis();
                logger.info("looking for peers (" + peers.size() + "/" + busyPeers.size() + ")");
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
            lastActivity = System.currentTimeMillis();
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
        logger.info("" + committed + "% (" + total + "%) done, " + peers.size() + "/" + busyPeers.size() + " peers");
    }

    public void onPeerData(PeerConnection peer, long offset, byte[] data, int start, int length) throws Exception {
        scheduler.setData(offset, data, start, length);
        scheduler.dump(out);
        busyPeers.remove(peer);
        peers.add(peer);
        status();
        lastActivity = System.currentTimeMillis();
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
