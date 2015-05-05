package com.householdplanner.shoppingapp.cross;

import android.content.Context;
import android.graphics.Typeface;

public class font {

	private static final String ROBOTO_LIGHT_PATH = "fonts/Roboto-Light.ttf";
	private static final String ROBOTO_BOLD_PATH = "fonts/Roboto-Bold.ttf";
	
	public static Typeface getListItemFont(Context context) {
		return Typeface.createFromAsset(context.getAssets() , ROBOTO_LIGHT_PATH);
	}

	public static Typeface getListItemSelectedFont(Context context) {
		return Typeface.createFromAsset(context.getAssets(), ROBOTO_BOLD_PATH);
	}
	
	public static Typeface getEditTextFont(Context context) {
		return Typeface.createFromAsset(context.getAssets() , ROBOTO_LIGHT_PATH);
	}
	
	public static Typeface getButtonFont(Context context) {
		return Typeface.createFromAsset(context.getAssets(), ROBOTO_BOLD_PATH);
	}
	
	public static Typeface getMessageFont(Context context) {
		return Typeface.createFromAsset(context.getAssets(), ROBOTO_BOLD_PATH);
	}
	
	public static Typeface getHelpTextFont(Context context) {
		return Typeface.createFromAsset(context.getAssets(), ROBOTO_LIGHT_PATH);
	}
	
}
