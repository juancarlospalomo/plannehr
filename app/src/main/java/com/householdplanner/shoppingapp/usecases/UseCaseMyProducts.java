package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.ProductHistory;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class UseCaseMyProducts {

    private Context mContext;
    //Fields for projection
    private static String[] mProjection = new String[]{ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + " AS " + ShoppingListContract.ProductHistoryEntry.ALIAS_ID,
            ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME,
            ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET,
            ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID};

    public UseCaseMyProducts(Context context) {
        mContext = context;
    }

    /**
     * Search all the entered product list
     *
     * @return List with all entered product list
     */
    public List<ProductHistory> getMyProductListNotEntered() {
        //Variable for returning
        List<ProductHistory> productList = new ArrayList<ProductHistory>();

        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry._ID + " IS NULL";
        String orderBy = ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME;

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductHistoryEntry.CONTENT_URI,
                mProjection, selection, null, orderBy);

        if ((cursor != null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                ProductHistory product = new ProductHistory();
                product._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_ID));
                product.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET));
                product.categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_CATEGORY_ID));
                productList.add(product);
                cursor.moveToNext();
            }
        }
        return productList;
    }

    /**
     * Copy a product from the my products list (history) to the current shopping list
     * @param productHistory product to copy to the shopping list
     * @return true if it was created successfully
     */
    public boolean copyToShoppingList(ProductHistory productHistory) {
        boolean result = false;
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        result = shoppingListRepository.createProductItem(productHistory.name ,
                productHistory.marketName, "", 0, productHistory.categoryId, 0);
        shoppingListRepository.close();
        return result;
    }

}
