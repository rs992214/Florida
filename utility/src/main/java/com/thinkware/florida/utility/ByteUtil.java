package com.thinkware.florida.utility;

/**
 * byte converting util.
 */
public class ByteUtil {

    private static final String HEXES = "0123456789ABCDEF";

    public static String toHexString(byte[] ba) {
        if (ba == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(2 * ba.length);
        for (final byte b : ba) {
            sb.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }

        return sb.toString();
    }

    public static String toHexString(byte[] ba, int length) {
        if (ba == null || length <= 0) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(2 * length);
        int i = 0;
        for (final byte b : ba) {
            if (i >= length) {
                break;
            }
            sb.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
            i++;
        }

        return sb.toString();
    }

    public static String toHexString(byte[] ba, int index, int length) {
        if (ba == null || length <= 0 || index >= ba.length) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(2 * length);
        for (int i = index; i < (index + length); i++) {
            if (i >= (index + length)) {
                break;
            }
            sb.append(HEXES.charAt((ba[i] & 0xF0) >> 4)).append(HEXES.charAt((ba[i] & 0x0F)));
        }

        return sb.toString();
    }

    public static String toHexString(byte b) {
        final StringBuilder sb = new StringBuilder(2);
        sb.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        return sb.toString();
    }

    public static String toSingleDigitString(byte[] ba) {
        if (ba == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(ba.length);
        for (final byte b : ba) {
            sb.append(HEXES.charAt(b & 0x0F));
        }

        return sb.toString();
    }

    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }

        return ba;
    }

    // 0 ~ 9
    public static byte[] decimalToByteArray(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        byte[] ba = new byte[str.length()];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(str.substring(i, i + 1), 16);
        }

        return ba;
    }

}
