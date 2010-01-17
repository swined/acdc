package dcpp;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;
import util.DCReader;

public class DownloadManager {

    private final int selectTimeout = 10 * 1000;
    private Selector selector = Selector.open();

    public DownloadManager() throws Exception {
    }

    private void select() throws Exception {
        if (selector.select(selectTimeout) > 0) {
            try {
                for (SelectionKey k : selector.selectedKeys()) {
                    ((DCReader)k.attachment()).update();
                }
            } finally {
                selector.selectedKeys().clear();
            }
        }
    }

    public void download(String host, int port) throws Exception {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
        new DCReader(channel).register(selector);
        while (true) {
            select();
        }
    }

    private String generateNick() {
        Random rand = new Random();
        byte[] bytes = new byte[8];
        rand.nextBytes(bytes);
        String r = "";
        for (byte b : bytes) {
            r += Integer.toHexString(b > 0 ? b : b + 0xFF);
        }
        return r;
    }

}
