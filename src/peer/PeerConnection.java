package peer;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import util.KeyGenerator;
import logger.ILogger;

public class PeerConnection {

    private ILogger logger;
    private IPeerEventHandler handler;
    private PeerReader reader;
    private PeerWriter writer;
    private String nick;

    public PeerConnection(ILogger logger, IPeerEventHandler handler, String ip, int port) throws Exception {
        this.logger = new PeerLogger(logger, ip);
        this.handler = handler;
        connect(ip, port);
    }

    public void run() throws Exception {
        reader.read();
    }

    private void connect(String ip, int port) throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));
        channel.configureBlocking(false);
        writer = new PeerWriter(channel, logger);
        reader = new PeerReader(channel, logger);
        reader.registerHandler(new MyNickHandler(handler, this));
        reader.registerHandler(new LockHandler(this));
        reader.registerHandler(new DirectionHandler(handler, this));
        reader.registerHandler(new KeyHandler(handler, this));
        reader.registerHandler(new ErrorHandler(handler, this));
        reader.registerHandler(new MaxedOutHandler(handler, this));
        reader.registerHandler(new DataHandler(handler, this));
        reader.registerHandler(new SupportsHandler(handler, this));
        reader.registerHandler(new AdcSndHandler(handler, this));
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
