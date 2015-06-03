package com.householdplanner.shoppingapp;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.householdplanner.shoppingapp.fragments.FragmentSetting;

/**
 * Created by JuanCarlos on 25/05/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FragmentSetting())
                .commit();

    }
}
