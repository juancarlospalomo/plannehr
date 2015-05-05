package com.householdplanner.shoppingapp.cross;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.fragments.FragmentEnterList;
import com.householdplanner.shoppingapp.fragments.FragmentMyProducts;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerEnterData extends FragmentStatePagerAdapter {

	private static final int FRAGMENT_NUMBER = 2;
	
	private Context mContext;
	
	public ViewPagerEnterData(FragmentManager fm, Context context) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch(position) {
		case 0:
			FragmentEnterList fragmentEnterList = new FragmentEnterList();
			fragmentEnterList.setOnLoadData((OnLoadData) mContext);
			return fragmentEnterList;
		case 1:
			FragmentMyProducts fragmentMyProducts = new FragmentMyProducts();
			fragmentMyProducts.setOnLoadData((OnLoadData) mContext);
			return fragmentMyProducts;
		}
		return null;
	}

	@Override
	public int getCount() {
		return FRAGMENT_NUMBER;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch(position) {
		case 0:
			return mContext.getResources().getString(R.string.textFragmentEnterListTitle);
		case 1:
			return mContext.getResources().getString(R.string.textFragmentMyProductsTitle);
		}
		return null;
	}
	
}
