package peer;

class KeyHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public KeyHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Key "))
            return false;
        conn.onKeyReceived();
        return true;
    }

}
