package cecs429.text;

import java.util.*;

public class ByteEncore {

    public static byte[] numberToByteArray(int n) {
        List bytes = new ArrayList<Byte>();
        byte[] rBytes;
        for (;;) {
            bytes.add((byte)(n % 128));
            if (n < 128)
                break;
            n /= 128;
        }
        rBytes = toArray(bytes);
        rBytes[rBytes.length - 1] += 128;
        return rBytes;
    }

    public static byte[] toArray(List<Byte> aBytes) {
        byte[] r = new byte[aBytes.size()];
        for (int i = 0; i < aBytes.size(); i++) {
            r[i] = aBytes.get(i);
        }
        return r;
    }

    public static int byteArrayToInt(byte[] bytes) {
        
    }

}