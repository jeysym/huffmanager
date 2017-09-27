package huffManager.codecs.exceptions;

/**
 * This exception should be thrown when codec ID that matches no known codec is encountered.
 */
public class UnknownCodecException extends Exception {

    public UnknownCodecException(String message) {
        super(message);
    }

    public UnknownCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
