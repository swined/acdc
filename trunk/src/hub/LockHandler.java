package hub;

import util.DCReader.IDCCommandHandler;

class LockHandler implements IDCCommandHandler {

    private HubConnection mgr;
    private final static byte[] cmd = "$Lock ".getBytes();

    public LockHandler(HubConnection mgr) {
        this.mgr = mgr;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String d = new String(data, start, length);
        String[] s = d.split(" ");
        if (s.length < 2) {
            return;
        }
        mgr.onHubConnected(s[1]);
    }

}
