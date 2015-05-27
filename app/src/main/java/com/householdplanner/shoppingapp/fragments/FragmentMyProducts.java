package com.householdplanner.shoppingapp.fragments;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applilandia.widget.CircleView;
import com.applilandia.widget.SnackBar;
import com.householdplanner.shoppingapp.BaseActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.listeners.RecyclerViewClickListener;
import com.householdplanner.shoppingapp.loaders.ProductHistoryLoader;
import com.householdplanner.shoppingapp.models.ProductHistory;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.usecases.UseCaseMyProducts;

import java.util.List;

public class FragmentMyProducts extends Fragment implements LoaderCallbacks<List<ProductHistory>>, OnFragmentProgress {

    private final static String LOG_TAG = FragmentMyProducts.class.getSimpleName();

    private static final int LOADER_ID = 1;

    //To save if the Toolbar is in contextual mode
    private boolean mContextualMode = false;
    //Adapter for the Recycler View
    private MyProductsAdapter mAdapter = null;
    private RecyclerView mRecyclerViewMyProducts;
    //Vertical Layout Manager
    private RecyclerView.LayoutManager mLayoutManager;
    //Action Bar mode

    //Callback to trigger the events
    private OnLoadData mCallback = null;
    //SnackBar
    private SnackBar mSnackBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_products, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        //Inflate the views into the module vars
        inflateViews();
        //Init the recycler View
        initRecyclerView();
        //Init the load
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Inflate the existing views in the Fragment
     */
    private void inflateViews() {
        mRecyclerViewMyProducts = (RecyclerView) getView().findViewById(R.id.recyclerViewMyProducts);
        mSnackBar = (SnackBar) getView().findViewById(R.id.snackBarMyProducts);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Init the recycler view
     */
    private void initRecyclerView() {
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mRecyclerViewMyProducts.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerViewMyProducts.setLayoutManager(mLayoutManager);
        mRecyclerViewMyProducts.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewMyProducts.addItemDecoration(new MyProductsItemDecoration());

        mRecyclerViewMyProducts.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(),
                        new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {

                            @Override
                            public void onItemClick(View view, int position) {
                                if (mContextualMode) {
                                    onListItemSelect(view, position);
                                }
                            }

                            @Override
                            public void onItemSecondaryActionClick(View view, final int position) {
                                final AppCompatCheckBox checkBoxActionIcon = (AppCompatCheckBox) view;
                                if (mSnackBar.getVisibility() == View.GONE) {
                                    //The snack bar is not displayed yet, so we have to show it
                                    checkBoxActionIcon.setChecked(true);
                                    mSnackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                                        @Override
                                        public void onClose() {
                                            ProductHistory productHistory = mAdapter.mProductHistoryListData.get(position);
                                            if (productHistory != null) {
                                                mAdapter.mProductHistoryListData.remove(position);
                                                mAdapter.notifyItemRangeRemoved(position, 1);
                                                UseCaseMyProducts useCaseMyProducts = new UseCaseMyProducts(getActivity());
                                                if (useCaseMyProducts.copyToShoppingList(productHistory)) {
                                                    getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onUndo() {
                                            //User click on Undo action
                                            checkBoxActionIcon.setChecked(false);
                                        }
                                    });
                                    mSnackBar.show(R.string.text_snack_bar_move_myproduct);
                                } else {
                                    //SnackBar is visible, but the user instead pushing on UNDO action,
                                    //Unchecked the checkbox, therefore the action has to be undone
                                    checkBoxActionIcon.setChecked(false);
                                    mSnackBar.undo();
                                }
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                if (!mContextualMode) {
                                    onListItemSelect(view, position);
                                }
                            }
                        })
        );
    }

