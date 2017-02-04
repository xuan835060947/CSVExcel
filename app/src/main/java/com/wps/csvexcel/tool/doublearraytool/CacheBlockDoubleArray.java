package com.wps.csvexcel.tool.doublearraytool;


import com.wps.csvexcel.tool.doublearraytool.readwrite.CacheConcurrentReadWrite;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ChangeFileAndData;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ReadWrite;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ReadWriteTool;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author w_chenxiaoxuan
 */
public class CacheBlockDoubleArray<T> {
    private int blockWidth;
    private int blockHeight;
    private int blockArea;
    private int sizeX, sizeY;
    private static final int DEFAULT_CACHE_BLOCK_AMOUNT = 4;
    private static final int DEFAULT_BLOCK_WIDTH = 20;
    private static final int DEFAULT_BLOCK__HEIGHT = 100;
    private LinkedHashMap<Integer, Block> blocks;
    private CacheDoubleArray<T[]> cacheDoubleArray;
    private IntegerXYKey integerXYKey = new IntegerXYKey();


    public CacheBlockDoubleArray() {
        this(DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK__HEIGHT, DEFAULT_CACHE_BLOCK_AMOUNT, null);
    }

    public CacheBlockDoubleArray(int blockWidth) {
        this(blockWidth, blockWidth);
    }

    public CacheBlockDoubleArray(int blockWidth, int blockHeight) {
        this(blockWidth, blockHeight, DEFAULT_CACHE_BLOCK_AMOUNT, null);
    }

    public CacheBlockDoubleArray(int blockWidth, int blockHeight,
                                 int cacheBlockAmount, ChangeFileAndData<T[]> changeFileAndData) {
        this(blockWidth, blockHeight, cacheBlockAmount, changeFileAndData, true);
    }

    public CacheBlockDoubleArray(int blockWidth, int blockHeight,
                                 int cacheBlockAmount, ChangeFileAndData<T[]> changeFileAndData, boolean openConcurrentWrite) {
        if (blockWidth <= 0 || blockHeight <= 0 || cacheBlockAmount < 0
                ) {
            throw new IllegalArgumentException("blockWidth" + blockWidth
                    + " blockHeight: " + blockHeight + " cacheBlockAmount "
                    + cacheBlockAmount + " changeFileAndCache: " + changeFileAndData);
        }
        if (changeFileAndData != null) {
            this.changeFileAndData = changeFileAndData;
        }
        ReadWrite<T[]> readWrite;
        if (openConcurrentWrite) {
            readWrite = new CacheConcurrentReadWrite<T>(this.changeFileAndData);
        } else {
            readWrite = new ReadWriteTool<T[]>(this.changeFileAndData);
        }
        cacheDoubleArray = new CacheDoubleArray<T[]>(DEFAULT_CACHE_BLOCK_AMOUNT, readWrite, this.changeFileAndData);
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.blockArea = this.blockWidth * this.blockHeight;
        this.blocks = new LRULinkedHashMap<Block>(
                cacheBlockAmount);
    }

    public void add(int locationX, int locationY, T object) {
        if (locationX < 0 || locationX > sizeX || locationY < 0
                || locationY > sizeY) {
            throw new IllegalArgumentException("the size of X is " + sizeX
                    + " the size of Y is " + sizeY
                    + " , can't to add locationX: " + locationX
                    + " , locationY : " + locationY);
        }
        insertTo(locationX, locationY, object);
    }

    public void insertTo(int locationX, int locationY, T object) {
        int blockNum = getNumInBlock(locationX, locationY);
        Block block = getBlock(getBlockXNum(locationX), getBlockYNum(locationY));
        if (block == null) {
            block = createBlock(getBlockXNum(locationX),
                    getBlockYNum(locationY));
        }
        block.getArr()[blockNum] = object;
        block.change();
        sizeX = locationX >= sizeX ? locationX + 1 : sizeX;
        sizeY = locationY >= sizeY ? locationY + 1 : sizeY;
    }

