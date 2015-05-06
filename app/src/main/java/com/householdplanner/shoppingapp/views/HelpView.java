package com.householdplanner.shoppingapp.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.householdplanner.shoppingapp.R;

import java.util.ArrayList;
import java.util.List;

public class HelpView extends ViewGroup {
	
	public enum TypeView {
		EnterProducts(0),
		SetMarket(1),
		StartBuy(2),
		WriteExpense(3);
		
		private int _value;
		private TypeView(int value) {
			_value = value;
		}
		
		public int getValue() {
			return _value;
		}

		public static TypeView map(int value) {
			return values()[value];
		}
	}
	
	public interface OnHelpViewClick {
		public void onCapsuleClick(TypeView capsule);
	}
	
	public class Div {
		public float left;
		public float top;
		public float right;
		public float bottom;
	}
	
	public class DivCapsule {
		public TypeView typeCapsule;
		public Div div;
		public CircleView view;
	}

/*	private static final int CAPSULES_NUMBER = 5;*/
	private static final int COLUMNS_NUMBER = 3;
	
	private int[] mCapsulesText = {R.string.textCapsule1, R.string.textCapsule2,
			R.string.textCapsule3, R.string.textCapsule4, R.string.textCapsule5 };
	
	private int mScreenWidth;
	private Float mRadius = null;
	private Context mContext;
	private List<DivCapsule> mCapsules;
	private TypeView mCurrentTouchedCapsule = null;
	private OnHelpViewClick mOnHelpViewClick = null;
	
	public HelpView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		getScreenMetrics();
		layoutHelpView();
		paintViews();
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		for(DivCapsule divCapsule : mCapsules) {
			int left = divCapsule.view.getOriginalLeft();
			int top = divCapsule.view.getOriginalTop();
			int right = divCapsule.view.getOriginalRight();
			int bottom = divCapsule.view.getOriginalBottom();
			divCapsule.view.layout(left, top, right, bottom);
		}
		
	}
	
	private float getRadius() {
		if (mRadius==null) {
			mRadius = ((mScreenWidth/COLUMNS_NUMBER)/2)*0.90f;
		}
		return mRadius.floatValue();
	}
	
	private void layoutHelpView() {
		mCapsules = new ArrayList<DivCapsule>(TypeView.values().length);
		float columnSize = mScreenWidth/COLUMNS_NUMBER;
		float r = getRadius();
		TypeView typeCapsule = TypeView.EnterProducts;
		
		for (int index=0; index<TypeView.values().length; index++) {
			DivCapsule divCapsule = new DivCapsule();
			Div div = new Div();
			
			if (index==TypeView.EnterProducts.getValue()) {
				div.left = 0;
				div.top = 2 * r;
				div.right = columnSize;
				div.bottom = div.top + columnSize;
				typeCapsule = TypeView.EnterProducts;
			} else if (index==TypeView.SetMarket.getValue()) {
				div.left = columnSize;
				div.top = (2*r) - (r/2);
				div.right = columnSize * 2;
				div.bottom = div.top + r;
				typeCapsule = TypeView.SetMarket;
			} else if (index==TypeView.StartBuy.getValue()) {
				div.left = columnSize * 2;
				div.top = 2 * r;
				div.right = columnSize * 3;
				div.bottom = div.top + columnSize;
				typeCapsule = TypeView.StartBuy;
			} else {
				div.left = columnSize;
				div.top = mCapsules.get(0).div.bottom  - (r/2);
				div.right = columnSize * 2;
				div.bottom = div.top + columnSize;
				typeCapsule = TypeView.WriteExpense;
			}
			
			divCapsule.typeCapsule = typeCapsule;
			divCapsule.div = div;
			divCapsule.view = getCircleLayout(div);
			mCapsules.add(divCapsule);
		}
	}
	
	private CircleView getCircleLayout(Div div) {
		float width = div.right - div.left;
		float height = div.bottom - div.top;
		float left, top, right, bottom;
		float r = getRadius();

		float diameter = 2 * r;
		float widthMargin = (width - diameter)/2;
		float heightMargin = (height - diameter)/4;
		left = div.left + widthMargin;
		top = div.top + heightMargin;
		right = left + diameter;
		bottom = top + diameter;
		
		float x = (right - left)/2;
		float y = (bottom - top)/2;
	
		CircleView capsuleView = new CircleView(mContext, x, y, r, getResources().getColor(R.color.blue));
		capsuleView.setOriginalLeft((int)left);
		capsuleView.setOriginalTop((int)top);
		capsuleView.setOriginalRight((int)right);
		capsuleView.setOriginalBottom((int)bottom);
		return capsuleView;
	}
	
	public TypeView getViewTouched(float x, float y) {
		for(DivCapsule divCapsule : mCapsules) {
			if (divCapsule.view.isFingerTouching(x, y)) {
				return divCapsule.typeCapsule;
			}
		}
		return null;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction()==MotionEvent.ACTION_DOWN) {
			mCurrentTouchedCapsule = getViewTouched(ev.getX(), ev.getY());
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction()==MotionEvent.ACTION_UP) {
			if (mCurrentTouchedCapsule!=null) {
				if (mOnHelpViewClick!=null) {
					mOnHelpViewClick.onCapsuleClick(mCurrentTouchedCapsule);
				}
			}
		}
		return true;
	}
	
	public void setOnHelpViewClick(OnHelpViewClick l) {
		mOnHelpViewClick = l;
	}
	
	private void paintViews() {
		int index = 0;
		for(DivCapsule divCapsule : mCapsules) {
			String text = getResources().getString(mCapsulesText[index]);
			divCapsule.view.setText(text, 2, Color.BLACK);
			addView(divCapsule.view);
			index++;
		}
	}

	private void getScreenMetrics() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		displayMetrics = mContext.getResources().getDisplayMetrics();
		mScreenWidth = displayMetrics.widthPixels;
	}
}
