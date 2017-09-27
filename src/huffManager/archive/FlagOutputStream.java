package huffManager.archive;

import java.io.*;

/**
 * An {@link OutputStream} that has two special byte values : {@link FlagOutputStream#markerFlag},
 * {@link FlagOutputStream#escapeFlag}. In addition of writing this stream provides method
 * {@link FlagOutputStream#writeMarker()} that writes the marker in the output stream marking that
 * position. Bytes that need to be escaped when written are escaped automatically.
 * @author Jan Bryda
 */
public class FlagOutputStream extends OutputStream {
    OutputStream output;
    /** byte value of marker flag used to mark a position in stream */
    byte markerFlag;
    /** byte value of escape flag used to escape bytes that have special meaning */
    byte escapeFlag;

    /**
     * Crates new FlagOutputStream with specified output stream, marker flag and escape flag.
     * @param output output stream
     * @param markerFlag byte value of marker flag
     * @param escapeFlag byte value of escape flag
     */
    public FlagOutputStream(OutputStream output, byte markerFlag, byte escapeFlag) {
        this.output = output;
        this.markerFlag = markerFlag;
        this.escapeFlag = escapeFlag;
    }

    public void write(int myByte) throws IOException {
        if (myByte == escapeFlag || myByte == markerFlag) {
            output.write(escapeFlag);
            output.write(myByte);
        } else {
            output.write(myByte);
        }
    }

    /**
     * Writes marker in this stream. When reading that stream with {@link FlagInputStream} method
     * {@link FlagInputStream#skipAfterMarker()} will jump after this marker.
     * @throws IOException
     */
    public void writeMarker() throws IOException {
        output.write(markerFlag);
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
