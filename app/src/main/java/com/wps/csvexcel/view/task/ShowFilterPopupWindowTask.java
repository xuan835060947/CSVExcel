package com.wps.csvexcel.view.task;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.wps.csvexcel.R;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;
import com.wps.csvexcel.bean.sheet.iterator.CellNoRepeatDealPartIterator;
import com.wps.csvexcel.bean.sheet.iterator.CellHeapSortIterator;
import com.wps.csvexcel.bean.sheet.filter.FilterMultValue;
import com.wps.csvexcel.tool.doublearraytool.CacheList;
import com.wps.csvexcel.util.SpeedTestUtil;
import com.wps.csvexcel.util.ProgressDialogUtil;
import com.wps.csvexcel.view.CellView;
import com.wps.csvexcel.view.SheetView;
import com.wps.csvexcel.view.adapter.FilterDataAdapter;

import java.util.Iterator;
import java.util.List;

/**
 * Created by kingsoft on 2015/8/7.
 */
public class ShowFilterPopupWindowTask extends AsyncTaskRun {
    private Sheet sheet;
    private int xNum, yNum;
    private Activity activity;
    private FilterMultValue filterMultValue;
    private SheetView sheetView;
    private ProgressDialogUtil progressDialogUtil;
    private static final int START_GET_LIST = 5;
    private static final int FINISH_GET_LIST = 60;
    private static final int FINISH_GET_NO_REPEAT = 70;
    private static final int FINISH_SORT_LIST = 90;
    private static final int BIGGER_ROW_NUM = 6000;
    private PopupWindow popupWindow;
    private FilterDataAdapter filterDataAdapter;
    private int currentFilterPopupWindowNum;

    public ShowFilterPopupWindowTask(Activity activity, SheetView sheetView, Sheet sheet) {
        super(activity);
        this.activity = activity;
        this.sheetView = sheetView;
        this.sheet = sheet;
    }

    public void show(FilterMultValue filterMultValue, int xNum, int yNum) {
        this.filterMultValue = filterMultValue;
        this.xNum = xNum;
        this.yNum = yNum;
        if (filterMultValue != null && isCurrentFilterPopupWindow(xNum)) {
            showPopup(xNum, yNum);
            return;
        }
        progressDialogUtil = new ProgressDialogUtil(activity, "正在获取列...");
        progressDialogUtil.show();
        execute();
    }

    @Override
    protected void doInBackground() {
        changeFilterMultValue();
    }

    @Override
    protected void onProgressUpdate(int values) {
        if (values == FINISH_GET_NO_REPEAT) {
            progressDialogUtil.setMessage("正在排序列...");
        }
        progressDialogUtil.setProgress(values);
    }

    @Override
    protected void finishToDoInUIThread() {
        progressDialogUtil.cancle();
        showPopup(xNum, yNum);
    }

    private void showPopup(final int xNum, int yNum) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.popup_window_filter, null);
        Switch sw = (Switch) view.findViewById(R.id.switchSingleOrMult);
        if (filterMultValue.isSingleFilter()) {
            sw.setChecked(true);
        }
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!filterMultValue.isSingleFilter()) {
                    filterMultValue.setSingle(true);
                } else {
                    filterMultValue.setSingle(false);
                }
                filterDataAdapter.notifyDataSetChanged();
            }
        });
        ListView lv = (ListView) view.findViewById(R.id.lvContents);
        filterDataAdapter = new FilterDataAdapter(activity,
                filterMultValue);
        lv.setAdapter(filterDataAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                filterMultValue.setChoice(position);
                if (filterMultValue.isSingleFilter()) {
                    popupWindow.dismiss();
                }
                filterDataAdapter.notifyDataSetChanged();
            }
        });
        popupWindow = new PopupWindow(view,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                sheetView.clearFilterMap();
                sheetView.addFilterMode(xNum, filterMultValue);
                sheetView.invalidate();
            }

        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        int[] location = new int[2];
        sheetView.getLocationOnScreen(location);
        SheetView.CellPosition cp = sheetView.getCellPositionInShow(xNum, yNum);
        CellView cv = sheetView.getCellView(xNum, yNum);
        final int locationX = location[0] + cp.getX()
                + cv.getWidth() / 2;
        final int locationY = location[1] + cp.getY() + cv.getHeight();
        popupWindow.showAtLocation(sheetView, Gravity.NO_GRAVITY, locationX, locationY);
    }


    private void changeFilterMultValue() {
        SpeedTestUtil.start("changeFilterMultValue");
        int startYNum = yNum + 1;
        Iterator<Cell> sortIt = getSortList();
        final Iterator<Cell> noRepeat = new CellNoRepeatDealPartIterator(sortIt);
        Iterator<Cell> shouldShowIterator;
        shouldShowIterator = new ShouldShowIterator(noRepeat);
        if (filterMultValue == null) {
            filterMultValue = new FilterMultValue(shouldShowIterator, xNum, this);
        } else {
            filterMultValue.filter(shouldShowIterator, xNum);
            filterMultValue.setStartYNum(startYNum);
        }
        SpeedTestUtil.end("changeFilterMultValue");
    }

    private Iterator<Cell> getSortList() {
        int startYNum = yNum + 1;
        Iterator<Cell> sortIt;
        if (sheet.sizeY() <= BIGGER_ROW_NUM) {
            updateProgress(START_GET_LIST);
            final Cell[] cells = sheet.getListArray(xNum, startYNum, sheet.sizeY() - startYNum, true);
            updateProgress(FINISH_GET_LIST);

            updateProgress(FINISH_GET_NO_REPEAT);
            sortIt = new CellHeapSortIterator(cells);
            updateProgress(FINISH_SORT_LIST);
        } else {
            updateProgress(START_GET_LIST);
            final List<Cell> cells = sheet.getCacheList(xNum, startYNum, sheet.sizeY() - startYNum, true);
            updateProgress(FINISH_GET_LIST);

            updateProgress(FINISH_GET_NO_REPEAT);
            sortIt = new CellHeapSortIterator(cells);
            updateProgress(FINISH_SORT_LIST);
        }
        return sortIt;
    }

    public void setCurrentFilterPopupWindowNum(int num) {
        this.currentFilterPopupWindowNum = num;
    }

    public boolean isCurrentFilterPopupWindow(int num) {
        if (currentFilterPopupWindowNum == num) {
            return true;
        }
        return false;
    }

    public class ShouldShowIterator implements Iterator<Cell> {
        private Iterator<Cell> originIterator;
        private Cell next;
        private boolean hasNext;

        public ShouldShowIterator(Iterator<Cell> originIterator) {
            this.originIterator = originIterator;
            next();
        }

        @Override
        public boolean hasNext() {//不准确
            return hasNext;
        }

        @Override
        public Cell next() {
            Cell currentCell = next;
            if (originIterator.hasNext()) {
                Cell cell = originIterator.next();
                Log.e("cell", "" + cell.getContent());
                boolean isOk = sheetView.getFilterHandler().isOK((XYNum) cell.getTag());
                while (originIterator.hasNext() && !isOk) {
                    cell = originIterator.next();
                    isOk = sheetView.getFilterHandler().isOK((XYNum) cell.getTag());
                }
                if (isOk) {
                    hasNext = true;
                    next = cell;
                } else {
                    hasNext = false;
                }
            } else {
                hasNext = false;
            }
            return currentCell;
        }


        @Override
        public void remove() {
            throw new IllegalArgumentException("the method can not be used!");
        }

    }

}
