package peer;

class DirectionHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public DirectionHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Direction "))
            return false;
        conn.onDirectionReceived(s.split(" ")[1], new Integer(s.split(" ")[2]));
        return true;
    }

}
