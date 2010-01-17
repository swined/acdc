package hub;

import java.util.Arrays;
import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class LockHandler implements IDCEventHandler {

    private HubConnection mgr;
    private final static byte[] cmd = "$Lock ".getBytes();

    public LockHandler(HubConnection mgr) {
        this.mgr = mgr;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        String d = new String(data, start, length);
        String[] s = d.split(" ");
        if (s.length < 2) {
            return;
        }
        mgr.onHubConnected(s[1]);
    }

}
