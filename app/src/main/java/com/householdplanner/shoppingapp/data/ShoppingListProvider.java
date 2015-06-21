package com.householdplanner.shoppingapp.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ShoppingListProvider extends ContentProvider {

    private final static String LOG_TAG = ShoppingListProvider.class.getSimpleName();

    //Uri codes when Uri match
    private final static int PRODUCT = 100;
    private final static int PRODUCT_HISTORY = 200;
    private final static int PRODUCT_HISTORY_SUGGEST = 201;
    private final static int BUDGET = 300;
    private final static int MARKET = 400;
    private final static int MARKET_PRODUCT = 401;

    private DatabaseHelper mDatabaseHelper;

    //Init UriMatcher in static block when class is initialized
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT, PRODUCT);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT_HISTORY, PRODUCT_HISTORY);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT_HISTORY + "/" +
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", PRODUCT_HISTORY_SUGGEST);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_BUDGET, BUDGET);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_MARKET, MARKET);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_MARKET + "/" +
                ShoppingListContract.PATH_PRODUCT, MARKET_PRODUCT);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Return the products on the list table
     */
    private Cursor getProductList(String[] projection, String selection, String[] selectionArgs, String orderBy) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //Build join
        sqLiteQueryBuilder.setTables(ShoppingListContract.ProductEntry.TABLE_NAME + " INNER JOIN " +
                ShoppingListContract.ProductHistoryEntry.TABLE_NAME + " ON "
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID + "="
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID
                + " LEFT JOIN " + ShoppingListContract.MarketEntry.TABLE_NAME + " ON "
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "="
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID);

        Cursor cursor = sqLiteQueryBuilder.query(mDatabaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    /**
     * Return the products on the history table
     */
    private Cursor getProductHistoryList(String[] projection, String selection, String[] selectionArgs, String orderBy) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //Build join
        sqLiteQueryBuilder.setTables(ShoppingListContract.ProductHistoryEntry.TABLE_NAME + " LEFT JOIN " +
                ShoppingListContract.ProductEntry.TABLE_NAME + " ON "
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID
                + " LEFT JOIN " + ShoppingListContract.MarketEntry.TABLE_NAME + " ON "
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID + "="
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID);

        return sqLiteQueryBuilder.query(mDatabaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy);
    }

    /**
     * Get the markets that have products on the List
     *
     * @param orderBy order by clause
     * @return Cursor with markets
     */
    public Cursor getMarketsWithProducts(String orderBy) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //Build join
        sqLiteQueryBuilder.setTables(ShoppingListContract.MarketEntry.TABLE_NAME + " INNER JOIN "
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + " ON "
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID + "="
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry.COLUMN_MARKET_ID
                + " INNER JOIN " + ShoppingListContract.ProductEntry.TABLE_NAME + " ON "
                + ShoppingListContract.ProductHistoryEntry.TABLE_NAME + "." + ShoppingListContract.ProductHistoryEntry._ID + "="
                + ShoppingListContract.ProductEntry.TABLE_NAME + "." + ShoppingListContract.ProductEntry.COLUMN_PRODUCT_ID);

        String[] projection = new String[]{ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID,
                ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME,
                ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_COLOR};

        String groupBy = ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry._ID + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + ","
                + ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_COLOR;

        Cursor cursor = sqLiteQueryBuilder.query(mDatabaseHelper.getReadableDatabase(), projection, null, null,
                groupBy, null, orderBy);
        return cursor;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        int code = mUriMatcher.match(uri);
        switch (code) {
            case PRODUCT:
                cursor = getProductList(projection, selection, selectionArgs, sortOrder);
                break;

            case PRODUCT_HISTORY:
                cursor = getProductHistoryList(projection, selection, selectionArgs, sortOrder);
                break;

            case PRODUCT_HISTORY_SUGGEST:
                String pattern = ShoppingListContract.ProductHistoryEntry.getUriSuggestionName(uri);
                ProductHistoryRepository productHistoryRepository = new ProductHistoryRepository(getContext());
                cursor = productHistoryRepository.getProductSuggestions(pattern);
                break;

            case BUDGET:
                cursor = mDatabaseHelper.getReadableDatabase().query(ShoppingListContract.BudgetEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case MARKET:
                cursor = mDatabaseHelper.getReadableDatabase().query(ShoppingListContract.MarketEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case MARKET_PRODUCT:
                cursor = getMarketsWithProducts(sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        //What kind of URI is it?
        int code = mUriMatcher.match(uri);
        switch (code) {
            case PRODUCT:
                return ShoppingListContract.ProductEntry.CONTENT_TYPE;
            case PRODUCT_HISTORY:
                return ShoppingListContract.ProductHistoryEntry.CONTENT_TYPE;
            case PRODUCT_HISTORY_SUGGEST:
                return ShoppingListContract.ProductHistoryEntry.CONTENT_TYPE;
            case BUDGET:
                return ShoppingListContract.BudgetEntry.CONTENT_TYPE;
            case MARKET:
                return ShoppingListContract.MarketEntry.CONTENT_TYPE;
            case MARKET_PRODUCT:
                return ShoppingListContract.MarketEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
