package com.wps.csvexcel.bean;

import java.io.Serializable;

import com.wps.csvexcel.bean.cell.DataFormat;

public class Cell implements Serializable {
    private String content;
    private Object tag;

    public Cell() {
    }

    public Cell(String content) {
        if (content != null && content.length() == 0) {
            this.content = null;
        } else {
            this.content = content;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Cell) {
            Cell oCell = (Cell) o;
            String thisContent = getContent();
            String oContent = oCell.getContent();
            if (thisContent == null || thisContent.length() == 0) {
                if (oContent == null || oContent.length() == 0) {
                    return true;
                }
            } else {
                if (oContent == null) {
                    return false;
                }
                if (thisContent.length() == oContent.length()) {
                    if (thisContent.equals(oContent)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (getContent() != null && getContent().length() != 0)
            return getContent().hashCode();
        return 0;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
