package nl.stevenhack.chatter.activities;

import java.io.IOException;

import com.codebutler.android_websockets.SocketIOClient;

import nl.stevenhack.chatter.R;
import nl.stevenhack.chatter.fragments.HomeFragment;
import nl.stevenhack.chatter.fragments.SettingsFragment;
import nl.stevenhack.chatter.helpers.DataHelper;
import nl.stevenhack.chatter.helpers.SocketHelper;
import nl.stevenhack.chatter.listeners.CustomTabListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class HomeActivity extends Activity {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	/**
	 * The Socket.IO client for communication with the server.
	 */
	private SocketIOClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();

		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		// For loading reasons, add the default tab first and the others around it.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(new CustomTabListener<HomeFragment>(this, "album", HomeFragment.class)));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
				.setTabListener(new CustomTabListener<SettingsFragment>(this, "album", SettingsFragment.class)));

		// Store the application context for global use
		DataHelper.setContext(getApplicationContext());

		// Store the unique identifier for global use
		TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		DataHelper.setIdentifier(tm.getDeviceId());

		// Create a new SocketIO client
		client = SocketHelper.getInstance();
		client.connect();
	}

	@Override
	protected void onDestroy() {
		try {
			client.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}
}