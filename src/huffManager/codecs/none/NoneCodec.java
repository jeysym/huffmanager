package huffManager.codecs.none;

import huffManager.codecs.*;
import huffManager.codecs.exceptions.*;
import huffManager.generator.*;

import java.io.InputStream;

/**
 * This is codec, that doesn't code or decode data in any way. It is codec representation of not using codec
 * at all.
 * @author Jan Bryda
 */
public class NoneCodec extends Codec {

    public NoneCodec() {
        super(0x4E6F6E6500000000L, "No codec");
    }

    @Override
    public Coder getCoder() {
        return new Coder() {
            @Override
            public InputStream code(Generator<InputStream> inputGenerator) throws CoderException {
                try {
                    return inputGenerator.generate();
                } catch (UnableToGenerateException e) {
                    throw new CoderException("No codec Coder : Unable to generate stream!", e);
                }
            }
        };
    }

    @Override
    public Decoder getDecoder() {
        return new Decoder() {
            @Override
            public InputStream decode(Generator<InputStream> inputGenerator) throws DecoderException {
                try {
                    return inputGenerator.generate();
                } catch (UnableToGenerateException e) {
                    throw new DecoderException("No codec decoder : Unable to generate stream!", e);
                }
            }
        };
    }
}
