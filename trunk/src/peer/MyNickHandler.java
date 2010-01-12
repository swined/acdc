package peer;

class MyNickHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public MyNickHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$MyNick "))
            return false;
        conn.onPeerNickReceived(s.split(" ")[1]);
        return true;
    }

}
