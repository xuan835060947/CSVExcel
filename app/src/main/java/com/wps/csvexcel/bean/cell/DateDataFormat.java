package com.wps.csvexcel.bean.cell;

/** 
 *
 * @author w_chenxiaoxuan
 * 
 */
public class DateDataFormat extends DataFormat{
	
	public DateDataFormat(String data) {
		super(data);
	}

	@Override
	public boolean isMatch() {
		return false;
	}

	@Override
	protected String format(String data) {
		return null;
	}
	
}
