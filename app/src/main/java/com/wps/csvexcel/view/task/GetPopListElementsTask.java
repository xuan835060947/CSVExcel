package com.wps.csvexcel.view.task;

import android.app.Activity;
import com.wps.csvexcel.bean.Cell;

import java.util.Iterator;

/**
 * Created by kingsoft on 2015/9/9.
 */
public class GetPopListElementsTask extends AsyncTaskRun{
    private Iterator<Cell> iterator;
    private int cacheNum;

    public GetPopListElementsTask(Activity activity,Iterator<Cell> iterator,int cacheNum) {
        super(activity);
        this.iterator = iterator;
        this.cacheNum = cacheNum;
    }



    @Override
    protected void doInBackground() {

    }

    @Override
    protected void onProgressUpdate(int values) {

    }

    @Override
    protected void finishToDoInUIThread() {

    }
}
