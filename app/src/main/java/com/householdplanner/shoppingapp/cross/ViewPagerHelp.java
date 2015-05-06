package com.householdplanner.shoppingapp.cross;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.householdplanner.shoppingapp.fragments.FragmentBackHelpCard;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;

public class ViewPagerHelp extends FragmentStatePagerAdapter {

	private static final int FRAGMENT_NUMBER = 4;
	
	public ViewPagerHelp(FragmentManager fm, Context context) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		FragmentBackHelpCard fragmentBackCard = null;
		switch(position) {
		case 0:
			fragmentBackCard = FragmentBackHelpCard.newInstance(TypeView.EnterProducts);
			return fragmentBackCard;
		case 1:
			fragmentBackCard = FragmentBackHelpCard.newInstance(TypeView.SetMarket);
			return fragmentBackCard;
		case 2:
			fragmentBackCard = FragmentBackHelpCard.newInstance(TypeView.StartBuy);
			return fragmentBackCard;
		case 3:
			fragmentBackCard = FragmentBackHelpCard.newInstance(TypeView.WriteExpense);
			return fragmentBackCard;
		}
		return fragmentBackCard;
	}

	@Override
	public int getCount() {
		return FRAGMENT_NUMBER;
	}
	
}
