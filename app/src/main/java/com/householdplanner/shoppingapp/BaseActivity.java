package com.householdplanner.shoppingapp;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by JuanCarlos on 25/05/2015.
 * Based on https://github.com/google/iosched/blob/dfaf8b83ad1b3e7c8d1af0b08d59caf4223e0b95/android/src/main/java/com/google/samples/apps/iosched/ui/BaseActivity.java
 */
public class BaseActivity extends AppCompatActivity {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    /**
     * Interface to communicate with fragments
     */
    public interface ToolbarContextualMode {
        //To send a signal that toolbar is in contextual mode
        public void onCreateToolbarContextualMode();

        //To end a signal that toolbar has finished its contextual mode
        public void onFinishToolbarContextualMode();
    }

    //Hold if the toolbar is in contextual mode
    private boolean mIsContextualMode = false;
    //Activity toolbar
    protected Toolbar mActionBarToolbar = null;
    //Toolbar Contextual Mode callback
    private ToolbarContextualMode mToolbarModeCallback = null;
    //Hold the current activity title for restoring later when contextual mode is finished
    private String mToolbarTitle;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolbarActionBar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsContextualMode) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_action_down);
        } else {
            mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mIsContextualMode) {
                    finishToolbarContextualActionMode();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Init the Toolbar as the ActionBar
     */
    private void initToolbarActionBar() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Return the existing ActionBar as Toolbar
     *
     * @return
     */
    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    /**
     * Start the Toolbar action mode
     *
     * @param mode ToolbarMode callback
     */
    public void startToolbarContextualActionMode(ToolbarContextualMode mode) {
        mToolbarModeCallback = mode;
        mIsContextualMode = true;
        mToolbarTitle = mActionBarToolbar.getTitle().toString();
        if (mToolbarModeCallback != null) {
            mToolbarModeCallback.onCreateToolbarContextualMode();
            invalidateOptionsMenu();
        }
    }

    /**
     * Finish the contextual action mode of the Toolbar
     */
    public void finishToolbarContextualActionMode() {
        mIsContextualMode = false;
        if (mToolbarModeCallback!=null) {
            mToolbarModeCallback.onFinishToolbarContextualMode();
            mActionBarToolbar.setTitle(mToolbarTitle);
            invalidateOptionsMenu();
        }
    }

}
