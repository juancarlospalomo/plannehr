package com.householdplanner.shoppingapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;

public class FragmentMyProducts extends Fragment implements LoaderCallbacks<Cursor>,
					OnItemClickListener, OnFragmentProgress {

	public static final String KEY_USUAL_PRODUCTS = "Usual";
	private static final int LOADER_ID = 1;
	
	private boolean mStarted = false;
	private MyProductsAdapter mAdapter = null;
	private ListView mListView = null;
	private ActionMode mActionMode;
	private OnLoadData mCallback = null;
	
	public FragmentMyProducts() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my_products, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mStarted = false;
		loadProducts();
	}

    @Override
    public void onStart() {
    	super.onStart();
    	if (mStarted) {
    		//getActivity().getContentResolver().notifyChange(AppPreferences.URI_HISTORY_TABLE, null);
    	}
    	mStarted = true;
    }

	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mActionMode!=null) {
			onListItemSelect(position);
		}
	}
	
    private void onListItemSelect(int position) {
    	mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;
 
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
 
        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }
	
    private void deleteProducts() {
    	int messageId = R.string.textProductDeleteWarning;
    	if (mAdapter.getSelectedCount()>1) {
    		messageId = R.string.textProductsDeleteWarning;
    	}
    	util.showAlertInfoMessage(getActivity(), messageId,
    			R.string.textButtonYes, R.string.textButtonNo, 
    			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doPositiveClick();
					}
				}, null);
    }
    
	private void doPositiveClick() {
    	SparseBooleanArray selected = mAdapter.getSelectedIds();
    	Cursor cursor = mAdapter.getCursor();
		if (cursor!=null) {
			int total = cursor.getCount();
			ProductHistoryRepository historyRepository = new ProductHistoryRepository(getActivity());
			for (int index=0; index<total; index++) {
				cursor.moveToPosition(index);
				if (selected.get(index)) {
					int id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
					historyRepository.deleteProduct(id);
				}
			}
			historyRepository.close();
		}
		if (mActionMode!=null) { 
			mActionMode.finish(); // Action picked, so close the CAB
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}
	
	private void loadProducts() {
        String[] fields = new String[] { ProductHistoryStore.COLUMN_PRODUCT_NAME };
        int[] listViewColumns = new int[] { R.id.label };
                
        try {
        	mListView = (ListView) getView().findViewById(R.id.listViewMyProducts);
    		LoaderManager loaderManager = getLoaderManager();
    		loaderManager.initLoader(LOADER_ID, null, this);
        	mAdapter = new MyProductsAdapter(this.getActivity(), R.layout.usual_rowlayout, null, 
        		fields, listViewColumns);
         	mListView.setAdapter(mAdapter);
         	mListView.setOnItemClickListener(this);
        	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					if (mActionMode==null) {
						onListItemSelect(position);
						return true;
					} else {
						return false;
					}
				}
			});
        } catch (Exception e) {

        }
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (mCallback!=null) mCallback.onLoadStart();
		return new MyProductsCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	@Override
	public void setOnLoadData(OnLoadData callback) {
		mCallback = callback;
	}
	
	static class ViewHolder {
		public ImageView imageStar;
		public TextView textName;
		public ImageView imageCheck;
	}
	
    public class MyProductsAdapter extends SimpleCursorAdapter {
    	
    	Context mContext;
    	Cursor mCursor;
    	private SparseBooleanArray mSelectedItems;
    	
		public MyProductsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    		super(context, layout, c, from, to, 0);
    		mContext = context;
    		mCursor = c;
    		mSelectedItems = new SparseBooleanArray();
    	}
		
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		final ViewHolder viewHolder;
    		final int pos = position;
    		
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.usual_rowlayout, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.imageStar = (ImageView) convertView.findViewById(R.id.imageFavourite);
    			viewHolder.textName = (TextView) convertView.findViewById(R.id.label);
    			viewHolder.imageCheck = (ImageView) convertView.findViewById(R.id.imageCheck);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}
    		viewHolder.imageStar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
	            	  ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext);
	            	  mCursor.moveToPosition(pos);
	            	  int id = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_ID));
	            	  int flagSuggestion = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_SUGGESTION));
	            	  if (flagSuggestion==ProductHistoryRepository.USUAL_CONFIRMED) {
	            		  historyRepository.setSuggest(id, false);
	            		  ((ImageView)v).setImageResource(android.R.drawable.btn_star_big_off);
	            	  } else {
	            		  historyRepository.setSuggest(id, true);
	            		  ((ImageView)v).setImageResource(android.R.drawable.btn_star_big_on);
	            	  }
	            	  historyRepository.close();
	            	  getLoaderManager().restartLoader(LOADER_ID, null, FragmentMyProducts.this);
				}
    		});
    		viewHolder.imageCheck.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCursor.moveToPosition(pos);
					int id = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_ID));
					ProductHistoryRepository historyRepository = new ProductHistoryRepository(getActivity());
					ShoppingListRepository listRepository = new ShoppingListRepository(getActivity());
					Cursor cursor = historyRepository.getProduct(id); 
					if ((cursor!=null) && (cursor.moveToFirst())) {
						String productName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_PRODUCT_NAME));
						String market = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
						int categoryId = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_CATEGORY));
						int sequence = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_SEQUENCE));
						if (listRepository.createProductItem(productName, market, "", 0, categoryId, sequence)) {
							((ImageView)v).setImageResource(R.drawable.ic_check_on);
							Toast.makeText(getActivity(), R.string.textProductsAdded, Toast.LENGTH_SHORT).show();
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									getLoaderManager().restartLoader(LOADER_ID, null, FragmentMyProducts.this);
									getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
								}
							}, 100);
						}
					}
					listRepository.close();
					historyRepository.close();
				}
			});
    		mCursor.moveToPosition(position);
