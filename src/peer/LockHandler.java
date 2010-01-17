package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class LockHandler implements IDCEventHandler {

    private PeerConnection peer;
    private final static byte[] cmd = "$Lock ".getBytes();

    public LockHandler(PeerConnection peer) {
        this.peer = peer;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String d = new String(data, start, length);
        String[] s = d.split(" ");
        if (s.length < 2)
            throw new Exception("failed to parse $Lock command");
        peer.onLockReceived(s[1]);
    }
    
}
