package huffManager.codecs;

import huffManager.codecs.exceptions.CoderException;
import huffManager.generator.*;

import java.io.*;

/**
 * Coder class provides the ability to code given {@link InputStream}.
 * @author Jan Bryda
 */
public abstract class Coder {

    /**
     * Takes {@link Generator} that generates {@link InputStream} and returns coded stream from which
     * coded data can be read. {@link Generator} is passed instead of regular {@link InputStream}, because
     * many coding algorithms need to read input stream multiple times, and generator provides the way to do
     * so.
     * @param inputGenerator generator of input stream
     * @return coded data stream
     * @throws CoderException
     */
    public abstract InputStream code(Generator<InputStream> inputGenerator) throws CoderException;
}
