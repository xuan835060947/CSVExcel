package com.wps.csvexcel.bean;

import java.io.Serializable;

/**
 * Created by kingsoft on 2015/8/9.
 */
public class XYNum implements Serializable{
    private int x;
    private int y;

    public XYNum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getXNum() {
        return x;
    }

    public int getYNum() {
        return y;
    }

    @Override
    public int hashCode() {
        return x ^ y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof XYNum) {
            XYNum oXY = (XYNum) o;
            if (oXY.getXNum() == getXNum() && oXY.getYNum() == getYNum()) {
                return true;
            }
        }
        return false;
    }
}
