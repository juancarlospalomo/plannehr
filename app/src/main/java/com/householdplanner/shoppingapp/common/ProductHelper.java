package com.householdplanner.shoppingapp.common;

import android.content.Context;
import android.content.DialogInterface;

import com.householdplanner.shoppingapp.BaseActivity;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.exceptions.ProductException;
import com.householdplanner.shoppingapp.fragments.AlertDialogFragment;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

public class ProductHelper {

    /**
     * Interface to communicate through a callback handler
     */
    public interface OnSaveProduct {
        void onSaveProduct();
    }

    //Callback handler
    private OnSaveProduct mProductHelperCallback;
    private Context mContext;
    private Product mProduct;

    public ProductHelper(Context context, Product product, OnSaveProduct onSave) {
        mContext = context;
        mProduct = product;
        mProductHelperCallback = onSave;
    }

    /**
     * Try to add a product to the list.  It´s the external interface method
     */
    public void addProductToList() {
        saveProduct(false);
    }

    /**
     * Save a product on List
     *
     * @param allowDuplicates if this value is true, it will save the product although it already exists
     */
    private void saveProduct(boolean allowDuplicates) {
        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(mContext);
        try {
            useCaseShoppingList.createProduct(mProduct, allowDuplicates);
            mContext.getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
            mContext.getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
            if (mProductHelperCallback!=null) {
                mProductHelperCallback.onSaveProduct();
            }
        } catch (ProductException e) {
            //Duplicated not allow, ask the user if he/she wants to allow it anyway
            showAskDialog();
        }
    }

    /**
     * Ask if add to the list one existing product on it
     */
    private void showAskDialog() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(mContext.getResources().getString(R.string.textDuplicateProductWarningTitle),
                mContext.getResources().getString(R.string.textDuplicateProductWarningMessage),
                mContext.getResources().getString(R.string.dialog_cancel),
                mContext.getResources().getString(R.string.product_add_existing_text), null
        );
        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialogFragment.INDEX_BUTTON_YES:
                        saveProduct(true);
                        break;
                }

            }
        });
        alertDialog.show(((BaseActivity) mContext).getSupportFragmentManager(), "confirmationDialog");
    }

}
