package huffManager.codecs.huffman;

import huffManager.codecs.*;
import huffManager.codecs.exceptions.*;
import huffManager.generator.*;

import java.io.*;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * This class provides access to coding data streams with Huffman coding.
 * @author Jan Bryda
 */
public class HuffmanCoder extends Coder {
    private int blockSize = 10 * 1024 * 1024;

    /**
     * Thread that takes the input stream codes it using the huffman tree and writes the output to the
     * piped output stream.
     */
    private class HuffmanCodingThread extends Thread {
        InputStream input;
        PipedOutputStream output;
        HuffmanTree huffmanTree;
        boolean success;

        /**
         * Creates new HuffmanCodingThread that takes the input stream codes it according to the huffman
         * tree and writes the codec data into piped output stream.
         * @param huffmanTree huffman tree used to code data
         * @param input input to code
         * @param output output, into which coded data are written
         */
        HuffmanCodingThread(HuffmanTree huffmanTree, InputStream input, PipedOutputStream output) {
            this.input = new BufferedInputStream(input, blockSize);
            this.output = output;
            this.huffmanTree = huffmanTree;
        }

        /**
         * Gets the bit codes for each byte value. It uses the method
         * {@link HuffmanCodingThread#getCodes(HuffmanTree, BitCode, BitCode[])} to do so.
         * @param huffmanTree huffman tree
         * @return bit codes
         */
        private BitCode[] getCodes(HuffmanTree huffmanTree) {
            BitCode code = new BitCode();
            BitCode[] result = new BitCode[256];
            getCodes(huffmanTree, code, result);
            return result;
        }

        private void getCodes(HuffmanTree huffmanTree, BitCode code, BitCode[] result) {
            if (huffmanTree.isLeaf())
                result[huffmanTree.byteValue] = code;
            else {
                getCodes(huffmanTree.left, code.add(Bit.High), result);
                getCodes(huffmanTree.right, code.add(Bit.Low), result);
            }
        }

        /**
         * Codes the huffman tree into an array of longs.
         * @param huffmanTree huffman tree to code
         * @return coded tree
         */
        private long[] codeHuffmanTree(HuffmanTree huffmanTree) {
            if (huffmanTree.isLeaf())
                return new long[]{codeHuffmanLeaf(huffmanTree)};

            long[] result1 = codeHuffmanTree(huffmanTree.left);
            long[] result2 = codeHuffmanTree(huffmanTree.right);

            long[] result = new long[result1.length + result2.length + 1];
            System.arraycopy(result1, 0, result, 0, result1.length);
            System.arraycopy(result2, 0, result, result1.length, result2.length);
            long frequencyLower = huffmanTree.frequency & 0x007FFFFFFFFFFFFFL;
            result[result.length - 1] = frequencyLower << 8;
            return result;
        }

        /**
         * Codes one leaf node of a huffman tree into long.
         * @param leaf leaf node to code
         * @return long representation of leaf node
         */
        private long codeHuffmanLeaf(HuffmanTree leaf) {
            long result;

            // gets the lower 55 bits of the frequency value
            long frequencyLower = leaf.frequency & 0x007FFFFFFFFFFFFFL;
            int byteValue = leaf.byteValue;
            result = 0x8000000000000000L | frequencyLower << 8 | byteValue;
            return result;
        }

        public void run() {
            try (BitDataOutputStream outputStream = new BitDataOutputStream(output)) {
                long[] codedTree = codeHuffmanTree(huffmanTree);
                for (int i = 0; i < codedTree.length; i++)
                    outputStream.writeLong(codedTree[i]);

                outputStream.writeLong(0L);

                BitCode[] bitCodes = getCodes(huffmanTree);
                int c;
                while ((c = input.read()) != -1) {
                    Bit[] bits = bitCodes[c].bits;
                    for (int i = 0; i < bits.length; i++)
                        outputStream.writeBit(bits[i]);
                }

                success = true;
            } catch (IOException e) {
                success = false;
            }
        }
    }

