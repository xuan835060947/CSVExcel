package com.wps.csvexcel.tool.doublearraytool.readwrite;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by kingsoft on 2015/8/4.
 */
public abstract class AbstractReadWrite<T> implements ReadWrite<T>{
    protected String tag= String.valueOf(this);
    protected String dirPath;
    protected ChangeFileAndData<T> changeFileAndData;

    public AbstractReadWrite(ChangeFileAndData<T> changeFileAndData){
        if(changeFileAndData ==null){
            throw new IllegalArgumentException("changeFileAndCache can not be null");
        }

        this.changeFileAndData = changeFileAndData;
    }

    @Override
    public T getObject(int xNum, int yNum) {
        return changeFileAndData.getData(xNum, yNum);
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getDirPath() {
        if (dirPath == null) {
            boolean sdCardExist = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED); // 判断sd卡是否存在

            File sdDir = null;
            if (sdCardExist) // 如果SD卡存在，则获取跟目录
            {
                sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            } else {
                throw new IllegalStateException();
            }
            dirPath = sdDir.toString() + "/kingsoft/wps/temp/"
                    + System.currentTimeMillis() + "/";
//            Log.v("AbstractReadWrite 路径: "+ ++num, dirPath);
            return dirPath;
        }
        return dirPath;
    }

    protected File getWriteFile(int xNum, int yNum) throws IOException {
        File dir = new File(getDirPath());
        if (!dir.exists()) {
            if(dir.mkdirs()){
//                throw new IOException("创建文件夹失败");
            }
        }
        String fileName = xNum + "_" + yNum;
        File file = new File(dir, fileName);
        if (!file.exists()) {
            if(file.createNewFile()){
//                throw new IOException("创建文件失败");
            }
        }
        return file;
    }

    protected File getReadFile(int xNum, int yNum) {
        String fileName = xNum + "_" + yNum;
        File f = new File(getDirPath() + fileName);
        if (!f.exists()) {
            return null;
        }
        return f;
    }
}
