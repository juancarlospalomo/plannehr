package com.householdplanner.shoppingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.ProgressCircle;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.loaders.MarketLoader;
import com.householdplanner.shoppingapp.models.Market;

import java.util.List;

public class MarketListActivity extends BaseActivity implements LoaderCallbacks<List<Market>> {

    private static final int REQUEST_CODE_NEW_MARKET = 1;

    public static final String EXTRA_MARKET_ID = "market_id";
    public static final String EXTRA_MARKET_NAME = "market_name";
    public static final String EXTRA_SHOW_ALL_MARKETS = "show_all";
    public static final String EXTRA_SHOW_CHECK_NO_MARKET = "show_check_no_market";
    public static final int SELECTED_NONE = 0;

    private static final int LOADER_ID = 1;

    //Hold if show label 'all supermarkets' or label 'without supermarket'
    private static boolean mShowAll = false;
    //Hold if show the check to selected the products not assigned to supermarkets
    private static boolean mShowCheckNoMarket = true;
    private MarketListAdapter mAdapter;
    private GridView mGridViewMarkets;
    private AppCompatCheckBox mCheckBoxProductsNotAssigned;
    private View mLayoutMarketNotAssigned;
    private AppCompatTextView mTextViewNone;
    private ProgressCircle mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_list);
        //Get the extra data from intent
        getIntentData();
        //inflate the views
        inflateViews();
        //Configure the activity
        setUpActivity();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Inflate the views on the Activity
     */
    private void inflateViews() {
        mGridViewMarkets = (GridView) findViewById(R.id.gridViewMarkets);
        mCheckBoxProductsNotAssigned = (AppCompatCheckBox) findViewById(R.id.checkboxProductsNotAssigned);
        mLayoutMarketNotAssigned = findViewById(R.id.layoutProductNotSet);
        mTextViewNone = (AppCompatTextView) findViewById(R.id.textImageSelectNoMarket);
    }

    /**
     * Get parameters through the intent data
     */
    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mShowAll = bundle.getBoolean(EXTRA_SHOW_ALL_MARKETS, false);
            mShowCheckNoMarket = bundle.getBoolean(EXTRA_SHOW_CHECK_NO_MARKET, true);
        }
    }

    /**
     * Configure the activity in its initial state
     */
    private void setUpActivity() {
        if (mShowCheckNoMarket) {
            mCheckBoxProductsNotAssigned.setChecked(util.getShowProductsNotSet(MarketListActivity.this));
            mCheckBoxProductsNotAssigned.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    util.setShowProductsNotSet(MarketListActivity.this, ((AppCompatCheckBox) v).isChecked());
                }
            });
        } else {
            mLayoutMarketNotAssigned.setVisibility(View.GONE);
        }

        if (!mShowAll) {
            mTextViewNone.setText(R.string.textProductsAllSupermarkets);
        } else {
            mTextViewNone.setText(R.string.textProductsWithoutSupermarket);
        }
        mTextViewNone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_MARKET_ID, SELECTED_NONE);
                data.putExtra(EXTRA_MARKET_NAME, (String) null);
                MarketListActivity.this.setResult(RESULT_OK, data);
                MarketListActivity.this.finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_MARKET) {
            if (resultCode == RESULT_OK) {
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((!AppGlobalState.getInstance().isShoppingMode(this)) && (!mShowCheckNoMarket)) {
            getMenuInflater().inflate(R.menu.market_list_activity, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.actionMarketAdd:
                Intent intent = new Intent(this, MarketActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEW_MARKET);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public Loader<List<Market>> onCreateLoader(int id, Bundle args) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressCircle(this);
            mProgressDialog.show();
        }
        return new MarketLoader(this, MarketLoader.TypeMarketSet.All);
    }

    @Override
    public void onLoadFinished(Loader<List<Market>> loader, List<Market> data) {
        if (loader.getId() == LOADER_ID) {
            TextView textViewMessage = (TextView) findViewById(R.id.textThereAreNotSupermarkets);
            TextView textImageSelectNoMarkets = (TextView) findViewById(R.id.textImageSelectNoMarket);
            if ((data != null) && (data.size() > 0)) {
                textImageSelectNoMarkets.setVisibility(View.VISIBLE);
                textViewMessage.setVisibility(View.GONE);
            } else {
                textImageSelectNoMarkets.setVisibility(View.GONE);
                textViewMessage.setVisibility(View.VISIBLE);
            }
            mAdapter = new MarketListAdapter(this, data);
            mGridViewMarkets.setAdapter(mAdapter);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Market>> loader) {
        mAdapter = null;
    }

    static class ViewHolder {
        public TextView mTextMarket;
    }

    public class MarketListAdapter extends ArrayAdapter<Market> {

        private List<Market> mMarketDataList;

        public MarketListAdapter(Context context, List<Market> data) {
            super(context, R.layout.market_item);
            mMarketDataList = data;
        }

        public int getCount() {
            if (mMarketDataList != null) {
                return mMarketDataList.size();
            } else {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) MarketListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.market_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mTextMarket = (TextView) convertView.findViewById(R.id.textMarket);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Market market = mMarketDataList.get(position);

            if (market.color != 0) {
                GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_blue);
                drawable.mutate();
                drawable.setColor(market.color);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.mTextMarket.setBackgroundDrawable(drawable);
                } else {
                    viewHolder.mTextMarket.setBackground(drawable);
                }
            }
            viewHolder.mTextMarket.setText(util.capitalize(market.name));
            viewHolder.mTextMarket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_MARKET_ID, market._id);
                    data.putExtra(EXTRA_MARKET_NAME, market.name);
                    MarketListActivity.this.setResult(RESULT_OK, data);
                    MarketListActivity.this.finish();
                }
            });

            return convertView;
        }

    }

}
