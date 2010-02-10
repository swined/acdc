package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {

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

    public static boolean startsWith(byte[] a, byte[] s) {
        return startsWith(a, 0, a.length, s);
    }

    public static boolean startsWith(byte[] a, int start, int length, byte[] s) {
        if (s.length > length)
            return false;
        for (int i = 0; i < s.length; i++)
            if (a[start + i] != s[i])
                return false;
        return true;
    }

    public static byte[][] split(byte[] a, byte b, int c) {
        List<byte[]> r = new ArrayList<byte[]>();
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
