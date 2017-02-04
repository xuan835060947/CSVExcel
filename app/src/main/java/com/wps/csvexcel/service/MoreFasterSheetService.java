package com.wps.csvexcel.service;


import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.util.ioutil.IOUtil;
import com.wps.csvexcel.util.SpeedTestUtil;
import com.wps.csvexcel.util.SplitUtil;
import com.wps.csvexcel.util.ioutil.BufferedReaderWrap;

/**
 * @author w_chenxiaoxuan
 *         <p/>
 *         异步写块进文件没问题的原因是:Sheet中读取块有从缓存文件读和从源文件读两种方式
 */
public class MoreFasterSheetService extends Service {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Binder binder = new SheetBackgroundServiceBinder();
    private List<ServiceFinishListener> finishListeners = new ArrayList<ServiceFinishListener>();
    private Sheet originSheet;
    private String copyFilePath;
    private SplitRowBlock splitRowBlock;
//    private static final int WAIT_TIME = 500;
    private static final int START_BLOCK_Y_NUM = 5;
    private static final int SPLIT_BLOCK_STEP_NUM = 5;
//    private static final int ONE_SPLIT_AMOUNT = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setSheet(final Sheet sheet) {
        originSheet = sheet;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                createCopyFile(sheet.getFilePath());
                if (copyFilePath != null) {
                    Log.e("", " copy file : " + copyFilePath);
                    splitRowBlock = new SplitRowBlock(START_BLOCK_Y_NUM);
                    executorService.execute(splitRowBlock);
                    originSheet.setOnGetBlockListener(new Sheet.OnGetBlockListener() {
                        @Override
                        public void onGetBlock(int blockXNum, int blockYNum) {
                            if (blockYNum + SPLIT_BLOCK_STEP_NUM > splitRowBlock.getStartSplitUntil()) {
                                splitRowBlock.startSplitUntil(blockYNum + SPLIT_BLOCK_STEP_NUM);
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public boolean onUnbind(Intent intent) {
        // 进行一些资源的处理
        Log.e(" onUnbind ", " onUnbind ");
        for (ServiceFinishListener sfl : finishListeners) {
            sfl.onFinish();
        }
        return super.onUnbind(intent);
    }

    public class SheetBackgroundServiceBinder extends Binder {
        public MoreFasterSheetService getService() {
            return MoreFasterSheetService.this;
        }
    }

    public void addFinishListener(ServiceFinishListener serviceFinishListener) {
        finishListeners.add(serviceFinishListener);
    }


    public void createCopyFile(String path) {
        File originFile = new File(path);
        if (!originFile.exists()) {
            return;
        }
        SpeedTestUtil.start("copy");
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            copyFilePath = getDirPath() + originFile.getName();
            File file = getWriteFile(originFile.getName());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileInputChannel = fileInputStream.getChannel();
            FileChannel fileOutputChannel = fileOutputStream.getChannel();
            fileOutputChannel.transferFrom(fileInputChannel, 0, fileInputChannel.size());

            fileInputChannel.close();
            fileOutputChannel.close();
            fileInputStream.close();
            fileOutputStream.close();
            SpeedTestUtil.end("copy");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface ServiceFinishListener {
        public void onFinish();
    }

    protected File getWriteFile(String fileName) throws IOException {
        File dir = new File(getDirPath());
        if (!dir.exists()) {
            if (dir.mkdirs()) {
//                throw new IOException("创建文件夹失败");
            }
        }
        File file = new File(dir, fileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
//                throw new IOException("创建文件失败");
            }
        }
        return file;
    }

    public String getDirPath() {
        String dirPath;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在

        File sdDir = null;
        if (sdCardExist) {// 如果SD卡存在，则获取跟目录
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        } else {
            throw new IllegalStateException();
        }
        dirPath = sdDir.toString() + "/kingsoft/wps/temp/";
//            Log.v("AbstractReadWrite 路径: "+ ++num, dirPath);
        return dirPath;
    }

    public class SplitRowBlock implements Runnable {
        private int startBlockYNum;
        private BufferedReaderWrap reader;
        private Cell[][] oneRowBlocks;
        private int blockArea;
        private Object lock = new Object();
        private volatile boolean startNext;
        private volatile boolean finish;
        private volatile int startSplitUntil;

        private int bufferLength = 2048;
        private char[] buffer = new char[bufferLength];

        public SplitRowBlock(int startBlockYNum) {
            this.startBlockYNum = startBlockYNum;
            int line = startBlockYNum * originSheet.getBlockHeight();
            try {
                reader = IOUtil.getReaderWrapToLine(copyFilePath, line, Sheet.ENCODING);
                blockArea = originSheet.getBlockWidth() * originSheet.getBlockHeight();
                oneRowBlocks = new Cell[originSheet.getBlockAmountX()][blockArea];
            } catch (IOException e) {
                e.printStackTrace();
            }
            addFinishListener(new ServiceFinishListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        }


        public void splitOneRow(final int blockYNum) throws IOException {
            int sizeX = originSheet.sizeX();
            int blocksAmount = originSheet.getBlockAmountX();
            int height = originSheet.getBlockHeight();
            int blockWidth = originSheet.getBlockWidth();
            int endPos = 0;
            int startNumInBlock = 0;
            for (int i = 0; i < height; i++) {
                endPos = getLineToBuffer(reader);
                if (endPos > 0) {
                    SplitUtil.StringArrLength sal = SplitUtil.splitString(buffer, endPos, 0, sizeX);
                    int length = sal.getArrElementAmount();
                    String[] strArr = sal.getArr();
                    int rowNum = 0;
                    int sNum = 0;
                    while (rowNum < blocksAmount) {
                        int endNumInBlock = startNumInBlock + blockWidth;
                        for (int numInBlock = startNumInBlock; numInBlock < endNumInBlock && sNum < length; numInBlock++) {
                            oneRowBlocks[rowNum][numInBlock] = new Cell(strArr[sNum++]);
                        }
                        rowNum++;
                    }
                    startNumInBlock += blockWidth;
                } else {
                    break;
                }
            }
            int rowNum = 0;
            while (rowNum < blocksAmount) {
                originSheet.writeBlocks(rowNum, blockYNum, oneRowBlocks[rowNum]);
                ++rowNum;
            }
            Log.e("write ", "originSheet.writeBlocks  Y:  " + blockYNum);

        }

        public void finish() {
            synchronized (lock) {
                this.finish = true;
                if (reader != null) {
                    try {
                        reader.close();
                        lock.notifyAll();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void startSplitUntil(int rowNum) {
            synchronized (lock) {
                startSplitUntil = rowNum;
                startNext = true;
                lock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!finish) {
                synchronized (lock) {
                    if (startNext) {
                        Log.e("splitRows", "splitRows");
                        try {
                            startNext = false;
//                            splitRows();
                            if (reader != null && startBlockYNum < originSheet.getBlockAmountY()) {
                                for (; startBlockYNum < startSplitUntil; startBlockYNum++) {
                                    splitOneRow(startBlockYNum);
//                                    lock.wait(WAIT_TIME);
                                }
                            } else {
                                finish();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.e("splitRows", " 醒来 ");
                    }
                }
            }
        }

        public int getStartSplitUntil() {
            return startSplitUntil;
        }

        private int getLineToBuffer(BufferedReaderWrap reader) throws IOException {
            int endPos = 0;
            while (true) {
                endPos = reader.readCharLine(buffer);
                if (endPos > 0) {
                    return endPos;
                } else {
                    switch (endPos) {
                        case BufferedReaderWrap.NO_ANY_LINE:
                            return BufferedReaderWrap.NO_ANY_LINE;
                        case BufferedReaderWrap.EXCEED_BRESULT:
                            bufferLength *= 2;
                            buffer = new char[bufferLength];
                            break;
                    }
                }
            }
        }
    }


}
