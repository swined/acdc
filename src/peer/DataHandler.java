package peer;

class DataHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public DataHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) throws Exception {
        handler.onPeerData(conn, data);
    }

    public void handlePeerCommand(byte[] data) throws Exception {
    }

}
