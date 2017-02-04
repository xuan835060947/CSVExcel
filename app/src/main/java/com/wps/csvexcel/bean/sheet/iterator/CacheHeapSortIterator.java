package com.wps.csvexcel.bean.sheet.iterator;

import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.cell.CellComparator;
import com.wps.csvexcel.tool.sorttool.HeapTool;
import com.wps.csvexcel.tool.doublearraytool.CacheList;

import java.util.Iterator;

/**
 * Created by kingsoft on 2015/8/11.
 */
public class CacheHeapSortIterator implements Iterator<Cell>{
    private HeapTool<Cell> heap;
    private int blockWidth = 600;
    private int cacheBlockNum = 2;

    public CacheHeapSortIterator(Iterator<Cell> iterator,int length) {
        CacheList<Cell> list = new CacheList<Cell>(blockWidth,cacheBlockNum);
        while(iterator.hasNext()){
            list.add(iterator.next());
        }
        heap = new HeapTool<Cell>(list, length,new CellComparator());
    }

    @Override
    public boolean hasNext() {
        return heap.getSize()>0? true:false;
    }

    @Override
    public Cell next() {
        return heap.popTop();
    }

    @Override
    public void remove() {
        throw new IllegalArgumentException();
    }
}
