package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.TextView;

import com.householdplanner.shoppingapp.repositories.WalletRepository;
import com.householdplanner.shoppingapp.stores.BudgetStore;

import java.util.ArrayList;
import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {

	private final int NEW_MODE = 1;
	private final int EDIT_MODE  = 2;
	
	private int mId;
	private int mMonthId = -1;
	private float mAvailable;
	private float mTarget;
	
	private int mMode = NEW_MODE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getIntentData();
		createButtonHandlers();
		if (mMode == NEW_MODE) {
			//It hasn't been called to edit a budget.   
			//Try to get if it has been called to create budget for a exact month
			getMonthIntentData();
		}
		setUIBudgetData();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	/**
	 * Create the handlers for the button in the activity
	 */
	private void createButtonHandlers() {
		AppCompatButton buttonOk = (AppCompatButton) findViewById(R.id.btnSaveBudget);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveBudget()) {
					setResult(Activity.RESULT_OK);
					finish();
				}
			}
		});
	}

	/**
	 * Save the budget in database
	 * @return true if the budget was saved correctly
	 */
	private boolean saveBudget() {
		WalletRepository budgetRepository = new WalletRepository(this);
		if (validate()) {
			if (mMode==NEW_MODE) {
				budgetRepository.createBudget(mMonthId, mAvailable, mTarget);
			} else {
				budgetRepository.updateBudget(mId, mMonthId, mAvailable, mTarget);
			}
			budgetRepository.close();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean validate() {
		boolean availableValidated = false, targetValidated = false;
		AppCompatTextView textViewAvailable = (AppCompatTextView) findViewById(R.id.edtAvailable);
		AppCompatTextView textViewTarget = (AppCompatTextView) findViewById(R.id.edtTarget);
		String sAvailable = textViewAvailable.getText().toString().trim();
		String sTarget = textViewTarget.getText().toString().trim();
		
		if (!TextUtils.isEmpty(sAvailable)) {
			mAvailable = Float.parseFloat(sAvailable);
			availableValidated = true;
		}
		if (!TextUtils.isEmpty(sTarget)) {
			mTarget = Float.parseFloat(sTarget);
			targetValidated = true;
		}
		if (!availableValidated || !targetValidated) {
			showValidationErrorMessage(availableValidated, targetValidated);
			return false;
		} else {
			return true;
		}
	}
	
	private void getMonthIntentData() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		try {
			mMonthId = bundle.getInt("MonthId");
			mMode = NEW_MODE;
			
		} catch (NumberFormatException e) {}
		  catch (NullPointerException e) {}
	}
	
	private void getIntentData() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		try {
			mId = bundle.getInt("BudgetId");
			if (mId>0) {
				mMode = EDIT_MODE;
				mMonthId = bundle.getInt(BudgetStore.COLUMN_MONTH);
				mAvailable = bundle.getFloat(BudgetStore.COLUMN_AVAILABLE);
				mTarget = bundle.getFloat(BudgetStore.COLUMN_TARGET);
			}
		} catch (NumberFormatException e) {}
		  catch (NullPointerException e) {}
	}
	
	private void setUIBudgetData() {
		AppCompatSpinner spinnerBudget = (AppCompatSpinner) findViewById(R.id.spMonth);
		spinnerBudget.setOnItemSelectedListener(new MonthOnItemSelectedListener());
		if (mMode==EDIT_MODE) {
			spinnerBudget.setSelection(mMonthId);
			EditText edtAvailable = (EditText) findViewById(R.id.edtAvailable);
			edtAvailable.setText(String.valueOf(mAvailable));
			EditText edtTarget = (EditText) findViewById(R.id.edtTarget);
			edtTarget.setText(String.valueOf(mTarget));
		} else {
			spinnerBudget.setSelection(getMonthToSelect());
		}
	}

	private int getMonthToSelect() {
		int monthId = mMonthId;
		if (monthId == -1) {
			monthId = Calendar.getInstance().get(Calendar.MONTH);
			WalletRepository budgetRepository = new WalletRepository(this);
			int lastMonthId = budgetRepository.getLastBudgetMonth();
			while (!isNextMonth(monthId, lastMonthId)) {
				if (monthId==11) {
					monthId = 0;
				} else {
					monthId++;
				}
			}
		}
		return monthId;
	}
	
	public boolean isNextMonth(int currentMonth, int lastMonth) {
		boolean result = false;
		
		if (currentMonth>lastMonth) result = true;
		else {
			if (currentMonth==0) {
				if (lastMonth==11) {
					result = true;
				}
			}
		}
		return result;
	}
	
	private void showValidationErrorMessage(boolean validatedAvailable, boolean validatedTarget) {
		ArrayList<String> messageList = new ArrayList<String>();
		String[] message;
		if (!validatedAvailable) {
			messageList.add(getResources().getString(R.string.textBudgetAvailableErrorMessage));
		}
		if (!validatedTarget) {
			messageList.add(getResources().getString(R.string.textBudgetTargetErrorMessage));
		}
		ValidationDialog alertDialog = new ValidationDialog();
		Bundle args = new Bundle();
		message = new String[messageList.size()];
		message = messageList.toArray(message);
		args.putStringArray("message", message);
		args.putString("title", getResources().getString(R.string.title_activity_budget));
		alertDialog.setArguments(args);
		alertDialog.show(getSupportFragmentManager(), "dialog");
	}
	
 	public class MonthOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			mMonthId = pos;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}
	
}
