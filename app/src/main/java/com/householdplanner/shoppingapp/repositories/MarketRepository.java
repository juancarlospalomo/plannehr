package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

import java.util.Locale;

public class MarketRepository {
    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mMarketDbHelper;
    private Cursor mCursor;
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
        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
        if (mMarketDbHelper != null) mMarketDbHelper.close();
    }

    /**
     * Create a new market
     * @param marketName market name
     * @return true if it was created
     */
    public boolean createMarketItem(String marketName) {
        int marketId = 1;
        String query = "SELECT MAX(" + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ") FROM "
                + ShoppingListContract.MarketEntry.TABLE_NAME;
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                marketId = cursor.getInt(0);
                marketId++;
            }
        }
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_ID, marketId);
        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, marketName.trim().toLowerCase(Locale.getDefault()));
        long insertId = getDatabase().insert(ShoppingListContract.MarketEntry.TABLE_NAME, null, values);

        if (insertId > 0) {
            close();
            return true;
        } else {
            close();
            return false;
        }
    }

    /**
     * Get markets that have products set
     * @return markets cursor
     */
    public Cursor getMarketsWithProducts() {
        String sql = "SELECT " + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ", "
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_COLOR + " "
                + "FROM " + ShoppingListContract.MarketEntry.TABLE_NAME + " INNER JOIN " + ShoppingListContract.ProductEntry.TABLE_NAME + " "
                + "ON " + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "=" + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_MARKET + " "
                + "WHERE " + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_MARKET + " is not null "
                + "GROUP BY " + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ", "
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_COLOR + " ";

        Cursor cursor = getDatabase().rawQuery(sql, null);
        return cursor;
    }

    /**
     * Delete a Market
     * @param marketId market identifier
     * @param marketName market name
     * @return true if the market was successfully deleted
     */
    public boolean deleteMarketItem(int marketId, String marketName) {
        int result;
        getDatabase().beginTransaction();

        result = getDatabase().delete(ShoppingListContract.MarketEntry.TABLE_NAME,
                ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + "=" + marketId, null);
        if (result > 0) {
            ShoppingListRepository listRepository = new ShoppingListRepository(mContext, getDatabase());
            listRepository.unSetMarket(marketName);
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
            historyRepository.unSetMarket(marketName);
        }
        if (result > 0) {
            getDatabase().setTransactionSuccessful();
            getDatabase().endTransaction();
            return true;
        } else {
            getDatabase().endTransaction();
            return false;
        }
    }

    /**
     * Rename the market name for a supermarket id
     * @param marketId market id
     * @param oldMarket current market name
     * @param newMarketName new market name
     * @return true if it was renamed
     */
    public boolean renameMarket(int marketId, String oldMarket, String newMarketName) {
        boolean result = true;
        ContentValues values = new ContentValues();
        SQLiteDatabase database = getDatabase();

        values.put(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME, newMarketName.toLowerCase(Locale.getDefault()));

        database.beginTransaction();
        database.update(ShoppingListContract.MarketEntry.TABLE_NAME, values,
                ShoppingListContract.MarketEntry._ID + "=" + marketId, null);
        ShoppingListRepository listRepository = new ShoppingListRepository(mContext, getDatabase());
        listRepository.renameSupermarket(oldMarket, newMarketName);

        ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
        historyRepository.renameSupermarket(oldMarket, newMarketName);

        database.setTransactionSuccessful();
        database.endTransaction();

        return result;
    }

    /**
     * Get all markets in table
     * @return cursor with existing markets
     */
    public Cursor getAllMarkets() {
        mCursor = this.getDatabase().query(ShoppingListContract.MarketEntry.TABLE_NAME,
                new String[]{ShoppingListContract.MarketEntry._ID,
                        ShoppingListContract.MarketEntry.COLUMN_MARKET_ID,
                        ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME,
                        ShoppingListContract.MarketEntry.COLUMN_COLOR},
                ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "<>'a'", null, null, null,
                ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME);
        return mCursor;
    }

    /**
     * Get the market name that belongs to one id
     * @param id market id
     * @return Market name string
     */
    public String getMarketName(int id) {
        String marketName = null;
        String sql = "SELECT " + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME
                + " FROM " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + "=" + id;
        Cursor cursor = getDatabase().rawQuery(sql, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME));
            }
        }
        return marketName;
    }

    /**
     * Get the market id that belongs to one market name
     * @param name market name
     * @return market id
     */
    public int getMarketId(String name) {
        int marketId = 0;
        String sql = "SELECT " + ShoppingListContract.MarketEntry.COLUMN_MARKET_ID
                + " FROM " + ShoppingListContract.MarketEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "='" + name.trim() + "'";
        Cursor cursor = getDatabase().rawQuery(sql, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                marketId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_MARKET_ID));
            }
        }
        return marketId;
    }

    /**
     * Get the color represeting a supermarket
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
     * @param marketId market identifier
     * @param color color number
     */
    public void setColor(int marketId, int color) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.MarketEntry.COLUMN_COLOR, color);
        getDatabase().update(ShoppingListContract.MarketEntry.TABLE_NAME, values,
                ShoppingListContract.MarketEntry.COLUMN_MARKET_ID + "=" + marketId, null);
    }

}
