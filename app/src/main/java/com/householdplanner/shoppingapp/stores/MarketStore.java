package com.householdplanner.shoppingapp.stores;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MarketStore {

	public static final String VIRTUAL_MARKET_NAME = "a";
	
	//Market table
	public static final String TABLE_MARKET = "Market";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MARKET_ID = "MarketId";
	public static final String COLUMN_MARKET_NAME = "Name";
	public static final String COLUMN_COLOR = "Color";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_MARKET + " (" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_MARKET_ID + " integer, "
			+ COLUMN_MARKET_NAME + " text COLLATE NOCASE, " 
			+ COLUMN_COLOR + " integer);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
		insertVirtualMarket(database);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if (oldVersion<3) {
			onUpgradeV3(database);
		}
		if (oldVersion<4) {
			onUpgradeV4(database);
		}
	}
	
	private static void onUpgradeV3(SQLiteDatabase database) {
		insertVirtualMarket(database);
	}

	private static void onUpgradeV4(SQLiteDatabase database) {
		String sql = "ALTER TABLE " + TABLE_MARKET + " RENAME TO " + TABLE_MARKET + "_old;";
		database.execSQL(sql);
		database.execSQL(SQL_TABLE_CREATE);
		database.execSQL("INSERT INTO " + TABLE_MARKET + "(" + COLUMN_MARKET_ID + ","
				+ COLUMN_MARKET_NAME + "," + COLUMN_COLOR + ") "
				+ "SELECT " + COLUMN_MARKET_ID + "," + COLUMN_MARKET_NAME + ",null " 
				+ "FROM " + TABLE_MARKET + "_old;");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKET + "_old");
	}
	
	public static void insertVirtualMarket(SQLiteDatabase database) {
		  String query = "SELECT MAX(" + MarketStore.COLUMN_MARKET_ID + ") FROM " + MarketStore.TABLE_MARKET;
		  Cursor cursor = database.rawQuery(query, null);
		  int marketId = 1;
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  marketId = cursor.getInt(0);
				  marketId++;
			  }
		  }
		  ContentValues values = new ContentValues();
		  values.put(MarketStore.COLUMN_MARKET_ID, marketId);
		  values.put(MarketStore.COLUMN_MARKET_NAME, VIRTUAL_MARKET_NAME); //For appearing first.
		  database.beginTransaction();
		  long insertId = database.insert(TABLE_MARKET, null, values);
		  int categoriesNumber = 11;
		  if (insertId>0) {
			  for(int index=0; index<categoriesNumber; index++) {
				  values = new ContentValues();
				  values.put(MarketCategoryStore.COLUMN_MARKET_ID, marketId);
				  values.put(MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID, index);
				  if (index==0) {
					  //To products without category defined will appear in the last positions
					  values.put(MarketCategoryStore.COLUMN_CATEGORY_ORDER, 30);
				  } else {
					  values.put(MarketCategoryStore.COLUMN_CATEGORY_ORDER, index);
				  }
				  insertId = database.insert(MarketCategoryStore.TABLE_MARKET_CATEGORY, null, values);
				  if (insertId<=0) break;
			  }
		  }
		  if (insertId>0) {
			  database.setTransactionSuccessful();
			  database.endTransaction();
		  }
	}
	
}
