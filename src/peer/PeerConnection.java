package peer;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import util.KeyGenerator;
import logger.ILogger;
import util.DCReader;
import util.ISelectable;

public class PeerConnection implements ISelectable {

    private ILogger logger;
    private IPeerEventHandler handler;
    private DCReader reader;
    private PeerWriter writer;
    private String nick;
    private long adcSndOffset = -1;

    public PeerConnection(ILogger logger, IPeerEventHandler handler, String ip, int port) throws Exception {
        this.logger = new PeerLogger(logger, ip);
        this.handler = handler;
        connect(ip, port);
    }

    public void adcGet(String tth, long from, long len) throws Exception {
        adcSndOffset = from;
        writer.sendAdcGet(tth, from, len);
    }

    public void close() throws Exception {
    	reader.close();
    }

    private void connect(String ip, int port) throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));
        channel.configureBlocking(false);
        writer = new PeerWriter(channel, logger);
        reader = new DCReader(channel);
        reader.registerCommandHandler(new MyNickHandler(this));
        reader.registerCommandHandler(new LockHandler(this));
        reader.registerCommandHandler(new KeyHandler(this));
        reader.registerCommandHandler(new ErrorHandler(handler, this));
        reader.registerCommandHandler(new MaxedOutHandler(handler, this));
        reader.registerCommandHandler(new SupportsHandler(handler, this));
        reader.registerCommandHandler(new AdcSndHandler(this));
        reader.registerCommandHandler(new CommandLoggingHandler(logger));
        reader.registerDataHandler(new DataHandler(this));
        reader.registerDataHandler(new DataLoggingHandler(logger));
        handler.onPeerConnected(this);
    }

    public void get(byte[] file, int start) throws Exception {
        writer.sendGet(file, start);
    }

    public String getNick() {
        return nick;
    }

    public long getOffset() {
    	return adcSndOffset;
    }

    public void handshake(String nick) throws Exception {
        writer.sendMyNick(nick);
        writer.sendLock("EXTENDEDPROTOCOL_some_random_lock", "kio_dcpp");
    }

    public void onAdcSndReceived(int from, int len) {
        logger.debug("peer offered " + len + " bytes of data");
        reader.expect(len);
    }

    public void onKeyReceived() throws Exception {
        handler.onHandShakeDone(this);
    }

    public void onLockReceived(String lock) throws Exception {
        writer.sendSupports("ADCGet TTHF");
        writer.sendDirection("Download", 42000);
        writer.sendKey(KeyGenerator.generateKey(lock.getBytes()));
    }

    public void onPeerData(PeerConnection peer, byte[] data, int start, int length) throws Exception {
        handler.onPeerData(peer, adcSndOffset, data, start, length);
        adcSndOffset = -1;
    }

    public void onPeerNickReceived(String nick) {
        this.nick = nick;
    }

    public void register(Selector selector) throws Exception {
        reader.getChannel().register(selector, SelectionKey.OP_READ, this);
    }

    public void send(int len) throws Exception {
        writer.sendSend();
        reader.expect(len);
    }

    public void sendSupports(String features) throws Exception {
        writer.sendSupports(features);
    }

    public void update() throws Exception {
        reader.update();
    }

}
