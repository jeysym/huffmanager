package huffManager.archive;

import java.util.List;

/**
 * Describes the archive structure. Archive has one root directory. Every directory can contain other
 * subdirectories and files.
 * @author Jan Bryda
 */
public class Archive {
    ArchiveDirectory rootDirectory = new ArchiveDirectory("root_directory");

    /**
     * Gets the root directory of the archive.
     * @return the root directory of the archive
     */
    public ArchiveDirectory getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Sets the root directory of the archive.
     * @param directory new root directory
     */
    public void setRootDirectory(ArchiveDirectory directory) {
        this.rootDirectory = directory;
    }

    /**
     * Is used to get collection of all files in archive. Meaning all files in its entire directory structure.
     * @return collection of all files in the archive
     */
    public List<ArchiveFile> getAllFiles() {
        return rootDirectory.getAllFiles();
    }

    // static properties of archives that are used by ArchiveLoader/Saver classes

    /**
     * Defines the {@link Archive#ESCAPE} byte, that is used in archive files to escape the occurrences of
     * {@link Archive#ESCAPE} and {@link Archive#START_OF_HEADER} bytes in archive files.
     */
    static final byte ESCAPE = 0x1b;

    /**
     * Defines the {@link Archive#START_OF_HEADER} byte, that is used to mark the start of header (that
     * describes the structure in the archive) in archive.
     */
    static final byte START_OF_HEADER = 0x01; // is used to mark the beginning of the header in the archive file

    /**
     * This byte in archive description (header) marks the start of directory definition.
     */
    static final byte DIR_START = (byte)'D';

    /**
     * This byte in archive description (header) marks the end of directory definition.
     */
    static final byte DIR_END = (byte)'E';

    /**
     * This byte in archive description (header) marks the start of file definition.
     */
    static final byte FILE = (byte)'F';

    /**
     * This value in the beginning of file
     */
    static final long ARCHIVE_IDENTIFIER = 0x4152434849564500L;

    /**
     * Buffer size used when loading / saving archive.
     */
    static final int BUFFER_SIZE = 10*1024*1024;
}
