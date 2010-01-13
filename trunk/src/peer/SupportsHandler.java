package peer;

class SupportsHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public SupportsHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) throws Exception {
    }

    public void handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Supports "))
            return;
        handler.onSupportsReceived(conn, s.split(" ", 2)[1].split(" "));
    }

}
