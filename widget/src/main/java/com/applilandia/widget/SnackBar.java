package com.applilandia.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by JuanCarlos on 09/03/2015.
 */
public class SnackBar extends RelativeLayout {

    private final static String LOG_TAG = SnackBar.class.getSimpleName();

    public interface OnSnackBarListener {
        public void onClose();

        public void onUndo();
    }

    private OnSnackBarListener mOnSnackBarListener;
    private Context mContext;

    private boolean mUndo = false; //Save if the Undo action has been pressed
    //Main text on the SnackBar
    private TextView mText;
    //View for setting the action text
    private TextView mActionText;
    //Text for action
    private String mTextActionSnackBar;

    public SnackBar(Context context) {
        this(context, null);
    }

    public SnackBar(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.snackBarStyle);
        mContext = context;
        //Load custom attributes values
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.snackBarView, 0, 0);
        try {
            mTextActionSnackBar = typedArray.getString(R.styleable.snackBarView_textAction);
        } finally {
            typedArray.recycle();
        }
        //Set the layout
        RelativeLayout.LayoutParams layoutParams = new LayoutParams(context, attrs);
        setLayoutParams(layoutParams);
        setVisibility(View.GONE);
        //Add text and action views
        createSnackBarText(context);
        createSnackBarAction(context);
    }

    /**
     * Create the TextView to show the Text for snack bar
     *
     * @param context
     */
    private void createSnackBarText(Context context) {
        mText = new TextView(context, null, R.attr.snackBarText);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mText.setLayoutParams(layoutParams);
        mText.setGravity(Gravity.CENTER);
        addView(mText);
    }

    /**
     * Create the TextView for the action
     *
     * @param context
     */
    private void createSnackBarAction(Context context) {
        mActionText = new TextView(context, null, R.attr.snackBarAction);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mActionText.setLayoutParams(layoutParams);
        mActionText.setPadding(getPixels(40), 0, 0, 0);
        mActionText.setGravity(Gravity.CENTER);
        mActionText.setText(mTextActionSnackBar);
        mActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSnackBarListener != null) {
                    mUndo = true;
                    mOnSnackBarListener.onUndo();
                    hide();
                }
            }
        });
        addView(mActionText);
    }

    /**
     * Show the animation for older versions than 14
     */
    private void showAnimation() {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.snackbar);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if ((!mUndo) && (getVisibility() == View.VISIBLE)) {
                    hide();
                    if (mOnSnackBarListener != null) {
                        mOnSnackBarListener.onClose();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.startAnimation(animation);
    }

    /**
     * Show the animation for newer api versions than 13 (At least 14)
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showAnimator() {
        setAlpha(1);
        animate().setDuration(2000)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //To avoid this event is called twice in Android v4.x
                        if ((!mUndo) && (getVisibility() == View.VISIBLE)) {
                            hide();
                            if (mOnSnackBarListener != null) {
                                mOnSnackBarListener.onClose();
                            }
                        }
                    }
                }).start();
    }

    /**
     * Show snack bar
     *
     * @param messageResId message resource to show as the text
     */
    public void show(int messageResId) {
        //When the snack bar is showed, reset the undo value
        mUndo = false;
        mText.setText(messageResId);
        setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showAnimator();
        } else {
            showAnimation();
        }
    }

    /**
     * Hide the snack bar
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void hide() {
        setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            animate().cancel();
        } else {
            this.clearAnimation();
        }
    }

    /**
     * Set the action as Undone
     */
    public void undo() {
        mUndo = true;
        hide();
    }

    /**
     * Set the listener
     *
     * @param l
     */
    public void setOnSnackBarListener(OnSnackBarListener l) {
        mOnSnackBarListener = l;
    }

    /**
     * Convert dp´s to pixels
     *
     * @param dpValue
     * @return
     */
    private int getPixels(int dpValue) {
        DisplayMetrics metrics;
        metrics = getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue);
    }
}