package com.householdplanner.shoppingapp.repositories;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.ProductHistory;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

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
     * Insert a product in the catalog
     *
     * @param productHistory product to be inserted
     * @return product id
     */
    public int createProductItem(ProductHistory productHistory) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME, productHistory.name);
        if (productHistory.marketId != 0) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID, productHistory.marketId);
        }
        long insertId = getDatabase().insert(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, null,
                values);
        return (int) insertId;
    }

    /**
     * Update a product in the catalog
     *
     * @param productHistory catalog product
     * @return true if it was updated
     */
    public boolean updateProductItem(ProductHistory productHistory) {
        boolean result = false;
        ContentValues values = new ContentValues();
        if (productHistory.marketId != 0) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID, productHistory.marketId);
        } else {
            values.putNull(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID);
        }
        if (productHistory.name != null) {
            values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME, util.capitalize(productHistory.name));
        }

        String whereClause = ShoppingListContract.ProductHistoryEntry._ID + "=" + productHistory._id;

        result = getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME,
                values, whereClause, null) > 0;

        return result;
    }

    /**
     * Delete a product from the Catalog
     *
     * @param id product id (PK)
     */
    public void deleteProduct(int id) {
        getDatabase().delete(ShoppingListContract.ProductHistoryEntry.TABLE_NAME,
                ShoppingListContract.ProductHistoryEntry._ID + "=" + id, null);
    }

    /**
     * Change the supermarket of all products that have another
     *
     * @param currentMarketId current market id of the products
     * @param newMarketId     new Market Id for the products that have currentMarketId
     * @param newMarketName   Market name of the newMarketId
     */
    public void updateSupermarket(int currentMarketId, int newMarketId, String newMarketName) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID, newMarketId);
        getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values,
                ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=" + currentMarketId, null);
    }


    /**
     * Remove one market from all products catalog
     *
     * @param marketId market id of the products
     */
    public void unSetMarket(int marketId) {
        ContentValues values = new ContentValues();
        values.putNull(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID);
        getDatabase().update(ShoppingListContract.ProductHistoryEntry.TABLE_NAME, values,
                ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=" + marketId, null);
    }

    /**
     * Get the product id of a product with a specific name
     *
     * @param productName
     * @param marketId    market id
     * @return product id
     */
    public int getProductId(String productName, int marketId) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry._ID
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "='" + productName + "'";
        if (marketId != 0) {
            sql += " AND " + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=" + marketId;
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
     * Get products that follow a string pattern in their names
     *
     * @param pattern string pattern
     * @return cursor
     */
    public Cursor GetProductFilteredByName(String pattern) {
        String sql = "SELECT " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + ","
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " AS " + ShoppingListContract.ProductHistoryEntry.ALIAS_MARKET_NAME
                + " FROM " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME
                + " LEFT JOIN " + ShoppingListContract.ProductEntry.TABLE_NAME
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID
                + " LEFT JOIN " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "="
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " LIKE '%" + pattern + "%'"
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
                + " ON " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID
                + " WHERE " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " LIKE '%" + pattern + "%'"
                + " AND " + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry._ID + " IS NULL"
                + " ORDER BY " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME;
        mCursor = this.getDatabase().rawQuery(sql, null);
        return mCursor;
    }

}
