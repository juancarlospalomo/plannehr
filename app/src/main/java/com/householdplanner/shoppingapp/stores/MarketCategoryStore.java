package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

public class MarketCategoryStore {

	//Market Category table
	public static final String TABLE_MARKET_CATEGORY = "MarketCategory";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MARKET_ID = "MarketId";
	public static final String COLUMN_MARKET_CATEGORY_ID = "CategoryId";
	public static final String COLUMN_CATEGORY_ORDER = "CategoryOrder";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_MARKET_CATEGORY + " (" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_MARKET_ID + " integer, "
			+ COLUMN_MARKET_CATEGORY_ID + " integer, "
			+ COLUMN_CATEGORY_ORDER + " integer);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

	}
}
