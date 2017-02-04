package com.wps.csvexcel.util;

import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.view.CellView;

/**
 * Created by kingsoft on 2015/8/27.
 */
public class DrawCellViewUtil {
    public static Sheet sheet;
    public static CellView cellView = new CellView();


    public static void setSheet(Sheet sheet){
        DrawCellViewUtil.sheet = sheet;
    }

    public static CellView getCellView(int xNum,int yNum){
        if(sheet==null){
            throw new IllegalStateException("sheet was null , should use the method: setSheet");
        }
        cellView.setCell(sheet.getCell(xNum,yNum));
        return cellView;
    }
}
