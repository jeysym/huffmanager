package huffManager.codecs;

import huffManager.codecs.exceptions.DecoderException;
import huffManager.generator.*;

import java.io.*;

/**
 * Created by jeysym on 23.5.16.
 */
public abstract class Decoder {
    public abstract InputStream decode(Generator<InputStream> input) throws DecoderException;
}
