package huffManager.archive.exceptions;

/**
 * Created by jeysym on 26.5.16.
 */
public class BadArchiveFormatException extends Exception {

    public BadArchiveFormatException(String message) {
        super(message);
    }

    public BadArchiveFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
