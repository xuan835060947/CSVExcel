package com.wps.csvexcel.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.wps.csvexcel.R;
import com.wps.csvexcel.service.MoreFasterSheetService;
import com.wps.csvexcel.share.ShareValue;
import com.wps.csvexcel.view.SheetView;

public class ShowSheetActivity extends BaseActivity {
	
	private LinearLayout llBottomBar;
	private Button btFilter;
	private SheetView sv;
	private boolean isFilter;
	private ServiceConnection conn;
	@Override
	public int getLayoutId() {
		return R.layout.activity_show_sheet;
	}

	@Override
	public void setUI() {
		sv = (SheetView) findViewById(R.id.svSheetView);
		sv.setSheet(ShareValue.sheet);
		llBottomBar = (LinearLayout) findViewById(R.id.llBottomBar);
		btFilter = (Button)mFindViewAndSetOnClick(R.id.btFilter);
		startService();
	}

	public void startService() {
		Intent serviceIntent = new Intent(this, MoreFasterSheetService.class);
				conn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				MoreFasterSheetService.SheetBackgroundServiceBinder binder = (MoreFasterSheetService.SheetBackgroundServiceBinder) service;
				MoreFasterSheetService moreFasterSheetService = binder.getService();
				ShareValue.moreFasterSheetService = moreFasterSheetService;
				moreFasterSheetService.setSheet(ShareValue.sheet);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.e(" onServiceDisconnected ", " onServiceDisconnected ");
			}

		};
		bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void OnClickListener(View view) {
		switch(view.getId()){
		case R.id.btFilter:
			changeFilter();
			break;
		}
	}

	private void changeFilter(){
		isFilter = sv.setFilter(!isFilter);
		if(isFilter){
			btFilter.setText(getString(R.string.filter_cancel));
		}else{
			btFilter.setText(getString(R.string.filter));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (conn != null){
			unbindService(conn);
		}
	}
	
}
