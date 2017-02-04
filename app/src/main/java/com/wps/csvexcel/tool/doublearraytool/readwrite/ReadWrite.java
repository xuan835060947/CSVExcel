package com.wps.csvexcel.tool.doublearraytool.readwrite;

import java.io.IOException;

/**
 * Created by kingsoft on 2015/8/4.
 */
public interface ReadWrite<T> {
    public void write(int xNum, int yNum, Object object) throws IOException;

    public T read(int xNum, int yNum) throws IOException,
            ClassNotFoundException;

    public T getObject(int xNum,int yNum);

    public String getDirPath();

    public void setDirPath(String dirPath);
}
