package huffManager.generator;

/**
 * This exception should be thrown when generator wants to generate new object, but the producer class will
 * throw an exception instead of producing a new object.
 * @author Jan Bryda
 */
public class UnableToGenerateException extends Exception {

    public UnableToGenerateException(String message) {
        super(message);
    }

    public UnableToGenerateException(String message, Throwable cause) {
        super(message, cause);
    }
}
