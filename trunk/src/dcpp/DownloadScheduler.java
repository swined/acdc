package dcpp;

import java.io.OutputStream;
import java.util.Arrays;

public class DownloadScheduler {

    private enum ChunkStatus { NONE, LOADING, READY };

    private final int chunkSize = 1024 * 1024;
    private final int maxChunks = 10 * 1024 * 1024 / chunkSize;
    private final long fileLength;

    private int committedChunks = 0;
    private byte[] buffer = new byte[maxChunks * chunkSize];
    private ChunkStatus[] status = new ChunkStatus[maxChunks];

    public DownloadScheduler(long fileLength) {
        this.fileLength = fileLength;
        Arrays.fill(status, ChunkStatus.NONE);
    }

    public long getChunk() {
        for (int i = 0; i < maxChunks; i++) {
            long offset = (i + committedChunks) * chunkSize;
            if (offset > fileLength)
                return -1;
            if (status[i] == ChunkStatus.NONE)
                return offset;
        }
        return -1;
    }

    private int getChunkByOffset(long offset) throws Exception {
        if (offset % chunkSize != 0)
            throw new Exception("chunk cannot start at " + offset);
        int chunk = (int)(offset / chunkSize - committedChunks);
        if (chunk < 0)
            throw new Exception("the chunk is already committed");
        return chunk;
    }

    public void markAsLoading(long offset) throws Exception {
        int chunk = getChunkByOffset(offset);
        if (status[chunk] != ChunkStatus.NONE)
            throw new Exception("the chunk cannot be loaded");
        status[chunk] = ChunkStatus.LOADING;
    }

    public int getChunkLength(long offset) {
        return (int)((offset + chunkSize > fileLength) ? (fileLength - offset) : chunkSize);
    }

    public void setData(long offset, byte[] data, int start, int length) throws Exception {
        if (length != getChunkLength(offset))
            throw new Exception("expected " + getChunkLength(offset) + " bytes, but got " + length + " instead");
        int chunk = getChunkByOffset(offset);
        if (status[chunk] != ChunkStatus.LOADING)
            throw new Exception("chunk is not loading");
        for (int i = 0; i < length; i++)
            buffer[chunk * chunkSize + i] = data[start + i];
        status[chunk] = ChunkStatus.READY;
    }

    private void shift() {
        for (int i = 0; i < maxChunks * chunkSize; i++)
            buffer[i] = buffer[(i + chunkSize) % (maxChunks * chunkSize)];
        ChunkStatus zero = status[0];
        for (int i = 0; i < maxChunks; i++)
            status[i] = status[(i + 1) % maxChunks];
        status[maxChunks - 1] = zero;
    }

    public void dump(OutputStream stream) throws Exception {
        while (status[0] == ChunkStatus.READY) {
            long offset = committedChunks * chunkSize;
            stream.write(buffer, 0, getChunkLength(offset));
            status[0] = ChunkStatus.NONE;
            committedChunks++;
            shift();
        }
    }

    public boolean isDone() {
        return committedChunks * chunkSize > fileLength;
    }

    public int totalChunks() {
        return (int)(fileLength / chunkSize + ((fileLength % chunkSize == 0) ? 0 : 1));
    }

    public int loadedChunks() {
        int r = 0;
        for (int i = 0; i < maxChunks; i++)
            if (status[i] == ChunkStatus.READY)
                r++;
        return r;
    }

    public double committedProgress() {
        return (double)(committedChunks) / (double)totalChunks();
    }

    public double totalProgress() {
        return (double)(committedChunks + loadedChunks()) / (double)totalChunks();
    }

}
