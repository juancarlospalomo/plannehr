package com.householdplanner.shoppingapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.BaseActivity;
import com.householdplanner.shoppingapp.ColorPickerActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.exceptions.MarketException;
import com.householdplanner.shoppingapp.loaders.MarketLoader;
import com.householdplanner.shoppingapp.models.Market;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.usecases.UseCaseMarket;

import java.util.List;

/**
 * Created by JuanCarlos on 25/05/2015.
 */
public class FragmentMarket extends Fragment implements LoaderManager.LoaderCallbacks<List<Market>>,
        AdapterView.OnItemClickListener {

    private final static String LOG_TAG = FragmentMarket.class.getSimpleName();

    private static final int LOADER_ID = 1;

    //Request Codes
    private static final int REQUEST_CODE_COLOR = 1;
    //Hold the row that contains the supermarket name being updated
    private static int mCurrentPosition = 0;
    private static boolean mRenamingMarket = false;
    //To save if the Toolbar is in contextual mode
    private boolean mContextualMode = false;
    //List View to show the Supermarkets list
    private ListView mListView;

    private MarketListAdapter mAdapter;
    private ValidationField mMarketValidationField;
    private AppCompatButton mButtonAddMarket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Inflate Views
        inflateViews();
        //Init view handlers
        createButtonHandlers();
        //Load list of markets
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Inflate the existing views in the activity
     */
    private void inflateViews() {
        mMarketValidationField = (ValidationField) getView().findViewById(R.id.validationViewMarket);
        mButtonAddMarket = (AppCompatButton) getView().findViewById(R.id.buttonAddMarket);
        mListView = (ListView) getView().findViewById(R.id.listViewMarket);
    }

    /**
     * Create the handlers for the buttons
     */
    private void createButtonHandlers() {
        mButtonAddMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarket();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mContextualMode) {
            inflater.inflate(R.menu.market_menu, menu);
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_COLOR) {
            if (resultCode == getActivity().RESULT_OK) {
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
        if (mContextualMode)
            onListItemSelect(view, position);
    }

    /**
     * Save in database the color set to the supermarket
     *
     * @param marketId market identifier
     * @param color    color
     */
    private void saveMarketColor(int marketId, int color) {
        MarketRepository marketRepository = new MarketRepository(getActivity());
        marketRepository.setColor(marketId, color);
        marketRepository.close();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
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
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (hasCheckedItems && !mContextualMode) {
            // there are some selected items, start the actionMode
            baseActivity.startToolbarContextualActionMode(new ToolbarContextualMode());
            //mActionMode = startSupportActionMode(new ActionModeCallback());
        } else if (!hasCheckedItems && mContextualMode)
            // there no selected items, finish the actionMode
            baseActivity.finishToolbarContextualActionMode();
        else if (hasCheckedItems && mContextualMode) {
            if (mAdapter.getSelectedCount() > 1) {
                baseActivity.getActionBarToolbar().getMenu().findItem(R.id.renameMarket).setVisible(false);
            } else {
                baseActivity.getActionBarToolbar().getMenu().findItem(R.id.renameMarket).setVisible(true);
            }
        }
        if (mContextualMode) {
            baseActivity.getActionBarToolbar().setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
        }

    }


    /**
     * Delete the selected markets if the user confirm it
     */
    private void deleteMarkets() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_supermarket_dialog_title),
                "", getResources().getString(R.string.delete_supermarket_dialog_cancel_text),
                getResources().getString(R.string.delete_supermarket_dalog_ok_text), null);

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    if (mAdapter.mMarketDataList != null) {
                        SparseBooleanArray selectedItems = mAdapter.getSelectedIds();
                        for (int index = 0; index < mAdapter.mMarketDataList.size(); index++) {
                            Market market = mAdapter.mMarketDataList.get(index);
                            if (selectedItems.get(index)) {
                                UseCaseMarket useCaseMarket = new UseCaseMarket(getActivity());
                                useCaseMarket.deleteMarket(market._id);
                                getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                            }
                        }
                        getLoaderManager().restartLoader(LOADER_ID, null, FragmentMarket.this);
                    }
                    if (mContextualMode)
                        ((BaseActivity) getActivity()).finishToolbarContextualActionMode();
                }
            }
        });
        alertDialog.show(getFragmentManager(), "confirmationDialog");
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
        String marketName = mMarketValidationField.getText();
        if (!TextUtils.isEmpty(marketName)) {
            Market market = mAdapter.mMarketDataList.get(position);
            UseCaseMarket useCaseMarket = new UseCaseMarket(getActivity());
            useCaseMarket.renameMarket(market._id, marketName);
            mMarketValidationField.setText("");
            mRenamingMarket = false;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
            if (mContextualMode) ((BaseActivity) getActivity()).finishToolbarContextualActionMode();
        } else {
            mMarketValidationField.setError(R.string.error_text_market_name_mandatory);
        }
    }

    /**
     * Get the market in the selected position and set it on TextView to be modified
     *
     * @param position row position
     */
    private void setMarketToRename(int position) {
        enableEntryData();
        mMarketValidationField.setText(mAdapter.mMarketDataList.get(position).name);
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
            Market market = new Market();
            market.name = mMarketValidationField.getText();
            if (market.validate()) {
                mMarketValidationField.setError("");
                UseCaseMarket useCaseMarket = new UseCaseMarket(getActivity());
                try {
                    useCaseMarket.createMarket(market);
                    mMarketValidationField.setText("");
                    getLoaderManager().restartLoader(LOADER_ID, null, this);
                    if (mContextualMode)
                        ((BaseActivity) getActivity()).finishToolbarContextualActionMode();
                } catch (MarketException e) {
                    mMarketValidationField.setError(R.string.error_text_market_exist);
                }
            } else {
                mMarketValidationField.setError(R.string.error_text_market_name_mandatory);
            }
        }
    }

    @Override
    public Loader<List<Market>> onCreateLoader(int id, Bundle args) {
        return new MarketLoader(getActivity(), MarketLoader.TypeMarketSet.All);
    }

    @Override
    public void onLoadFinished(Loader<List<Market>> loader, List<Market> data) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter = new MarketListAdapter(getActivity(), data);
                mListView.setAdapter(mAdapter);
                mListView.setOnItemClickListener(this);
                mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!mContextualMode) {
                            onListItemSelect(view, position);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Market>> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mListView.setAdapter(null);
        mAdapter = null;
    }

    static class ViewHolder {
        public TextView mTextMarket;
        public ImageView mImagePicker;
    }

    public class MarketListAdapter extends ArrayAdapter<Market> {

        private Context mContext;
        private List<Market> mMarketDataList;
        SparseBooleanArray mSelectedItems = null;

        public MarketListAdapter(Context context, List<Market> data) {
            super(context, R.layout.market_rowlayout);
            mContext = context;
            mMarketDataList = data;
            mSelectedItems = new SparseBooleanArray();
        }

        @Override
        public int getCount() {
            if (mMarketDataList!=null) {
                return mMarketDataList.size();
            } else {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.market_rowlayout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mTextMarket = (TextView) convertView.findViewById(R.id.textMarketName);
                viewHolder.mImagePicker = (ImageView) convertView.findViewById(R.id.imageColorPicker);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Market market = mMarketDataList.get(position);
            viewHolder.mTextMarket.setText(util.capitalize(market.name));
            if (mSelectedItems.get(position)) {
                convertView.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                convertView.setBackgroundResource(R.drawable.list_row_background);
            }
            if (market.color != 0) {
                viewHolder.mImagePicker.setImageDrawable(null);
                GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_blue);
                drawable.mutate();
                drawable.setColor(market.color);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.mImagePicker.setBackgroundDrawable(drawable);
                } else {
                    viewHolder.mImagePicker.setBackground(drawable);
                }
            } else {
                viewHolder.mImagePicker.setBackgroundResource(R.drawable.ic_action_colorpicker);
            }
            viewHolder.mImagePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ColorPickerActivity.class);
                    if (market.color == 0) {
                        intent.putExtra(ColorPickerActivity.EXTRA_OLD_COLOR, 0);
                    } else {
                        intent.putExtra(ColorPickerActivity.EXTRA_OLD_COLOR, market.color);
                    }
                    intent.putExtra(ColorPickerActivity.EXTRA_MARKET_ID, market._id);
                    startActivityForResult(intent, REQUEST_CODE_COLOR);
                }
            });
            return convertView;
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

    private class ToolbarContextualMode implements BaseActivity.ToolbarContextualMode {

        @Override
        public void onCreateToolbarContextualMode() {
            mContextualMode = true;
        }

        @Override
        public void onFinishToolbarContextualMode() {
            mAdapter.removeSelection();
            mContextualMode = false;
            enableEntryData();
        }
    }

}
