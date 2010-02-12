package util;

public class KeyGenerator {

    private static String dcnEncode(String lockstring) {
        for (int i : new int[]{0, 5, 36, 96, 124, 126}) {
            String paddedDecimal = String.format("%03d", i);
            String paddedHex = String.format("%02x", i);
            lockstring = lockstring.replaceAll("\\x" + paddedHex, "/%DCN" + paddedDecimal + "%/");
        }
        return lockstring;
    }

    public static String generateKey(byte[] lock) {
        byte[] key = new byte[lock.length];
        for (int i = 1; i < lock.length; i++) {
            key[i] = (byte) ((lock[i] ^ lock[i - 1]) & 0xFF);
        }
        key[0] = (byte) ((((lock[0] ^ lock[lock.length - 1]) ^ lock[lock.length - 2]) ^ 5) & 0xFF);
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) ((((key[i] << 4) & 0xF0) | ((key[i] >> 4) & 0x0F)) & 0xFF);
        }
        return dcnEncode(new String(key));

    }

}
