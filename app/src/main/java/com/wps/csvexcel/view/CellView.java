package com.wps.csvexcel.view;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;

import com.wps.csvexcel.bean.Cell;

public class CellView implements Serializable{
	private int width = 200, height = 70;
	private int left, top;
	private Cell cell;

	private static final Paint CLICK_ACTION_FRAME_PAINT = new Paint();
	private static final Paint NORMAL_FONT_PAINT = new Paint();
	private static final Paint NORMAL_FRAME_PAINT = new Paint();
	private static final Paint NORMAL_BACKGROUND_PAINT = new Paint();

	private static final int LEFT_PADDING = 5;

	{
		CLICK_ACTION_FRAME_PAINT.setARGB(255, 27, 140, 55);
		CLICK_ACTION_FRAME_PAINT.setStrokeWidth(4);
		CLICK_ACTION_FRAME_PAINT.setStyle(Paint.Style.STROKE);

		NORMAL_FONT_PAINT.setColor(Color.BLACK);
//		NORMAL_FONT_PAINT.setTextAlign(Align.CENTER);
		NORMAL_FONT_PAINT.setTextSize((float) (0.7 * height));
		NORMAL_FRAME_PAINT.setColor(Color.LTGRAY);
		NORMAL_FRAME_PAINT.setStyle(Paint.Style.STROKE);
		NORMAL_BACKGROUND_PAINT.setColor(Color.WHITE);
		NORMAL_BACKGROUND_PAINT.setStyle(Paint.Style.FILL);

	}

	public void draw(Canvas canvas) {
		Paint fontPaint, framePaint;
		fontPaint = NORMAL_FONT_PAINT;
		framePaint = NORMAL_FRAME_PAINT;
		canvas.drawRect(left, top, left + width, top + height, NORMAL_BACKGROUND_PAINT);

		if (getCell() != null && getCell().getContent() != null) {
			FontMetrics fontMetrics = fontPaint.getFontMetrics();
			float textBaseY = top
					+ (height - fontMetrics.bottom + fontMetrics.top) / 2
					- fontMetrics.top;
			canvas.drawText(getCell().getContent(), left+LEFT_PADDING , textBaseY,
					fontPaint);
//			canvas.drawText(getCell().getContent(), left + width / 2, textBaseY,
//					fontPaint);
		}
				canvas.drawRect(left, top, left + width, top + height, framePaint);

	}

	public static void draw(Cell cell, Canvas canvas, int left, int top,
			int width, int height,Paint fontPaint,
			Paint framePaint) {
		FontMetrics fontMetrics = fontPaint.getFontMetrics();
		float textBaseY = top + (height - fontMetrics.bottom + fontMetrics.top)
				/ 2 - fontMetrics.top;
		if (cell != null && cell.getContent() != null)
			canvas.drawText(cell.getContent(), left + width / 2, textBaseY,
					fontPaint);
		canvas.drawRect(left, top, left + width, top + height, framePaint);

	}

	public static void drawBackground(Canvas canvas, int left, int top,
			int width, int height, Paint paint) {
		canvas.drawRect(left, top, left + width, top + height, paint);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}
	
}
