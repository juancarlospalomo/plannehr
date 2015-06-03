package com.householdplanner.shoppingapp.cross;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.householdplanner.shoppingapp.R;

public class AppPreferences extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
