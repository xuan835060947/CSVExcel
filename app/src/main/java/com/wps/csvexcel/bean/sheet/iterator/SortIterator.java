package com.wps.csvexcel.bean.sheet.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.cell.CellComparator;

/**
 * 
 * @author w_chenxiaoxuan
 * 
 */
public class SortIterator implements Iterator<Cell> {
	Iterator<Cell> sortIt;
	private List<Cell> values;

	public SortIterator(Iterator<Cell> it, int capacity, boolean toBig) {
		values = new ArrayList<Cell>(capacity);
		while (it.hasNext()) {
			values.add(it.next());
		}
		Collections.sort(values, new CellComparator());
		sortIt = values.iterator();
	}

	@Override
	public boolean hasNext() {
		return sortIt.hasNext();
	}

	@Override
	public Cell next() {
		return sortIt.next();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
