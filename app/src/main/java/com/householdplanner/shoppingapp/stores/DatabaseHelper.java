package com.householdplanner.shoppingapp.stores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShoppingList.db";
    private static final int DATABASE_VERSION = 6;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        ShoppingListStore.onCreate(database);
        ProductHistoryStore.onCreate(database);
        BudgetStore.onCreate(database);
        MarketStore.onCreate(database);
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        upgradeDb(database, oldVersion, newVersion);
        //Order is very important.  It is regarding to the relationships
        //in inverse order to the FK´s
        MarketStore.onUpgrade(database, oldVersion, newVersion);
        ProductHistoryStore.onUpgrade(database, oldVersion, newVersion);
        ShoppingListStore.onUpgrade(database, oldVersion, newVersion);
        BudgetStore.onUpgrade(database, oldVersion, newVersion);
    }

    /**
     * Upgrade The entire database, mainly to delete tables
     *
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    private void upgradeDb(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            //Delete MarketCategory and Ticket tables as they are not going to be used anymore
            database.execSQL("DROP TABLE IF EXISTS MarketCategory");
            database.execSQL("DROP TABLE IF EXISTS Ticket");
        }
    }

}