    /**
     * Manage the functionality when one row is clicked one
     *
     * @param view     row
     * @param position row position
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

        if (hasCheckedItems && !mContextualMode)
            // there are some selected items, start the actionMode
            baseActivity.startToolbarContextualActionMode(new ToolbarContextualMode());
        else if (!hasCheckedItems && mContextualMode)
            // there no selected items, finish the actionMode
            baseActivity.finishToolbarContextualActionMode();

        if (mContextualMode)
            baseActivity.getActionBarToolbar().setTitle(String.valueOf(mAdapter.getSelectedCount()) + " " + getResources().getString(R.string.textSelected));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mContextualMode) {
            inflater.inflate(R.menu.history_context_menu, menu);
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteProduct:
                deleteProducts();
                return true;

            default:
                return false;
        }
    }

    /**
     * Start the deletion of the selected products
     */
    private void deleteProducts() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_product_dialog_title),
                "", getResources().getString(R.string.delete_product_dialog_cancel_text),
                getResources().getString(R.string.delete_product_dalog_ok_text));

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    AsyncMyProducts asyncMyProducts = new AsyncMyProducts();
                    asyncMyProducts.execute();
                }
            }
        });
        alertDialog.show(getFragmentManager(), "confirmationDialog");
    }

    @Override
    public Loader<List<ProductHistory>> onCreateLoader(int id, Bundle args) {
        if (mCallback != null) mCallback.onLoadStart();
        return new ProductHistoryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ProductHistory>> loader, List<ProductHistory> data) {
        if (mCallback != null) {
            int items = 0;
            if (data != null) items = data.size();
            mCallback.onLoadFinish(items, null);
        }
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use.
                mAdapter = new MyProductsAdapter(data);
                mRecyclerViewMyProducts.setAdapter(mAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ProductHistory>> loader) {
        mRecyclerViewMyProducts.setAdapter(null);
        mAdapter = null;
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

    public class MyProductsAdapter extends RecyclerView.Adapter<MyProductsAdapter.ViewHolder> {

        private List<ProductHistory> mProductHistoryListData;
        private SparseBooleanArray mSelectedItems;

        public MyProductsAdapter(List<ProductHistory> data) {
            mProductHistoryListData = data;
            mSelectedItems = new SparseBooleanArray();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public CircleView mCircleAvatar;
            public TextView mTextName;
            public AppCompatCheckBox mCheckBoxProduct;

            public ViewHolder(View itemView) {
                super(itemView);
                mCircleAvatar = (CircleView) itemView.findViewById(R.id.circleAvatar);
                mTextName = (TextView) itemView.findViewById(R.id.textProduct);
                mCheckBoxProduct = (AppCompatCheckBox) itemView.findViewById(R.id.imageSecondaryActionIcon);
            }
        }

        @Override
        public MyProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.usual_rowlayout, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyProductsAdapter.ViewHolder holder, int position) {
            ProductHistory productHistory = mProductHistoryListData.get(position);
            //Set color in avatar
            String marketName = productHistory.marketName;
            if (marketName != null) {
                MarketRepository marketRepository = new MarketRepository(getActivity());
                Integer color = marketRepository.getMarketColor(marketName);
                marketRepository.close();
                if (color != null) {
                    holder.mCircleAvatar.setColor(color);
                } else {
                    holder.mCircleAvatar.setColor(getResources().getColor(android.R.color.transparent));
                }
            } else {
                holder.mCircleAvatar.setColor(getResources().getColor(android.R.color.transparent));
            }
            //Set product name
            holder.mTextName.setText(productHistory.name);
            if (mSelectedItems.get(position)) {
                holder.itemView.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.list_row_background);
            }
            //Set action checkbox
            holder.mCheckBoxProduct.setChecked(false);
        }

        @Override
        public int getItemCount() {
            if (mProductHistoryListData != null) {
                return mProductHistoryListData.size();
            } else {
                return 0;
            }
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

        /**
         * Return the selected rows number
         *
         * @return
         */
        public int getSelectedCount() {
            return mSelectedItems.size();
        }

        /**
         * Toggle the selection of the row in a position
         *
         * @param position row to toggle the selection
         */
        public void toggleSelection(int position) {
            selectView(position, !mSelectedItems.get(position));
        }

        /**
         * Set the item in the array as selected or not
         *
         * @param position row number
         * @param value    set selected value
         */
        public void selectView(int position, boolean value) {
            if (value)
                mSelectedItems.put(position, value);
            else
                mSelectedItems.delete(position);
        }

        /**
         * Clear all selected items
         */
        public void clearSelection() {
            mSelectedItems.clear();
            mSelectedItems = new SparseBooleanArray();
        }
    }

    /**
     * Inner class for recycler view item decoration
     */
    public class MyProductsItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration {
        Drawable mDivider;

        public MyProductsItemDecoration() {
            mDivider = ResourcesCompat.getDrawable(getResources(), R.drawable.list_row_background, null);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildLayoutPosition(view) < 1) return;
            if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayout.VERTICAL) {
                outRect.top = mDivider.getIntrinsicHeight();
            } else {
                return;
            }
        }
    }

    /**
     * Toolbar Contextual Mode Callback
     */
    private class ToolbarContextualMode implements BaseActivity.ToolbarContextualMode {

        @Override
        public void onCreateToolbarContextualMode() {
            mContextualMode = true;
        }

        @Override
        public void onFinishToolbarContextualMode() {
            mContextualMode = false;
            mAdapter.clearSelection();
        }
    }

    /**
     * Make a delete operation for a list of task in an asynchronous way
     */
    private class AsyncMyProducts extends AsyncTask<Void, Integer, Boolean> {

        private void deleteSelectedProducts() {
            SparseBooleanArray selected = mAdapter.mSelectedItems;
            if (mAdapter.mProductHistoryListData != null) {
                int total = mAdapter.mProductHistoryListData.size();
                ProductHistoryRepository historyRepository = new ProductHistoryRepository(getActivity());
                for (int index = total - 1; index >= 0; index--) {
                    if (selected.get(index)) {
                        int id = mAdapter.mProductHistoryListData.get(index)._id;
                        historyRepository.deleteProduct(id);
                        publishProgress(new Integer(index));
                    }
                }
                historyRepository.close();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            deleteSelectedProducts();
            return new Boolean(true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values != null) {
                if (values.length > 0) {
                    int position = values[0].intValue();
                    mAdapter.mProductHistoryListData.remove(position);
                    mAdapter.notifyItemRangeRemoved(position, 1);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mContextualMode) {
                ((BaseActivity)getActivity()).finishToolbarContextualActionMode();
            }
            getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
        }
    }


}
