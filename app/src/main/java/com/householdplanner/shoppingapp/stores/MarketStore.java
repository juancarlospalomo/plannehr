package com.householdplanner.shoppingapp.stores;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;

public class MarketStore {

    public static final String VIRTUAL_MARKET_NAME = "a";

    // Database creation SQL statement
    private static final String SQL_TABLE_CREATE = "CREATE TABLE " + ShoppingListContract.MarketEntry.TABLE_NAME
            + " (" + ShoppingListContract.MarketEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + " INTEGER, "
            + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " TEXT COLLATE NOCASE, "
            + ShoppingListContract.MarketEntry.COLUMN_COLOR + " INTEGER);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_TABLE_CREATE);
        insertVirtualMarket(database);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            onUpgradeV3(database);
        }
        if (oldVersion < 4) {
            onUpgradeV4(database);
        }
    }

    private static void onUpgradeV3(SQLiteDatabase database) {
        insertVirtualMarket(database);
    }

    private static void onUpgradeV4(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.MarketEntry.TABLE_NAME
                + "(" + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ","
                + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ","
                + ShoppingListContract.MarketEntry.COLUMN_COLOR + ") "
                + "SELECT " + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ","
                + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ",null "
                + "FROM " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;");
        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old");
    }

    /**
     * Create a supermarket to show the label All
     * @param database
     */
    public static void insertVirtualMarket(SQLiteDatabase database) {
        String query = "SELECT MAX(" + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ") FROM "
                + ShoppingListContract.MarketEntry.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        int marketId = 1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                marketId = cursor.getInt(0);
                marketId++;
            }
        }
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_ID, marketId);
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, VIRTUAL_MARKET_NAME); //For appearing first.
        database.insert(ShoppingListContract.MarketEntry.TABLE_NAME, null, values);
    }

}
