package peer;

class LockHandler implements IPeerHandler {

    private PeerConnection peer;

    public LockHandler(PeerConnection peer) {
        this.peer = peer;
    }

    public void handlePeerData(byte[] data)  throws Exception {
        
    }

    public void handlePeerCommand(byte[] data) throws Exception {
        String d = new String(data);
        if (!d.startsWith("$Lock "))
            return;
        String[] s = d.split(" ");
        if (s.length < 2)
            throw new Exception("failed to parse $Lock command");
        peer.onLockReceived(s[1]);
    }
    
}
