package com.householdplanner.shoppingapp.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ProductLoader extends AsyncTaskLoader<List<Product>> {

    private Context mContext;
    private List<Product> mProductList;
    private ProductObserver mObserver;

    public ProductLoader(Context context) {
        super(context);
        mContext = context;
        mObserver = new ProductObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(AppPreferences.URI_LIST_TABLE, true, mObserver);
    }

    @Override
    public List<Product> loadInBackground() {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(this.getContext());
        Cursor cursor = shoppingListRepository.getProductsNoCommittedByMarket(null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            mProductList = new ArrayList<Product>();
            while (!cursor.isAfterLast()) {
                Product product = new Product();
                product._id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
                product.name = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                product.marketName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
                product.amount = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT));
                product.unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID));
                product.categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY));
                mProductList.add(product);
                cursor.moveToNext();
            }
        }
        return mProductList;
    }

    @Override
    public void deliverResult(List<Product> data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Product> oldList = mProductList;
        mProductList = data;
        if (isStarted()) {
            //Deliver result to the client as the loader is in started state
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mProductList != null) {
            deliverResult(mProductList);
        }
        if (takeContentChanged() || mProductList == null) {
            //If the content changed (we know this by the Observer) or
            //data have not been loaded yet, start the load
            forceLoad();
        }
    }

    /**
     * Manage the request to stop the loader
     */
    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Product> data) {
        //Attempt to cancel the asynchronous load
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        //ensure the loader has been stopped
        onStopLoading();
        onReleaseResources(mProductList);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Product> data) {
        if (data != null) {
            data.clear();
        }
        //Unregister the observer to avoid GC doesn't eliminate the class object
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    private final class ProductObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ProductObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onContentChanged();
        }
    }
}
