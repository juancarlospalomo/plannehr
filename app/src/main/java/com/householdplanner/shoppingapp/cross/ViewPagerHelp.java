package com.householdplanner.shoppingapp.cross;

import com.householdplanner.shoppingapp.fragments.FragmentBackHelpCard;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerHelp extends FragmentStatePagerAdapter {

	private static final int FRAGMENT_NUMBER = 5;
	
	public ViewPagerHelp(FragmentManager fm, Context context) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		FragmentBackHelpCard fragmentBackCard = null;
		switch(position) {
		case 0:
			fragmentBackCard = new FragmentBackHelpCard(TypeView.EnterProducts);
			return fragmentBackCard;
		case 1:
			fragmentBackCard = new FragmentBackHelpCard(TypeView.SetMarket);
			return fragmentBackCard;
		case 2:
			fragmentBackCard = new FragmentBackHelpCard(TypeView.StartBuy);
			return fragmentBackCard;
		case 3:
			fragmentBackCard = new FragmentBackHelpCard(TypeView.WriteExpense);
			return fragmentBackCard;
		case 4:
			fragmentBackCard = new FragmentBackHelpCard(TypeView.ShareList);
			return fragmentBackCard;
		}
		return fragmentBackCard;
	}

	@Override
	public int getCount() {
		return FRAGMENT_NUMBER;
	}
	
}
