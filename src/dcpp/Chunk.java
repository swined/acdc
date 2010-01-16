package dcpp;

import java.util.Date;
import peer.PeerConnection;

public class Chunk {

    private final long start = new Date().getTime();
    private final int from;
    private final int len;
    private final PeerConnection peer;

    private byte[] data;

    public Chunk(PeerConnection peer, int from, int len) {
        this.peer = peer;
        this.from = from;
        this.len = len;
        this.data = null;
    }

    public PeerConnection getPeer() {
        return peer;
    }

    public byte[] getData() {
        return data;
    }

    public int getStart() {
        return from;
    }

    public int getLength() {
        return len;
    }

    public void setData(byte[] data) throws Exception {
        if (data.length != len)
            throw new Exception("bad buffer length");
        this.data = data;
    }

    @Override
    public String toString() {
        return "chunk(" + from + "-" + (from + len) + ((data == null) ? "" : ", " + data.length) + ")";
    }

    public long getCTime() {
        return start;
    }

}
