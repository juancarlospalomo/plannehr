package com.householdplanner.shoppingapp.cross;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

public class ProgressCircle {

	private ProgressBar mProgress = null;
	
	public ProgressCircle(Context context) {
		mProgress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		LayoutParams progressParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progressParams.gravity = Gravity.CENTER;
		mProgress.setLayoutParams(progressParams);
		mProgress.setVisibility(View.GONE);
		
		LinearLayout layout = new LinearLayout(context);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(layoutParams);
		layout.setGravity(Gravity.CENTER);
		layout.addView(mProgress);
		((Activity) context).addContentView(layout, layoutParams);
	}
	
	public void show() {
		mProgress.setVisibility(View.VISIBLE);
	}
	
	public void dismiss() {
		mProgress.setVisibility(View.GONE);
	}
	
}
