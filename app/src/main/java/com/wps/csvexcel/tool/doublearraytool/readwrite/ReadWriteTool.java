package com.wps.csvexcel.tool.doublearraytool.readwrite;

import android.util.Log;

import java.io.*;

/**
 * @author w_chenxiaoxuan
 */
public class ReadWriteTool<T> extends AbstractReadWrite<T> {

    public ReadWriteTool(ChangeFileAndData<T> changeFileAndData) {
        super(changeFileAndData);
    }

    @Override
    public void write(int xNum, int yNum, Object object) throws IOException {
        File file = getWriteFile(xNum, yNum);
        changeFileAndData.writeDataInFile(xNum, yNum, object, file);
    }

    // return null is not the file
    public T read(int xNum, int yNum) throws IOException,
            ClassNotFoundException {
        final File file = getReadFile(xNum, yNum);
        if (file == null) {
            Log.v(tag," file == null ");
            return null;
        }
        return changeFileAndData.readDataFromFile(xNum, yNum, file);
    }



}
