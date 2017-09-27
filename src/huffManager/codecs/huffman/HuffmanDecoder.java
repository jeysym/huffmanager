package huffManager.codecs.huffman;

import huffManager.codecs.*;
import huffManager.codecs.exceptions.*;
import huffManager.generator.*;

import java.util.Stack;
import java.lang.Thread;

import java.io.*;
import java.util.*;

/**
 * Class that allows decoding the stream of huffman coded data.
 * @author Jan Bryda
 */
public class HuffmanDecoder extends Decoder {
    private int blockSize = 10 * 1024 * 1024;

    /**
     * Thread that reads the coded data from the input and puts decoded data into piped output stream.
     */
    private class HuffmanDecodingThread extends Thread {
        InputStream input;
        PipedOutputStream output;
        HuffmanTree huffmanTree;
        HuffmanTree positionInTree;

        /**
         * Crates new HuffmanDecodingThread.
         * @param huffmanTree huffman tree to be used while decoding
         * @param input input of coded data
         * @param output piped output stream to which decoded data will be put
         */
        public HuffmanDecodingThread(HuffmanTree huffmanTree, InputStream input, PipedOutputStream output) {
            this.huffmanTree = huffmanTree;
            this.input = input;
            this.output = output;
        }

        public void run() {
            try (BitDataInputStream inputStream = new BitDataInputStream(input);
                 PipedOutputStream outputStream = output) {
                long remainingBytes = huffmanTree.frequency;
                positionInTree = huffmanTree;

                int bit;
                while ((bit = inputStream.readBit()) != -1) {
                    if (bit == 1) {
                        positionInTree = positionInTree.left;
                    } else {
                        positionInTree = positionInTree.right;
                    }

                    if (positionInTree.isLeaf()) {
                        output.write(positionInTree.byteValue);
                        positionInTree = huffmanTree;
                        remainingBytes--;
                    }

                    if (remainingBytes == 0)
                        break;
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {

            }
        }
    }

    @Override
    public InputStream decode(Generator<InputStream> inputGenerator) throws DecoderException {
        try {
            InputStream input = inputGenerator.generate();
            HuffmanTree huffmanTree = readHuffmanTree(input);

            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream, blockSize);
            HuffmanDecodingThread decodingThread = new HuffmanDecodingThread(huffmanTree, input, pipedOutputStream);
            decodingThread.start();
            return pipedInputStream;
        } catch (IOException e) {
            throw new DecoderException("Huffman Decoder : an IO exception occurred!", e);
        } catch (UnableToGenerateException e) {
            throw new DecoderException("Huffman Decoder : unable to generate stream!", e);
        }
    }

    /**
     * Reads the huffman tree from the input stream.
     * @param input input stream
     * @return read huffman tree
     * @throws IOException
     * @throws DecoderException
     */
    private HuffmanTree readHuffmanTree(InputStream input) throws IOException, DecoderException {
        DataInputStream dataInputStream = new DataInputStream(input);
        List<Long> nodes = new ArrayList<Long>(256);

        long l;
        while ((l = dataInputStream.readLong()) != 0L) {
            if (l == -1)
                throw new DecoderException("Huffman decoder : reading huffman tree : unexpected end of file!");
            nodes.add(l);
        }

        Stack<HuffmanTree> trees = new Stack<>();
        try {
            for (long longValue : nodes) {
                if ((longValue >>> 63) == 1) {
                    trees.add(readLeaf(longValue));
                } else {
                    HuffmanTree tree1 = trees.pop();
                    HuffmanTree tree2 = trees.pop();

                    long frequency3 = readLeaf(longValue).frequency;
                    HuffmanTree tree3 = new HuffmanTree(tree2, null, frequency3, tree1);
                    trees.push(tree3);
                }
            }
        } catch (EmptyStackException e) {
            throw new DecoderException("Huffman decoder : reading huffman tree error!", e);
        }

        if (trees.size() != 1)
            throw new DecoderException("Huffman decoder : reading huffman tree error, inconsistent tree!");

        return trees.pop();
    }

    /**
     * Reads the leaf node of huffman tree from the long value.
     * @param longValue value that represents the leaf node of huffman tree
     * @return loaf of huffman tree
     */
    private HuffmanTree readLeaf(long longValue) {
        long frequency = (longValue >>> 8) & 0x007FFFFFFFFFFFFFL;
        int byteValue = (int)(longValue & 0xFFL);
        return new HuffmanTree(byteValue, frequency);
    }
}

/**
 * Stream that allows reading bit by bit.
 */
class BitDataInputStream extends DataInputStream {
    byte readByte;
    int position = 8;

    public BitDataInputStream(InputStream input) {
        super(input);
    }

    /**
     * Reads on bit. Either 0 or 1.
     * @return either 0 or 1
     * @throws IOException
     */
    int readBit() throws IOException {
        if (position == 8) {
            int x = read();
            if (x == -1)
                return -1;

            readByte = (byte) x;
            position = 0;
        }

        return (readByte >>> (7 - position++)) & 0x01;
    }
}
