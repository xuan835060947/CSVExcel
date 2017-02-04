package com.wps.csvexcel.util.ioutil;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Random;

/**
 * Created by kingsoft on 2015/8/27.
 */
public class BufferedReaderWrap extends BufferedReader {
    private static final int HAD_NO_READ_LINE = -1001;
    public static final int EXCEED_BRESULT = -1002;
    public static final int NO_ANY_LINE = -1003;

    private int bufferLength = 2048;
    private char[] readerBuffer = new char[bufferLength];
    private int pos;
    private int end;
    private boolean lastWasCR;
    private char[] result;
    private int resultPos;


    public BufferedReaderWrap(Reader in) {
        super(in);
    }

    public int readCharLine(char[] buffer) throws IOException {
        result = buffer;
        resultPos = 0;
        while (true) {
            if (pos == end) {
                pos = 0;
                end = read(readerBuffer);
                if (end < 0) {
                    return NO_ANY_LINE;
                }
            }
            checkCR();
            int endPos = readReaderBufferLine();
            switch (endPos) {
                case HAD_NO_READ_LINE:
                    break;
                case EXCEED_BRESULT:
                    processExceed();
                    return EXCEED_BRESULT;
                default:
                    return endPos;
            }
        }
    }

    private int readReaderBufferLine() {
        for (int i = pos; i < end; ++i) {
            char ch = readerBuffer[i];
            if (ch == '\n' || ch == '\r') {
                int length = i - pos;
                if (resultPos + length > result.length) {

                    return EXCEED_BRESULT;
                }
                System.arraycopy(readerBuffer, pos, result, resultPos, length);
                resultPos += length;
                pos = i + 1;
                lastWasCR = (ch == '\r');
                return resultPos;
            }
        }
        int length = end - pos;
        if (resultPos + length > result.length) {
            return EXCEED_BRESULT;
        }
        System.arraycopy(readerBuffer, pos, result, resultPos, length);
        resultPos += length;
        pos = end;
        return HAD_NO_READ_LINE;
    }

    private void processExceed() {
        if (pos >= resultPos) {
            pos -= resultPos;
        } else {
            char[] oldBuffer = readerBuffer;
            char[] newBuffer = readerBuffer;
            if (bufferLength < (resultPos + end - pos)) {
                while (bufferLength < (resultPos + end - pos)) {//足够容量
                    bufferLength *= 2;
                }
                newBuffer = new char[bufferLength];
            }
            System.arraycopy(oldBuffer,pos,newBuffer,resultPos,end-pos);
            System.arraycopy(result,0,newBuffer,0,resultPos);
            pos = 0;
            end = resultPos + end - pos;
            readerBuffer = newBuffer;
        }
        Log.e("", " length : " + bufferLength);
    }

    void checkCR() throws IOException {
        if (lastWasCR) {
            if (pos != end) {
                if (readerBuffer[pos] == '\n') {
                    ++pos;
                }
            }
            lastWasCR = false;
        }
    }

    public String readLine() throws IOException {
        throw new IllegalStateException("the method can not be used!! ");
    }
}
