package com.wps.csvexcel.activity;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.wps.csvexcel.R;
import com.wps.csvexcel.bean.Sheet;
import com.wps.csvexcel.share.ShareValue;
import com.wps.csvexcel.util.ProgressDialogUtil;
import com.wps.csvexcel.util.ToastUtil;
import com.wps.csvexcel.view.task.AsyncTaskRun;

import java.io.File;

public class MainActivity extends BaseActivity {
    private ProgressDialogUtil progressDialogUtil;
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void setUI() {
        progressDialogUtil = new ProgressDialogUtil(this, getString(R.string.opening_file));
        mFindViewAndSetOnClick(R.id.btOpenDefault);
        mFindViewAndSetOnClick(R.id.llOpenFile);

    }

    @Override
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.llOpenFile:
                openFile();
                break;
            case R.id.btOpenDefault:
                openDefaultFile();
                break;
        }
    }

    public void openDefaultFile() {
        String path = "/storage/emulated/0/a1/6000行规则数据.csv";
//        String path = "/storage/emulated/0/a1/测试65535行1-65535.csv";
        StartSheetTask startSheetTask = new StartSheetTask(this, path);
        startSheetTask.execute();
    }

    private void openFile() {
        Intent getFile = new Intent(Intent.ACTION_GET_CONTENT);
        getFile.setType("text/*");
        startActivityForResult(getFile, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
//        ContentResolver resolver = getContentResolver();
        Uri originalUri = data.getData();
        final String path = originalUri.getPath();

        StartSheetTask startSheetTask = new StartSheetTask(this, path);
        startSheetTask.execute();
    }


    @Override
    protected void onStart() {
        super.onStart();
        clear(getDirPath());
    }


    public void clear(final String dirPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("MainActivity", "delete all cache ");
                deleteAll(new File(dirPath));
                Log.e("MainActivity", "had delete all cache ");
            }
        }).start();
    }

    private void deleteAll(File path) {
        if (path == null) {
            return;
        }
        if (!path.exists()) // 路径存在
            return;
        if (path.isFile()) { // 是文件
            if (path.delete()) {
//                Log.e(this.toString(), "文件:" + path + "删除失败");
            }
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            deleteAll(files[i]);
        }
        if (path.delete()) {
//            Log.e(this.toString(), "文件:" + path + "删除失败");
        }
    }

    private String getDirPath() {

        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在

        File sdDir = null;
        if (sdCardExist) // 如果SD卡存在，则获取跟目录
        {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        } else {
            throw new IllegalStateException();
        }
        String dirPath = sdDir.toString() + "/kingsoft/wps/temp/";
        return dirPath;
    }

    private Handler handler = new Handler();

    private class StartSheetTask extends AsyncTaskRun {
        private String path;
        private Sheet sheet;
        private int progress = 1;
        private int delayMillis = 100;

        public StartSheetTask(Activity activity, String path) {
            super(activity);
            this.path = path;
            progressDialogUtil.setProgress(progress);
            progressDialogUtil.show();
            handler.postDelayed(runnable, delayMillis);
        }

        @Override
        protected void doInBackground() {
            startSheet(path);
        }

        @Override
        protected void onProgressUpdate(int values) {

        }

        @Override
        protected void finishToDoInUIThread() {
            if (sheet.getState() != Sheet.STATE_OK) {
                switch (sheet.getState()) {
                    case Sheet.STATE_FILE_NOT_EXIST:
                        ToastUtil.show(activity, getString(R.string.file_not_exist));
                        progressDialogUtil.cancle();
                        break;
                    case Sheet.STATE_FILE_READ_ERROR:
                        ToastUtil.show(activity, getString(R.string.file_read_error));
                        progressDialogUtil.cancle();
                        break;
                }
                return;
            }
            progressDialogUtil.cancle();
            ShareValue.sheet = sheet;
            Intent intent = new Intent(MainActivity.this, ShowSheetActivity.class);
            startActivity(intent);
        }

        private void startSheet(final String path) {
            sheet = new Sheet(path);
        }


        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isWork()) {
                    progressDialogUtil.setProgress(progress++);
                    handler.postDelayed(this, delayMillis);
                }
            }
        };
    }
}
