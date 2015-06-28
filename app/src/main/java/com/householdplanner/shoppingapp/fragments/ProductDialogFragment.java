package com.householdplanner.shoppingapp.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.householdplanner.shoppingapp.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by JuanCarlos on 25/06/2015.
 */
public class ProductDialogFragment extends DialogFragment {

    private static final String LOG_TAG = ProductDialogFragment.class.getSimpleName();

    //Arguments Keys
    private final static String KEY_TITLE = "key_title";
    private final static String KEY_PHOTO_NAME = "key_photo_name";
    private final static String KEY_PRODUCT_ID = "key_product_id";
    private final static String KEY_PRIMARY_ACTION = "key_primary_action";

    public enum ProductActions {
        None(0),
        Add(1),
        Remove(2);

        private int mValue = 0;

        private ProductActions(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ProductActions map(int value) {
            return values()[value];
        }
    }

    //Current primary action for the Dialog
    private ProductActions mAction;
    private int mProductId;
    private String mPhotoName;
    private String mTitle;

    private View.OnClickListener mListener;

    private TextView mTextViewTitle;
    private ImageView mImageViewPhoto;
    private AppCompatButton mButtonAction;
    private AppCompatButton mButtonCancel;

    public static ProductDialogFragment newInstance(String title, int productId, String photoName, ProductActions action) {
        ProductDialogFragment productDialogFragment = new ProductDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putInt(KEY_PRODUCT_ID, productId);
        args.putString(KEY_PHOTO_NAME, photoName);
        args.putInt(KEY_PRIMARY_ACTION, action.getValue());
        productDialogFragment.setArguments(args);
        return productDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ApplilandiaDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mProductId = args.getInt(KEY_PRODUCT_ID);
        mPhotoName = args.getString(KEY_PHOTO_NAME);
        mAction = ProductActions.map(args.getInt(KEY_PRIMARY_ACTION));
        mTitle = args.getString(KEY_TITLE);
        View view = inflater.inflate(R.layout.fragment_product_dialog, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        inflateViews();
        createButtonHandlers();
        configureViews();
    }

    /**
     * Set the listener for Action onClick event
     *
     * @param l handler callback
     */
    public void setOnClickListener(View.OnClickListener l) {
        mListener = l;
    }

    /**
     * Inflate the views in the Dialog
     */
    private void inflateViews() {
        mImageViewPhoto = (ImageView) getView().findViewById(R.id.imageViewPhoto);
        mButtonAction = (AppCompatButton) getView().findViewById(R.id.buttonAction);
        mButtonCancel = (AppCompatButton) getView().findViewById(R.id.buttonCancel);
        mTextViewTitle = (TextView) getView().findViewById(R.id.textViewTitle);
    }

    /**
     * Create the handlers for the buttons
     */
    private void createButtonHandlers() {
        mButtonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v);
                    dismiss();
                }
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /**
     * Configure Views for the Dialog
     */
    private void configureViews() {
        mTextViewTitle.setText(mTitle);
        if (mAction == ProductActions.Add) {
            mButtonAction.setText(R.string.text_button_action_add);
        } else if (mAction == ProductActions.Remove) {
            mButtonAction.setText(R.string.text_button_action_remove);
        } else {
            mButtonAction.setVisibility(View.GONE);
        }
        showImage(mPhotoName, 1f);
    }

    /**
     * Display Imaged
     *
     * @param fileName file name that contains the image
     * @param scale    scale for the image
     */
    private void showImage(String fileName, float scale) {
        Bitmap bitmap;
        int rotationAngle = 0;
        if (fileName != null) {
            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String pathFileName = directory.getPath() + "/" + fileName;
            bitmap = BitmapFactory.decodeFile(pathFileName);

            String orientation = getImageOrientation(pathFileName);
            int orientationValue = orientation != null ? Integer.parseInt(orientation) : ExifInterface.ORIENTATION_NORMAL;
            if (orientationValue == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientationValue == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientationValue == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_red);
        }
        Bitmap resizedBitmap = getScaledBitmap(bitmap, scale, rotationAngle);

        mImageViewPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageViewPhoto.setImageBitmap(resizedBitmap);
    }

    /**
     * Get the current orientation of a image file
     *
     * @param filePathName file name including the absolute path
     * @return Orientation value
     */
    private String getImageOrientation(String filePathName) {
        File file = new File(filePathName);
        return getImageOrientation(file);
    }

    /**
     * Get the current orientation of a image file
     *
     * @param file file object with the image
     * @return Orientation value
     */
    private String getImageOrientation(File file) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            return exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a bitmap from a bitmap source with a scale factor and a rotation angle
     *
     * @param source        bitmap source
     * @param scale         scale factor
     * @param rotationAngle angle: 0, 90, 180, 270
     * @return Bitmap resized
     */
    private Bitmap getScaledBitmap(Bitmap source, float scale, int rotationAngle) {
        //Calculate and set the new scale
        int width = source.getWidth();
        int height = source.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        if (rotationAngle != ExifInterface.ORIENTATION_UNDEFINED) {
            matrix.postRotate(rotationAngle);
        }
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        return scaledBitmap;
    }

}
