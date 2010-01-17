package hub;

import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class ConnectToMeHandler implements IDCEventHandler {

    private HubConnection hub;
    private IHubEventHandler handler;
    private final static byte[] cmd = "$ConnectToMe ".getBytes();

    public ConnectToMeHandler(HubConnection hub, IHubEventHandler handler) {
        this.handler = handler;
        this.hub = hub;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        if (!ArrayUtils.startsWith(data, cmd))
            return;
        String s = new String(data);
        String addr = s.split(" ")[2];
        String[] ip = addr.split(":");
        handler.onPeerConnectionRequested(hub, ip[0], Integer.parseInt(ip[1]));
    }

}
