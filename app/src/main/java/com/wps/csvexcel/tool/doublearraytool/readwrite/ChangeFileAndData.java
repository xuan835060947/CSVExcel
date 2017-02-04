package com.wps.csvexcel.tool.doublearraytool.readwrite;

import java.io.File;

/**
 * Created by kingsoft on 2015/8/4.
 */
public interface ChangeFileAndData<T> {
    public T getData(int xNum, int yNum);
    public T readDataFromFile(int xNum, int yNum, File file);
    public void writeDataInFile(int xNum, int yNum, Object object, File file);
}
