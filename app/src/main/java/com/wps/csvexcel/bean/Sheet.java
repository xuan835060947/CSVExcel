package com.wps.csvexcel.bean;

import java.io.*;
import java.util.List;

import android.util.Log;
import com.wps.csvexcel.bean.sheet.iterator.*;

import com.wps.csvexcel.util.ioutil.IOUtil;
import com.wps.csvexcel.tool.doublearraytool.*;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ChangeFileAndData;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ReadWriteTool;
import com.wps.csvexcel.util.SplitUtil;
import com.wps.csvexcel.util.ioutil.BufferedReaderWrap;


public class Sheet {
    private static final int DEFAULT_BLOCK_WIDTH = 10;
    private static final int DEFAULT_BLOCK_HEIGHT = 300;
    private static final int DEFAULT_CACHE_BLOCK_AMOUNT = 4;
    public static final int STATE_OK = 1000;
    public static final int STATE_FILE_NOT_EXIST = 1001;
    public static final int STATE_FILE_READ_ERROR = 1002;
    public static final int maxListAmount = 255;

    private int state = STATE_OK;
    private int maxRowPosition = -1, maxListPosition = -1;
    private final int blockWidth;
    private final int blockHeight;
    private final int blockArea;
    private int blockAmountX, blockAmountY;
    private DoubleArray<Boolean> hadInsertBlocks = new DoubleArray<Boolean>(20, 100);
    private String filePath;
    private CacheDoubleArray<Cell[]> allCells;
    private OnGetBlockListener getBlockListener;
    private int bufferLength = 2048;
    private char[] buffer = new char[bufferLength];
    public static final String ENCODING = "GBK";

    public Sheet(String path) {
        this(path, DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_HEIGHT,
                DEFAULT_CACHE_BLOCK_AMOUNT);
    }

    public Sheet(String path, int blockWidth, int blockHeight,
                 int cacheBlockAmount) {
        this.filePath = path;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.blockArea = blockWidth * blockHeight;
        initMaxRowList();
        allCells = new CacheDoubleArray<Cell[]>(cacheBlockAmount, new ReadWriteTool<Cell[]>(changeFileAndData), changeFileAndData);
        allCells.setSizeX((maxListPosition + 1) / getBlockWidth() + 1);
        allCells.setSizeY(((maxRowPosition + 1) / getBlockHeight()) + 1);
        Log.e("Sheet"," size X: "+sizeX()+"   sixe Y: "+sizeY());
    }


    public Cell getCell(int listPosition, int rowPosition) {
        if (listPosition > getMaxListPosition()
                || rowPosition > getMaxRowPosition()) {
            return null;
        }

        final int blockXNum = getBlockXNum(listPosition);
        final int blockYNum = getBlockYNum(rowPosition);
        Cell[] cells = getBlock(blockXNum, blockYNum);
        if (cells != null) {
            final int num = getNumInBlock(listPosition, rowPosition);
            return cells[num];
        }
        return null;
    }

    public Cell[] getBlock(int blockXNum, int blockYNum) {
        Cell[] cells = allCells.get(blockXNum, blockYNum);
        if (getBlockListener != null) {
            getBlockListener.onGetBlock(blockXNum, blockYNum);
        }
        return cells;
    }

    public int getNumInBlock(int locationX, int locationY) {
        int xNum = locationX % blockWidth;
        int yNum = locationY % blockHeight;
        return yNum * blockWidth + xNum;
    }

    public int getBlockXNum(int locationX) {
        return locationX / blockWidth;
    }

    public int getBlockYNum(int locationY) {
        return locationY / blockHeight;
    }

    public void insertCell(int listPosition, int rowPosition, Cell newCell) {
        if (rowPosition > maxRowPosition || listPosition > maxListPosition) {
            if (newCell != null && newCell.getContent() != null) {
                if (rowPosition > maxRowPosition) {
                    setMaxRowPosition(rowPosition);
                }
                if (listPosition > maxListPosition) {
                    setMaxListPosition(listPosition);
                }

            }
        }
        Cell[] cells = getBlock(getBlockXNum(listPosition), getBlockYNum(rowPosition));
        cells[getNumInBlock(listPosition, rowPosition)] = newCell;
        allCells.insertTo(listPosition, rowPosition, cells);
    }

    public synchronized boolean isBlockInsert(int blockXNum, int blockYNum) {
        Boolean isInsert = hadInsertBlocks.get(blockXNum, blockYNum);
        isInsert = isInsert == null ? false : isInsert;
        return isInsert;
    }

