package HuffManager.codecs.huffman;

import HuffManager.codecs.*;
import HuffManager.codecs.exceptions.*;

import java.io.*;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Created by jeysym on 23.5.16.
 */
public class HuffmanCoder extends Coder {
    private int blockSize = 1024 * 1024;

    class HuffmanCodingThread extends Thread {
        InputStream input;
        PipedOutputStream output;
        HuffmanTree huffmanTree;
        boolean success;

        public HuffmanCodingThread(HuffmanTree huffmanTree, InputStream input, PipedOutputStream output) {
            this.input = new BufferedInputStream(input, blockSize);
            this.output = output;
            this.huffmanTree = huffmanTree;
        }

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

        private long codeHuffmanLeaf(HuffmanTree leaf) {
            long result;

            // gets the lower 55 bits of the frequency value
            long frequencyLower = leaf.frequency & 0x007FFFFFFFFFFFFFL;
            byte byteValue = leaf.byteValue;
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

    public InputStream code(InputStream input) throws CoderException {
        try {
            if (input.available() == 0)
                throw new CoderException("Huffman Coder : input stream is empty!");

            HuffmanTree huffmanTree = constructHuffmanTree(input);
            input.reset();

            PipedInputStream pipeIn = new PipedInputStream(blockSize);
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
            HuffmanCodingThread codingThread = new HuffmanCodingThread(huffmanTree, input, pipeOut);
            codingThread.start();
            return pipeIn;
        } catch (IOException e) {
            throw new CoderException("Huffman Coder : an IO exception occurred!");
        }
    }

    private HuffmanTree constructHuffmanTree(InputStream input) throws IOException {
        Comparator<HuffmanTree> huffmanTreeComparator = new Comparator<HuffmanTree>() {
            @Override
            public int compare(HuffmanTree tree1, HuffmanTree tree2) {
                int comp = Long.compare(tree1.frequency, tree2.frequency);
                if (comp != 0)
                    return comp;

                if (tree1.isLeaf()) {
                    if (tree2.isLeaf())
                        return Byte.compare(tree1.byteValue, tree2.byteValue);
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
                trees.add(new HuffmanTree((byte) i, frequencies[i]));

        while (trees.size() != 1) {
            HuffmanTree tree1 = trees.remove();
            HuffmanTree tree2 = trees.remove();

            long newFrequency = tree1.frequency + tree2.frequency;
            trees.add(new HuffmanTree(tree1, null, newFrequency, tree2));
        }

        return trees.remove();
    }

    private long[] getFrequenciesTable(InputStream input) throws IOException {
        BufferedInputStream bInput = new BufferedInputStream(input, blockSize);
        long[] frequencies = new long[256];

        int b;
        while ((b = bInput.read()) != -1) {
            frequencies[(byte) b]++;
        }

        return frequencies;
    }
}

enum Bit {
    High, Low;

    public byte getValue() {
        switch (this) {
            case High:
                return 1;
            case Low:
                return 0;
        }
        assert (false);
        return 0;
    }
}

class BitCode {
    public Bit[] bits;

    public BitCode add(Bit bit) {
        BitCode newBitCode = new BitCode();
        newBitCode.bits = new Bit[bits.length + 1];
        System.arraycopy(bits, 0, newBitCode.bits, 0, bits.length);
        newBitCode.bits[newBitCode.bits.length - 1] = bit;
        return newBitCode;
    }
}

class BitDataOutputStream extends DataOutputStream {
    byte[] buffer = new byte[8];
    int bufferLength = 0;

    public BitDataOutputStream(OutputStream out) {
        super(out);
    }

    public void writeBit(Bit bit) throws IOException {
        buffer[bufferLength++] = bit.getValue();

        if (bufferLength == 8)
            flushBuffer();
    }

    public void flushBuffer() throws IOException {
        byte aByte = 0;
        for (int i = 0; i < 8; i++) {
            aByte |= buffer[i] << (7 - i);
        }
        write(aByte);
        bufferLength = 0;
    }

    public void close() throws IOException {
        while (bufferLength > 1 && bufferLength < 8)
            buffer[bufferLength++] = 0;

        flushBuffer();
        super.close();
    }
}