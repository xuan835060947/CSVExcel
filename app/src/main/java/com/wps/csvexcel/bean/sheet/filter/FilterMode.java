package com.wps.csvexcel.bean.sheet.filter;


import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.bean.XYNum;

/**
 * ɸѡģʽ�ӿ�
 * 
 * @author w_chenxiaoxuan
 * 
 */

public interface FilterMode {
	public void setSheet(Sheet sheet);

	public void setStartYNum(int startYNum);

	public void setEndYNum(int endYNum);

	// return true if rc should be shown,if not return false
	public boolean isOK(XYNum num);

	public XYNum chooseRow(int XNum, int YNum);

//	static class RowList {
//		private int xNum;
//		private int yNum;
////		private Cell cell;
//
//		public RowList(int xNum, int yNum) {
//			this.xNum = xNum;
//			this.yNum = yNum;
//		}
//
//		public int getXNum() {
//			return xNum;
//		}
//
//		public void setXNum(int xNum) {
//			this.xNum = xNum;
//		}
//
//		public int getYNum() {
//			return yNum;
//		}
//
//		public void setYNum(int yNum) {
//			this.yNum = yNum;
//		}
//
////		public Cell getCell() {
////			return cell;
////		}
////		public void setCell(Cell cell) {
////			this.cell = cell;
////		}
//	}

}
