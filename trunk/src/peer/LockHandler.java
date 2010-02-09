package peer;

import util.DCReader.IDCCommandHandler;

class LockHandler implements IDCCommandHandler {

    private PeerConnection peer;
    private final static byte[] cmd = "$Lock ".getBytes();

    public LockHandler(PeerConnection peer) {
        this.peer = peer;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String d = new String(data, start, length);
        String[] s = d.split(" ");
        if (s.length < 2)
            throw new Exception("failed to parse $Lock command");
        peer.onLockReceived(s[1]);
    }
    
}
