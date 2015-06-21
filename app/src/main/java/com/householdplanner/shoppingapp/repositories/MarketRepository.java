package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

public class MarketRepository {
    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mMarketDbHelper;
    private Context mContext;

    public MarketRepository(Context context) {
        mMarketDbHelper = new DatabaseHelper(context);
        mContext = context;
    }

    private void open() throws SQLException {
        mDatabase = mMarketDbHelper.getWritableDatabase();
    }

    private SQLiteDatabase getDatabase() {
        if (mDatabase == null) {
            this.open();
        }
        return mDatabase;
    }

    public void close() {
        if (mDatabase != null) mDatabase.close();
        if (mMarketDbHelper != null) mMarketDbHelper.close();
    }

    /**
     * Create a new market
     *
     * @param market market object
     * @return true if it was created
     */
    public boolean insert(Market market) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, util.capitalize(market.name));
        long insertId = getDatabase().insert(ShoppingListContract.MarketEntry.TABLE_NAME, null, values);
        if (insertId > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Delete a market
     * @param marketId market id
     * @return true if it was deleted
     */
    public boolean delete(int marketId) {
        boolean result = getDatabase().delete(ShoppingListContract.MarketEntry.TABLE_NAME,
                ShoppingListContract.MarketEntry._ID + "=" + marketId, null) > 0;
        return result;
    }

    /**
     * Rename the market name for a supermarket id
     *
     * @param marketId      market id
     * @param newMarketName new market name
     * @return true if it was renamed
     */
    public boolean renameMarket(int marketId, String newMarketName) {
        boolean result = true;
        ContentValues values = new ContentValues();
        SQLiteDatabase database = getDatabase();

        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, util.capitalize(newMarketName));
        result = database.update(ShoppingListContract.MarketEntry.TABLE_NAME, values,
                ShoppingListContract.MarketEntry._ID + "=" + marketId, null) > 0;

        return result;
    }

    /**
     * Get the color represeting a supermarket
     *
     * @param name market name
     * @return color number
     */
    public Integer getMarketColor(String name) {
        Integer color = null;
        String sql = "SELECT " + ShoppingListContract.MarketEntry.COLUMN_COLOR
                + " FROM " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "='" + name + "'";
        Cursor cursor = getDatabase().rawQuery(sql, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String value = cursor.getString(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_COLOR));
                if (value != null) {
                    color = Integer.parseInt(value);
                }
            }
        }
        return color;
    }

    /**
     * Set a color to one supermarket
     *
     * @param marketId market identifier
     * @param color    color number
     */
    public void setColor(int marketId, int color) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_COLOR, color);
        getDatabase().update(ShoppingListContract.MarketEntry.TABLE_NAME, values,
                ShoppingListContract.MarketEntry._ID + "=" + marketId, null);
    }

}
