package com.wps.csvexcel.bean.sheet.iterator;

import java.util.Iterator;

/**
 * Created by kingsoft on 2015/8/7.
 */
public interface FastIterator<T> extends Iterator<T>{
    public void finish();
}
