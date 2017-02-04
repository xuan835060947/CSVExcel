package com.wps.csvexcel.tool.doublearraytool;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by kingsoft on 2015/8/10.
 */
public class CacheList<T> implements List<T> {
    private static final int BLOCK_HEIGHT = 1;
    private static final int DEFAULT_BLOCK_WIDTH = 600;
    private static final int DEFAULT_CACHE_BLOCK_AMOUNT = 4;
    private static final int LOCATION_Y =0;
    private CacheBlockDoubleArray<T> cacheBlockDoubleArray;

    public CacheList() {
        this(DEFAULT_BLOCK_WIDTH,DEFAULT_CACHE_BLOCK_AMOUNT);
    }

    public CacheList(int blockWidht, int cacheBlockNum) {
        cacheBlockDoubleArray = new CacheBlockDoubleArray<T>(blockWidht, BLOCK_HEIGHT,cacheBlockNum,null,false);
    }

    @Override
    public void add(final int i, T t) {
        if(i>size() || i<0){
            throw new IllegalArgumentException("i: " + i);
        }
        if(i==size()){
            add(t);
        }else{
            for(int num = size();num!=i;num--){
                cacheBlockDoubleArray.insertTo(num, LOCATION_Y, cacheBlockDoubleArray.get(num-1, LOCATION_Y));
            }
            set(i,t);
        }
    }

    @Override
    public boolean add(T t) {
        int end = cacheBlockDoubleArray.sizeX();
        cacheBlockDoubleArray.add(end, LOCATION_Y,t);
        return true;
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        int collectionlength = collection.size();
        int insertEnd = i+collectionlength;
        if(i>size() || i<0){
            throw new IllegalArgumentException("i: " + i);
        }
        if(i==size()){
            Iterator<? extends T> it = collection.iterator();
            int num =i;
            while(it.hasNext()){
                set(num++,it.next());
            }
        }else{
            for(int num = size()+collectionlength-1;num>=insertEnd;num--){
                cacheBlockDoubleArray.insertTo(num, LOCATION_Y, cacheBlockDoubleArray.get(num-collectionlength, LOCATION_Y));
            }
            Iterator<? extends T> it = collection.iterator();
            int num =i;
            while(it.hasNext()){
                set(num++,it.next());
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return addAll(0,collection);
    }

    @Override
    public void clear() {
        for(int i= cacheBlockDoubleArray.sizeX();i>=0;i--){
            cacheBlockDoubleArray.add(i, LOCATION_Y,null);
        }
    }

    @Override
    public boolean contains(Object o) {
        for(int i= cacheBlockDoubleArray.sizeX()-1;i>=0;i--){
            if(cacheBlockDoubleArray.get(i, LOCATION_Y).equals(o)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for(Object t:collection){
            if(!contains(t)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        }
        if(o instanceof List){
            List oList = (List)o;
            if(oList.size()== cacheBlockDoubleArray.sizeX()){
                for(int i=0;i<oList.size();i++){
                    if(!cacheBlockDoubleArray.get(i, LOCATION_Y).equals(oList.get(i))){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public T get(int i) {
        return cacheBlockDoubleArray.get(i, LOCATION_Y);
    }

    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public int indexOf(Object o) {
        for(int i=0;i<size();i++){
            if(cacheBlockDoubleArray.get(i, LOCATION_Y).equals(o)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        if(size()==0){
            return true;
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int lastIndexOf(Object o) {
        for(int i=size();i>=0;i--){
            if(cacheBlockDoubleArray.get(i, LOCATION_Y).equals(o)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return null;
    }

    @Override
      public T remove(int i) {
        T t = cacheBlockDoubleArray.get(i, LOCATION_Y);
        cacheBlockDoubleArray.add(i, LOCATION_Y,null);
        return t;
    }

    @Override
    public boolean remove(Object o) {
        int i = indexOf(o);
        remove(i);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public T set(int i, T t) {
        if(i>size() || i<0){
            throw new IllegalArgumentException("i: " + i);
        }
        T pre = get(i);
        cacheBlockDoubleArray.insertTo(i, LOCATION_Y,t);
        return pre;
    }

    @Override
    public int size() {
        return cacheBlockDoubleArray.sizeX();
    }

    @Override
    public List<T> subList(int i, int i1) {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return null;
    }
}
