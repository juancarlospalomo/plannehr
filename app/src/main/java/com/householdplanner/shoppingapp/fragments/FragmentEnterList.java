package com.householdplanner.shoppingapp.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.applilandia.widget.CircleView;
import com.householdplanner.shoppingapp.BaseActivity;
import com.householdplanner.shoppingapp.MarketListActivity;
import com.householdplanner.shoppingapp.ProductActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.listeners.RecyclerViewClickListener;
import com.householdplanner.shoppingapp.loaders.ProductLoader;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.repositories.MarketRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.usecases.UseCaseMyProducts;

import java.util.ArrayList;
import java.util.List;

public class FragmentEnterList extends Fragment implements LoaderManager.LoaderCallbacks<List<Product>>,
        OnFragmentProgress {

    private static final String LOG_TAG = FragmentEnterList.class.getSimpleName();

    public static final String TAG_FRAGMENT = "fragmentEnterList";

    private static final int LOADER_ID = 1;
    private static final int REQUEST_CODE_MARKET_FOR_MOVE = 2;
    private static final int EDIT_PRODUCT = 3;
    private static final String KEY_ITEMS_SELECTED = "items";
    private static final String KEY_SELECT_ITEM = "selectItem";

    //To save if the Toolbar is in contextual mode
    private boolean mContextualMode = false;
    private static String mSelectItemName = null;
    private EnterListAdapter mAdapter;
    private RecyclerView mProductRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Integer> mItemsSelected = null;
    private OnLoadData mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enter_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mItemsSelected = savedInstanceState.getIntegerArrayList(KEY_ITEMS_SELECTED);
            mSelectItemName = savedInstanceState.getString(KEY_SELECT_ITEM);
        }
        initRecyclerView();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mContextualMode) {
            inflater.inflate(R.menu.current_list, menu);
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

            case R.id.moveProduct:
                mItemsSelected = getProductsSelected();
                actionMoveSelectedToTarget();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_MARKET_FOR_MOVE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    int marketId = bundle.getInt(MarketListActivity.EXTRA_MARKET_ID);
                    moveSelectedToTarget(marketId);
                }
                ((BaseActivity) getActivity()).finishToolbarContextualActionMode();
                break;
        }
    }

    private void initRecyclerView() {
        mProductRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewProductList);
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mProductRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mProductRecyclerView.setLayoutManager(mLayoutManager);
        mProductRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mProductRecyclerView.addItemDecoration(new EnterListItemDecoration());

        mProductRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(),
                        new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {

                            @Override
                            public void onItemClick(View view, int position) {
                                if (mContextualMode) {
                                    onListItemSelect(view, position);
                                }
                            }

                            @Override
                            public void onItemSecondaryActionClick(View view, int position) {
                                EditProduct(position);
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

    @Override
    public void setOnLoadData(OnLoadData callback) {
        mCallback = callback;
    }

    /**
     * Start the activity for selecting a market for the selected products
     */
    private void actionMoveSelectedToTarget() {
        showTargetMarket();
    }

    /**
     * Start the deletion of the selected products
     */
    private void deleteProducts() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_product_dialog_title),
                "", getResources().getString(R.string.delete_product_dialog_cancel_text),
                getResources().getString(R.string.delete_product_dalog_ok_text), null);

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    AsyncProductList asyncProductList = new AsyncProductList();
                    asyncProductList.execute();
                }
            }
        });
        alertDialog.show(getFragmentManager(), "confirmationDialog");
    }

    /**
     * Edit the product set in one position
     *
     * @param position position of the row
     */
    private void EditProduct(int position) {
        Product product = mAdapter.mProductListData.get(position);
        if (product != null) {
            Intent intent = new Intent(this.getActivity(), ProductActivity.class);
            intent.putExtra(ProductActivity.EXTRA_ID, product._id);
            intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, product.productId);
            intent.putExtra(ProductActivity.EXTRA_PRODUCT_NAME, product.name);
            intent.putExtra(ProductActivity.EXTRA_MARKET_NAME, product.marketName);
            intent.putExtra(ProductActivity.EXTRA_AMOUNT, product.amount);
            intent.putExtra(ProductActivity.EXTRA_UNIT_ID, product.unitId);
            startActivityForResult(intent, EDIT_PRODUCT);
        }
    }

    /**
     * Set the market to the selected products
     *
     * @param targetMarket market
     */
    private void moveSelectedToTarget(int targetMarket) {
        if (mItemsSelected != null) {
            UseCaseMyProducts useCaseMyProducts = new UseCaseMyProducts(getActivity());
            for (int index = 0; index < mItemsSelected.size(); index++) {
                int id = mItemsSelected.get(index).intValue();
                useCaseMyProducts.moveToSupermarket(id, targetMarket);
            }
            mItemsSelected = null;
        }
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * Execute the right flow when one row is clicked
     *
     * @param position row position
     */
    private void onListItemSelect(View view, int position) {
        if (view!=null) {
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
    }

    /**
     * Get the selected rows
     *
     * @return array with the selected product´s id
     */
    private ArrayList<Integer> getProductsSelected() {
        SparseBooleanArray selected = mAdapter.mSelectedItems;
        ArrayList<Integer> items = null;
        List<Product> data = mAdapter.mProductListData;
        if (data != null) {
            int total = data.size();
            if (selected.size() > 0) items = new ArrayList<Integer>();
            for (int index = 0; index < total; index++) {
                if (selected.get(index)) {
                    int id = data.get(index)._id;
                    items.add((Integer) id);
                }
            }
        }
        return items;
    }

    @Override
    public Loader<List<Product>> onCreateLoader(int id, Bundle args) {
        if (mCallback != null) mCallback.onLoadStart();
        return new ProductLoader(getActivity(), ProductLoader.TypeProducts.All);
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> data) {
        if (loader.getId() == LOADER_ID) {
            mAdapter = new EnterListAdapter(data);
            mProductRecyclerView.setAdapter(mAdapter);
            if (!TextUtils.isEmpty(mSelectItemName)) {
                int position = mAdapter.findPositionByName(mSelectItemName);
                if (position != -1) {
                    mLayoutManager.scrollToPosition(position);
                }
            }
        }
        if (mCallback != null) {
            int items = 0;
            if (data != null) {
                items = data.size();
                if (items == 0) {
                    setVisibleEmptyList(true);
                } else {
                    setVisibleEmptyList(false);
                }
            }
            mCallback.onLoadFinish(items, TAG_FRAGMENT);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Product>> loader) {
        mProductRecyclerView.setAdapter(null);
        mAdapter = null;
    }

    /**
     * Show or hide the empty list image
     *
     * @param value true will show the empty list image
     */
    private void setVisibleEmptyList(boolean value) {
        ImageView imageViewEmptyList = (ImageView) getView().findViewById(R.id.imageViewEmptyList);
        if (value) {
            imageViewEmptyList.setVisibility(View.VISIBLE);
        } else {
            imageViewEmptyList.setVisibility(View.GONE);
        }
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

    /**
     * Set the feedback animation for product with name = name
     *
     * @param name product name
     */
    public void setProductVisible(String name) {
        mSelectItemName = name;
        if (mAdapter != null) {
            int position = mAdapter.findPositionByName(name);
            if (position != -1) {
                //It has been found
                EnterListAdapter.ViewHolder viewHolder = (EnterListAdapter.ViewHolder) mProductRecyclerView.findViewHolderForAdapterPosition(position);
                mAdapter.animateRowInserted(viewHolder.itemView);
                mLayoutManager.scrollToPosition(position);
            }
        }
    }

    /**
     * Show the list of existing markets
     */
    private void showTargetMarket() {
        Intent intent = new Intent(getActivity(), MarketListActivity.class);
        intent.putExtra(MarketListActivity.EXTRA_SHOW_ALL_MARKETS, true);
        intent.putExtra(MarketListActivity.EXTRA_SHOW_CHECK_NO_MARKET, false);
        startActivityForResult(intent, REQUEST_CODE_MARKET_FOR_MOVE);
    }

    /**
     * RecyclerView adapter for product list
     */
    public class EnterListAdapter extends RecyclerView.Adapter<EnterListAdapter.ViewHolder> {

        List<Product> mProductListData;
        private SparseBooleanArray mSelectedItems;

        public EnterListAdapter(List<Product> data) {
            mProductListData = data;
            mSelectedItems = new SparseBooleanArray();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CircleView mImageAvatar;
            public AppCompatTextView mText;
            public ImageView mImageEdit;

            public ViewHolder(View itemView) {
                super(itemView);
                mImageAvatar = (CircleView) itemView.findViewById(R.id.imageAvatar);
                mText = (AppCompatTextView) itemView.findViewById(R.id.textProduct);
                mImageEdit = (ImageView) itemView.findViewById(R.id.imageSecondaryActionIcon);
            }
        }

        /**
         * Create view holder
         *
         * @param viewGroup
         * @param position
         * @return
         */
        @Override
        public EnterListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View view = FragmentEnterList.this.getActivity().getLayoutInflater().inflate(R.layout.rowlayout, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        /**
         * Replace the contents of a list item when it is called from the LayoutManager
         *
         * @param viewHolder: ViewHolder containing the views
         * @param position:   position from the Dataset to be showed
         */
        @Override
        public void onBindViewHolder(EnterListAdapter.ViewHolder viewHolder, final int position) {

            viewHolder.mImageEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditProduct(position);
                }
            });

            Product product = mProductListData.get(position);
            String marketName = product.marketName;

            if (marketName != null) {
                MarketRepository marketRepository = new MarketRepository(getActivity());
                Integer color = marketRepository.getMarketColor(marketName);
                marketRepository.close();
                if (color != null) {
                    viewHolder.mImageAvatar.setColor(color);
                } else {
                    viewHolder.mImageAvatar.setColor(getResources().getColor(android.R.color.transparent));
                }
            } else {
                viewHolder.mImageAvatar.setColor(getResources().getColor(android.R.color.transparent));
            }

            viewHolder.mText.setText(product.name);
            viewHolder.mImageEdit.setImageResource(R.drawable.ic_edit);
            if (mSelectedItems.get(position)) {
                viewHolder.itemView.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                viewHolder.itemView.setBackgroundResource(R.drawable.list_row_background);
            }
            if (!TextUtils.isEmpty(mSelectItemName) && mSelectItemName.equals(product.name)) {
                animateRowInserted(viewHolder.itemView);
                mLayoutManager.scrollToPosition(position);
                mSelectItemName = null;
            }
        }

        @Override
        public int getItemCount() {
            if (mProductListData != null) {
                return mProductListData.size();
            } else {
                return 0;
            }
        }

        /**
         * Find the position in the adapter of the row with product name = name
         *
         * @param name product name
         * @return return position or -1 if it isn't found
         */
        public int findPositionByName(String name) {
            for (int index = 0; index < mProductListData.size(); index++) {
                if (mProductListData.get(index).name.equals(name)) {
                    return index;
                }
            }
            return -1;
        }

        /**
         * Animate a view to emergence
         *
         * @param view it is the row to animate
         */
        private void animateRowInserted(final View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                view.setAlpha(0f);
                view.animate().alpha(1f).setDuration(1000)
                        .start();
            } else {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.row_inserted);
                view.startAnimation(animation);
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
            Log.v(LOG_TAG, "clearSelection");
            mSelectedItems.clear();
            mSelectedItems = new SparseBooleanArray();
            notifyDataSetChanged();
        }
    }

    public class EnterListItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration {
        Drawable mDivider;

        public EnterListItemDecoration() {
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
    private class AsyncProductList extends AsyncTask<Void, Integer, Boolean> {

        private void deleteSelectedProducts() {
            SparseBooleanArray selected = mAdapter.mSelectedItems;
            if (mAdapter.mProductListData != null) {
                int total = mAdapter.mProductListData.size();
                ShoppingListRepository shoppingListRepository = new ShoppingListRepository(getActivity());
                for (int index = total - 1; index >= 0; index--) {
                    if (selected.get(index)) {
                        int id = mAdapter.mProductListData.get(index)._id;
                        shoppingListRepository.deletePermanentProductItem(id);
                        publishProgress(new Integer(index));
                    }
                }
                shoppingListRepository.close();
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
                    mAdapter.mProductListData.remove(position);
                    mAdapter.notifyItemRangeRemoved(position, 1);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mContextualMode) {
                ((BaseActivity) getActivity()).finishToolbarContextualActionMode();
            }
            if (mAdapter.mProductListData.size() == 0) {
                setVisibleEmptyList(true);
            }
            getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
        }
    }

}
