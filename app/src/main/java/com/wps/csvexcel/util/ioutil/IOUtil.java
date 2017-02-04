package com.wps.csvexcel.util.ioutil;

import android.util.Log;
import com.wps.csvexcel.util.ioutil.BufferedReaderWrap;
import junit.framework.Assert;

import java.io.*;

/**
 * Created by kingsoft on 2015/8/26.
 */
public class IOUtil {
    public static BufferedReader getReaderToLine(String filePath , int line,String encoding) throws IOException {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), encoding);
            BufferedReader reader = new BufferedReader(read);
//                String lineText = null;
            for (int i = 0; i < line && (reader.readLine() != null); i++) {
            }
            return reader;
        } else {
            Log.e("--XUAN--", "文件不存在");
        }
        return null;
    }

    public static BufferedReaderWrap getReaderWrapToLine(String filePath , int line,String encoding) throws IOException {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            BufferedReaderWrap bufferedReaderWrap = new BufferedReaderWrap(new InputStreamReader(
                    new FileInputStream(filePath), encoding));
            int length = 2048;
            int count = 0;
            char[] arr = new char[length];
            count:
            for(int i=0;count<line;i++){
                switch (bufferedReaderWrap.readCharLine(arr)) {
                    case BufferedReaderWrap.EXCEED_BRESULT:
                        length*=2;
                        Log.e(""," EXCEED_BRESULT ");
                        arr = new char[length];
                        break;
                    case BufferedReaderWrap.NO_ANY_LINE:
                        break count;
                    default:
                        count++;
                        break;
                }
            }
            return bufferedReaderWrap;
        } else {
            Log.e("--XUAN--", "文件不存在");
        }
        return null;

    }
}
