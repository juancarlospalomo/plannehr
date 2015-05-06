package com.householdplanner.shoppingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class HelpActivity extends AppCompatActivity {

	public static final String EXTRA_HELP_SCREEN = "helpScreen";
	
	public static final String PARAM_SHARE_SCREEN = "shareScreen";
	public static final String PARAM_WALLET_SCREEN = "walletScreen";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle data = getIntent().getExtras();
		if (data!=null) {
			String param = data.getString(EXTRA_HELP_SCREEN);
			if (param.equals(PARAM_SHARE_SCREEN)) {
				setContentView(R.layout.sharing_screen_help);
				ImageView view = (ImageView) findViewById(R.id.imageHelpSharingKeyNote2);
				view.setOnClickListener(new AccountsOnClick());
			} else if (param.equals(PARAM_WALLET_SCREEN)) {
				setContentView(R.layout.budget_screen_help);
			}
		}
		getSupportActionBar().setTitle(R.string.title_activity_help);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override 
	protected void onStop() {
		super.onStop();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.action_closeHelp:
				finish();
				return true;
			default: 
				return super.onOptionsItemSelected(item);
		}
	}

	private class AccountsOnClick implements View.OnClickListener {

		@SuppressLint("InlinedApi")
		@Override
		public void onClick(View v) {
			   Intent addAccountIntent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT)
			    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    addAccountIntent.putExtra(android.provider.Settings.EXTRA_ACCOUNT_TYPES, new String[] {"com.google"});
			    startActivity(addAccountIntent); 
		}
		
	}
	
	
}
