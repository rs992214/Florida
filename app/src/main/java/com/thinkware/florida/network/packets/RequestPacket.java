package com.thinkware.florida.network.packets;

import android.text.TextUtils;

/**
 * Created by zic325 on 2016. 9. 6..
 */
public class RequestPacket {

    protected byte[] buffers;
    protected int offset;
    protected int messageType;

    public RequestPacket(int messageType) {
        this.messageType = messageType;
        this.buffers = new byte[Packets.getSize(messageType)];
        this.offset = 0;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] toBytes() {
        offset = 0;
        writeInt(messageType, 2);
        return buffers;
    }

    // Little Endian
    protected void writeInt(int value, int len) {
        switch (len) {
            case 1:
                buffers[offset + 0] = (byte) (value >>> 0);
                break;
            case 2:
                buffers[offset + 1] = (byte) (value >>> 8);
                buffers[offset + 0] = (byte) (value >>> 0);
                break;
            case 3:
                buffers[offset + 2] = (byte) (value >>> 16);
                buffers[offset + 1] = (byte) (value >>> 8);
                buffers[offset + 0] = (byte) (value >>> 0);
                break;
            case 4:
                buffers[offset + 3] = (byte) (value >>> 24);
                buffers[offset + 2] = (byte) (value >>> 16);
                buffers[offset + 1] = (byte) (value >>> 8);
                buffers[offset + 0] = (byte) (value >>> 0);
                break;
        }
        offset += len;
    }

    protected void writeFloat(float value, int len) {
        int floatValue =  Float.floatToIntBits(value);
        writeInt(floatValue, len);
    }

    protected void writeString(String value, int len) {
        if (!TextUtils.isEmpty(value)) {
            for (int i = 0, j = 0; i < buffers.length && j < len; i++, j++) {
                if (j < value.length()) {
                    buffers[offset + i] = (byte) value.charAt(j);
                } else {
                    buffers[offset + i] = (byte) 0x00;
                }
            }
        }
        offset += len;
    }

    /**
     * 시간형식이 년월일시분초(yyMMddHHmmss) 인 경우
     * 1byte 씩 끊어서 보내야 하므로 여기서 변형 하도록 한다
     * @param dateTime yyMMddHHmmss
     * @param len 6
     */
    protected void writeDateTime(String dateTime, int len) {
        if (len != 6) {
            throw new IllegalArgumentException("Date+Time format must be yyMMddHHmmss!");
        }
        writeInt(Integer.valueOf(dateTime.substring(0, 2)).intValue(), 1);
        writeInt(Integer.valueOf(dateTime.substring(2, 4)).intValue(), 1);
        writeInt(Integer.valueOf(dateTime.substring(4, 6)).intValue(), 1);
        writeInt(Integer.valueOf(dateTime.substring(6, 8)).intValue(), 1);
        writeInt(Integer.valueOf(dateTime.substring(8, 10)).intValue(), 1);
        writeInt(Integer.valueOf(dateTime.substring(10, 12)).intValue(), 1);
    }

}
