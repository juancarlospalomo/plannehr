package com.householdplanner.shoppingapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ValidationDialog extends DialogFragment {
	
	public ValidationDialog() {
		super();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String[] message = getArguments().getStringArray("message");
		String title = getArguments().getString("title");
		String alertMessage = "";
		Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		
		alertDialogBuilder.setTitle(title);
		for (int i=0; i<message.length; i++) {
			alertMessage+=message[i] + System.getProperty("line.separator");
		}
		alertDialogBuilder.setMessage(alertMessage);
		alertDialogBuilder.setPositiveButton(android.R.string.ok, null);
		return alertDialogBuilder.create();
	}
	
}

