package hub;

import util.ArrayUtils;

class SRHandler implements IHubHandler {

    private HubConnection hub;
    private IHubEventHandler handler;

    public SRHandler(HubConnection hub, IHubEventHandler handler) {
        this.hub = hub;
        this.handler = handler;
    }

    public void handleHubCommand(byte[] data) throws Exception {
        String s = new String(data);
        if (!s.startsWith("$SR"))
            return;
        byte[][] d = ArrayUtils.split(data, (byte)0x20, 3);
        byte[][] r = ArrayUtils.split(d[2], (byte)0x05);
        byte[][] x = ArrayUtils.split(r[1], (byte)0x20, 2);
        String info = new String(r[1]).split(" ", 2)[1];
        String[] slots = info.split("/", 2);
        int size = new Integer(new String(x[0]));
        handler.onSearchResult(hub, new SearchResult(new String(d[1]), r[0], size, new Integer(slots[0]), new Integer(slots[1])));
    }

}
