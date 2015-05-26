package com.householdplanner.shoppingapp.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Budget;
import com.householdplanner.shoppingapp.usecases.UseCaseBudget;

import java.util.List;

/**
 * Created by JuanCarlos on 26/05/2015.
 */
public class WalletLoader extends AsyncTaskLoader<List<Budget>> {

    private final static String LOG_TAG = WalletLoader.class.getSimpleName();

    private Context mContext;
    private List<Budget> mBudgetList;
    private WalletObserver mObserver;

    public WalletLoader(Context context) {
        super(context);
        mContext = context;
        mObserver = new WalletObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(ShoppingListContract.BudgetEntry.CONTENT_URI, true, mObserver);
    }

    @Override
    public List<Budget> loadInBackground() {
        UseCaseBudget useCaseBudget = new UseCaseBudget(mContext);
        mBudgetList = useCaseBudget.getBudgets();
        return mBudgetList;
    }

    @Override
    public void deliverResult(List<Budget> data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Budget> oldList = mBudgetList;
        mBudgetList = data;
        if (isStarted()) {
            //Deliver result to the client as the loader is in started state
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mBudgetList != null) {
            deliverResult(mBudgetList);
        }
        if (takeContentChanged() || mBudgetList == null) {
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
    public void onCanceled(List<Budget> data) {
        //Attempt to cancel the asynchronous load
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        //ensure the loader has been stopped
        onStopLoading();
        onReleaseResources(mBudgetList);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Budget> data) {
        if (data != null) {
            data.clear();
        }
        //Unregister the observer to avoid GC doesn't eliminate the class object
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    private final class WalletObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public WalletObserver(Handler handler) {
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
