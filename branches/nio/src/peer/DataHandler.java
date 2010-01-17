package peer;

import util.DCReader.IDCEventHandler;

class DataHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public DataHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        handler.onPeerData(conn, data);
    }

}