/*    		int id = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_ID));*/
    		int suggestion = mCursor.getInt(mCursor.getColumnIndex(ProductHistoryStore.COLUMN_SUGGESTION));
    		if (suggestion==ProductHistoryRepository.USUAL_CONFIRMED) {
    			viewHolder.imageStar.setImageResource(android.R.drawable.btn_star_big_on);
    		} else {
    			viewHolder.imageStar.setImageResource(android.R.drawable.btn_star_big_off);
    		}
   			viewHolder.imageCheck.setImageResource(R.drawable.ic_check_off);
   			viewHolder.textName.setTypeface(font.getListItemFont(getActivity()));
   			viewHolder.textName.setText(util.getCompleteHistoryRow(mContext, mCursor, false));
			if (mSelectedItems.get(position)) {
				convertView.setBackgroundColor(getResources().getColor(R.color.rowSelected));
			} else {
				convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
    		return convertView;
    	}
    	
    	@Override
    	public Cursor swapCursor(Cursor c) {
    		mCursor = c;
    		return super.swapCursor(c);
    	}

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItems.get(position));
        }
    	
        public void removeSelection() {
        	mSelectedItems = new SparseBooleanArray();
            notifyDataSetChanged();
        }
    	
    	public void selectView(int position, boolean value) {
    		if (value) 
    			mSelectedItems.put(position, value);
    		else 
    			mSelectedItems.delete(position);
            notifyDataSetChanged();
    	}
    	
    	public int getSelectedCount() {
    		return mSelectedItems.size();
    	}
    	
    	public SparseBooleanArray getSelectedIds() {
    		return mSelectedItems;
    	}
    }
	
	private static class MyProductsCursorLoader extends CursorLoader {
		
		// We hold a reference to the Loader's data here.
		private Cursor mCursor = null;
		ProductHistoryRepository mHistoryRepository = null;
		private final ForceLoadContentObserver mObserver;
		
		public MyProductsCursorLoader(Context context) {
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
	    	mHistoryRepository = new ProductHistoryRepository(this.getContext());
    		mCursor = mHistoryRepository.getMyProductsList();
	    	if (mCursor!=null) {
	    		mCursor.getCount();
	    		mCursor.registerContentObserver(mObserver);
	    		mCursor.setNotificationUri(getContext().getContentResolver(), AppPreferences.URI_HISTORY_TABLE);
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
				mHistoryRepository.close();
			}
		}
	}

	private class ActionModeCallback implements ActionMode.Callback {
		 
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.history_context_menu, menu);
            return true;
        }
 
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 
            switch (item.getItemId()) {
            case R.id.deleteProduct:
            	deleteProducts();
                return true;

            default:
                return false;
            }
 
        }
 
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            mAdapter.removeSelection();
            mActionMode = null;
        }
    }


	
}
