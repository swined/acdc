package util;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class DCReader implements ISelectable {

    public interface IDCEventHandler {
        void handleDCEvent(byte[] data, int start, int length) throws Exception;
    }
    private SocketChannel socketChannel;
    private List<IDCEventHandler> commandHandlers = new ArrayList();
    private List<IDCEventHandler> dataHandlers = new ArrayList();
    private int expectData = 0;
    private Buffer b = new Buffer();
    private static ByteBuffer bb = ByteBuffer.allocate(100 * 1024);

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

    private boolean readCommand() throws Exception {
        int ix = ArrayUtils.indexOf(b.data(), (byte) 0x7C, b.getOffset(), b.getSize()); // |
        if (ix != -1) {
            for (int i = 0; i < commandHandlers.size(); i++)
                 commandHandlers.get(i).handleDCEvent(b.data(), b.getOffset(), ix - b.getOffset());
            b.markRead(ix - b.getOffset() + 1);
            return true;
        }
        return false;
    }

    private boolean readData() throws Exception {
        if (b.getSize() >= expectData) {
            int len = expectData;
            expectData = 0;
            for (int i = 0; i < dataHandlers.size(); i++)
                 dataHandlers.get(i).handleDCEvent(b.data(), b.getOffset(), len);
            b.markRead(len);
            return true;
        }
        return false;
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
                if (!readData())
                    return;
            } else {
                if (!readCommand())
                    return;
            }
        }
    }
}