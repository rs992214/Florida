package com.thinkware.florida.external.service;

import com.thinkware.florida.utility.ByteUtil;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

/**
 * 데이터를 쓰는 Thread
 */
class WriteThread extends Thread {

    private OutputStream outputStream;
    private BlockingQueue<byte[]> queue;
    private DataParseCallback callback;

    WriteThread(OutputStream outputStream, BlockingQueue<byte[]> queue, DataParseCallback callback) {
        this.outputStream = outputStream;
        this.queue = queue;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                byte[] b = queue.take();
                LogHelper.d("write buffer: %s", ByteUtil.toHexString(b));
                outputStream.write(b);
                outputStream.flush();
            } catch (IOException | NullPointerException e) {
                if (callback != null) {
                    callback.onError(e);
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
