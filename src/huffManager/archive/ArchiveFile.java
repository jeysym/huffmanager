package huffManager.archive;

import huffManager.archive.exceptions.UnableToGetStreamException;
import huffManager.codecs.exceptions.UnknownCodecException;
import huffManager.codecs.*;

import java.io.InputStream;

/**
 * Abstract class that describes file in an archive. File has to have specified these properties:
 * name, size and codec ID.
 * @author Jan Bryda
 */
public abstract class ArchiveFile {
    String name;
    long size;
    long codecID;

    /**
     * Default constructor that initializes the file with name, size and codec ID.
     * @param name file name
     * @param size file size (in bytes)
     * @param codecID codec ID
     */
    public ArchiveFile(String name, long size, long codecID) {
        this.name = name;
        this.size = size;
        this.codecID = codecID;
    }

    /**
     * Gets the name of the file.
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size of file.
     * @return size of file (in bytes)
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the codec ID of codec that is tied to this file.
     * @return codec ID
     */
    public long getCodecID() {
        return codecID;
    }

    /**
     * Gets the {@link Codec} class that is tied to this file, or throws {@link UnknownCodecException} if the
     * codec specified by the codec ID is unknown.
     * @return codec class
     * @throws UnknownCodecException
     */
    public Codec getCodec() throws UnknownCodecException {
        return Codecs.getCodecByID(codecID);
    }

    /**
     * Gets the stream of coded data. This stream should be used when saving the archive.
     * @return coded stream
     * @throws UnableToGetStreamException
     */
    public abstract InputStream getCodedStream() throws UnableToGetStreamException;

    /**
     * Gets the stream of decoded data. This stream should be used when extracting file from archive.
     * @return decoded stream
     * @throws UnableToGetStreamException
     */
    public abstract InputStream getDecodedStream() throws UnableToGetStreamException;

    @Override
    public String toString() {
        String codecName;
        try {
            codecName = getCodec().toString();
        } catch (UnknownCodecException e) {
            codecName = "unknown codec";
        }
        return "File[" + name + ", " + size + "B, " + codecName + "]";
    }
}
