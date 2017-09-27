package huffManager.archive;

import java.io.*;

/**
 * An {@link InputStream} that has two special byte values : {@link FlagInputStream#markerFlag},
 * {@link FlagInputStream#escapeFlag}. This stream has the ability to skip the stream after the occurrence of
 * marker flag. If the marker flag (or escape flag) appears in the data stream, it has to be escaped with
 * the escape flag to free it of its special meaning. In the end by reading from this stream, special flags
 * are not read, and the stream appears to have no special flags.
 * @author Jan Bryda
 */
public class FlagInputStream extends InputStream {
    InputStream input;
    /** byte value of marker flag used to mark a position in stream */
    byte markerFlag;
    /** byte value of escape flag used to escape bytes that have special meaning */
    byte escapeFlag;

    /**
     * Creates new FlagInputStream, with given input stream, marker flag and escape flag.
     * @param input input stream
     * @param markerFlag byte value of marker flag
     * @param escapeFlag byte value of escape flag
     */
    public FlagInputStream(InputStream input, byte markerFlag, byte escapeFlag) {
        this.input = input;
        this.markerFlag = markerFlag;
        this.escapeFlag = escapeFlag;
    }

    public int read() throws IOException {
        int myByte = input.read();

        if (myByte == escapeFlag)
            return input.read();

        while (myByte == markerFlag)
            myByte = input.read();

        return myByte;
    }

    /**
     * Skips the stream to the position of marker. Consecutive reading from this stream after calling this
     * method will start with the data that are right after the marker.
     * @throws IOException
     */
    public void skipAfterMarker() throws IOException {
        int myByte;
        boolean escape = false;

        while ((myByte = input.read()) != -1) {
            if (escape) {
                escape = false;
                continue;
            }

            if (myByte == escapeFlag)
                escape = true;

            if (myByte == markerFlag)
                return;
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
