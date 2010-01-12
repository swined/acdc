package hub;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import logger.ILogger;
import util.ArrayUtils;


class HubReader {

    private ILogger logger;
    private SocketChannel in;
    private byte[] buffer = new byte[0];
    private Set<IHubHandler> handlers = new HashSet();

    public HubReader(SocketChannel in, ILogger logger) {
        this.in = in;
        this.logger = logger;
    }

    private void readStream() throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        int r = in.read(bb);
        if (r <= 0)
            return;
        buffer = ArrayUtils.append(buffer, bb.array(), r);
    }

    private byte[] readCommand() throws Exception {
        readStream();
        int ix = ArrayUtils.indexOf(buffer, (byte)0x7C); // |
        if (ix != -1) {
            byte[] b = ArrayUtils.sub(buffer, 0, ix - 1);
            buffer = ArrayUtils.sub(buffer, ix + 1, buffer.length - 1);
            return b;
        }
        return null;
    }

    public void registerHandler(IHubHandler handler) {
        handlers.add(handler);
    }

    public void read() throws Exception {
        byte[] data = readCommand();
        if (data == null)
            return;
        logger.debug("received hub command: " + new String(data));
        for (IHubHandler handler : handlers)
            handler.handleHubCommand(data);
    }

}
