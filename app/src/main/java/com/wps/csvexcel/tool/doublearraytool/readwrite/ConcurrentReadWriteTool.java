package com.wps.csvexcel.tool.doublearraytool.readwrite;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kingsoft on 2015/8/18.
 */
public class ConcurrentReadWriteTool<T> extends ReadWriteTool<T> {
    private LinkedBlockingQueue<WriteSources> queue = new LinkedBlockingQueue<WriteSources>();
    protected ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ReadWriteRunnable readWriteRunnable = new ReadWriteRunnable();

    public ConcurrentReadWriteTool(ChangeFileAndData<T> changeFileAndData) {
        super(changeFileAndData);
        executorService.execute(readWriteRunnable);
    }

    @Override
    public void write(int xNum, int yNum, Object object) throws IOException {
        try {
//            Log.e("","put ! ");
            queue.put(new WriteSources(xNum, yNum, object));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void save(WriteSources writeSources) {
        try {
            super.write(writeSources.getXNum(), writeSources.getYNum(), writeSources.getObject());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadWriteRunnable implements Runnable {
        private volatile boolean isFinish;

        @Override
        public void run() {
            while (!isFinish) {
                WriteSources writeSources = null;
                try {
//                    Log.e("","take ! ");
                    writeSources = queue.take();
                    ConcurrentReadWriteTool.this.save(writeSources);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void finish() {
            this.isFinish = true;
        }
    }


    protected static class WriteSources {
        private int xNum;
        private int yNum;
        private Object object;

        public WriteSources(int xNum, int yNum, Object object) {
            this.xNum = xNum;
            this.yNum = yNum;
            this.object = object;
        }

        public int getXNum() {
            return xNum;
        }

        public int getYNum() {
            return yNum;
        }

        public Object getObject() {
            return object;
        }
    }
}
