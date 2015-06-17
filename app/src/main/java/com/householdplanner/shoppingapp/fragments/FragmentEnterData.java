package com.householdplanner.shoppingapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applilandia.widget.SlidingTabLayout;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.OnFragmentProgress;
import com.householdplanner.shoppingapp.cross.OnLoadData;
import com.householdplanner.shoppingapp.cross.ViewPagerEnterData;

public class FragmentEnterData extends Fragment implements OnFragmentProgress,
        OnLoadData {

    private ViewPager mViewPager;
    private ViewPagerEnterData mViewPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
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
        inflateViews();
        createTabHandler();
    }

    /**
     * Inflate view on Fragment
     */
    private void inflateViews() {
        mSlidingTabLayout = (SlidingTabLayout) getView().findViewById(R.id.sliding_tabs);
        mViewPager = (ViewPager) getView().findViewById(R.id.pagerEnterData);
    }

    /**
     * Create the ViewPager Adapter and set the ViewPager for the SlidingTabLayout
     */
    private void createTabHandler() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPagerAdapter = new ViewPagerEnterData(getFragmentManager(), this.getActivity());
        mViewPager.setAdapter(mViewPagerAdapter);
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout.setViewPager(mViewPager);
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
        if (mCallback != null) mCallback.onLoadStart();
    }

    @Override
    public void onLoadFinish(int items, String source) {
        if (mCallback != null) mCallback.onLoadFinish(items, source);
    }

}
