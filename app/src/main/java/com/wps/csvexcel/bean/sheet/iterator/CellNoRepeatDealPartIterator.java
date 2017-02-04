package com.wps.csvexcel.bean.sheet.iterator;

import com.wps.csvexcel.bean.Cell;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by kingsoft on 2015/8/26.
 */
public class CellNoRepeatDealPartIterator implements Iterator<Cell> {
    private Iterator<Cell> originIterator;
    private Cell next;
    private Set<Cell> set = new HashSet<Cell>(400);

    public CellNoRepeatDealPartIterator(Iterator<Cell> originIterator) {
        this.originIterator = originIterator;
        next = getNoRepeatCell();
    }

    @Override
    public boolean hasNext() {
        if(next!=null){
            return true;
        }
        return false;
    }

    @Override
    public Cell next() {
        Cell result = next;
        next = getNoRepeatCell();
        return result;
    }

    private Cell getNoRepeatCell() {
        if (originIterator.hasNext()) {
            Cell cell = originIterator.next();
            while (set.contains(cell)){
                if (originIterator.hasNext()) {
                    cell = originIterator.next();
                }else{
                    cell = null;
                    break;
                }
            }
            set.add(cell);
            return cell;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {

    }
}
