package com.householdplanner.shoppingapp.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.applilandia.widget.CircleView;
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

                            /**
                             * Animate the deletion of a row
                             * @param view row view to animate
                             */
                            private void animateRowDeleted(final View view, final int position) {
                                if (view != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                        Animator animator = AnimatorInflater.loadAnimator(getActivity(), R.animator.row_deleted);
                                        animator.setTarget(view);
                                        animator.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                Product product = mAdapter.mProductDataList.get(position);
                                                if (product != null) {
                                                    UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                                    useCaseShoppingList.removeFromBasket(product);
                                                    getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                                }
                                                mAdapter.mProductDataList.remove(position);
                                                mAdapter.notifyItemRangeRemoved(position, 1);
                                            }
                                        });
                                        animator.start();
                                    } else {
                                        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.row_deleted);
                                        animation.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                Product product = mAdapter.mProductDataList.get(position);
                                                if (product != null) {
                                                    UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                                    useCaseShoppingList.removeFromBasket(product);
                                                    getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                                }
                                                mAdapter.mProductDataList.remove(position);
                                                mAdapter.notifyItemRangeRemoved(position, 1);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                        view.startAnimation(animation);
                                    }
                                }
                            }

                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemPrimaryActionClick(final View view, final int position) {
                                final Product product = mAdapter.mProductDataList.get(position);
                                ProductDialogFragment productDialogFragment = ProductDialogFragment.newInstance(product.name,
                                        product._id, product.photoName, ProductDialogFragment.ProductActions.Remove);
                                productDialogFragment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        animateRowDeleted(view, position);
                                    }
                                });
                                productDialogFragment.show(getFragmentManager(), "fragment_product_dialog");
                            }

                            @Override
                            public void onItemSecondaryActionClick(final View view, int position) {
                                final AppCompatCheckBox checkBoxActionIcon = (AppCompatCheckBox) view;

                                if (!checkBoxActionIcon.isChecked()) {
                                    //Unchecked the checkbox, therefore the action has to be undone
                                    Product product = (Product) mSnackBar.getAdapterItem();
                                    position = mSnackBar.getAdapterPosition();
                                    if (product != null && position != SnackBar.INVALID_POSITION) {
                                        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                        useCaseShoppingList.backToCart(product);
                                        getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                    }
                                    checkBoxActionIcon.setChecked(true);
                                    mSnackBar.undo();
                                } else {
                                    checkBoxActionIcon.setChecked(false);
                                    //We check if there is a row being deleted currently
                                    int lastPosition = mSnackBar.getAdapterPosition();
                                    if (lastPosition != mSnackBar.INVALID_POSITION) {
                                        //Then, hide the SnackBar
                                        mSnackBar.hide();
                                        //After hide the SnackBar, it contains an INVALID_POSITION
                                    }
                                    if (mSnackBar.getAdapterPosition() == SnackBar.INVALID_POSITION) {
                                        //Deleting has finished
                                        final int currentPosition = position;
                                        //SnackBar tied to the current position
                                        mSnackBar.setAdapterPosition(currentPosition);
                                        mSnackBar.setAdapterItem(mAdapter.mProductDataList.get(currentPosition));
                                        mSnackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                                            @Override
                                            public void onClose() {
                                            }

                                            @Override
                                            public void onUndo() {
                                                //User click on Undo action
                                                Product product = (Product) mSnackBar.getAdapterItem();
                                                int position = mSnackBar.getAdapterPosition();
                                                if (product != null && position != SnackBar.INVALID_POSITION) {
                                                    UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(getActivity());
                                                    useCaseShoppingList.backToCart(product);
                                                    getActivity().getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                        ((View) view.getParent()).setScaleY(1f);
                                                    }
                                                }
                                                checkBoxActionIcon.setChecked(true);
                                            }
                                        });
                                        BasketAdapter.ViewHolder viewHolder = (BasketAdapter.ViewHolder) mRecyclerViewBasket.findViewHolderForAdapterPosition(currentPosition);
                                        animateRowDeleted(viewHolder.itemView, currentPosition);
                                        mSnackBar.show(R.string.text_snack_bar_move_shoppinglist);
                                    }
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
                mAdapter = new BasketAdapter(data);
                mRecyclerViewBasket.setAdapter(mAdapter);
                ImageView imageCart = (ImageView) getView().findViewById(R.id.imageEmptyCart);
                if (items == 0) {
                    imageCart.setVisibility(View.VISIBLE);
                } else {
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

            public CircleView mCircleAvatar;
            public AppCompatTextView mTextViewPrimary;
            public AppCompatTextView mTextViewSecondary;
            public AppCompatCheckBox mSecondaryActionIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                mCircleAvatar = (CircleView) itemView.findViewById(R.id.imagePrimaryActionIcon);
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
                //Set photo in avatar
                if (product.photoName != null) {
                    String pathFileName = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/" + product.photoName;
                    Bitmap bitmap = BitmapFactory.decodeFile(pathFileName);
                    viewHolder.mCircleAvatar.setBitmap(bitmap);
                } else {
                    viewHolder.mCircleAvatar.setDrawable(R.drawable.ic_photo_red);
                }
                viewHolder.mTextViewPrimary.setText(product.name);
                if (product.amount != null) {
                    viewHolder.mTextViewSecondary.setVisibility(View.VISIBLE);
                    viewHolder.mTextViewSecondary.setText(product.amount + " " + util.getMeasureUnitName(getActivity(),
                            product.unitId, product.amount));
                } else {
                    viewHolder.mTextViewSecondary.setVisibility(View.GONE);
                }
                viewHolder.mSecondaryActionIcon.setChecked(true);
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

        /**
         * Find the position in the adapter of the row with product name = name
         *
         * @param name product name
         * @return return position or -1 if it isn't found
         */
        public int findPositionByName(String name) {
            for (int index = 0; index < mProductDataList.size(); index++) {
                if (mProductDataList.get(index).name.equals(name)) {
                    return index;
                }
            }
            return -1;
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
