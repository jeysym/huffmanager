package huffManager.archive;

import huffManager.archive.exceptions.*;
import huffManager.generator.*;
import static huffManager.archive.Archive.*;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides ability to save archive to output stream.
 * @author Jan Bryda
 */
public class ArchiveSaver {
    Map<ArchiveFile, FileInArchiveInfo> fileInfos;

    /**
     * Saves the given archive into given output stream.
     * @param archive archive to be saved
     * @param outputGenerator output stream, which the archive will be saved into
     * @throws ArchiveSavingException
     */
    public void save(Archive archive, Generator<OutputStream> outputGenerator) throws ArchiveSavingException {
        try {
            try (FlagOutputStream output =
                         new FlagOutputStream(
                                 new BufferedOutputStream(outputGenerator.generate(), BUFFER_SIZE),
                                 START_OF_HEADER, ESCAPE)) {
                List<ArchiveFile> allFiles = archive.getAllFiles();

                new DataOutputStream(output).writeLong(ARCHIVE_IDENTIFIER);

                fileInfos = new HashMap<>();
                long position = 8;
                for (ArchiveFile file : allFiles) {
                    InputStream fileStream = file.getCodedStream();
                    long offset = position;
                    long length = 0;

                    int c;
                    while ((c = fileStream.read()) != -1) {
                        output.write(c);
                        position++;
                        length++;
                    }

                    fileInfos.put(file, new FileInArchiveInfo(offset, length));
                }

                output.writeMarker();

                writeArchiveDescription(new DataOutputStream(output), archive);
            }
        } catch (IOException e) {
            throw new ArchiveSavingException("Archive saving : IO exception occurred!", e);
        } catch (UnableToGenerateException e) {
            throw new ArchiveSavingException("Archive saving : Unable to generate output stream!", e);
        } catch (UnableToGetStreamException e) {
            throw new ArchiveSavingException("Archive saving : Unable to get stream for coded file!", e);
        }
    }

    /**
     * Writes the description of whole archive (directories, files) into output stream.
     * @param dataOutput output stream, which the description will be written into
     * @param archive archive, that will be described
     * @throws IOException
     */
    private void writeArchiveDescription(DataOutputStream dataOutput, Archive archive) throws IOException {
        ArchiveDirectory rootDirectory = archive.getRootDirectory();

        for (ArchiveDirectory subdirectory : rootDirectory.getSubdirectories()){
            writeArchiveDirectory(dataOutput, subdirectory);
        }

        for (ArchiveFile file : rootDirectory.getFiles()) {
            writeArchiveFile(dataOutput, file);
        }
    }

    /**
     * Writes the description of one archive directory into output stream.
     * @param dataOutput output stream, which the description will be written into
     * @param directory directory that will be described
     * @throws IOException
     */
    private void writeArchiveDirectory(DataOutputStream dataOutput, ArchiveDirectory directory) throws IOException {
        dataOutput.writeByte(DIR_START);
        dataOutput.writeUTF(directory.getName());

        for (ArchiveDirectory subdirectory : directory.getSubdirectories()){
            writeArchiveDirectory(dataOutput, subdirectory);
        }

        for (ArchiveFile file : directory.getFiles()) {
            writeArchiveFile(dataOutput, file);
        }

        dataOutput.writeByte(DIR_END);
    }

    /**
     * Writes the description of one archive file into output stream.
     * @param dataOutput output stream, which the description will be written into
     * @param file file that will be described
     * @throws IOException
     */
    private void writeArchiveFile(DataOutputStream dataOutput, ArchiveFile file) throws IOException {
        dataOutput.writeByte(FILE);
        dataOutput.writeLong(file.getCodecID());
        dataOutput.writeLong(file.getSize());
        dataOutput.writeLong(fileInfos.get(file).offset);
        dataOutput.writeLong(fileInfos.get(file).length);
        dataOutput.writeUTF(file.getName());
    }
}

/**
 * Class that describes file that is stored in archive. It captures properties of file in archive like
 * offset at which it is stored and length of file in archive.
 */
class FileInArchiveInfo {
    /** offset at which the file is stored in the archive (in bytes) */
    public long offset;
    /** length of actual file data in archive (in bytes) */
    public long length;

    public FileInArchiveInfo(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }
}
