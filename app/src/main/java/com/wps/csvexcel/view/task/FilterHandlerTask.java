package com.wps.csvexcel.view.task;

import android.app.Activity;
import android.util.Log;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;
import com.wps.csvexcel.bean.sheet.filter.FilterMode;
import com.wps.csvexcel.util.SpeedTestUtil;
import com.wps.csvexcel.util.ProgressDialogUtil;
import com.wps.csvexcel.view.SheetView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kingsoft on 2015/8/6.
 */
public class FilterHandlerTask extends AsyncTaskRun implements FilterMode {

    private Sheet sheet;
    private SheetView sheetView;
    private int startYNum, endYNum = Integer.MAX_VALUE;
    private int lastXNum = -1, lastYNum = -1;
    private int lastYNumInSheet = -1;
    private Map<Integer, FilterMode> filterModes = new HashMap<Integer, FilterMode>();
    private boolean hasFilterMode;
    private XYNum find;
    private int findAmount;
    private ProgressDialogUtil progressDialogUtil;
    private boolean isShouldShowProgressDialog = true;
    private static final long MAX_SHOULD_SHOW_PROGRESS_DIALOG_TIME = 100;

    public FilterHandlerTask(SheetView sheetView, Activity activity, Sheet sheet) {
        super(activity);
        this.progressDialogUtil = new ProgressDialogUtil(activity, "筛选中 . . .");
        this.sheet = sheet;
        this.sheetView = sheetView;
    }

    public void filter(int startYNum, int endYNum, XYNum find, int findAmount) {
        if (isWork()) {
            return;
        }
        SpeedTestUtil.start("FilterHandlerTask");
        if (isShouldShowProgressDialog)
            progressDialogUtil.show();
        this.startYNum = startYNum;
        this.endYNum = endYNum;
        this.find = find;
        this.findAmount = findAmount;
        execute();
    }

    @Override
    protected void doInBackground() {
        Log.v("", " doInBackground ");
        if (isShouldShowProgressDialog)
            progressDialogUtil.show();
        int end = find.getYNum() + findAmount;
        for (int y = find.getYNum(); y < end && y < sheet.sizeY(); y++) {
            XYNum chooseRow = chooseRow(find.getXNum(), y);
            sheetView.putInFilterMap(startYNum + y, chooseRow.getYNum());
            if (isShouldShowProgressDialog) {
                updateProgress((int) ((float) (end - y) / findAmount * 100));
            }
        }
    }

    @Override
    protected void onProgressUpdate(int values) {
        Log.v("", " onProgressUpdate ");
        progressDialogUtil.setProgress(values);
    }

    @Override
    protected void finishToDoInUIThread() {
        Log.v("", " finishToDoInUIThread ");
        progressDialogUtil.cancle();
        sheetView.invalidate();
        long time = SpeedTestUtil.end("FilterHandlerTask");
        if (time < MAX_SHOULD_SHOW_PROGRESS_DIALOG_TIME) {
            setIsShouldShowProgressDialog(false);
        }
    }

    private void setIsShouldShowProgressDialog(boolean isShouldShowProgressDialog) {
        this.isShouldShowProgressDialog = isShouldShowProgressDialog;
    }

    public boolean hasFilterMode() {
        return hasFilterMode;
    }

    public void addFilterMode(int num, FilterMode fm) {
        fm.setSheet(sheet);
        fm.setStartYNum(startYNum);
        fm.setEndYNum(endYNum);
        filterModes.put(num, fm);
        hasFilterMode = true;
        setIsShouldShowProgressDialog(true);
        init();
    }

    public void deleteFilterMode(int num) {
        filterModes.remove(num);
        init();
        if (filterModes.isEmpty()) {
            hasFilterMode = false;
        }
    }

    public void clear() {
        filterModes.clear();
        hasFilterMode = false;
        init();
    }

    public FilterMode getFilterMode(int num) {
        return filterModes.get(num);
    }

    @Override
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        Set<Map.Entry<Integer, FilterMode>> set = filterModes.entrySet();
        for (Map.Entry<Integer, FilterMode> e : set) {
            e.getValue().setSheet(sheet);
        }
        init();
    }

    @Override
    public void setStartYNum(int startYNum) {
        this.startYNum = startYNum;
        Set<Map.Entry<Integer, FilterMode>> set = filterModes.entrySet();
        for (Map.Entry<Integer, FilterMode> e : set) {
            e.getValue().setStartYNum(startYNum);
        }
        init();
    }

    @Override
    public boolean isOK(XYNum xyNum) {
        int yNum = xyNum.getYNum();
        if (yNum > endYNum) {
            return true;
        }
        if (hasFilterMode) {
            Set<Map.Entry<Integer, FilterMode>> set = filterModes.entrySet();
            for (Map.Entry<Integer, FilterMode> e : set) {
                if (!e.getValue().isOK(xyNum)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void init() {
        lastXNum = -1;
        lastYNum = -1;
        lastYNumInSheet = -1;

        if (sheet != null && endYNum > sheet.getMaxRowPosition()) {
            endYNum = sheet.getMaxRowPosition();
        }
    }

    @Override
    public XYNum chooseRow(int xNum, int yNum) {
        if (xNum != lastXNum) {
            init();
        }
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
            startY = 0;
        }
        while (true) {
            XYNum xyNum = new XYNum(xNum, startY);
            if (isOK(xyNum)) {
                lastXNum = xNum;
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

    private int lastShowYNum = -1;
    private int lastShowYNumInSheet = -1;

    public int getShowYNum(final int yNum) {
        if (yNum == lastShowYNum) {
            return yNum;
        }
        int startY = 0;
        int distance = 0;
        boolean toBig = false;
        if (yNum > lastShowYNum) {
            toBig = true;
            startY = lastShowYNumInSheet + 1;
            distance = yNum - lastShowYNum;
        } else {
            toBig = false;
            startY = lastShowYNumInSheet - 1;
            distance = lastShowYNum - yNum;
        }
        startY = startY < startYNum ? startYNum : startY;
        if (startY < 0) {
            startY = 0;
        }
        while (true) {
            XYNum xyNum = new XYNum(0, startY);
            if (isOK(xyNum)) {
                lastShowYNum = yNum;
                lastShowYNumInSheet = startY;
                --distance;
                if (distance <= 0) {
                    return xyNum.getYNum();
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
    public void setEndYNum(int endYNum) {
        if (sheet == null) {
            throw new IllegalStateException("setSheet !! ");
        }

        if (endYNum < sheet.getMaxRowPosition()) {
            this.endYNum = endYNum;
        } else {
            this.endYNum = sheet.getMaxRowPosition();
        }
        Set<Map.Entry<Integer, FilterMode>> set = filterModes.entrySet();
        for (Map.Entry<Integer, FilterMode> e : set) {
            e.getValue().setEndYNum(this.endYNum);
        }
        init();
    }


//    class LRULinkedHashMap<V> extends LinkedHashMap<Integer, V> {
//        private static final int INITIAL_CAPACITY = 20;
//        private int maxAcount;
//        private static final float DEFAULT_LOAD_FACTOR = 0.75f;
//
//        public LRULinkedHashMap(int maxAcount) {
//            super(INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
//            this.maxAcount = maxAcount;
//        }
//
//        @Override
//        protected boolean removeEldestEntry(java.util.Map.Entry<Integer, V> eldest) {
//            if (size() > maxAcount) {
//                return true;
//            }
//            return false;
//        }
//    }
}
