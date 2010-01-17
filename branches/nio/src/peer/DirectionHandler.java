package peer;

import util.DCReader.IDCEventHandler;

class DirectionHandler implements IDCEventHandler {

    private PeerConnection conn;

    public DirectionHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Direction "))
            return;
        conn.onDirectionReceived(s.split(" ")[1], new Integer(s.split(" ")[2]));
    }

}
