package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class SupportsHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public SupportsHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String s = new String(Arrays.copyOfRange(data, start, start + length));
        if (!s.startsWith("$Supports "))
            return;
        handler.onSupportsReceived(conn, s.split(" ", 2)[1].split(" "));
    }

}
