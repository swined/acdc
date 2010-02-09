package peer;

import util.ArrayUtils;
import util.DCReader.IDCCommandHandler;

class MyNickHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$MyNick ".getBytes();

    public MyNickHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String s = new String(data, start, length);
        conn.onPeerNickReceived(s.split(" ")[1]);
    }

}
