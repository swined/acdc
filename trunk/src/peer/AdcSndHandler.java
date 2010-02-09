package peer;

import util.DCReader.IDCCommandHandler;

class AdcSndHandler implements IDCCommandHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$ADCSND ".getBytes();

    public AdcSndHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String s = new String(data, start, length);
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
    }

}
