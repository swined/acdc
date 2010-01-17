package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class MyNickHandler implements IDCEventHandler {

    private PeerConnection conn;

    public MyNickHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String s = new String(Arrays.copyOfRange(data, start, length));
        if (!s.startsWith("$MyNick "))
            return;
        conn.onPeerNickReceived(s.split(" ")[1]);
    }

}
