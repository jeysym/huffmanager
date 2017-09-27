package huffManager.codecs.huffman;

/**
 * This class represents the Huffman tree, that captures separate bytes in file and their frequencies.
 * @author Jan Bryda
 */
public class HuffmanTree {
    static long currentTime = 0;

    public HuffmanTree left;
    public HuffmanTree right;

    public long timeCreated;
    public Integer byteValue;
    public long frequency;

    /**
     * Creates the new huffman tree leaf node with specified byte value and frequency.
     * @param byteValue byte value
     * @param frequency the frequency of that byte value
     */
    public HuffmanTree(Integer byteValue, long frequency) {
        this(null, byteValue, frequency, null);
    }

    /**
     * Crates new huffman tree with specified son trees and the byte value and frequency in the root node.
     * @param left left son
     * @param byteValue byte value
     * @param frequency frequency of that byte value
     * @param right right son
     */
    public HuffmanTree(HuffmanTree left, Integer byteValue, long frequency, HuffmanTree right) {
        this.left = left;
        this.timeCreated = currentTime++;
        this.byteValue = byteValue;
        this.frequency = frequency;
        this.right = right;
    }

    /**
     * Returns true if this huffman tree is leaf node.
     * @return true / false
     */
    public boolean isLeaf() {
        return (byteValue != null);
    }
}