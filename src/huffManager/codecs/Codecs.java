package huffManager.codecs;

import huffManager.codecs.exceptions.UnknownCodecException;
import huffManager.codecs.huffman.*;
import huffManager.codecs.none.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores the known codecs and allows getting the {@link Codec} class of known codec, by supplying
 * only the codec ID.
 * @author Jan Bryda
 */
public class Codecs {

    /**
     * Map that stores known codecs.
     */
    private static Map<Long, Codec> knownCodecs = new HashMap<>();

    static {
        Codec huffman = new HuffmanCodec();
        knownCodecs.put(huffman.getID(), huffman);

        Codec none = new NoneCodec();
        knownCodecs.put(none.getID(), none);
    }

    /**
     * This method returns the codec associated with given ID. In case where ID of unknown codec is given,
     * {@link UnknownCodecException} is thrown.
     * @param id unique ID of codec
     * @return the appropriate Codec class
     * @throws UnknownCodecException
     */
    public static Codec getCodecByID(long id) throws UnknownCodecException {
        if (knownCodecs.containsKey(id))
            return knownCodecs.get(id);
        else
            throw new UnknownCodecException("Codec with ID = " + id + " is unknown!");
    }

    /**
     * Gets the collection of all known codecs.
     * @return collection of known codecs
     */
    public static Collection<Codec> getKnownCodecs() {
        return knownCodecs.values();
    }
}
