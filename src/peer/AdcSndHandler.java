package peer;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class AdcSndHandler implements IDCEventHandler {

    private PeerConnection conn;
    private final static byte[] cmd = "$ADCSND ".getBytes();

    public AdcSndHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String s = new String(data, start, length);
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
    }

}
