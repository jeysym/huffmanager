package huffManager.codecs.huffman;

/**
 * Created by jeysym on 23.5.16.
 */
public class HuffmanTree {
    static long currentTime = 0;

    public HuffmanTree left;
    public HuffmanTree right;

    public long timeCreated;
    public Byte byteValue;
    public long frequency;

    public HuffmanTree(Byte byteValue, long frequency) {
        this(null, byteValue, frequency, null);
    }

    public HuffmanTree(HuffmanTree left, Byte byteValue, long frequency, HuffmanTree right) {
        this.left = left;
        this.timeCreated = currentTime++;
        this.byteValue = byteValue;
        this.frequency = frequency;
        this.right = right;
    }

    public boolean isLeaf() {
        return (byteValue != null);
    }
}