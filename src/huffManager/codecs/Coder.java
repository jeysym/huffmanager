package huffManager.codecs;

import huffManager.codecs.exceptions.CoderException;

import java.io.*;

/**
 * Created by jeysym on 23.5.16.
 */
public abstract class Coder {
    public abstract InputStream code(InputStream input) throws CoderException;
}
