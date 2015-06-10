package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;

public class ShoppingListStore {

    public enum TypeOperation {
        Add(1),
        Update(2),
        Delete(3),
        Unchanged(4);

        private final int _value;

        private TypeOperation(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }
    }

    //Database creation sql statement
    private static final String SQL_TABLE_CREATE = "CREATE TABLE "
            + ShoppingListContract.ProductEntry.TABLE_NAME + "(" + ShoppingListContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT COLLATE NOCASE, "
            + ShoppingListContract.ProductEntry.COLUMN_MARKET + " TEXT, "
            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + " TEXT, "
            + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + " INTEGER, "
            + ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID + " INTEGER, "
            + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + " INTEGER);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            onUpgradeV2(db);
        }
        if (oldVersion < 5 && newVersion == 5) {
            onUpgradeV5(db);
        }
    }

    /**
     * Upgrade table to V2 version
     *
     * @param database
     */
    private static void onUpgradeV2(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.ProductEntry.TABLE_NAME + "(Modify, Name, Market, Amount, "
                + "UnitId, Category, Comitted, Sequence, SDate, op, SyncDate) "
                + "SELECT Modify, Name, Brand, Amount, UnitId, Category, Comitted, "
                + "Sequence, SDate, op, SyncDate FROM " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;");
        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old");
    }

    /**
     * Update table to V5 version
     *
     * @param database
     */
    private static void onUpgradeV5(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);
        database.execSQL("INSERT INTO " + ShoppingListContract.ProductEntry.TABLE_NAME
                + "(" + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + ","
                + ShoppingListContract.ProductEntry.COLUMN_MARKET + ","
                + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                + ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID + ","
                + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + ") "
                + "SELECT " + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + ","
                + ShoppingListContract.ProductEntry.COLUMN_MARKET + ","
                + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                + ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID + ","
                + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + " "
                + "FROM " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;");

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old");
    }

}
