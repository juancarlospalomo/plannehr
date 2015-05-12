package com.householdplanner.shoppingapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.ProgressCircle;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.stores.MarketStore;

public class MarketListActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	private static final int NEW_MARKET_REQUEST_CODE = 1;
	
	public static final String EXTRA_MARKET_ID = "MarketId";
	public static final String EXTRA_MARKET_NAME = "MarketName";
	public static final String IN_EXTRA_SHOW_ALL = "ShowAll";
	public static final String IN_EXTRA_SHOW_CHECK_NO_MARKET = "ShowCheckNoMarket";
	public static final int SELECTED_NONE = 0;
	
	private static final int LOADER_ID = 1; 

	private boolean mStarted = false;
	private static boolean mShowAll = false;
	private static boolean mShowCheckNoMarket = true;
	private MarketListAdapter mAdapter;
	private ProgressCircle mProgressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle!=null) {
			mShowAll = bundle.getBoolean(IN_EXTRA_SHOW_ALL, false);
			mShowCheckNoMarket = bundle.getBoolean(IN_EXTRA_SHOW_CHECK_NO_MARKET, true);
		}
		setContentView(R.layout.activity_market_list);
		setUpActivity();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mStarted) {
			loadMarkets();
		} else {
			if (getSupportLoaderManager().getLoader(LOADER_ID)!=null) {
				getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
			} else {
				loadMarkets();
			}
		}
		mStarted = true;
	}
	
	private void setUpActivity() {
		if (mShowCheckNoMarket) {
			ImageView view = (ImageView) findViewById(R.id.imageCheckShowProductNotSet);
			if (util.getShowProductsNotSet(MarketListActivity.this)) {
				view.setImageResource(R.drawable.ic_check_on);
			} else {
				view.setImageResource(R.drawable.ic_check_off);
			}
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean productsNotSet = util.getShowProductsNotSet(MarketListActivity.this);
					if (productsNotSet) {
						productsNotSet = false;
						((ImageView)v).setImageResource(R.drawable.ic_check_off);
					} else {
						productsNotSet = true;
						((ImageView)v).setImageResource(R.drawable.ic_check_on);
					}
					util.setShowProductsNotSet(MarketListActivity.this, productsNotSet);
				}
			});
			TextView textCheckNoMarket = (TextView)findViewById(R.id.textImageCheckShowProductNotSet);
		} else {
			View layout = findViewById(R.id.layoutProductNotSet);
			layout.setVisibility(View.GONE);
		}
		
		TextView textViewNone = (TextView) findViewById(R.id.textImageSelectNoMarket);
		if (!mShowAll) {
			textViewNone.setText(R.string.textProductsAllSupermarkets);
		} else {
			textViewNone.setText(R.string.textProductsWithoutSupermarket);
		}
		textViewNone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra(EXTRA_MARKET_ID, SELECTED_NONE);
				data.putExtra(EXTRA_MARKET_NAME, (String)null);
				MarketListActivity.this.setResult(RESULT_OK, data);
				MarketListActivity.this.finish();
			}
		});
	}
	
	private void loadMarkets() {
        String[] fields = new String[] { MarketStore.COLUMN_MARKET_NAME };
        int[] listViewColumns = new int[] { android.R.id.text1 };
                
        try {
        	GridView gridView = (GridView) findViewById(R.id.gridViewMarkets);
        	LoaderManager loaderManager = getSupportLoaderManager();
    		loaderManager.initLoader(LOADER_ID, null, this);
			mAdapter = new MarketListAdapter(R.layout.market_item, null, fields, listViewColumns);
			gridView.setAdapter(mAdapter);
        } catch (Exception e) {
        }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_MARKET_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if ((!AppGlobalState.getInstance().isShoppingMode(this)) && (!mShowCheckNoMarket))  {
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
				startActivityForResult(intent, NEW_MARKET_REQUEST_CODE);
				return true;
				
			default: 
				return super.onOptionsItemSelected(item);
		}
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (mProgressDialog==null) {
			mProgressDialog = new ProgressCircle(this);
			mProgressDialog.show();
		}
		return new MarketListCursorLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId()==LOADER_ID) {
			TextView textViewMessage = (TextView) findViewById(R.id.textThereAreNotSupermarkets);
			TextView textImageSelectNoMarkets = (TextView) findViewById(R.id.textImageSelectNoMarket);
			if ((cursor!=null) && (cursor.getCount()>0)) {
				textImageSelectNoMarkets.setVisibility(View.VISIBLE);
				textViewMessage.setVisibility(View.GONE);
			} else {
				textImageSelectNoMarkets.setVisibility(View.GONE);
				textViewMessage.setVisibility(View.VISIBLE);
			}
			mAdapter.swapCursor(cursor);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	static class ViewHolder {
		public TextView text;
	}
	
    public class MarketListAdapter extends SimpleCursorAdapter {
    	
    	Cursor mCursor;
    	
		public MarketListAdapter(int layout, Cursor c, String[] from, int[] to) {
    		super(MarketListActivity.this, layout, c, from, to, 0);
    		mCursor = c;
    	}
		
		public int getCount() {
			if (mCursor!=null) return mCursor.getCount();
			else return 0;
		}
		
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		final ViewHolder viewHolder;
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) MarketListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.market_item, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.text = (TextView) convertView.findViewById(R.id.textMarket);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}
    		mCursor.moveToPosition(position);
    		final String marketName = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
    		
    		final int pos = position;
    		
			String color = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_COLOR));
			if (color!=null) {
				GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_blue);
				drawable.mutate();
				drawable.setColor(Integer.parseInt(color));
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					viewHolder.text.setBackgroundDrawable(drawable);
				} else {
					viewHolder.text.setBackground(drawable);
				}
			}
    		viewHolder.text.setText(util.capitalize(marketName));
    		viewHolder.text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCursor.moveToPosition(pos);
					int marketId = mCursor.getInt(mCursor.getColumnIndex(MarketStore.COLUMN_MARKET_ID));
					String marketName = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
					Intent data = new Intent();
					data.putExtra(EXTRA_MARKET_ID, marketId);
					data.putExtra(EXTRA_MARKET_NAME, marketName);
					MarketListActivity.this.setResult(RESULT_OK, data);
					MarketListActivity.this.finish();
				}
			});
    		
    		return convertView;
    	}
    	
    	@Override
    	public Cursor swapCursor(Cursor c) {
    		mCursor = c;
    		return super.swapCursor(c);
    	}
    	
    }

	private static class MarketListCursorLoader extends AsyncTaskLoader<Cursor> {
		
		// We hold a reference to the Loader's data here.
		private Cursor mCursor = null;
		private MarketRepository mMarketRepository;
		
		public MarketListCursorLoader(Context context) {
			// Loaders may be used across multiple Activitys (assuming they aren't
		    // bound to the LoaderManager), so NEVER hold a reference to the context
		    // directly. Doing so will cause you to leak an entire Activity's context.
		    // The superclass constructor will store a reference to the Application
		    // Context instead, and can be retrieved with a call to getContext().
			super(context);
		}

		/****************************************************/
		/** (1) A task that performs the asynchronous load **/
		/****************************************************/
	    @Override
		public Cursor loadInBackground() {
		    // This method is called on a background thread and should generate a
		    // new set of data to be delivered back to the client.
	    	mMarketRepository = new MarketRepository(this.getContext());
	    	if (!mShowAll) {
	    		mCursor = mMarketRepository.getMarketsWithProducts();
	    	} else {
	    		mCursor = mMarketRepository.getAllMarkets();
	    	}
			return mCursor;
		}
		
		/********************************************************/
		/** (2) Deliver the results to the registered listener **/
		/********************************************************/
		@Override
		public void deliverResult(Cursor cursor) {
			if (isReset()) {
				// The Loader has been reset; ignore the result and invalidate the data.
				if (cursor!=null) {
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
			if (oldCursor!=null && oldCursor!=cursor) {
				ReleaseResources(oldCursor);
			}
		}

	    /*********************************************************/
	    /** (3) Implement the Loader�s state-dependent behavior **/
	    /*********************************************************/

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
			cursor.close();
			mMarketRepository.close();
		}
	}

}
