package peer;

class MaxedOutHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public MaxedOutHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$MaxedOut"))
            return false;
        handler.onNoFreeSlots(conn);
        return true;
    }

}
