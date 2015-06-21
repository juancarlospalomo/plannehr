package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.exceptions.MarketException;
import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.stores.MarketStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 19/06/2015.
 */
public class UseCaseMarket {

    private Context mContext;
    //Fields for projection
    private static String[] mProjection = new String[]{ShoppingListContract.MarketEntry._ID,
            ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME,
            ShoppingListContract.MarketEntry.COLUMN_COLOR};

    public UseCaseMarket(Context context) {
        mContext = context;
    }

    /**
     * Convert the cursor with Markets to a List with Markets objects
     *
     * @param cursor contains the market cursor
     * @return List of markets
     */
    private List<Market> toList(Cursor cursor) {
        List<Market> marketList = new ArrayList<Market>();
        if ((cursor != null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                Market market = new Market();
                market._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry._ID));
                market.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME));
                market.color = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_COLOR));
                marketList.add(market);
                cursor.moveToNext();
            }
        }
        return marketList;
    }

    /**
     * Search all the entered product list
     *
     * @return List with all entered product list
     */
    public List<Market> getAllMarkets() {
        //Variable for returning product list
        List<Market> marketList = new ArrayList<Market>();

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.MarketEntry.CONTENT_URI,
                mProjection, ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "<>'" + MarketStore.VIRTUAL_MARKET_NAME + "'",
                null, ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " ASC");
        marketList = toList(cursor);

        return marketList;
    }

    /**
     * Get the Market list with some product on the List
     *
     * @return List of Markets
     */
    public List<Market> getMarketsWithSomeProduct() {
        List<Market> marketList = new ArrayList<>();

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.MarketEntry.setUriProductMarket(),
                null, null, null,
                ShoppingListContract.MarketEntry.TABLE_NAME + "." + ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + " ASC");
        marketList = toList(cursor);

        return marketList;
    }

    /**
     * Search a Market by its id
     *
     * @param id market id
     * @return Market object or null
     */
    public Market getMarket(int id) {
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.MarketEntry.CONTENT_URI,
                mProjection, ShoppingListContract.MarketEntry._ID + "=?",
                new String[]{String.valueOf(id)}, null);
        if (cursor != null & cursor.moveToFirst()) {
            Market market = new Market();
            market._id = id;
            market.name = cursor.getString(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME));
            market.color = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_COLOR));
            return market;
        } else {
            return null;
        }
    }

    /**
     * Search a market by its name
     *
     * @param name market name
     * @return market object if it already exists
     */
    public Market getMarket(String name) {
        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.MarketEntry.CONTENT_URI,
                mProjection, ShoppingListContract.MarketEntry.COLUMN_MARKET_NAME + "=?",
                new String[]{name}, null);
        if (cursor != null & cursor.moveToFirst()) {
            Market market = new Market();
            market._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry._ID));
            market.name = name;
            market.color = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.MarketEntry.COLUMN_COLOR));
            return market;
        } else {
            return null;
        }
    }

    /**
     * Check if there are at least one product belonging to one supermarket on the List
     *
     * @return true if there are at least one product with supermarket
     */
    public boolean existProductWithSupermarketOnList() {
        List<Market> marketList = getMarketsWithSomeProduct();
        if (marketList != null & marketList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Change the name to a supermarket
     *
     * @param marketId      market identifier
     * @param newMarketName new market name
     * @return true if it was changed
     */
    public boolean renameMarket(int marketId, String newMarketName) {
        boolean result = false;
        MarketRepository marketRepository = new MarketRepository(mContext);
        if (marketRepository.renameMarket(marketId, newMarketName)) {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
            historyRepository.updateSupermarket(marketId, marketId, newMarketName);
            historyRepository.close();
            result = true;
        }
        marketRepository.close();
        return result;
    }

    /**
     * Create a new market
     *
     * @param market market object
     * @return
     */
    public boolean createMarket(Market market) throws MarketException {
        Market currentMarket = getMarket(market.name);
        if (currentMarket == null) {
            MarketRepository marketRepository = new MarketRepository(mContext);
            boolean result = marketRepository.insert(market);
            marketRepository.close();
            return result;
        } else {
            throw new MarketException();
        }
    }

    /**
     * Delete a market and remove the assignment for the product that owns it
     *
     * @param marketId market id
     * @return true if it was deleted
     */
    public boolean deleteMarket(int marketId) {
        boolean result;
        MarketRepository marketRepository = new MarketRepository(mContext);
        result = marketRepository.delete(marketId);
        if (result) {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
            historyRepository.unSetMarket(marketId);
            historyRepository.close();
        }
        marketRepository.close();
        return result;
    }

}
