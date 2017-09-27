package huffManager.generator;

/**
 * Generator class provides way to store the process of producing some object and then generating it multiple
 * times through the invocation of {@link Generator#generate()} method.
 * @author Jan Bryda
 */
public class Generator<T> {
    Producer<T> producer;

    /**
     * Crates a new generator. Parameter specifies the way, how to construct given object.
     * @param producer specifies the way how to produce given object
     */
    public Generator(Producer<T> producer) {
        this.producer = producer;
    }

    /**
     * Generates new object in the way it was specified during generator construction.
     * @return new generated object
     * @throws UnableToGenerateException
     */
    public T generate() throws UnableToGenerateException {
        try {
            return producer.produce();
        } catch (Exception e) {
            throw new UnableToGenerateException("Generation failed!", e);
        }
    }
}
