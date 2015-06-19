package com.householdplanner.shoppingapp.cross;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.usecases.UseCaseMarket;

public class AppGlobalState {

    private final static String STATE_SHOPPING_MODE = "pref_state_shopping_mode";
    private final static String STATE_MARKET_ID = "pref_market_id";
    private final static String STATE_MARKET_NAME = "pref_market_name";

    public enum AsyncTaskCodes {
        BasketTask(1),
        SynchronizationTask(2);

        private int _value;

        private AsyncTaskCodes(int value) {
            _value = value;
        }

        public int getValue() {
            return _value;
        }
    }

    //Shopping mode state
    private static Boolean mIsShoppingMode = null;
    //Current market Id
    private static Integer mMarketId = null;
    //Current market name
    private static String mMarketName = null;
    //Global state instance
    private static AppGlobalState mAppGlobalState;
    //Is application opened?
    private static boolean mIsActive = false;

    /**
     * Return the singleton instance
     *
     * @return AppGlobalState instance
     */
    public synchronized static AppGlobalState getInstance() {
        if (mAppGlobalState == null) {
            synchronized (AppGlobalState.class) {
                if (mAppGlobalState == null) {
                    mAppGlobalState = new AppGlobalState();
                }
            }
        }
        return mAppGlobalState;
    }

    /**
     * Set if the app is currently opened or not
     *
     * @param active true is opened
     */
    public synchronized void setActiveValue(boolean active) {
        mIsActive = active;
    }

    /**
     * Get if App is in shopping mode or not
     *
     * @param context Context
     * @return true if the app is in shopping mode
     */
    public synchronized boolean isShoppingMode(Context context) {
        if (mIsShoppingMode == null) {
            mIsShoppingMode = new Boolean(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(STATE_SHOPPING_MODE, false));
        }
        return mIsShoppingMode.booleanValue();
    }

    /**
     * Set a value for shopping mode state
     *
     * @param context context
     * @param value   shopping mode value, true or false
     */
    public synchronized void setShoppingMode(Context context, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STATE_SHOPPING_MODE, value);
        editor.commit();

        mIsShoppingMode = new Boolean(value);
    }

    /**
     * Get the current Market name
     *
     * @param context
     * @return current market name
     */
    public synchronized String getMarketName(Context context) {
        if (mMarketName == null) {
            mMarketName = PreferenceManager.getDefaultSharedPreferences(context).getString(STATE_MARKET_NAME, null);
            if (mMarketName == null) {
                mMarketId = new Integer(PreferenceManager.getDefaultSharedPreferences(context).getInt(STATE_MARKET_ID, 0));
                if (mMarketId.intValue() != 0) {
                    UseCaseMarket useCaseMarket = new UseCaseMarket(context);
                    Market market = useCaseMarket.getMarket(mMarketId.intValue());
                    if (market != null) {
                        mMarketName = market.name;
                    }
                    if (mMarketName != null) {
                        if (mMarketName.equals("a")) {
                            mMarketName = context.getResources().getString(R.string.textAllSupermarkets);
                        }
                    }
                }
            }
        }
        return mMarketName;
    }

    /**
     * Return the current market Id
     * @param context
     * @return market id
     */
    public synchronized int getMarketId(Context context) {
        mMarketId = new Integer(PreferenceManager.getDefaultSharedPreferences(context).getInt(STATE_MARKET_ID, 0));
        return mMarketId.intValue();
    }

    /**
     * Set market state
     *
     * @param context
     * @param value      market identifier
     * @param marketName market name
     */
    public synchronized void setMarket(Context context, int value, String marketName) {
        mMarketId = new Integer(value);
        mMarketName = marketName;

        if (marketName == null && mMarketId.intValue() != 0) {
            UseCaseMarket useCaseMarket = new UseCaseMarket(context);
            Market market = useCaseMarket.getMarket(mMarketId.intValue());
            if (market != null) {
                marketName = market.name;
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(STATE_MARKET_ID, mMarketId.intValue());
        editor.putString(STATE_MARKET_NAME, mMarketName);
        editor.commit();
    }

}
