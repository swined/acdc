package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class MaxedOutHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$MaxedOut".getBytes();

    public MaxedOutHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        handler.onNoFreeSlots(conn);
    }

}
