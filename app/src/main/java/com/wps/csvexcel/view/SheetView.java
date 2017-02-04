package com.wps.csvexcel.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;
import com.wps.csvexcel.util.DrawCellViewUtil;
import com.wps.csvexcel.view.task.FilterHandlerTask;
import com.wps.csvexcel.bean.sheet.filter.FilterMode;
import com.wps.csvexcel.bean.sheet.filter.FilterMultValue;
import com.wps.csvexcel.util.CalculateScrollLength;
import com.wps.csvexcel.util.ToastUtil;
import com.wps.csvexcel.view.task.FilterSingleChosenRegionTask;
import com.wps.csvexcel.view.task.ShowFilterPopupWindowTask;

import java.util.*;

public class SheetView extends View {
    private String tag = "SheetView";
    private int canvasContentX, canvasContentY;
    private int sheetX, sheetY; // 表格坐标相对显示的坐标 (负无穷,0]
    private int leftSheetNumX, topSheetNumY;// 显示的左端点

    private int cellWidth = 200;
    private int cellHeight = 70;
    private int headListWidth = 180;
    private int headRowHeight = 70;
    private static final int CLICK_MAX_MOVE = 10;
    private static final Paint HEAD_FONT_PAINT = new Paint();
    private static final Paint HEAD_FRAME_PAINT = new Paint();
    private static final Paint HEAD_BACKGROUND_PAINT = new Paint();
    private CalculateScrollLength calculateScrollLength = new CalculateScrollLength();

    private Sheet sheet;
    private FilterViewManager filterViewManager = new FilterViewManager();
    private int width, height;
    private int drawYStartNum = 0;
    private ChosenRegion chosenRegion = new ChosenRegion();
    private Map<Integer, Integer> filterSheetShowMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> filterShowSheetMap = new HashMap<Integer, Integer>();
    private boolean hasFilterMode = false;
    private static final int GET_FILTER_AMOUNT = 30;
    private FilterHandlerTask filterHandlerTask = new FilterHandlerTask(this, (Activity) this.getContext(), sheet);
    private ShowFilterPopupWindowTask showFilterPopupWindowTask;

    public SheetView(Context context) {
        super(context);
        setup();
    }

