package com.householdplanner.shoppingapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.fragments.AlertDialogFragment;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.stores.MarketStore;

public class MarketActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        OnItemClickListener {

    private static final int LOADER_ID = 1;

    //Request Codes
    private static final int REQUEST_CODE_COLOR = 1;

    public static final String BITMAP_FILE_NAME = "Markets.png";

    private static int mCurrentPosition = 0;
    private static boolean mRenamingMarket = false;
    private MarketListAdapter mAdapter;
    private ListView mListView;
    private ActionMode mActionMode;

    private ValidationField mMarketValidationField;
    private AppCompatButton mButtonAddMarket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Inflate Views
        inflateViews();
        //Init view handlers
        createButtonHandlers();
        //Load list of markets
        loadMarkets();
    }

    private void inflateViews() {
        mMarketValidationField = (ValidationField) findViewById(R.id.validationViewMarket);
        mButtonAddMarket = (AppCompatButton) findViewById(R.id.buttonAddMarket);
    }

    /**
     * Create the handlers for the buttons
     */
    private void createButtonHandlers() {
        mButtonAddMarket.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarket();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_COLOR) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    int marketId = extras.getInt(ColorPickerActivity.EXTRA_MARKET_ID);
                    int color = extras.getInt(ColorPickerActivity.EXTRA_CURRENT_COLOR);
                    saveMarketColor(marketId, color);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (mActionMode != null)
            onListItemSelect(view, position);
    }

    private void saveMarketColor(int marketId, int color) {
        MarketRepository marketRepository = new MarketRepository(MarketActivity.this);
        marketRepository.setColor(marketId, color);
        marketRepository.close();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, MarketActivity.this);
        getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
    }

    /**
     * Manage the list item click functionality
     *
     * @param position row
     */
    private void onListItemSelect(View view, int position) {
        mAdapter.toggleSelection(position);
        if (mAdapter.isSelected(position)) {
            view.setBackgroundResource(R.drawable.list_row_background_selected);
        } else {
            view.setBackgroundResource(R.drawable.list_row_background);
        }
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ActionModeCallback());
        } else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        else if (hasCheckedItems && mActionMode != null) {
            if (mAdapter.getSelectedCount() > 1) {
                mActionMode.getMenu().findItem(R.id.renameMarket).setVisible(false);
            } else {
                mActionMode.getMenu().findItem(R.id.renameMarket).setVisible(true);
            }
        }
        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " " + getResources().getString(R.string.textSelected));

    }

    /**
     * Delete the selected markets if the user confirm it
     */
    private void deleteMarkets() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_supermarket_dialog_title),
                "", getResources().getString(R.string.delete_supermarket_dialog_cancel_text),
                getResources().getString(R.string.delete_supermarket_dalog_ok_text));

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    Cursor cursor = (Cursor) mAdapter.getCursor();
                    if (cursor != null) {
                        SparseBooleanArray selectedItems = mAdapter.getSelectedIds();
                        MarketRepository marketRepository = new MarketRepository(MarketActivity.this);
                        for (int index = 0; index < cursor.getCount(); index++) {
                            if (cursor.moveToPosition(index)) {
                                if (selectedItems.get(index)) {
                                    int marketId = cursor.getInt(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_ID));
                                    String marketName = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
                                    marketRepository.deleteMarketItem(marketId, marketName);
                                    getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
                                }
                            }
                        }
                        marketRepository.close();
                        getSupportLoaderManager().restartLoader(LOADER_ID, null, MarketActivity.this);
                    }
                    if (mActionMode != null) mActionMode.finish();
                }
            }
        });
        alertDialog.show(getSupportFragmentManager(), "confirmationDialog");
    }

    /**
     * Disable the view to enter data
     */
    private void disableEntryData() {
        mMarketValidationField.setEnabled(false);
        mButtonAddMarket.setEnabled(false);
    }

    /**
     * Enable the view to enter data
     */
    private void enableEntryData() {
        mMarketValidationField.setEnabled(true);
        mButtonAddMarket.setEnabled(true);
    }

    /**
     * Rename a market
     *
     * @param position position beloging to the market to rename
     */
    private void renameMarket(int position) {
        Cursor cursor = mAdapter.getCursor();
        String marketName = mMarketValidationField.getText();
        if (!TextUtils.isEmpty(marketName)) {
            cursor.moveToPosition(position);
            int marketId = cursor.getInt(cursor.getColumnIndex(MarketStore.COLUMN_ID));
            String oldMarket = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
            MarketRepository marketRepository = new MarketRepository(this);
            marketRepository.renameMarket(marketId, oldMarket, marketName);
            marketRepository.close();
            mMarketValidationField.setText("");
            mRenamingMarket = false;
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            if (mActionMode != null) mActionMode.finish();
        } else {
            util.showAlertErrorMessage(this, R.string.textErrorMessageMarketNameMandatory);
        }
    }

    private void setMarketToRename(int position) {
        enableEntryData();
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        mMarketValidationField.setText(cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME)));
        mCurrentPosition = position;
        mRenamingMarket = true;
    }

    /**
     * Add market to the list
     */
    private void addMarket() {
        if (mRenamingMarket) {
            renameMarket(mCurrentPosition);
        } else {
            String marketName;
            int categories = getResources().getStringArray(R.array.category_array).length;
            marketName = mMarketValidationField.getText();
            if (!TextUtils.isEmpty(marketName)) {
                mMarketValidationField.setError("");
                MarketRepository marketRepository = new MarketRepository(this);
                if (marketRepository.getMarketId(marketName) == 0) {
                    marketRepository.createMarketItem(marketName, categories);
                    mMarketValidationField.setText("");
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                    if (mActionMode != null) mActionMode.finish();
                } else {
                    mMarketValidationField.setError(R.string.error_text_market_exist);
                }
                marketRepository.close();
            } else {
                mMarketValidationField.setError(R.string.error_text_market_name_mandatory);
            }
        }
    }

    private void loadMarkets() {
        String[] fields = new String[]{MarketStore.COLUMN_MARKET_NAME};
        int[] listViewColumns = new int[]{android.R.id.text1};

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
                    if (mActionMode == null) {
                        onListItemSelect(view, position);
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
        switch (loader.getId()) {
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
            viewHolder.text.setText(util.capitalize(marketName));
            if (mSelectedItems.get(position)) {
                convertView.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                convertView.setBackgroundResource(R.drawable.list_row_background);
            }
            final String color = mCursor.getString(mCursor.getColumnIndex(MarketStore.COLUMN_COLOR));
            if (color != null) {
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
                    Intent intent = new Intent(MarketActivity.this, ColorPickerActivity.class);
                    if (color == null) {
                        intent.putExtra(ColorPickerActivity.EXTRA_OLD_COLOR, 0);
                    } else {
                        intent.putExtra(ColorPickerActivity.EXTRA_OLD_COLOR, Integer.parseInt(color));
                    }
                    intent.putExtra(ColorPickerActivity.EXTRA_MARKET_ID, marketId);
                    startActivityForResult(intent, REQUEST_CODE_COLOR);
                }
            });
            return convertView;
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            mCursor = c;
            return super.swapCursor(c);
        }

        /**
         * Revert the selection or unselection
         *
         * @param position row
         */
        public void toggleSelection(int position) {
            selectView(position, !mSelectedItems.get(position));
        }

        /**
         * Clear all rows selected
         */
        public void removeSelection() {
            mSelectedItems = new SparseBooleanArray();
            notifyDataSetChanged();
        }

        /**
         * Return if the row in one position is selected or not
         *
         * @param position row
         * @return
         */
        public boolean isSelected(int position) {
            return mSelectedItems.get(position);
        }

        public void selectView(int position, boolean value) {
            if (value)
                mSelectedItems.put(position, value);
            else
                mSelectedItems.delete(position);
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
        /**
         * ************************************************
         */
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
                    if (mAdapter.getSelectedCount() == 1) {
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
