package com.householdplanner.shoppingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.ProgressCircle;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.fragments.AlertDialogFragment;
import com.householdplanner.shoppingapp.loaders.WalletLoader;
import com.householdplanner.shoppingapp.models.Budget;
import com.householdplanner.shoppingapp.usecases.UseCaseBudget;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

public class WalletActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<List<Budget>>, OnItemClickListener {

    private final static String LOG_TAG = WalletActivity.class.getSimpleName();

    private static final int BUDGET_ADD = 1;
    private static final int REQUEST_CODE_BUDGET_MODIFY = 2;
    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID = 1;

    private boolean mIsHelpInActivity = false;
    private String mCurrencySymbol = null;
    private WalletListAdapter mAdapter;
    private ListView mListView = null;
    private ProgressCircle mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        inflateViews();
        LoadWalletData();
    }

    /**
     * Inflate the views
     */
    private void inflateViews() {
        mListView = (ListView) findViewById(R.id.listViewBudget);
    }

    /**
     * Return the configured Currency symbol
     *
     * @return
     */
    private String getCurrencySymbol() {
        if (mCurrencySymbol == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            mCurrencySymbol = prefs.getString("prefCurrencySymbol", "€");
        }
        return mCurrencySymbol;
    }

    /**
     * Load the Budgets
     */
    private void LoadWalletData() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(null);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                if (!AppGlobalState.getInstance().isShoppingMode(WalletActivity.this)) {
                    if (!mIsContextualMode) {
                        onListItemSelect(view, position);
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

    private void onListItemSelect(View view, int position) {
        mAdapter.toggleSelection(position);
        if (mAdapter.isSelected(position)) {
            view.setBackgroundResource(R.drawable.list_row_background_selected);
        } else {
            view.setBackgroundResource(R.drawable.list_row_background);
        }
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && !mIsContextualMode) {
            // there are some selected items, start the actionMode
            startToolbarContextualActionMode(new ToolbarContextualMode());
        } else if (!hasCheckedItems && mIsContextualMode)
            // there no selected items, finish the actionMode
            finishToolbarContextualActionMode();
        else if (hasCheckedItems && mIsContextualMode) {
            if (mAdapter.getSelectedCount() > 1) {
                getActionBarToolbar().getMenu().findItem(R.id.editBudget).setVisible(false);
            } else {
                getActionBarToolbar().getMenu().findItem(R.id.editBudget).setVisible(true);
            }
        }

        if (mIsContextualMode)
            getActionBarToolbar().setTitle(String.valueOf(mAdapter
                    .getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mIsContextualMode) {
            getMenuInflater().inflate(R.menu.wallet_context_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.wallet, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
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

            case R.id.editBudget:
                if (mAdapter.getSelectedCount() == 1) {
                    int position = mAdapter.getSelectedIds().keyAt(0);
                    EditBudget(position);
                }
                return true;
            case R.id.deleteBudget:
                deleteBudgets();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (mIsContextualMode) {
            onListItemSelect(view, position);
        }
    }

    /**
     * Edit an existing Budget
     *
     * @param position
     */
    private void EditBudget(int position) {
        Budget budget = mAdapter.getItem(position);
        if (budget != null) {
            if (isModificationRuleOk(budget)) {
                Intent intent = new Intent(this, BudgetActivity.class);
                intent.putExtra(BudgetActivity.EXTRA_BUDGET_ID, budget._id);
                intent.putExtra(BudgetActivity.EXTRA_MONTH_ID, budget.monthId);
                intent.putExtra(BudgetActivity.EXTRA_AVAILABLE, budget.available);
                intent.putExtra(BudgetActivity.EXTRA_TARGET, budget.target);
                startActivityForResult(intent, REQUEST_CODE_BUDGET_MODIFY);
                finishToolbarContextualActionMode();
            }
        }
    }

    /**
     * Start the deletion of the selected products
     */
    private void deleteBudgets() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_budget_dialog_title),
                "", getResources().getString(R.string.delete_budget_dialog_cancel_text),
                getResources().getString(R.string.delete_budget_dalog_ok_text));

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    AsyncBudgetList asyncBudgetList = new AsyncBudgetList();
                    asyncBudgetList.execute();
                }
            }
        });
        alertDialog.show(getSupportFragmentManager(), "confirmationDialog");
    }

    /**
     * Check if the budget can be modified
     *
     * @param budget
     * @return
     */
    private boolean isModificationRuleOk(Budget budget) {
        boolean result = true;
        if ((budget.withDrawn > 0) || (budget.wallet > 0)) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.info_budget_title),
                    getString(R.string.info_budget_modification_message), null, getString(R.string.info_button_ok));
            alertDialogFragment.show(getSupportFragmentManager(), "errorDialog");
            result = false;
        }
        return result;
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

        switch (requestCode) {
            case BUDGET_ADD:
                if (resultCode == RESULT_OK) {
                    //load new budget data
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                }
                if (resultCode == RESULT_CANCELED) {
                    //write code if there's no result
                }
                break;
            case REQUEST_CODE_BUDGET_MODIFY:
                if (resultCode == RESULT_OK) {
                    //load updated budget
                    if (mIsContextualMode) finishToolbarContextualActionMode();
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                }
                break;
        }
    }

    @Override
    public Loader<List<Budget>> onCreateLoader(int id, Bundle args) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressCircle(this);
            mProgressDialog.show();
        }
        return new WalletLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Budget>> loader, List<Budget> data) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter = new WalletListAdapter(this, data);
                mListView.setAdapter(mAdapter);
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                if (data.size() > 0) {
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
    public void onLoaderReset(Loader<List<Budget>> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mAdapter.mBudgetListData = null;
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

    /**
     * List Adapter
     */
    public class WalletListAdapter extends ArrayAdapter {

        private Context mContext;
        private List<Budget> mBudgetListData;
        private SparseBooleanArray mSelectedItems = null;

        public WalletListAdapter(Context context, List<Budget> data) {
            super(context, R.layout.wallet_rowlayout);
            mContext = context;
            mBudgetListData = data;
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

            if (mBudgetListData != null) {
                Budget budget = mBudgetListData.get(position);
                float totalSpent = budget.withDrawn + budget.wallet;
                String currencySymbol = getCurrencySymbol();

                viewHolder.textMonth.setText(getResources().getString(R.string.textBudgetMonthName) + ": " + getMonthName(budget.monthId));
                viewHolder.textAvailable.setText(getResources().getString(R.string.textBudgetAvailable) + ": " + String.valueOf(budget.available) + " " + currencySymbol);
                viewHolder.textTarget.setText(getResources().getString(R.string.textBudgetTarget) + ": " + String.valueOf(budget.target) + " " + currencySymbol);
                viewHolder.textWithDrawn.setText(getResources().getString(R.string.textBudgetWithDrawn)
                        + ": " + String.valueOf(budget.withDrawn)
                        + " " + currencySymbol);

                viewHolder.textWallet.setText(getResources().getString(R.string.textWalletMoney)
                        + ": " + String.valueOf(budget.wallet)
                        + " " + currencySymbol);

                viewHolder.textAvailableAmount.setText(String.valueOf(budget.available));
                viewHolder.textSpentAmount.setText(String.valueOf(totalSpent));
                int height_max_in_pixels = (int) (100 * mContext.getResources().getDisplayMetrics().density + 0.5f);
                int size = (int) (totalSpent * height_max_in_pixels / budget.available);
                viewHolder.textSpentBar.setHeight(size);

                int monthDaysConsumed;
                float savingForecast;

                if (budget.monthId == Calendar.getInstance().get(Calendar.MONTH)) {
                    //The budget belongs to the current month
                    int daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                    monthDaysConsumed = Calendar.getInstance().get(Calendar.DATE);
                    if ((totalSpent / budget.available) * 100 > 25 && monthDaysConsumed > 7) {
                        //we estimate the forecast if the total spent is greater than 25%
                        //of the available money
                        float spentByDay = totalSpent / (monthDaysConsumed - 1); //we get the spent by day
                        //now we must multiply the spent by day for the days remaining in the month
                        savingForecast = budget.available - (totalSpent + (spentByDay * ((daysInMonth - monthDaysConsumed) + 1)));
                    } else {
                        //forecast = available - target because we haven't got data to estimate
                        savingForecast = budget.available - budget.target;
                    }
                } else {
                    //It is a past or future budget
                    if (totalSpent == 0) {
                        //It is a future budget, so forecast = available - target
                        savingForecast = budget.available - budget.target;
                    } else {
                        //It is a past budget, so forecast = available - totalSpent
                        savingForecast = budget.available - totalSpent;
                    }
                }
                viewHolder.textForecastAmount.setText(String.valueOf(savingForecast));
                size = (int) (savingForecast * height_max_in_pixels / budget.available);
                viewHolder.textForecastBar.setHeight(size);
            }

            if (mSelectedItems.get(position)) {
                convertView.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                convertView.setBackgroundResource(R.drawable.list_row_background);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (mBudgetListData != null) {
                return mBudgetListData.size();
            } else {
                return 0;
            }
        }

        @Override
        public Budget getItem(int position) {
            if (mBudgetListData != null) {
                Budget budget = mBudgetListData.get(position);
                return budget;
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItems.get(position));
        }

        /**
         * Return if a row is selected or not
         *
         * @param position product list position
         * @return true or false
         */
        public boolean isSelected(int position) {
            if (mSelectedItems != null) {
                return mSelectedItems.get(position);
            } else {
                return false;
            }
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

    /**
     * Contextual Toolbar callback
     */
    private class ToolbarContextualMode implements BaseActivity.ToolbarContextualMode {

        @Override
        public void onCreateToolbarContextualMode() {

        }

        @Override
        public void onFinishToolbarContextualMode() {
            mAdapter.removeSelection();
        }
    }

    /**
     * Make a delete operation for a list of task in an asynchronous way
     */
    private class AsyncBudgetList extends AsyncTask<Void, Integer, Boolean> {

        private void deleteSelectedBudgets() {
            SparseBooleanArray selected = mAdapter.mSelectedItems;
            if (mAdapter.mBudgetListData != null) {
                int total = mAdapter.mBudgetListData.size();
                UseCaseBudget useCaseBudget = new UseCaseBudget(WalletActivity.this);
                for (int index = total - 1; index >= 0; index--) {
                    if (selected.get(index)) {
                        int id = mAdapter.mBudgetListData.get(index)._id;
                        useCaseBudget.delete(id);
                    }
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            deleteSelectedBudgets();
            return new Boolean(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mIsContextualMode) {
                finishToolbarContextualActionMode();
            }
            getContentResolver().notifyChange(ShoppingListContract.BudgetEntry.CONTENT_URI, null);
        }
    }

}


