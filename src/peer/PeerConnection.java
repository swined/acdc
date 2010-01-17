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

    public PeerConnection(ILogger logger, IPeerEventHandler handler, String ip, int port) throws Exception {
        this.logger = new PeerLogger(logger, ip);
        this.handler = handler;
        connect(ip, port);
    }

    public void register(Selector selector) throws Exception {
        reader.getChannel().register(selector, SelectionKey.OP_READ, this);
    }

    public void update() throws Exception {
        reader.update();
    }

    private void connect(String ip, int port) throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));
        channel.configureBlocking(false);
        writer = new PeerWriter(channel, logger);
        reader = new DCReader(channel);
        reader.registerCommandHandler(new MyNickHandler(this));
        reader.registerCommandHandler(new LockHandler(this));
        reader.registerCommandHandler(new DirectionHandler(this));
        reader.registerCommandHandler(new KeyHandler(this));
        reader.registerCommandHandler(new ErrorHandler(handler, this));
        reader.registerCommandHandler(new MaxedOutHandler(handler, this));
        reader.registerCommandHandler(new SupportsHandler(handler, this));
        reader.registerCommandHandler(new AdcSndHandler(this));
        reader.registerCommandHandler(new CommandLoggingHandler(logger));
        reader.registerDataHandler(new DataHandler(handler, this));
        reader.registerDataHandler(new DataLoggingHandler(logger));
        handler.onPeerConnected(this);
    }

    public void handshake(String nick) throws Exception {
        writer.sendMyNick(nick);
        writer.sendLock("EXTENDEDPROTOCOL_some_random_lock", "kio_dcpp");
    }

    public void get(byte[] file, int start) throws Exception {
        writer.sendGet(file, start);
    }

    public void onKeyReceived() throws Exception {
        handler.onHandShakeDone(this);
    }

    public void onLockReceived(String lock) throws Exception {
        writer.sendSupports("ADCGet TTHF");
        writer.sendDirection("Download", 42000);
        writer.sendKey(KeyGenerator.generateKey(lock.getBytes()));
    }

    public void onDirectionReceived(String direction, int i) throws Exception {
        
    }

    public void onPeerNickReceived(String nick) throws Exception {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public void send(int len) throws Exception {
        writer.sendSend();
        reader.expect(len);
    }

    public void adcGet(String tth, int from, int len) throws Exception {
        writer.sendAdcGet(tth, from, len);
    }

    public void onAdcSndReceived(int from, int len) throws Exception {
        logger.debug("peer offered " + len + " bytes of data");
        reader.expect(len);
    }

    public void sendSupports(String features) throws Exception {
        writer.sendSupports(features);
    }

}
