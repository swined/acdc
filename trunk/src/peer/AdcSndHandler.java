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

    public void handlePeerCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$ADCSND "))
            return;
        String f[] = s.split(" ");
        conn.onAdcSndReceived(new Integer(f[3]), new Integer(f[4]));
    }

}