    @Override
    public InputStream code(Generator<InputStream> inputGenerator) throws CoderException {
        try {
            InputStream input = inputGenerator.generate();
            HuffmanTree huffmanTree = null;

            try {
                if (input.available() == 0)
                    throw new CoderException("Huffman Coder : input stream is empty!");

                huffmanTree = constructHuffmanTree(input);
            } finally {
                input.close();
            }


            input = inputGenerator.generate();
            PipedInputStream pipeIn = new PipedInputStream(blockSize);
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
            HuffmanCodingThread codingThread = new HuffmanCodingThread(huffmanTree, input, pipeOut);
            codingThread.start();
            return pipeIn;
        } catch (IOException e) {
            throw new CoderException("Huffman Coder : an IO exception occurred!", e);
        } catch (UnableToGenerateException e) {
            throw new CoderException("Huffman Coder : unable to generate stream!", e);
        }
    }

    /**
     * Takes the input stream and constructs appropriate huffman tree.
     * @param input input stream
     * @return huffman tree for this input stream
     * @throws IOException
     */
    private HuffmanTree constructHuffmanTree(InputStream input) throws IOException {
        Comparator<HuffmanTree> huffmanTreeComparator = new Comparator<HuffmanTree>() {
            @Override
            public int compare(HuffmanTree tree1, HuffmanTree tree2) {
                int comp = Long.compare(tree1.frequency, tree2.frequency);
                if (comp != 0)
                    return comp;

                if (tree1.isLeaf()) {
                    if (tree2.isLeaf())
                        return Integer.compare(tree1.byteValue, tree2.byteValue);
                    else
                        return -1;
                } else {
                    return Long.compare(tree1.timeCreated, tree2.timeCreated);
                }
            }
        };

        long[] frequencies = getFrequenciesTable(input);
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<>(256, huffmanTreeComparator);

        for (int i = 0; i < frequencies.length; i++)
            if (frequencies[i] != 0)
                trees.add(new HuffmanTree(i, frequencies[i]));

        while (trees.size() != 1) {
            HuffmanTree tree1 = trees.remove();
            HuffmanTree tree2 = trees.remove();

            long newFrequency = tree1.frequency + tree2.frequency;
            trees.add(new HuffmanTree(tree1, null, newFrequency, tree2));
        }

        return trees.remove();
    }

    /**
     * Gets the byte frequencies table for this input.
     * @param input input stream
     * @return frequencies table
     * @throws IOException
     */
    private long[] getFrequenciesTable(InputStream input) throws IOException {
        BufferedInputStream bInput = new BufferedInputStream(input, blockSize);
        long[] frequencies = new long[256];

        int b;
        while ((b = bInput.read()) != -1) {
            frequencies[b & 0xFF]++;
        }

        return frequencies;
    }
}

/**
 * Enum describing bit values. Possible values are: {@link Bit#High} and {@link Bit#Low}.
 */
enum Bit {
    High, Low;

    /**
     * Gets the byte value of bit. High = 1. Low = 0.
     * @return byte value representing bit
     */
    public byte getValue() {
        switch (this) {
            case High:
                return 1;
            case Low:
                return 0;
        }
        return 0;
    }
}

/**
 * Represents the bit code that is used to code one byte value.
 */
class BitCode {
    public Bit[] bits = new Bit[0];

    /**
     * Extends the bit code with another specified bit.
     * @param bit
     * @return extended bit code
     */
    public BitCode add(Bit bit) {
        BitCode newBitCode = new BitCode();
        newBitCode.bits = new Bit[bits.length + 1];
        System.arraycopy(bits, 0, newBitCode.bits, 0, bits.length);
        newBitCode.bits[newBitCode.bits.length - 1] = bit;
        return newBitCode;
    }
}

/**
 * Output stream that gives the ability to write bit by bit.
 */
class BitDataOutputStream extends DataOutputStream {
    byte[] buffer = new byte[8];
    int bufferLength = 0;

    public BitDataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes bit to output.
     * @param bit bit to write
     * @throws IOException
     */
    public void writeBit(Bit bit) throws IOException {
        buffer[bufferLength++] = bit.getValue();

        if (bufferLength == 8)
            flushBuffer();
    }

    /**
     * Writes the byte that is constructed from bits that are stored in the bit buffer.
     * @throws IOException
     */
    public void flushBuffer() throws IOException {
        byte aByte = 0;
        for (int i = 0; i < 8; i++) {
            aByte |= buffer[i] << (7 - i);
        }
        write(aByte);
        bufferLength = 0;
    }

    /**
     * Closes the stream and if the last byte is unfinished it is augmented with zeroes and written out.
     * @throws IOException
     */
    public void close() throws IOException {
        while (bufferLength > 1 && bufferLength < 8)
            buffer[bufferLength++] = 0;

        flushBuffer();
        super.close();
    }
}