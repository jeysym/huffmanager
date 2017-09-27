package huffManager.archive.exceptions;

/**
 * Created by jeysym on 26.5.16.
 */
public class ArchiveLoadingException extends Exception {

    public ArchiveLoadingException(String message) {
        super(message);
    }

    public ArchiveLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
