package HuffManager.codecs.exceptions;

/**
 * Created by jeysym on 23.5.16.
 */
public class DecoderException extends Exception {
    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
