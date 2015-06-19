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
        if (mTypeMarketSet==TypeMarketSet.All) {
            mMarketList = useCaseMarket.getAllMarkets();
        } else {
            mMarketList = useCaseMarket.getMarketsWithSomeProduct();
        }
        return mMarketList;
    }
}
