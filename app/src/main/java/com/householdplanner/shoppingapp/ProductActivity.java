package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.common.ProductHelper;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

public class ProductActivity extends BaseActivity {

    private static final int NEW_MODE = 1;
    private static final int EDIT_MODE = 2;

    public static final String EXTRA_ID = "_id";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "name";
    public static final String EXTRA_MARKET_NAME = "market_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_UNIT_ID = "unit_id";

    //Current mode (Edit|New)
    private int mMode = NEW_MODE;
    private Product mProduct;
    private String mMarketName;
    private ValidationField mValidationFieldProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        //Get extra intent data if there are any
        getIntentData();
        inflateViews();
        if (mMode == EDIT_MODE) {
            //Load existing data got from the intent
            initViewsData();
        }
        createButtonHandlers();
        addListenerOnSpinnerMeasureItemSelection();
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
            mProduct = new Product();
            mProduct._id = bundle.getInt(EXTRA_ID);
            if (mProduct._id > 0) {
                mMode = EDIT_MODE;
                mProduct.productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0);
                mProduct.name = intent.getStringExtra(EXTRA_PRODUCT_NAME);
                mProduct.marketName = intent.getStringExtra(EXTRA_MARKET_NAME);
                mProduct.amount = intent.getStringExtra(EXTRA_AMOUNT);
                mProduct.unitId = intent.getIntExtra(EXTRA_UNIT_ID, 0);

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
                setProductData();
                if (mProduct.validate()) {
                    if (mMode == NEW_MODE) {
                        ProductHelper productHelper = new ProductHelper(ProductActivity.this, mProduct, new ProductHelper.OnSaveProduct() {
                            @Override
                            public void onSaveProduct() {
                                finishActivity();
                            }
                        });
                        productHelper.addProductToList();
                    } else {
                        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(ProductActivity.this);
                        useCaseShoppingList.updateProduct(mProduct);
                        finishActivity();
                    }
                } else {
                    mValidationFieldProductName.setError(R.string.textProductNameErrorMessage);
                }
            }
        });
    }

    /**
     * End the activity, sending the out data in the Intent
     */
    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PRODUCT_NAME, mProduct.name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Set initial data into the views
     */
    private void initViewsData() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        mValidationFieldProductName.setText(mProduct.name);
        editAmount.setText(mProduct.amount);
        spinnerMeasure.setSelection(mProduct.unitId);
    }

    /**
     * Unit Id spinner handler
     */
    private void addListenerOnSpinnerMeasureItemSelection() {
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        spinnerMeasure.setOnItemSelectedListener(new MeasureOnItemSelectedListener());
    }

    /**
     * Get the product data from the UI and set in the product object
     */
    private void setProductData() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        mProduct.name = mValidationFieldProductName.getText().trim();
        mProduct.amount = editAmount.getText().toString().trim();
    }

    public class MeasureOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mProduct.unitId = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}



	