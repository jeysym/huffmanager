package HuffManager.codecs.exceptions;

/**
 * Created by jeysym on 23.5.16.
 */
public class CoderException extends Exception {
    public CoderException(String message) {
        super(message);
    }

    public CoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
