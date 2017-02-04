package com.wps.csvexcel.bean.cell;

import java.io.Serializable;
import java.util.Comparator;

import com.wps.csvexcel.bean.Cell;

/** 
 *
 * @author w_chenxiaoxuan
 * 
 */
public class CellComparator implements Comparator<Cell> ,Serializable{

	@Override
	public int compare(Cell lhs, Cell rhs) {

		if (lhs == null && rhs == null) {
			return 0;
		}
		if (lhs == null) {
			return 1;
		}
		if (rhs == null) {
			return -1;
		}
		if (lhs.getContent() == null && rhs.getContent() == null) {
			return 0;
		}
		if (lhs.getContent() == null) {
			return 1;
		}
		if (rhs.getContent() == null) {
			return -1;
		}

		if (isPureNumber(lhs)) {
			if (isPureNumber(rhs)) {
				if (lhs.getContent().length() == rhs.getContent()
						.length()) {
					return lhs.getContent().compareTo(rhs.getContent());
				}

				if (lhs.getContent().length() < rhs.getContent()
						.length()) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return -1;
			}
		} else {
			if (isPureNumber(rhs)) {
				return 1;
			}
			return lhs.getContent().compareTo(rhs.getContent());
		}
	
	}
	
	public boolean isPureNumber(Cell s) {
		char[] chars = s.getContent().toCharArray();
		for(char c : chars){
			if(c<'0'||c>'9'){
				return false;
			}
		}
		return true;
	}
//	public boolean isPureNumber(Cell s) {
//		return s.getContent().matches("\\d+");
//	}

}
