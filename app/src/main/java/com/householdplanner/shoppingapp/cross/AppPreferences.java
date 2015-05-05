package com.householdplanner.shoppingapp.cross;

import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.householdplanner.shoppingapp.R;

public class AppPreferences extends PreferenceActivity {

    private final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 1;
    private final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 2;
    public static final Uri URI_LIST_TABLE = Uri.parse("sqlite://com.householdplanner.shoppingapp/list");
	public static final Uri URI_HISTORY_TABLE = Uri.parse("sqlite://com.householdplanner.shoppingapp/ProductHistory");
    public static final String PREF_GOOGLE_DRIVE_ACCOUNT = "prefGoogleDriveAccount";
	public static final String PREF_UNIQUE_ID = "prefUUID";
	public static final String PREF_ALLOW_COLLECT_DATA = "prefAllowCollectData";
	public static final String PREF_SHOW_PRODUCTS_NOT_SET = "prefShowProductsNotSet";
	public static final String PREF_GCM_REGISTRATION_ID = "prefGCMRegistrationId";
	public static final String PREF_PROPERTY_APP_VERSION = "appVersion";
	public static final String PREF_MEMBER_ID = "prefMemberId";
	private final String PREF_CLEAR_CACHE = "prefClearCache";
	
	private String mAccountNameSelected = null;
	ListPreference mList = null;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
