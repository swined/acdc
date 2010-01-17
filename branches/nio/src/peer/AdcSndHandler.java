package peer;

import util.DCReader.IDCEventHandler;

class AdcSndHandler implements IDCEventHandler {

    private PeerConnection conn;

    public AdcSndHandler(PeerConnection conn) {
        this.conn = conn;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$ADCSND "))
            return;
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
    }

}
