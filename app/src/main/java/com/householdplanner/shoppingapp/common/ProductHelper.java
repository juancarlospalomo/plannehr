package com.householdplanner.shoppingapp.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.exceptions.ProductException;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

public class ProductHelper implements DialogInterface.OnClickListener {

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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                saveProduct(true);
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }

    /**
     * Try to add a product to the list.  It´s the external interface method
     */
    public void addProductToList() {
        saveProduct(false);
    }

    /**
     * Save a product on List
     * @param allowDuplicates if this value is true, it will save the product although it already exists
     */
    private void saveProduct(boolean allowDuplicates) {
        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(mContext);
        try {
            useCaseShoppingList.createProduct(mProduct, allowDuplicates);
            mContext.getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
            mContext.getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
            mProductHelperCallback.onSaveProduct();
        } catch (ProductException e) {
            //Duplicated not allow, ask the user if he/she wants to allow it anyway
            showAskDialog();
        }
    }

    /**
     * Show a dialog to ask the user if he wants to allow insert the product,
     * because it already exists
     */
    private void showAskDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setMessage(mContext.getResources().getString(R.string.textDuplicateProductWarningMessage));
        alertDialog.setTitle(mContext.getResources().getString(R.string.textDuplicateProductWarningTitle));
        alertDialog.setPositiveButton(android.R.string.ok, this);
        alertDialog.setNegativeButton(android.R.string.cancel, this);
        alertDialog.create().show();
    }

}
