package com.householdplanner.shoppingapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.WalletActivity.ConfirmationDialog;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.ColorPickerDialog;
import com.householdplanner.shoppingapp.cross.ColorPickerDialog.OnColorChangedListener;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.stores.MarketStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		OnItemClickListener {

	private static final int LOADER_ID = 1;

	public static final String BITMAP_FILE_NAME = "Markets.png";
	
	private static int mCurrentPosition = 0;
	private boolean mDataChanged = false;
	private static boolean mRenamingMarket = false;
	private MarketListAdapter mAdapter;
	private ListView mListView;
	private ActionMode mActionMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_market);
		setUpFont();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		loadMarkets();
	}
	

	@Override
	protected void onStop() {
		super.onStop();
		if (mDataChanged) {
			getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
			saveBitmap();
			mDataChanged = false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mActionMode!=null)
			onListItemSelect(position);
	}
	
    private void onListItemSelect(int position) {
    	mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;
        
        if (hasCheckedItems && mActionMode == null) {
			// there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ActionModeCallback());
		}
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        else if (hasCheckedItems && mActionMode != null) {
            if (mAdapter.getSelectedCount()>1) {
            	mActionMode.getMenu().findItem(R.id.renameMarket).setVisible(false);
            } else {
            	mActionMode.getMenu().findItem(R.id.renameMarket).setVisible(true);
            }
        }
        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " " + getResources().getString(R.string.textSelected));

    }
	
	private void deleteMarkets() {
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		String message = getResources().getString(R.string.textMarketDeleteWarningMessage);
		Bundle args = new Bundle();
		args.putString("message", message);
		args.putString("title", getResources().getString(R.string.textMarketDeleteWarningTitle)); 
		confirmationDialog.setArguments(args);
		confirmationDialog.callback = this;
		confirmationDialog.show(getSupportFragmentManager(), "dialog");
	}
	
	private void disableEntryData() {
		EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
		Button buttonAccept = (Button) findViewById(R.id.btnAddMarket);
		edMarketName.setEnabled(false);
		buttonAccept.setEnabled(false);
	}
	
	private void enableEntryData() {
		EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
		Button buttonAccept = (Button) findViewById(R.id.btnAddMarket);
		edMarketName.setEnabled(true);
		buttonAccept.setEnabled(true);
	}

	private  File getOutputBitMapFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
	    File mediaStorageDir = getFilesDir();

	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            return null;
	        }
	    } 
	    // Create a media file name
	    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + BITMAP_FILE_NAME);  
	    return mediaFile;
	} 
	
	public Bitmap getListViewBitmap() {
		
	    int itemscount = mAdapter.getCount();
	    int allitemsheight   = 0;
	    List<Bitmap> bmps    = new ArrayList<Bitmap>();

	    for (int i = 0; i < itemscount; i++) {
	        View childView      = mAdapter.getView(i, null, mListView);
	        childView.measure(MeasureSpec.makeMeasureSpec(mListView.getWidth(), MeasureSpec.EXACTLY), 
	                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

	        childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
	        childView.setDrawingCacheEnabled(true);
	        childView.buildDrawingCache();
	        bmps.add(childView.getDrawingCache());
	        allitemsheight+=childView.getMeasuredHeight();
	    }

	    Bitmap bigbitmap    = Bitmap.createBitmap(mListView.getMeasuredWidth(), allitemsheight, Bitmap.Config.ARGB_8888);
	    Canvas bigcanvas    = new Canvas(bigbitmap);

	    Paint paint = new Paint();
	    int iHeight = 0;

	    for (int i = 0; i < bmps.size(); i++) {
	        Bitmap bmp = bmps.get(i);
	        bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
	        iHeight+=bmp.getHeight();

	        bmp.recycle();
	        bmp=null;
	    }
	    return bigbitmap;
	}
	
	private void renameMarket(int position) {
		EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
		Cursor cursor = mAdapter.getCursor();
		String marketName = edMarketName.getText().toString();
		if (!TextUtils.isEmpty(marketName)) {
			cursor.moveToPosition(position);
			int marketId = cursor.getInt(cursor.getColumnIndex(MarketStore.COLUMN_ID));
			String oldMarket = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
			MarketRepository marketRepository = new MarketRepository(this);
			marketRepository.renameMarket(marketId, oldMarket, marketName);
			marketRepository.close();
			edMarketName.setText("");
			mRenamingMarket = false;
			getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
			mDataChanged = true;
			if (mActionMode!=null) mActionMode.finish();
		} else {
			util.showAlertErrorMessage(this, R.string.textErrorMessageMarketNameMandatory);
		}
	}
	
	private void saveBitmap() {
		if (mAdapter.getCount()>0) {
			Bitmap bitmap = getListViewBitmap();
			File file = getOutputBitMapFile();
			if (file!=null) {
				try {
					FileOutputStream outputStream = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
					outputStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setMarketToRename(int position) {
		EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
		enableEntryData();
		Cursor cursor = mAdapter.getCursor();
		cursor.moveToPosition(position);
		edMarketName.setText(cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME)));
		mCurrentPosition = position;
		mRenamingMarket = true;
	}
	
	private void setUpFont() {
		EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
		edMarketName.setTypeface(font.getEditTextFont(this));
	}
	
	public void doPositiveClick() {
		Cursor cursor = (Cursor)mAdapter.getCursor();
		if (cursor!=null) {
			SparseBooleanArray selectedItems = mAdapter.getSelectedIds();
			MarketRepository marketRepository = new MarketRepository(this);
			for(int index=0; index<cursor.getCount(); index++) {
				if (cursor.moveToPosition(index)) {
					if (selectedItems.get(index)) {
						int marketId = cursor.getInt(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_ID));
						String marketName = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
						marketRepository.deleteMarketItem(marketId, marketName);
						mDataChanged = true;
					}
				}
			}
			marketRepository.close();
			getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
		}
    	if (mActionMode!=null)mActionMode.finish();
	}
	
	public void btnAddMarket_onClick (View view) {
		if (mRenamingMarket) {
			renameMarket(mCurrentPosition);
		} else {
			String marketName;
			int categories = getResources().getStringArray(R.array.category_array).length;
			EditText edMarketName = (EditText) findViewById(R.id.edMarketName);
			marketName = edMarketName.getText().toString();
			if (!TextUtils.isEmpty(marketName)) {
				MarketRepository marketRepository = new MarketRepository(this);
				if (marketRepository.getMarketId(marketName)==0) {
					marketRepository.createMarketItem(marketName, categories);
					edMarketName.setText("");
					getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
					if (mActionMode!=null) mActionMode.finish();
					mDataChanged = true;
				} else {
					util.showAlertErrorMessage(this, R.string.textErrorMessageMarketExist);
				}
				marketRepository.close();
			} else {
				util.showAlertErrorMessage(this, R.string.textErrorMessageMarketNameMandatory);
			}
		}
	}
	
	private void loadMarkets() {
        String[] fields = new String[] { MarketStore.COLUMN_MARKET_NAME };
        int[] listViewColumns = new int[] { android.R.id.text1 };
                
        try {
        	mListView = (ListView) findViewById(R.id.listViewMarket);
        	LoaderManager loaderManager = getSupportLoaderManager();
    		loaderManager.initLoader(LOADER_ID, null, this);
    		int layoutId;
  			layoutId = R.layout.market_rowlayout;
			mAdapter = new MarketListAdapter(layoutId, null, fields, listViewColumns);
        	mListView.setAdapter(mAdapter);
        	mListView.setOnItemClickListener(this);
        	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
		return new MarketCursorLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
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
	    // For whatever reason, the Loader's data is now unavailable.
	    // Remove any references to the old data by replacing it with
	    // a null Cursor.
		mAdapter.swapCursor(null);
	}
	
	static class ViewHolder {
		public TextView text;
		public ImageView imagePicker;
	}
	
    public class MarketListAdapter extends SimpleCursorAdapter {
    	
    	Cursor mCursor;
    	SparseBooleanArray mSelectedItems = null;
    	
		public MarketListAdapter(int layout, Cursor c, String[] from, int[] to) {
    		super(MarketActivity.this, layout, c, from, to, 0);
    		mCursor = c;
    		mSelectedItems = new SparseBooleanArray();
    	}
		
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		final ViewHolder viewHolder;
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) MarketActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.market_rowlayout, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.text = (TextView) convertView.findViewById(R.id.textMarketName);
    			viewHolder.imagePicker = (ImageView) convertView.findViewById(R.id.imageColorPicker);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}

    		mCursor.moveToPosition(position);
    		String marketName = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
    		viewHolder.text.setTypeface(font.getListItemFont(MarketActivity.this));
    		viewHolder.text.setText(util.capitalize(marketName));
			if (mSelectedItems.get(position))  {
				convertView.setBackgroundColor(getResources().getColor(R.color.rowSelected));
			} else {
				convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
			String color = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_COLOR));
			if (color!=null) {
				viewHolder.imagePicker.setImageDrawable(null);
				GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_blue);
				drawable.mutate();
				drawable.setColor(Integer.parseInt(color));
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					viewHolder.imagePicker.setBackgroundDrawable(drawable);
				} else {
					viewHolder.imagePicker.setBackground(drawable);
				}
			} else {
				viewHolder.imagePicker.setBackgroundResource(R.drawable.ic_action_colorpicker);
			}
			final int marketId = mCursor.getInt(mCursor.getColumnIndex(MarketStore.COLUMN_MARKET_ID));
			viewHolder.imagePicker.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				       Paint paint;  
				       paint = new Paint();
				       // on button click
				       ColorPickerDialog colorPickerDialog = new ColorPickerDialog(MarketActivity.this, 
				    		   new OnColorChangedListener() {
								@Override
								public void colorChanged(int color) {
									MarketRepository marketRepository = new MarketRepository(MarketActivity.this);
									marketRepository.setColor(marketId, color);
									marketRepository.close();
									getSupportLoaderManager().restartLoader(LOADER_ID, null, MarketActivity.this);
									
									mDataChanged = true;
								}
							}, paint.getColor());
				       colorPickerDialog.show();
				}
			});
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

	
	private static class MarketCursorLoader extends AsyncTaskLoader<Cursor> {
		
		// We hold a reference to the Loader's data here.
		private Cursor mCursor = null;
		MarketRepository mMarketRepository = null;
		
		public MarketCursorLoader(Context context) {
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
	        mCursor = mMarketRepository.getAllMarkets();
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

	private class ActionModeCallback implements ActionMode.Callback {
		 
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.market_context_menu, menu);
            disableEntryData();
            return true;
        }
 
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         	
            switch (item.getItemId()) {
            case R.id.deleteMarket:
            	deleteMarkets();
                return true;
            case R.id.renameMarket:
            	if (mAdapter.getSelectedCount()==1) {
            		int position = mAdapter.getSelectedIds().keyAt(0);
            		setMarketToRename(position);
            	}
            	
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
            enableEntryData();
        }
    }




}
