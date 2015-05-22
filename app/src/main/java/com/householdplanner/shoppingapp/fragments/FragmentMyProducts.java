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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applilandia.widget.CircleView;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
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

    private MyProductsAdapter mAdapter = null;
    private RecyclerView mRecyclerViewMyProducts;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionMode mActionMode;
    private OnLoadData mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_products, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        initRecyclerView();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void initRecyclerView() {
        mRecyclerViewMyProducts = (RecyclerView) getView().findViewById(R.id.recyclerViewMyProducts);
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
                                if (mActionMode != null) {
                                    onListItemSelect(view, position);
                                }
                            }

                            @Override
                            public void onItemSecondaryActionClick(View view, int position) {
                                AppCompatCheckBox checkBoxActionIcon = (AppCompatCheckBox) view;
                                checkBoxActionIcon.setChecked(!checkBoxActionIcon.isChecked());
                                ProductHistory productHistory = mAdapter.mProductHistoryListData.get(position);
                                if (productHistory != null) {
                                    mAdapter.mProductHistoryListData.remove(position);
                                    mAdapter.notifyItemRangeRemoved(position, 1);
                                    UseCaseMyProducts useCaseMyProducts = new UseCaseMyProducts(getActivity());
                                    if (useCaseMyProducts.copyToShoppingList(productHistory)) {
                                        getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
                                        Toast.makeText(getActivity(), R.string.textProductsAdded, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                if (mActionMode == null) {
                                    onListItemSelect(view, position);
                                }
                            }
                        })
        );
    }


    private void onListItemSelect(View view, int position) {
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
            notifyDataSetChanged();
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
            mAdapter.clearSelection();
            mActionMode = null;
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
            if (mActionMode != null) {
                mActionMode.finish();
                mActionMode = null;
            }
            getActivity().getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
        }
    }


}
