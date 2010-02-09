package peer;

import util.DCReader.IDCDataHandler;

class DataHandler implements IDCDataHandler {

    private PeerConnection conn;

    public DataHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCData(byte[] data, int start, int length) throws Exception {
        conn.onPeerData(conn, data, start, length);
    }

}
