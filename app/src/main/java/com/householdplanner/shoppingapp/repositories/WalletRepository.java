package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.stores.BudgetStore;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WalletRepository {

	  // Database fields
	  private SQLiteDatabase mDatabase;
	  private DatabaseHelper mDatabaseHelper;
	  private Cursor mCursor;
	  private Context mContext;
	  
	  private String[] allColumns = {BudgetStore.COLUMN_ID,
			  BudgetStore.COLUMN_MONTH,
			  BudgetStore.COLUMN_AVAILABLE, 
			  BudgetStore.COLUMN_TARGET,
			  BudgetStore.COLUMN_WITHDRAWN,
			  BudgetStore.COLUMN_WALLET, 
			  BudgetStore.COLUMN_DEVICE_WITHDRAWN,
			  BudgetStore.COLUMN_DEVICE_WALLET,
			  BudgetStore.COLUMN_LAST_WITHDRAWN,
			  BudgetStore.COLUMN_LAST_WALLET
	  };

	  public WalletRepository(Context context) {
		  mContext = context;
		  mDatabaseHelper = new DatabaseHelper(context);
	  }

	  protected void onDestroy () {
		  this.close();
	  }
	  
	  private void open() throws SQLException {
		  mDatabase = mDatabaseHelper.getWritableDatabase();
	  }

	  public SQLiteDatabase getDatabase() {
		  if (mDatabase == null) {
			  this.open();
		  }
		  return mDatabase;
	  }
	  
	  public void close() {
		  if (mCursor!=null) mCursor.close();
		  if (mDatabase!=null) mDatabase.close();
		  if (mDatabaseHelper!=null) mDatabaseHelper.close();
	  }

	  public boolean createBudget(int month, float available, float target) {
		  return createBudget(month, available, target, 0, 0);
	  }
	  
	  public boolean createBudget(int month, float available, float target, float withDrawn, float wallet) {
		  ContentValues values = new ContentValues();
		  values.put(BudgetStore.COLUMN_MONTH, month);
		  values.put(BudgetStore.COLUMN_AVAILABLE, available);
		  values.put(BudgetStore.COLUMN_TARGET, target);
		  values.put(BudgetStore.COLUMN_WITHDRAWN, withDrawn);
		  values.put(BudgetStore.COLUMN_WALLET, wallet);
		  values.put(BudgetStore.COLUMN_DEVICE_WITHDRAWN, 0);
		  values.put(BudgetStore.COLUMN_DEVICE_WALLET, 0);
		  values.put(BudgetStore.COLUMN_LAST_WITHDRAWN, withDrawn);
		  values.put(BudgetStore.COLUMN_LAST_WALLET, wallet);
		  long insertId = getDatabase().insert(BudgetStore.TABLE_BUDGET, null, values);
		  if (insertId > 0) {

			  return true;
		  }
		  else return false;
	  }
	  
	  public Cursor getBudget(int month) {
		  return getDatabase().query(BudgetStore.TABLE_BUDGET, allColumns, BudgetStore.COLUMN_MONTH + "=" + month, null, null, null, null);
	  }
	  
	  public boolean existBudget(int month) {
		  Cursor cursor = getBudget(month);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) return true;
			  else return false;
		  } else {
			  return false;
		  }
	  }
	  
	  public boolean updateBudget(int id, int month, float available, float target) {
		  ContentValues values = new ContentValues();
		  values.put(BudgetStore.COLUMN_MONTH, month);
		  values.put(BudgetStore.COLUMN_AVAILABLE, available);
		  values.put(BudgetStore.COLUMN_TARGET, target);
		  int rowsAffected  = getDatabase().update(BudgetStore.TABLE_BUDGET, values, "_id=?", new String[]{String.valueOf(id)});
		  if (rowsAffected>0) {
			  return true;
		  } else {
			  return false;
		  }
	  }
	  
	  public void deleteBudget(int id) {
		  getDatabase().execSQL("DELETE FROM Budget WHERE _id=" + id);
	  }
	  
	  public boolean createExpense(String eDate, String expense, boolean hasProducts) {
		  float ticketExpense = Float.parseFloat(expense);
		  int monthNumber = getMonth(eDate);
		  boolean result = true;
		  getDatabase().beginTransaction();
		  try {
			  if (hasProducts) {
				  getDatabase().execSQL("UPDATE " + BudgetStore.TABLE_BUDGET + " SET " 
						  + BudgetStore.COLUMN_WITHDRAWN + "=" + BudgetStore.COLUMN_WITHDRAWN
						  + "+" + ticketExpense + ", "
						  + BudgetStore.COLUMN_DEVICE_WITHDRAWN + "=" + BudgetStore.COLUMN_DEVICE_WITHDRAWN
						  + "+" + ticketExpense
						  + " WHERE " + BudgetStore.COLUMN_MONTH + "=" + monthNumber);
				  ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext, getDatabase());
				  shoppingListRepository.emptyCommitted();
				  getDatabase().setTransactionSuccessful();
			  } else {
				  getDatabase().execSQL("UPDATE " + BudgetStore.TABLE_BUDGET + " SET " 
						  + BudgetStore.COLUMN_WALLET + "=" + BudgetStore.COLUMN_WALLET
						  + "+" + ticketExpense + ", "
						  + BudgetStore.COLUMN_DEVICE_WALLET + "=" + BudgetStore.COLUMN_DEVICE_WALLET
						  + "+" + ticketExpense
						  + " WHERE " + BudgetStore.COLUMN_MONTH + "=" + monthNumber);
				  getDatabase().setTransactionSuccessful();
			  }
		  }
		  catch (Exception e) {
			  result = false;
		  }
		  finally {
			  getDatabase().endTransaction();
		  }
		  return result;
	  }

	  public Cursor getCommonBudgetData() {
		  String[] columns = {BudgetStore.COLUMN_ID, BudgetStore.COLUMN_MONTH, BudgetStore.COLUMN_AVAILABLE, BudgetStore.COLUMN_TARGET, BudgetStore.COLUMN_WITHDRAWN, BudgetStore.COLUMN_WALLET};
		  mCursor = this.getDatabase().query(BudgetStore.TABLE_BUDGET, columns, null, null, null, null, null);
		  return mCursor;
	  }
	  
	  public Cursor getAllBudgets() {
		  mCursor = this.getDatabase().query(BudgetStore.TABLE_BUDGET, allColumns, null, null, null, null, BudgetStore.COLUMN_ID + " DESC");
		  return mCursor;
	  }
	  
	  private int getMonth(String dateTo) {
		  try {
				DateFormat d = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
				Date date = d.parse(dateTo);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				return calendar.get(Calendar.MONTH);
		  } catch (ParseException e) {
			  	return -1;
		  }
	  }
	  
	  public void syncBudget(int monthId, float available, float target, float withDrawn, float wallet) {
		  String query = "UPDATE " + BudgetStore.TABLE_BUDGET + " SET " 
				  + BudgetStore.COLUMN_AVAILABLE + "=" + available + ", "
				  + BudgetStore.COLUMN_TARGET + "=" + target + ", "
				  + BudgetStore.COLUMN_WITHDRAWN + "=" + BudgetStore.COLUMN_WITHDRAWN + " + "
				  + withDrawn + "-" + BudgetStore.COLUMN_LAST_WITHDRAWN + ", "
				  + BudgetStore.COLUMN_WALLET + "=" + BudgetStore.COLUMN_WALLET + " + "
				  + wallet + "-" + BudgetStore.COLUMN_LAST_WALLET
				  + " WHERE Month=" + monthId;
	      String sql = "UPDATE " + BudgetStore.TABLE_BUDGET + " SET "
				  + BudgetStore.COLUMN_LAST_WITHDRAWN + "=" + BudgetStore.COLUMN_WITHDRAWN + ", "
				  + BudgetStore.COLUMN_LAST_WALLET + "=" + BudgetStore.COLUMN_WALLET
				  + " WHERE Month=" + monthId;
		  getDatabase().beginTransaction();
		  getDatabase().execSQL(query);
		  getDatabase().execSQL(sql);
		  getDatabase().setTransactionSuccessful();
		  getDatabase().endTransaction();
	  }
	  
	  public void setLastValues() {
		  Cursor cursor = getAllBudgets();
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  while (!cursor.isAfterLast()) {
					  int monthId = cursor.getInt(cursor.getColumnIndex(BudgetStore.COLUMN_MONTH));
				      String sql = "UPDATE " + BudgetStore.TABLE_BUDGET + " SET "
							  + BudgetStore.COLUMN_LAST_WITHDRAWN + "=" + BudgetStore.COLUMN_WITHDRAWN + ", "
							  + BudgetStore.COLUMN_LAST_WALLET + "=" + BudgetStore.COLUMN_WALLET
							  + " WHERE Month=" + monthId;
				      getDatabase().execSQL(sql);
				      cursor.moveToNext();
				  }
			  }
		  }
	  }
	  
	  public int getLastBudgetMonth() {
		  int monthId = 0;
		  String query = "SELECT MAX(Month) FROM Budget";
		  Cursor cursor = getDatabase().rawQuery(query, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  monthId = cursor.getInt(0);
			  }
		  }
		  return monthId;
	  }
	  
}
