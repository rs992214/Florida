package com.thinkware.florida.external.service;

import android.os.SystemClock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 데이터를 읽는 Thread
 */
class ReadFileThread extends Thread {

    private boolean active;
    private long sleep;
    private ByteBuffer buffer;
    private FileChannel inputStream;
    private DataParser dataParser;

    ReadFileThread(FileChannel inputStream, DataParser dataParser) {
        this.inputStream = inputStream;
        this.dataParser = dataParser;
        active = true;
        buffer = ByteBuffer.allocate(2);
    }

    /**
     * thread pause
     */
    synchronized void pause() {
        active = false;
    }

    /**
     * thread unpause
     */
    synchronized void unpause() {
        active = true;
        this.notifyAll();
    }

    /**
     * set sleep time
     *
     * @param sleep sleep time
     */
    synchronized void setSleep(long sleep) {
        this.sleep = sleep;
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

                buffer.clear();
                inputStream.position(0);

                int size = inputStream.read(buffer);

                if (size > 0) {
                    byte[] readBuffer = buffer.array();
//                    LogHelper.d("size :" + size + "\tbytes: 0x" + ByteUtil.toHexString(readBuffer, size));

                    if (dataParser != null) {
                        dataParser.parse(readBuffer, size);
                    }
                }

                SystemClock.sleep(sleep);
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
