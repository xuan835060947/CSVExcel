package com.wps.csvexcel.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by kingsoft on 2015/8/6.
 */
public class ProgressDialogUtil {

    private ProgressDialog progressDialog;
    public ProgressDialogUtil(Context context, String content) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(content);
        progressDialog.setCancelable(false);
    }

    public void show() {
        progressDialog.show();
    }

    public void cancle(){
        progressDialog.cancel();
    }

    public void setProgress(int progress){
        progressDialog.setProgress(progress);
    }

    public void setMessage(String msg) {
        progressDialog.setMessage(msg);
    }
}
