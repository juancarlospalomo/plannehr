package com.applilandia.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by JuanCarlos on 06/03/2015.
 * This class depicts a view with a TextView or EditText
 * and a below view to show a validation error message
 */
public class ValidationField extends LinearLayout {

    private enum TypeView {
        EditText(0),
        TextView(1);

        private int mValue = 0;

        private TypeView(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static TypeView map(int value) {
            return values()[value];
        }

    }

    public OnClickListener mOnClickListener;
    public View mContentView;

    private TypeView mViewType;
    private String mHint;
    private int mInputType;
    private int mValidationTextAppearanceResId = 0;
    private TextView mValidationView;
    private int mBackground;
    private int mValidationViewHeight;

    public ValidationField(Context context) {
        super(context);
    }

    public ValidationField(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs, 0);
        createViewGroup(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ValidationField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributes(attrs, defStyleAttr);
        createViewGroup(attrs);
    }

    /**
     * Load the attributes and set into the module variables
     * @param attrs
     */
    private void setAttributes(AttributeSet attrs, int defStyle) {
        setOrientation(VERTICAL);

        //Load ValidationField attributes, taking default styles
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.ValidationField,
                defStyle, 0);
        //Get Attribute values
        try {
            int value = typedArray.getInteger(R.styleable.ValidationField_viewType, -1);
            mHint = typedArray.getString(R.styleable.ValidationField_hint);
            mInputType = typedArray.getInteger(R.styleable.ValidationField_android_inputType, InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            if (value == -1) {
                mViewType = TypeView.EditText;
            } else {
                mViewType = TypeView.map(value);
            }
            mBackground = typedArray.getResourceId(R.styleable.ValidationField_background, 0);
            int validationViewResId = typedArray.getResourceId(R.styleable.ValidationField_validationView, -1);
            if (validationViewResId != -1) {
                typedArray = getContext().getTheme().obtainStyledAttributes(validationViewResId, R.styleable.validationView);
                mValidationTextAppearanceResId = typedArray.getResourceId(R.styleable.validationView_android_textAppearance,
                        mValidationTextAppearanceResId);
                mValidationViewHeight = typedArray.getLayoutDimension(R.styleable.validationView_android_layout_height,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Create the full View, including the validation message view
     *
     * @param attrs
     */
    private void createViewGroup(AttributeSet attrs) {
        createView(attrs);
        createMessageView(attrs);
    }

    /**
     * Create the content view: EditText or TextView
     *
     * @param attrs
     */
    private void createView(AttributeSet attrs) {
        int height, width, gravity;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_height,
                android.R.attr.layout_width, android.R.attr.layout_gravity});
        try {
            height = typedArray.getLayoutDimension(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            width = WindowManager.LayoutParams.MATCH_PARENT;
            gravity = typedArray.getInteger(2, Gravity.CENTER);
        } finally {
            typedArray.recycle();
        }
        if ((height != WindowManager.LayoutParams.WRAP_CONTENT) &&
                (height != WindowManager.LayoutParams.MATCH_PARENT)) {
            //Android will not to scale itself, so we calculate the height for the content view
            height = height - mValidationViewHeight;
        }
        if (mViewType == TypeView.EditText) {
            mContentView = new AppCompatEditText(getContext());
            ((AppCompatEditText) mContentView).setHint(mHint);
            ((AppCompatEditText) mContentView).setInputType(mInputType);
        } else {
            mContentView = new TextView(getContext());
            ((TextView) mContentView).setGravity(gravity);
            ((TextView) mContentView).setText(mHint);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
            mContentView.setLayoutParams(layoutParams);
            if (mBackground != 0) {
                mContentView.setBackgroundResource(mBackground);
            }
        }
        addView(mContentView);
    }

    /**
     * Create the validation message view
     *
     * @param attrs
     */
    private void createMessageView(AttributeSet attrs) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mValidationView = new TextView(getContext(), attrs, R.attr.textValidationAppearance);
        } else {
            if (mValidationTextAppearanceResId == 0) {
                mValidationView = new TextView(getContext(), attrs, R.attr.textValidationAppearance);
            } else {
                mValidationView = new TextView(getContext(), attrs, 0, mValidationTextAppearanceResId);
            }
        }
        mValidationView.setGravity(Gravity.LEFT);
        if (mViewType == TypeView.EditText) {
            mValidationView.setPadding(getPixels(5), 0, 0, 0);
        }
        addView(mValidationView);
    }

    /**
     * Convert dp to pixels
     *
     * @param dpValue
     * @return
     */
    private int getPixels(int dpValue) {
        DisplayMetrics metrics;
        metrics = getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue);
    }

    @Override
    public void setAlpha(float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mContentView.setAlpha(alpha);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public ViewPropertyAnimator animate() {
        return mContentView.animate();
    }

    /**
     * Return the text of the content view
     *
     * @return
     */
    public String getText() {
        if (mViewType == TypeView.EditText) {
            return ((AppCompatEditText) mContentView).getText().toString();
        } else {
            return (String) ((TextView) mContentView).getText();
        }
    }

    /**
     * Check if the text is empty in EditText View
     * @return true if EditText is empty
     */
    public boolean isEmpty() {
        if (mViewType==TypeView.EditText) {
            if (TextUtils.isEmpty(getText())) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("Unsupported in TextView");
        }
    }

    /**
     * Set the text in the content view
     *
     * @param text string
     */
    public void setText(CharSequence text) {
        if (mViewType == TypeView.EditText) {
            ((AppCompatEditText) mContentView).setText(text);
        } else {
            ((TextView) mContentView).setText(text);
        }
    }

    /**
     * Set the text from the resource in the content view
     *
     * @param resId string resource Id
     */
    public void setText(int resId) {
        if (mViewType == TypeView.EditText) {
            ((AppCompatEditText) mContentView).setText(resId);
        } else {
            ((TextView) mContentView).setText(resId);
        }
    }

    /**
     * Set the enable status of the Content View
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        mContentView.setEnabled(enabled);
    }

    /**
     * Show the validation error message
     *
     * @param message String
     */
    public void setError(String message) {
        mValidationView.setText(message);
    }

    /**
     * Show the validation error message from the resource id
     *
     * @param resId string resource Id
     */
    public void setError(int resId) {
        mValidationView.setText(resId);
    }

    /**
     * Clear the validation error message
     */
    public void removeError() {
        mValidationView.setText("");
    }

    /**
     * Assign the OnClickListener handler
     *
     * @param l
     */
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        mContentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick(v);
            }
        });
    }

    /**
     * Set the editor action listener
     *
     * @param l
     */
    public void setOnEditorActionListener(AppCompatEditText.OnEditorActionListener l) {
        if (mViewType == TypeView.EditText) {
            ((AppCompatEditText) mContentView).setOnEditorActionListener(l);
        }
    }

}