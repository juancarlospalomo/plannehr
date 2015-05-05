package com.householdplanner.shoppingapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.MarketCategoryStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;

public class FragmentShoppingList extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>,
					OnFragmentProgress {

	private static final int LOADER_ID = 1;
	
	private static boolean mHasRecalculate = false;
	private int mFirstCategoryOrder = 0;
	private static int mMarketId = 0;
	private static String mMarketName;
	private ListView mListView;
	private ShoppingListAdapter mAdapter;
	private OnLoadData mCallback = null;
	
	public FragmentShoppingList() {
		super();
	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_shopping_list, container, false);
	}
	
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mHasRecalculate = prefs.getBoolean("prefRecalculateOrder", false);
		mMarketId = AppGlobalState.getInstance().getMarket(getActivity());
		mMarketName = AppGlobalState.getInstance().getMarketName(getActivity());
		LoadProductList();
	}
	
	private void LoadProductList() {
        String[] fields = new String[] { ShoppingListStore.COLUMN_PRODUCT_NAME };
        int[] listViewColumns = new int[] { R.id.label };
                
        try {
        	mListView = (ListView) getView().findViewById(R.id.listview01);
        	LoaderManager loaderManager = getLoaderManager();
    		loaderManager.initLoader(LOADER_ID, null, this);
        	mAdapter = new ShoppingListAdapter(this.getActivity(), R.layout.rowlayout, null, 
        		fields, listViewColumns);
        	mListView.setAdapter(mAdapter);
        } catch (Exception e) {

        }
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (mCallback!=null) mCallback.onLoadStart();
		return new ShoppingListCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
		if (mCallback!=null) {
			int items = 0;
			if (cursor!=null) items = cursor.getCount();
			mCallback.onLoadFinish(items, null);
		}
		switch(loader.getId()) {
			case LOADER_ID:
		        // The asynchronous load is complete and the data
		        // is now available for use. Only now can we associate
		        // the queried Cursor with the SimpleCursorAdapter.
				mAdapter.swapCursor(cursor);
				if (cursor!=null) {
					if (cursor.getCount()>0){
						if ((mHasRecalculate) && (AppGlobalState.getInstance().isShoppingMode(getActivity()))) {
							if (cursor.moveToFirst()) {
								mFirstCategoryOrder = cursor.getInt(cursor.getColumnIndex(MarketCategoryStore.COLUMN_CATEGORY_ORDER));
							}
						}
					}
				} 
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
	    // For whatever reason, the Loader's data is now unavailable.
	    // Remove any references to the old data by replacing it with
	    // a null Cursor.
		mAdapter.swapCursor(null);
	}
	
	@Override
	public void setOnLoadData(OnLoadData callback) {
		mCallback = callback;
	}
	
	static class ViewHolder {
		public TextView text;
		public ImageView image;
	}
	
    public class ShoppingListAdapter extends SimpleCursorAdapter {
    	
    	Context mContext;
    	Cursor mCursor;
    	
		public ShoppingListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
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
    			convertView = inflater.inflate(R.layout.rowlayout, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.text = (TextView) convertView.findViewById(R.id.label);
    			viewHolder.image = (ImageView) convertView.findViewById(R.id.imageRightArrow);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}

			viewHolder.image.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
		        	  ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext);
		        	  mCursor.moveToPosition(pos);
		        	  int id = mCursor.getInt(mCursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
		        	  String productName = mCursor.getString(mCursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
		        	  if (mHasRecalculate) {
		            	  int currentCategoryOrder = mCursor.getInt(mCursor.getColumnIndex(MarketCategoryStore.COLUMN_CATEGORY_ORDER));
		            	  int currentCategoryId = mCursor.getInt(mCursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY));
		            	  if ((currentCategoryOrder!=mFirstCategoryOrder) && (currentCategoryId!=0)) {
		            		  /*
		            		   * from = currentCategoryOrder, to = _firstCategoryOrder
		            		   */
		            		  MarketRepository marketRepository = new MarketRepository(mContext);
		            		  marketRepository.recalculateOrderMarketCategory(currentCategoryOrder, mFirstCategoryOrder, 
		            				  currentCategoryId, mMarketId);
		            		  marketRepository.close();
		            	  }
		        	  }
		        	  shoppingListRepository.commitProduct(id);
		        	  shoppingListRepository.close();
		        	  getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
		        	  Toast.makeText(mContext, getResources().getString(R.string.textMovedToTicket) + ": " + productName, Toast.LENGTH_SHORT).show();
				}
			});
			
    		mCursor.moveToPosition(position);
    		viewHolder.text.setTypeface(font.getListItemFont(getActivity()));
    		viewHolder.text.setText(util.getCompleteProductRow(mContext, mCursor, false));
    		return convertView;
    	}
    	
    	@Override
    	public Cursor swapCursor(Cursor c) {
    		mCursor = c;
    		return super.swapCursor(c);
    	}

    }
    
	private static class ShoppingListCursorLoader extends CursorLoader {
		
		// We hold a reference to the Loader's data here.
		private Cursor mCursor = null;
		ShoppingListRepository mShoppingListRepository = null;
		final ForceLoadContentObserver mObserver;
		
		public ShoppingListCursorLoader(Context context) {
			// Loaders may be used across multiple Activitys (assuming they aren't
		    // bound to the LoaderManager), so NEVER hold a reference to the context
		    // directly. Doing so will cause you to leak an entire Activity's context.
		    // The superclass constructor will store a reference to the Application
		    // Context instead, and can be retrieved with a call to getContext().
			super(context);
			this.mObserver = new ForceLoadContentObserver();
		}
		
		/****************************************************/
		/** (1) A task that performs the asynchronous load **/
		/****************************************************/
	    @Override
		public Cursor loadInBackground() {
		    // This method is called on a background thread and should generate a
		    // new set of data to be delivered back to the client.
	        mShoppingListRepository = new ShoppingListRepository(this.getContext());
	        String marketValue = mMarketName;
	        if (mMarketName!=null) {
	        	if (mMarketName.equals(getContext().getResources().getString(R.string.textAllSupermarkets)))
	        		marketValue = null;
	        }
        	boolean getNotSet = util.getShowProductsNotSet(this.getContext());
        	mCursor = mShoppingListRepository.getProductsNotCommittedOrderedByCategory(mMarketId, marketValue, getNotSet);
	        if (mCursor!=null) {
	            // Ensure the cursor window is filled
	        	mCursor.getCount();
	            // this is to force a reload when the content change
	        	mCursor.registerContentObserver(this.mObserver);
	            // this make sure this loader will be notified when
	            // a notifyChange is called on the URI_MY_TABLE
	            mCursor.setNotificationUri(getContext().getContentResolver(), AppPreferences.URI_LIST_TABLE);
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
			if (cursor!=null) {
				cursor.close();
				mShoppingListRepository.close();
			}
		}
	}



}
