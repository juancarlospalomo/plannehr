package com.householdplanner.shoppingapp;

import android.os.Bundle;
import android.view.Menu;

import com.householdplanner.shoppingapp.fragments.FragmentMarket;

public class MarketActivity extends BaseActivity {

    private final static String LOG_TAG = MarketActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        initActivity();
    }

    /**
     * Init the activity loading the needed fragments
     */
    private void initActivity() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FragmentMarket())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu);
    }

}
