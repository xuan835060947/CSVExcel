package com.wps.csvexcel.bean.sheet.iterator;

import android.util.Log;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;

/**
 * Created by kingsoft on 2015/8/6.
 */
public class FastCellIerator implements FastIterator<Cell> {
    private Sheet sheet;
    private final int xNumInBlock;
    private FastIterator<Cell[]> it;
    private Cell[] curBlock;
    private int xNum;
    private int readYNum;
    private final int maxYNum;
    private int readBlockYNum = -1;
    private boolean needRowListTag=false;

    public FastCellIerator(Sheet sheet, int xNum, int startYNum) {
        this(sheet,xNum,startYNum,false);
    }

    public FastCellIerator(Sheet sheet, int xNum, int startYNum,boolean needRowListTag) {
        this.sheet = sheet;
        this.xNum =xNum;
        this.xNumInBlock = xNum % sheet.getBlockWidth();
        this.readYNum = startYNum;
        this.maxYNum = sheet.getMaxRowPosition();
        this.needRowListTag = needRowListTag;
        int blockXNum = xNum / sheet.getBlockWidth();
        it = sheet.iteratorBlockList(blockXNum, startYNum / sheet.getBlockHeight());
        Log.e(" FastCellIerator  "," FastCellIerator start");
    }

    @Override
    public boolean hasNext() {
        if (readYNum <= maxYNum) {
            return true;
        }
        return false;
    }

    @Override
    public Cell next() {
        Cell cell = getBlock()[getNumInBlock(readYNum)];
        if(needRowListTag){
            cell.setTag(new XYNum(xNum,readYNum));
        }
        ++readYNum;
//        if(cell == null){
//            return new Cell();
//        }
        return cell;
    }



    @Override
    public void remove() {
        throw new IllegalArgumentException("this method can not to be used");
    }

    private Cell[] getBlock(){
        int blockYNum = readYNum / sheet.getBlockHeight();
        if(blockYNum > readBlockYNum){
            curBlock = it.next();
            readBlockYNum = blockYNum;
        }
        return curBlock;
    }

    private int getNumInBlock(int yNum){
        int yNumInBlock = yNum % sheet.getBlockHeight();
        return yNumInBlock*sheet.getBlockWidth() + xNumInBlock;
    }

    @Override
    public void finish() {
        Log.e(" FastCellIerator  "," FastCellIerator finish");
        it.finish();
    }
}