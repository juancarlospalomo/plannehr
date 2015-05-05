package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

public class WalletStore {

	//Wallet table
	public static final String TABLE_WALLET = "Wallet";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MONEY = "Money";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_WALLET
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_MONEY + " text);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		
	}
}
