package com.householdplanner.shoppingapp.models;

import android.text.TextUtils;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class Product {
    //Local identifier for the product
    public int _id;
    //Product Id relationship
    public int productId;
    //Product name
    public String name;
    //Market Id
    public int marketId;
    //Market name
    public String marketName;
    //Quantity
    public String amount;
    //Measure unit id
    public int unitId;
    //Hold if the product is in the basket
    public boolean committed;

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
