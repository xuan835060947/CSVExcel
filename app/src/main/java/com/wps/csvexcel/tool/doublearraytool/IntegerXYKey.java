package com.wps.csvexcel.tool.doublearraytool;

/**
 * Created by kingsoft on 2015/8/26.
 */

public class IntegerXYKey{
    private int xBit = 10;
    private int yBit = 22;

    public IntegerXYKey() {
    }

    public IntegerXYKey(int xBit, int yBit) {
        this.xBit = xBit;
        this.yBit = yBit;
    }

    public Integer createKey(int x,int y){
        int key = x<<yBit | y;
        return key;
    }

    public int getX(Integer key){
        int xNum = key >>> yBit;
        return xNum;
    }

    public int getY(Integer key){
        int yNum = key << xBit >>> xBit;
        return yNum;
    }
}
