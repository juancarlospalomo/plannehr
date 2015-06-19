package com.householdplanner.shoppingapp.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

import java.util.List;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class ProductLoader extends AsyncTaskLoader<List<Product>> {

    public enum TypeProducts {
        All,
        InShoppingList,
        InShoppingListBySupermarket,
        InShoppingListWithSupermarketAndWithoutAny,
        InBasket
    }

    private Context mContext;
    //Products set to get
    private TypeProducts mTypeProducts;
    //Market identifier
    private int mMarketId;
    private List<Product> mProductList;
    private ProductObserver mObserver;

    public ProductLoader(Context context, TypeProducts typeProducts) {
        super(context);
        mContext = context;
        mTypeProducts = typeProducts;
        mObserver = new ProductObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(ShoppingListContract.ProductEntry.CONTENT_URI, true, mObserver);
    }

    /**
     * Constructor to receive the current market
     *
     * @param context
     * @param typeProducts product set
     * @param marketId     market identifier
     */
    public ProductLoader(Context context, TypeProducts typeProducts, int marketId) {
        this(context, typeProducts);
        mMarketId = marketId;
    }

    @Override
    public List<Product> loadInBackground() {
        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(mContext);
        if (mTypeProducts == TypeProducts.All) {
            mProductList = useCaseShoppingList.getFullEnteredList();
        } else if (mTypeProducts == TypeProducts.InShoppingList) {
            mProductList = useCaseShoppingList.getShoppingListProducts();
        } else if (mTypeProducts == TypeProducts.InShoppingListBySupermarket) {
            mProductList = useCaseShoppingList.getShoppingListProducts(mMarketId);
        } else if (mTypeProducts == TypeProducts.InShoppingListWithSupermarketAndWithoutAny) {
            mProductList = useCaseShoppingList.getShoppingListWithMarketAndWithoutMarket(mMarketId);
        } else if (mTypeProducts == TypeProducts.InBasket) {
            mProductList = useCaseShoppingList.getProductsInBasket();
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
