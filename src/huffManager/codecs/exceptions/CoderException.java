package huffManager.codecs.exceptions;

/**
 * This exception should be thrown when Coder can't encode input stream.
 * @author Jan Bryda
 */
public class CoderException extends Exception {
    public CoderException(String message) {
        super(message);
    }

    public CoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
