package util;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DCReader {

    public interface IDCEventHandler {
        void handleDCEvent(byte[] data) throws Exception;
    }

    private SocketChannel in;
    private byte[] buffer = new byte[0];
    private Set<IDCEventHandler> commandHandlers = new HashSet();
    private Set<IDCEventHandler> dataHandlers = new HashSet();
    private int expectData;
    private ByteBuffer bb = ByteBuffer.allocate(1024*1024);

    public DCReader(SocketChannel in) {
        this.in = in;
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
            byte[] b = Arrays.copyOfRange(buffer, 0, ix);
            buffer = Arrays.copyOfRange(buffer, ix + 1, buffer.length);
            return b;
        }
        return null;
    }

    private byte[] readData() throws Exception {
        readStream();
        if (buffer.length >= expectData) {
            byte[] b = Arrays.copyOfRange(buffer, 0, expectData);
            buffer = Arrays.copyOfRange(buffer, expectData, buffer.length);
            expectData = 0;
            return b;
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
        this.expectData = len;
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
