package com.householdplanner.shoppingapp.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.householdplanner.shoppingapp.R;

/**
 * Created by JuanCarlos on 25/05/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentSetting extends PreferenceFragment {

    public static final String PREF_SHOW_PRODUCTS_NOT_SET = "prefShowProductsNotSet";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
