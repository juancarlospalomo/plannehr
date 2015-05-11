package com.householdplanner.shoppingapp.fragments;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

import com.householdplanner.shoppingapp.MarketListActivity;
import com.householdplanner.shoppingapp.ProductActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.help.HelpActivityFrame;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;
import com.householdplanner.shoppingapp.views.HelpView;
import com.householdplanner.shoppingapp.views.HelpView.OnHelpViewClick;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;

import java.util.ArrayList;

public class FragmentEnterList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnItemClickListener, OnFragmentProgress {

    public static final String TAG_FRAGMENT = "fragmentEnterList";

    private static final int LOADER_ID = 1;
    private static final int SELECT_MARKET_FOR_MOVE = 2;
    private static final int EDIT_PRODUCT = 3;
    private static final String KEY_ITEMS_SELECTED = "items";
    private static final String KEY_SELECT_ITEM = "selectItem";

    private static String mSelectItemName = null;
    private EnterListAdapter mAdapter;
    private ListView mListView;
    private ActionMode mActionMode;
    private ArrayList<Integer> mItemsSelected = null;
    private OnLoadData mCallback = null;
    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;

    public FragmentEnterList() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enter_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mItemsSelected = savedInstanceState.getIntegerArrayList(KEY_ITEMS_SELECTED);
            mSelectItemName = savedInstanceState.getString(KEY_SELECT_ITEM);
        }
        LoadProductList();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSelectItemName != null) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_MARKET_FOR_MOVE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String marketName = bundle.getString(MarketListActivity.EXTRA_MARKET_NAME);
                    moveSelectedToTarget(marketName);
                }
                break;
        }
    }

    @Override
    public void setOnLoadData(OnLoadData callback) {
        mCallback = callback;
    }

    private void actionMoveSelectedToTarget() {
        showTargetMarket();
    }

    private ArrayList<Integer> getProductsSelected() {
        SparseBooleanArray selected = mAdapter.getSelectedIds();
        ArrayList<Integer> items = null;
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null) {
            int total = cursor.getCount();
            if (selected.size() > 0) items = new ArrayList<Integer>();
            for (int index = 0; index < total; index++) {
                cursor.moveToPosition(index);
                if (selected.get(index)) {
                    int id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
                    items.add((Integer) id);
                }
            }
        }
        return items;
    }

    private int getItemPosition(Cursor cursor, String name) {
        int result = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                if (productName.equals(name)) {
                    result = cursor.getPosition();
                    break;
                }
                cursor.moveToNext();
            }
        }
        return result;
    }

    private void DeleteProduct(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int id = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID));
        ConfirmationDialog confirmationDialog = new ConfirmationDialog();
        Bundle args = new Bundle();
        args.putInt("id", id);
        String message = getResources().getString(R.string.textProductDeleteWarning);
        args.putString("message", message);
        String title = getResources().getString(R.string.deleteProduct);
        title += " " + cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
        args.putString("title", title);
        confirmationDialog.setArguments(args);
        confirmationDialog.callback = this;
        confirmationDialog.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick(int id) {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(getActivity());
        if (shoppingListRepository.deleteProductItem(id)) {
            getActivity().getContentResolver().notifyChange(AppPreferences.URI_HISTORY_TABLE, null);
            getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
        }
        shoppingListRepository.close();
    }

    private void EditProduct(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        if (cursor != null) {
            Intent intent = new Intent(this.getActivity(), ProductActivity.class);
            intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_ID)));
            intent.putExtra(ProductActivity.EXTRA_PRODUCT_NAME, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME)));
            intent.putExtra(ProductActivity.EXTRA_MARKET_NAME, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET)));
            intent.putExtra(ProductActivity.EXTRA_AMOUNT, cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT)));
            intent.putExtra(ProductActivity.EXTRA_UNIT_ID, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID)));
            intent.putExtra(ProductActivity.EXTRA_CATEGORY, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY)));
            startActivityForResult(intent, EDIT_PRODUCT);
        }
    }

    private void LoadProductList() {
        String[] fields = new String[]{ShoppingListStore.COLUMN_PRODUCT_NAME};
        int[] listViewColumns = new int[]{R.id.label};

        try {
            mListView = (ListView) getView().findViewById(R.id.listview01);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_ID, null, this);
            mAdapter = new EnterListAdapter(this.getActivity(), R.layout.rowlayout, null,
                    fields, listViewColumns);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int position, long id) {
                    if (mActionMode == null) {
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

    private void moveSelectedToTarget(String targetMarket) {
        if (mItemsSelected != null) {
            ShoppingListRepository listRepository = new ShoppingListRepository(getActivity());
            for (int index = 0; index < mItemsSelected.size(); index++) {
                int id = mItemsSelected.get(index).intValue();
                listRepository.moveToSupermaket(id, targetMarket);
            }
            listRepository.close();
            mItemsSelected = null;
        }
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode != null) {
            onListItemSelect(position);
        }
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mCallback != null) mCallback.onLoadStart();
        return new ShoppingListCursorLoader(getActivity());
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_ID) {
            mAdapter.swapCursor(cursor);
            if (mSelectItemName != null) {
                final int position = getItemPosition(cursor, mSelectItemName);
                mListView.post(new Runnable() {
                    //push at the end of queue message and will be processed
                    //after listview is loaded.
                    @Override
                    public void run() {
                        mListView.setSelection(position);
                    }
                });
                mSelectItemName = null;
            }
        }
        if (mCallback != null) {
            int items = 0;
            if (cursor != null) {
                items = cursor.getCount();
                HelpView helpView = (HelpView) getView().findViewById(R.id.viewCapsules);
                if (items == 0) {
                    helpView.setVisibility(View.VISIBLE);
                    helpView.setOnHelpViewClick(new OnHelpViewClick() {
                        @Override
                        public void onCapsuleClick(TypeView capsule) {
                            Intent intent = new Intent(getActivity(), HelpActivityFrame.class);
                            intent.putExtra(HelpActivityFrame.EXTRA_INITIAL_CAPSULE, capsule.getValue());
                            startActivity(intent);
                        }
                    });
                } else {
                    helpView.setVisibility(View.GONE);
                }
            }
            mCallback.onLoadFinish(items, TAG_FRAGMENT);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mItemsSelected != null) {
            outState.putIntegerArrayList(KEY_ITEMS_SELECTED, mItemsSelected);
        }
        if (mSelectItemName != null) {
            outState.putString(KEY_SELECT_ITEM, mSelectItemName);
        }
    }

    public void setProductVisible(String name) {
        mSelectItemName = name;
    }

    private void showTargetMarket() {
        Intent intent = new Intent(getActivity(), MarketListActivity.class);
        intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_ALL, true);
        intent.putExtra(MarketListActivity.IN_EXTRA_SHOW_CHECK_NO_MARKET, false);
        startActivityForResult(intent, SELECT_MARKET_FOR_MOVE);
    }

    static class ViewHolder {
        public ImageView imageMarket;
        public TextView text;
        public ImageView image;
        public ImageView imageDelete;
    }

    public class EnterListAdapter extends SimpleCursorAdapter {

        Context mContext;
        Cursor mCursor;
        private SparseBooleanArray mSelectedItems;

        public EnterListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
            mContext = context;
            mCursor = c;
            mSelectedItems = new SparseBooleanArray();
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final int pos = position;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageMarket = (ImageView) convertView.findViewById(R.id.imageSuperMarket);
                viewHolder.text = (TextView) convertView.findViewById(R.id.label);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageRightArrow);
                viewHolder.imageDelete = (ImageView) convertView.findViewById(R.id.imageDelete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditProduct(pos);
                }
            });

            viewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteProduct(pos);
                }
            });

            mCursor.moveToPosition(position);
            String marketName = mCursor.getString(mCursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
            viewHolder.imageMarket.setVisibility(View.VISIBLE);
            if (marketName != null) {
                MarketRepository marketRepository = new MarketRepository(getActivity());
                Integer color = marketRepository.getMarketColor(marketName);
                marketRepository.close();
                if (color != null) {
                    viewHolder.imageMarket.setImageDrawable(null);
                    GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_white);
                    gradientDrawable.mutate();
                    gradientDrawable.setColor(color);
                    viewHolder.imageMarket.setImageDrawable(gradientDrawable);
                } else {
                    viewHolder.imageMarket.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_market));
                }
            } else {
                viewHolder.imageMarket.setImageDrawable(null);
                GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.square_white);
                viewHolder.imageMarket.setImageDrawable(gradientDrawable);
            }

            viewHolder.text.setText(util.getCompleteProductRow(mContext, mCursor, false));
            viewHolder.image.setImageResource(R.drawable.ic_action_edit);
            viewHolder.imageDelete.setVisibility(View.VISIBLE);
            if (mSelectedItems.get(position)) {
                viewHolder.text.setTextColor(getResources().getColor(android.R.color.white));
                viewHolder.text.setTypeface(font.getListItemSelectedFont(getActivity()));
                convertView.setBackgroundColor(getResources().getColor(R.color.rowSelected));
            } else {
                viewHolder.text.setTextColor(getResources().getColor(android.R.color.black));
                viewHolder.text.setTypeface(font.getListItemFont(getActivity()));
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

    private static class ShoppingListCursorLoader extends CursorLoader {

        // We hold a reference to the Loader�s data here.
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
        /**
         * ************************************************
         */
        @Override
        public Cursor loadInBackground() {
            // This method is called on a background thread and should generate a
            // new set of data to be delivered back to the client.
            mShoppingListRepository = new ShoppingListRepository(this.getContext());
            mCursor = mShoppingListRepository.getProductsNoCommittedByMarket(null);
            if (mCursor != null) {
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
                mShoppingListRepository.close();
            }
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.current_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.moveProduct:
                    mItemsSelected = getProductsSelected();
                    actionMoveSelectedToTarget();
                    mode.finish();
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

        public Fragment callback;

        public ConfirmationDialog() {
            super();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final int id = getArguments().getInt("id");
            String message = getArguments().getString("message");
            String title = getArguments().getString("title");
            Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((FragmentEnterList) callback).doPositiveClick(id);
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
