package com.householdplanner.shoppingapp.models;

/**
 * Created by JuanCarlos on 26/05/2015.
 */
public class Budget {
    //Local identifier for the budget
    public int _id;
    //Month identifier number
    public int monthId;
    //Available amount
    public float available;
    //Target amount
    public float target;
    //Withdrawn amount
    public float withDrawn;
    //Wallet amount
    public float wallet;
    //Last withdrawn amount
    public float lastWithDrawn;
    //Last cash paid amount
    public float lastWallet;
}
