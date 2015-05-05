package com.householdplanner.shoppingapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class ScreenHelp extends LinearLayout {

	public ScreenHelp(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setOrientation(LinearLayout.VERTICAL);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.budget_screen_help, this, true);
	}
		
}
