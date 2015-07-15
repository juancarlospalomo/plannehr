package com.householdplanner.shoppingapp.stores;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

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
            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, "
            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + " TEXT, "
            + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + " INTEGER, "
            + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + " INTEGER);";

    //Database creation sql statement
    private static final String SQL_TABLE_CREATE_V2 = "CREATE TABLE "
            + "List(_id integer primary key autoincrement, "
            + "Modify integer, "
            + "Name text, "
            + "Market text, "
            + "Amount text, "
            + "UnitId integer, "
            + "Category text, "
            + "Comitted integer, "
            + "Sequence integer, "
            + "SDate date, "
            + "op integer, "
            + "SyncDate date);";

    //Database creation sql statement
    private static final String SQL_TABLE_CREATE_V5 = "CREATE TABLE "
            + ShoppingListContract.ProductEntry.TABLE_NAME + "(" + ShoppingListContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "Name TEXT COLLATE NOCASE, "
            + "Market TEXT, "
            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + " TEXT, "
            + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + " INTEGER, "
            + "Category INTEGER, "
            + "Comitted INTEGER);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            onUpgradeV2(db);
        }
        if (oldVersion < 5) {
            onUpgradeV5(db);
        }
        if (oldVersion < 6) {
            onUpgradeV6(db);
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
        database.execSQL(SQL_TABLE_CREATE_V2);
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
        database.execSQL(SQL_TABLE_CREATE_V5);
        database.execSQL("INSERT INTO " + ShoppingListContract.ProductEntry.TABLE_NAME
                + "(Name, Market,"
                + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                + "Category,"
                + "Comitted) "
                + "SELECT Name, Market,"
                + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                + "Category,"
                + "Comitted "
                + "FROM " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;");

        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old");
    }

    /**
     * Update table to V6 version
     *
     * @param database
     */
    private static void onUpgradeV6(SQLiteDatabase database) {
        String sql = "ALTER TABLE " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " RENAME TO " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old;";
        database.execSQL(sql);
        database.execSQL(SQL_TABLE_CREATE);

        Cursor cursor = getVersionLessThan6Rows(database);

        if ((cursor != null) & (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                String amount = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT));
                if (TextUtils.isEmpty(amount)) {
                    sql = "INSERT INTO " + ShoppingListContract.ProductEntry.TABLE_NAME + " ("
                            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID + ","
                            + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                            + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + ") VALUES ("
                            + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry._ID)) + ","
                            + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID)) + ","
                            + cursor.getInt(cursor.getColumnIndex("Comitted")) + ")";
                } else {
                    sql = "INSERT INTO " + ShoppingListContract.ProductEntry.TABLE_NAME + " ("
                            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID + ","
                            + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                            + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                            + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + ") VALUES ("
                            + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry._ID)) + ",'"
                            + cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT)) + "',"
                            + cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID)) + ","
                            + cursor.getInt(cursor.getColumnIndex("Comitted")) + ")";
                }
                database.execSQL(sql);
                cursor.moveToNext();
            }
        }

        if (cursor != null) cursor.close();
        database.execSQL("DROP TABLE IF EXISTS " + ShoppingListContract.ProductEntry.TABLE_NAME + "_old");
    }

    private static Cursor getVersionLessThan6Rows(SQLiteDatabase database) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + ","
                + ShoppingListContract.ProductEntry.TABLE_NAME + "_old." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT + ","
                + ShoppingListContract.ProductEntry.TABLE_NAME + "_old." + ShoppingListContract.ProductEntry.COLUMN_UNIT_ID + ","
                + ShoppingListContract.ProductEntry.TABLE_NAME + "_old.Comitted"
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + " INNER JOIN "
                + ShoppingListContract.ProductEntry.TABLE_NAME + "_old"
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "_old.Name "
                + " LEFT JOIN " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "="
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID;

        Cursor cursor = database.rawQuery(sql, null);
        return cursor;
    }

}
