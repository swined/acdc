package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {

    public static byte[] append(byte[] a, byte[] b) {
        return append(a, b, b.length);
    }

    public static byte[] append(byte[] a, byte[] b, int l) {
        byte[] r = new byte[a.length + l];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        for (int i = 0; i < l; i++)
            r[i + a.length] = b[i];
        return r;
    }

    public static int indexOf(byte[] a, byte c) {
        return indexOf(a, c, 0, a.length);
    }

    public static int indexOf(byte[] a, byte c, int s, int l) {
        for (int i = s; i < s + l; i++)
            if (a[i] == c)
                return i;
        return -1;
    }

    public static byte[][] split(byte[] a, byte b) {
        return split(a, b, 0);
    }

    public static byte[][] split(byte[] a, byte b, int c) {
        List<byte[]> r = new ArrayList();
        byte[] t = a;
        while (indexOf(t, b) != -1) {
            if (c != 0 && r.size() >= c - 1) {
                r.add(t);
                break;
            }
            int ix = indexOf(t, b);
            byte[] s = Arrays.copyOfRange(t, 0, ix);
            t = Arrays.copyOfRange(t, ix + 1, t.length);
            r.add(s);
        }
        byte[][] br = new byte[r.size()][];
        for (int i = 0; i < r.size(); i++)
            br[i] = r.get(i);
        return br;
    }

}