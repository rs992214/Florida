package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 공지사항 (GT-1114) 293 Byte
 * Server -> MDT
 */
public class NoticesPacket extends ResponsePacket {

    private int carId; // car ID (2)
    private int noticeCode; // 공지코드 (2)
    private String noticeTitle; // 공지제목 (31)
    private String notice; // 공지본문 (256)

    public NoticesPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public int getNoticeCode() {
        return noticeCode;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public String getNotice() {
        return notice;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        noticeCode = readInt(2);
        noticeTitle = readString(31);
        notice = readString(256);
    }

    @Override
    public String toString() {
        return "공지사항 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", noticeCode=" + noticeCode +
                ", noticeTitle='" + noticeTitle + '\'' +
                ", notice='" + notice + '\'';
    }
}