package com.wps.csvexcel.tool.sorttool;

import java.util.Comparator;
import java.util.List;

/**
 * @author Administrator
 */

public class HeapTool<T> {

    private T[] heapArr;
    private List<T> heapList;
    private Comparator<? super T> comp;
    private int size;
    private boolean useList;

    public HeapTool(T[] arr, int length, Comparator<? super T> comp) {
        this.heapArr = arr;
        this.comp = comp;
        size = arr.length<length?arr.length:length;
        buildHeap();
    }

    public HeapTool(List<T> list, int length, Comparator<? super T> comp) {
        useList = true;
        this.heapList = list;
        this.comp = comp;
        size = list.size()<length?list.size():length;
        buildHeap();
    }

    private T get(int i){
        if(useList){
            return heapList.get(i);
        }else{
            return heapArr[i];
        }
    }

    private void set(int i,T t){
        if(useList){
            heapList.set(i, t);
        }else{
            heapArr[i] = t;
        }
    }

    private int parent(int i) {
        return (i - 1) >> 1;
    }

    private int left(int i) {
        return ((i + 1) << 1) - 1;
    }

    private int right(int i) {
        return (i + 1) << 1;
    }

    private void heapify(int i) {
        heapify(i, size);
    }

    private void heapify(int i, int size) {
        int l = left(i);
        int r = right(i);
        int next = i;
        if (l < size && comp.compare(get(l), get(i)) < 0)
            next = l;
        if (r < size && comp.compare(get(r), get(next)) < 0)
            next = r;
        if (i == next)
            return;
        swap(i, next);
        heapify(next, size);
    }

    private void swap(int i, int j) {
        T tmp = get(i);
        set(i,get(j));
        set(j,tmp);
    }

    public void buildHeap() {
        for (int i = (size) / 2 - 1; i >= 0; i--) {
            heapify(i);
        }
    }


    public T popTop() {
        if (size == 0) {
            throw new IllegalArgumentException(
                    "can not extract max element in empty ");
        }
        T top = get(0);
        set(0,get(size-1));
        set(size-1,null);
        heapify(0, --size);
        return top;
    }

    public int getSize() {
        return size;
    }

}
