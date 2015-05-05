package com.householdplanner.shoppingapp.repositories;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;

import java.util.Locale;

public class ProductHistoryRepository {

	public static final int USUAL_CONFIRMED = 1;
	public static final int USUAL_PENDING = 3;
	public static final int UNUSUAL_CONFIRMED = 0;
	public static final int UNUSUAL_PENDING = 2;

	private Context mContext;
	// Database fields
	private SQLiteDatabase mDatabase;
	private DatabaseHelper mDatabaseHelper;
	private Cursor mCursor;
	  
	private String[] allColumns = {ProductHistoryStore.COLUMN_ID,
		ProductHistoryStore.COLUMN_PRODUCT_NAME,
	    ProductHistoryStore.COLUMN_MARKET, 
	    ProductHistoryStore.COLUMN_CATEGORY, 
	    ProductHistoryStore.COLUMN_SEQUENCE,
	    ProductHistoryStore.COLUMN_LAST_DATE,
	    ProductHistoryStore.COLUMN_SUGGESTION
	};

	public ProductHistoryRepository(Context context) {
		mContext = context;
		mDatabaseHelper = new DatabaseHelper(context);
	}
	  
	public ProductHistoryRepository(Context context, SQLiteDatabase database) {
		mContext = context;
		mDatabaseHelper = new DatabaseHelper(context);
		mDatabase = database;
	}
	  
	private void open() throws SQLException {
		mDatabase = mDatabaseHelper.getWritableDatabase();
	}

	public SQLiteDatabase getDatabase() {
		if ((mDatabase!=null) && (mDatabase.isOpen())) {
			return mDatabase;
		} else {
			this.open();
			return mDatabase;
		}
	}
	  
	public void close() {
		if (mCursor!=null) mCursor.close();
		if (mDatabase!=null) mDatabase.close();
		if (mDatabaseHelper!=null) mDatabaseHelper.close();
	}
	  
