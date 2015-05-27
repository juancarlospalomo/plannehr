package com.householdplanner.shoppingapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;

public class SearchProductActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private final static int LOADER_ID = 1;
    private final static String KEY_QUERY_PATTERN = "queryPattern";

    private String mQuery;
    private ListView mListView;
    private ProductsAdapter mAdapter;
    private static boolean mIsSuggestion = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mIsSuggestion = false;
            String query = intent.getStringExtra(SearchManager.QUERY);
            doQuery(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            mIsSuggestion = true;
            String data = intent.getDataString();
            Integer id = Integer.parseInt(data);
            if (id != null) {
                createProduct(id.intValue());
            }
            this.finish();
        }
    }

    private void createProduct(int id) {
        ProductHistoryRepository historyRepository = new ProductHistoryRepository(SearchProductActivity.this);
        ShoppingListRepository listRepository = new ShoppingListRepository(SearchProductActivity.this);
        Cursor cursor = historyRepository.getProduct(id);
        if ((cursor != null) && (cursor.moveToFirst())) {
            String productName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_PRODUCT_NAME));
            String market = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
            int categoryId = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_CATEGORY));
            int sequence = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_SEQUENCE));
            if (listRepository.createProductItem(productName, market, "", 0, categoryId, sequence)) {
                Toast.makeText(SearchProductActivity.this, R.string.textProductsAdded, Toast.LENGTH_SHORT).show();
                getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
            }
        }
        listRepository.close();
        historyRepository.close();
    }

    private void doQuery(String query) {
        String[] fields = new String[]{ProductHistoryStore.COLUMN_PRODUCT_NAME};
        int[] listViewColumns = new int[]{R.id.textProduct};

        try {
            mListView = (ListView) findViewById(R.id.listViewProductsFiltered);
            LoaderManager loaderManager = getSupportLoaderManager();
            Bundle args = new Bundle();
            args.putString(KEY_QUERY_PATTERN, query);
            loaderManager.initLoader(LOADER_ID, args, this);
            mAdapter = new ProductsAdapter(this, R.layout.usual_rowlayout, null,
                    fields, listViewColumns);
            mListView.setAdapter(mAdapter);
        } catch (Exception e) {

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (args != null) {
            mQuery = args.getString(KEY_QUERY_PATTERN);
        }
        return new ProductsCursorLoader(this, mQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_ID) {
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    TextView textMessage = (TextView) findViewById(R.id.textThereAreNotProducts);
                    String message = textMessage.getText().toString();
                    message += " '" + mQuery + "'";
                    textMessage.setText(message);
                    textMessage.setVisibility(View.VISIBLE);
                }
            }
            mAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    static class ViewHolder {
        public TextView textName;
        public AppCompatCheckBox imageCheck;
    }

    public class ProductsAdapter extends SimpleCursorAdapter {

        Context mContext;
        Cursor mCursor;

        public ProductsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
            mContext = context;
            mCursor = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final int pos = position;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.usual_rowlayout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textName = (TextView) convertView.findViewById(R.id.textProduct);
                viewHolder.imageCheck = (AppCompatCheckBox) convertView.findViewById(R.id.imageSecondaryActionIcon);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.imageCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.imageCheck.setChecked(true);
                    mCursor.moveToPosition(pos);
                    int id = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_ID));
                    ProductHistoryRepository historyRepository = new ProductHistoryRepository(SearchProductActivity.this);
                    ShoppingListRepository listRepository = new ShoppingListRepository(SearchProductActivity.this);
                    Cursor cursor = historyRepository.getProduct(id);
                    if ((cursor != null) && (cursor.moveToFirst())) {
                        String productName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_PRODUCT_NAME));
                        String market = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
                        int categoryId = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_CATEGORY));
                        int sequence = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_SEQUENCE));
                        if (listRepository.createProductItem(productName, market, "", 0, categoryId, sequence)) {
                            Toast.makeText(SearchProductActivity.this, R.string.textProductsAdded, Toast.LENGTH_SHORT).show();
                            getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                            getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getSupportLoaderManager().restartLoader(LOADER_ID, null, SearchProductActivity.this);
                                }
                            }, 100);
                        }
                    }
                    listRepository.close();
                    historyRepository.close();
                }
            });
            mCursor.moveToPosition(position);
            viewHolder.imageCheck.setChecked(false);
            viewHolder.textName.setText(util.getCompleteHistoryRow(mContext, mCursor, false));
            return convertView;
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            mCursor = c;
            return super.swapCursor(c);
        }
    }

    private static class ProductsCursorLoader extends CursorLoader {

        // We hold a reference to the Loader's data here.
        private Cursor mCursor = null;
        ProductHistoryRepository mHistoryRepository = null;
        private String mPattern;

        public ProductsCursorLoader(Context context, String pattern) {
            // Loaders may be used across multiple Activitys (assuming they aren't
            // bound to the LoaderManager), so NEVER hold a reference to the context
            // directly. Doing so will cause you to leak an entire Activity's context.
            // The superclass constructor will store a reference to the Application
            // Context instead, and can be retrieved with a call to getContext().
            super(context);
            mPattern = pattern;
        }

        /****************************************************/
        /** (1) A task that performs the asynchronous load **/
        /**
         * ************************************************
         */
        @Override
        public Cursor loadInBackground() {
            // This method is called on a background thread and should generate a
            // new set of data to be delivered back to the client.
            mHistoryRepository = new ProductHistoryRepository(this.getContext());
            if (mIsSuggestion) {
                mCursor = mHistoryRepository.getProductSuggestions(mPattern);
            } else {
                mCursor = mHistoryRepository.GetProductFilteredByName(mPattern);
            }
            return mCursor;
        }

        /********************************************************/
        /** (2) Deliver the results to the registered listener **/
        /**
         * ****************************************************
         */
        @Override
        public void deliverResult(Cursor cursor) {
            if (isReset()) {
                // The Loader has been reset; ignore the result and invalidate the data.
                if (cursor != null) {
                    ReleaseResources(cursor);
                }
                return;
            }
            // Hold a reference to the old data so it doesn't get garbage collected.
            // We must protect it until the new data has been delivered.
            Cursor oldCursor = mCursor;
            mCursor = cursor;

            if (isStarted()) {
                // If the Loader is in a started state, deliver the results to the
                // client. The superclass method does this for us.
                super.deliverResult(cursor);
            }
            // Invalidate the old data as we don't need it any more
            if (oldCursor != null && oldCursor != cursor) {
                ReleaseResources(oldCursor);
            }
        }

        /*********************************************************/
        /** (3) Implement the Loader�s state-dependent behavior **/
        /**
         * *****************************************************
         */

        @Override
        protected void onStartLoading() {
            if (mCursor != null) {
                // Deliver any previously loaded data immediately.
                deliverResult(mCursor);
            }
            if (takeContentChanged() || mCursor == null) {
                // When the observer detects a change, it should call onContentChanged()
                // on the Loader, which will cause the next call to takeContentChanged()
                // to return true. If this is ever the case (or if the current data is
                // null), we force a new load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // The Loader is in a stopped state, so we should attempt to cancel the
            // current load (if there is one).
            cancelLoad();

            // Note that we leave the observer as is. Loaders in a stopped state
            // should still monitor the data source for changes so that the Loader
            // will know to force a new load if it is ever started again.
        }

        @Override
        public void onCanceled(Cursor cursor) {
            // Attempt to cancel the current asynchronous load.
            super.onCanceled(mCursor);

            // The load has been canceled, so we should release the resources
            // associated with 'data'.
            ReleaseResources(cursor);
        }

        @Override
        protected void onReset() {
            // Ensure the loader has been stopped.
            onStopLoading();
            // At this point we can release the resources associated with 'mData'.
            if (mCursor != null) {
                ReleaseResources(mCursor);
                mCursor = null;
            }
        }

        private void ReleaseResources(Cursor cursor) {
            if (cursor != null) {
                cursor.close();
                mHistoryRepository.close();
            }
        }
    }

}
