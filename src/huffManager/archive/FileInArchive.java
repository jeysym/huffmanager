package huffManager.archive;

import huffManager.archive.exceptions.UnableToGetStreamException;
import huffManager.codecs.*;
import huffManager.codecs.exceptions.*;
import huffManager.generator.*;
import static huffManager.archive.Archive.*;

import java.io.*;

/**
 * Describes file that is saved in an existing archive. It is therefore easy to get the coded stream, because
 * the file in the archive is already coded. On the other hand getting the decoded stream requires decoding
 * the data stored in the archive.
 * @author Jan Bryda
 */
public class FileInArchive extends ArchiveFile {
    private Generator<InputStream> inputGenerator;
    private long offset;
    private long length;

    /**
     * Crates new FileInArchive.
     * @param inputGenerator generator of input stream
     * @param name file name
     * @param size size of file
     * @param codecID codec ID of codec that codes this file
     * @param offset offset of file data in archive
     * @param length length of file data in archive
     */
    public FileInArchive(Generator<InputStream> inputGenerator, String name, long size, long codecID, long offset, long length) {
        super(name, size, codecID);
        this.inputGenerator = inputGenerator;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public InputStream getCodedStream() throws UnableToGetStreamException {
        try {
            return new SegmentInputStream(
                    new FlagInputStream(
                            new BufferedInputStream(
                                    inputGenerator.generate(),
                                    BUFFER_SIZE),
                            START_OF_HEADER, ESCAPE)
                    , offset, length);
        } catch (UnableToGenerateException e) {
            throw new UnableToGetStreamException("Unable to get coded stream : could not generate stream!", e);
        }
    }

    @Override
    public InputStream getDecodedStream() throws UnableToGetStreamException {
        try {
            Codec codec = getCodec();
            Generator<InputStream> generator =
                    new Generator<>(() ->
                            new SegmentInputStream(
                                new FlagInputStream(
                                        new BufferedInputStream(
                                                inputGenerator.generate(),
                                                BUFFER_SIZE),
                                        START_OF_HEADER, ESCAPE),
                            offset, length));
            return codec.getDecoder().decode(generator);
        } catch (UnknownCodecException e) {
            throw new UnableToGetStreamException("Unable to get decoded stream : unknown codec!", e);
        } catch (DecoderException e) {
            throw new UnableToGetStreamException("Unable to get decoded stream : decoder exception occurred!", e);
        }
    }
}

/**
 * This class serves as an {@link InputStream} that does not read the stream from beginning, but reads the
 * given segment of stream specified with {@link SegmentInputStream#offset} from beginning of file and
 * {@link SegmentInputStream#length} of the segment that should be read.
 * @author Jan Bryda
 */
class SegmentInputStream extends InputStream {
    InputStream input;
    private long offset, length, remainingBytes;

    /**
     * Creates new SegmentInputStream from given input stream, offset and length value.
     * @param input input stream
     * @param offset offset (in bytes) from beginning of stream
     * @param length length (in bytes) of segment that should be read
     */
    public SegmentInputStream(InputStream input, long offset, long length) {
        this.input = input;
        this.offset = offset;
        this.length = length;

        remainingBytes = length;
        try {
            input.skip(offset);
        } catch (IOException e) {
            remainingBytes = 0;
        }
    }

    public int read() throws IOException {
        if (remainingBytes == 0)
            return -1;

        remainingBytes--;
        return input.read();
    }

}