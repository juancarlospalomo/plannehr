package com.householdplanner.shoppingapp.cross;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.ExifInterface;
import android.preference.PreferenceManager;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.fragments.FragmentSetting;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class util {

    public static String getMeasureUnitName(Context context, int id, String amount) {
        String result = "";
        String[] items = context.getResources().getStringArray(R.array.measure_array);
        if (id > 0) {
            result = getCorrectWordNumber(items[id], amount);
        }
        return result;
    }

    public static int getMonth(String dateTo) {
        try {
            DateFormat d = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            Date date = d.parse(dateTo);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MONTH);
        } catch (ParseException e) {
            return -1;
        }
    }

    public static String getFormattedDate(int year, int month, int day) {
        DateFormat d = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        return d.format(date);
    }

    public static String getFormattedDate() {
        DateFormat d = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        Date date = new Date();
        return d.format(date);
    }

    public static String getFormattedDate(String date, int currentStyle, String newFormat) {
        DateFormat d = SimpleDateFormat.getDateInstance(currentStyle, Locale.getDefault());
        try {
            Date currentDate = d.parse(date);
            SimpleDateFormat newDate = new SimpleDateFormat(newFormat, Locale.getDefault());
            return newDate.format(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getCorrectWordNumber(String word, String amount) {
        String result = word;
        try {
            float fAmount = Float.parseFloat(amount);
            if (fAmount == 1) {
                if (word.endsWith("os")) {
                    result = word.substring(0, word.length() - 1);
                }
            } else {
                if (word.endsWith("e")) {
                    result = word + "s";
                }
                if (word.endsWith("d")) {
                    result = word + "es";
                }
            }
        } catch (NumberFormatException e) {
            result = word;
        }

        return result;
    }

    public static String capitalize(String word) {
        if (word.length() == 0)
            return word;
        return word.substring(0, 1).toUpperCase(Locale.getDefault()) + word.substring(1);
    }

    public static boolean getShowProductsNotSet(Context context) {
        boolean result = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FragmentSetting.PREF_SHOW_PRODUCTS_NOT_SET, false);
        return result;
    }

    public static void setShowProductsNotSet(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putBoolean(FragmentSetting.PREF_SHOW_PRODUCTS_NOT_SET, value);
        editor.commit();
    }


    /**
     * Get the current orientation of a image file
     *
     * @param filePathName file name including the absolute path
     * @return Orientation value
     */
    public static String getImageOrientation(String filePathName) {
        File file = new File(filePathName);
        return getImageOrientation(file);
    }

    /**
     * Get the current orientation of a image file
     *
     * @param file file object with the image
     * @return Orientation value
     */
    public static String getImageOrientation(File file) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            return exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the current rotation angle of an image
     * @param filePathName image file name
     * @return rotation angle
     */
    public static int getImageRotationAngle(String filePathName) {
        String orientation = util.getImageOrientation(filePathName);
        int orientationValue = orientation != null ? Integer.parseInt(orientation) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    /**
     * Get the current rotation angle of an image
     * @param file image file object
     * @return rotation angle
     */
    public static int getImageRotationAngle(File file){
        String orientation = util.getImageOrientation(file);
        int orientationValue = orientation != null ? Integer.parseInt(orientation) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

}