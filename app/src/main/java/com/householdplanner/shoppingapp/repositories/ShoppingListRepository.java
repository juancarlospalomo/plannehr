package com.householdplanner.shoppingapp.repositories;

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
import com.householdplanner.shoppingapp.stores.ShoppingListStore.TypeOperation;

import java.util.Locale;

public class ShoppingListRepository {

    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mShoppingListStore;
    private Cursor mCursor;
    private Context mContext;

    private String[] allColumns = {ShoppingListStore.COLUMN_ID,
            ShoppingListStore.COLUMN_PRODUCT_MODIFIED,
            ShoppingListStore.COLUMN_PRODUCT_NAME,
            ShoppingListStore.COLUMN_MARKET,
            ShoppingListStore.COLUMN_AMOUNT,
            ShoppingListStore.COLUMN_UNIT_ID,
            ShoppingListStore.COLUMN_CATEGORY,
            ShoppingListStore.COLUMN_COMMITTED,
            ShoppingListStore.COLUMN_SEQUENCE,
            ShoppingListStore.COLUMN_SHOPPING_DATE,
            ShoppingListStore.COLUMN_OPERATION,
            ShoppingListStore.COLUMN_SYNC_TIMESTAMP};

    public ShoppingListRepository(Context context) {
        mShoppingListStore = new DatabaseHelper(context);
        mContext = context;
    }

    public ShoppingListRepository(Context context, SQLiteDatabase database) {
        mShoppingListStore = new DatabaseHelper(context);
        mContext = context;
        mDatabase = database;
    }

    protected void onDestroy() {
        this.close();
    }

    private void open() throws SQLException {
        mDatabase = mShoppingListStore.getWritableDatabase();
    }

    private SQLiteDatabase getDatabase() {
        if (mDatabase == null) {
            this.open();
        }
        return mDatabase;
    }

    public void close() {
        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
        if (mShoppingListStore != null) mShoppingListStore.close();
    }

    public boolean createProductItem(String productName,
                                     String market, String amount, int unitId, int categoryId, int sequence) {

        return createProductItem(1, productName, market, amount,
                unitId, categoryId, sequence, 0, TypeOperation.Add, util.getDateTime());
    }

