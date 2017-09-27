package huffManager.generator;

import java.io.*;

/**
 * This class serves as a shorthand for creating {@link Generator} that generates {@link FileOutputStream}
 * with specified file path.
 * @author Jan Bryda
 */
public class FileOutputStreamGenerator extends Generator<OutputStream> {

    /**
     * Creates new generator that will generate {@link FileOutputStream} that will point to file in specified
     * path.
     * @param path path to file
     */
    public FileOutputStreamGenerator(String path) {
        super(() -> new FileOutputStream(path));
    }

    /**
     * Creates new generator that will generate {@link FileOutputStream} that will point to specified file.
     * @param file output file
     */
    public FileOutputStreamGenerator(File file) {
        super(() -> new FileOutputStream(file));
    }
}
