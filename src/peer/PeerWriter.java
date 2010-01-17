package peer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import logger.ILogger;

class PeerWriter {

    private ILogger logger;
    private SocketChannel out;

    public PeerWriter(SocketChannel out, ILogger logger) {
        this.out = out;
        this.logger = logger;
    }

    private void sendBytes(byte[] s) throws IOException {
        out.write(ByteBuffer.wrap(s));
        logger.debug("sent string to peer: " + new String(s));
    }

    private void sendString(String s) throws IOException {
        sendBytes(s.getBytes());
    }

    public void sendMyNick(String nick) throws Exception {
        sendString("$MyNick " + nick + "|");
    }

    public void sendLock(String lock, String pk) throws Exception {
        sendString("$Lock " + lock + " Pk=" + pk + "|");
    }

    public void sendGet(byte[] file, int start) throws Exception {
        sendString("$Get ");
        sendBytes(file);
        sendString("$" + start + "|");
    }

    public void sendKey(String key) throws IOException {
        sendString("$Key " + key + "|");
    }

    public void sendSupports(String features) throws IOException {
        sendString("$Supports " + features + "|");
    }

    public void sendDirection(String direction, int a) throws Exception {
        sendString("$Direction " + direction + " " + a + "|");
    }

    public void sendSend() throws Exception {
        sendString("$Send|");
    }

    public void sendAdcGet(String tth, long from, long len) throws Exception {
        sendString("$ADCGET file TTH/" + tth + " " + from + " " + len + "|");
    }

}
