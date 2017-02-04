package com.wps.csvexcel.view.task;

import android.app.Activity;
import android.util.Log;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;
import com.wps.csvexcel.tool.doublearraytool.CacheBlockDoubleArray;
import com.wps.csvexcel.util.ProgressDialogUtil;
import com.wps.csvexcel.util.ToastUtil;
import com.wps.csvexcel.view.SheetView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by kingsoft on 2015/8/7.
 */
public class FilterSingleChosenRegionTask extends AsyncTaskRun {//广度优先搜索
    private Sheet sheet;
    private final int maxX;
    private final int maxY;
    private final CacheBlockDoubleArray<Boolean> isUsed;
    private int minLeft = Integer.MAX_VALUE, minTop = Integer.MAX_VALUE,
            maxRight = -1;
    private Queue<XYNum> queue = new LinkedList<XYNum>();
    private final int x;
    private final int y;
    private ProgressDialogUtil progressDialogUtil;
    private SheetView sheetView;
    private HaveContent haveContent;
    private int progressLength;

    public FilterSingleChosenRegionTask(Activity activity, SheetView sheetView, Sheet sheet, final int x, final int y) {
        super(activity);
        this.sheet = sheet;
        this.sheetView = sheetView;
        maxX = sheet.getMaxListPosition();
        maxY = sheet.getMaxRowPosition();
        this.x = x;
        this.y = y;
        isUsed = new CacheBlockDoubleArray<Boolean>(sheet.sizeX(), sheet.sizeX() * 2, Integer.MAX_VALUE, null);
    }

    public void start() {
        this.progressDialogUtil = new ProgressDialogUtil(activity, "正在查找中...");
        progressDialogUtil.setProgress(0);
        progressDialogUtil.show();
        execute();
    }

    @Override
    protected void doInBackground() {
        startWork(x, y);
    }

    @Override
    protected void onProgressUpdate(int values) {
        progressDialogUtil.setProgress(values);
    }

    @Override
    protected void finishToDoInUIThread() {
        progressDialogUtil.cancle();
        for (int i = getMinLeft(); i <= getMaxRight(); i++) {
            sheetView.addFilterView(i, getMinTop());
        }
        if (!hasSomethingToFilter()) {
            ToastUtil.show(activity, "请选择数据区域的单元格");
        }
        sheetView.invalidate();
    }

    private void startWork(int x, int y) {
        haveContent = new HaveContent();
        Log.v("", " maxX : " + maxX);
        if (x >= 0 && x <= maxX + 1 && y >= 0 && y <= maxY + 1) {
        } else {
            return;
        }

        LeftRightTop oldLRT = new LeftRightTop(getMinLeft(),
                getMaxRight(), getMinTop());
        find(x, y);
        LeftRightTop newLRT = new LeftRightTop(getMinLeft(),
                getMaxRight(), getMinTop());
        while (!oldLRT.equals(newLRT)) {
            for (int i = newLRT.getLeft(); i <= newLRT.getRight(); i++) {
                oldLRT = newLRT;
                find(i, newLRT.top);
                newLRT = new LeftRightTop(getMinLeft(),
                        getMaxRight(), getMinTop());
            }

        }
    }


    private void find(int x, int y) {
        queue.add(new XYNum(x, y));
        while (!queue.isEmpty()) {
            XYNum xyNum = queue.poll();
            int endX = xyNum.getXNum() + 1 > maxX ? maxX : xyNum.getXNum() + 1;
            int endY = xyNum.getYNum() + 1 > maxY ? maxY : xyNum.getYNum() + 1;
            for (int i = xyNum.getXNum() - 1 < 0 ? 0 : xyNum.getXNum() - 1; i <= endX; i++) {
                for (int j = xyNum.getYNum() - 1 < 0 ? 0 : xyNum.getYNum() - 1; j <= endY; j++) {
                    if (!isUsed(i, j)) {
                        used(i, j);
                        if (havaContent(i, j)) {
                            if (i < getMinLeft()) {
                                setMinLeft(i);
                            }
                            if (i > getMaxRight()) {
                                setMaxRight(i);
                            }
                            if (j < getMinTop()) {
                                setMinTop(j);
                            }
                            if (isFinishEarly()) {
                                return;
                            }
                            queue.add(new XYNum(i, j));
                        }

                    }
                }
            }

        }
    }

