package peer;

class ErrorHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public ErrorHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Error "))
            return false;
        handler.onPeerError(conn, s.split(" ", 2)[1]);
        return true;
    }

}
