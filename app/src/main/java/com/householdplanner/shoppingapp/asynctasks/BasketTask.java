package com.householdplanner.shoppingapp.asynctasks;

import com.householdplanner.shoppingapp.cross.AppGlobalState;
import com.householdplanner.shoppingapp.repositories.ShoppingListRepository;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class BasketTask extends AsyncTask<Handler, Void, Void> {

	private Context mContext = null;
	private Handler mHandler = null;
	
	public BasketTask(Context context) {
		mContext = context;
	}
	
	@Override
	protected Void doInBackground(Handler... params) {
		if (params!=null) {
			if (params[0]!=null) {
				mHandler = params[0];
			}
		}
		ShoppingListRepository listRepository = new ShoppingListRepository(mContext);
		listRepository.emptyCommitted();
		listRepository.close();
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		if (mHandler!=null) {
			Message msg = mHandler.obtainMessage();
			msg.arg1 = AppGlobalState.AsyncTaskCodes.BasketTask.getValue();
			mHandler.sendMessage(msg);
		}
	}
}