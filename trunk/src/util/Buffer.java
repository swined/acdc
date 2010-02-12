package util;

public class Buffer {

    private final int block = 1024 * 1024;

    private int start = 0;
    private int length = 0;
    private byte[] data = new byte[2 * block];

    private void compact() {
        if (start > length) {
            for (int i = 0; i < length; i++)
                data[i] = data[start + i];
            start = 0;
        }
    }

    public byte[] data() {
        return data;
    }

    public int getOffset() {
        return start;
    }

    public int getSize() {
        return length;
    }

    private void grow(int size) {
        if (size + start + length > data.length) {
            byte[] r = new byte[data.length + size / block + block];
            for (int i = 0; i < data.length; i++)
                r[i] = data[i];
            data = r;
        }
    }

    public void markRead(int count) throws Exception {
        if (count > length)
            throw new Exception("buffer underflow");
        length -= count;
        start += count;
        compact();
    }

    public void write(byte[] data, int offset, int len) {
        grow(len);
        for (int i = 0; i < len; i++)
            this.data[start + length + i] = data[offset + i];
        length += len;
    }

}
