package huffManager.codecs;

import huffManager.codecs.exceptions.UnknownCodecException;
import huffManager.codecs.huffman.HuffmanCodec;
import huffManager.codecs.none.NoneCodec;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by jeysym on 23.5.16.
 */
public abstract class Codec {
    private long id;
    private String codecName;

    public Codec(long id, String codecName) {
        this.id = id;
        this.codecName = codecName;
    }

    public abstract Coder getCoder();

    public abstract Decoder getDecoder();

    public long getID() {
        return id;
    }

    public String getCodecName() {
        return codecName;
    }

}
