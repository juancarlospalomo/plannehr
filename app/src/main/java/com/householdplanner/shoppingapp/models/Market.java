package com.householdplanner.shoppingapp.models;

import android.text.TextUtils;

/**
 * Created by JuanCarlos on 19/06/2015.
 */
public class Market {
    //Local identifier for the market
    public int _id;
    //Market name
    public String name;
    //Color number for the market
    public int color;

    /**
     * Return if the entity is valid
     * @return
     */
    public boolean validate() {
        if (!TextUtils.isEmpty(name)) {
            return true;
        } else {
            return false;
        }
    }
}
