package huffManager.codecs.huffman;

import huffManager.codecs.*;

/**
 * The Huffman coding codec class, which is used to get coder/decoder for encoding/decoding data streams
 * with the Huffman coding.
 * @author Jan Bryda
 */
public class HuffmanCodec extends Codec {

    public HuffmanCodec() {
        super(0x48554646, "Huffman");
    }

    public Coder getCoder() {
        return new HuffmanCoder();
    }

    public Decoder getDecoder() {
        return new HuffmanDecoder();
    }
}