    public SheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                height = getMeasuredHeight();
                width = getMeasuredWidth();
                return true;
            }
        });
        getFilterHandler().setSheet(sheet);
        getFilterHandler().setStartYNum(0);
        initPaint();
    }

    public boolean setFilter(boolean isFilter) {
        if (showFilterPopupWindowTask == null) {
            showFilterPopupWindowTask = new ShowFilterPopupWindowTask((Activity) this.getContext(), this, sheet);
        }
        boolean result = false;
        if (!isFilter) {
            backInit();
            result = false;
        } else {
            result = chooseSomethingToFilter();
        }
        invalidate();
        return result;
    }


    private boolean chooseSomethingToFilter() {
        if (chosenRegion.isSingle()) {
            final int xShowNum = chosenRegion.getStartXNum();
            final int yShowNum = chosenRegion.getStartYNum();
            final XYNum xyNum = getSheetNumFromShowNum(xShowNum, yShowNum);
            FilterSingleChosenRegionTask task = new FilterSingleChosenRegionTask((Activity) this.getContext(), this, sheet, xyNum.getXNum(), xyNum.getYNum());
            task.start();
            return true;
        } else {
            if (chosenRegion.getStartXNum() >= 0 && chosenRegion.getStartYNum() >= 0 && chosenRegion.getStartXNum() <= sheet.getMaxListPosition()
                    && chosenRegion.getStartYNum() <= sheet.getMaxRowPosition()) {
                for (int i = chosenRegion.getStartXNum(); i <= chosenRegion
                        .getEndXNum() && i <= sheet.getMaxListPosition(); i++) {
                    Log.v(tag, getDetail());
                    addFilterView(i, chosenRegion.getStartYNum());
                }
                return true;

            } else {
                ToastUtil.show(this.getContext(), "请选择数据区域");
            }
        }
        return false;
    }

    // 相对移动
    private void move(int x, int y) {
//        Log.e("move : ", " y: " + y);
        sheetX += x;
        sheetY += y;
        if (sheetX > 0) {
            sheetX = 0;
        }
        if (sheetY > 0) {
            sheetY = 0;
        }
    }

    private void initPaint() {
        HEAD_FONT_PAINT.setColor(Color.GRAY);
        HEAD_FONT_PAINT.setTextSize((float) (0.7 * headRowHeight));
        HEAD_FONT_PAINT.setTextAlign(Align.CENTER);
        HEAD_BACKGROUND_PAINT.setColor(Color.rgb(222, 222, 222));
        HEAD_BACKGROUND_PAINT.setStyle(Paint.Style.FILL_AND_STROKE);
        HEAD_FRAME_PAINT.setColor(Color.LTGRAY);
        HEAD_FRAME_PAINT.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        refrashLeftTopCellNum();
        drawContent(canvas);
        filterViewManager.drawFilterView(canvas);
        chosenRegion.draw(canvas);
        drawHead(canvas);
        drawScroll();
    }


    // 更新坐标系和左顶点
    private void refrashLeftTopCellNum() {
        canvasContentX = -sheetX - headListWidth;
        canvasContentY = -sheetY - headRowHeight;
        leftSheetNumX = canvasContentX / cellWidth - 1 < 0 ? 0 : canvasContentX
                / cellWidth - 1;
        topSheetNumY = canvasContentY / cellHeight - 1 < 0 ? 0 : canvasContentY
                / cellHeight - 1;
    }

    private void drawHead(Canvas canvas) {
        int i = 0, left = (leftSheetNumX + i) * cellWidth - canvasContentX;
        while (left < width) {
            CellView.drawBackground(canvas, left, 0, cellWidth, headRowHeight,
                    HEAD_BACKGROUND_PAINT);
            CellView.draw(getHeadRowCell(leftSheetNumX + i), canvas, left, 0,
                    cellWidth, headRowHeight, HEAD_FONT_PAINT,
                    HEAD_FRAME_PAINT);
            ++i;
            left += cellWidth;
        }
        // 画侧列
        int top = topSheetNumY * cellHeight - canvasContentY;
        i = 0;
        while (top < height) {
            if (topSheetNumY + i < getDrawYStartNum()) {
                CellView.drawBackground(canvas, 0, top, headListWidth,
                        cellHeight, HEAD_BACKGROUND_PAINT);
                CellView.draw(new Cell("" + (topSheetNumY + i + 1)), canvas, 0,
                        top, headListWidth, cellHeight,  HEAD_FONT_PAINT,
                        HEAD_FRAME_PAINT);
            } else {
                CellView.drawBackground(canvas, 0, top, headListWidth,
                        cellHeight, HEAD_BACKGROUND_PAINT);
                final int row = getSheetNumFromShowNum(0, topSheetNumY + i)
                        .getYNum();
                CellView.draw(new Cell("" + (row + 1)), canvas, 0, top,
                        headListWidth, cellHeight, HEAD_FONT_PAINT,
                        HEAD_FRAME_PAINT);
            }
            ++i;
            top += cellHeight;
        }

        // 画左顶点空格
        CellView.drawBackground(canvas, 0, 0, headListWidth, headRowHeight,
                HEAD_BACKGROUND_PAINT);
        CellView.draw(new Cell(), canvas, 0, 0, headListWidth, headRowHeight,
                HEAD_FONT_PAINT, HEAD_FRAME_PAINT);

    }

    private void drawContent(Canvas canvas) {
        int i = 0;
        int top = topSheetNumY * cellHeight - canvasContentY;
        final int lineLeft = cellWidth * leftSheetNumX - canvasContentX;
        while (top < height) {
            XYNum xyNum = getSheetNumFromShowNum(leftSheetNumX, topSheetNumY + i);
            drawOneRowCells(canvas, xyNum, top, lineLeft);
            ++i;
            top += cellHeight;
        }
    }

    private void drawOneRowCells(Canvas canvas, XYNum xyNum, int top, int left) {
        for (int i = 0; left < width; i++) {
            CellView cv = getCellView(xyNum.getXNum() + i, xyNum.getYNum());
            if (xyNum.getYNum() == 0) {
                Log.e("", "");
            }
            cv.setLeft(left);
            cv.setTop(top);
            cv.draw(canvas);
            left += cv.getWidth();
        }
    }

    private void drawScroll() {
        int moveX = 0, moveY = 0;
        boolean isScroll = false;
        if (calculateScrollLength.isScrollX()) {
            moveX = calculateScrollLength.getCurrentMoveX();
            isScroll = true;
        }
        if (calculateScrollLength.isScrollY()) {
            moveY = calculateScrollLength.getCurrentMoveY();
            isScroll = true;
        }
        if (isScroll) {
            move(moveX, moveY);
            invalidate();
        }
    }

    private float curDownX, curDownY;
    private float lastMoveX, lastMoveY;
    private VelocityTracker velocityTracker;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curDownX = event.getX();
                curDownY = event.getY();
                lastMoveX = curDownX;
                lastMoveY = curDownY;
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                if (chosenRegion.onTouchEvent(event))
                    return true;
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                if (chosenRegion.isHandlerOnTouchEvent()) {
                    chosenRegion.onTouchEvent(event);
                    break;
                }
                move((int) (event.getX() - lastMoveX), (int) (event.getY() - lastMoveY));
                if (sheetX > 0) {
                    sheetX = 0;
                }
                if (sheetY > 0) {
                    sheetY = 0;
                }
                lastMoveX = event.getX();
                lastMoveY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (chosenRegion.isHandlerOnTouchEvent()) {
                    chosenRegion.onTouchEvent(event);
                    break;
                }
                if (Math.abs(event.getX() - curDownX) < CLICK_MAX_MOVE
                        && Math.abs(event.getY() - curDownY) < CLICK_MAX_MOVE) {
                    onClick(event.getX(), event.getY());
                    break;
                }
                velocityTracker.computeCurrentVelocity(1000);
                calculateScrollLength.startScroll((int) velocityTracker.getXVelocity(), (int) velocityTracker.getYVelocity());
                velocityTracker.recycle();
                break;
        }
        invalidate();
        return true;
    }

    public void onClick(float x, float y) {
        final int xShowNum = getShowXNumFromPosition(x);
        final int yShowNum = getShowYNumFromPosition(y);
        final int xSheetNum = getSheetNumFromShowNum(xShowNum, yShowNum)
                .getXNum();
        final int ySheetNum = getSheetNumFromShowNum(xShowNum, yShowNum)
                .getYNum();
        if (isFilterView(xSheetNum, ySheetNum)) {
            setDrawYStartNum(ySheetNum + 1);
            getFilterHandler().setStartYNum(getDrawYStartNum());
            showFilterPopupWindowTask.show((FilterMultValue) getFilterHandler().getFilterMode(xSheetNum), xSheetNum, ySheetNum);
        } else {
            chosenRegion.backInit();
            chosenRegion.setStartXNum(xSheetNum);
            chosenRegion.setEndXNum(xSheetNum);
            chosenRegion.setStartYNum(ySheetNum);
            chosenRegion.setEndYNum(ySheetNum);
        }
    }

    private Cell getHeadRowCell(int position) {
        if (position < 0) {
            return new Cell();
        } else {

            return new Cell(changeToWord(position));
        }
    }

    private String changeToWord(int x) {
        if (x < 0)
            return "";

        int a = x / 26;
        int b = x % 26;
        return changeToWord(a - 1) + (char) (b + 'A');
    }

    private int getShowXNumFromPosition(float x) {
        int cellNum = (int) ((x + canvasContentX) / cellWidth);
        if (cellNum <= 0) {
            return 0;
        } else {
            return (cellNum);
        }

    }

    private int getShowYNumFromPosition(float y) {
        int cellNum = (int) ((y + canvasContentY) / cellHeight);
        if (cellNum <= 0) {
            return 0;
        } else {
            return cellNum;
        }
    }


    private XYNum getSheetNumFromShowNum(int xNum, int yNum) {
        XYNum xyNum = new XYNum(xNum, yNum);
        if(yNum > sheet.getMaxRowPosition()){
            return xyNum;
        }
        if (yNum < getDrawYStartNum()) {
            return new XYNum(xNum, yNum);
        }
        Integer sheetNum = filterShowSheetMap.get(yNum);
        if (sheetNum != null) {
            return new XYNum(xNum, sheetNum);
        }
        if (hasFilterMode) {
            filterHandlerTask.filter(getDrawYStartNum(), sheet.getMaxRowPosition() + 1, new XYNum(xNum, yNum - getDrawYStartNum()), GET_FILTER_AMOUNT);
        }
        // Log.v(tag, yNum + "显示 在 sheetView中的行为 : " + rl.getYNum());
        return xyNum;
    }

    public CellPosition getCellPositionInShow(int xNum, int yNum) {
        int x = xNum * cellWidth - canvasContentX;
        int y = yNum * cellHeight - canvasContentY;
        CellPosition position = new CellPosition(x, y);
        return position;
    }

    public CellView getCellView(int x, int y) {
        return DrawCellViewUtil.getCellView(x, y);
    }


    private String getDetail() {
        return "Detail :\n drawYStartNum: " + getDrawYStartNum()
                + " sheet max X position:" + sheet.getMaxListPosition()
                + "max Y position:" + sheet.getMaxRowPosition()
                + "\n";
    }

    private Cell getCell(int x, int y) {
        Cell cell = sheet.getCell(x, y);
        if (cell != null && cell.getContent() == null) {
            return null;
        }
        return cell;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        DrawCellViewUtil.setSheet(sheet);
        getFilterHandler().setSheet(sheet);
    }

    public void addFilterMode(int num, FilterMode filterMode) {
        getFilterHandler().addFilterMode(num, filterMode);
        hasFilterMode = true;
    }

    public int getDrawYStartNum() {
        return drawYStartNum;
    }

    public void setDrawYStartNum(int drawYStartNum) {
        this.drawYStartNum = drawYStartNum;
    }

    public FilterHandlerTask getFilterHandler() {
        return filterHandlerTask;
    }

    private void backInit() {
        leftSheetNumX = 0;
        topSheetNumY = 0;
        setDrawYStartNum(0);
        clearFilterMode();
        clearFilterView();
        clearFilterMap();
    }

    public void clearFilterMap() {
        filterShowSheetMap.clear();
        filterSheetShowMap.clear();
    }

    private void clearFilterMode() {
        getFilterHandler().clear();
        hasFilterMode = false;
    }

    private void clearFilterView() {
        Log.e("", " clearFilterView !! ");
        filterViewManager.clear();
    }

    public void putInFilterMap(Integer showYNum, Integer sheetYNum) {
        filterShowSheetMap.put(showYNum, sheetYNum);
        filterSheetShowMap.put(sheetYNum, showYNum);
    }

    public void addFilterView(int xNum, int yNum) {
        filterViewManager.putView(xNum, yNum);
    }

    private boolean isFilterView(int xNum, int yNum) {
        return filterViewManager.isFilterView(xNum, yNum);
    }

    private class FilterViewManager {
        private int yNum;
        private int leftXNum;
        private int rightXNum;
        private final Paint FILTER_BACKGROUND_PAINT = new Paint();
        private final Paint FILTER_PAINT = new Paint();

        public FilterViewManager() {
            clear();
            FILTER_PAINT.setARGB(255, 23, 143, 57);
            FILTER_PAINT.setStrokeWidth(3);
            FILTER_BACKGROUND_PAINT.setColor(Color.WHITE);
            FILTER_BACKGROUND_PAINT.setStyle(Paint.Style.FILL);

            FILTER_BACKGROUND_PAINT.setColor(Color.WHITE);
            FILTER_BACKGROUND_PAINT.setStyle(Paint.Style.FILL);
        }

        public void clear() {
            yNum = -1;
            leftXNum = Integer.MAX_VALUE;
            rightXNum = -1;
        }

        public void putView(int xNum, int yNum) {
            if (this.yNum != yNum) {
                this.yNum = yNum;
            }
            if (xNum < leftXNum) {
                leftXNum = xNum;
            }
            if (xNum > rightXNum) {
                rightXNum = xNum;
            }
        }

        public boolean isFilterView(int xNum, int yNum) {
            if (xNum >= leftXNum && xNum <= rightXNum && yNum == this.yNum) {
                return true;
            }
            return false;
        }

        public void drawFilterView(Canvas canvas) {
            if (rightXNum < 0 || leftSheetNumX > rightXNum || topSheetNumY > yNum) {
                return;
            }

            for (int i = leftXNum; i <= rightXNum; i++) {
                CellPosition cp = getCellPositionInShow(i, yNum);
                if (cp.getX() > 0 ) {
                    if(cp.getX() > width){
                        break;
                    }
                    CellView cellView = DrawCellViewUtil.getCellView(i, yNum);
                    int left = cp.getX();
                    int top = cp.getY();
                    int width = cellView.getWidth();
                    int height = cellView.getHeight();
                    draw(canvas,width,height,left,top);
                }
            }
        }

        private void draw(Canvas canvas,int width,int height,int left,int top){
            canvas.drawRect(left + width - height, top, left + width, top
                    + height, FILTER_BACKGROUND_PAINT);

            FILTER_PAINT.setStyle(Paint.Style.STROKE);
            canvas.drawRect(left + width - height, top, left + width, top
                    + height, FILTER_BACKGROUND_PAINT);
            canvas.drawRect(left + width - height, top, left + width, top
                    + height, FILTER_PAINT);
            int distanceX = (int) (0.2 * height);
            int distanceY = (int) (0.35 * height);
            int filterX = left + width - height;
            Path path = new Path();
            path.moveTo(filterX + distanceX, top + distanceY);
            path.lineTo(left + width - distanceX, top + distanceY);
            path.lineTo((filterX + height / 2), top + height - distanceY);
            path.close();
            FILTER_PAINT.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, FILTER_PAINT);
        }

    }

    public static class CellPosition {
        private int x;
        private int y;

        public CellPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }


    class ChosenRegion {
        private int startXNum = -1, startYNum = -1;
        private int endXNum = Integer.MAX_VALUE, endYNum = Integer.MAX_VALUE;
        private static final int SQUARE_WIDTH = 10;
        private static final int TOUCH_WIDTH = 50;
        private Paint paint = new Paint();
        private Paint whitePaint = new Paint();
        private Paint grayPaint = new Paint();
        private boolean isChangeAreaLeft;
        private boolean changeArea;

        public ChosenRegion() {
            paint.setARGB(255, 27, 140, 55);
            paint.setStrokeWidth(3);
            whitePaint.setARGB(255, 255, 255, 255);
            whitePaint.setStrokeWidth(3);
            whitePaint.setStyle(Paint.Style.STROKE);
            grayPaint.setARGB(50, 207, 207, 207);
            grayPaint.setStyle(Paint.Style.FILL);
        }

        public void backInit() {
            startXNum = -1;
            startYNum = -1;
            endXNum = Integer.MAX_VALUE;
            endYNum = Integer.MAX_VALUE;
        }

        public void draw(Canvas canvas) {
//            Log.e(" ChosenRegion : ", " start x,y " + startXNum + ","
//            + startYNum + " end :" + endXNum + "," + endYNum);
            if (startYNum < 0 || endYNum < 0 || endXNum < 0 || endYNum < 0) {
                return;
            }

            final int yStartShowNum = chooseStartShowYNum();
            final int yEndShowNum = chooseEndShowYNum();
            if (yStartShowNum < 0 || yEndShowNum < 0 || yEndShowNum < topSheetNumY) {
                return;
            }
            final CellPosition startCp = getCellPositionInShow(startXNum,
                    yStartShowNum);
            final CellPosition endCp = getCellPositionInShow(endXNum + 1,
                    yEndShowNum + 1);
            canvas.drawRect(startCp.getX(), startCp.getY(), endCp.getX(),
                    endCp.getY(), grayPaint);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(startCp.getX(), startCp.getY(), endCp.getX(),
                    endCp.getY(), paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(startCp.getX() - SQUARE_WIDTH, startCp.getY()
                            - SQUARE_WIDTH, startCp.getX() + SQUARE_WIDTH,
                    startCp.getY() + SQUARE_WIDTH, paint);
            canvas.drawRect(startCp.getX() - SQUARE_WIDTH, startCp.getY()
                            - SQUARE_WIDTH, startCp.getX() + SQUARE_WIDTH,
                    startCp.getY() + SQUARE_WIDTH, whitePaint);
            canvas.drawRect(endCp.getX() - SQUARE_WIDTH, endCp.getY()
                    - SQUARE_WIDTH, endCp.getX() + SQUARE_WIDTH, endCp.getY()
                    + SQUARE_WIDTH, paint);
            canvas.drawRect(endCp.getX() - SQUARE_WIDTH, endCp.getY()
                    - SQUARE_WIDTH, endCp.getX() + SQUARE_WIDTH, endCp.getY()
                    + SQUARE_WIDTH, whitePaint);
        }

        private int chooseStartShowYNum() {
            Integer yStartShowNum = getShowYNumFromSheetNum(startYNum);
            int num = 1;
            while (yStartShowNum == null && (startYNum + num) <= endYNum) {
                yStartShowNum = getShowYNumFromSheetNum(startYNum + num++);
            }
            if (yStartShowNum != null) {
                return yStartShowNum;
            }
            return -1;
        }

        private Integer getShowYNumFromSheetNum(int yNum) {
            if (filterSheetShowMap.isEmpty()) {
                return yNum;
            }
            Integer showYNum = filterSheetShowMap.get(yNum);
            if (showYNum != null) {
                return showYNum;
            }
            return null;
        }

        private int chooseEndShowYNum() {
            Integer yEndShowNum = getShowYNumFromSheetNum(endYNum);
            int num = 1;
            while (yEndShowNum == null && (endYNum - num) >= startYNum) {
                yEndShowNum = getShowYNumFromSheetNum(endYNum - num++);
            }
            if (yEndShowNum != null) {
                return yEndShowNum;
            }
            return -1;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (startYNum < 0 || endYNum < 0 || endXNum < 0 || endYNum < 0) {
                return false;
            }
            final int startShowYNum = chooseStartShowYNum();
            final int endShowYNum = chooseEndShowYNum();
            CellPosition startCp = getCellPositionInShow(startXNum,
                    startShowYNum);
            CellPosition endCp = getCellPositionInShow(endXNum + 1,
                    endShowYNum + 1);
            Region leftSquare = new Region(startCp.getX() - TOUCH_WIDTH,
                    startCp.getY() - TOUCH_WIDTH, startCp.getX() + TOUCH_WIDTH,
                    startCp.getY() + TOUCH_WIDTH);
            Region rightSquare = new Region(endCp.getX() - TOUCH_WIDTH,
                    endCp.getY() - TOUCH_WIDTH, endCp.getX() + TOUCH_WIDTH,
                    endCp.getY() + TOUCH_WIDTH);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (leftSquare.isInRegion((int) event.getX(),
                            (int) event.getY())) {
                        changeArea = true;
                        isChangeAreaLeft = true;
                        return true;
                    } else {
                        if (rightSquare.isInRegion((int) event.getX(),
                                (int) event.getY())) {
                            changeArea = true;
                            isChangeAreaLeft = false;
                            return true;
                        } else {
                            // cancelChoose();
                        }

                    }
                    changeArea = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int xShowNum = getShowXNumFromPosition(event.getX());
                    final int yShowNum = getShowYNumFromPosition(event.getY());
                    final int xNum = xShowNum;
                    final int yNum = getSheetNumFromShowNum(xShowNum, yShowNum)
                            .getYNum();
                    if (isChangeAreaLeft) {
                        setStartXNum(xNum);
                        setStartYNum(yNum);
                    } else {
                        setEndXNum(xNum);
                        setEndYNum(yNum);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }

        public boolean isSingle() {
            if (startXNum == endXNum && startYNum == endYNum) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isHandlerOnTouchEvent() {
            if (changeArea) {
                return true;
            }
            return false;
        }

        public void setStartXNum(int xNum) {
            if (xNum > endXNum) {
                startXNum = endXNum;
                endXNum = xNum;
            } else {
                startXNum = xNum;
            }
        }

        public void setStartYNum(int yNum) {
            if (yNum > endYNum) {
                startYNum = endYNum;
                endYNum = yNum;
            } else {
                startYNum = yNum;
            }
        }

        public void setEndXNum(int xNum) {
            if (xNum < startXNum) {
                endXNum = startXNum;
                startXNum = xNum;
            } else {
                endXNum = xNum;
            }
        }

        public void setEndYNum(int yNum) {
            if (yNum < startYNum) {
                endYNum = startYNum;
                startYNum = yNum;
            } else {
                endYNum = yNum;
            }
        }

        public int getStartXNum() {
            return startXNum;
        }

        public int getStartYNum() {
            return startYNum;
        }

        public int getEndXNum() {
            return endXNum;
        }

        public int getEndYNum() {
            return endYNum;
        }

        class Region {
            int startX, startY;
            int endX, endY;

            public Region(int startX, int startY, int endX, int endY) {
                this.startX = startX;
                this.startY = startY;
                this.endX = endX;
                this.endY = endY;
            }

            public boolean isInRegion(int x, int y) {
                if (x >= startX && x <= endX && y >= startY && y <= endY) {
                    return true;
                }
                return false;
            }

        }

    }

}
