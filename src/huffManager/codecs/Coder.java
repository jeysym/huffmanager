package huffManager.codecs;

import huffManager.codecs.exceptions.CoderException;
import huffManager.generator.*;

import java.io.*;

/**
 * Created by jeysym on 23.5.16.
 */
public abstract class Coder {
    public abstract InputStream code(Generator<InputStream> input) throws CoderException;
}
