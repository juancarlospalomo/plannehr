package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;
import com.householdplanner.shoppingapp.stores.ShoppingListStore.TypeOperation;

import java.util.Locale;

public class ShoppingListRepository {

    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mShoppingListStore;
    private Cursor mCursor;
    private Context mContext;

    private String[] allColumns = {ShoppingListContract.ProductEntry._ID,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME,
            ShoppingListContract.ProductEntry.COLUMN_MARKET,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT,
            ShoppingListContract.ProductEntry.COLUMN_UNIT_ID,
            ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID,
            ShoppingListContract.ProductEntry.COLUMN_COMMITTED};

    public ShoppingListRepository(Context context) {
        mShoppingListStore = new DatabaseHelper(context);
        mContext = context;
    }

    public ShoppingListRepository(Context context, SQLiteDatabase database) {
        mShoppingListStore = new DatabaseHelper(context);
        mContext = context;
        mDatabase = database;
    }

    private void open() throws SQLException {
        mDatabase = mShoppingListStore.getWritableDatabase();
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
        if (mShoppingListStore != null) mShoppingListStore.close();
    }

    public boolean createProductItem(String productName,
                                     String market, String amount, int unitId, int categoryId) {

        return createProductItem(productName, market, amount,
                unitId, categoryId, 0, TypeOperation.Add, util.getDateTime());
    }

