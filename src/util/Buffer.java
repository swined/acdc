package util;

import java.nio.ByteBuffer;

public class Buffer {

    private final int block = 1024 * 1024;

    private int start = 0;
    private int length = 0;
    private byte[] data = new byte[0];

    public int getOffset() {
        return start;
    }

    public int getSize() {
        return length;
    }

    public byte[] data() {
        return data;
    }

    public byte[] read(int count, int skip) throws Exception {
        if (count + skip > length)
            throw new Exception("buffer underflow");
        byte[] r = new byte[count];
        for (int i = 0; i < count; i++)
            r[i] = data[start + i];
        length -= count + skip;
        start += count + skip;
        compact();
        return r;
    }

    public void write(ByteBuffer data, int len) {
        grow(len);
        for (int i = 0; i < len; i++)
            this.data[start + length + i] = data.array()[data.arrayOffset() + i];
        length += len;
    }

    private void grow(int size) {
        if (size + start + length > data.length) {
            byte[] r = new byte[data.length + size / block + block];
            for (int i = 0; i < data.length; i++)
                r[i] = data[i];
            data = r;
        }
    }

    private void compact() {
        if (start > block) {
            for (int i = 0; i < length; i++)
                data[i] = data[start + i];
            start = 0;
        }
    }

}
