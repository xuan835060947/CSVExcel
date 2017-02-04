package com.wps.csvexcel.bean.sheet.iterator;

import java.util.Iterator;
import java.util.List;

import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.cell.CellComparator;
import com.wps.csvexcel.tool.sorttool.HeapTool;

/**
 *
 * @author w_chenxiaoxuan
 *
 */
public class CellHeapSortIterator implements Iterator<Cell> {
	private HeapTool<Cell> heap;

	public CellHeapSortIterator(Iterator<Cell> iterator, int length) {
		Cell[] arr = new Cell[length];
		int amount =0;
		while(iterator.hasNext()){
			arr[amount++] = iterator.next();
		}
		heap = new HeapTool<Cell>(arr, amount,new CellComparator());
	}

	public CellHeapSortIterator(Cell[] arr) {
		heap = new HeapTool<Cell>(arr,arr.length,new CellComparator());
	}

	public CellHeapSortIterator(List<Cell> list) {
		heap = new HeapTool<Cell>(list, list.size(), new CellComparator());
	}

	@Override
	public boolean hasNext() {
		return heap.getSize()>0? true:false;
	}

	@Override
	public Cell next() {
		return heap.popTop();
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException();
	}

}
