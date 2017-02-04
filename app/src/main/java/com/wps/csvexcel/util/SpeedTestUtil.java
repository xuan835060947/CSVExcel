package com.wps.csvexcel.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kingsoft on 2015/8/11.
 */
public class SpeedTestUtil {
    private static Map<String,Long> startTimes = new HashMap<String,Long>();
    private static Map<String,Long> endTimes = new HashMap<String,Long>();

    public static void start(String s){
        startTimes.put(s,System.currentTimeMillis());
    }

    public static long end(String s){
        long end = System.currentTimeMillis();
        long time = end - startTimes.get(s);
        Log.e("SpeedTest",s + " : "+time);
        return time;
    }

    public static long getTime(String s){
        long end = endTimes.get(s);
        long time = end - startTimes.get(s);
        return time;
    }
}
