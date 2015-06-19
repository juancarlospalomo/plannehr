package com.householdplanner.shoppingapp.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

public class BasketTask extends AsyncTask<Handler, Void, Void> {

    private Context mContext = null;
    private Handler mHandler = null;

    public BasketTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Handler... params) {
        if (params != null) {
            if (params[0] != null) {
                mHandler = params[0];
            }
        }
        UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(mContext);
        useCaseShoppingList.clearCart();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage();
            msg.arg1 = AppGlobalState.AsyncTaskCodes.BasketTask.getValue();
            mHandler.sendMessage(msg);
        }
    }
}