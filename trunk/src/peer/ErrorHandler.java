package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class ErrorHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public ErrorHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String s = new String(Arrays.copyOfRange(data, start, length));
        if (!s.startsWith("$Error "))
            return;
        handler.onPeerError(conn, s.split(" ", 2)[1]);
    }

}
