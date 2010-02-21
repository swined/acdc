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
import util.EventDispatcher;
import util.ISelectable;

public class DownloadManager implements IHubEventHandler, IPeerEventHandler {

    private final int selectTimeout = 10 * 1000;
    private final String nick = generateNick();
    private final ILogger logger;
    private final String tth;
    private final OutputStream out;
    private final Selector selector = Selector.open();
    private final EventDispatcher dispatcher = new EventDispatcher();
    private List<PeerConnection> peers = new LinkedList<PeerConnection>();
    private Set<PeerConnection> busyPeers = new HashSet<PeerConnection>();
    private DownloadScheduler scheduler = null;

    public interface IHubConnectedEventHandler {
    	void onHubConnected(DownloadManager manager, HubConnection hub);
    }
    
    public interface IMainLoopEventHandler {
    	void onMainLoop(DownloadManager manager) throws Exception;
    }
    
    public DownloadManager(ILogger logger, OutputStream out, String tth) throws Exception {
        this.logger = logger;
        this.out = out;
        this.tth = tth;
        dispatcher.register(new Search(logger, tth));
    }

    public void download(String host, int port) throws Exception {
        HubConnection hub = new HubConnection(this, logger, host, port, nick);
        hub.register(selector);
        logger.info("downlading TTH/" + tth);
        while (scheduler == null || !scheduler.isDone()) {
            select();
            dispatcher.invoke(IMainLoopEventHandler.class).onMainLoop(this);
            if (scheduler != null)
                requestChunks();
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

    public void onHandShakeDone(PeerConnection peer) {
        peers.add(peer);
    }

    public void onHubConnected(HubConnection hub) {
        logger.info("connected to hub");
        dispatcher.invoke(IHubConnectedEventHandler.class).onHubConnected(this, hub);
    }

    public void onNoFreeSlots(PeerConnection peer) throws Exception {
        throw new Exception("no free slots");
    }

    public void onPeerConnected(PeerConnection peer) throws Exception {
        peer.handshake(nick);
    }

    public void onPeerConnectionRequested(HubConnection hub, String ip, int port) {
        try {
            logger.info("connecting to " + ip + ":" + port);
            new PeerConnection(logger, this, ip, port).register(selector);
        } catch (Exception e) {
            logger.warn("connection failed: " + e.getMessage());
        }
    }

    public void onPeerData(PeerConnection peer, long offset, byte[] data, int start, int length) throws Exception {
        scheduler.setData(offset, data, start, length);
        scheduler.dump(out);
        busyPeers.remove(peer);
        peers.add(peer);
        status();
    }

    public void onPeerError(PeerConnection peer, String err) throws Exception {
        throw new Exception(err);
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
                        long offset = ((PeerConnection)selectable).getOffset();
                        if (offset != -1) {
                        	scheduler.cancelDownload(offset);
                        }
                    } else {
                        throw e;
                    }
                }
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

    private void select() throws Exception {
        if (selector.select(selectTimeout) > 0) {
            try {
                processSelected();
            } finally {
                selector.selectedKeys().clear();
            }
        }
    }

    private void status() {
        double committed = Math.round(1000.0 * scheduler.committedProgress()) / 10.0;
        double total = Math.round(1000.0 * scheduler.totalProgress()) / 10.0;
        logger.info("" + committed + "% (" + total + "%) done, " + peers.size() + "/" + busyPeers.size() + " peers");
    }
}
