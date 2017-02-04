package com.wps.csvexcel.bean.sheet.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.util.Log;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;
import com.wps.csvexcel.view.task.ShowFilterPopupWindowTask;

/**
 * @author w_chenxiaoxuan
 */
public class FilterMultValue implements FilterMode {

    private Sheet sheet;
    private List<Cell> cells = new ArrayList<Cell>();
    private Set<Cell> notChooseSet = new HashSet<Cell>();
    private List<Boolean> isCheck = new ArrayList<Boolean>();
    private Iterator<Cell> it;
    private int lastYNum = -1;
    private int lastYNumInSheet = -1;
    private int listNum = 0;
    private int startYNum = 0, endYNum = -1;
    private boolean isSingleFilter;
    private Cell singleChoice;
    private static final boolean DEFAULT_CHOICE_STATE = true;
    private ShowFilterPopupWindowTask showFilterPopupWindowTask;

    public FilterMultValue(Iterator<Cell> it, int xNum) {
        this.listNum = xNum;
        this.it = it;
    }

    public FilterMultValue(Iterator<Cell> it, int xNum, ShowFilterPopupWindowTask showFilterPopupWindowTask) {
        this.listNum = xNum;
        this.it = it;
        this.showFilterPopupWindowTask = showFilterPopupWindowTask;
    }

    public void filter(Iterator<Cell> it, int xNum) {
        this.listNum = xNum;
        this.it = it;
        cells.clear();
        isCheck.clear();
    }


    public boolean isChoice(int num) {
        if (num < isCheck.size())
            return isCheck.get(num);
        return false;
    }

    public void setSingle(boolean single) {
        this.isSingleFilter = single;
    }

    public boolean isSingleFilter() {
        return isSingleFilter;
    }


    @Override
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        if (endYNum > sheet.getMaxRowPosition()) {
            endYNum = sheet.getMaxRowPosition();
        }
    }

    @Override
    public XYNum chooseRow(int xNum, int yNum) {
        if (yNum == lastYNum) {
            return new XYNum(xNum, lastYNumInSheet);
        }

        int startY = 0;
        int distance = 0;
        boolean toBig = false;
        if (yNum > lastYNum) {
            toBig = true;
            startY = lastYNumInSheet + 1;
            distance = yNum - lastYNum;
        } else {
            toBig = false;
            startY = lastYNumInSheet - 1;
            distance = lastYNum - yNum;
        }
        startY = startY < startYNum ? startYNum : startY;
        if (startY < 0) {
            // Log.v(" Filter MUlt ", "");
        }
        while (true) {
            XYNum xyNum = new XYNum(listNum, startY);
            if ((isOK(xyNum))) {
                lastYNum = yNum;
                lastYNumInSheet = startY;
                --distance;
                if (distance <= 0) {
                    return xyNum;
                }
            }
            if (toBig) {
                ++startY;
            } else {
                --startY;
            }
        }
    }

    @Override
    public void setStartYNum(int startYNum) {
        if (startYNum < 0) {
            throw new IllegalArgumentException("startYNum must >= 0");
        }
        this.startYNum = startYNum;
    }

    @Override
    public boolean isOK(XYNum xyNum) {
        if (xyNum.getYNum() > endYNum) {
            return true;
        }

        if (isSingleFilter()) {
            Cell filterCell = sheet.getCell(listNum, xyNum.getYNum());
            if (singleChoice != null) {
                if (singleChoice.equals(filterCell)) {
                    return true;
                }
            } else {
                if (filterCell.getContent() == null) {
                    return true;
                }
            }
            return false;
        }

        if (notChooseSet.isEmpty()) {
            return true;
        }


        Cell filterCell = sheet.getCell(listNum, xyNum.getYNum());
        if (!notChooseSet.contains(filterCell)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setEndYNum(int endYNum) {
        if (sheet == null) {
            throw new IllegalStateException("  sheet can not be null !! ");
        }

        if (endYNum < sheet.getMaxRowPosition()) {
            this.endYNum = endYNum;
        } else {
            this.endYNum = sheet.getMaxRowPosition();
        }
    }

//    public void setCell(int position, Cell cell) {
//        cells.set(position, cell);
//    }



    public Cell getCell(int position) {
        while (position >= cells.size()) {
            if (it.hasNext()) {
                cells.add(it.next());
            } else {
                cells.add(null);
            }
            isCheck.add(DEFAULT_CHOICE_STATE);
        }
        return cells.get(position);
    }

    public void setChoice(int position) {
        Log.e("choice : ", " choice :  " + position);
        if (showFilterPopupWindowTask != null)
            showFilterPopupWindowTask.setCurrentFilterPopupWindowNum(listNum);
        if (isSingleFilter()) {
            singleChoice = cells.get(position);
        } else {
            if (isCheck.get(position)) {
                isCheck.set(position, false);
                addNotChoiceCell(cells.get(position));
            } else {
                isCheck.set(position, true);
                removeNotChoiceCell(cells.get(position));
            }
        }
    }

    private void addNotChoiceCell(Cell cell) {
        if (cell == null) {
            notChooseSet.add(new Cell());
        }
        notChooseSet.add(cell);
    }

    private void removeNotChoiceCell(Cell cell) {
        if (cell == null) {
            notChooseSet.remove(new Cell());
        }
        notChooseSet.remove(null);
    }

    public void clearChoice() {
        notChooseSet.clear();
    }

}
