package com.householdplanner.shoppingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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
import android.widget.ListView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.ProgressCircle;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.WalletRepository;
import com.householdplanner.shoppingapp.stores.BudgetStore;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class WalletActivity extends AppCompatActivity implements
	LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final int BUDGET_ADD = 1;
	private static final int BUDGET_MODIFY = 2;
	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 1;
	
	private boolean mIsHelpInActivity = false;
	private String mCurrencySymbol = null;
	private WalletListAdapter mAdapter;
	private ActionMode mActionMode = null;
	private ListView mListView = null;
	private ProgressCircle mProgressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallet);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		LoadWalletData();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (getSupportLoaderManager().getLoader(LOADER_ID)==null) {
			getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		getSupportLoaderManager().destroyLoader(LOADER_ID);
	}

	private String getCurrencySymbol() {
		if (mCurrencySymbol==null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			mCurrencySymbol = prefs.getString("prefCurrencySymbol","€");
		}
		return mCurrencySymbol;
	}
	
	private void LoadWalletData() {
		LoadBudgetSection();
	}
	
	private void LoadBudgetSection() {
		String[] fields = new String[] {BudgetStore.COLUMN_AVAILABLE};
		int[] listViewColumns = {R.id.textBudgetAvailable};
		mListView = (ListView) findViewById(R.id.listViewBudget);
		LoaderManager loaderManager = getSupportLoaderManager();
		loaderManager.initLoader(LOADER_ID, null, this);
		mAdapter = new WalletListAdapter(this, R.layout.wallet_rowlayout, null, fields, listViewColumns);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
    	mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
										   View view, int position, long id) {
				if (!AppGlobalState.getInstance().isShoppingMode(WalletActivity.this)) {
					if (mActionMode == null) {
						onListItemSelect(position);
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		});
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
            	mActionMode.getMenu().findItem(R.id.editBudget).setVisible(false);
            } else {
            	mActionMode.getMenu().findItem(R.id.editBudget).setVisible(true);
            }
        }
 
        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wallet, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			return true;
			
		case R.id.action_budgetAdd:
			intent = new Intent(this, BudgetActivity.class);
			startActivityForResult(intent, BUDGET_ADD);
			return true;
			
		case R.id.action_help:
			if (!mIsHelpInActivity) {
				intent = new Intent(this, HelpActivity.class);
				intent.putExtra(HelpActivity.EXTRA_HELP_SCREEN, HelpActivity.PARAM_WALLET_SCREEN);
				startActivity(intent);
			}
			return true;
		}
		return true;
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mActionMode!=null) {
			onListItemSelect(position);
		}
	}

	private void EditBudget(int position) {
		Cursor cursor = (Cursor)mAdapter.getItem(position);
		if (cursor!=null) {
			if (isModificationRuleOk(cursor)) {
				Intent intent = new Intent(this, BudgetActivity.class);
				intent.putExtra("BudgetId", cursor.getInt(cursor.getColumnIndex(BudgetStore.COLUMN_ID)));
				intent.putExtra(BudgetStore.COLUMN_MONTH, cursor.getInt(cursor.getColumnIndex(BudgetStore.COLUMN_MONTH)));
				intent.putExtra(BudgetStore.COLUMN_AVAILABLE, cursor.getFloat(cursor.getColumnIndex(BudgetStore.COLUMN_AVAILABLE)));
				intent.putExtra(BudgetStore.COLUMN_TARGET, cursor.getFloat(cursor.getColumnIndex(BudgetStore.COLUMN_TARGET)));
				startActivityForResult(intent, BUDGET_MODIFY);
			}
		}
	}
	
	private void DeleteBudgets() {
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		Bundle args = new Bundle();
		args.putInt("id",0);
		String message = "";
		if (mAdapter.getSelectedCount()>1) {
			message = getResources().getString(R.string.textBudgetsDeleteWarningMessage);
		} else {
			message = getResources().getString(R.string.textBudgetDeleteWarningMessage);
		}
		args.putString("message", message);
		args.putString("title", getResources().getString(R.string.textBudgetDeleteWarningTitle)); 
		confirmationDialog.setArguments(args);
		confirmationDialog.callback = this;
		confirmationDialog.show(getSupportFragmentManager(), "dialog");
	}
	
	public void doPositiveClick() {
		Cursor cursor = (Cursor)mAdapter.getCursor();
		if (cursor!=null) {
			SparseBooleanArray selectedItems = mAdapter.getSelectedIds();
			WalletRepository walletRepository = new WalletRepository(this);
			for(int index=0; index<cursor.getCount(); index++) {
				if (cursor.moveToPosition(index)) {
					if (selectedItems.get(index)) {
						int id = cursor.getInt(cursor.getColumnIndex(BudgetStore.COLUMN_ID));
						//int monthId = cursor.getInt(cursor.getColumnIndex(BudgetStore.COLUMN_MONTH));
						walletRepository.deleteBudget(id);
					}
				}
			}
			walletRepository.close();
			getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
		}
    	if (mActionMode!=null)mActionMode.finish();
	}
	
	private boolean isModificationRuleOk(Cursor cursor) {
		boolean result = true;
		if ((cursor.getFloat(cursor.getColumnIndex(BudgetStore.COLUMN_WITHDRAWN))>0)
				|| (cursor.getFloat(cursor.getColumnIndex(BudgetStore.COLUMN_WALLET))>0)) {
			ShowValidationErrorMessage();
			result = false;
		}
		return result;
	}
	
	@SuppressLint("NewApi")
	private void ShowValidationErrorMessage() {
		String[] message = new String[] {getResources().getString(R.string.textBudgetModificationWarningMessage) };
		ValidationDialog alertDialog = new ValidationDialog();
		Bundle args = new Bundle();
		args.putStringArray("message", message);
		args.putString("title", getResources().getString(R.string.textBudgetModificationWarningTitle));
		alertDialog.setArguments(args);
		alertDialog.show(getSupportFragmentManager(), "dialog");
	}
	
	private void ShowHelp() {
		ScreenHelp screenHelp = (ScreenHelp) findViewById(R.id.mainHelp);
		mListView.setVisibility(View.GONE);
		screenHelp.setVisibility(View.VISIBLE);
		mIsHelpInActivity = true;
	}
	
	private void HideHelp() {
		if (mIsHelpInActivity) {
			ScreenHelp screenHelp = (ScreenHelp) findViewById(R.id.mainHelp);
			screenHelp.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			mIsHelpInActivity = false;
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch(requestCode) {
			case BUDGET_ADD:
				if (resultCode == RESULT_OK) {
					//load new budget data
					getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
				}
				if (resultCode == RESULT_CANCELED) {
					//write code if there's no result
				}
				break;
			case BUDGET_MODIFY:
				if (resultCode == RESULT_OK) {
					//load updated budget
					if (mActionMode!=null)mActionMode.finish();
					getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
				}
				break;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (mProgressDialog==null) {
			mProgressDialog = new ProgressCircle(this);
			mProgressDialog.show();
		}
		return new WalletCursorLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// A switch-case is useful when dealing with multiple Loaders/IDs
		switch(loader.getId()) {
			case LOADER_ID:
				mAdapter.swapCursor(cursor);
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (cursor.getCount()>0) {
			        // The asynchronous load is complete and the data
			        // is now available for use. Only now can we associate
			        // the queried Cursor with the SimpleCursorAdapter.
					HideHelp();
				} else {
					ShowHelp();
				}
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
		public TextView textMonth;
		public TextView textAvailable;
		public TextView textTarget;
		public TextView textWithDrawn;
		public TextView textWallet;
		public TextView textAvailableBar;
		public TextView textSpentBar;
		public TextView textForecastBar;
		public TextView textAvailableAmount;
		public TextView textSpentAmount;
		public TextView textForecastAmount;
	}
	
    public class WalletListAdapter extends SimpleCursorAdapter {
    	
    	Context mContext;
    	Cursor mCursor;
    	SparseBooleanArray mSelectedItems = null;
    	
		public WalletListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    		super(context, layout, c, from, to, 0);
    		mContext = context;
    		mCursor = c;
    		mSelectedItems = new SparseBooleanArray();
    	}
		
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		final ViewHolder viewHolder;
    		
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.wallet_rowlayout, parent, false);
    			viewHolder = new ViewHolder();
    			viewHolder.textMonth = (TextView) convertView.findViewById(R.id.textMonthName);
    			viewHolder.textAvailable = (TextView) convertView.findViewById(R.id.textBudgetAvailable);
    			viewHolder.textTarget = (TextView) convertView.findViewById(R.id.textBudgetTarget);
    			viewHolder.textWithDrawn = (TextView) convertView.findViewById(R.id.textWithDrawn);
    			viewHolder.textWallet = (TextView) convertView.findViewById(R.id.textWalletMoney);
    			viewHolder.textAvailableBar = (TextView) convertView.findViewById(R.id.textAvailableBar);
    			viewHolder.textSpentBar = (TextView) convertView.findViewById(R.id.textSpentBar);
    			viewHolder.textForecastBar = (TextView) convertView.findViewById(R.id.textForecastBar);
    			viewHolder.textAvailableAmount = (TextView) convertView.findViewById(R.id.textAvailableAmount);
    			viewHolder.textSpentAmount = (TextView) convertView.findViewById(R.id.textSpentAmount);
    			viewHolder.textForecastAmount = (TextView) convertView.findViewById(R.id.textForecastAmount);
    			convertView.setTag(viewHolder);
    		} else {
    			viewHolder = (ViewHolder) convertView.getTag();
    		}

    		if (mCursor!=null) {
	    		mCursor.moveToPosition(position);
	    		int monthId = mCursor.getInt(mCursor.getColumnIndex(BudgetStore.COLUMN_MONTH));
	    		float available =  mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_AVAILABLE));
	    		float target = mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_TARGET));
	    		float withDrawn = mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_WITHDRAWN));
	    		float wallet = mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_WALLET));
	    		float deviceWithDrawn = mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_DEVICE_WITHDRAWN));
	    		float deviceWallet = mCursor.getFloat(mCursor.getColumnIndex(BudgetStore.COLUMN_DEVICE_WALLET));
	    		float totalSpent = withDrawn + wallet;
	    		String currencySymbol = getCurrencySymbol();
	    		
	    		viewHolder.textMonth.setText(getResources().getString(R.string.textBudgetMonthName) + ": " + getMonthName(monthId));
	    		viewHolder.textAvailable.setText(getResources().getString(R.string.textBudgetAvailable) + ": " + String.valueOf(available) + " " + currencySymbol);
	    		viewHolder.textTarget.setText(getResources().getString(R.string.textBudgetTarget) + ": " + String.valueOf(target) + " " + currencySymbol);
	    		viewHolder.textWithDrawn.setText(getResources().getString(R.string.textBudgetWithDrawn) 
	    				+ ": " + String.valueOf(withDrawn) 
	    				+ " " + currencySymbol
	    				+ " (" + String.valueOf(deviceWithDrawn) + currencySymbol + ")");
	    		
	    		viewHolder.textWallet.setText(getResources().getString(R.string.textWalletMoney) 
	    				+ ": " + String.valueOf(wallet) 
	    				+ " " + currencySymbol
	    				+ " (" + String.valueOf(deviceWallet) + currencySymbol + ")"); 
    		
	    		viewHolder.textAvailableAmount.setText(String.valueOf(available));
	    		viewHolder.textSpentAmount.setText(String.valueOf(totalSpent));
	    		int height_max_in_pixels = (int) (100 * mContext.getResources().getDisplayMetrics().density + 0.5f); //viewHolder.textAvailable.getHeight();
	    		int size = (int) (totalSpent * height_max_in_pixels / available);
	    		viewHolder.textSpentBar.setHeight(size);
	    		
	    		int monthDaysConsumed;
	    		float savingForecast;
	    		
	    		if (monthId==Calendar.getInstance().get(Calendar.MONTH)) {
	    			//The budget belongs to the current month
	    			int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	    			monthDaysConsumed = Calendar.getInstance().get(Calendar.DATE);
		    		if ((totalSpent/available)*100>25 && monthDaysConsumed>7) {
		    			//we estimate the forecast if the total spent is greater than 25%
		    			//of the available money
		    			float spentByDay = totalSpent / (monthDaysConsumed - 1); //we get the spent by day
		    			//now we must multiply the spent by day for the days remaining in the month
		    			savingForecast = available - (totalSpent + (spentByDay * ((daysInMonth-monthDaysConsumed)+1)));
		    		} else {
		    			//forecast = available - target because we haven't got data to estimate
		    			savingForecast = available - target;
		    		}
	    		} else {
	    			//It is a past or future budget
	    			if (totalSpent==0) {
	    				//It is a future budget, so forecast = available - target
	    				savingForecast = available - target;
	    			} else {
	    				//It is a past budget, so forecast = available - totalSpent
	    				savingForecast = available - totalSpent;
	    			}
	    		}
	    		viewHolder.textForecastAmount.setText(String.valueOf(savingForecast));
	    		size = (int) (savingForecast * height_max_in_pixels / available);
	    		viewHolder.textForecastBar.setHeight(size);
    		}
    		
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

    private String getMonthName(int id) {
    	DateFormatSymbols dateFormat = new DateFormatSymbols(getResources().getConfiguration().locale);
    	return util.capitalize(dateFormat.getMonths()[id]);
    }
    
	/****************************************************/
	/** http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html **/
	/****************************************************/
	private static class WalletCursorLoader extends AsyncTaskLoader<Cursor> {
		
		// We hold a reference to the Loader’s data here.
		private Cursor mCursor = null;
		WalletRepository mWalletRepository = null;
		
		public WalletCursorLoader(Context context) {
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
			mWalletRepository = new WalletRepository(getContext());
			mCursor = mWalletRepository.getAllBudgets();
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
	    /** (3) Implement the Loader’s state-dependent behavior **/
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
			mWalletRepository.close();
		}
	}
	
	private class ActionModeCallback implements ActionMode.Callback {
		 
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.wallet_context_menu, menu);
            return true;
        }
 
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 
            switch (item.getItemId()) {
            case R.id.editBudget:
            	if (mAdapter.getSelectedCount()==1) {
            		int position = mAdapter.getSelectedIds().keyAt(0);
            		EditBudget(position);
            	}
                return true;
            case R.id.deleteBudget:
            	DeleteBudgets();
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

	
	public static class ConfirmationDialog extends DialogFragment {
		
		public Activity callback;
		
		public ConfirmationDialog() {
			super();
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString("message");
			String title = getArguments().getString("title");
			Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle(title);
			alertDialogBuilder.setMessage(message);
			alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (callback instanceof WalletActivity) {
						((WalletActivity) callback).doPositiveClick();
					}
				}
			});
			alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getDialog().dismiss();
				}
			});
			
			return alertDialogBuilder.create();
		}
		
	}
	
}


