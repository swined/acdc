package peer;

import util.DCReader.IDCEventHandler;

class ErrorHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public ErrorHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Error "))
            return;
        handler.onPeerError(conn, s.split(" ", 2)[1]);
    }

}
