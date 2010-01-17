package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class MyNickHandler implements IDCEventHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$MyNick ".getBytes();

    public MyNickHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String s = new String(data, start, length);
        conn.onPeerNickReceived(s.split(" ")[1]);
    }

}
