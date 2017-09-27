package huffManager.generator;

/**
 * This interface describes classes that can produce something.
 * @author Jan Bryda
 */
public interface Producer<T> {
    T produce() throws Exception;
}
