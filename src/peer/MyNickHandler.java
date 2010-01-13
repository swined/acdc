package peer;

import util.DCReader.IDCEventHandler;

class MyNickHandler implements IDCEventHandler {

    private PeerConnection conn;

    public MyNickHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$MyNick "))
            return;
        conn.onPeerNickReceived(s.split(" ")[1]);
    }

}
