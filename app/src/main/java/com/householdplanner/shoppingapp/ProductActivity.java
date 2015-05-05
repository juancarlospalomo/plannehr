package com.householdplanner.shoppingapp;

import java.util.ArrayList;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.householdplanner.shoppingapp.cross.AppPreferences;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

public class ProductActivity extends SherlockFragmentActivity implements DialogInterface.OnClickListener {

	private static final int NEW_MODE = 1;
	private static final int EDIT_MODE  = 2;
	
	public static final String EXTRA_PRODUCT_ID = "ProductId";
	public static final String EXTRA_PRODUCT_NAME = "Name";
	public static final String EXTRA_MARKET_NAME = "Market";
	public static final String EXTRA_AMOUNT = "Amount";
	public static final String EXTRA_UNIT_ID = "UnitId";
	public static final String EXTRA_CATEGORY = "Category";
	
	//MVC pattern
	//http://androidexample.com/index.php?view=article_discription&aid=116&aaid=138#at_pco=smlre-1.0&at_tot=4&at_ab=per-12&at_pos=1
	private int mMode = NEW_MODE;
	private int mId;
	private String mName;
	private String mMarketName;
	private String mAmount;
	private int mMeasureId = 0;
	private int mCategoryId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setUpFont();
		getIntentData();
		if (mMode==EDIT_MODE) {
			setUIProductData();
		} 
		addListenerOnSpinnerMeasureItemSelection();
		addListenerOnSpinnerCategoryItemSelection();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getIntentData() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		try {
			mId = bundle.getInt(EXTRA_PRODUCT_ID);
			if (mId>0) {
				mMode = EDIT_MODE;
				mName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
				mMarketName = intent.getStringExtra(EXTRA_MARKET_NAME);
				mAmount = intent.getStringExtra(EXTRA_AMOUNT);
				mMeasureId = intent.getIntExtra(EXTRA_UNIT_ID, 0);
				mCategoryId = intent.getIntExtra(EXTRA_CATEGORY, 0);
			}
		} catch (NumberFormatException e) {}
		  catch (NullPointerException e) {}
	}
	
	private void setUIProductData() {
		EditText editProductName = (EditText) findViewById(R.id.edProductName);
		EditText editAmount = (EditText) findViewById(R.id.edAmount);
		Spinner spinnerMeasure = (Spinner) findViewById(R.id.spMeasure);
		Spinner spinnerCategory = (Spinner) findViewById(R.id.spCategory);
		editProductName.setText(mName);
		editAmount.setText(mAmount);
		spinnerMeasure.setSelection(mMeasureId);
		spinnerCategory.setSelection(mCategoryId);
	}
	
	private void setUpFont() {
		EditText editProductName = (EditText) findViewById(R.id.edProductName);
		EditText editAmount = (EditText) findViewById(R.id.edAmount);
		editProductName.setTypeface(font.getEditTextFont(this));
		editAmount.setTypeface(font.getEditTextFont(this));
	}
	
	private void addListenerOnSpinnerMeasureItemSelection() {
		Spinner spinnerMeasure = (Spinner) findViewById(R.id.spMeasure);
		spinnerMeasure.setOnItemSelectedListener(new MeasureOnItemSelectedListener());
	}
	
	private void addListenerOnSpinnerCategoryItemSelection() {
		Spinner spinnerCategory = (Spinner) findViewById(R.id.spCategory);
		spinnerCategory.setOnItemSelectedListener(new CategoryOnItemSelectedListener());
	}

	public void btnSave_onClick(View view) {
		setFieldValuesFromUI();
		if (validate()) {
			if (mMode == NEW_MODE) {
				if (!existProductInList()) {
					saveAndFinish();
				} else {
					showAskDialog();
				}
			} else {
				saveAndFinish();
			}
		} else {
			showValidationErrorMessage();
		}
	}
	
	public void btnCancel_onClick(View view) {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	public void saveProduct() {
		ShoppingListRepository shoppingListRepository = new ShoppingListRepository(this);
		if (mMode==NEW_MODE) {
			ProductHistoryRepository historyRepository = new ProductHistoryRepository(this);
			Cursor cursor = historyRepository.getProduct(mName, null);
			if ((cursor!=null) && (cursor.moveToFirst())) {
				mMarketName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
				if (TextUtils.isEmpty(mMarketName)) mMarketName = null;
			}
			shoppingListRepository.createProductItem(mName, mMarketName, mAmount, mMeasureId, mCategoryId, 0);
			shoppingListRepository.close();
		} else {
			shoppingListRepository.updateProductItem(mId, mName, mMarketName, mAmount, mMeasureId, mCategoryId);
			shoppingListRepository.close();
		}
		getContentResolver().notifyChange(AppPreferences.URI_LIST_TABLE, null);
		getContentResolver().notifyChange(AppPreferences.URI_HISTORY_TABLE, null);
	}
		
	public void setFieldValuesFromUI() {
		EditText editTextName = (EditText) findViewById(R.id.edProductName);
		EditText editAmount = (EditText) findViewById(R.id.edAmount);
		mName = editTextName.getText().toString().trim();
		mAmount = editAmount.getText().toString().trim();
	}
	
	private boolean validate() {
		boolean result = false;
		if (!TextUtils.isEmpty(mName)) {
			result = true;
		}
		return result;
	}
	
	private boolean existProductInList() {
		boolean result = false;
		ShoppingListRepository listRepository = new ShoppingListRepository(this);
		result = listRepository.existProductInNotCommittedList(mName);
		listRepository.close();
		return result;
	}
	
	private void saveAndFinish() {
		saveProduct();
		Intent intent = new Intent();
		intent.putExtra(EXTRA_PRODUCT_NAME, mName);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	private void showValidationErrorMessage() {
		ArrayList<String> messageList = new ArrayList<String>();
		String[] message;
		messageList.add(getResources().getString(R.string.textProductNameErrorMessage));
		ValidationDialog alertDialog = new ValidationDialog();
		Bundle args = new Bundle();
		message = new String[messageList.size()];
		message = messageList.toArray(message);
		args.putStringArray("message", message);
		args.putString("title", getResources().getString(R.string.title_activity_product));
		alertDialog.setArguments(args);
		alertDialog.show(getSupportFragmentManager(), "dialog");
	}
	
	private void showAskDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setMessage(getResources().getString(R.string.textDuplicateProductWarningMessage));
		alertDialog.setTitle(getResources().getString(R.string.textDuplicateProductWarningTitle));
		alertDialog.setPositiveButton(android.R.string.ok, this);
		alertDialog.setNegativeButton(android.R.string.cancel, this);
		alertDialog.create().show();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case AlertDialog.BUTTON_POSITIVE:
			saveAndFinish();
			break;
		case AlertDialog.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		}
	}
	
	public class MeasureOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			mMeasureId = pos;
    	}
		 
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	
	public class CategoryOnItemSelectedListener implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			mCategoryId = pos;
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

}



	