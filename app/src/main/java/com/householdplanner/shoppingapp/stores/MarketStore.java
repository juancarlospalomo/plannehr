package com.householdplanner.shoppingapp.stores;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;

public class MarketStore {

    public static final String VIRTUAL_MARKET_NAME = "a";

    // Database creation SQL statement
    private static final String SQL_TABLE_CREATE = "CREATE TABLE " + ShoppingListContract.MarketEntry.TABLE_NAME
            + " (" + ShoppingListContract.MarketEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " TEXT COLLATE NOCASE, "
            + ShoppingListContract.MarketEntry.COLUMN_COLOR + " INTEGER);";

    // Database creation SQL statement
    private static final String SQL_TABLE_CREATE_V4 = "CREATE TABLE "
            + "Market ("
            + ShoppingListContract.MarketEntry._ID + " integer primary key autoincrement, "
            + "MarketId integer, "
            + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " text COLLATE NOCASE, "
            + ShoppingListContract.MarketEntry.COLUMN_COLOR + " integer);";

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
        if (oldVersion < 6) {
            onUpgradeV6(database);
        }
    }

    /**
     * Create an useless market starting by 'a'.
     * It´s only to order the markets and appears "All" in the first position
     *
     * @param database
     */
    private static void onUpgradeV3(SQLiteDatabase database) {
        insertVirtualMarket(database);
    }

    /**
     * Add Color column
     *
     * @param database
     */
    private static void onUpgradeV4(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE_V4);
        database.execSQL("INSERT INTO " + ShoppingListContract.MarketEntry.TABLE_NAME
                + "(MarketId" + ","
                + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ","
                + ShoppingListContract.MarketEntry.COLUMN_COLOR + ") "
                + "SELECT MarketId,"
                + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ",null "
                + "FROM " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;");
        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old");
    }

    /**
     * Remove MarketID column
     *
     * @param database
     */
    private static void onUpgradeV6(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);

        sql = "SELECT " + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ","
                + ShoppingListContract.MarketEntry.COLUMN_COLOR + " "
                + "FROM " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old;";
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor != null & cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                sql = "INSERT INTO " + ShoppingListContract.MarketEntry.TABLE_NAME
                        + "(" + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ","
                        + ShoppingListContract.MarketEntry.COLUMN_COLOR + ") VALUES ('"
                        + util.capitalize(cursor.getString(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME))) + "',"
                        + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_COLOR)) + ")";
                database.execSQL(sql);
                cursor.moveToNext();
            }
        }

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.MarketEntry.TABLE_NAME + "_old");
    }

    /**
     * Create a supermarket to show the label All
     *
     * @param database
     */
    public static void insertVirtualMarket(SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, VIRTUAL_MARKET_NAME); //For appearing first.
        database.insert(ShoppingListContract.MarketEntry.TABLE_NAME, null, values);
    }

}
