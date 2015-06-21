package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.models.ProductHistory;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
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
            ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID,
            ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " AS " + ShoppingListContract.ProductHistoryEntry.ALIAS_MARKET_NAME};

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
                product.marketId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_MARKET_NAME));
                productList.add(product);
                cursor.moveToNext();
            }
        }
        return productList;
    }

    /**
     * Get a product from catalog by its id
     *
     * @param id product id
     * @return ProductHistory or null
     */
    public ProductHistory getProduct(int id) {
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(id)};

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductHistoryEntry.CONTENT_URI,
                mProjection, selection, args, null);

        if (cursor != null & cursor.moveToFirst()) {
            ProductHistory productHistory = new ProductHistory();
            productHistory._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_ID));
            productHistory.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME));
            productHistory.marketId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID));
            productHistory.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_MARKET_NAME));
            return productHistory;
        } else {
            return null;
        }
    }

    /**
     * Get a product from its name in a market
     *
     * @param name     product name
     * @param marketId market id
     * @return ProductHistory object or null
     */
    public ProductHistory getProduct(String name, int marketId) {
        String[] args;
        String selection = ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "=?";
        if (marketId == 0) {
            args = new String[]{name};
        } else {
            selection += " AND " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=?";
            args = new String[]{name, String.valueOf(marketId)};
        }
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductHistoryEntry.CONTENT_URI,
                mProjection, selection, args, null);

        if (cursor != null & cursor.moveToFirst()) {
            ProductHistory productHistory = new ProductHistory();
            productHistory._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_ID));
            productHistory.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME));
            productHistory.marketId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID));
            productHistory.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.ALIAS_MARKET_NAME));
            return productHistory;
        } else {
            return null;
        }
    }

    /**
     * Copy a product from the my products list (history) to the current shopping list
     *
     * @param productHistory product to copy to the shopping list
     * @return true if it was created successfully
     */
    public boolean copyToShoppingList(ProductHistory productHistory) {
        boolean result = false;
        //New product object
        Product product = new Product();
        product.productId = productHistory._id;
        product.name = productHistory.name;
        product.committed = false;
        //Save it
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        result = shoppingListRepository.createProductItem(product);
        shoppingListRepository.close();

        return result;
    }

    /**
     * Set a new market for a product in the catalog
     *
     * @param id       product id
     * @param marketId market id
     */
    public void moveToSupermarket(int id, int marketId) {
        if (marketId != 0) {
            UseCaseMarket useCaseMarket = new UseCaseMarket(mContext);
            Market market = useCaseMarket.getMarket(marketId);
            if (market != null) {
                ProductHistory productHistory = new ProductHistory();
                productHistory._id = id;
                productHistory.marketId = marketId;
                productHistory.marketName = market.name;
                ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
                historyRepository.updateProductItem(productHistory);
                historyRepository.close();
            }
        }
    }


}
