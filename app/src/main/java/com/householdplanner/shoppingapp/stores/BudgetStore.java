package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;

public class BudgetStore {

    // Database creation SQL statement
    private static final String SQL_TABLE_CREATE = "CREATE TABLE "
            + ShoppingListContract.BudgetEntry.TABLE_NAME
            + "(" + ShoppingListContract.BudgetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ShoppingListContract.BudgetEntry.COLUMN_MONTH + " INTEGER, "
            + ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE + " REAL, "
            + ShoppingListContract.BudgetEntry.COLUMN_TARGET + " REAL, "
            + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN + " REAL, "
            + ShoppingListContract.BudgetEntry.COLUMN_WALLET + " REAL, "
            + ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN + " REAL, "
            + ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET + " REAL);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            onUpgradeV5(database);
        }
    }

    /**
     * Update table to V5 version
     * @param database
     */
    private static void onUpgradeV5(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.BudgetEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.BudgetEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.BudgetEntry.TABLE_NAME
                + "(" + ShoppingListContract.BudgetEntry.COLUMN_MONTH + ","
                + ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE + ","
                + ShoppingListContract.BudgetEntry.COLUMN_TARGET + ","
                + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN + ","
                + ShoppingListContract.BudgetEntry.COLUMN_WALLET + ","
                + ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN + ","
                + ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET + ") "
                + "SELECT " + ShoppingListContract.BudgetEntry.COLUMN_MONTH + ","
                + ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE + ","
                + ShoppingListContract.BudgetEntry.COLUMN_TARGET + ","
                + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN + ","
                + ShoppingListContract.BudgetEntry.COLUMN_WALLET + ","
                + ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN + ","
                + ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET + " "
                + "FROM " + ShoppingListContract.BudgetEntry.TABLE_NAME + "_old;");

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.BudgetEntry.TABLE_NAME + "_old");
    }

}
