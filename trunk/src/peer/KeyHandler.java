package peer;

import util.DCReader.IDCCommandHandler;

class KeyHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$Key ".getBytes();

    public KeyHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        conn.onKeyReceived();
    }

}
