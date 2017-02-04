package com.wps.csvexcel.tool.doublearraytool.readwrite;

import android.util.Log;
import com.wps.csvexcel.tool.doublearraytool.IntegerXYKey;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kingsoft on 2015/8/25.
 * 非线程安全
 */
public class CacheConcurrentReadWrite<T> extends AbstractReadWrite<T[]> {

    private static final int MAP_CAPACITY = 20;
    private final IntegerXYKey integerXYKey = new IntegerXYKey();
    private ConcurrentHashMap<Integer, Object[]> map = new ConcurrentHashMap<Integer, Object[]>(
            MAP_CAPACITY);
    protected ExecutorService executorService = Executors.newSingleThreadExecutor();


    public CacheConcurrentReadWrite(ChangeFileAndData<T[]> changeFileAndCache) {
        super(changeFileAndCache);
    }


    @Override
    public void write(int xNum, int yNum, Object object) throws IOException {
        Integer key = integerXYKey.createKey(xNum, yNum);
        map.put(key, (Object[]) object);
        executorService.execute(new WriteRunnable(xNum, yNum, (Object[]) object));
    }


    // return null while there is not the file
    public T[] read(int xNum, int yNum) throws IOException,
            ClassNotFoundException {
        Object object = map.get(integerXYKey.createKey(xNum, yNum));
        if (object != null) {
            Log.v(tag, " read from map ");
            return (T[]) object;
        }

        final File file = getReadFile(xNum, yNum);
        if (file == null) {
            return null;
        }
        T[] result = changeFileAndData.readDataFromFile(xNum, yNum, file);
        return result;
    }


    class WriteRunnable implements Runnable {
        private int xNum;
        private int yNum;
        private Object[] arr;


        public WriteRunnable(int xNum, int yNum, Object[] arr) {
            this.xNum = xNum;
            this.yNum = yNum;
            this.arr = arr;
        }


        @Override
        public void run() {
            try {
                write(xNum, yNum, arr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void write(int xNum, int yNum, Object[] arr) throws IOException {
            File file = getWriteFile(xNum, yNum);
            changeFileAndData.writeDataInFile(xNum, yNum, arr, file);
            map.remove(integerXYKey.createKey(xNum, yNum));
        }
    }

}
