package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.householdplanner.shoppingapp.cross.ExpenseStructure;
import com.householdplanner.shoppingapp.cross.font;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.repositories.WalletRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TicketActivity extends AppCompatActivity {

	public static final String EXTRA_HAS_PRODUCTS = "hasProducts";
	
	private static final int CREATE_BUDGET = 1;
	
	boolean mHasProducts = false;
	String mTicketDate = "";
	String mTicketValue = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket);
		Intent intent = this.getIntent();
		mHasProducts = intent.getBooleanExtra(EXTRA_HAS_PRODUCTS, false);
		setDefaultValues();
		createButtonHandlers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	private void createButtonHandlers() {
		AppCompatButton buttonOk = (AppCompatButton) findViewById(R.id.btnSaveTicket);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GetDataFromUI();
				if (!Validate()) {
					ShowValidationErrorMessage();
				} else {
					checkExistBudget();
				}
			}
		});
	}

	public void showDatePickerDialog(View view) {
		DialogFragment dateFragment = new DatePickerFragment();
		dateFragment.show(getSupportFragmentManager(), "fecha");
	}
	
	private void checkExistBudget() {
		WalletRepository wallet = new WalletRepository(this);
		int month = util.getMonth(mTicketDate);
		if (!wallet.existBudget(month)) {
			Intent intent = new Intent(this, BudgetActivity.class);
			intent.putExtra("MonthId", month);
			startActivityForResult(intent, CREATE_BUDGET);
		} else {
			Save();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case CREATE_BUDGET:
				if (resultCode == Activity.RESULT_OK) {
					Save();
				}
				break;
		}
	}
	
	private boolean Validate() {
		boolean result = false;
		if ((!TextUtils.isEmpty(mTicketDate)) && (!TextUtils.isEmpty(mTicketValue))) {
			result = true;
		}
		return result;
	}
	
	private void setDefaultValues() {
		AppCompatButton btnDate = (AppCompatButton) findViewById(R.id.btnTicketDate);
		btnDate.setTypeface(font.getButtonFont(this));
		btnDate.setText(util.getFormattedDate());
		EditText editTextExpense = (EditText) findViewById(R.id.edtExpense);
		editTextExpense.setTypeface(font.getEditTextFont(this));
	}
	
	private void ShowValidationErrorMessage() {
		ArrayList<String> messageList = new ArrayList<String>();
		String[] message;
		if (TextUtils.isEmpty(mTicketDate)) {
			messageList.add(getResources().getString(R.string.textTicketDateErrorMessage));
		}
		if (TextUtils.isEmpty(mTicketValue)) {
			messageList.add(getResources().getString(R.string.textTicketValueErrorMessage));
		}
		ValidationDialog alertDialog = new ValidationDialog();
		Bundle args = new Bundle();
		message = new String[messageList.size()];
		message = messageList.toArray(message);
		args.putStringArray("message", message);
		args.putString("title", getResources().getString(R.string.title_activity_ticket));
		alertDialog.setArguments(args);
		alertDialog.show(getSupportFragmentManager(), "dialog");
	}
	
	private void GetDataFromUI() {
		AppCompatButton btnDate = (AppCompatButton) findViewById(R.id.btnTicketDate);
		String sTicketDate = btnDate.getText().toString().trim();
		if (sTicketDate!=getResources().getString(R.string.atTicketDate)) {
			mTicketDate = sTicketDate;
		}
		EditText edtValue = (EditText) findViewById(R.id.edtExpense);
		mTicketValue = edtValue.getText().toString().trim();
	}
	
	private void Save() {
		ExpenseStructure expense = new ExpenseStructure();
		expense.hasProducts = mHasProducts;
		expense.expenseDate = util.getFormattedDate(mTicketDate, DateFormat.MEDIUM, "yyyy-MM-dd");
		expense.expenseValue = mTicketValue;
		ExpenseOperation ticketOperation = new ExpenseOperation(this);
		ticketOperation.execute(expense);
	}
	
	private class ExpenseOperation extends AsyncTask<ExpenseStructure, Void, Boolean> {
		private Context _context;
		
		public ExpenseOperation(Context context) {
			_context = context;
		}
		
		@Override
		protected Boolean doInBackground(ExpenseStructure... params) {
			boolean result = true;
			WalletRepository walletRepository = new WalletRepository(_context);
			try {
				walletRepository.createExpense(mTicketDate, mTicketValue, mHasProducts);
				walletRepository.close();
			} catch (Exception e) 
			{	
				result = false;
			}
			return result;
		}
		
		@Override 
		public void onPostExecute(Boolean result) {
			if (result) {
				setResult(RESULT_OK);
				finish();
			}
		}
	}
	
	public static class DatePickerFragment extends DialogFragment
								implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			AppCompatButton btnDate = (AppCompatButton) this.getActivity().findViewById(R.id.btnTicketDate);
			btnDate.setText(util.getFormattedDate(year, monthOfYear, dayOfMonth));
		}		
	}

}
