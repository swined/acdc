package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class LockHandler implements IDCEventHandler {

    private PeerConnection peer;

    public LockHandler(PeerConnection peer) {
        this.peer = peer;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String d = new String(Arrays.copyOfRange(data, start, start + length));
        if (!d.startsWith("$Lock "))
            return;
        String[] s = d.split(" ");
        if (s.length < 2)
            throw new Exception("failed to parse $Lock command");
        peer.onLockReceived(s[1]);
    }
    
}
