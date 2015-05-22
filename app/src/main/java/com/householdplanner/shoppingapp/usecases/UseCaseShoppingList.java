package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class UseCaseShoppingList {

    private Context mContext;
    //Fields for projection
    private static String[] mProjection = new String[]{ShoppingListContract.ProductEntry._ID,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT,
            ShoppingListContract.ProductEntry.COLUMN_UNIT_ID,
            ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID,
            ShoppingListContract.ProductEntry.COLUMN_MARKET};

    public UseCaseShoppingList(Context context) {
        mContext = context;
    }

    /**
     * Search all the entered product list
     *
     * @return List with all entered product list
     */
    public List<Product> getFullEnteredList() {
        //Variable for returning
        List<Product> productList = new ArrayList<Product>();

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, null, null, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");

        if ((cursor != null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                Product product = new Product();
                product._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry._ID));
                product.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_MARKET));
                product.amount = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT));
                product.unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID));
                product.categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID));
                productList.add(product);
                cursor.moveToNext();
            }
        }
        return productList;
    }

}
