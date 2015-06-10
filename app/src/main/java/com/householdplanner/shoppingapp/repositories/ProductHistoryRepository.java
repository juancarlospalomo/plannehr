package com.householdplanner.shoppingapp.repositories;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

import java.util.Locale;

public class ProductHistoryRepository {

    public static final int USUAL_CONFIRMED = 1;
    public static final int USUAL_PENDING = 3;
    public static final int UNUSUAL_CONFIRMED = 0;
    public static final int UNUSUAL_PENDING = 2;

    private Context mContext;
    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDatabaseHelper;
    private Cursor mCursor;

    private String[] allColumns = {ShoppingListContract.ProductHistoryEntry._ID,
            ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME,
            ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET,
            ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID
    };

    public ProductHistoryRepository(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public ProductHistoryRepository(Context context, SQLiteDatabase database) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
        mDatabase = database;
    }

    private void open() throws SQLException {
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        if ((mDatabase != null) && (mDatabase.isOpen())) {
            return mDatabase;
        } else {
            this.open();
            return mDatabase;
        }
    }

    public void close() {
        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
        if (mDatabaseHelper != null) mDatabaseHelper.close();
    }

    /**
     * Create a product in the catalog
     *
     * @param productName
     * @param market      market name
     * @param categoryId
     * @return true if it was created
     */
    public boolean createProductItem(String productName,
                                     String market, int categoryId) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME, productName);
        if (!TextUtils.isEmpty(market)) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET, market);
        }
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID, categoryId);
        long insertId = getDatabase().insert(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, null,
                values);
        if (insertId > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update a product
     *
     * @param id
     * @param market
     * @param categoryId
     * @return true if it was updated
     */
    public boolean updateProductItem(int id, String market, int categoryId) {
        boolean result = false;
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(market)) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET, market);
        } else {
            values.putNull(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET);
        }
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID, categoryId);
        if (getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values, "_id=" + id, null) > 0) {
            result = true;
        }
        return result;
    }

    public void deleteProduct(int id) {
        getDatabase().delete(ShoppingListContract.ProductHistoryEntry.TABLE_NAME,
                ShoppingListContract.ProductHistoryEntry._ID + "=" + id, null);
    }

    /**
     * Rename a supermarket
     *
     * @param oldMarket current name
     * @param newMarket new name
     */
    public void renameSupermarket(String oldMarket, String newMarket) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET, newMarket);
        if (getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values,
                ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='" + oldMarket + "'", null) > 0) {
        }

    }

    /**
     * Set the product in another supermarket
     *
     * @param product   product name
     * @param oldMarket current market name
     * @param newMarket new market name
     */
    public void moveToSupermarket(String product, String oldMarket, String newMarket) {
        ContentValues values = new ContentValues();
        if (newMarket != null) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET, newMarket.toLowerCase(Locale.getDefault()));
        } else {
            values.putNull(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET);
        }
        String selection = ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "='" + product + "' AND ";
        if (oldMarket == null) {
            selection += ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + " IS NULL";
        } else {
            selection += ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='" + oldMarket + "'";
        }
        if (getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values,
                selection, null) > 0) {
        }
    }

    /**
     * Remove one market from all products catalog
     *
     * @param oldMarket market name
     */
    public void unSetMarket(String oldMarket) {
        ContentValues values = new ContentValues();
        values.putNull(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET);
        if (getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values,
                ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='" + oldMarket.toLowerCase(Locale.getDefault()) + "'", null) > 0) {
        }
    }

    /**
     * Get the product id of a product with a specific name
     *
     * @param productName
     * @param market
     * @return product id
     */
    public int getProductId(String productName, String market) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry._ID +
                " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "='" + productName + "'";
        if (market != null) {
            sql += " AND " + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='" + market + "'";
        } else {
            sql += " AND (" + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + " is null "
                    + "OR " + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='')";
        }
        Cursor cursor = getDatabase().rawQuery(sql, null);
        if ((cursor != null) && (cursor.getCount() > 0)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Get a product by name from the catalog
     *
     * @param productName
     * @param market
     * @return
     */
    public Cursor getProduct(String productName, String market) {
        String query = "SELECT " + ShoppingListContract.ProductHistoryEntry._ID + ","
                + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ", "
                + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + ", "
                + ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "='" + productName + "'";
        if (!TextUtils.isEmpty(market))
            query += " AND " + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + "='" + market + "'";
        Cursor cursor = getDatabase().rawQuery(query, null);
        return cursor;
    }

    /**
     * Get product from catalog by product id
     *
     * @param id product id
     * @return cursor with the product
     */
    public Cursor getProduct(int id) {
        String query = "SELECT " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ", "
                + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + ", "
                + ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry._ID + "=" + id;
        Cursor cursor = getDatabase().rawQuery(query, null);
        return cursor;
    }

    /**
     * Get products that follow a string pattern in their names
     *
     * @param pattern string pattern
     * @return cursor
     */
    public Cursor GetProductFilteredByName(String pattern) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " LEFT JOIN " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " LIKE '" + pattern + "%'"
                + " AND " + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry._ID + " IS NULL"
                + " ORDER BY " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME;
        mCursor = this.getDatabase().rawQuery(sql, null);
        return mCursor;
    }

    /**
     * Get Suggestions for a string pattern
     *
     * @param pattern string pattern
     * @return cursor with the products matching the pattern
     */
    public Cursor getProductSuggestions(String pattern) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " LEFT JOIN " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " LIKE '" + pattern + "%'"
                + " AND " + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry._ID + " IS NULL"
                + " ORDER BY " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME;
        mCursor = this.getDatabase().rawQuery(sql, null);
        return mCursor;
    }

}
