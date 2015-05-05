package com.householdplanner.shoppingapp.fragments;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.views.HelpView.TypeView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentBackHelpCard extends Fragment {

	private TypeView mCapsule;
	
	public FragmentBackHelpCard(TypeView capsule) {
		mCapsule = capsule;
	}
	
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    
}
