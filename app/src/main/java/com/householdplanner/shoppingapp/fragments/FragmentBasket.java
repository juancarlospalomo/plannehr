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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.applilandia.widget.SnackBar;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.listeners.RecyclerViewClickListener;
import com.householdplanner.shoppingapp.loaders.ProductLoader;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

import java.util.List;

public class FragmentBasket extends Fragment implements LoaderManager.LoaderCallbacks<List<Product>>,
        OnFragmentProgress {

    private static final int LOADER_ID = 1;
    private RecyclerView mRecyclerViewBasket;
    private RecyclerView.LayoutManager mLayoutManager;
    private SnackBar mSnackBar;
    private BasketAdapter mAdapter;
    private OnLoadData mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basket, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflateViews();
        initRecyclerView();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Inflate the existing views in the Fragment
     */
    private void inflateViews() {
        mRecyclerViewBasket = (RecyclerView) getView().findViewById(R.id.recyclerViewBasket);
        mSnackBar = (SnackBar) getView().findViewById(R.id.snackBarBasket);
    }

    /**
     * Init the recycler view
     */
    private void initRecyclerView() {
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mRecyclerViewBasket.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerViewBasket.setLayoutManager(mLayoutManager);
        mRecyclerViewBasket.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewBasket.addItemDecoration(new BasketItemDecoration());

        mRecyclerViewBasket.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(),
                        new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {

                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemSecondaryActionClick(View view, final int position) {
                                final AppCompatCheckBox checkBoxActionIcon = (AppCompatCheckBox) view;
                                if (mSnackBar.getVisibility() == View.GONE) {
                                    //The snack bar is not displayed yet, so we have to show it
                                    checkBoxActionIcon.setChecked(false);
                                    mSnackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                                        @Override
                                        public void onClose() {
                                            Product product = mAdapter.mProductDataList.get(position);
                                            if (product != null) {
                                                mAdapter.mProductDataList.remove(position);
                                                mAdapter.notifyItemRangeRemoved(position, 1);
                                                UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                                useCaseShoppingList.removeFromBasket(product);
                                                getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                            }
                                        }

                                        @Override
                                        public void onUndo() {
                                            //User click on Undo action
                                            checkBoxActionIcon.setChecked(true);
                                        }
                                    });
                                    mSnackBar.show(R.string.text_snack_bar_move_myproduct);
                                } else {
                                    //SnackBar is visible, but the user instead pushing on UNDO action,
                                    //Unchecked the checkbox, therefore the action has to be undone
                                    checkBoxActionIcon.setChecked(true);
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
        return new ProductLoader(getActivity(), ProductLoader.TypeProducts.InBasket);
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> data) {
        int items = 0;
        if (mCallback != null) {
            if (data != null) items = data.size();
            mCallback.onLoadFinish(items, null);
        }
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                ImageView imageCart = (ImageView) getView().findViewById(R.id.imageEmptyCart);
                if (items == 0) {
                    imageCart.setVisibility(View.VISIBLE);
                } else {
                    mAdapter = new BasketAdapter(data);
                    mRecyclerViewBasket.setAdapter(mAdapter);
                    imageCart.setVisibility(View.GONE);
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Product>> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mRecyclerViewBasket.setAdapter(null);
        mAdapter = null;
    }

    @Override
    public void setOnLoadData(OnLoadData callback) {
        mCallback = callback;
    }

    public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder> {

        private List<Product> mProductDataList;

        public BasketAdapter(List<Product> data) {
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
        public BasketAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.shopping_rowlayout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BasketAdapter.ViewHolder viewHolder, int position) {
            if (mProductDataList != null) {
                Product product = mProductDataList.get(position);
                if (product != null) {
                    viewHolder.mTextViewPrimary.setText(product.name);
                    viewHolder.mTextViewSecondary.setText(product.amount + " " + util.getMeasureUnitName(getActivity(),
                            product.unitId, product.amount));
                    viewHolder.mSecondaryActionIcon.setChecked(true);
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
    public class BasketItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration {
        Drawable mDivider;

        public BasketItemDecoration() {
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
