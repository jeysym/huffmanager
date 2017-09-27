package huffManager.codecs;

import huffManager.codecs.exceptions.DecoderException;
import huffManager.generator.*;

import java.io.*;

/**
 * Decoder class provides the ability to decode given {@link InputStream}.
 * @author Jan Bryda
 */
public abstract class Decoder {

    /**
     * Takes {@link Generator} of {@link InputStream} and returns stream of decoded data, from which decoded
     * data can be read. {@link Generator} is used instead of just {@link InputStream}, because some codecs
     * may require to read input stream multiple times to decode it properly, and generator provides the way
     * to do so.
     * @param inputGenerator generator of input stream
     * @return decoded stream
     * @throws DecoderException
     */
    public abstract InputStream decode(Generator<InputStream> inputGenerator) throws DecoderException;
}
