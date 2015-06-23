package com.applilandia.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by JuanCarlos on 20/05/2015.
 */
public class CircleView extends View {

    private final String LOG_TAG = CircleView.class.getSimpleName();

    /**
     * Attributes
     */
    private int mBackground; //Background resource
    private int mStrokeWidth; //Stroke width
    private int mStrokeColor; //Stroke Color
    //View size
    private boolean mSetStroke = false;
    private int mSize = 0;
    //Paint to draw on canvas
    private Paint mPaintCircle;
    private Paint mPaintRect;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circleViewStyle);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //Default attributes from Theme
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.style.ApplilandiaWidget_CircleView,
                new int[]{R.attr.strokeColor, R.attr.strokeWidth, R.attr.background});
        try {
            mStrokeColor = typedArray.getResourceId(R.styleable.circleView_strokeColor, 0);
            mStrokeWidth = typedArray.getLayoutDimension(R.styleable.circleView_strokeWidth, 0);
            mBackground = typedArray.getResourceId(R.styleable.circleView_background, 0);
        } finally {
            typedArray.recycle();
        }

        //Attributes
        typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.circleView, 0, 0);
        try {
            mBackground = typedArray.getResourceId(R.styleable.circleView_background, mBackground);
            mStrokeWidth = typedArray.getLayoutDimension(R.styleable.circleView_strokeWidth, mStrokeWidth);
            mStrokeColor = typedArray.getResourceId(R.styleable.circleView_strokeColor, mStrokeColor);
        } finally {
            typedArray.recycle();
        }

        createCirclePaint();
        createRectPaint();
    }

    /**
     * Set color to the circle
     *
     * @param color color number
     */
    public void setColor(int color) {
        mBackground = color;
        if (mBackground == getResources().getColor(android.R.color.transparent)) {
            mPaintCircle.setColor(getResources().getColor(mStrokeColor));
            mPaintCircle.setStyle(Paint.Style.STROKE);
            mPaintCircle.setStrokeWidth(mStrokeWidth);
            mSetStroke = true;
        } else {
            mSetStroke = false;
            mPaintCircle.setStyle(Paint.Style.FILL);
            mPaintCircle.setColor(mBackground);
        }
        invalidate();
    }

    /**
     * Create a transparent Paint to draw a rect inside the circle will be drawn
     */
    private void createRectPaint() {
        mPaintRect = new Paint();
        mPaintRect.setAntiAlias(true);
        mPaintRect.setColor(getResources().getColor(android.R.color.transparent));
        mPaintRect.setStyle(Paint.Style.FILL);
    }

    /**
     * Create a circle Paint
     */
    private void createCirclePaint() {
        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        if (getResources().getColor(mBackground) == getResources().getColor(android.R.color.transparent)) {
            //We draw the stroke line only if the color is transparent
            mPaintCircle.setColor(getResources().getColor(mStrokeColor));
            mPaintCircle.setStyle(Paint.Style.STROKE);
            mPaintCircle.setStrokeWidth(mStrokeWidth);
            mSetStroke = true;
        } else {
            mPaintCircle.setColor(getResources().getColor(mBackground));
            mPaintCircle.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSize == 0 && heightSize == 0) {
            // If there are no constraints on size, let FrameLayout measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // Now use the smallest of the measured dimensions for both dimensions
            final int minSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
            setMeasuredDimension(minSize, minSize);
            return;
        }

        if (widthSize == 0 || heightSize == 0) {
            // If one of the dimensions has no restriction on size, set both dimensions to be the
            // on that does
            mSize = Math.max(widthSize, heightSize);
        } else {
            // Both dimensions have restrictions on size, set both dimensions to be the
            // smallest of the two
            mSize = Math.min(widthSize, heightSize);
        }

        mSize += getPaddingTop() + getPaddingBottom();

        final int newMeasureSpec = MeasureSpec.makeMeasureSpec(mSize, MeasureSpec.EXACTLY);
        super.onMeasure(newMeasureSpec, newMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        //We have to deduct the strokewidth * 2 (top + bottom)
        int strokeWidth = mStrokeWidth;
        if (!mSetStroke) {
            strokeWidth = 0;
        }
        int rectHeight = mSize;
        int rectWidth = mSize;
        RectF innerRectF = new RectF();
        innerRectF.set(0, 0, rectWidth, rectHeight);
        //Remove padding to calculate the radius
        //Remove stroke width too
        final int radius = ((mSize - (paddingTop + paddingBottom)) / 2) - (strokeWidth * 2);
        canvas.drawRect(innerRectF, mPaintRect);
        canvas.drawCircle(rectWidth / 2, rectHeight / 2, radius, mPaintCircle);
    }
}
