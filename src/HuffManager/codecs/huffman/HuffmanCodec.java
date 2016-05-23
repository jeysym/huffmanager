package HuffManager.codecs.huffman;

import HuffManager.codecs.*;

/**
 * Created by jeysym on 23.5.16.
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
