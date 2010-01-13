package peer;

import util.DCReader.IDCEventHandler;

class SupportsHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public SupportsHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Supports "))
            return;
        handler.onSupportsReceived(conn, s.split(" ", 2)[1].split(" "));
    }

}
