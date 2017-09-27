package huffManager.archive;

import huffManager.archive.exceptions.*;
import huffManager.generator.*;
import static huffManager.archive.Archive.*;

import java.io.*;
import java.util.*;

/**
 * This class provides the ability to load the {@link Archive} class from the data stream.
 * @author Jan Bryda
 */
public class ArchiveLoader {
    private Generator<InputStream> inputGenerator;

    /**
     * Takes the generator of input stream and loads an archive from this stream.
     * @param inputGenerator generator of input stream
     * @return loaded archive
     * @throws BadArchiveFormatException
     * @throws ArchiveLoadingException
     */
    public Archive load(Generator<InputStream> inputGenerator) throws BadArchiveFormatException, ArchiveLoadingException {
        this.inputGenerator = inputGenerator;

        try {
            DataInputStream input = new DataInputStream(inputGenerator.generate());
            if (input.readLong() != ARCHIVE_IDENTIFIER)
                throw new ArchiveLoadingException("Archive loading : file is not an archive!");

            Archive archive = null;
            FlagInputStream flagInput =
                    new FlagInputStream(
                            new BufferedInputStream(input, BUFFER_SIZE),
                            START_OF_HEADER, ESCAPE);

            flagInput.skipAfterMarker();

            try (DataInputStream dataInput = new DataInputStream(flagInput)) {
                archive = loadArchive(dataInput);
            }

            if (archive == null)
                throw new BadArchiveFormatException("Archive load : archive seems to have no header!");

            return archive;
        } catch (UnableToGenerateException e) {
            throw new ArchiveLoadingException("Archive load : unable to generate stream!", e);
        } catch (IOException e) {
            throw new ArchiveLoadingException("Archive load : IO exception occurred!", e);
        }
    }

    /**
     * Loads the archive from stream, supposing that the data that will be read from input stream is just
     * the archive header. Thus supposing that data part of archive was skipped.
     * @param dataInput input stream
     * @return loaded archive
     * @throws IOException
     * @throws BadArchiveFormatException
     */
    private Archive loadArchive(DataInputStream dataInput) throws IOException, BadArchiveFormatException {
        List<ArchiveFile> fileList = new ArrayList<>();
        Archive archive = new Archive();
        ArchiveDirectory currentDirectory = archive.getRootDirectory();

        int myByte;
        while ((myByte = dataInput.read()) != -1) {
            switch (myByte) {
                case FILE:
                    currentDirectory.addFile(loadArchiveFile(dataInput));
                    break;
                case DIR_START:
                    String directoryName = dataInput.readUTF();
                    ArchiveDirectory newDirectory = new ArchiveDirectory(directoryName);
                    currentDirectory.addSubdirectory(newDirectory);
                    currentDirectory = newDirectory;
                    break;
                case DIR_END:
                    currentDirectory = currentDirectory.getParent();
                    break;
                default:
                    throw new BadArchiveFormatException("Archive loader : unknown flag found!");
            }

            if (currentDirectory == null)
                throw new BadArchiveFormatException("Archive loader : archive has bad format : more directories closed than opened!");
        }

        if (currentDirectory.equals(archive.getRootDirectory()) == false)
            throw new BadArchiveFormatException("Archive loader : archive has bad format : unclosed directories definitions!");

        return archive;
    }

    /**
     * Loads the file from stream, supposing that the file description is the next thing that will be read
     * from the stream.
     * @param input input stream
     * @return archive file that was read
     * @throws IOException
     */
    private ArchiveFile loadArchiveFile(DataInputStream input) throws IOException {
        long codecID = input.readLong();
        long size = input.readLong();
        long offset = input.readLong();
        long length = input.readLong();
        String name = input.readUTF();

        FileInArchive file = new FileInArchive(inputGenerator, name, size, codecID, offset, length);
        return file;
    }
}
