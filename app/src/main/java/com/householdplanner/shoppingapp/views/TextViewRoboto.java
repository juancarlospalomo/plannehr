package com.householdplanner.shoppingapp.views;

import com.householdplanner.shoppingapp.cross.font;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewRoboto extends TextView {

	public TextViewRoboto(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(font.getHelpTextFont(context));
	}

}
