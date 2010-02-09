package peer;

import util.DCReader.IDCCommandHandler;

class SupportsHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$Supports ".getBytes();

    public SupportsHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String s = new String(data, start, length);
        handler.onSupportsReceived(conn, s.split(" ", 2)[1].split(" "));
    }

}
