package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.stores.DatabaseHelper;
import com.householdplanner.shoppingapp.stores.MarketCategoryStore;
import com.householdplanner.shoppingapp.stores.MarketStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore.TypeOperation;

import java.util.ArrayList;
import java.util.Locale;

public class MarketRepository {
	  // Database fields
	  private SQLiteDatabase mDatabase;
	  private DatabaseHelper mMarketDbHelper;
	  private Cursor mCursor;
	  private Context mContext;
	  
	  public MarketRepository(Context context) {
		  mMarketDbHelper = new DatabaseHelper(context);
		  mContext = context;
	  }
	  
	  private void open() throws SQLException {
		  mDatabase = mMarketDbHelper.getWritableDatabase();
	  }

	  private SQLiteDatabase getDatabase() {
		  if (mDatabase == null) {
			  this.open();
		  }
		  return mDatabase;
	  }
	  
	  public void close() {
		  if (mCursor!=null) mCursor.close();
		  if (mDatabase!=null) mDatabase.close();
		  if (mMarketDbHelper!=null) mMarketDbHelper.close();
	  }

	  public boolean createMarketItem(String marketName, int categoriesNumber) {
		  int marketId = 1;
		  String query = "SELECT MAX(" + MarketStore.COLUMN_MARKET_ID + ") FROM " + MarketStore.TABLE_MARKET;
		  Cursor cursor = getDatabase().rawQuery(query, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  marketId = cursor.getInt(0);
				  marketId++;
			  }
		  }
		  ContentValues values = new ContentValues();
		  values.put(MarketStore.COLUMN_MARKET_ID, marketId);
		  values.put(MarketStore.COLUMN_MARKET_NAME, marketName.trim().toLowerCase(Locale.getDefault()));
		  getDatabase().beginTransaction();
		  long insertId = getDatabase().insert(MarketStore.TABLE_MARKET, null, values);
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
				  insertId = getDatabase().insert(MarketCategoryStore.TABLE_MARKET_CATEGORY, null, values);
				  if (insertId<=0) break;
			  }
		  }
		  if (insertId>0) {
			  getDatabase().setTransactionSuccessful();
			  getDatabase().endTransaction();
			  close();
			  return true;
		  }
          else {
        	  getDatabase().endTransaction();
        	  close();
        	  return false;
          }
	  }
	  
	  public Cursor getMarketsWithProducts() {
		  String sql = "SELECT " + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_ID + "," 
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_MARKET_ID + ","
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_MARKET_NAME + ", "
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_COLOR + " "
				  + "FROM " + MarketStore.TABLE_MARKET + " INNER JOIN " + ShoppingListStore.TABLE_LIST + " "
				  + "ON " + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_MARKET_NAME + "=" + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_MARKET + " "
				  + "WHERE " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_MARKET + " is not null "
				  + "AND " + ShoppingListStore.TABLE_LIST + "." + ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue() + " "
				  + "GROUP BY " +  MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_ID + ","
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_MARKET_ID + ","
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_MARKET_NAME + ", "
				  + MarketStore.TABLE_MARKET + "." + MarketStore.COLUMN_COLOR + " ";
		  
		  Cursor cursor = getDatabase().rawQuery(sql, null);
		  return cursor;
	  }
	  
	  public boolean replaceMarketCollection(ArrayList<ContentValues> rows) {
		  boolean result = false;
		  getDatabase().delete(MarketStore.TABLE_MARKET, null, null);
		  for (ContentValues value : rows) {
			  if (getDatabase().insert(MarketStore.TABLE_MARKET, null, value)>0) result = true;
		  }
		  return result;
	  }
	  
	  public void createVirtualMarket() {
		  int marketId = getMarketId(MarketStore.VIRTUAL_MARKET_NAME);
		  if (marketId==0) {
			  MarketStore.insertVirtualMarket(getDatabase());
		  }
	  }
	  
	  public boolean replaceMarketCategoriesCollection(ArrayList<ContentValues> rows) {
		  boolean result = false;
		  getDatabase().delete(MarketCategoryStore.TABLE_MARKET_CATEGORY, null, null);
		  for (ContentValues value : rows) {
			  if (getDatabase().insert(MarketCategoryStore.TABLE_MARKET_CATEGORY, null, value)>0) result = true;
		  }
		  return result;
	  }
	  
	  public boolean deleteMarketItem(int marketId, String marketName) {
		  int result;
		  getDatabase().beginTransaction();
		  result = getDatabase().delete(MarketCategoryStore.TABLE_MARKET_CATEGORY, MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId, null);
		  if (result>0) {
			  result = getDatabase().delete(MarketStore.TABLE_MARKET, MarketStore.COLUMN_MARKET_ID + "=" + marketId, null);
			  if (result>0) {
				  ShoppingListRepository listRepository = new ShoppingListRepository(mContext, getDatabase());
				  listRepository.unSetMarket(marketName);
				  ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
				  historyRepository.unSetMarket(marketName);
			  }
		  }
		  if (result>0) {
			  getDatabase().setTransactionSuccessful();
			  getDatabase().endTransaction();
			  return true;
		  } else {
			  getDatabase().endTransaction();
			  return false;
		  }
	  }
	  
	  public boolean renameMarket(int marketId, String oldMarket, String newMarketName) {
		  boolean result = true;
		  ContentValues values = new ContentValues();
		  SQLiteDatabase database = getDatabase();
		  
		  values.put(MarketStore.COLUMN_MARKET_NAME, newMarketName.toLowerCase(Locale.getDefault()));
		  
		  database.beginTransaction();
		  database.update(MarketStore.TABLE_MARKET, values, 
				  MarketStore.COLUMN_ID + "=" + marketId, null);
		  ShoppingListRepository listRepository = new ShoppingListRepository(mContext, getDatabase());
		  listRepository.renameSupermarket(oldMarket, newMarketName);
		  
		  ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
		  historyRepository.renameSupermarket(oldMarket, newMarketName);
		  
		  database.setTransactionSuccessful();
		  database.endTransaction();
		  
		  return result;
	  }
	  
	  public Cursor getAllMarkets() {
		  mCursor = this.getDatabase().query(MarketStore.TABLE_MARKET, 
				  new String[] {MarketStore.COLUMN_ID, MarketStore.COLUMN_MARKET_ID, MarketStore.COLUMN_MARKET_NAME, MarketStore.COLUMN_COLOR}, 
				  MarketStore.COLUMN_MARKET_NAME + "<>'a'" , null, null, null, MarketStore.COLUMN_MARKET_NAME);
		  return mCursor;
	  }
	  
	  public String getMarketName(int id) {
		  String marketName = null;
		  String sql = "SELECT " + MarketStore.COLUMN_MARKET_NAME
				  + " FROM " + MarketStore.TABLE_MARKET
				  + " WHERE " + MarketStore.COLUMN_MARKET_ID + "=" + id;
		  Cursor cursor = getDatabase().rawQuery(sql, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  marketName = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_NAME));
			  }
		  }
		  return marketName; 
	  }
	  
	  public int getMarketId(String name) {
		  int marketId = 0;
		  String sql = "SELECT " + MarketStore.COLUMN_MARKET_ID
				  + " FROM " + MarketStore.TABLE_MARKET
				  + " WHERE " + MarketStore.COLUMN_MARKET_NAME + "='" + name.trim() + "'";
		  Cursor cursor = getDatabase().rawQuery(sql, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  marketId = cursor.getInt(cursor.getColumnIndex(MarketStore.COLUMN_MARKET_ID));
			  }
		  }
		  return marketId; 
	  }
	  
	  public Integer getMarketColor(String name) {
		  Integer color = null;
		  String sql = "SELECT " + MarketStore.COLUMN_COLOR
				  + " FROM " + MarketStore.TABLE_MARKET
				  + " WHERE " + MarketStore.COLUMN_MARKET_NAME + "='" + name + "'";
		  Cursor cursor = getDatabase().rawQuery(sql, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  String value = cursor.getString(cursor.getColumnIndex(MarketStore.COLUMN_COLOR));
				  if (value!=null) {
					  color = Integer.parseInt(value);
				  }
			  }
		  }
		  return color; 
	  }
	  
	  public Cursor getMarketCategories(int marketId) {
		  Cursor cursor = null;
		  String sql = "SELECT " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID 
				  + " FROM " + MarketCategoryStore.TABLE_MARKET_CATEGORY
				  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId
				  + " AND " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + "<>0"
				  + " ORDER BY " + MarketCategoryStore.COLUMN_CATEGORY_ORDER;
		  cursor = getDatabase().rawQuery(sql, null);
		  return cursor;
	  }
	  
	  public Cursor getMarketCategories() {
		  Cursor cursor = null;
		  String sql = "SELECT " + MarketCategoryStore.COLUMN_MARKET_ID + ", "
				  + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + ", "
				  + MarketCategoryStore.COLUMN_CATEGORY_ORDER
				  + " FROM " + MarketCategoryStore.TABLE_MARKET_CATEGORY;
		  cursor = getDatabase().rawQuery(sql, null);
		  return cursor;
	  }
	  
	  public void setColor(int marketId, int color) {
		  ContentValues values = new ContentValues();
		  values.put(MarketStore.COLUMN_COLOR, color);
	  }
	  
	  public boolean updateMarketCategoryOrder(int marketId, int categoryId, int sequence) {
		  ContentValues values = new ContentValues();
		  values.put(MarketCategoryStore.COLUMN_CATEGORY_ORDER, sequence);
		  int rowsAffected = getDatabase().update(MarketCategoryStore.TABLE_MARKET_CATEGORY, values, 
				  MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId + " AND " +
				  MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + "=" + categoryId, null);
		  if (rowsAffected>0) {
			  return true;
		  } else {
			  return false;
		  }
	  }
	  
	  public boolean updateMarketCategoryId(int marketId, int categoryId, int categoryOrder) {
		  ContentValues values = new ContentValues();
		  values.put(MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID, categoryId);
		  int rowsAffected = getDatabase().update(MarketCategoryStore.TABLE_MARKET_CATEGORY, values, 
				  MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId + " AND " +
				  MarketCategoryStore.COLUMN_CATEGORY_ORDER + "=" + categoryOrder, null);
		  if (rowsAffected>0) {
			  return true;
		  } else {
			  return false;
		  }
	  }
	  
	  public void recalculateOrderMarketCategory(int currentCategoryOrder, int firstCategoryOrder, 
			  int currentCategoryId, int marketId) {
		  
		  //if marketId = 0, there's not supermarket selected
		  //so, we update the generic one
		  if (marketId==0) marketId = 1;
		  /*
		   * from = currentCategoryOrder, to = _firstCategoryOrder
		   */
		  getDatabase().beginTransaction();
		  for (int index=currentCategoryOrder; index>firstCategoryOrder; index--) {
			  upOrderMarketCategory(marketId, index);
		  }
		  //MarketCategory.CategoryId[_firstCategory] = currentCategoryId
		  updateMarketCategoryId(marketId, currentCategoryId, firstCategoryOrder);
		  repairInconsistencies(marketId);
		  getDatabase().setTransactionSuccessful();
		  getDatabase().endTransaction();
	  }
	  
	  public void upOrderMarketCategory(int marketId, int categoryOrder) {
		  String sqlSelect = "SELECT " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID 
				  + " FROM " + MarketCategoryStore.TABLE_MARKET_CATEGORY 
				  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId + " AND " 
				  + MarketCategoryStore.COLUMN_CATEGORY_ORDER + "=" + (categoryOrder- 1);
		  
		  String sql = "UPDATE " + MarketCategoryStore.TABLE_MARKET_CATEGORY 
				  + " SET " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + " = (" + sqlSelect + ")"
				  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId + " AND " 
				  + MarketCategoryStore.COLUMN_CATEGORY_ORDER + "=" + categoryOrder; 
		  
		  getDatabase().execSQL(sql);
	  }
	  
	  public void repairInconsistencies(int marketId) {
		  String sql = "SELECT " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + ", COUNT(*)"
				  + " FROM " + MarketCategoryStore.TABLE_MARKET_CATEGORY
				  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId 
				  + " GROUP BY " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID
				  + " HAVING COUNT(*)>1"
				  + " ORDER BY " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID;

		  Cursor cursor = getDatabase().rawQuery(sql, null);
		  if (cursor!=null) {
			  if (cursor.moveToFirst()) {
				  sql = "SELECT " + MarketCategoryStore.COLUMN_ID + "," + MarketCategoryStore.COLUMN_CATEGORY_ORDER
						  + " FROM " + MarketCategoryStore.TABLE_MARKET_CATEGORY
						  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId;
				  cursor = getDatabase().rawQuery(sql, null);
				  if (cursor!=null) {
					  if (cursor.moveToFirst()) {
						  int index = 0;
						  int id;
						  while (!cursor.isAfterLast()) {
							  id = cursor.getInt(cursor.getColumnIndex(MarketCategoryStore.COLUMN_ID));
							  sql = "Update " + MarketCategoryStore.TABLE_MARKET_CATEGORY
									  + " SET " + MarketCategoryStore.COLUMN_MARKET_CATEGORY_ID + "=" + index
									  + " WHERE " + MarketCategoryStore.COLUMN_MARKET_ID + "=" + marketId
									  + " AND " + MarketCategoryStore.COLUMN_ID + "=" + id;
							  getDatabase().execSQL(sql);
							  cursor.moveToNext();
							  index++;
						  }
					  }
				  }
				  
			  }
		  }
		  if (cursor!=null) cursor.close();
	  }
	  
}
