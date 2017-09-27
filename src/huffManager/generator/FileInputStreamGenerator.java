package huffManager.generator;

import java.io.*;

/**
 * This class serves as a shorthand for creating {@link Generator} that generates {@link FileInputStream}
 * with specified file path.
 * @author Jan Bryda
 */
public class FileInputStreamGenerator extends Generator<InputStream> {

    /**
     * Creates new generator that will generate {@link FileInputStream} that will point to file in specified
     * path.
     * @param path path to file
     */
    public FileInputStreamGenerator(String path) {
        super(() -> new FileInputStream(path));
    }

    /**
     * Creates new generator that will generate {@link FileInputStream} that will point to specified file.
     * @param file input file
     */
    public FileInputStreamGenerator(File file) {
        super(() -> new FileInputStream(file));
    }
}
