package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

public class BudgetStore {

	//Budget table
	public static final String TABLE_BUDGET = "Budget";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MONTH = "Month";
	public static final String COLUMN_AVAILABLE = "Available";
	public static final String COLUMN_TARGET = "Target";
	public static final String COLUMN_WITHDRAWN = "WithDrawn";
	public static final String COLUMN_LAST_WITHDRAWN = "LWithDrawn";
	public static final String COLUMN_WALLET = "Wallet";
	public static final String COLUMN_LAST_WALLET = "LWallet";
	public static final String COLUMN_DEVICE_WITHDRAWN = "DeviceWithDrawn";
	public static final String COLUMN_DEVICE_WALLET = "DeviceWallet";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_BUDGET
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_MONTH + " integer, "
			+ COLUMN_AVAILABLE + " real, "
			+ COLUMN_TARGET + " real, "
			+ COLUMN_WITHDRAWN + " real, "
			+ COLUMN_WALLET + " real, "
			+ COLUMN_DEVICE_WITHDRAWN + " real, "
			+ COLUMN_DEVICE_WALLET + " real, "
			+ COLUMN_LAST_WITHDRAWN + " real, " 
			+ COLUMN_LAST_WALLET + " real);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

	}
}
