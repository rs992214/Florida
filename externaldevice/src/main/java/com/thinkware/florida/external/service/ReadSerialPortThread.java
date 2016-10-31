package com.thinkware.florida.external.service;

import android.os.SystemClock;

import com.thinkware.florida.utility.ByteUtil;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 데이터를 읽는 Thread
 */
class ReadSerialPortThread extends Thread {

    private boolean active;
    private byte[] buffer;
    private InputStream inputStream;
    private DataParser dataParser;


    ReadSerialPortThread(InputStream inputStream, DataParser dataParser) {
        this.inputStream = inputStream;
        this.dataParser = dataParser;
        buffer = new byte[256];
        active = true;
    }

    /**
     * thread pause
     */
    public synchronized void pause() {
        active = false;
    }

    /**
     * thread unpause
     */
    public synchronized void unpause() {
        active = true;
        this.notifyAll();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                synchronized (this) {
                    if (!active) {
                        this.wait();
                    }
                }

                Arrays.fill(buffer, (byte) 0x0);
                int size = inputStream.read(buffer);

                if (size > 0) {
                    LogHelper.d("size :" + size + "\tbytes: 0x" + ByteUtil.toHexString(buffer, size));

                    if (dataParser != null) {
                        dataParser.parse(buffer, size);
                    }
                    SystemClock.sleep(50);
                } else {
                    SystemClock.sleep(100);
                }
            } catch (IOException | NullPointerException e) {
                if (dataParser != null) {
                    dataParser.setError(e);
                }
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
