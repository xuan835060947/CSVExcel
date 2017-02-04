package com.wps.csvexcel.util;

/**
 * Created by kingsoft on 2015/8/14.
 */
public class CalculateScrollLength {
    private long startTime;
    private int lastLengthX, lastLengthY;
    private int acceleratedVelocityX, acceleratedVelocityY;
    private int startVelocityX, startVelocityY;
    private static final int DEFAULT_ACCELERATED_VELOCITY_Y = 10;
    private static final int DEFAULT_ACCELERATED_VELOCITY_X = 10;
    private static final float MOVE_RASTE_Y = 0.001f;
    private static final float MOVE_RATE_X = 0.001f;


    public int getCurrentMoveY() {
        int time = getTimeGap();
        int length = (int) (startVelocityY * time + 0.5 * acceleratedVelocityY * time * time);
        int move = length - lastLengthY;
//        Log.v("", " getCurrentMoveY:   " + move + "  time : " + time + "  last Length : " + lastLengthY + "  length : " + length);
        lastLengthY = length;
        return (int) (move * MOVE_RASTE_Y);
    }

    public int getCurrentMoveX() {
        int time = getTimeGap();
        int length = (int) (startVelocityX * time + 0.5 * acceleratedVelocityX * time * time);
        int move = length - lastLengthX;
        int resultMove = (int) (move * MOVE_RATE_X);
        lastLengthX = length;
        return resultMove;
    }

    public boolean isScrollY() {
        int time = getTimeGap();
        if (Math.abs(startVelocityY) > Math.abs(acceleratedVelocityY * time)) {
            return true;
        }
        return false;
    }

    public boolean isScrollX() {
        int time = getTimeGap();
        if (Math.abs(startVelocityX) > Math.abs(acceleratedVelocityX * time)) {
            return true;
        }
        return false;
    }

    private int getTimeGap() {
        int millisTime = (int) (System.currentTimeMillis() - startTime);
        int lessenTime = millisTime >> 0;
        return lessenTime;
    }

    public void startScroll(int startVelocityX, int startVelocityY) {
        if (startVelocityX < 0) {
            this.acceleratedVelocityX = DEFAULT_ACCELERATED_VELOCITY_X;
        } else {
            this.acceleratedVelocityX = -DEFAULT_ACCELERATED_VELOCITY_X;
        }
        if (startVelocityY < 0) {
            this.acceleratedVelocityY = DEFAULT_ACCELERATED_VELOCITY_Y;
        } else {
            this.acceleratedVelocityY = -DEFAULT_ACCELERATED_VELOCITY_Y;
        }
        startTime = System.currentTimeMillis();
        this.startVelocityX = startVelocityX;
        this.startVelocityY = startVelocityY;
        int time = getTimeGap();
        lastLengthX = (int) (startVelocityX * time + 0.5 * acceleratedVelocityX * time * time);
        lastLengthY = (int) (startVelocityY * time + 0.5 * acceleratedVelocityY * time * time);
    }
}
