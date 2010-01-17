package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class KeyHandler implements IDCEventHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$Key ".getBytes();

    public KeyHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        conn.onKeyReceived();
    }

}
