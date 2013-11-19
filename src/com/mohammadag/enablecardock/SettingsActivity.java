package com.mohammadag.enablecardock;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/* Consistency with Xposed, make the user feel this is part of it. */
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		addPreferencesFromResource(R.xml.preferences);

		findPreference(Constants.SETTINGS_KEY_AUTO_LAUNCH_CAR_HOME).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sendBroadcast(new Intent(Constants.INTENT_SETTINGS_UPDATED));
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
