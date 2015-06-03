package com.householdplanner.shoppingapp.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class AlertDialogFragment extends DialogFragment {
    private final static String LOG_TAG = AlertDialogFragment.class.getSimpleName();

    //Arguments Keys
    private final static String KEY_TITLE = "key_title";
    private final static String KEY_CONTENT = "key_content";
    private final static String KEY_CANCEL_TEXT = "key_cancel_text";
    private final static String KEY_OK_TEXT = "key_ok_text";
    private final static String KEY_THIRD_BUTTON_TEXT = "key_third_button_text";
    //Buttons index
    public final static int INDEX_BUTTON_YES = -1;
    public final static int INDEX_BUTTON_NO = -2;
    public final static int INDEX_BUTTON_NEUTRAL = -3;

    private String mTitle;
    private String mContent;
    private String mCancelText;
    private String mOkText;
    private String mThirdButtonText;

    private DialogInterface.OnClickListener mOnClickListener;

    /**
     * Instance a Dialog with the arguments
     *
     * @param title
     * @param content
     * @param cancelText
     * @param okText
     * @return
     */
    public static AlertDialogFragment newInstance(String title, String content, String cancelText, String okText, String thirdText) {
        AlertDialogFragment alertDialog = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_CONTENT, content);
        args.putString(KEY_CANCEL_TEXT, cancelText);
        args.putString(KEY_OK_TEXT, okText);
        args.putString(KEY_THIRD_BUTTON_TEXT, thirdText);
        alertDialog.setArguments(args);
        return alertDialog;
    }

    /**
     * Create an Alert Dialog
     *
     * @param savedInstanceState
     * @return Dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(mTitle)) {
            builder.setTitle(mTitle);
        }
        if (!TextUtils.isEmpty(mContent)) {
            builder.setMessage(mContent);
        }
        if (!TextUtils.isEmpty(mCancelText)) {
            builder.setNegativeButton(mCancelText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(dialog, INDEX_BUTTON_NO);
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(mOkText)) {
            builder.setPositiveButton(mOkText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(dialog, which);
                    }
                }
            });
        }
        if (!TextUtils.isEmpty((mThirdButtonText))) {
            builder.setNeutralButton(mThirdButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(dialog, which);
                    }
                }
            });
        }
        return builder.create();
    }

    /**
     * Set the buttons listener
     *
     * @param l
     */
    public void setButtonOnClickListener(DialogInterface.OnClickListener l) {
        mOnClickListener = l;
    }

    /**
     * Load arguments passed to the Dialog into module vars
     */
    private void loadArguments() {
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(KEY_TITLE);
            mContent = args.getString(KEY_CONTENT);
            mCancelText = args.getString(KEY_CANCEL_TEXT);
            mOkText = args.getString(KEY_OK_TEXT);
            mThirdButtonText = args.getString(KEY_THIRD_BUTTON_TEXT);
        }
    }
}