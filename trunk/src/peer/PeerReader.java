package peer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import logger.ILogger;
import util.ArrayUtils;

class PeerReader {

    private ILogger logger;
    private SocketChannel in;
    private byte[] buffer = new byte[0];
    private Set<IPeerHandler> handlers = new HashSet();
    private int expectData;
    private ByteBuffer bb = ByteBuffer.allocate(1024*1024);

    public PeerReader(SocketChannel in, ILogger logger) {
        this.in = in;
        this.logger = logger;
        this.expectData = 0;
    }

    private void readStream() throws Exception {
        bb.clear();
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

    private byte[] readData() throws Exception {
        readStream();
        if (buffer.length >= expectData) {
            byte[] b = ArrayUtils.sub(buffer, 0, expectData - 1);
            buffer = ArrayUtils.sub(buffer, expectData, buffer.length - 1);
            expectData = 0;
            return b;
        }
        return null;
    }

    public void registerHandler(IPeerHandler handler) {
        handlers.add(handler);
    }

    public void expect(int len) {
        this.expectData = len;
    }

    public void read() throws Exception {
        if (expectData > 0) {
            byte[] data = readData();
            if (data == null)
                return;
            logger.debug("received " + data.length + " bytes");
            for (IPeerHandler handler : handlers)
                handler.handlePeerData(data);
        } else {
            byte[] data = readCommand();
            if (data == null)
                return;
            logger.debug("received peer command: " + new String(data));
            boolean handled = false;
            for (IPeerHandler handler : handlers)
                if (handler.handlePeerCommand(data))
                    handled = true;
            if (!handled)
                logger.warn("unhandled command : " + new String(data));
        }
    }

}
