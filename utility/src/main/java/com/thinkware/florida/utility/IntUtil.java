package com.thinkware.florida.utility;

/**
 * int converting utils.
 */
public class IntUtil {

    public static int toInt16BeFrom2Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 2) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 0] & 0xFF) << 8)
                | ((data[offset + 1] & 0xFF) << 0);
        return value;
    }

    public static int toUInt16BeFrom2Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 2) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 0] & 0xFF) << 8)
                | ((data[offset + 1] & 0xFF) << 0);
        return value & 0xFFFF;
    }

    public static int toUInt24BeFrom3Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 3) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 0] & 0xFF) << 16)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 0);
        return value & 0xFFFF;
    }

    public static int toInt16From2Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 2) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 0] & 0xFF) << 0);
        return value;
    }

    public static int toUInt16From2Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 2) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 0] & 0xFF) << 0);
        return value & 0xFFFF;
    }

    public static int toUInt24From3Bytes(byte[] data, int offset) {
        if (data == null || data.length - offset < 3) {
            throw new IndexOutOfBoundsException();
        }
        int value = ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 0] & 0xFF) << 0);
        return value & 0xFFFF;
    }

    public static int toUInt(short x) {
        return ((int) x) & 0xffff;
    }

}
