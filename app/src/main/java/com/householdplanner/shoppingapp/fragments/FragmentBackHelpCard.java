package com.householdplanner.shoppingapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;

public class FragmentBackHelpCard extends Fragment {

	//Argument Keys
	private final static String KEY_TYPE_VIEW = "key_typeview";

	private TypeView mCapsule;

	public FragmentBackHelpCard() {}

	public static FragmentBackHelpCard newInstance(TypeView capsule) {
		FragmentBackHelpCard fragmentBackHelpCard = new FragmentBackHelpCard();
		Bundle args = new Bundle();
		args.putInt(KEY_TYPE_VIEW, capsule.getValue());
		fragmentBackHelpCard.setArguments(args);
		return fragmentBackHelpCard;
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		loadArguments();
    	if (mCapsule==TypeView.EnterProducts) {
    		return inflater.inflate(R.layout.fragment_back_help_enter_card, container, false);
    	} else if (mCapsule==TypeView.SetMarket) {
    		return inflater.inflate(R.layout.fragment_back_help_set_market, container, false);
    	} else if (mCapsule==TypeView.StartBuy) {
    		return inflater.inflate(R.layout.fragment_back_help_start_buy, container, false);
    	} else if (mCapsule==TypeView.WriteExpense) {
    		return inflater.inflate(R.layout.fragment_back_help_expenses, container, false);
    	} else {
    		return inflater.inflate(R.layout.fragment_back_help_share, container, false);
    	}
	}

	/**
	 * Load arguments passed to the Fragment into module vars
	 */
	private void loadArguments() {
		Bundle args = getArguments();
		if (args != null) {
			mCapsule = TypeView.map(args.getInt(KEY_TYPE_VIEW));
		}
	}

}
