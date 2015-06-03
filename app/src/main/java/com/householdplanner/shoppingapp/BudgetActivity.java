package com.householdplanner.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.householdplanner.shoppingapp.fragments.FragmentBudget;

public class BudgetActivity extends BaseActivity {

    private final static String LOG_TAG = BudgetActivity.class.getSimpleName();
    //EXTRAS
    public final static String EXTRA_BUDGET_ID = "budget_id";
    public final static String EXTRA_MONTH_ID = "month_id";
    public final static String EXTRA_AVAILABLE = "available";
    public final static String EXTRA_TARGET = "target";

    private int mId;
    private int mMonthId = -1;
    private float mAvailable;
    private float mTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        getIntentData();
        setUpFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    private void setUpFragment() {
        FragmentBudget fragmentBudget = FragmentBudget.newInstance(mId, mMonthId, mAvailable, mTarget);
        fragmentBudget.setButtonOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragmentBudget)
                .commit();
    }

    /**
     * Get extras from Intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!=null) {
            mId = bundle.getInt(EXTRA_BUDGET_ID);
            if (mId > 0) {
                mMonthId = bundle.getInt(EXTRA_MONTH_ID);
                mAvailable = bundle.getFloat(EXTRA_AVAILABLE);
                mTarget = bundle.getFloat(EXTRA_TARGET);
            }
        }
    }
}
