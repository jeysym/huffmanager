package huffManager.archive.exceptions;

/**
 * Created by jeysym on 26.5.16.
 */
public class ArchiveSavingException extends Exception {

    public ArchiveSavingException(String message) {
        super(message);
    }

    public ArchiveSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}
