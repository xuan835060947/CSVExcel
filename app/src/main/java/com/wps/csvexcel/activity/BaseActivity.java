package com.wps.csvexcel.activity;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;


public abstract class BaseActivity extends Activity {
	
	
	protected String TAG = this.toString();
	private OnClickListener onClick;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onClick = new OnClickListener() {
			@Override
			public void onClick(View view) {
				OnClickListener(view);
			}
		};
		setContentView(getLayoutId());
		setUI();
		
	}
	
	public final View mFindViewAndSetOnClick(int id) {
		View view = findViewById(id);
		view.setOnClickListener(onClick);
		return view;
	}

	public final void mFindViewAndSetOnClick(View view) {
		view.setOnClickListener(onClick);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}
	
	
	public abstract int getLayoutId();

	public abstract void setUI();

	public abstract void OnClickListener(View view);
}
