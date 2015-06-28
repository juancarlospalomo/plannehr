package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

public class ShoppingListRepository {

    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mShoppingListStore;

    public ShoppingListRepository(Context context) {
        mShoppingListStore = new DatabaseHelper(context);
    }

    public ShoppingListRepository(Context context, SQLiteDatabase database) {
        mShoppingListStore = new DatabaseHelper(context);
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
        if (mDatabase != null) mDatabase.close();
        if (mShoppingListStore != null) mShoppingListStore.close();
    }

    /**
     * Insert a product item on the List
     *
     * @param product item
     * @return true if it was inserted
     */
    public boolean createProductItem(Product product) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID, product.productId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, product.amount != null ? product.amount : null);
        values.put(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID, product.unitId);
        values.put(ShoppingListContract.ProductEntry.COLUMN_COMMITTED, product.committed ? 1 : 0);
        long insertId = getDatabase().insert(ShoppingListContract.ProductEntry.TABLE_NAME, null,
                values);

        if (insertId > 0) {
            product._id = (int) insertId;
            return true;
        } else return false;
    }

    /**
     * Update an item in the List
     *
     * @param product item
     * @return true if it was updated
     */
    public boolean updateProductItem(Product product) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT, product.amount);
        values.put(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID, product.unitId);

        result = getDatabase().update(ShoppingListContract.ProductEntry.TABLE_NAME,
                values, ShoppingListContract.ProductEntry._ID + "=" + product._id, null) > 0;

        return result;
    }

    /**
     * Delete product from list
     *
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
     * Delete the product from List whose ProductId = id
     * @param id productid (FK to Catalog)
     * @return true if it was deleted
     */
    public boolean deleteProductByProductId(int id) {
        int rowsAffected = getDatabase().delete(ShoppingListContract.ProductEntry.TABLE_NAME,
                ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID + "=" + id, null);
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
     * Clean the cart
     *
     * @return true if the products were deleted from the cart
     */
    public boolean deleteCommittedProducts() {
        return getDatabase().delete(ShoppingListContract.ProductEntry.TABLE_NAME, ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=1", null) > 0;
    }

}
