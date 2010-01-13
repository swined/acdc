package peer;

import util.DCReader.IDCEventHandler;

class LockHandler implements IDCEventHandler {

    private PeerConnection peer;

    public LockHandler(PeerConnection peer) {
        this.peer = peer;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String d = new String(data);
        if (!d.startsWith("$Lock "))
            return;
        String[] s = d.split(" ");
        if (s.length < 2)
            throw new Exception("failed to parse $Lock command");
        peer.onLockReceived(s[1]);
    }
    
}
