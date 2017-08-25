package com.thinkware.florida.external.serial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Serial port를 오픈하고 InputStream과 OutputStream를 생성한다.
 */
public class SerialPort {

    public static final int O_RDONLY = 0x0000;
    public static final int O_WRONLY = 0x0001;
    public static final int O_RDWR = 0x0002;
    public static final int O_ACCMODE = 0x0003;
    public static final int O_NONBLOCK = 0x0004;
    public static final int O_APPEND = 0x0008;
    public static final int O_SHLOCK = 0x0010;
    public static final int O_EXLOCK = 0x0020;
    public static final int O_ASYNC = 0x0040;
    public static final int O_FSYNC = 0x0080;
    public static final int O_CREAT = 0x0200;
    public static final int O_TRUNC = 0x0400;
    public static final int O_EXCL = 0x0800;

    //USB허브(ep-100)
    public static final int USB_VENDOR_ID = 1250;
    public static final int USB_PRODUCT_ID = 5140;

    private FileDescriptor fd;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;

    public SerialPort(File port, int baudrate, int flag) {
        fd = openFd(port.getAbsolutePath(), baudrate, flag);
        if (fd != null) {
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
        } else {
            throw new NullPointerException();
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            closeFd();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }


    public static boolean isAttachedUSB(Context context, int vendorId, int productId) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList != null) {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                if (device != null) {
                    if (vendorId == device.getVendorId()
                            && productId == device.getProductId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // JNI
    private native FileDescriptor openFd(String path, int baudrate, int flag);

    private native void closeFd();

    static {
        System.loadLibrary("serial_port");
    }

}
