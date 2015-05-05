package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

public class ShoppingListStore {

	public enum TypeOperation {
		Add(1),
		Update(2),
		Delete(3),
		Unchanged(4);
		
		private final int _value;
		private TypeOperation(int value) {
			this._value = value;
		}
		
		public int getValue() {
			return _value;
		}
	}
	
	public static final String TABLE_LIST = "List";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PRODUCT_MODIFIED = "Modify";
	public static final String COLUMN_PRODUCT_NAME = "Name";
	public static final String COLUMN_MARKET = "Market";
	public static final String COLUMN_AMOUNT = "Amount";
	public static final String COLUMN_UNIT_ID = "UnitId";
	public static final String COLUMN_CATEGORY = "Category";
	public static final String COLUMN_COMMITTED = "Comitted";
	public static final String COLUMN_SEQUENCE = "Sequence";
	public static final String COLUMN_SHOPPING_DATE = "SDate";
	public static final String COLUMN_OPERATION = "op";
	public static final String COLUMN_SYNC_TIMESTAMP = "SyncDate";
	
	//Database creation sql statement
	private static final String SQL_TABLE_CREATE = "create table "
			+ TABLE_LIST + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PRODUCT_MODIFIED + " integer, "
			+ COLUMN_PRODUCT_NAME + " text COLLATE NOCASE, "
			+ COLUMN_MARKET + " text, "
			+ COLUMN_AMOUNT + " text, "
			+ COLUMN_UNIT_ID + " integer, " 
			+ COLUMN_CATEGORY + " integer, "
			+ COLUMN_COMMITTED + " integer, "
			+ COLUMN_SEQUENCE + " integer, " 
			+ COLUMN_SHOPPING_DATE + " date, "
			+ COLUMN_OPERATION + " integer, " 
			+ COLUMN_SYNC_TIMESTAMP + " date);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
	}
			
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion<2) {
			onUpgradeV2(db);
		}
	}
	
	public static void createTemp(SQLiteDatabase db) {
		final String SQL_TEMP_TABLE_CREATE = "create temp table List_Temp"
				+ "(" + COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_PRODUCT_MODIFIED + " integer, "
				+ COLUMN_PRODUCT_NAME + " text COLLATE NOCASE, "
				+ COLUMN_MARKET + " text, "
				+ COLUMN_AMOUNT + " text, "
				+ COLUMN_UNIT_ID + " integer, " 
				+ COLUMN_CATEGORY + " integer, "
				+ COLUMN_COMMITTED + " integer, "
				+ COLUMN_SEQUENCE + " integer, " 
				+ COLUMN_SHOPPING_DATE + " date, "
				+ COLUMN_OPERATION + " integer, " 
				+ COLUMN_SYNC_TIMESTAMP + " date);";
		db.execSQL(SQL_TEMP_TABLE_CREATE);
	}
	
	private static void onUpgradeV2(SQLiteDatabase database) {
		String sql = "ALTER TABLE " + TABLE_LIST + " RENAME TO " + TABLE_LIST + "_old;";
		database.execSQL(sql);
		database.execSQL(SQL_TABLE_CREATE);
		database.execSQL("INSERT INTO " + TABLE_LIST + "(Modify, Name, Market, Amount, " 
				+ "UnitId, Category, Comitted, Sequence, SDate, op, SyncDate) " 
				+ "SELECT Modify, Name, Brand, Amount, UnitId, Category, Comitted, " 
				+ "Sequence, SDate, op, SyncDate FROM " + TABLE_LIST + "_old;");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST + "_old");
	}
	
}
