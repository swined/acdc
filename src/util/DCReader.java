package util;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class DCReader implements ISelectable {

    public interface IDCEventHandler {

        void handleDCEvent(byte[] data) throws Exception;
    }
    private SocketChannel socketChannel;
    private Set<IDCEventHandler> commandHandlers = new HashSet();
    private Set<IDCEventHandler> dataHandlers = new HashSet();
    private int expectData = 0;
    private Buffer b = new Buffer();
    private ByteBuffer bb = ByteBuffer.allocate(1024 * 1024);

    public DCReader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getChannel() {
        return socketChannel;
    }

    public void register(Selector selector) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    private void readStream() throws Exception {
        bb.clear();
        int r = socketChannel.read(bb);
        if (0 < r)
            b.write(bb.array(), 0, r);
        if (r == 0)
            throw new Exception("nothing to read");
        if (r < 0) {
            socketChannel.close();
            throw new Exception("read failed");
        }
    }

    private byte[] readCommand() throws Exception {
        int ix = ArrayUtils.indexOf(b.data(), (byte) 0x7C, b.getOffset(), b.getSize()); // |
        if (ix != -1) {
            return b.read(ix - b.getOffset(), 1);
        }
        return null;
    }

    private byte[] readData() throws Exception {
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

    public void update() throws Exception {
        readStream();
        while (true) {
            if (expectData > 0) {
                byte[] data = readData();
                if (data == null) {
                    return;
                }
                for (IDCEventHandler handler : dataHandlers) {
                    handler.handleDCEvent(data);
                }
            } else {
                byte[] data = readCommand();
                if (data == null) {
                    return;
                }
                for (IDCEventHandler handler : commandHandlers) {
                    handler.handleDCEvent(data);
                }
            }
        }
    }
}