    /**
     * Create a product on the list
     *
     * @param productName
     * @param market
     * @param amount
     * @param unitId
     * @param categoryId
     * @param committed
     * @param operation
     * @param syncDate
     * @return
     */
    private boolean createProductItem(String productName, String market, String amount, int unitId, int categoryId,
                                      int committed, TypeOperation operation, String syncDate) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME, util.capitalize(productName));
        if (!TextUtils.isEmpty(market))
            values.put(ShoppingListContract.ProductEntry.COLUMN_MARKET, market.toLowerCase(Locale.getDefault()));
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, amount);
        values.put(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID, categoryId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_COMMITTED, committed);
        long insertId = getDatabase().insert(ShoppingListContract.ProductEntry.TABLE_NAME, null,
                values);

        if (insertId > 0) {
            return true;
        } else return false;
    }

    public void emptyCommitted() {
        boolean innerTransaction = false;
        if (!getDatabase().inTransaction()) {
            getDatabase().beginTransaction();
            innerTransaction = true;
        }
        try {
            Cursor cursor = getProductsCommitted();
            ProductHistoryRepository productHistoryRepository = new ProductHistoryRepository(mContext, getDatabase());
            String productName, market;
            int categoryId;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    productName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME));
                    market = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_MARKET));
                    categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID));
                    Cursor product = productHistoryRepository.getProduct(productName, market);
                    if (product != null) {
                        if (product.moveToFirst()) {
                            int historyCategoryId = product.getInt(product.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID));
                            int id = product.getInt(product.getColumnIndex(ShoppingListContract.ProductHistoryEntry._ID));
                            if (historyCategoryId != categoryId) {
                                productHistoryRepository.updateProductItem(id, market, categoryId);
                            }
                        } else {
                            productHistoryRepository.createProductItem(productName, market, categoryId);
                        }
                    }
                    cursor.moveToNext();
                }
            }
            deleteCommittedProducts();

            if (innerTransaction) {
                getDatabase().setTransactionSuccessful();
            }
        } catch (Exception e) {
        } finally {
            if (innerTransaction) {
                getDatabase().endTransaction();
            }
        }
    }

    public boolean updateProductItem(int id, String productName,
                                     String market, String amount, int unitId, int categoryId) {
        boolean result = false;
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME, util.capitalize(productName));
        if (market != null)
            values.put(ShoppingListContract.ProductEntry.COLUMN_MARKET, market.toLowerCase(Locale.getDefault()));
        else
            values.putNull(ShoppingListContract.ProductEntry.COLUMN_MARKET);
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, amount);
        values.put(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID, categoryId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_COMMITTED, 0);
        getDatabase().beginTransaction();
        long rowsAffected = getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME,
                values, "_id=" + id, null);
        if (rowsAffected > 0) {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
            int historyId = historyRepository.getProductId(productName, market);
            if (historyId > 0) {
                result = historyRepository.updateProductItem(id, market, categoryId);
                result = true;
            } else {
                result = true;
            }
        }
        if (result) {
            getDatabase().setTransactionSuccessful();
            getDatabase().endTransaction();
        } else {
            getDatabase().endTransaction();
        }

        return result;
    }


    public void moveToSupermaket(int id, String newMarket) {
        Cursor cursor = getDatabase().query(ShoppingListContract.ProductEntry.TABLE_NAME,
                new String[]{ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME,
                        ShoppingListContract.ProductEntry.COLUMN_MARKET}, "_id=" + id, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String productName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME));
                String market = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_MARKET));
                ContentValues values = new ContentValues();
                if (newMarket != null) {
                    values.put(ShoppingListContract.ProductEntry.COLUMN_MARKET, newMarket.toLowerCase(Locale.getDefault()));
                } else {
                    values.putNull(ShoppingListContract.ProductEntry.COLUMN_MARKET);
                }
                getDatabase().beginTransaction();
                if (getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME, values,
                        ShoppingListContract.ProductEntry._ID + "=" + id, null) > 0) {
                    ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
                    historyRepository.moveToSupermarket(productName, market, newMarket);
                    getDatabase().setTransactionSuccessful();
                }
                getDatabase().endTransaction();
            }
        }
    }

    /**
     * Rename supermarket
     *
     * @param oldMarket current name
     * @param newMarket new name
     */
    public void renameSupermarket(String oldMarket, String newMarket) {
        ContentValues values = new ContentValues();
        if (newMarket != null) {
            newMarket = newMarket.toLowerCase(Locale.getDefault());
        }
        if (oldMarket != null) {
            oldMarket = oldMarket.toLowerCase(Locale.getDefault());
        }
        values.put(ShoppingListContract.ProductEntry.COLUMN_MARKET, newMarket);
        getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME, values,
                ShoppingListContract.ProductEntry.COLUMN_MARKET + "='" + oldMarket + "'", null);

    }

    /**
     * Remove a supermarket from all products that owns it
     *
     * @param oldMarket market name
     */
    public void unSetMarket(String oldMarket) {
        ContentValues values = new ContentValues();
        values.putNull(ShoppingListContract.ProductEntry.COLUMN_MARKET);
        getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME, values,
                ShoppingListContract.ProductEntry.COLUMN_MARKET + "='" + oldMarket.toLowerCase(Locale.getDefault()) + "'", null);
    }

    /**
     * Delete product from list
     * @param id product id
     * @return true if it was deleted
     */
    public boolean deletePermanentProductItem(int id) {
        int rowsAffected = getDatabase().delete(ShoppingListContract.ProductEntry.TABLE_NAME,
                ShoppingListContract.ProductEntry._ID + "=" + id, null);
        if (rowsAffected > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Take the product to the cart
     *
     * @param id product identifier
     */
    public void commitProduct(int id) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_COMMITTED, 1);
        getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME, values, ShoppingListContract.ProductEntry._ID + "=" + id
                , null);
    }

    /**
     * Get out the product from the cart
     *
     * @param id product identifier
     */
    public void rollbackProduct(int id) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_COMMITTED, 0);
        getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME, values, ShoppingListContract.ProductEntry._ID + "=" + id
                , null);
    }

    /**
     * Get if there are at least one supermarket with products
     *
     * @return true if there are some supermarkets with products
     */
    public boolean existsProductsInSupermarket() {
        String selection = ShoppingListContract.ProductEntry.COLUMN_MARKET + " is not null ";
        Cursor cursor = getDatabase().query(ShoppingListContract.ProductEntry.TABLE_NAME, allColumns,
                selection, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) return true;
            else return false;
        } else {
            return false;
        }
    }

    /**
     * Get the products in the cart
     *
     * @return cursor with the products in the cart
     */
    public Cursor getProductsCommitted() {
        return this.getDatabase().query(ShoppingListContract.ProductEntry.TABLE_NAME, allColumns,
                ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=1 ", null, null, null, null);
    }

    /**
     * Clean the cart
     *
     * @return true if the products were deleted from the cart
     */
    public boolean deleteCommittedProducts() {
        return getDatabase().delete(ShoppingListContract.ProductEntry.TABLE_NAME, ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=1", null) > 0;
    }

    /**
     * Find out if one product is in Shopping List
     *
     * @param name product name
     * @return true if the product exists on the shopping list
     */
    public boolean existProductInNotCommittedList(String name) {
        boolean result = false;
        String sql = "SELECT " + ShoppingListContract.ProductEntry._ID + " "
                + "FROM " + ShoppingListContract.ProductEntry.TABLE_NAME + " "
                + "WHERE " + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + "'" + name + "'";

        Cursor cursor = this.getDatabase().rawQuery(sql, null);
        if (cursor.getCount() > 0) result = true;
        return result;
    }

}
