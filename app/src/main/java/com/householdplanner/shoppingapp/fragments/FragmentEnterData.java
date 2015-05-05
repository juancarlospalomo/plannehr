package com.householdplanner.shoppingapp.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.ViewPagerEnterData;

public class FragmentEnterData extends SherlockFragment implements OnFragmentProgress, 
				OnLoadData {

	private ViewPager mViewPager;
	private ViewPagerEnterData mViewPagerAdapter;
	private OnLoadData mCallback = null;
	
	public FragmentEnterData() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_enter_data, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewPager = (ViewPager) getView().findViewById(R.id.pagerEnterData);
		mViewPagerAdapter = new ViewPagerEnterData(getFragmentManager(), this.getActivity());
		mViewPager.setAdapter(mViewPagerAdapter);
		setPagerTitleStripStyle();
	}
	
	private void setPagerTitleStripStyle() {
		PagerTitleStrip titleStrip = (PagerTitleStrip) getView().findViewById(R.id.pager_title_strip);
		int[] attrs = {android.R.attr.textSize};
		TypedArray tStyles = getActivity().obtainStyledAttributes(R.style.TextPagerTitle, attrs);
		String size = tStyles.getString(0);
		size = size.substring(0, size.length()-2);
		titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(size));
		tStyles.recycle();
	}

	public void setProductVisible(String name) {
		FragmentEnterList fragment = (FragmentEnterList) mViewPagerAdapter.getItem(0);
		fragment.setProductVisible(name);
	}
	
	@Override
	public void setOnLoadData(OnLoadData callback) {
		mCallback = callback;
	}

	@Override
	public void onLoadStart() {
		if (mCallback!=null) mCallback.onLoadStart();
	}

	@Override
	public void onLoadFinish(int items, String source) {
		if (mCallback!=null) mCallback.onLoadFinish(items, source);
	}
	
}
