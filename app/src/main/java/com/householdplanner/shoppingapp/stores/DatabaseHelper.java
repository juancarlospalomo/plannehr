package com.householdplanner.shoppingapp.stores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ShoppingList.db";
	private static final int DATABASE_VERSION = 4;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		ShoppingListStore.onCreate(database);
		ProductHistoryStore.onCreate(database);
		BudgetStore.onCreate(database);
		MarketCategoryStore.onCreate(database);
		MarketStore.onCreate(database);
	}
	
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		ProductHistoryStore.onUpgrade(database, oldVersion, newVersion);
		ShoppingListStore.onUpgrade(database, oldVersion, newVersion);
		BudgetStore.onUpgrade(database, oldVersion, newVersion);
		MarketCategoryStore.onUpgrade(database, oldVersion, newVersion);
		MarketStore.onUpgrade(database, oldVersion, newVersion);
	}
	
}
