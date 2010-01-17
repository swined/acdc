package peer;

import java.util.Arrays;
import util.DCReader.IDCEventHandler;

class AdcSndHandler implements IDCEventHandler {

    private PeerConnection conn;

    public AdcSndHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        String s = new String(Arrays.copyOfRange(data, start, length));
        if (!s.startsWith("$ADCSND "))
            return;
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
    }

}
