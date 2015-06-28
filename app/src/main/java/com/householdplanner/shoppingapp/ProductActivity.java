package com.householdplanner.shoppingapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.applilandia.widget.ValidationField;
import com.householdplanner.shoppingapp.common.ProductHelper;
import com.householdplanner.shoppingapp.cross.util;
import com.householdplanner.shoppingapp.data.ShoppingListContract;
import com.householdplanner.shoppingapp.fragments.AlertDialogFragment;
import com.householdplanner.shoppingapp.models.Product;
import com.householdplanner.shoppingapp.usecases.UseCaseShoppingList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductActivity extends BaseActivity {

    private final static String LOG_TAG = ProductActivity.class.getSimpleName();

    private static final int NEW_MODE = 1;
    private static final int EDIT_MODE = 2;

    private static final int REQUEST_CHOOSE_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    public static final String EXTRA_ID = "_id";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "name";
    public static final String EXTRA_PHOTO_NAME = "photo_name";
    public static final String EXTRA_MARKET_NAME = "market_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_UNIT_ID = "unit_id";

    //Current mode (Edit|New)
    private int mMode = NEW_MODE;
    private Product mProduct;
    private ImageView mImageViewPhoto;
    private ImageView mImageViewPrimaryAction;
    private ImageView mImageViewSecondaryAction;
    private ValidationField mValidationFieldProductName;

    private String mCurrentTempPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        //Get extra intent data if there are any
        getIntentData();
        inflateViews();
        configureFrameViewPhoto();
        if (mMode == EDIT_MODE) {
            //Load existing data got from the intent
            initViewsData();
        }
        createButtonHandlers();
        createImageActionHandlers();
        addListenerOnSpinnerMeasureItemSelection();
        //Hide the soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Inflate views from activity layout
     */
    private void inflateViews() {
        mValidationFieldProductName = (ValidationField) findViewById(R.id.validationFieldProductName);
        mImageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
        mImageViewPrimaryAction = (ImageView) findViewById(R.id.imageViewPrimaryAction);
        mImageViewSecondaryAction = (ImageView) findViewById(R.id.imageViewSecondaryAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mProduct.photoName = getFileName();
            savePicture(mProduct.photoName);
            configureFrameViewPhoto();
            showImage(mProduct.photoName, 1f);
            if (mMode == EDIT_MODE) saveProduct(false);
        }
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    if (mProduct.photoName != null) {
                        //It has already a photo, so we delete the file
                        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/" + mProduct.photoName);
                        file.delete();
                    }
                    mProduct.photoName = getFileName();
                    importPicture(bitmap, mProduct.photoName);
                    configureFrameViewPhoto();
                    showImage(mProduct.photoName, 1f);
                    if (mMode == EDIT_MODE) saveProduct(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(LOG_TAG, "Not chosen");
            }
        }
    }

    /**
     * Generate a new jpg unique file name
     *
     * @return
     */
    private String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String suffix = simpleDateFormat.format(new Date());
        return "product_" + suffix + ".jpg";
    }

    /**
     * Recover data from Intent when the activity is called
     */
    private void getIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try {
            mProduct = new Product();
            mProduct._id = bundle.getInt(EXTRA_ID);
            if (mProduct._id > 0) {
                mMode = EDIT_MODE;
                mProduct.productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0);
                mProduct.name = intent.getStringExtra(EXTRA_PRODUCT_NAME);
                mProduct.photoName = intent.getStringExtra(EXTRA_PHOTO_NAME);
                mProduct.marketName = intent.getStringExtra(EXTRA_MARKET_NAME);
                mProduct.amount = intent.getStringExtra(EXTRA_AMOUNT);
                mProduct.unitId = intent.getIntExtra(EXTRA_UNIT_ID, 0);

            }
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {
        }
    }

    /**
     * Create the handlers for buttons in the activity
     */
    private void createButtonHandlers() {
        AppCompatButton buttonOk = (AppCompatButton) findViewById(R.id.btnSave);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct(true);
            }
        });
    }

    /**
     * Create the handlers for image actions in photo layout
     */
    private void createImageActionHandlers() {
        mImageViewSecondaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProduct.photoName == null) {
                    chooseImageFromGallery();
                } else {
                    removePhoto();
                }
            }
        });
        mImageViewPrimaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProduct.photoName == null) {
                    dispatchTakePictureIntent();
                } else {
                    changePhoto();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProduct.photoName != null) {
            if (mMode == NEW_MODE & mProduct._id == 0) {
                //Product has not been saved, so we delete the photo file
                File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/" + mProduct.photoName);
                file.delete();
            }
        }
    }

    /**
     * Save the product into the DB
     */
    private void saveProduct(final boolean finish) {
        mValidationFieldProductName.setError("");
        setProductData();
        if (mProduct.validate()) {
            if (mMode == NEW_MODE) {
                ProductHelper productHelper = new ProductHelper(ProductActivity.this, mProduct, new ProductHelper.OnSaveProduct() {
                    @Override
                    public void onSaveProduct() {
                        if (finish) finishActivity();
                    }
                });
                productHelper.addProductToList();
            } else {
                UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(ProductActivity.this);
                useCaseShoppingList.updateProduct(mProduct);
                getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                if (finish) finishActivity();
            }
        } else {
            mValidationFieldProductName.setError(R.string.textProductNameErrorMessage);
        }
    }

    /**
     * End the activity, sending the out data in the Intent
     */
    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PRODUCT_NAME, mProduct.name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Set initial data into the views
     */
    private void initViewsData() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        mValidationFieldProductName.setText(mProduct.name);
        if (mProduct.photoName != null) {
            configureFrameViewPhoto();
            showImage(mProduct.photoName, 1f);
        }
        editAmount.setText(mProduct.amount);
        spinnerMeasure.setSelection(mProduct.unitId);
    }

    /**
     * Configure the initial size for the photo
     */
    private void configureFrameViewPhoto() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_photo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
        final int minimumWidth = displayMetrics.widthPixels - (padding * 2);
        int minimumHeight = minimumWidth;
        if (mProduct.photoName == null) {
            mImageViewPhoto.setVisibility(View.GONE);
            mImageViewPrimaryAction.setImageResource(R.drawable.ic_photo_camera);
            mImageViewSecondaryAction.setImageResource(R.drawable.ic_photo_library);
            minimumHeight = (int) getResources().getDimension(R.dimen.touchable_grid_metric);
        } else {
            mImageViewPhoto.setVisibility(View.VISIBLE);
            mImageViewPhoto.setMinimumWidth(minimumWidth);
            mImageViewPhoto.setMinimumHeight(minimumHeight);
            mImageViewPrimaryAction.setImageResource(R.drawable.ic_action_edit_circle);
            mImageViewSecondaryAction.setImageResource(R.drawable.ic_action_discard_circle);
        }
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(minimumWidth, minimumHeight));
    }

    /**
     * Delete the photo file
     */
    private void removePhoto() {
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_product_photo_dialog_title),
                "", getResources().getString(R.string.delete_product_dialog_cancel_text),
                getResources().getString(R.string.delete_product_dalog_ok_text), null);

        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    UseCaseShoppingList useCaseShoppingList = new UseCaseShoppingList(ProductActivity.this);
                    useCaseShoppingList.removePhoto(mProduct);
                    configureFrameViewPhoto();
                    getContentResolver().notifyChange(ShoppingListContract.ProductEntry.CONTENT_URI, null);
                }
            }
        });
        alertDialog.show(getSupportFragmentManager(), "confirmationDialog");
    }

    /**
     * Change the photo for the product
     */
    private void changePhoto() {
        chooseImageFromGallery();
    }

    /**
     * Launch intent to allow user select an existing image
     */
    private void chooseImageFromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        if (getIntent().resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_CHOOSE_IMAGE);
        }
    }

    /**
     * Save a bitmap scaled to the half size into a file in the pictures external directory
     *
     * @param bitmap   Source bitmap
     * @param fileName file name where to save bitmap
     */
    private void importPicture(Bitmap bitmap, String fileName) {
        Bitmap resizedBitmap = getScaledBitmap(bitmap, 0.5f, ExifInterface.ORIENTATION_UNDEFINED);
        File storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDirectory, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePicture(String fileName) {
        //Open the temp file created when the photo has been taken
        File tempFile = new File(mCurrentTempPhotoPath);
        int rotationAngle = util.getImageRotationAngle(tempFile);
        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath());

        //Create a new bitmap resized to the new dimensions
        Bitmap resizedBitmap = getScaledBitmap(bitmap, 0.5f, rotationAngle);

        FileOutputStream outputStream = null;
        try {
            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            outputStream = new FileOutputStream(file);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Remove the temporary file created when the photo was taken
        tempFile.delete();
    }

    /**
     * Show an image in the ImageView scaled as stated by parameter
     *
     * @param fileName file name containing the image
     * @param scale    scale factor
     */
    private void showImage(String fileName, float scale) {
        File directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String pathFileName = directory.getPath() + "/" + fileName;
        Bitmap bitmap = BitmapFactory.decodeFile(pathFileName);

        String orientation = util.getImageOrientation(pathFileName);
        int orientationValue = orientation != null ? Integer.parseInt(orientation) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Bitmap resizedBitmap = getScaledBitmap(bitmap, scale, rotationAngle);

        mImageViewPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageViewPhoto.setImageBitmap(resizedBitmap);
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

    /**
     * Raise intent to take a photo
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createTempImageFile(getFileName());
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri fileUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        fileUri);
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Create a temporary file to store the photo taken from the camera app
     *
     * @param fileName temporary file name
     * @return File object
     * @throws IOException
     */
    private File createTempImageFile(String fileName) throws IOException {
        //Gallery directory:
        //          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File tempFile = File.createTempFile(
                fileName,  /* prefix */
                "",         /* suffix */
                storageDir      /* directory */
        );
        //Hold the path of the file for the future processing
        mCurrentTempPhotoPath = tempFile.getAbsolutePath();
        return tempFile;
    }




    /**
     * Save one orientation value into the metadata image file
     *
     * @param file  File object with the image
     * @param value Orientation value
     */
    private void setImageOrientation(File file, String value) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, value);
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unit Id spinner handler
     */
    private void addListenerOnSpinnerMeasureItemSelection() {
        AppCompatSpinner spinnerMeasure = (AppCompatSpinner) findViewById(R.id.spMeasure);
        spinnerMeasure.setOnItemSelectedListener(new MeasureOnItemSelectedListener());
    }

    /**
     * Get the product data from the UI and set in the product object
     */
    private void setProductData() {
        EditText editAmount = (EditText) findViewById(R.id.edAmount);
        mProduct.name = mValidationFieldProductName.getText().trim();
        mProduct.amount = editAmount.getText().toString().trim();
    }

    public class MeasureOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mProduct.unitId = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}



	