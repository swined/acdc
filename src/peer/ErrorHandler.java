package peer;

import util.DCReader.IDCCommandHandler;

class ErrorHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$Error ".getBytes();

    public ErrorHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String s = new String(data, start, length);
        handler.onPeerError(conn, s.split(" ", 2)[1]);
    }

}
