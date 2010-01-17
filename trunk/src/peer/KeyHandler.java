package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class KeyHandler implements IDCEventHandler {

    private PeerConnection conn;

    public KeyHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String s = new String(Arrays.copyOfRange(data, start, start + length));
        if (!s.startsWith("$Key "))
            return;
        conn.onKeyReceived();
    }

}
