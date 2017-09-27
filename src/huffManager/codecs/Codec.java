package huffManager.codecs;

/**
 * This class describes the universal way to code and decode streams of data. It provides access to properties
 * as codec ID (long value, which identifies the codec) and codec name (just informative). Methods
 * {@link Codec#getCoder()} and {@link Codec#getDecoder()} can be used to get the appropriate coder/decoder
 * for this codec.
 * @author Jan Bryda
 */
public abstract class Codec {
    private long id;
    private String codecName;

    /**
     * Default Codec constructor, which sets the codec ID and codec name.
     * @param id unique ID of the codec
     * @param codecName name of the codec
     */
    public Codec(long id, String codecName) {
        this.id = id;
        this.codecName = codecName;
    }

    /**
     * Gets the respective Coder for the Codec.
     * @return appropriate Coder class
     */
    public abstract Coder getCoder();

    /**
     * Gets the respective Decoder for the Codec.
     * @return appropriate Decoder class
     */
    public abstract Decoder getDecoder();

    /**
     * Gets the unique ID of the Codec.
     * @return unique ID
     */
    public long getID() {
        return id;
    }

    /**
     * Gets the name of the Codec
     * @return codec name
     */
    public String getCodecName() {
        return codecName;
    }

    @Override
    public String toString() {
        return codecName;
    }
}
