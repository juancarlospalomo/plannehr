package com.applilandia.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    //Bitmap to draw in the circle
    private Bitmap mBitmap;
    //Gesture detector
    private GestureDetectorCompat mGestureDetector;

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
        mGestureDetector = new GestureDetectorCompat(context, new CircleGestureListener());
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
     * Set the bitmap to draw inside the circle
     *
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mSetStroke = false;
        mPaintCircle.setColor(Color.WHITE);
        mPaintCircle.setStyle(Paint.Style.FILL);
        mPaintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mBitmap = bitmap;
        invalidate();
    }


    public void setDrawable(int resId) {
        setBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    /**
     * Scale a bitmap
     *
     * @return Scaled bitmap
     */
    private Bitmap getScaledBitmap() {
        final int width = mBitmap.getWidth();
        final int height = mBitmap.getHeight();
        final int targetSize = mSize - (getPaddingTop() + getPaddingBottom());
        Matrix matrix = new Matrix();
        float scaleX = (float) targetSize / (float) mBitmap.getWidth();
        float scaleY = (float) targetSize / (float) mBitmap.getHeight();

        matrix.postScale(scaleX, scaleY);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
        return mBitmap;
    }

    /**
     * Build a Rounded Bitmap
     *
     * @param source
     * @return bitmap rounded
     */
    private Bitmap getCircleBitmap(Bitmap source) {
        //http://stackoverflow.com/questions/8280027/what-does-porterduff-mode-mean-in-android-graphics-what-does-it-do
        //http://ssp.impulsetrain.com/porterduff.html
        int w = source.getWidth();
        int h = source.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2), (h / 2), radius, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        c.drawBitmap(source, 0, 0, p);

        return output;
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
        //Remove stroke width too
        final int radius = ((mSize - (paddingTop + paddingBottom)) / 2) - (strokeWidth * 2);
        if (mBitmap != null) {
            canvas.drawARGB(0, 0, 0, 0);
            mPaintCircle.setColor(Color.RED);
            mPaintCircle.setStyle(Paint.Style.FILL);
            mPaintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(getCircleBitmap(getScaledBitmap()), mSize - ((radius * 2) + getPaddingLeft()), mSize - ((radius * 2) + getPaddingLeft()), mPaintCircle);
        } else {
            canvas.drawCircle(rectWidth / 2, rectHeight / 2, radius, mPaintCircle);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * Manage the basic Gesture Listener
     */
    private class CircleGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            performClick();
            return true;
        }
    }


}
