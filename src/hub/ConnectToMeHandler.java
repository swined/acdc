package hub;

import util.DCReader.IDCEventHandler;

class ConnectToMeHandler implements IDCEventHandler {

    private HubConnection hub;
    private IHubEventHandler handler;

    public ConnectToMeHandler(HubConnection hub, IHubEventHandler handler) {
        this.handler = handler;
        this.hub = hub;
    }

    public void handleDCEvent(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$ConnectToMe"))
            return;
        String addr = s.split(" ")[2];
        String[] ip = addr.split(":");
        handler.onPeerConnectionRequested(hub, ip[0], Integer.parseInt(ip[1]));
    }

}