    public synchronized void setBlockInsert(int blockXNum, int blockYNum,
                                            boolean isInsert) {
        if (blockXNum < 0 || blockYNum < 0) {
            throw new IllegalArgumentException("blockXNum: " + blockXNum
                    + " blockYNum : " + blockYNum);
        }
        if (isInsert) {
            hadInsertBlocks.insertTo(blockXNum, blockYNum, Boolean.TRUE);
        } else {
            hadInsertBlocks.insertTo(blockXNum, blockYNum, Boolean.FALSE);
        }
    }

    public void writeBlocks(int blockXNum, int blockYNum, Object[] arr) {
        allCells.save(blockXNum, blockYNum, new CacheDoubleArray.Element(arr, true));
        setBlockInsert(blockXNum, blockYNum, true);
    }

    private SplitUtil.StringArrLength splitString(char[] arr, int endPos) {

        return SplitUtil.splitString(arr, endPos, 0, maxListAmount);
    }

    private String getSelfDetail() {
        return "Sheet's detail message :max X : " + maxListPosition + "  Y : "
                + maxRowPosition;
    }

    public int getBlockWidth() {
        return blockWidth;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    private BufferedReaderWrap getReaderWrapToLine(int line) throws IOException {
        return IOUtil.getReaderWrapToLine(filePath, line, ENCODING);
    }

    private int getLineToBuffer(BufferedReaderWrap reader) throws IOException {
        int endPos = 0;
        while (true) {
            endPos = reader.readCharLine(buffer);
            if (endPos > 0) {
                return endPos;
            } else {
                switch (endPos) {
                    case BufferedReaderWrap.NO_ANY_LINE:
                        return BufferedReaderWrap.NO_ANY_LINE;
                    case BufferedReaderWrap.EXCEED_BRESULT:
                        bufferLength *= 2;
                        buffer = new char[bufferLength];
                        break;
                }
            }
        }
    }

    private void initMaxRowList() {
        try {
            File file = new File(filePath);
            BufferedReaderWrap reader = null;
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), ENCODING);
                reader = new BufferedReaderWrap(read);
                String lineText = null;
                int i = 0;
                int maxInitRowNum = 30;
                int endPos = 0;
                for (; (endPos = getLineToBuffer(reader)) > 0; i++) {
                    if (i < maxInitRowNum) {
                        SplitUtil.StringArrLength sal = splitString(buffer, endPos);
                        if (maxListPosition < sal.getStringTotalAmount() - 1) {
                            setMaxListPosition(sal.getStringTotalAmount() - 1);
                        }
                    }
                }
                setMaxRowPosition(i - 1);
                reader.close();
            } else {
                Log.e("--XUAN--", "文件不存在");
                state = STATE_FILE_NOT_EXIST;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("--XUAN--", "文件读取失败");
            state = STATE_FILE_READ_ERROR;
        }
    }

    public Cell[] getListArray(int listNum, final int startRowNum, int length) {
        return getListArray(listNum, startRowNum, length, false);
    }

    public Cell[] getListArray(int listNum, final int startRowNum, int length, final boolean needTag) {
        final Cell[] cells = new Cell[length];
        getRegionCellIterator(new FindCellListener() {
            @Override
            public void onFindCell(int xNum, int yNum, Cell cell) {
                if (needTag == true) {
                    cell.setTag(new XYNum(xNum, yNum));
                }
                cells[yNum - startRowNum] = cell;
            }
        }, listNum, startRowNum, 1, length).iterator();
        return cells;
    }

    public List<Cell> getCacheList(int listNum, final int startRowNum, int length) {
        return getCacheList(listNum, startRowNum, length, false);
    }

    public List<Cell> getCacheList(int listNum, final int startRowNum, int length, final boolean needTag) {
        final CacheList<Cell> cells = new CacheList<Cell>();
        getRegionCellIterator(new FindCellListener() {
            @Override
            public void onFindCell(int xNum, int yNum, Cell cell) {
                if (needTag == true) {
                    cell.setTag(new XYNum(xNum, yNum));
                }
                cells.set(yNum - startRowNum, cell);
            }
        }, listNum, startRowNum, 1, length).iterator();
        return cells;
    }

    public FastIterator<Cell[]> iteratorBlockList(int writeBlockXNum, int startBlockYNum) {
        return new BlockIterator(writeBlockXNum, startBlockYNum);
    }

    public FastIterator<Cell> getListIteratorFast(int listNum, int startRowNum) {
        return new FastCellIerator(this, listNum, startRowNum);
    }

    public FastIterator<Cell> getListIteratorFast(int listNum, int startRowNum, boolean needRowListTag) {
        return new FastCellIerator(this, listNum, startRowNum, needRowListTag);
    }

    public RegionCellIterator getRegionCellIterator(FindCellListener findCellListener, int startXNum, int startYNum, int xLength, int yLength) {
        if (startXNum + xLength > sizeX() && startYNum + yLength > sizeY()) {
            throw new IllegalArgumentException(getSelfDetail() + " but was: startXNum: " + startXNum + " startYNum " + startYNum + " xLength " + xLength + "  yLength " + yLength);
        }
        return new RegionCellIterator(findCellListener, startXNum, startYNum, xLength, yLength);
    }

    public int getState() {
        return state;
    }

    public int getMaxRowPosition() {
        return maxRowPosition;
    }

    public int getMaxListPosition() {
        return maxListPosition;
    }

    private void setMaxRowPosition(int maxRowPosition) {
        this.maxRowPosition = maxRowPosition;
        int blockCellAmount = getBlockHeight() * blockAmountY;
        while (maxRowPosition >= blockCellAmount) {
            ++blockAmountY;
            blockCellAmount = getBlockHeight() * blockAmountY;
        }
    }

    private void setMaxListPosition(int maxListPosition) {
        this.maxListPosition = maxListPosition;
        int blockCellAmount = getBlockWidth() * blockAmountX;
        while (maxListPosition >= blockCellAmount) {
            ++blockAmountX;
            blockCellAmount = getBlockWidth() * blockAmountX;
        }
    }

    public void setOnGetBlockListener(OnGetBlockListener getBlockListener) {
        this.getBlockListener = getBlockListener;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getBlockAmountX() {
        return blockAmountX;
    }

    public int getBlockAmountY() {
        return blockAmountY;
    }

    public int sizeX() {
        return maxListPosition + 1;
    }

    public int sizeY() {
        return maxRowPosition + 1;
    }

    private ChangeFileAndData<Cell[]> changeFileAndData = new ChangeFileAndData<Cell[]>() {
        @Override
        public Cell[] readDataFromFile(int xNum, int yNum, File file) {
//            Log.v("","read From File : x : "+xNum+"  y : "+yNum);
            BufferedReaderWrap reader = null;
            try {

                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), ENCODING);
                reader = new BufferedReaderWrap(read);
                int endPos = 0;
                Cell[] cells = new Cell[blockWidth * blockHeight];
                int startNum = 0;
                int endNum = startNum + blockWidth;
                while (endNum <= blockArea && (endPos = reader.readCharLine(buffer)) != BufferedReaderWrap.NO_ANY_LINE) {
                    if (endPos > 0) {
                        SplitUtil.StringArrLength stringArrLength = splitString(buffer, endPos);
                        for (int j = startNum, k = 0; j < endNum; j++, k++) {
                            if (k < stringArrLength.getArrElementAmount()) {
                                cells[j] = new Cell(stringArrLength.getArr()[k]);
                            } else {
                                break;
                            }
                        }
                        startNum = endNum;
                        endNum += blockWidth;
                    } else {
                        if (endPos == BufferedReaderWrap.EXCEED_BRESULT) {
                            bufferLength *= 2;
                            buffer = new char[bufferLength];
                        }
                    }
                }
                reader.close();
                return cells;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void writeDataInFile(int xNum, int yNum, Object object, File file) {
            //简单规则
            Cell[] cells = (Cell[]) object;
            int startNum = 0;
            int endNum = startNum + blockWidth;
            StringBuilder sb = new StringBuilder();
            while (endNum <= blockArea) {
                for (int j = startNum; j < endNum; j++) {
                    if (cells != null) {
                        if (cells[j] != null) {
                            if (cells[j].getContent() != null) {
                                sb.append(cells[j].getContent());
                            }
                            sb.append(",");
                        }
                    } else {
                        break;
                    }
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("\n");
                startNum = endNum;
                endNum += blockWidth;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
//                byte[] bytes = br.toString().getBytes();
                br.write(sb.toString());
                br.flush();
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            setBlockInsert(xNum, yNum, true);
        }

        @Override
        public Cell[] getData(int xNum, int yNum) {
            final int left = xNum * blockWidth;
            final int right = left + blockWidth;
            final int top = yNum * blockHeight;
            final int bottom = top + blockHeight;
            Cell[] cells = new Cell[blockArea];
            BufferedReaderWrap reader = null;
            try {
                reader = getReaderWrapToLine(top);
                int endPos = 0;
                for (int i = top; i < bottom && (endPos = getLineToBuffer(reader)) > 0; i++) {
                    SplitUtil.StringArrLength sal = SplitUtil.splitString(buffer, endPos, left, right);
                    String[] arr = sal.getArr();
                    int length = sal.getArrElementAmount();
                    if (sal.getStringTotalAmount() - 1 > maxListPosition) {
                        setMaxListPosition(sal.getStringTotalAmount() - 1);
                    }
                    for (int j = 0; j < length; j++) {
                        cells[(i - top) * blockWidth + j] = new Cell(arr[j]);
                    }
                }
//                Log.e("getData", "xNum  " + xNum + "  yNum : " + yNum + "  content " + cells[0].getContent());
                reader.close();
                return cells;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }
    };

    class BlockIterator implements FastIterator<Cell[]> {
        private int readBlockNum = 0;
        private final int blockXNum;
        private int maxBlockYNum = getMaxRowPosition() / getBlockHeight();
        private BufferedReaderWrap reader;
        private boolean isClose;

        public BlockIterator(int blockXNum, int startBlockYNum) {
            this.blockXNum = blockXNum;
            this.readBlockNum = startBlockYNum;
            try {
                reader = getReaderWrapToLine(startBlockYNum * getBlockHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(" IteratorBlock  ", " IteratorBlock  start !! ");
        }

        @Override
        public boolean hasNext() {
            if (isClose) {
                return false;
            }
            if (readBlockNum <= maxBlockYNum) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Cell[] next() {
            ++readBlockNum;
            return getBlock();
        }

        @Override
        public void finish() {
            try {
                if (!isClose) {
                    isClose = true;
                    reader.close();
                    Log.e(" IteratorBlock  ", " IteratorBlock  finish !! ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Cell[] getBlock() {
            final int left = blockXNum * blockWidth;
            final int right = left + blockWidth;
            final int top = 0;
            final int bottom = top + blockHeight;
            Cell[] cells = new Cell[blockArea];
            try {
                int endPos = 0;
                for (int i = top; i < bottom && (endPos = getLineToBuffer(reader)) > 0; i++) {
                    SplitUtil.StringArrLength sal = SplitUtil.splitString(buffer, endPos, left, right, false);
                    String[] arr = sal.getArr();
                    int length = sal.getArrElementAmount();
                    for (int j = 0; j < length; j++) {
                        cells[(i - top) * blockWidth + j] = new Cell(arr[j]);
                    }
                }
                return cells;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public void remove() {
            throw new IllegalArgumentException("the mothod can not used !");
        }
    }

    public class RegionCellIterator {
        private FindCellListener findCellListener;
        private int startXNum, startYNum;
        private int xLength, yLength;
        private BufferedReaderWrap reader;

        public RegionCellIterator(FindCellListener findCellListener, int startXNum, int startYNum, int xLength, int yLength) {
            this.findCellListener = findCellListener;
            this.startXNum = startXNum;
            this.startYNum = startYNum;
            this.xLength = xLength;
            this.yLength = yLength;
        }

        public void iterator() {
            final int left = startXNum;
            final int right = left + xLength;
            final int top = startYNum;
            final int bottom = top + yLength;
            try {
                reader = getReaderWrapToLine(startYNum);
                int endPos = 0;
                for (int i = top; i < bottom && (endPos = getLineToBuffer(reader)) > 0; i++) {
                    SplitUtil.StringArrLength sal = SplitUtil.splitString(buffer, endPos, left, right, false);
                    String[] arr = sal.getArr();
                    int length = sal.getArrElementAmount();
                    for (int j = 0; startXNum + j < right; j++) {
                        if (j < length) {
                            findCellListener.onFindCell(startXNum + j, i, new Cell(arr[j]));
                        } else {
                            findCellListener.onFindCell(startXNum + j, i, new Cell());
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public interface FindCellListener {
        public void onFindCell(int xNum, int yNum, Cell cell);
    }

    public interface OnGetBlockListener {
        public void onGetBlock(int blockXNum, int blockYNum);
    }
}
