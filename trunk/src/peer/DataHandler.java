package peer;

import util.DCReader.IDCEventHandler;

class DataHandler implements IDCEventHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public DataHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        conn.onPeerData(conn, data, start, length);
    }

}