	public boolean createProductItem(String productName, 
			  String market, int categoryId, int changed, String lastDate) {
		ContentValues values = new ContentValues();
		values.put(ProductHistoryStore.COLUMN_PRODUCT_NAME, productName);
		if (!TextUtils.isEmpty(market)) {
			values.put(ProductHistoryStore.COLUMN_MARKET, market);
		}
		values.put(ProductHistoryStore.COLUMN_CATEGORY, categoryId);
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, changed);
		values.put(ProductHistoryStore.COLUMN_LAST_DATE, lastDate);
		values.put(ProductHistoryStore.COLUMN_SUGGESTION, USUAL_CONFIRMED);
		long insertId = getDatabase().insert(ProductHistoryStore.TABLE_PRODUCT_HISTORY, null,
		        values);
		if (insertId>0) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean updateProductItem(int id, String market, int categoryId, int sequence, String lastDate, 
			int suggest) {
		boolean result = false;
		ContentValues values = new ContentValues();
		if (!TextUtils.isEmpty(market)) {
			values.put(ProductHistoryStore.COLUMN_MARKET, market); 
		} else {
			values.putNull(ProductHistoryStore.COLUMN_MARKET);
		}
		values.put(ProductHistoryStore.COLUMN_CATEGORY, categoryId);
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, sequence);
		values.put(ProductHistoryStore.COLUMN_LAST_DATE, lastDate);
		if (suggest!=-1) values.put(ProductHistoryStore.COLUMN_SUGGESTION, suggest);
		if (getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values, "_id=" + id, null)>0) {
			result = true;
		}
		return result;
	}
	
	public void deleteProduct(int id) {
		getDatabase().delete(ProductHistoryStore.TABLE_PRODUCT_HISTORY, 
				ProductHistoryStore.COLUMN_ID + "=" + id , null);
	}
	
	public void setAllRowsUnchanged() {
		ContentValues values = new ContentValues();
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, 0);
		getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values, null, null);
	}
	
	public void renameSupermarket(String oldMarket, String newMarket) {
		ContentValues values = new ContentValues();
		values.put(ProductHistoryStore.COLUMN_MARKET, newMarket);
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, 1);
		if (getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values,
			  ShoppingListStore.COLUMN_MARKET + "='" + oldMarket + "'", null)>0) {
		}
	  
	}
	
	public void moveToSupermarket(String product, String oldMarket, String newMarket) {
		ContentValues values = new ContentValues();
		if (newMarket!=null) {
			values.put(ProductHistoryStore.COLUMN_MARKET, newMarket.toLowerCase(Locale.getDefault()));
		} else {
			values.putNull(ProductHistoryStore.COLUMN_MARKET);
		}
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, 1);
		String selection = ShoppingListStore.COLUMN_PRODUCT_NAME + "='" + product + "' AND ";
		if (oldMarket==null) {
			selection += ShoppingListStore.COLUMN_MARKET + " IS NULL";
		} else {
			selection += ShoppingListStore.COLUMN_MARKET + "='" + oldMarket + "'";
		}
		values.put(ProductHistoryStore.COLUMN_LAST_DATE, util.getDateTime());
		if (getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values,
			  selection, null)>0) {
		}
	}
	
	public void unSetMarket(String oldMarket) {
		ContentValues values = new ContentValues();
		values.putNull(ProductHistoryStore.COLUMN_MARKET);
		values.put(ProductHistoryStore.COLUMN_SEQUENCE, 1);
		if (getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values,
				  ProductHistoryStore.COLUMN_MARKET + "='" + oldMarket.toLowerCase(Locale.getDefault()) + "'", null)>0) {
		}
	}
	  
	public boolean setSuggest(int id, boolean usual) {
		ContentValues values = new ContentValues();
		if (usual)
			values.put(ProductHistoryStore.COLUMN_SUGGESTION, USUAL_CONFIRMED);
		else {
			values.put(ProductHistoryStore.COLUMN_SUGGESTION, UNUSUAL_CONFIRMED);
		} 
		if (getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values, "_id=" + id, null)>0) {
			return true;
		}
		else {
			return false;
		}
	}

	public Cursor getMyProductsList() {
		String sql = "SELECT " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_ID + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + "," 
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_MARKET + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_CATEGORY + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_SEQUENCE + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_LAST_DATE + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_SUGGESTION
			  + " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
			  + " LEFT JOIN " + ShoppingListStore.TABLE_LIST 
			  + " ON " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + "="
			  + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_PRODUCT_NAME
			  + " AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_OPERATION + "<>" + ShoppingListStore.TypeOperation.Delete.getValue()
			  + " WHERE (" + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_ID + " IS NULL)"
			  + " ORDER BY " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_SUGGESTION + " DESC "
			  + ", " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME;
		mCursor = this.getDatabase().rawQuery(sql, null);
		return mCursor;
	}
	
	public int getProductId(String productName, String market) {
		String sql = "SELECT " + ProductHistoryStore.COLUMN_ID + " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
				  + " WHERE " + ProductHistoryStore.COLUMN_PRODUCT_NAME + "='" + productName + "'";
		if (market!=null) {
			sql += " AND " + ProductHistoryStore.COLUMN_MARKET + "='" + market + "'";
		} else {
			sql += " AND (" + ProductHistoryStore.COLUMN_MARKET + " is null "
					+ "OR " + ProductHistoryStore.COLUMN_MARKET + "='')";
		}
		Cursor cursor = getDatabase().rawQuery(sql, null);
		if ((cursor!=null) && (cursor.getCount()>0)) {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	public Cursor getProduct(String productName, String market) {
		String query = "SELECT " + ProductHistoryStore.COLUMN_ID + "," + ProductHistoryStore.COLUMN_PRODUCT_NAME + ", " + ProductHistoryStore.COLUMN_MARKET + ", " 
			+ ProductHistoryStore.COLUMN_CATEGORY + ", " + ProductHistoryStore.COLUMN_LAST_DATE + ", "
			+ ProductHistoryStore.COLUMN_SEQUENCE + ", " + ProductHistoryStore.COLUMN_SUGGESTION
			+ " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
			+ " WHERE " + ProductHistoryStore.COLUMN_PRODUCT_NAME + "='" + productName + "'";
		if (!TextUtils.isEmpty(market)) 
			query+= " AND " + ProductHistoryStore.COLUMN_MARKET + "='" + market + "'";
		Cursor cursor = getDatabase().rawQuery(query, null);
		return cursor;
	}
	
	public Cursor getProduct(int id) {
		String query = "SELECT " + ProductHistoryStore.COLUMN_PRODUCT_NAME + ", " + ProductHistoryStore.COLUMN_MARKET + ", " 
				+ ProductHistoryStore.COLUMN_CATEGORY + ", " + ProductHistoryStore.COLUMN_SEQUENCE + ", " 
				+ ProductHistoryStore.COLUMN_LAST_DATE + ", " + ProductHistoryStore.COLUMN_SUGGESTION
				+ " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
				+ " WHERE " + ProductHistoryStore.COLUMN_ID + "=" + id;
			Cursor cursor = getDatabase().rawQuery(query, null);
			return cursor;
	}
	
	public Cursor getAllProductItem() {
		mCursor = this.getDatabase().query(ProductHistoryStore.TABLE_PRODUCT_HISTORY, 
				  allColumns, null, null, null, null, null);
		return mCursor;
	}
	  
	public Cursor getChangedProducts() {
		mCursor = this.getDatabase().query(ProductHistoryStore.TABLE_PRODUCT_HISTORY, 
			  allColumns, ProductHistoryStore.COLUMN_SEQUENCE + "=1", null, null, null, "_id");
		return mCursor;
	}
	  
	public Cursor GetProductFilteredByName(String pattern) {
		String sql = "SELECT " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_ID + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + "," 
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_MARKET + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_CATEGORY + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_SEQUENCE + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_LAST_DATE
			  + " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
			  + " LEFT JOIN " + ShoppingListStore.TABLE_LIST 
			  + " ON " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + "="
			  + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_PRODUCT_NAME 
			  + " AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_OPERATION + "<>" + ShoppingListStore.TypeOperation.Delete.getValue()
			  + " WHERE " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + " LIKE '" + pattern + "%'"
			  + " AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_ID + " IS NULL"
			  + " ORDER BY " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME;
		mCursor = this.getDatabase().rawQuery(sql, null);
		return mCursor;
	}
	
	public Cursor getProductSuggestions(String pattern) {
		String sql = "SELECT " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_ID + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 + ","
			  + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA
			  + " FROM " + ProductHistoryStore.TABLE_PRODUCT_HISTORY
			  + " LEFT JOIN " + ShoppingListStore.TABLE_LIST 
			  + " ON " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + "="
			  + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_PRODUCT_NAME 
			  + " AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_OPERATION + "<>" + ShoppingListStore.TypeOperation.Delete.getValue()
			  + " WHERE " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME + " LIKE '" + pattern + "%'"
			  + " AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_ID + " IS NULL"
			  + " ORDER BY " + ProductHistoryStore.TABLE_PRODUCT_HISTORY + "." + ProductHistoryStore.COLUMN_PRODUCT_NAME;
		mCursor = this.getDatabase().rawQuery(sql, null);
		return mCursor;
	}

	private void updateChangedRow(int id, int categoryId, int sequence, String lastDate, int suggest) {
		ContentValues values = new ContentValues();
	    values.put(ProductHistoryStore.COLUMN_CATEGORY, categoryId);
	    values.put(ProductHistoryStore.COLUMN_SEQUENCE, sequence);
	    values.put(ProductHistoryStore.COLUMN_LAST_DATE, lastDate);
	    values.put(ProductHistoryStore.COLUMN_SUGGESTION, suggest);
	    getDatabase().update(ProductHistoryStore.TABLE_PRODUCT_HISTORY, values, "_id=" + id, null);
	}
	
	private void removeTempRow(int id) {
	    getDatabase().delete("History_Temp", "_id=" + id, null);
	}
	
	public void updateRowsInHistory() {
		String sqlSelect = "SELECT ProductHistory._id, History_Temp.Category, History_Temp.Sequence, History_Temp._id, History_Temp.LastDate, History_Temp.Suggest " + 
			  		"FROM History_Temp INNER JOIN ProductHistory " +
					"ON History_Temp.Name = ProductHistory.Name AND History_Temp.Market = ProductHistory.Market " + 
					"WHERE History_Temp.Category <> ProductHistory.Category OR " +
					"History_Temp.Sequence <> ProductHistory.Sequence";
		  
		Cursor cursor = getDatabase().rawQuery(sqlSelect, null);
        if (cursor!=null) {
        	if (cursor.moveToFirst()) {
        		while (!cursor.isAfterLast()) {
        			int id = cursor.getInt(0); //id
        			int categoryId = cursor.getInt(1);
        			int sequence = cursor.getInt(2);
        			int tempId = cursor.getInt(3);
        			String lastDate = cursor.getString(4);
         			int suggest = cursor.getInt(5);
        			updateChangedRow(id, categoryId, sequence, lastDate, suggest);
        			removeTempRow(tempId);
        			cursor.moveToNext();
        		}
        	}
        	cursor.close();
        }	
	}
	
	public void insertRowsNotInHistory() {
		String sqlSelect = "SELECT History_Temp.Name, History_Temp.Market, " +
				"History_Temp.Category, History_Temp.Sequence, History_Temp.LastDate, History_Temp.Suggest " + 
		  		"FROM History_Temp";
		String query = "INSERT INTO ProductHistory (Name, Market, Category, Sequence, LastDate, Suggest) " + sqlSelect;
		getDatabase().execSQL(query);
	}

}
