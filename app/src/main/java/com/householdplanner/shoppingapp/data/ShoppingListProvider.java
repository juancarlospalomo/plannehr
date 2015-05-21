package com.householdplanner.shoppingapp.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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

    private DatabaseHelper mDatabaseHelper;

    //Init UriMatcher in static block when class is initialized
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT, PRODUCT);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT_HISTORY, PRODUCT_HISTORY);
        mUriMatcher.addURI(ShoppingListContract.CONTENT_AUTHORITY, ShoppingListContract.PATH_PRODUCT_HISTORY + "/" +
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", PRODUCT_HISTORY_SUGGEST);
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
        Cursor cursor = mDatabaseHelper.getReadableDatabase().query(ShoppingListContract.ProductEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    /**
     * Return the products on the history table
     */
    private Cursor getProductHistoryList(String[] projection, String selection, String[] selectionArgs, String orderBy) {
        Cursor cursor = mDatabaseHelper.getReadableDatabase().query(ShoppingListContract.ProductHistoryEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, orderBy);
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

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
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
