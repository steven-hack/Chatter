package nl.stevenhack.chatter.fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.stevenhack.chatter.R;
import nl.stevenhack.chatter.helpers.SocketHelper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("preference_nickname")) {
			// Retrieve nickname
			String nickname = sharedPreferences.getString(key, "Guest");

			// Notify server
			try {
				SocketHelper.getInstance().emit("nickchange", 
					new JSONArray().put(
						new JSONObject().put("nickname", nickname)
					)
				);
			}
			catch (JSONException e) {
				e.printStackTrace();
				Log.d("SettingsFragment", String.format("Could not change nickname to '%s': %s", nickname, e.getMessage()));
			}
		}
	}
}