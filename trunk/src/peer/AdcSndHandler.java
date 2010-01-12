package peer;

class AdcSndHandler implements IPeerHandler {

    private PeerConnection conn;
    private IPeerEventHandler handler;

    public AdcSndHandler(IPeerEventHandler handler, PeerConnection conn) {
        this.handler = handler;
        this.conn = conn;
    }

    public void handlePeerData(byte[] data) {

    }

    public boolean handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$ADCSND "))
            return false;
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
        return true;
    }

}