    private boolean createProductItem(int modified, String productName,
                                      String market, String amount, int unitId, int categoryId, int sequence,
                                      int committed, TypeOperation operation, String syncDate) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, modified);
        values.put(ShoppingListStore.COLUMN_PRODUCT_NAME, util.capitalize(productName));
        if (!TextUtils.isEmpty(market))
            values.put(ShoppingListStore.COLUMN_MARKET, market.toLowerCase(Locale.getDefault()));
        values.put(ShoppingListStore.COLUMN_AMOUNT, amount);
        values.put(ShoppingListStore.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListStore.COLUMN_CATEGORY, categoryId);
        values.put(ShoppingListStore.COLUMN_COMMITTED, committed);
        values.put(ShoppingListStore.COLUMN_SEQUENCE, sequence);
        values.put(ShoppingListStore.COLUMN_SHOPPING_DATE, "");
        values.put(ShoppingListStore.COLUMN_OPERATION, operation.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, syncDate);
        long insertId = getDatabase().insert(ShoppingListStore.TABLE_LIST, null,
                values);

        if (insertId > 0) {
            return true;
        } else return false;
    }

    public void emptyCommitted() {
        boolean innerTransaction = false;
        if (!getDatabase().inTransaction()) {
            getDatabase().beginTransaction();
            innerTransaction = true;
        }
        try {
            Cursor cursor = getProductsCommitted();
            ProductHistoryRepository productHistoryRepository = new ProductHistoryRepository(mContext, getDatabase());
            String productName, market;
            int categoryId;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                    market = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
                    categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY));
                    Cursor product = productHistoryRepository.getProduct(productName, market);
                    if (product != null) {
                        if (product.moveToFirst()) {
                            int historyCategoryId = product.getInt(product.getColumnIndex(ProductHistoryStore.COLUMN_CATEGORY));
                            int id = product.getInt(product.getColumnIndex(ProductHistoryStore.COLUMN_ID));
                            if (historyCategoryId != categoryId) {
                                productHistoryRepository.updateProductItem(id, market, categoryId, 1, util.getDateTime(), -1);
                            }
                        } else {
                            productHistoryRepository.createProductItem(productName, market, categoryId, 1, util.getDateTime());
                        }
                    }
                    cursor.moveToNext();
                }
            }
            deleteCommittedProducts();

            if (innerTransaction) {
                getDatabase().setTransactionSuccessful();
            }
        } catch (Exception e) {
        } finally {
            if (innerTransaction) {
                getDatabase().endTransaction();
            }
        }
    }

    public boolean updateProductItem(int id, String productName,
                                     String market, String amount, int unitId, int categoryId) {
        boolean result = false;
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
        values.put(ShoppingListStore.COLUMN_PRODUCT_NAME, util.capitalize(productName));
        if (market != null)
            values.put(ShoppingListStore.COLUMN_MARKET, market.toLowerCase(Locale.getDefault()));
        else
            values.putNull(ShoppingListStore.COLUMN_MARKET);
        values.put(ShoppingListStore.COLUMN_AMOUNT, amount);
        values.put(ShoppingListStore.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListStore.COLUMN_CATEGORY, categoryId);
        values.put(ShoppingListStore.COLUMN_COMMITTED, 0);
        values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
        getDatabase().beginTransaction();
        long rowsAffected = getDatabase().update(ShoppingListStore.TABLE_LIST,
                values, "_id=" + id, null);
        if (rowsAffected > 0) {
            ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
            int historyId = historyRepository.getProductId(productName, market);
            if (historyId > 0) {
                result = historyRepository.updateProductItem(id, market, categoryId, 1, util.getDateTime(), -1);
                result = true;
            } else {
                result = true;
            }
        }
        if (result) {
            getDatabase().setTransactionSuccessful();
            getDatabase().endTransaction();
        } else {
            getDatabase().endTransaction();
        }

        return result;
    }

    public void copyToSupermarket(int id, String newMarket) {
        String selectSql, insertSql;
        selectSql = "SELECT 1, Name, ";
        insertSql = "INSERT INTO " + ShoppingListStore.TABLE_LIST
                + " (Modify, Name,";
        if (newMarket != null) {
            newMarket = newMarket.toLowerCase(Locale.getDefault());
            selectSql += "'" + newMarket + "',";

            insertSql += "Market,";
        }
        selectSql += " Amount, UnitId, Category, Comitted, Sequence, SDate, " + TypeOperation.Add.getValue() + ",'" + util.getDateTime() + "'"
                + " FROM " + ShoppingListStore.TABLE_LIST
                + " WHERE _id = " + id;
        insertSql += "Amount, UnitId, Category, Comitted, Sequence, SDate, op, SyncDate) ";
        getDatabase().execSQL(insertSql + selectSql);
    }

    public void moveToSupermaket(int id, String newMarket) {
        Cursor cursor = getDatabase().query(ShoppingListStore.TABLE_LIST,
                new String[]{ShoppingListStore.COLUMN_PRODUCT_NAME,
                        ShoppingListStore.COLUMN_MARKET}, "_id=" + id, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                String market = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
                ContentValues values = new ContentValues();
                if (newMarket != null) {
                    values.put(ShoppingListStore.COLUMN_MARKET, newMarket.toLowerCase(Locale.getDefault()));
                } else {
                    values.putNull(ShoppingListStore.COLUMN_MARKET);
                }
                values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
                values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
                values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
                getDatabase().beginTransaction();
                if (getDatabase().update(ShoppingListStore.TABLE_LIST, values,
                        ShoppingListStore.COLUMN_ID + "=" + id, null) > 0) {
                    ProductHistoryRepository historyRepository = new ProductHistoryRepository(mContext, getDatabase());
                    historyRepository.moveToSupermarket(productName, market, newMarket);
                    getDatabase().setTransactionSuccessful();
                }
                getDatabase().endTransaction();
            }
        }
    }

    public void renameSupermarket(String oldMarket, String newMarket) {
        ContentValues values = new ContentValues();
        if (newMarket != null) {
            newMarket = newMarket.toLowerCase(Locale.getDefault());
        }
        if (oldMarket != null) {
            oldMarket = oldMarket.toLowerCase(Locale.getDefault());
        }
        values.put(ShoppingListStore.COLUMN_MARKET, newMarket);
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
        values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
        getDatabase().update(ShoppingListStore.TABLE_LIST, values,
                ShoppingListStore.COLUMN_MARKET + "='" + oldMarket + "'", null);

    }

    public void unSetMarket(String oldMarket) {
        ContentValues values = new ContentValues();
        values.putNull(ShoppingListStore.COLUMN_MARKET);
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
        values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
        getDatabase().update(ShoppingListStore.TABLE_LIST, values,
                ShoppingListStore.COLUMN_MARKET + "='" + oldMarket.toLowerCase(Locale.getDefault()) + "'", null);
    }

    public boolean deleteProductItem(int id) {
        return deletePermanentProductItem(id);
    }

    public boolean deletePermanentProductItem(int id) {
        int rowsAffected = getDatabase().delete(ShoppingListStore.TABLE_LIST, "_id=" + id, null);
        if (rowsAffected > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void commitProduct(int id) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
        values.put(ShoppingListStore.COLUMN_COMMITTED, 1);
        values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
        getDatabase().update(ShoppingListStore.TABLE_LIST, values, ShoppingListStore.COLUMN_ID + "=" + id
                , null);
    }

    public void rollbackProduct(int id) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 1);
        values.put(ShoppingListStore.COLUMN_COMMITTED, 0);
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, util.getDateTime());
        values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Update.getValue());
        getDatabase().update(ShoppingListStore.TABLE_LIST, values, ShoppingListStore.COLUMN_ID + "=" + id
                , null);
    }

    public Cursor getProduct(String name) {
        return getDatabase().query(ShoppingListStore.TABLE_LIST, allColumns,
                ShoppingListStore.COLUMN_PRODUCT_NAME + "='" + name + "'", null, null, null, null);

    }

    public boolean existsProductsInSupermarket() {
        String selection = ShoppingListStore.COLUMN_MARKET + " is not null "
                + "AND " + ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue();
        Cursor cursor = getDatabase().query(ShoppingListStore.TABLE_LIST, allColumns,
                selection, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) return true;
            else return false;
        } else {
            return false;
        }
    }

    public Cursor getDeviceChangedProductItem() {
        mCursor = this.getDatabase().query(ShoppingListStore.TABLE_LIST,
                allColumns, ShoppingListStore.COLUMN_PRODUCT_MODIFIED + "=1", null, null, null, null);
        return mCursor;
    }

    public int getProductsCount() {
        String selection = ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue();
        Cursor cursor = this.getDatabase().query(ShoppingListStore.TABLE_LIST,
                allColumns, ShoppingListStore.COLUMN_COMMITTED + "=0 and " +
                        selection, null, null, null,
                ShoppingListStore.COLUMN_PRODUCT_NAME + " ASC");
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor getProductsNoCommittedByMarket(String marketName) {
        String selection = ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue();
        if ((marketName != null) && (!TextUtils.isEmpty(marketName)))
            selection += " AND " + ShoppingListStore.COLUMN_MARKET + "='" + marketName.toLowerCase(Locale.getDefault()) + "'";
        mCursor = this.getDatabase().query(ShoppingListStore.TABLE_LIST,
                allColumns, ShoppingListStore.COLUMN_COMMITTED + "=0 and " +
                        selection, null, null, null,
                ShoppingListStore.COLUMN_PRODUCT_NAME + " ASC");
        return mCursor;
    }

    public Cursor getProductsNotCommittedOrderedByCategory(int marketId, String marketName, boolean getNotSet) {
        if (marketId == 0) marketId = 1;

        String sql = "SELECT List._id, List.Name, List.Market, List.Amount, "
                + "List.UnitId, List.Category, List.Sequence, List.SDate, MarketCategory.CategoryOrder "
                + " FROM List LEFT JOIN MarketCategory ON List.Category = MarketCategory.CategoryId "
                + " WHERE List.Comitted = 0 AND List.op <> " + TypeOperation.Delete.getValue()
                + " AND MarketCategory.MarketId = " + marketId;
        if (!TextUtils.isEmpty(marketName)) {
            if (getNotSet) {
                sql += " AND (List.Market = '" + marketName.toLowerCase(Locale.getDefault()) + "'"
                        + " OR List.Market IS NULL OR List.Market='')";
            } else {
                sql += " AND List.Market = '" + marketName.toLowerCase(Locale.getDefault()) + "'";
            }
        }
        sql += " ORDER BY MarketCategory.CategoryOrder";
        mCursor = getDatabase().rawQuery(sql, null);
        return mCursor;
    }

    public Cursor getProductsCommitted() {
        return this.getDatabase().query(ShoppingListStore.TABLE_LIST, allColumns,
                ShoppingListStore.COLUMN_COMMITTED + "=1 and " +
                        ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue(), null, null, null,
                ShoppingListStore.COLUMN_SHOPPING_DATE + " DESC");
    }

    public boolean deleteCommittedProducts() {
        return getDatabase().delete(ShoppingListStore.TABLE_LIST, ShoppingListStore.COLUMN_COMMITTED + "=1", null) > 0;
    }

    public boolean existProductInNotCommittedList(String name) {
        boolean result = false;
        Cursor cursor = this.getDatabase().rawQuery("SELECT _id FROM List WHERE Name = '"
                + name + "' and " + ShoppingListStore.COLUMN_OPERATION + "<>" + TypeOperation.Delete.getValue(), null);
        if (cursor.getCount() > 0) result = true;
        return result;
    }

    public boolean endSyncProcess() {
        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 0);
            values.put(ShoppingListStore.COLUMN_OPERATION, TypeOperation.Unchanged.getValue());
            getDatabase().beginTransaction();
            getDatabase().delete(ShoppingListStore.TABLE_LIST, ShoppingListStore.COLUMN_OPERATION + "=" + TypeOperation.Delete.getValue(), null);
            getDatabase().update(ShoppingListStore.TABLE_LIST, values, null, null);
            getDatabase().setTransactionSuccessful();
            getDatabase().endTransaction();
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public boolean createTempTable() {
        boolean result = false;
        try {
            ShoppingListStore.createTemp(this.getDatabase());
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean createTempProductItem(String productName, String market, String amount,
                                         int unitId, int categoryId, int committed, int sequence, int operation, String syncDate) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 0);
        values.put(ShoppingListStore.COLUMN_PRODUCT_NAME, productName);
        values.put(ShoppingListStore.COLUMN_MARKET, market);
        values.put(ShoppingListStore.COLUMN_AMOUNT, amount);
        values.put(ShoppingListStore.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListStore.COLUMN_CATEGORY, categoryId);
        values.put(ShoppingListStore.COLUMN_COMMITTED, committed);
        values.put(ShoppingListStore.COLUMN_SEQUENCE, sequence);
        values.put(ShoppingListStore.COLUMN_OPERATION, operation);
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, syncDate);
        long insertId = getDatabase().insert("List_Temp", null, values);
        if (insertId > 0) return true;
        else return false;
    }

    public void insertRowsNotInList() {
        String sqlSelect = "SELECT List_Temp.Name, List_Temp.Market, " +
                "List_Temp.Amount, List_Temp.UnitId, " +
                "List_Temp.Category, List_Temp.Comitted, List_Temp.Sequence, List_Temp.op, " +
                "List_Temp.SyncDate " +
                "FROM List_Temp " +
                "WHERE List_Temp.op = " + TypeOperation.Add.getValue();

        Cursor cursor = getDatabase().rawQuery(sqlSelect, null);
        Cursor product = null;
        int unitId, categoryId, sequence, committed;
        String productName, market, amount, syncDate;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    boolean insertRecord = true;
                    product = getProduct(cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME)));
                    if (product != null) {
                        if (product.moveToFirst()) {
                            int id = product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_ID));
                            if (product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_MODIFIED)) == 1) {
                                String deviceSyncDate = product.getString(product.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                                String dbSyncDate = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                                if (util.compareDates(dbSyncDate, deviceSyncDate) == 1) {
                                    //Device has got the product, so we delete it
                                    //as he was entered in centralized db before this one
                                    deletePermanentProductItem(id);
                                    //we insert the record from db
                                    insertRecord = true;
                                } else {
                                    //we have inserted the record before the other users
                                    insertRecord = false;
                                }
                            } else {
                                deletePermanentProductItem(id);
                                insertRecord = true;
                            }
                        }
                    }
                    if (insertRecord) {
                        productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                        market = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
                        amount = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT));
                        unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID));
                        categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY));
                        committed = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_COMMITTED));
                        sequence = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_SEQUENCE));
                        syncDate = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                        createProductItem(0, productName, market, amount, unitId, categoryId, sequence, committed, TypeOperation.Unchanged, syncDate);
                    }
                    cursor.moveToNext();
                }
                if (product != null) product.close();
            }
        }
        if (cursor != null) cursor.close();
    }

    public void updateChangedRows() {
        String sqlSelect = "SELECT List_Temp.Name, List_Temp.Market, " +
                "List_Temp.Amount, List_Temp.UnitId, " +
                "List_Temp.Category, List_Temp.Comitted, List_Temp.Sequence, List_Temp.op, " +
                "List_Temp.SyncDate " +
                "FROM List_Temp " +
                "WHERE List_Temp.op = " + TypeOperation.Update.getValue();
        Cursor cursor = getDatabase().rawQuery(sqlSelect, null);
        Cursor product = null;
        int unitId, categoryId, sequence, committed;
        String productName, market, amount, syncDate, deviceSyncDate;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    productName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
                    market = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
                    amount = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT));
                    unitId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID));
                    categoryId = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_CATEGORY));
                    committed = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_COMMITTED));
                    sequence = cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_SEQUENCE));
                    syncDate = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                    product = getProduct(cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME)));
                    if (product != null) {
                        if (product.moveToFirst()) {
                            //Device has got the product, so we check the timestamp
                            //get the timestamp from centralized db
                            int id = product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_ID));
                            deviceSyncDate = product.getString(product.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                            //if timestamp db > timestamp device, then update the data
                            //in the device, because the db is newer
                            if (product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_MODIFIED)) == 1) {
                                //If it has been modified in the device too, the compare dates
                                if (util.compareDates(syncDate, deviceSyncDate) == 1) {
                                    updateChangedRow(id, productName, market, amount, unitId, categoryId, committed, sequence, TypeOperation.Update, syncDate);
                                }
                            } else {
                                updateChangedRow(id, productName, market, amount, unitId, categoryId, committed, sequence, TypeOperation.Update, syncDate);
                            }
                        } else {
                            createProductItem(0, productName, market, amount, unitId, categoryId, sequence, committed, TypeOperation.Unchanged, syncDate);
                        }
                    }
                    cursor.moveToNext();
                }
                if (product != null) product.close();
            }
        }
        if (cursor != null) cursor.close();
    }

    public void deleteRows() {
        String sqlSelect = "SELECT List_Temp.Name, List_Temp.Market, " +
                "List_Temp.Amount, List_Temp.UnitId, " +
                "List_Temp.Category, List_Temp.Comitted, List_Temp.Sequence, List_Temp.op, " +
                "List_Temp.SyncDate " +
                "FROM List_Temp " +
                "WHERE List_Temp.op = " + TypeOperation.Delete.getValue();

        Cursor cursor = getDatabase().rawQuery(sqlSelect, null);
        Cursor product = null;
        String syncDate, deviceSyncDate;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    syncDate = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                    product = getProduct(cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME)));
                    if (product != null) {
                        if (product.moveToFirst()) {
                            //Exists, then we compare the dates
                            //if timestamp in db is greater than timestamp in device
                            //then we delete it
                            int id = product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_ID));
                            //Compare dates only if the product has been changed in
                            //the device too.
                            if (product.getInt(product.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_MODIFIED)) == 1) {
                                deviceSyncDate = product.getString(product.getColumnIndex(ShoppingListStore.COLUMN_SYNC_TIMESTAMP));
                                //if timestamp db > timestamp device, then update the data
                                //in the device, because the db is newer
                                if (util.compareDates(syncDate, deviceSyncDate) == 1) {
                                    deletePermanentProductItem(id);
                                }
                            } else {
                                deletePermanentProductItem(id);
                            }
                        }
                    }
                    cursor.moveToNext();
                }
                if (product != null) product.close();
            }
        }
        if (cursor != null) cursor.close();
    }

    private void updateChangedRow(int id, String productName, String market, String amount,
                                  int unitId, int categoryId, int committed, int sequence, TypeOperation operation,
                                  String syncDate) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListStore.COLUMN_PRODUCT_MODIFIED, 0);
        values.put(ShoppingListStore.COLUMN_PRODUCT_NAME, productName);
        if (TextUtils.isEmpty(market)) {
            values.putNull(ShoppingListStore.COLUMN_MARKET);
        } else {
            values.put(ShoppingListStore.COLUMN_MARKET, market);
        }
        values.put(ShoppingListStore.COLUMN_AMOUNT, amount);
        values.put(ShoppingListStore.COLUMN_UNIT_ID, unitId);
        values.put(ShoppingListStore.COLUMN_CATEGORY, categoryId);
        values.put(ShoppingListStore.COLUMN_COMMITTED, committed);
        values.put(ShoppingListStore.COLUMN_SEQUENCE, sequence);
        values.put(ShoppingListStore.COLUMN_OPERATION, operation.getValue());
        values.put(ShoppingListStore.COLUMN_SYNC_TIMESTAMP, syncDate);
        getDatabase().update(ShoppingListStore.TABLE_LIST, values, "_id=" + id, null);
    }

}
