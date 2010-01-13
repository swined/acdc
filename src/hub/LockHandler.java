package hub;

import util.DCReader.IDCEventHandler;

class LockHandler implements IDCEventHandler {

    private HubConnection mgr;

    public LockHandler(HubConnection mgr) {
        this.mgr = mgr;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String d = new String(data);
        if (!d.startsWith("$Lock ")) {
            return;
        }
        String[] s = d.split(" ");
        if (s.length < 2) {
            return;
        }
        mgr.onHubConnected(s[1]);
    }

}
