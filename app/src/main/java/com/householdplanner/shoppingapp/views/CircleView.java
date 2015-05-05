package com.householdplanner.shoppingapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

public class CircleView extends View {

	private float mX;
	private float mY;
	private float mRadius;
	private float mTextCenterX;
	private float mTextCenterY;
	private String mText;
	private int mNumber;
	private Paint mPaintText;
	private Paint mPaintCircle;
	private int mLeft;
	private int mTop;
	private int mRight;
	private int mBottom;
	private boolean mAllowDragging = false;
	private boolean mIsDragging = false;
	
	public CircleView(Context context) {
		super(context);
	}
	
	public CircleView(Context context, float x, float y, float radius, int color) {
		super(context);
		mX = x;
		mY = y;
		mRadius = radius;
			
		mPaintCircle = new Paint();
		mPaintCircle.setAntiAlias(true);
		mPaintCircle.setColor(color);
		mPaintCircle.setStyle(Paint.Style.FILL);
		mPaintCircle.setStrokeWidth(2);
	}
	
	private float[] getTextXY(Paint paintText, String text) {
		float[] xy = new float[2];

		Rect bounds = new Rect();
		mPaintText.getTextBounds(text, 0, text.length(), bounds);
		float circleWidth = (mRadius * 2) - (mRadius * 2 * 0.05f);
		float textWidth  = bounds.right - bounds.left;
		if (textWidth>circleWidth) {
			float textSize = mPaintText.getTextSize();
			while (textWidth>circleWidth) {
				textSize--;
				mPaintText.setTextSize(textSize);
				mPaintText.getTextBounds(text, 0, text.length(), bounds);
				circleWidth = (mRadius * 2) - (mRadius * 2 * 0.05f);
				textWidth  = bounds.right - bounds.left;
			}
		}
		
		float width = paintText.measureText(text, 0, text.length());
		float height = paintText.descent() - paintText.ascent();
		
		float l = mX - mRadius + (((mRadius*2) - width)/2.f);
		float t = mY - mRadius + (((mRadius*2) - height)/2.f);
		xy[0]=l;
		xy[1]=t-paintText.ascent();
		return xy;
	}
	
	public void setText(String text, float stroke, int color) {
		mText = text;
		mPaintText = new Paint();
		mPaintText.setAntiAlias(true);
		mPaintText.setColor(color);
		mPaintText.setStrokeWidth(stroke);
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setTextSize(mRadius/2);
		
		float[] xy = getTextXY(mPaintText, text);
		mTextCenterX = xy[0];
		mTextCenterY = xy[1];
	}

	public void setOriginalLeft(int left) {
		mLeft = left;
	}
	
	public void setOriginalTop(int top) {
		mTop = top;
	}
	
	public void setOriginalRight(int right) {
		mRight = right;
	}
	
	public void setOriginalBottom(int bottom) {
		mBottom = bottom;
	}
	
	public int getOriginalLeft() {
		return mLeft;
	}
	
	public int getOriginalTop() {
		return mTop;
	}
	
	public int getOriginalRight() {
		return mRight;
	}
	
	public int getOriginalBottom() {
		return mBottom;
	}
	
	public float getAbsoluteCenterX() {
		return mLeft + mX;
	}
	
	public float getAbsoluteCenterY() {
		return mTop + mY;
	}
	
	public float getRadius() {
		return mRadius;
	}
	
	public void setAllowDragging(boolean allow) {
		mAllowDragging = allow;
	}
	
	public boolean isDraggingAllowed() {
		return mAllowDragging;
	}
	
	public boolean isFingerTouching(float x, float y) {
		if ((x>=mLeft) && (x<=mRight)) {
			if ((y>=mTop) && (y<=mBottom)) {
				return true;
			}
		}
		return false;
	}
	
	public int getNumber() {
		return mNumber;
	}
	
	public void setDragging(boolean value) {
		mIsDragging = value;
	}
	
	public boolean isDragging() {
		return mIsDragging;
	}
	
	public void hide() {
		setVisibility(View.GONE);
	}
	
	public void reset() {
		layout(mLeft, mTop, mRight, mBottom);
		setVisibility(View.VISIBLE);
		invalidate();
	}
	
	public void changeBackGroundColor(int color) {
		mPaintCircle.setColor(color);
		invalidate();
	}
	
	public boolean pointInBounds(int x, int y) {
		Rect outRect = new Rect();
		int[] location = new int[2];
		getDrawingRect(outRect);
		getLocationOnScreen(location);
		outRect.offset(location[0], location[1]);
		return outRect.contains(x, y);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(mX, mY, mRadius, mPaintCircle);
		if (!TextUtils.isEmpty(mText)) {
			canvas.drawText(mText, mTextCenterX, mTextCenterY, mPaintText);
		}
	}
	
}
