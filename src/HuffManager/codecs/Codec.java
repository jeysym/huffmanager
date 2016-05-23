package HuffManager.codecs;

/**
 * Created by jeysym on 23.5.16.
 */
public abstract class Codec {
    private int id;
    private String codecName;

    public Codec(int id, String codecName) {
        this.id = id;
        this.codecName = codecName;
    }

    public abstract Coder getCoder();

    public abstract Decoder getDecoder();

    public int getID() {
        return id;
    }

    public String getCodecName() {
        return codecName;
    }
}