    private Block createBlock(int blockXNum, int blockYNum) {
        Object[] arr = new Object[blockArea];
        Block block = new Block(arr, true);
        blocks.put(integerXYKey.createKey(blockXNum, blockYNum), block);
        return block;
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

    private Block getBlock(int blockXNum, int blockYNum) {
        final Integer blockNum = integerXYKey.createKey(blockXNum, blockYNum);
        final Block block = blocks.get(blockNum);
        if (block != null) {
            return block;
        }
        final Object[] arr = cacheDoubleArray.get(blockXNum, blockYNum);
//            final Object[] arr = readWriteArray.read(blockXNum, blockYNum);
        if (arr != null) {
            final Block newBlock = new Block(arr, false);
            blocks.put(blockNum, newBlock);
            return newBlock;
        } else {
            return null;
        }
    }

    public void saveBlock(int blockXNum, int blockYNum, Block block) {
        if (block.needWrite) {
//            Log.w("",
//                    "cache Double array list save block "
//                            + "X: "
//                            + blockXNum + "  Y: " + blockYNum);
            CacheDoubleArray.Element element = new CacheDoubleArray.Element(block.getArr(), block.needWrite);
            cacheDoubleArray.save(blockXNum, blockYNum, element);
        }

    }

    public T get(int locationX, int locationY) {
        if (locationX < 0 || locationY < 0) {
            throw new IllegalArgumentException("this size of X is " + sizeX
                    + "this size of Y is " + sizeY
                    + ", can't to get locationX: " + locationX
                    + " , locationY : " + locationY);
        }
        if (locationX >= sizeX || locationY >= sizeY) {
            return null;
        }
        Block block = getBlock(getBlockXNum(locationX), getBlockYNum(locationY));
        final int blockNum = getNumInBlock(locationX, locationY);
        if (block != null && block.getArr() != null) {
            Object object = block.getArr()[blockNum];
            if (object != null) {
                return (T) object;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return sizeX == 0 && sizeY == 0;
    }

//    public void setDirPath(String path) {
//        readWriteArray.setDirPath(path);
//    }


    public Iterator<T> iteratorXLine(int x) {
        if (x < sizeX) {
            return new IteratorX(x);
        } else {
            throw new IllegalArgumentException("this size of X is " + sizeX
                    + "this size of Y is " + sizeY
                    + ", can't to iterator x Line: " + x);
        }
    }

    public Iterator<T> iteratorXLine(int x, int startLocationY, int length) {
        if (x < sizeX && startLocationY >= 0
                && startLocationY + length <= sizeY()) {
            return new IteratorX(x, startLocationY, length);
        } else {
            throw new IllegalArgumentException("this size of X is " + sizeX
                    + "this size of Y is " + sizeY
                    + ", can't to iterator x Line: " + x + " when startY: "
                    + startLocationY + "  length :" + length);
        }
    }

    public Iterator<T> iteratorYLine(int y) {
        if (y < sizeY) {
            return new IteratorY(y);
        } else {
            throw new IllegalArgumentException("this size of X is " + sizeX
                    + "this size of Y is " + sizeY
                    + ", can't to iterator Y Line: " + y);
        }
    }

    public Iterator<T> iteratorYLine(int y, int startLocationX, int length) {
        if (y < sizeY && startLocationX >= 0
                && startLocationX + length <= sizeX()) {
            return new IteratorY(y, startLocationX, length);
        } else {
            throw new IllegalArgumentException("this size of X is " + sizeX
                    + "this size of Y is " + sizeY
                    + ", can't to iterator y Line: " + y + " when startX: "
                    + startLocationX + "  length :" + length);
        }
    }

    public int sizeX() {
        return sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    protected ChangeFileAndData<T[]> changeFileAndData = new ChangeFileAndData<T[]>() {
        @Override
        public T[] getData(int xNum, int yNum) {
            return null;
        }

        @Override
        public T[] readDataFromFile(int xNum, int yNum, File file) {
            FileInputStream fis = null;
            T[] arr = null;
            try {
                fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = null;
                ois = new ObjectInputStream(bis);
                arr = (T[]) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return arr;
        }

        @Override
        public void writeDataInFile(int xNum, int yNum, Object object, File file) {
            Object[] arr = (Object[]) object;
            try {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(arr);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    class IteratorX implements Iterator<T> {
        private final int locationX;
        private int startLocationY, endLocationY;

        public IteratorX(int locationX) {
            this.locationX = locationX;
            startLocationY = 0;
            endLocationY = sizeY();
        }

        public IteratorX(int locationX, int startLocationY, int length) {
            this.locationX = locationX;
            this.startLocationY = startLocationY;
            this.endLocationY = startLocationY + length;
        }

        @Override
        public boolean hasNext() {
            if (startLocationY < endLocationY) {
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            if (startLocationY < endLocationY) {
                return get(locationX, startLocationY++);
            } else {
                throw new IllegalArgumentException("has not Element");
            }
        }

        @Override
        public void remove() {
            if (startLocationY < endLocationY) {
                add(locationX, startLocationY, null);
            } else {
                throw new IllegalArgumentException("has not Element");
            }
        }

    }

    class IteratorY implements Iterator<T> {
        private final int locationY;
        private int startLocationX, endLocationX;

        public IteratorY(int locationY) {
            this.locationY = locationY;
            this.startLocationX = 0;
            this.endLocationX = sizeX();
        }

        public IteratorY(int locationY, int startLocationX, int length) {
            this.locationY = locationY;
            this.startLocationX = startLocationX;
            this.endLocationX = startLocationX + length;
        }

        @Override
        public boolean hasNext() {
            if (startLocationX < endLocationX) {
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            if (startLocationX < endLocationX) {
                return get(startLocationX++, locationY);
            } else {
                throw new IllegalArgumentException("has not Element");
            }
        }

        @Override
        public void remove() {
            if (startLocationX < endLocationX) {
                add(startLocationX, locationY, null);
            } else {
                throw new IllegalArgumentException("has not Element");
            }
        }

    }

    public static class Block {
        private Object[] arr;
        private boolean needWrite;

        public Block(Object[] arr, boolean needWrite) {
            this.arr = arr;
            this.needWrite = needWrite;
        }

        public boolean needWrite() {
            return needWrite;
        }

        public void change() {
            this.needWrite = true;
        }

        public Object[] getArr() {
            return arr;
        }

        public void setArr(Object[] arr) {
            this.arr = arr;
        }

    }

    class LRULinkedHashMap<V> extends LinkedHashMap<Integer, V> {
        private int maxAcount;
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;

        public LRULinkedHashMap(int maxAcount) {
            super(DEFAULT_CACHE_BLOCK_AMOUNT, DEFAULT_LOAD_FACTOR, true);
            this.maxAcount = maxAcount;
        }

        @Override
        protected boolean removeEldestEntry(Entry<Integer, V> eldest) {
            if (size() > maxAcount) {
                Integer blockNum = eldest.getKey();
                saveBlock(integerXYKey.getX(blockNum), integerXYKey.getY(blockNum),
                        (Block) eldest.getValue());
                return true;
            }
            return false;
        }
    }


}
