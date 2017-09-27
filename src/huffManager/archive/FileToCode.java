package huffManager.archive;

import huffManager.archive.exceptions.*;
import huffManager.codecs.*;
import huffManager.codecs.exceptions.*;
import huffManager.generator.*;
import static huffManager.archive.Archive.*;

import java.io.*;

/**
 * This class represents file that is not yet stored in an archive but in some external location. It is therefore
 * ease to get decoded stream as the file itself has not yet been coded. If coded stream is required, appropriate
 * {@link Coder} must be invoked to code the stream.
 * @author Jan Bryda
 */
public class FileToCode extends ArchiveFile {
    Generator<InputStream> inputGenerator;

    /**
     * Creates new FileToCode with specified path to file and codec.
     * @param path path to file
     * @param codec codec that will be used to code the file
     */
    public FileToCode(String path, Codec codec) {
        this(new FileInputStreamGenerator(path),
                new File(path).getName(),
                new File(path).length(),
                codec.getID());
    }

    /**
     * Crates new FileToCode with specified input stream generator, file name, size and codec ID.
     * @param inputGenerator input stream generator
     * @param name file name
     * @param size file size
     * @param codecID ID of codec that will be used to code this file
     */
    public FileToCode(Generator<InputStream> inputGenerator, String name, long size, long codecID) {
        super(name, size, codecID);
        this.inputGenerator = inputGenerator;
    }

    @Override
    public InputStream getCodedStream() throws UnableToGetStreamException {
        try {
            Coder coder = Codecs.getCodecByID(codecID).getCoder();
            return new BufferedInputStream(
                    coder.code(inputGenerator),
                    BUFFER_SIZE);
        } catch (UnknownCodecException e) {
            throw new UnableToGetStreamException("Unable to get coded stream : unknown codec!", e);
        } catch (CoderException e) {
            throw new UnableToGetStreamException("Unable to get coded stream : coder exception occurred!", e);
        }
    }

    @Override
    public InputStream getDecodedStream() throws UnableToGetStreamException {
        try {
            return new BufferedInputStream(
                    inputGenerator.generate(),
                    BUFFER_SIZE);
        } catch (UnableToGenerateException e) {
            throw new UnableToGetStreamException("Unable to get decoded stream : failed to generate stream!", e);
        }
    }
}
