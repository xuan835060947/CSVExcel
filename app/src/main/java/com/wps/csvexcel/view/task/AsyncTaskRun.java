package com.wps.csvexcel.view.task;

import android.app.Activity;
import android.util.Log;

/**
 * Created by kingsoft on 2015/8/6.
 */

public abstract class AsyncTaskRun implements Runnable{
    private String tag = this.toString();
    protected Activity activity;
    private boolean isWork;

    public AsyncTaskRun(Activity activity) {
        this.activity = activity;
    }

    protected abstract void doInBackground();

    protected abstract void onProgressUpdate(int values);

    protected abstract void finishToDoInUIThread();

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        doInBackground();
        long endTime = System.currentTimeMillis();
        Log.e(tag,""+tag+" doInBackground运行时间为: "+(endTime - startTime) +" ms ");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finishToDoInUIThread();
            }
        });
        isWork = false;
    }

    public boolean isWork(){
        return isWork;
    }

    public void updateProgress(final int value){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onProgressUpdate(value);
            }
        });
    }

    public void execute(){
        isWork = true;
        new Thread(this).start();
    }

}
