package huffManager.codecs.exceptions;

/**
 * This exception should be thrown when decoder can't decode input stream.
 * @author Jan Bryda
 */
public class DecoderException extends Exception {
    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
