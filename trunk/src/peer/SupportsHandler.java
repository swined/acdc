package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class SupportsHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$Supports ".getBytes();

    public SupportsHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String s = new String(data, start, length);
        handler.onSupportsReceived(conn, s.split(" ", 2)[1].split(" "));
    }

}
