package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;

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
            ShoppingListContract.ProductEntry.COLUMN_MARKET,
            ShoppingListContract.ProductEntry.COLUMN_COMMITTED};

    public UseCaseShoppingList(Context context) {
        mContext = context;
    }

    /**
     * Convert the cursor with products to a List for Product objects
     *
     * @param cursor contains the product cursor
     * @return List of products
     */
    private List<Product> toList(Cursor cursor) {
        List<Product> productList = new ArrayList<Product>();
        if ((cursor != null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                Product product = new Product();
                product._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry._ID));
                product.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_MARKET));
                product.amount = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT));
                product.unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID));
                product.categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_CATEGORY_ID));
                product.committed = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_COMMITTED)) == 1;
                productList.add(product);
                cursor.moveToNext();
            }
        }
        return productList;
    }

    /**
     * Search all the entered product list
     *
     * @return List with all entered product list
     */
    public List<Product> getFullEnteredList() {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, null, null, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get the current products in the basket
     *
     * @return list of products
     */
    public List<Product> getProductsInBasket() {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=?";
        String[] selectionArgs = new String[]{"1"};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, null);
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get the products to include in the shopping list to buy
     *
     * @return List of products
     */
    public List<Product> getShoppingListProducts() {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=?";
        String[] selectionArgs = new String[]{"0"};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, null);
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get the products in a market to include in the shopping list to buy
     *
     * @param market market name
     * @return List of products
     */
    public List<Product> getShoppingListProducts(String market) {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=? AND " +
                ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_MARKET + "=?";
        String[] selectionArgs = new String[]{"0", market};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, null);
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get all products that belongs to a market or they haven't been assigned to any
     * @param market market name
     * @return List of products
     */
    public List<Product> getShoppingListWithMarketAndWithoutMarket(String market) {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=? AND (" +
                ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_MARKET + "=? OR " +
                ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_MARKET + " IS NULL)";
        String[] selectionArgs = new String[]{"0", market};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, null);
        productList = toList(cursor);

        return productList;
    }

    /**
     * Commit a product for moving it to basket
     * @param product product to move
     */
    public void moveToBasket(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.commitProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Get out a product from the cart
     * @param product product to remove
     */
    public void removeFromBasket(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.rollbackProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Remove a product from the list
     * @param product product to remove
     */
    public void removeFromList(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.deletePermanentProductItem(product._id);
        shoppingListRepository.close();
    }

}
