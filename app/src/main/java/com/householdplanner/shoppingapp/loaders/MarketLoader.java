package com.householdplanner.shoppingapp.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.usecases.UseCaseMarket;

import java.util.List;

/**
 * Created by JuanCarlos on 19/06/2015.
 */
public class MarketLoader extends AsyncTaskLoader<List<Market>> {

    public enum TypeMarketSet {
        All,
        OnlyWithProducts
    }

    private Context mContext;
    //Set of markets to return
    private TypeMarketSet mTypeMarketSet;
    //List containing the markets
    private List<Market> mMarketList;

    public MarketLoader(Context context, TypeMarketSet typeMarketSet) {
        super(context);
        mContext = context;
        mTypeMarketSet = typeMarketSet;
    }

    @Override
    public List<Market> loadInBackground() {
        // This method is called on a background thread and should generate a
        // new set of data to be delivered back to the client.
        UseCaseMarket useCaseMarket = new UseCaseMarket(mContext);
        if (mTypeMarketSet == TypeMarketSet.All) {
            mMarketList = useCaseMarket.getAllMarkets();
        } else {
            mMarketList = useCaseMarket.getMarketsWithSomeProduct();
        }
        return mMarketList;
    }

    @Override
    public void deliverResult(List<Market> data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Market> oldMarketList = mMarketList;
        mMarketList = data;
        if (isStarted()) {
            //Deliver result to the client as the loader is in started state
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mMarketList != null) {
            deliverResult(mMarketList);
        }
        if (takeContentChanged() || mMarketList == null) {
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
    public void onCanceled(List<Market> data) {
        //Attempt to cancel the asynchronous load
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        //ensure the loader has been stopped
        onStopLoading();
        onReleaseResources(mMarketList);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Market> data) {
        if (data != null) {
            data.clear();
        }
    }

}
