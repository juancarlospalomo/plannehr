package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.exceptions.ProductException;
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
public class UseCaseShoppingList {

    private Context mContext;
    //Fields for projection
    private static String[] mProjection = new String[]{ShoppingListContract.ProductEntry._ID,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID,
            ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME,
            ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT,
            ShoppingListContract.ProductEntry.COLUMN_UNIT_ID,
            ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_NAME,
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
                product.productId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID));
                product.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_NAME));
                product.amount = cursor.getString(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_PRODUCT_AMOUNT));
                product.unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.ProductEntry.COLUMN_UNIT_ID));
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
                mProjection, null, null, ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + " ASC");
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
                mProjection, selection, selectionArgs, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");
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
                mProjection, selection, selectionArgs, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get the products in a market to include in the shopping list to buy
     *
     * @param marketId market identifier
     * @return List of products
     */
    public List<Product> getShoppingListProducts(int marketId) {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=? AND " +
                ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=?";
        String[] selectionArgs = new String[]{"0", String.valueOf(marketId)};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");
        productList = toList(cursor);

        return productList;
    }

    /**
     * Get all products that belongs to a market or they haven't been assigned to any
     *
     * @param marketId market identifier
     * @return List of products
     */
    public List<Product> getShoppingListWithMarketAndWithoutMarket(int marketId) {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=? AND (" +
                ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "=? OR " +
                ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + " IS NULL)";
        String[] selectionArgs = new String[]{"0", String.valueOf(marketId)};
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, ShoppingListContract.ProductEntry.COLUMN_PRODUCT_NAME + " ASC");
        productList = toList(cursor);

        return productList;
    }

    /**
     * Check if a product, based on its product name and market name is on the list
     *
     * @param productName product name
     * @param marketName  market name
     * @return true if the product is on the list
     */
    public boolean isProductInList(String productName, String marketName) {
        //Variable for returning product list
        List<Product> productList = new ArrayList<Product>();
        String selection = ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_PRODUCT_NAME + "=? AND "
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_COMMITTED + "=?";
        String[] selectionArgs = new String[]{productName, "0"};
        if (marketName != null) {
            selection += " AND " + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_NAME + "=?";
            selectionArgs = new String[]{productName, "0", marketName};
        }
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.ProductEntry.CONTENT_URI,
                mProjection, selection, selectionArgs, null);
        if ((cursor != null) & (cursor.getCount() > 0)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Create a product on the list and if it doesn't exist on the catalog
     * It will create there too
     *
     * @param product item
     */
    public void createProduct(Product product, boolean allowDuplicates) throws ProductException {
        //First, we´ll check if the product is already on the list
        if (!allowDuplicates & isProductInList(product.name, product.marketName)) {
            //Raise exception
            throw new ProductException();
        } else {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
            ShoppingListRepository listRepository = new ShoppingListRepository(mContext);

            int productId = historyRepository.getProductId(product.name, product.marketId);
            if (productId != 0) {
                //Product already exists in the catalog
                //Then, we have only to add it to the list
                product.productId = productId;
                listRepository.createProductItem(product);
            } else {
                //Product doesn't exit in the catalog yet
                //Then, we have to add it to the catalog and later to the list
                ProductHistory productHistory = new ProductHistory();
                productHistory.name = product.name;
                productHistory.marketName = product.marketName;
                //Get marketId by the market name
                UseCaseMarket useCaseMarket = new UseCaseMarket(mContext);
                Market market = useCaseMarket.getMarket(productHistory.marketName);
                productHistory.marketId = market._id;
                product.productId = historyRepository.createProductItem(productHistory);
                listRepository.createProductItem(product);
            }
            historyRepository.close();
            listRepository.close();
        }
    }

    /**
     * Update an item on the List and its item linked in the Catalog
     *
     * @param product product item
     */
    public void updateProduct(Product product) {
        ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
        ShoppingListRepository listRepository = new ShoppingListRepository(mContext);

        UseCaseMyProducts useCaseMyProducts = new UseCaseMyProducts(mContext);
        ProductHistory productHistory = useCaseMyProducts.getProduct(product.name, product.marketId);

        if (productHistory != null) {
            if (productHistory.marketName != product.marketName) {
                productHistory.marketName = product.marketName;
                UseCaseMarket useCaseMarket = new UseCaseMarket(mContext);
                Market market = useCaseMarket.getMarket(productHistory.name);
                productHistory.marketId = market._id;
            }
            if (historyRepository.updateProductItem(productHistory)) {
                listRepository.updateProductItem(product);
            }
        }
        historyRepository.close();
        listRepository.close();
    }

    /**
     * Commit a product for moving it to basket
     *
     * @param product product to move
     */
    public void moveToBasket(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.commitProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Get out a product from the cart
     *
     * @param product product to remove
     */
    public void removeFromBasket(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.rollbackProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Remove a product from the cart in shopping mode, restoring it on the shopping list
     *
     * @param product product to remove
     */
    public void backToShoppingList(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.rollbackProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Remove a product from the shopping list, sending it to cart again
     *
     * @param product
     */
    public void backToCart(Product product) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.commitProduct(product._id);
        shoppingListRepository.close();
    }

    /**
     * Remove from list the last product entered with a name
     *
     * @param productId product identifier (FK)
     */
    public void removeFromList(int productId) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.deleteProductByProductId(productId);
        shoppingListRepository.close();
    }

    /**
     * Erase all existing products in the cart
     */
    public void clearCart() {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
        shoppingListRepository.deleteCommittedProducts();
        shoppingListRepository.close();
    }

}
