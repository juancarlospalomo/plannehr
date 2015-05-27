package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.fragments.AlertDialogFragment;
import com.householdplanner.shoppingapp.repositories.ProductHistoryRepository;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;

public class ProductActivity extends BaseActivity {

    private static final int NEW_MODE = 1;
    private static final int EDIT_MODE = 2;

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

    private ValidationField mValidationFieldProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        getIntentData();
        inflateViews();

        if (mMode == EDIT_MODE) {
            setUIProductData();
        }

        createButtonHandlers();
        addListenerOnSpinnerMeasureItemSelection();
        addListenerOnSpinnerCategoryItemSelection();
    }

    /**
     * Inflate views from activity layout
     */
    private void inflateViews() {
        mValidationFieldProductName = (ValidationField) findViewById(R.id.validationFieldProductName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Recover data from Intent when the activity is called
     */
    private void getIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try {
            mId = bundle.getInt(EXTRA_PRODUCT_ID);
            if (mId > 0) {
                mMode = EDIT_MODE;
                mName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
                mMarketName = intent.getStringExtra(EXTRA_MARKET_NAME);
                mAmount = intent.getStringExtra(EXTRA_AMOUNT);
                mMeasureId = intent.getIntExtra(EXTRA_UNIT_ID, 0);
                mCategoryId = intent.getIntExtra(EXTRA_CATEGORY, 0);
            }
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {
        }
    }

    /**
     * Create the handlers for buttons in the activity
     */
    private void createButtonHandlers() {
        AppCompatButton buttonOk = (AppCompatButton) findViewById(R.id.btnSave);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationFieldProductName.setError("");
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
                    mValidationFieldProductName.setError(R.string.textProductNameErrorMessage);
                }
            }
        });
    }

    private void setUIProductData() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        AppCompatSpinner spinnerCategory = (AppCompatSpinner) findViewById(R.id.spCategory);
        mValidationFieldProductName.setText(mName);
        editAmount.setText(mAmount);
        spinnerMeasure.setSelection(mMeasureId);
        spinnerCategory.setSelection(mCategoryId);
    }

    private void addListenerOnSpinnerMeasureItemSelection() {
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        spinnerMeasure.setOnItemSelectedListener(new MeasureOnItemSelectedListener());
    }

    private void addListenerOnSpinnerCategoryItemSelection() {
        AppCompatSpinner spinnerCategory = (AppCompatSpinner) findViewById(R.id.spCategory);
        spinnerCategory.setOnItemSelectedListener(new CategoryOnItemSelectedListener());
    }

    public void saveProduct() {
        ShoppingListRepository shoppingListRepository = new ShoppingListRepository(this);
        if (mMode == NEW_MODE) {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(this);
            Cursor cursor = historyRepository.getProduct(mName, null);
            if ((cursor != null) && (cursor.moveToFirst())) {
                mMarketName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
                if (TextUtils.isEmpty(mMarketName)) mMarketName = null;
            }
            shoppingListRepository.createProductItem(mName, mMarketName, mAmount, mMeasureId, mCategoryId, 0);
            shoppingListRepository.close();
        } else {
            shoppingListRepository.updateProductItem(mId, mName, mMarketName, mAmount, mMeasureId, mCategoryId);
            shoppingListRepository.close();
        }
        getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
        getContentResolver().notifyChange(ShoppingListContract.ProductHistoryEntry.CONTENT_URI, null);
    }

    public void setFieldValuesFromUI() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        mName = mValidationFieldProductName.getText().trim();
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

    /**
     * Ask if add to the list one existing product on it
     */
    private void showAskDialog() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.textDuplicateProductWarningTitle),
                getResources().getString(R.string.textDuplicateProductWarningMessage),
                getResources().getString(R.string.dialog_cancel),
                getResources().getString(R.string.product_add_existing_text)
        );
        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialogFragment.INDEX_BUTTON_YES:
                        saveAndFinish();
                        break;
                }

            }
        });
        alertDialog.show(getSupportFragmentManager(), "confirmationDialog");
    }

    public class MeasureOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mMeasureId = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    public class CategoryOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mCategoryId = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}



	