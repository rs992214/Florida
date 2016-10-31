package com.thinkware.florida.network.packets;

import com.thinkware.florida.network.packets.server2mdt.CallerInfoResendPacket;
import com.thinkware.florida.network.packets.server2mdt.CancelEmergencyPacket;
import com.thinkware.florida.network.packets.server2mdt.NoticesPacket;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoProcPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseAccountPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseAckPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseMessagePacket;
import com.thinkware.florida.network.packets.server2mdt.ResponsePeriodSendingPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseRestPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseServiceReportPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitCancelPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.ServiceConfigPacket;
import com.thinkware.florida.network.packets.server2mdt.ServiceRequestResultPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitOrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitPlaceInfoPacket;
import com.thinkware.florida.utility.log.LogHelper;

/**
 * Created by zic325 on 2016. 9. 7..
 */
public class ResponsePacket {

    protected int messageType;
    protected byte[] buffers;
    protected int offset;

    public ResponsePacket(byte[] bytes) {
        this.offset = 0;
        parse(bytes);
    }

    public void parse(byte[] buffers) {
        this.buffers = buffers;
        messageType = readInt(2);
    }

    public int getMessageType() {
        return messageType;
    }

    // Little Endian
    public int readInt(int length) {
        if (isValid(length)) {
            int ret = 0;
            switch (length) {
                case 1:
                    ret = buffers[offset] & 0x000000FF;
                    break;
                case 2:
                    ret = (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
                case 3:
                    ret = (buffers[offset + 2] & 0x000000FF) << 16;
                    ret += (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
                case 4:
                    ret = (buffers[offset + 3] & 0x000000FF) << 24;
                    ret += (buffers[offset + 2] & 0x000000FF) << 16;
                    ret += (buffers[offset + 1] & 0x000000FF) << 8;
                    ret += (buffers[offset] & 0x000000FF) << 0;
                    break;
            }

            offset += length;
            return ret;
        } else {
            LogHelper.d(">> ResponsePacket.readInt() ERROR -> buffer:"
                    + ((buffers == null) ? "null" : buffers.length)
                    + " offset:" + offset + " length:" + length);
            return 0;
        }
    }

    public float readFloat(int length) {
        int bits = readInt(length);
        return Float.intBitsToFloat(bits);
    }

    public String readString(int length) {
        if (isValid(length)) {
            String str = "Encoding Fail(EUC-KR)";
            try {
                str = new String(buffers, offset, length, "EUC-KR");
            } catch (Exception e) {
                e.printStackTrace();
            }
            offset += length;

            // byte 중에 쓰레기값이 들어오는 케이스가 있어 예외처리 추가 한다.
            char szGarbage = 0xFFFD;
            str = str.replace('\r', ' ');
            str = str.replace(szGarbage, ' ');

            return str.trim();
        } else {
            LogHelper.d(">> ResponsePacket.readString() ERROR -> buffer:"
                    + ((buffers == null) ? "null" : buffers.length)
                    + " offset:" + offset + " length:" + length);
            return "";
        }
    }

    private boolean isValid(int len) {
        if (buffers == null || offset + len > buffers.length) {
            return false;
        } else {
            return true;
        }
    }

    public static ResponsePacket create(int messageType, byte[] bytes) {
        switch (messageType) {
            case Packets.RESPONSE_ACK:
                return new ResponseAckPacket(bytes);
            case Packets.SERVICE_REQUEST_RESULT:
                return new ServiceRequestResultPacket(bytes);
            case Packets.NOTICES:
                return new NoticesPacket(bytes);
            case Packets.SERVICE_CONFIG:
                return new ServiceConfigPacket(bytes);
            case Packets.RESPONSE_PERIOD_SENDING:
                return new ResponsePeriodSendingPacket(bytes);
            case Packets.ORDER_INFO:
                return new OrderInfoPacket(bytes);
            case Packets.ORDER_INFO_PROC:
                return new OrderInfoProcPacket(bytes);
            case Packets.RESPONSE_SERVICE_REPORT:
                return new ResponseServiceReportPacket(bytes);
            case Packets.WAIT_PLACE_INFO:
                return new WaitPlaceInfoPacket(bytes);
            case Packets.RESPONSE_WAIT_DECISION:
                return new ResponseWaitDecisionPacket(bytes);
            case Packets.RESPONSE_WAIT_CANCEL:
                return new ResponseWaitCancelPacket(bytes);
            case Packets.WAIT_ORDER_INFO:
                return new WaitOrderInfoPacket(bytes);
            case Packets.RESPONSE_ACCOUNT:
                return new ResponseAccountPacket(bytes);
            case Packets.CANCEL_EMERGENCY:
                return new CancelEmergencyPacket(bytes);
            case Packets.RESPONSE_MESSAGE:
                return new ResponseMessagePacket(bytes);
            case Packets.CALLER_INFO_RESEND:
                return new CallerInfoResendPacket(bytes);
            case Packets.RESPONSE_REST:
                return new ResponseRestPacket(bytes);
            default:
                return new ResponsePacket(bytes);
        }
    }

    @Override
    public String toString() {
        return "ResponsePacket{" +
                "messageType=0x" + Integer.toHexString(messageType) +
                '}';
    }
}
