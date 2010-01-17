package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class ErrorHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$Error ".getBytes();

    public ErrorHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String s = new String(data, start, length);
        handler.onPeerError(conn, s.split(" ", 2)[1]);
    }

}
