package peer;

import util.DCReader.IDCCommandHandler;

class MaxedOutHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;
    private final static byte[] cmd = "$MaxedOut".getBytes();

    public MaxedOutHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        handler.onNoFreeSlots(conn);
    }

}
