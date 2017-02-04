package com.wps.csvexcel.bean.cell;

/**
 * 
 * @author w_chenxiaoxuan
 * 
 */
public class NormalDataFormat extends DataFormat {
	

	public NormalDataFormat(String data) {
		super(data);
		setFormat(Format.NORMAL);
	}

	@Override
	public boolean isMatch() {
		return true;
	}

	@Override
	protected String format(String data) {
		return data;
	}

	

}
