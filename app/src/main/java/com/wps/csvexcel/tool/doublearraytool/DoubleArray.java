package com.wps.csvexcel.tool.doublearraytool;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by kingsoft on 2015/8/24.
 */
public class DoubleArray<T> {
        private int sizeX = 0, sizeY = 0;
        private static final int CAPACITY_X = 20, CAPACITY_Y = 20;
        private int capacityX = CAPACITY_X, capacityY = CAPACITY_Y;
        private Object arrays[][];


        public DoubleArray() {
            arrays = new Object[capacityX][capacityY];
        }


        public DoubleArray(int capacityX, int capacityY) {
            this.capacityX = capacityX;
            this.capacityY = capacityY;
            arrays = new Object[capacityX][capacityY];
        }


        public void add(final int locationX, final int locationY, final T object) {
            if (locationX < 0 || locationX > sizeX || locationY < 0
                    || locationY > sizeY) {
                throw new IllegalArgumentException("this size of X is " + sizeX
                        + "this size of Y is " + sizeY
                        + ", can't to add locationX: " + locationX
                        + " , locationY : " + locationY);
            }
            insertTo(locationX, locationY, object);


        }


        public void insertTo(int locationX, int locationY, T object){
            expandArrayTo(locationX + 1, locationY + 1);
            sizeX = locationX >= sizeX ? locationX + 1 : sizeX;
            sizeY = locationY >= sizeY ? locationY + 1 : sizeY;
            arrays[locationX][locationY] = object;
        }

        private void expandArrayTo(final int wantCapacityX, final int wantCapacityY) {
            if (wantCapacityX <= capacityX && wantCapacityY <= capacityY) {
                return;
            }


            int newCapacityX = capacityX > 0 ? capacityX : CAPACITY_X;
            while (wantCapacityX > newCapacityX) {
                newCapacityX *= 2;
            }
            int newCapacityY = capacityY > 0 ? capacityY : CAPACITY_Y;
            while (wantCapacityY > newCapacityY) {
                newCapacityY *= 2;
            }
            final Object oldArr[][] = arrays;
            final Object newArr[][] = new Object[newCapacityX][newCapacityY];
            for (int i = 0; i < sizeX; i++) {
                System.arraycopy(oldArr[i], 0, newArr[i], 0, sizeY);
            }
            arrays = newArr;
            capacityX = newCapacityX;
            capacityY = newCapacityY;
        }




        public void clear() {
            for (int i = 0; i < sizeX; i++) {
                Arrays.fill(arrays[i], null);
            }
            sizeX = 0;
            sizeY = 0;
        }


        public boolean contains(Object object) {
            if (object != null) {
                for (int i = 0; i < sizeX; i++) {
                    for (int j = 0; j < sizeY; j++) {
                        if (object.equals(arrays[i][j])) {
                            return true;
                        }
                    }
                }
            } else {
                for (int i = 0; i < sizeX; i++) {
                    for (int j = 0; j < sizeY; j++) {
                        if (arrays[i][j] == null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }


        // public boolean containsAll(Collection<?> collection) {
        // return false;
        // }


        public T get(int locationX, int locationY) {
            if (locationX >= sizeX || locationY >= sizeY) {
//                throw new IllegalArgumentException("the size of X is " + sizeX
//                        + "the size of Y is " + sizeY
//                        + ", can't to get locationX: " + locationX
//                        + " , locationY : " + locationY);
                return null;
            }
            return (T) arrays[locationX][locationY];
        }


        // public int indexOf(Object object) {
        // return 0;
        // }


        public boolean isEmpty() {
            if (sizeX == 0 && sizeY == 0) {
                return true;
            }
            return false;
        }


        public Iterator<T> iteratorXLine(int x) {
            if (x < sizeX) {
                return new IteratorX(x);
            } else {
                return null;
            }
        }


        public Iterator<T> iteratorXLine(int x, int startLocationY, int length) {
            if (x < sizeX &&startLocationY >=0 && startLocationY + length <= sizeY()) {
                return new IteratorX(x, startLocationY, length);
            } else {
                return null;
            }
        }


        public Iterator<T> iteratorYLine(int y) {
            if (y < sizeY) {
                return new IteratorY(y);
            } else {
                return null;
            }
        }


        public Iterator<T> iteratorYLine(int y, int startLocationX, int length) {
            if (y < sizeY &&startLocationX >=0 && startLocationX + length <= sizeX()) {
                return new IteratorY(y, startLocationX, length);
            } else {
                return null;
            }
        }


        public T set(int locationX, int locationY, T object) {
            if (locationX >= 0 && locationX < sizeX && locationY >= 0
                    && locationY < sizeY) {
                Object result = arrays[locationX][locationY];
                arrays[locationX][locationY] = object;
                return (T) result;
            } else {
                throw new IllegalArgumentException("this size of X is " + sizeX
                        + "this size of Y is " + sizeY
                        + ", can't to set locationX: " + locationX
                        + " , locationY : " + locationY);
            }
        }


        public int sizeX() {
            return sizeX;
        }


        public int sizeY() {
            return sizeY;
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
                    return (T) arrays[locationX][startLocationY++];
                } else {
                    throw new IllegalArgumentException("has not Element");
                }
            }


            @Override
            public void remove() {
                if (startLocationY < endLocationY) {
                    arrays[locationX][startLocationY] = null;
                } else {
                    throw new IllegalArgumentException("has not Element");
                }
            }


        }


        class IteratorY implements Iterator<T> {
            private final int locationY;
            private int startLocationX, endLocationX;// ����endLocationY


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
                    return (T) arrays[startLocationX++][locationY];
                } else {
                    throw new IllegalArgumentException("has not Element");
                }
            }


            @Override
            public void remove() {
                if (startLocationX < endLocationX) {
                    arrays[startLocationX][locationY] = null;
                } else {
                    throw new IllegalArgumentException("has not Element");
                }
            }


        }

}
