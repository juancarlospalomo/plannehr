package com.householdplanner.shoppingapp.stores;

import android.database.sqlite.SQLiteDatabase;

public class TicketStore {
	//Ticket table
	public static final String TABLE_TICKET = "Ticket";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "TDate";
	public static final String COLUMN_EXPENSE = "Expense";

	// Database creation SQL statement
	private static final String SQL_TABLE_CREATE = "create table " 
			+ TABLE_TICKET
			+ "(" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_DATE + " text, "
			+ COLUMN_EXPENSE + " text);";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_TABLE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		
	}
}
