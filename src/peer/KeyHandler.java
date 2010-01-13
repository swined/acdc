package peer;

import util.DCReader.IDCEventHandler;

class KeyHandler implements IDCEventHandler {

    private PeerConnection conn;

    public KeyHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$Key "))
            return;
        conn.onKeyReceived();
    }

}
