package com.householdplanner.shoppingapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
    protected boolean mIsContextualMode = false;
    //Activity toolbar
    protected Toolbar mActionBarToolbar = null;
    //Toolbar Contextual Mode callback
    private ToolbarContextualMode mToolbarModeCallback = null;
    //Hold the current activity title for restoring later when contextual mode is finished
    private String mToolbarTitle;
    //Set if if has to show the title in the Toolbar
    private boolean mShowTitle = true;
    //Navigation drawer
    private DrawerLayout mDrawerLayout;
    //Drawer listener linked to the ActionBar
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolbarActionBar();
    }

    /**
     * To set the title or not on the toolbar
     *
     * @param value
     */
    protected void setTitleVisible(boolean value) {
        mShowTitle = value;
    }

    /**
     * Check if this activity has navigation drawer
     *
     * @return
     */
    private boolean hasNavigationDrawer() {
        return mDrawerLayout != null;
    }

    //Close the drawers opened in the drawer layout
    protected void closeDrawers() {
        if (mDrawerLayout!=null)
            mDrawerLayout.closeDrawers();
    }

    /**
     * Create the navigation drawer
     */
    protected void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.text_accessibility_drawer_open,
                R.string.text_accessibility_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    /**
     * Executed once instance state has been restored
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newChange) {
        super.onConfigurationChanged(newChange);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newChange);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsContextualMode) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_action_down);
        } else {
            if (hasNavigationDrawer()) {
                mDrawerToggle.syncState();
            } else {
                mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            }
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
                    if (!hasNavigationDrawer()) {
                        finish();
                    } else {
                        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                            mDrawerLayout.closeDrawer(Gravity.START);
                        } else {
                            mDrawerLayout.openDrawer(Gravity.START);
                        }
                        return super.onOptionsItemSelected(item);
                    }
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(mShowTitle);
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
        if (mShowTitle) {
            mToolbarTitle = mActionBarToolbar.getTitle().toString();
        }
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
        if (mToolbarModeCallback != null) {
            mToolbarModeCallback.onFinishToolbarContextualMode();
            if (mShowTitle) {
                mActionBarToolbar.setTitle(mToolbarTitle);
            } else {
                mActionBarToolbar.setTitle("");
            }
            invalidateOptionsMenu();
        }
    }

}
