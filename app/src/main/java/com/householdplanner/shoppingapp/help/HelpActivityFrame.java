package com.householdplanner.shoppingapp.help;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.cross.ViewPagerHelp;
import com.householdplanner.shoppingapp.cross.ViewPagerTransformer;
import com.householdplanner.shoppingapp.cross.ViewPagerTransformer.TransformType;

public class HelpActivityFrame extends AppCompatActivity{

	public final static String EXTRA_INITIAL_CAPSULE = "initialCapsule";
	
	private int mCurrentPosition = 0;
	private ViewPager mViewPager;
	private ViewPagerHelp mViewPagerAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_frame);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mViewPager = (ViewPager) findViewById(R.id.pagerHelp);
		mViewPagerAdapter = new ViewPagerHelp(getSupportFragmentManager(), this);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOnPageChangeListener(new HelpPageChange());
		
		Bundle args = getIntent().getExtras();
		if (args!=null) {
			int firstPosition = args.getInt(EXTRA_INITIAL_CAPSULE);
			mViewPager.setCurrentItem(firstPosition);
			swapCapsule(mCurrentPosition, firstPosition);
			mCurrentPosition = firstPosition;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mViewPager.setPageTransformer(false, new ViewPagerTransformer(TransformType.SLIDE_OVER));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		
			default: 
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setColor(int position, int colorResource) {
		ImageView view = null;
		
		switch(position) {
		case 0:
			view = (ImageView) findViewById(R.id.imageCapsule1);
			break;
		case 1:
			view = (ImageView) findViewById(R.id.imageCapsule2);
			break;
		case 2:
			view = (ImageView) findViewById(R.id.imageCapsule3);
			break;
		case 3:
			view = (ImageView) findViewById(R.id.imageCapsule4);
			break;
		}
		
		if (view!=null) {
			view.setImageResource(colorResource);
		}
		
	}
	
	private void swapCapsule(int currentPosition, int newPosition) {
		setColor(currentPosition, R.drawable.circle_white);
		setColor(newPosition, R.drawable.circle_blue);
	}
	
	private class HelpPageChange implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			swapCapsule(mCurrentPosition, position);
			mCurrentPosition = position;
		}
		
	}
	
}
