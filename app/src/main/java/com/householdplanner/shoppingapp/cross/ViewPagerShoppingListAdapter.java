package com.householdplanner.shoppingapp.cross;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.fragments.FragmentBasket;
import com.householdplanner.shoppingapp.fragments.FragmentShoppingList;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerShoppingListAdapter extends FragmentStatePagerAdapter {

	private static final int FRAGMENTS_NUMBER = 2;
	
	private Context mContext;
	
	public ViewPagerShoppingListAdapter(FragmentManager fm, Context context) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch(position) {
		case 0:
			FragmentShoppingList fragmentShoppingList = new FragmentShoppingList();
			fragmentShoppingList.setOnLoadData((OnLoadData) mContext);
			return fragmentShoppingList;
		case 1:
			FragmentBasket fragmentBasket = new FragmentBasket();
			fragmentBasket.setOnLoadData((OnLoadData) mContext);
			return fragmentBasket;
		}
		return null;
	}

	@Override
	public int getCount() {
		return FRAGMENTS_NUMBER;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		switch(position) {
		case 0:
			return mContext.getResources().getString(R.string.textFragmentShoppingListTitle);
		case 1:
			return mContext.getResources().getString(R.string.textFragmentBasketTitle);
		}
		return null;
	}
	
}
