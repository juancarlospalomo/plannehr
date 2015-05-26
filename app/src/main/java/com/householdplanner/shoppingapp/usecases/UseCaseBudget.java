package com.householdplanner.shoppingapp.usecases;

import android.content.Context;
import android.database.Cursor;

import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.models.Budget;
import com.householdplanner.shoppingapp.repositories.WalletRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 26/05/2015.
 */
public class UseCaseBudget {

    private Context mContext;

    //Fields for projection
    private static String[] mProjection = new String[]{ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry._ID,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_MONTH,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_TARGET,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_WALLET,
            ShoppingListContract.BudgetEntry.TABLE_NAME + "." + ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET
    };


    public UseCaseBudget(Context context) {
        mContext = context;
    }

    /**
     * Convert the cursor with budgets to a List for Budget objects
     * @param cursor contains the budget cursor
     * @return List of budgets
     */
    private List<Budget> toList(Cursor cursor) {
        List<Budget> budgetList = new ArrayList<>();
        if ((cursor != null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                Budget budget = new Budget();
                budget._id = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.BudgetEntry._ID));
                budget.monthId = cursor.getInt(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_MONTH));
                budget.available = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_AVAILABLE));
                budget.target = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_TARGET));
                budget.withDrawn = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_WITHDRAWN));
                budget.lastWithDrawn = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_LAST_WITHDRAWN));
                budget.wallet = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_WALLET));
                budget.lastWallet = cursor.getFloat(cursor.getColumnIndex(ShoppingListContract.BudgetEntry.COLUMN_LAST_WALLET));
                budgetList.add(budget);
                cursor.moveToNext();
            }
        }
        return budgetList;
    }

    /**
     * Create or update a budget depending of it is new or not
     *
     * @param budget object
     */
    public boolean createOrUpdateBudget(Budget budget) {
        boolean result = false;
        WalletRepository budgetRepository = new WalletRepository(mContext);
        if (budget._id == 0) {
            //It´s new
            result = budgetRepository.createBudget(budget.monthId, budget.available, budget.target);
        } else {
            result = budgetRepository.updateBudget(budget._id, budget.monthId, budget.available, budget.target);
        }
        budgetRepository.close();
        return result;
    }

    /**
     * Get all the budgets
     * @return List with all budgets
     */
    public List<Budget> getBudgets() {
        //Variable for returning product list
        List<Budget> budgetList = new ArrayList<Budget>();

        Cursor cursor = mContext.getContentResolver().query(ShoppingListContract.BudgetEntry.CONTENT_URI,
                mProjection, null, null, ShoppingListContract.BudgetEntry._ID + " DESC");
        budgetList = toList(cursor);

        return budgetList;
    }

    /**
     * Delete a budget
     * @param id budget _id
     */
    public void delete(int id) {
        WalletRepository walletRepository = new WalletRepository(mContext);
        walletRepository.deleteBudget(id);
        walletRepository.close();
    }

}
