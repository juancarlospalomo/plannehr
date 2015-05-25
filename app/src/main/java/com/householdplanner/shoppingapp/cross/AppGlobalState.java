package com.householdplanner.shoppingapp.cross;

import android.content.Context;

import com.householdplanner.shoppingapp.R;
import com.householdplanner.shoppingapp.repositories.MarketRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AppGlobalState {

	public enum AsyncTaskCodes {
		BasketTask(1),
		SynchronizationTask(2);
		
		private int _value;
		private AsyncTaskCodes(int value) {
			_value = value;
		}
		
		public int getValue() {
			return _value;
		}
	}
	
	private enum StatusVar {
		ShoppingMode,
		SynchronizationState,
		Market
	}
	
	private static final String APP_STATUS_INFO = "app.txt";
	private static Boolean _isShoppingMode = null;
	private static Boolean _isSyncNow = null;
	private static Integer _marketId = null;
	private static String _marketName = null;
	private static AppGlobalState _appGlobalState;
	private static boolean _isActive = false;
	
	private AppGlobalState() {

	}
	
	public synchronized static AppGlobalState getInstance() {
		if (_appGlobalState==null) {
			synchronized (AppGlobalState.class) {
				if (_appGlobalState==null) {
					_appGlobalState = new AppGlobalState();
				}
			}
		}
		return _appGlobalState;
	}
	
	public synchronized void setActiveValue(boolean active) {
		_isActive = active;
	}

	public synchronized boolean isShoppingMode(Context context) {
		if (_isShoppingMode==null) {
			if (!existAppInfoFile(context)) {
				writeAppState(context, StatusVar.ShoppingMode, 0);
				_isShoppingMode = Boolean.valueOf(false);
			} else 
				readAppState(context);
		}
		return _isShoppingMode.booleanValue();
	}
		
	public synchronized void setShoppingMode(Context context, boolean value) {
		if (value) {
			writeAppState(context, StatusVar.ShoppingMode, 1);
		} else {
			writeAppState(context, StatusVar.ShoppingMode, 0);
		}
		_isShoppingMode = Boolean.valueOf(value);
	}

	public synchronized String getMarketName(Context context) {
		if (_marketName==null) {
			if (_marketId!=0){
				MarketRepository marketRepository = new MarketRepository(context);
				_marketName = marketRepository.getMarketName(_marketId);
				marketRepository.close();
				if (_marketName==null) {
					/* AppGlobalState file is not synchronized with db
					 * then, we delete it
					 */
					deleteAppInfoFile(context);
				} else {
					if (_marketName.equals("a")) {
						_marketName = context.getResources().getString(R.string.textAllSupermarkets);
					}
				}
			}
		}
		return _marketName;
	}
	
	public synchronized int getMarket(Context context) {
		if (_marketId==null) {
			if (!existAppInfoFile(context)) {
				writeAppState(context, StatusVar.Market, 0);
				_marketId = Integer.valueOf(0);
			} else 
				readAppState(context);
		}
		return _marketId.intValue();
	}
	
	public synchronized void setMarket(Context context, int value, String marketName) {
		_marketId = Integer.valueOf(value);
		writeAppState(context, StatusVar.Market, value);
		if (marketName==null) {
			MarketRepository marketRepository = new MarketRepository(context);
			_marketName = marketRepository.getMarketName(_marketId);
			marketRepository.close();
		} else {
			_marketName = marketName;
		}
	}
	
	private void writeAppState(Context context, StatusVar var, int value) {
		if (existAppInfoFile(context)) readAppState(context);
		String values = "";
		if (var==StatusVar.ShoppingMode) {
			values = String.valueOf(value);
		} else {
			if ((_isShoppingMode!=null) && (_isShoppingMode.booleanValue())) values = "1";
			else values = "0";
		}
		if (var==StatusVar.SynchronizationState) {
			values += String.valueOf(value);
		} else {
			if ((_isSyncNow!=null) && (_isSyncNow.booleanValue())) values += String.valueOf("1");
			else values += String.valueOf("0");
		}
		values += "\t";
		if (var==StatusVar.Market) {
			values += String.valueOf(value);
		} else {
			if (_marketId!=null) values += String.valueOf(_marketId.intValue());
			else values += String.valueOf(0);
		}

		try {
			BufferedWriter appInfoWriter;
			appInfoWriter = new BufferedWriter(new FileWriter(context.getFilesDir() + "/" + APP_STATUS_INFO));
	        appInfoWriter.write(values);
	        appInfoWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readAppState(Context context) {
		BufferedReader appInfoReader = null;
		try {
			String line = null;
			appInfoReader = new BufferedReader(new FileReader(context.getFilesDir() + "/" + APP_STATUS_INFO));
			line = appInfoReader.readLine();
			
			if (line!=null) {
				//Read the Status line.   There's only one
				//Split the line by TAB
				//First two chars are Shopping mode and Sync
				//The second element after TAB is the marketID
				String[] tokens = line.split("\t");
				char[] values = tokens[0].toCharArray();
				if (values[0]=='1') {
					_isShoppingMode = Boolean.valueOf(true);
				} else {
					_isShoppingMode = Boolean.valueOf(false);
				}
				if (values[1]=='1') {
					_isSyncNow = Boolean.valueOf(true);
				} else {
					_isSyncNow = Boolean.valueOf(false);
				}
				_marketId = Integer.valueOf(tokens[1].toString());
			}
			appInfoReader.close();
		} catch (ArrayIndexOutOfBoundsException e) {
			if (appInfoReader!=null)
				try {
					appInfoReader.close();
					deleteAppInfoFile(context);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean existAppInfoFile(Context context) {
		java.io.File infoFile = new java.io.File(context.getFilesDir() + "/" + APP_STATUS_INFO);
		return infoFile.exists();
	}
	
	private void deleteAppInfoFile(Context context) {
		java.io.File infoFile = new java.io.File(context.getFilesDir() + "/" + APP_STATUS_INFO);
		infoFile.delete();
	}
	
}
