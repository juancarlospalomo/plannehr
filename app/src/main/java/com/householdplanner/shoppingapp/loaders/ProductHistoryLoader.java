package com.householdplanner.shoppingapp.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.ProductHistory;
import com.householdplanner.shoppingapp.usecases.UseCaseMyProducts;

import java.util.List;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ProductHistoryLoader extends AsyncTaskLoader<List<ProductHistory>> {

    private Context mContext;
    private List<ProductHistory> mProductList;
    private ProductHistoryObserver mObserver;

    public ProductHistoryLoader(Context context) {
        super(context);
        mContext = context;
        mObserver = new ProductHistoryObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, true, mObserver);
    }

    @Override
    public List<ProductHistory> loadInBackground() {
        UseCaseMyProducts useCaseMyProducts = new UseCaseMyProducts(mContext);
        mProductList = useCaseMyProducts.getMyProductListNotEntered();
        return mProductList;
    }

    @Override
    public void deliverResult(List<ProductHistory> data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<ProductHistory> oldList = mProductList;
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
    public void onCanceled(List<ProductHistory> data) {
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
    protected void onReleaseResources(List<ProductHistory> data) {
        if (data != null) {
            data.clear();
        }
        //Unregister the observer to avoid GC doesn't eliminate the class object
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }


    private final class ProductHistoryObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ProductHistoryObserver(Handler handler) {
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
