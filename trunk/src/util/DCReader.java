package util;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class DCReader implements ISelectable {

    public interface IDCCommandHandler {
        byte[] getCommandPattern();
        void handleDCCommand(byte[] data, int start, int length) throws Exception;
    }

    public interface IDCDataHandler {
        void handleDCData(byte[] data, int start, int length) throws Exception;
    }

    private SocketChannel socketChannel;
    private List<IDCCommandHandler> commandHandlers = new ArrayList<IDCCommandHandler>();
    private List<IDCDataHandler> dataHandlers = new ArrayList<IDCDataHandler>();
    private int expectData = 0;
    private Buffer b = new Buffer();
    private static ByteBuffer bb = ByteBuffer.allocate(100 * 1024);

    public DCReader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void close() throws Exception {
    	socketChannel.close();
    }

    public void expect(int len) {
        expectData = len;
    }

    public SocketChannel getChannel() {
        return socketChannel;
    }

    private boolean readCommand() throws Exception {
        int ix = ArrayUtils.indexOf(b.data(), (byte) 0x7C, b.getOffset(), b.getSize()); // |
        if (ix != -1) {
            for (int i = 0; i < commandHandlers.size(); i++) {
                IDCCommandHandler handler = commandHandlers.get(i);
                if (ArrayUtils.startsWith(b.data(), b.getOffset(), ix - b.getOffset(), handler.getCommandPattern()))
                    handler.handleDCCommand(b.data(), b.getOffset(), ix - b.getOffset());
            }
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
                 dataHandlers.get(i).handleDCData(b.data(), b.getOffset(), len);
            b.markRead(len);
            return true;
        }
        return false;
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

    public void register(Selector selector) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    public void registerCommandHandler(IDCCommandHandler handler) {
        commandHandlers.add(handler);
    }

    public void registerDataHandler(IDCDataHandler handler) {
        dataHandlers.add(handler);
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
