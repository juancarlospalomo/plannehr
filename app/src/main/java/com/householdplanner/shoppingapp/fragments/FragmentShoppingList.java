package com.householdplanner.shoppingapp.fragments;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.applilandia.widget.SnackBar;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.listeners.RecyclerViewClickListener;
import com.householdplanner.shoppingapp.loaders.ProductLoader;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

import java.util.List;

public class FragmentShoppingList extends Fragment implements LoaderManager.LoaderCallbacks<List<Product>>,
        OnFragmentProgress {

    private static final String LOG_TAG = FragmentShoppingList.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private static String mMarketName;
    private RecyclerView mRecyclerViewShoppingList;
    private RecyclerView.LayoutManager mLayoutManager;
    private SnackBar mSnackBar;
    private ShoppingListAdapter mAdapter;
    private OnLoadData mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Inflate the views into the module vars
        inflateViews();
        //Init the recycler View
        initRecyclerView();
        loadMarketData();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Inflate the existing views in the Fragment
     */
    private void inflateViews() {
        mRecyclerViewShoppingList = (RecyclerView) getView().findViewById(R.id.recyclerViewShoppingList);
        mSnackBar = (SnackBar) getView().findViewById(R.id.snackBarShoppingList);
    }

    /**
     * Load the current market data
     */
    private void loadMarketData() {
        mMarketName = AppGlobalState.getInstance().getMarketName(getActivity());
    }

    /**
     * Init the recycler view
     */
    private void initRecyclerView() {
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mRecyclerViewShoppingList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerViewShoppingList.setLayoutManager(mLayoutManager);
        mRecyclerViewShoppingList.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewShoppingList.addItemDecoration(new ShoppingListItemDecoration());

        mRecyclerViewShoppingList.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(),
                        new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {

                            @Override
                            public void onItemClick(View view, int position) {

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
                                            Product product = mAdapter.mProductDataList.get(position);
                                            if (product != null) {
                                                mAdapter.mProductDataList.remove(position);
                                                mAdapter.notifyItemRangeRemoved(position, 1);
                                                UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                                useCaseShoppingList.moveToBasket(product);
                                                getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
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
                            }
                        })
        );
    }

    @Override
    public Loader<List<Product>> onCreateLoader(int id, Bundle args) {
        if (mCallback != null) mCallback.onLoadStart();
        boolean completedList = util.getShowProductsNotSet(getActivity());
        if (completedList || TextUtils.isEmpty(mMarketName)) {
            return new ProductLoader(getActivity(), ProductLoader.TypeProducts.InShoppingList);
        } else {
            return new ProductLoader(getActivity(), ProductLoader.TypeProducts.InShoppingListBySupermarket, mMarketName);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> data) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        if (mCallback != null) {
            int items = 0;
            if (data != null) items = data.size();
            mCallback.onLoadFinish(items, null);
        }
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter = new ShoppingListAdapter(data);
                mRecyclerViewShoppingList.setAdapter(mAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Product>> cursor) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mRecyclerViewShoppingList.setAdapter(null);
        mAdapter = null;
    }

    @Override
    public void setOnLoadData(OnLoadData callback) {
        mCallback = callback;
    }

    public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

        private List<Product> mProductDataList;

        public ShoppingListAdapter(List<Product> data) {
            mProductDataList = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public AppCompatTextView mTextViewPrimary;
            public AppCompatTextView mTextViewSecondary;
            public AppCompatCheckBox mSecondaryActionIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextViewPrimary = (AppCompatTextView) itemView.findViewById(R.id.textview_primary_text);
                mTextViewSecondary = (AppCompatTextView) itemView.findViewById(R.id.textview_secondary_text);
                mSecondaryActionIcon = (AppCompatCheckBox) itemView.findViewById(R.id.imageSecondaryActionIcon);
            }
        }


        @Override
        public ShoppingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.shopping_rowlayout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShoppingListAdapter.ViewHolder viewHolder, int position) {
            if (mProductDataList != null) {
                Product product = mProductDataList.get(position);
                if (product != null) {
                    viewHolder.mTextViewPrimary.setText(product.name);
                    viewHolder.mTextViewSecondary.setText(product.amount + " " + util.getMeasureUnitName(getActivity(),
                            product.unitId, product.amount));
                    viewHolder.mSecondaryActionIcon.setChecked(false);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mProductDataList != null) {
                return mProductDataList.size();
            } else {
                return 0;
            }
        }
    }

    /**
     * Inner class for recycler view item decoration
     */
    public class ShoppingListItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration {
        Drawable mDivider;

        public ShoppingListItemDecoration() {
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

}
