package com.wps.csvexcel.util;

/**
 * Created by kingsoft on 2015/8/22.
 */
public class SplitUtil {
    /**
     * Created by kingsoft on 2015/8/21.
     */
    public static StringArrLength splitString(String s, int startNum, int endNum) {
        return splitString(s, startNum, endNum, true);
    }

    public static StringArrLength splitString(String s, int startNum, int endNum, boolean needTotalAmount) {
        if (s == null || s.length() == 0) {
            return new SplitUtil.StringArrLength(new String[0], 0, 0);
        }
        char[] chars = s.toCharArray();
        return splitString(chars, chars.length, startNum, endNum, needTotalAmount);
    }

    public static StringArrLength splitString(char[] chars, int endPos, int startNum, int endNum) {
        return splitString(chars, endPos, startNum, endNum, true);
    }

//    public static StringArrLength splitString(char[] chars, int endPos, int startNum, int endNum, boolean needTotalAmount) {
//        int amount = endNum - startNum;
//        String[] sArr = new String[amount];
//        int start = 0;
//        int sTotalNum = 0;
//        int arrNum = 0;
//        int i = 0;
//        for (; i < endPos; i++) {
//            if (chars[i] == ',') {
//                if (sTotalNum >= startNum && sTotalNum < endNum) {
//                    sArr[arrNum++] = new String(chars, start, i - start);
//                    if (needTotalAmount == false && arrNum >= amount) {
//                        break;
//                    }
//                }
//                start = i + 1;
//                ++sTotalNum;
//            }
//        }
//        if (i == endPos && start != endPos) {
//            if (arrNum < amount && sTotalNum < endNum)
//                sArr[arrNum++] = new String(chars, start, endPos - start);
//            ++sTotalNum;
//        }
//        return new SplitUtil.StringArrLength(sArr, arrNum, sTotalNum);
//    }
//
//    public static StringArrLength splitStringCompletely(char[] chars, int endPos, int startNum, int endNum) {
//        return splitStringCompletely(chars, endPos, startNum, endNum, true);
//    }

    public static StringArrLength splitString(char[] chars, int endPos, int startNum, int endNum, boolean needTotalAmount) {
        int amount = endNum - startNum;
        String[] sArr = new String[amount];
        int start = 0;
        int sTotalNum = 0;
        int arrNum = 0;
        int i = 0;
        for (; i < endPos; i++) {
            if (chars[i] == ',') {
                if (isCompleteCell(chars, start, i)) {
                    if (sTotalNum >= startNum && sTotalNum < endNum) {
                        if ('"' == chars[start]) {
                            boolean preIsDoubleQuotes = false;
                            StringBuilder sb = new StringBuilder();
                            for (int k = start + 1; k < i; k++) {
                                if ('"' == chars[k]) {
                                    if (preIsDoubleQuotes) {
                                        preIsDoubleQuotes = false;
                                        sb.append('"');
                                    } else {
                                        preIsDoubleQuotes = true;
                                    }
                                } else {
                                    sb.append(chars[k]);
                                }
                            }
                            if (preIsDoubleQuotes) {
                                sArr[arrNum++] = sb.toString();
                            }
                        } else {
                            sArr[arrNum++] = new String(chars, start, i - start);
                        }
                        if (needTotalAmount == false && arrNum >= amount) {
                            break;
                        }
                    }
                    start = i + 1;
                    ++sTotalNum;
                }
            }
        }
        if (i == endPos && start != endPos) {
            if (arrNum < amount && sTotalNum < endNum)
                sArr[arrNum++] = new String(chars, start, endPos - start);
            ++sTotalNum;
        }
        return new SplitUtil.StringArrLength(sArr, arrNum, sTotalNum);
    }


    public static boolean isCompleteCell(char[] chars, int start, int endPos) {
        if ('"' == chars[start]) {
            boolean preIsDoubleQuotes = false;
            for (int k = start + 1; k < endPos; k++) {
                if ('"' == chars[k]) {
                    if (preIsDoubleQuotes) {
                        preIsDoubleQuotes = false;
                    } else {
                        preIsDoubleQuotes = true;
                    }
                }
            }
            if (preIsDoubleQuotes) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static class StringArrLength {
        private String[] arr;
        private int arrElementAmount;
        private int stringTotalAmount;


        public StringArrLength(String[] arr, int arrElementAmount,
                               int stringTotalAmount) {
            this.arr = arr;
            this.arrElementAmount = arrElementAmount;
            this.stringTotalAmount = stringTotalAmount;
        }


        public String[] getArr() {
            return arr;
        }


        public int getArrElementAmount() {
            return arrElementAmount;
        }


        public int getStringTotalAmount() {
            return stringTotalAmount;
        }


    }
}
