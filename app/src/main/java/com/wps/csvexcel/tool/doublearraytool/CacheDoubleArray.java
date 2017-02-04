package com.wps.csvexcel.tool.doublearraytool;


import com.wps.csvexcel.tool.doublearraytool.readwrite.ChangeFileAndData;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ConcurrentReadWriteTool;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ReadWrite;
import com.wps.csvexcel.tool.doublearraytool.readwrite.ReadWriteTool;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author w_chenxiaoxuan
 */
public class CacheDoubleArray<T> implements ChangeFileAndData<T> {
    private int sizeX, sizeY;
    private static final int DEFAULT_CACHE_AMOUNT = 4;
    private LinkedHashMap<Integer, Element> elements;
    private ReadWrite<T> normalReadWrite;
    private ReadWrite<T> concurrentReadWrite;
    private ChangeFileAndData<T> changeFileAndData;
    private IntegerXYKey integerXYKey = new IntegerXYKey();

    public CacheDoubleArray(ChangeFileAndData changeFileAndData) {
        this(Integer.MAX_VALUE,new ReadWriteTool<T>(changeFileAndData), changeFileAndData);
    }

    public CacheDoubleArray(int cacheAmount, ChangeFileAndData changeFileAndData) {
        this(cacheAmount, new ReadWriteTool<T>(changeFileAndData), changeFileAndData);
    }

    public CacheDoubleArray(
            int cacheAmount, ReadWrite<T> normalReadWrite, ChangeFileAndData<T> changeFileAndData) {
        this.normalReadWrite = normalReadWrite;
        this.elements = new LRULinkedHashMap<Element>(
                cacheAmount);
        this.changeFileAndData = changeFileAndData;
        concurrentReadWrite = new ConcurrentReadWriteTool<T>(changeFileAndData);
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
        Element element = getElement(locationX, locationY);
        element.setObject(object);
        element.change();
        sizeX = locationX >= sizeX ? locationX + 1 : sizeX;
        sizeY = locationY >= sizeY ? locationY + 1 : sizeY;
    }

    private Element getElement(int elementXNum, int elementYNum) {
        final Integer elementNum = integerXYKey.createKey(elementXNum, elementYNum);
        final Element element = elements.get(elementNum);
        if (element != null) {
            return element;
        }
        try {
            final Object object = normalReadWrite.read(elementXNum, elementYNum);
            if (object == null) {
                final Element newElement = new Element(normalReadWrite.getObject(elementXNum,elementYNum), true);
                elements.put(elementNum, newElement);
                return newElement;
            } else {
                final Element newElement = new Element(object, false);
                elements.put(elementNum, newElement);
                return newElement;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(int elementXNum, int elementYNum, Element element) {
        try {
            if (element.needWrite) {
                sizeX = elementXNum >= sizeX ? elementXNum + 1 : sizeX;
                sizeY = elementYNum >= sizeY ? elementYNum + 1 : sizeY;
                normalReadWrite.write(elementXNum, elementYNum, element.getObject());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void concurrentSave(int elementXNum, int elementYNum, Element element) {
        try {
            if (element.needWrite) {
                sizeX = elementXNum >= sizeX ? elementXNum + 1 : sizeX;
                sizeY = elementYNum >= sizeY ? elementYNum + 1 : sizeY;
                concurrentReadWrite.write(elementXNum, elementYNum, element.getObject());
                Integer key = integerXYKey.createKey(elementXNum,elementYNum);
                if(!elements.containsKey(key)){
                    elements.put(key,element);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        Element element = getElement(locationX, locationY);
        if (element != null && element.getObject() != null) {
            Object object = element.getObject();
            if (object != null) {
                return (T) object;
            }
        }
        return null;
    }

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



    @Override
    public T getData(int xNum, int yNum) {
        return changeFileAndData.getData(xNum, yNum);
    }

    @Override
    public T readDataFromFile(int xNum, int yNum, File file) {
        return changeFileAndData.readDataFromFile(xNum, yNum, file);
    }

    @Override
    public void writeDataInFile(int xNum, int yNum, Object object, File file) {
        changeFileAndData.writeDataInFile(xNum, yNum, object, file);
    }

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

    public static class Element {
        private Object object;
        private boolean needWrite;

        public Element(Object object, boolean needWrite) {
            this.object = object;
            this.needWrite = needWrite;
        }

        public boolean needWrite() {
            return needWrite;
        }

        public void change() {
            this.needWrite = true;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

    }

    class LRULinkedHashMap<V> extends LinkedHashMap<Integer, V> {
        private int maxAcount;
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;

        public LRULinkedHashMap(int maxAcount) {
            super(DEFAULT_CACHE_AMOUNT, DEFAULT_LOAD_FACTOR, true);
            this.maxAcount = maxAcount;
        }

        @Override
        protected boolean removeEldestEntry(Entry<Integer, V> eldest) {
            if (size() > maxAcount) {
                Integer elementNum = eldest.getKey();
                concurrentSave(integerXYKey.getX(elementNum), integerXYKey.getY(elementNum),
                        (Element) eldest.getValue());
//                Log.v(""," cache double list --removeEldestEntry --");
                return true;
            }
            return false;
        }
    }

}
