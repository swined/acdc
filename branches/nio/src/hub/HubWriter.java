package hub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import logger.ILogger;

class HubWriter {

    private ILogger logger;
    private SocketChannel out;

    public HubWriter(SocketChannel out, ILogger logger) {
        this.out = out;
        this.logger = logger;
    }

    private void sendString(String s) throws IOException {
        out.write(ByteBuffer.wrap(s.getBytes()));
        logger.debug("sent string to hub: " + s);
    }

    public void sendValidateNick(String nick) throws IOException {
        sendString("$ValidateNick " + nick + "|");
    }

    public void sendKey(String key) throws IOException {
        sendString("$Key " + key + "|");
    }

    public void sendVersion(String version) throws IOException {
        sendString("$Version " + version + "|");
    }

    public void sendMyInfo(String nick) throws IOException {
        sendString("$MyINFO $ALL " + nick + " $ $Cable" + new String(new byte[] { 0x08 }) + "$$0$|");
    }
    
    public void sendTTHSearch(String nick, String tth) throws IOException {
        sendString("$Search Hub:" + nick + " F?F?0?9?TTH:" + tth + "|");
    }

    public void sendRevConnectToMe(String nick, String target) throws IOException {
        sendString("$RevConnectToMe " + nick + " " + target + "|");
    }

    public void sendUserIP(String target) throws IOException {
        sendString("$UserIP " + target + "|");
    }

}
