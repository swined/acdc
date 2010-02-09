package hub;

import util.DCReader.IDCCommandHandler;

class ConnectToMeHandler implements IDCCommandHandler {

    private HubConnection hub;
    private IHubEventHandler handler;
    private final static byte[] cmd = "$ConnectToMe ".getBytes();

    public ConnectToMeHandler(HubConnection hub, IHubEventHandler handler) {
        this.handler = handler;
        this.hub = hub;
    }

    public byte[] getCommandPattern() {
        return cmd;
    }

    public void handleDCCommand(byte[] data, int start, int length) throws Exception {
        String s = new String(data, start, length);
        String addr = s.split(" ")[2];
        String[] ip = addr.split(":");
        handler.onPeerConnectionRequested(hub, ip[0], Integer.parseInt(ip[1]));
    }

}
