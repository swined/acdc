package peer;

import util.DCReader.IDCEventHandler;

class MaxedOutHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public MaxedOutHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$MaxedOut"))
            return;
        handler.onNoFreeSlots(conn);
    }

}
