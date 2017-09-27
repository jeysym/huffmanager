package huffManager.archive;

import java.util.List;
import java.util.ArrayList;

/**
 * This class represents a directory in the archive.
 * @author Jan Bryda
 */
public class ArchiveDirectory {
    String name;
    /** parent directory of this directory */
    ArchiveDirectory parent = null;
    /** files in this directory */
    List<ArchiveFile> files = new ArrayList<>();
    /** subdirectories in this directory */
    List<ArchiveDirectory> subdirectories = new ArrayList<>();

    /**
     * Crates a new archive directory with specified name and unspecified parent directory.
     * @param name name of the directory
     */
    public ArchiveDirectory(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the directory.
     * @return name of the directory.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the directory.
     * @param value the name to be set
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Adds file to the archive directory.
     * @param file file to be added
     */
    public void addFile(ArchiveFile file) {
        files.add(file);
    }

    /**
     * Removes the file from the archive directory.
     * @param file file to be removed
     */
    public void removeFile(ArchiveFile file) {
        files.remove(file);
    }

    /**
     * Adds directory as a subdirectory into this directory. This directory will be registered as a parent
     * directory for the new subdirectory.
     * @param directory directory to be added as a subdirectory
     */
    public void addSubdirectory(ArchiveDirectory directory) {
        subdirectories.add(directory);
        directory.parent = this;
    }

    /**
     * Removes target subdirectory from this directory.
     * @param directory directory to be removed
     */
    public void removeSubdirectory(ArchiveDirectory directory) {
        subdirectories.remove(directory);
    }

    /**
     * Gets the collection of all subdirectories of this directory.
     * @return collection of all subdirectories
     */
    public List<ArchiveDirectory> getSubdirectories() {
        return new ArrayList<ArchiveDirectory>(subdirectories);
    }

    /**
     * Gets collection of all files in this directory.
     * @return collection of files in this directory
     */
    public List<ArchiveFile> getFiles() {
        return new ArrayList<ArchiveFile>(files);
    }

    /**
     * Gets collection of all files in this directory and its subdirectories.
     * @return collection of all files contained in this directory
     */
    public List<ArchiveFile> getAllFiles() {
        List<ArchiveFile> allFiles = new ArrayList<ArchiveFile>(files);
        for (ArchiveDirectory subdir : subdirectories) {
            allFiles.addAll(subdir.getAllFiles());
        }
        return allFiles;
    }

    /**
     * Gets the parent directory of this directory.
     * @return parent directory
     */
    public ArchiveDirectory getParent() {
        return parent;
    }

    /**
     * Gets the total size of this directory, which is the sum of sizes of all files it contains.
     * @return total size of this directory
     */
    public long getSize() {
        long size = 0;
        for (ArchiveDirectory subDir : subdirectories) {
            size += subDir.getSize();
        }

        for (ArchiveFile file : files) {
            size += file.getSize();
        }

        return size;
    }
}
