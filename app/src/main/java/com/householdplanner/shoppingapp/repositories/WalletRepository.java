package com.householdplanner.shoppingapp.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.stores.DatabaseHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WalletRepository {

    // Database fields
    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDatabaseHelper;
    private Context mContext;

    private String[] allColumns = {ShoppingListContract.BudgetEntry._ID,
            ShoppingListContract.BudgetEntry.COLUMN_MONTH,
            ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE,
            ShoppingListContract.BudgetEntry.COLUMN_TARGET,
            ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN,
            ShoppingListContract.BudgetEntry.COLUMN_WALLET,
            ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN,
            ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET
    };

    public WalletRepository(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
    }

    private void open() throws SQLException {
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        if (mDatabase == null) {
            this.open();
        }
        return mDatabase;
    }

    public void close() {
        if (mDatabase != null) mDatabase.close();
        if (mDatabaseHelper != null) mDatabaseHelper.close();
    }

    /**
     * Create an initial budget with 0 expenses
     * @param month
     * @param available
     * @param target
     * @return true if it was created
     */
    public boolean createBudget(int month, float available, float target) {
        return createBudget(month, available, target, 0, 0);
    }

    /**
     * Create a budget
     *
     * @param month     month of the budget
     * @param available available money
     * @param target    target money
     * @param withDrawn planned spent
     * @param wallet    unplanned spent
     * @return
     */
    public boolean createBudget(int month, float available, float target, float withDrawn, float wallet) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.BudgetEntry.COLUMN_MONTH, month);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE, available);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_TARGET, target);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN, withDrawn);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_WALLET, wallet);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN, withDrawn);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET, wallet);
        long insertId = getDatabase().insert(ShoppingListContract.BudgetEntry.TABLE_NAME, null, values);
        if (insertId > 0) {
            return true;
        } else return false;
    }

    /**
     * Get the budget for one month
     * @param month month number
     * @return Cursor with budget data
     */
    public Cursor getBudget(int month) {
        return getDatabase().query(ShoppingListContract.BudgetEntry.TABLE_NAME, allColumns,
                ShoppingListContract.BudgetEntry.COLUMN_MONTH + "=" + month, null, null, null, null);
    }

    public boolean existBudget(int month) {
        Cursor cursor = getBudget(month);
        if (cursor != null) {
            if (cursor.moveToFirst()) return true;
            else return false;
        } else {
            return false;
        }
    }

    /**
     * Update budget data
     * @param id budget id
     * @param month month number
     * @param available available money
     * @param target target money
     * @return true if the budget was updated
     */
    public boolean updateBudget(int id, int month, float available, float target) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListContract.BudgetEntry.COLUMN_MONTH, month);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE, available);
        values.put(ShoppingListContract.BudgetEntry.COLUMN_TARGET, target);
        int rowsAffected = getDatabase().update(ShoppingListContract.BudgetEntry.TABLE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
        if (rowsAffected > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Delete a budget
     * @param id budget id
     */
    public void deleteBudget(int id) {
        getDatabase().execSQL("DELETE FROM "
                + ShoppingListContract.BudgetEntry.TABLE_NAME
                + " WHERE " + ShoppingListContract.BudgetEntry._ID + "=" + id);
    }

    /**
     * Add a expense for a month
     *
     * @param eDate
     * @param expense
     * @param hasProducts
     * @return
     */
    public boolean createExpense(String eDate, String expense, boolean hasProducts) {
        float ticketExpense = Float.parseFloat(expense);
        int monthNumber = getMonth(eDate);
        boolean result = true;
        getDatabase().beginTransaction();
        try {
            if (hasProducts) {
                getDatabase().execSQL("UPDATE " + ShoppingListContract.BudgetEntry.TABLE_NAME + " SET "
                        + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN + "=" + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN + "+" + ticketExpense
                        + " WHERE " + ShoppingListContract.BudgetEntry.COLUMN_MONTH + "=" + monthNumber);
                ShoppingListRepository shoppingListRepository = new ShoppingListRepository(mContext, getDatabase());
                shoppingListRepository.deleteCommittedProducts();
                getDatabase().setTransactionSuccessful();
            } else {
                getDatabase().execSQL("UPDATE " + ShoppingListContract.BudgetEntry.TABLE_NAME + " SET "
                        + ShoppingListContract.BudgetEntry.COLUMN_WALLET + "=" + ShoppingListContract.BudgetEntry.COLUMN_WALLET + "+" + ticketExpense
                        + " WHERE " + ShoppingListContract.BudgetEntry.COLUMN_MONTH + "=" + monthNumber);
                getDatabase().setTransactionSuccessful();
            }
        } catch (Exception e) {
            result = false;
        } finally {
            getDatabase().endTransaction();
        }
        return result;
    }

    /**
     * Calculate the month for a specific date
     * @param dateTo date
     * @return month number starting by 0 = January
     */
    private int getMonth(String dateTo) {
        try {
            DateFormat d = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            Date date = d.parse(dateTo);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MONTH);
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * Find out the first month without created budget
     * @return month number starting by 0 = January
     */
    public int getLastBudgetMonth() {
        int monthId = 0;
        String query = "SELECT MAX(Month) FROM Budget";
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                monthId = cursor.getInt(0);
            }
        }
        return monthId;
    }

}
