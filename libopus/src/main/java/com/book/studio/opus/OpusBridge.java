package com.book.studio.opus;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OpusBridge {

    // Used to load the 'opuslib' library on application startup.
    static {
        System.loadLibrary("opus");
    }

    /**
     * A native method that is implemented by the 'opuslib' native library,
     * which is packaged with this application.
     */

    public static native long createEncoder(int sampleRate, int channelConfig, int complexity);

    public static native long createDecoder(int sampleRateInHz, int channelConfig);

    public static native int encode(long handle, short[] shortArray, int offset, byte[] encoded);

    public static native int decode(long handle, byte[] encodeByte, short[] shortArray);

    public static native void destroyEncoder(long handle);

    public static native void destroyDecoder(long handle);


    public static short[] toShortArray(byte[] byteArray) {
        int count = byteArray.length >> 1;
        short[] shortArray = new short[count];
        ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder()).asShortBuffer().get(shortArray);
        return shortArray;
    }

    public static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] & 0xFF);
            dest[i * 2 + 1] = (byte) ((src[i] >> 8) & 0xFF);
        }
        return dest;
    }
}