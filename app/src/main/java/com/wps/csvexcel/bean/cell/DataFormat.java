package com.wps.csvexcel.bean.cell;

/**
 * 
 * @author w_chenxiaoxuan
 * 
 */
public abstract class DataFormat {
	protected String originData;
	protected String formatData;
	protected Format format;

	public void setFormat(Format format) {
		this.format = format;
	}

	public Format getFormat() {
		return format;
	}

	public DataFormat(String data) {
		this.originData = data;
	}

	public abstract boolean isMatch();

	protected abstract String format(String data);

	public String getFormatData() {
		if (isMatch()) {
			if (formatData != null) {
				return formatData;
			} else {
				return format(originData);
			}
		} else {
			return getOriginData();
		}
	}

	public String getOriginData() {
		return originData;
	}

	protected void setFormatData(String data) {
		this.originData = data;
	}

	public enum Format {
		NORMAL, NUMERIAL, MONEY, DATE, TIME, PERSENT, FRACTION, TEXT
	}

}