    private boolean isFinishEarly() {
        return minLeft == 0 && maxRight == maxX && minTop == 0;
    }

    public boolean hasSomethingToFilter() {
        if (getMinLeft() != Integer.MAX_VALUE) {
            return true;
        }
        return false;
    }

    public int getMinLeft() {
        return minLeft;
    }

    private void setMinLeft(int minLeft) {
        updateProgress(getProgress());
        this.minLeft = minLeft;
    }

    public int getMinTop() {
        updateProgress(getProgress());
        return minTop;
    }

    private void setMinTop(int minTop) {
        updateProgress(getProgress());
        this.minTop = minTop;
    }

    private int getProgress() {
        if (progressLength == 0) {
            progressLength = sheet.sizeX() + y;
        }
        int progress = (((x - minLeft) + (maxRight - x) + (y - minTop)) * 100) / progressLength;
        return progress;
    }

    public int getMaxRight() {
        return maxRight;
    }

    private void setMaxRight(int maxRight) {
        this.maxRight = maxRight;
    }

    private boolean havaContent(int x, int y) {
        Boolean b = haveContent.haveContent(x, y);
        if (b == null || b == Boolean.FALSE) {
            return false;
        }
        return true;
    }

    private boolean isUsed(int x, int y) {
        if (isUsed.get(x, y) == null || isUsed.get(x, y) == false) {
            return false;
        } else {
            return true;
        }
    }

    private void used(int x, int y) {
        isUsed.insertTo(x, y, Boolean.TRUE);
    }

    static class LeftRightTop {
        private int left;
        private int right;
        private int top;

        public LeftRightTop(int left, int right, int top) {
            this.left = left;
            this.right = right;
            this.top = top;
        }

        public boolean equals(LeftRightTop lrt) {
            if (getLeft() == lrt.getLeft() && getRight() == lrt.getRight()
                    && getTop() == lrt.getTop()) {
                return true;
            } else {
                return false;
            }
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }

        public int getTop() {
            return top;
        }

    }

    class HaveContent {
        private int centerY = y;
        private int startXNum = 0, startYNum;
        private int width = sheet.sizeX();
        private int xLength = width;

        private int endYNum;
        private CacheBlockDoubleArray<Boolean> array = new CacheBlockDoubleArray<Boolean>(width, width * 2, Integer.MAX_VALUE, null);
        private Sheet.FindCellListener findCellListener = new Sheet.FindCellListener() {
            @Override
            public void onFindCell(int xNum, int yNum, Cell cell) {
                if (cell != null && cell.getContent() != null) {
                    array.insertTo(xNum, yNum, Boolean.TRUE);
                } else {
                    array.insertTo(xNum, yNum, Boolean.FALSE);
                }
            }
        };

        public HaveContent() {
            startYNum = centerY - width < 0 ? 0 : centerY - width;
            int yLength = width * 2;
            yLength = startYNum + yLength > sheet.sizeY() ? sheet.sizeY() - startYNum : yLength;
            endYNum = startYNum + yLength;
            Sheet.RegionCellIterator regionCellIterator = sheet.getRegionCellIterator(findCellListener, startXNum, startYNum, xLength, yLength);
            regionCellIterator.iterator();
        }

        public Boolean haveContent(int x, int y) {
            if (y >= startYNum && y <= endYNum) {
                return array.get(x, y);
            } else {
                if (y < startYNum) {
                    //增加上部分
                    int newStartYNum = y - width < 0 ? 0 : y - width;
                    int length = startYNum - newStartYNum;
                    startYNum = newStartYNum;
                    Sheet.RegionCellIterator regionCellIterator = sheet.getRegionCellIterator(findCellListener, startXNum, startYNum, xLength, length);
                    regionCellIterator.iterator();
                } else {
                    //增加下部分
                    int newEndYNum = endYNum + width;
                    newEndYNum = newEndYNum > sheet.sizeY() ? sheet.sizeY() : newEndYNum;
                    Sheet.RegionCellIterator regionCellIterator = sheet.getRegionCellIterator(findCellListener, startXNum, endYNum, xLength, newEndYNum - endYNum);
                    regionCellIterator.iterator();
                    endYNum = newEndYNum;
                }
            }
            return array.get(x, y);
        }
    }

}
