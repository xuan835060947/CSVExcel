package com.wps.csvexcel.bean.sheet.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.wps.csvexcel.bean.Cell;

/**
 * 
 * @author w_chenxiaoxuan
 * 
 */
public class CellNoRepeatIterator implements Iterator<Cell> {
	private Iterator<Cell> iterator;
	private Set<Cell> set;

	public CellNoRepeatIterator(final Iterator<Cell> it, int length) {
		set = new HashSet<Cell>(length);
		while (it.hasNext()) {
				set.add(it.next());
		}
		this.iterator = set.iterator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Cell next() {
		return iterator.next();
	}

	@Override
	public void remove() {

	}

	public Iterator<Cell> iterator(){
		return iterator;
	}
	
	public Set<Cell> getValues() {
		return set;
	}
	
	public int size(){
		return set.size();
	}
}

	

