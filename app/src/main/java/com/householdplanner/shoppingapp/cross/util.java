package com.householdplanner.shoppingapp.cross;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.fragments.FragmentSetting;
import com.householdplanner.shoppingapp.stores.ProductHistoryStore;
import com.householdplanner.shoppingapp.stores.ShoppingListStore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class util {
	
	public static String getCompleteProductRow(Context context, Cursor cursor, boolean includeMarket) {
		String itemText;
		itemText = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_PRODUCT_NAME));
		if (includeMarket) {
			String marketName = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_MARKET));
			if (marketName!=null) itemText += " " + capitalize(marketName);
		}
		String amount = cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT));
		itemText += " " + cursor.getString(cursor.getColumnIndex(ShoppingListStore.COLUMN_AMOUNT));
		itemText += " " + util.getMeasureUnitName(context, cursor.getInt(cursor.getColumnIndex(ShoppingListStore.COLUMN_UNIT_ID)), amount);
		return itemText;
	}

	public static String getCompleteHistoryRow(Context context, Cursor cursor, boolean includeMarket) {
		String itemText;
		itemText = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_PRODUCT_NAME));
		if (includeMarket) {
			String marketName = cursor.getString(cursor.getColumnIndex(ProductHistoryStore.COLUMN_MARKET));
			if (marketName!=null) itemText += " " + capitalize(marketName);
		}
		return itemText;
	}
	
	public static String getMeasureUnitName(Context context, int id, String amount) {
		String result = "";
	
		String[] items = context.getResources().getStringArray(R.array.measure_array);
		if (id>0) {
			result = getCorrectWordNumber(items[id], amount);
		}
		return result;
	}
	
	public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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

	public static Date getDate(String dateFormatted) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
			return dateFormat.parse(dateFormatted);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * return: 0 => both dates are equals
	 * 		   1 => first date greater than the second one
	 *         2 => second date greater than the first one
	 */
	public static int compareDates(String firstDate, String secondDate) {
		int result = 0;
		Date dateFirst, dateSecond;
		dateFirst = getDate(firstDate);
		dateSecond = getDate(secondDate);
		result = dateFirst.compareTo(dateSecond);
		if (result<0) result = 2;
		else {
			if (result>0) result = 1;
		}
		return result;
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
					result = word.substring(0, word.length()-1);
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
	
	public static void showAlertErrorMessage(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId);
		builder.setTitle(R.string.textErrorTitle);
		builder.setInverseBackgroundForced(true);
		builder.create().show();
	}
	
	public static void showAlertInfoMessage(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId);
		builder.setTitle(R.string.textInfoTitle);
		builder.setInverseBackgroundForced(true);
		builder.create().show();
	}

	public static void showAlertInfoMessage(Context context, int messageId, int messagePositiveId, int messageNegativeId, DialogInterface.OnClickListener positiveHandler, 
			DialogInterface.OnClickListener negativeHandler) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId);
		builder.setTitle(R.string.textInfoTitle);
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton(context.getResources().getString(messagePositiveId), positiveHandler);
		builder.setNegativeButton(context.getResources().getString(messageNegativeId), negativeHandler);
		builder.create().show();
	}
	
	public static void showAlertInfoMessage(Context context, int messageId, int messagePositiveId, int messageNegativeId, DialogInterface.OnClickListener positiveHandler, 
			DialogInterface.OnClickListener negativeHandler, 
			DialogInterface.OnClickListener cancelHandler) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId);
		builder.setTitle(R.string.textInfoTitle);
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton(context.getResources().getString(messagePositiveId), positiveHandler);
		builder.setNeutralButton(context.getResources().getString(messageNegativeId), negativeHandler);
		builder.setNegativeButton(R.string.btnCancel, cancelHandler);
		builder.create().show();
	}

	public static String capitalize(String word) {
	    if(word.length() == 0)
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
}