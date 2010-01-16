package util;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class DCReader {

    public interface IDCEventHandler {
        void handleDCEvent(byte[] data) throws Exception;
    }

    private SocketChannel in;
    private Set<IDCEventHandler> commandHandlers = new HashSet();
    private Set<IDCEventHandler> dataHandlers = new HashSet();
    private int expectData = 0;
    private Buffer b = new Buffer();
    private ByteBuffer bb = ByteBuffer.allocate(1024);

    public DCReader(SocketChannel in) {
        this.in = in;
    }

    private void readStream() throws Exception {
        bb.clear();
        int r = in.read(bb);
        if (0 < r)
            b.write(bb.array(), 0, r);
    }

    private byte[] readCommand() throws Exception {
        readStream();
        int ix = ArrayUtils.indexOf(b.data(), (byte)0x7C, b.getOffset(), b.getSize()); // |
        if (ix != -1)
            return b.read(ix - b.getOffset(), 1);
        return null;
    }

    private byte[] readData() throws Exception {
        readStream();
        if (b.getSize() >= expectData) {
            byte[] r = b.read(expectData, 0);
            expectData = 0;
            return r;
        }
        return null;
    }

    public void registerCommandHandler(IDCEventHandler handler) {
        commandHandlers.add(handler);
    }

    public void registerDataHandler(IDCEventHandler handler) {
        dataHandlers.add(handler);
    }

    public void expect(int len) {
        expectData = len;
    }

    public void read() throws Exception {
        if (expectData > 0) {
            byte[] data = readData();
            if (data == null)
                return;
            for (IDCEventHandler handler : dataHandlers)
                handler.handleDCEvent(data);
        } else {
            byte[] data = readCommand();
            if (data == null)
                return;
            for (IDCEventHandler handler : commandHandlers)
                handler.handleDCEvent(data);
        }
    }

}
