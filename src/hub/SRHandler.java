package hub;

import java.util.Arrays;
import util.ArrayUtils;
import util.DCReader.IDCEventHandler;

class SRHandler implements IDCEventHandler {

    private HubConnection hub;
    private IHubEventHandler handler;
    private final static byte[] cmd = "$SR ".getBytes();

    public SRHandler(HubConnection hub, IHubEventHandler handler) {
        this.hub = hub;
        this.handler = handler;
    }

    public void handleDCEvent(byte[] data, int start, int length) throws Exception {
        if (!ArrayUtils.startsWith(data, start, length, cmd))
            return;
        byte[] t = Arrays.copyOfRange(data, start, start + length);
        byte[][] d = ArrayUtils.split(t, (byte)0x20, 3);
        byte[][] r = ArrayUtils.split(d[2], (byte)0x05);
        byte[][] x = ArrayUtils.split(r[1], (byte)0x20, 2);
        String info = new String(r[1]).split(" ", 2)[1];
        String[] slots = info.split("/", 2);
        int size = Integer.parseInt(new String(x[0]));
        final SearchResult sr = new SearchResult(new String(d[1]), r[0], size, new Integer(slots[0]), new Integer(slots[1]));
        handler.onSearchResult(hub,sr);
    }

}
