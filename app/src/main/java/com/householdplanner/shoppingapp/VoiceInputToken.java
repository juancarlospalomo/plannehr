package com.householdplanner.shoppingapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class VoiceInputToken {
	
	private String _productName = "";
	private String _amount = "";
	private int _unitId;

	private String _phrase;
	private ArrayList<String> _words;
	
	public VoiceInputToken(String phrase) {
		_phrase = phrase;
	}
	
	/*
	 *  <item>Unidad de medida</item>
    	<item>gramos</item>
    	<item>kilogramos</item>
    	<item>litros</item>
    	<item>mililitros</item>
    	<item>unidad</item>
    	<item>paquete</item>
    */
	private int getMeasureUnitId(String word) {
		int result = 0;
		if (word.startsWith("gramo")) {
			result = 1;
		}
		if (word.startsWith("kilo")) {
			result = 2;
		}
		if (word.startsWith("litro")) {
			result = 3;
		}
		if (word.startsWith("mililitro")) {
			result = 4;
		}
		if (word.startsWith("unidad")) {
			result = 5;
		}
		if (word.startsWith("paquete")) {
			result = 6;
		}
		return result;
	}
	
	private float convertNumberWordToNumber(String word) {
		float result = -1;
		String lowerCaseWord = word.trim().toLowerCase(Locale.getDefault());
		if (lowerCaseWord.equals("un")
				|| lowerCaseWord.equals("uno")
				|| lowerCaseWord.equals("una")) {
			result = 1;
		}
		if (lowerCaseWord.equals("dos")) result = 2;
		if (lowerCaseWord.equals("tres")) result = 3;
		if (lowerCaseWord.equals("cuatro")) result = 4;
		if (lowerCaseWord.equals("cinco")) result = 5;
		if (lowerCaseWord.equals("seis")) result = 6;
		if (lowerCaseWord.equals("siete")) result = 7;
		if (lowerCaseWord.equals("ocho")) result = 8;
		if (lowerCaseWord.equals("nueve")) result = 9;
		if (lowerCaseWord.equals("diez")) result = 10;
		if (lowerCaseWord.equals("medio")) result = 0.5f;
		if (lowerCaseWord.equals("un cuarto")) result = 0.25f;
		if (lowerCaseWord.equals("tres cuartos")) result = 0.75f;
		return result;
	}
	
	private boolean isQuarter(String word) {
		if (word.equals("cuarto")) return true;
		if (word.equals("cuartos")) return true;
		return false;
	}
	
	private void getAmountAndUnit() {
		float result = -1;
		int index = 0;
	
		while (result==-1 && index<_words.size()) {
			try {
				result = Float.parseFloat(_words.get(index));
			} catch (NumberFormatException e) {
				result = convertNumberWordToNumber(_words.get(index));
			}
			if (result==-1) index++; //Number not found yet
		}
		
		if (result!=-1) {
			//Number found ==> It will be stored in _amount and removed from arrayList;
			//next word is the unit or the end of number (for instance: un cuarto).
			if (_words.size()>index+1) {
				if (isQuarter(_words.get(index+1))) {
					if (result==1) {
						//un cuarto
						_amount = String.valueOf(0.250f);
					}
					if (result==3) {
						//tres cuartos
						_amount = String.valueOf(0.750f);
					}
					if (_words.size()>index+2) {
						_unitId = getMeasureUnitId(_words.get(index+2));
						_words.remove(index+2);
						_words.remove(index+1);
						_words.remove(index);
					}
				} else {
					if (result == (int)result) {
						_amount = String.valueOf((int)result);
					} else {
						_amount = String.valueOf(result);
					}
					_unitId = getMeasureUnitId(_words.get(index+1));
					_words.remove(index+1);
					_words.remove(index);
				};
			}
		}
	}
	
	private void getMarketAndProduct() {
		//In this moment the brand is the last element in the arraylist for sure
		//if brand exists
		int lastPosition = _words.size()-1;
		for (int index=0; index<=lastPosition; index++) {
			if (index==lastPosition) {
				_productName+=_words.get(index);
			} else {
				_productName+=_words.get(index) + " ";
			}
		}
	}
	
	public void parse() {
		_words = new ArrayList<String>(Arrays.asList(_phrase.split(" ")));
		if (_words.size()>0) {
			getAmountAndUnit();
			getMarketAndProduct();
		}
	}
	
	public String getProductName() {
		return _productName;
	}
	
	public String getAmount() {
		return _amount;
	}
	
	public int getUnitId() {
		return _unitId;
	}
	
}
