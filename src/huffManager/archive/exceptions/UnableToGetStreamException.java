package huffManager.archive.exceptions;

/**
 * Created by jeysym on 26.5.16.
 */
public class UnableToGetStreamException extends Exception {

    public UnableToGetStreamException(String message) {
        super(message);
    }

    public UnableToGetStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
