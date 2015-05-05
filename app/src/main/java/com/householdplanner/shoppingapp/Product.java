package com.householdplanner.shoppingapp;

import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.Toast;

public class Product implements DialogInterface.OnClickListener {

	private OnSaveProduct mProductCallback;
	private Activity mActivity;
	private String mProductName;
	private String mMarket;
	private String mAmount;
	private int mUnitId;
	
	public Product(Activity activity, String productName, String market, String amount, int unitId, OnSaveProduct onSave) {
		mProductName = productName;
		mMarket = market;
		mAmount = amount;
		mUnitId = unitId;
		mActivity = activity;
		mProductCallback = onSave;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case AlertDialog.BUTTON_POSITIVE:
			saveProduct();
			break;
		case AlertDialog.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		}
	}

	private boolean existProductInList() {
		boolean result = false;
		ShoppingListRepository listRepository = new ShoppingListRepository(mActivity);
		result = listRepository.existProductInNotCommittedList(mProductName);
		listRepository.close();
		return result;
	}
	
	public void addProductToList() {
		if (!existProductInList()) {
			saveProduct();
		} else {
			showAskDialog();
		}
	}
	
	private void saveProduct() {
		Toast.makeText(mActivity, mProductName, Toast.LENGTH_SHORT).show();
		int categoryId = 0;
		int sequence = 0;
		ProductHistoryRepository historyRepository = new ProductHistoryRepository(mActivity);
		ShoppingListRepository listRepository = new ShoppingListRepository(mActivity);
		Cursor cursor = historyRepository.getProduct(mProductName, mMarket);
		if (cursor!=null) {
			if (cursor.moveToFirst()) {
				categoryId = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_CATEGORY));
				sequence = cursor.getInt(cursor.getColumnIndex(ProductHistoryStore.COLUMN_SEQUENCE));
				if (TextUtils.isEmpty(mMarket)) {
					mMarket = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
				}
			}
		}
		if (listRepository.createProductItem(mProductName, mMarket, mAmount, mUnitId, categoryId, sequence)) {
			mActivity.getContentResolver().notifyChange(AppPreferences.URI_HISTORY_TABLE, null);
			mActivity.getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
			mProductCallback.onSaveProduct();
		}
		historyRepository.close();
		listRepository.close();
	}
	
	private void showAskDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
		alertDialog.setMessage(mActivity.getResources().getString(R.string.textDuplicateProductWarningMessage));
		alertDialog.setTitle(mActivity.getResources().getString(R.string.textDuplicateProductWarningTitle));
		alertDialog.setPositiveButton(android.R.string.ok, this);
		alertDialog.setNegativeButton(android.R.string.cancel, this);
		alertDialog.create().show();
	}
	
	public interface OnSaveProduct {
		public void onSaveProduct();
	}
	
}
