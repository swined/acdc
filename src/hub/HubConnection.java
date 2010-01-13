package hub;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import logger.ILogger;
import util.DCReader;
import util.KeyGenerator;

public class HubConnection {

    private IHubEventHandler handler;
    private DCReader reader;
    private HubWriter writer;
    private String nick;

    public HubConnection(IHubEventHandler handler, ILogger logger, String host, int port, String nick) throws Exception {
        this.handler = handler;
        logger.debug("connecting to " + host + ":" + port);
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
        this.reader = new DCReader(channel);
        this.writer = new HubWriter(channel, logger);
        this.nick = nick;
        reader.registerCommandHandler(new CommandLoggingHandler(logger));
        reader.registerCommandHandler(new LockHandler(this));
        reader.registerCommandHandler(new SRHandler(this, handler));
        reader.registerCommandHandler(new ConnectToMeHandler(this, handler));
    }

    public void run() throws Exception {
        reader.read();
    }

    public void onHubConnected(String lock) throws Exception {
        writer.sendKey(KeyGenerator.generateKey(lock.getBytes()));
        writer.sendValidateNick(nick);
        writer.sendVersion("0.01");
        writer.sendMyInfo(nick);
        handler.onHubConnected(this);
    }

    public void search(String tth) throws Exception {
        writer.sendTTHSearch(nick, tth);
    }

    public void requestPeerConnection(String target) throws Exception {
        writer.sendRevConnectToMe(nick, target);
    }

}
