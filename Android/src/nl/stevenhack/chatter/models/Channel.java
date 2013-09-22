package nl.stevenhack.chatter.models;

import java.util.ArrayList;

import nl.stevenhack.chatter.helpers.SocketHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Channel {

	/**
	 * Public enum for channel events.
	 */
	public enum Status { Message, NickChanged, UserList, UserJoined, UserParted, UserQuited, UserNickChanged };

	/**
	 * Properties
	 */
	private String name;
	private ArrayList<String> events;

	/**
	 * A channel always needs a name, this is used as a unique identifier.
	 * Once created, there is no way to rename it.
	 */
	public Channel(String name) {
		this.name = name;
		this.events = null;
	}

	/**
	 * Getters
	 */
	public String getName() {
		return this.name;
	}

	public ArrayList<String> getEvents() {
		// First time events are requested?
		if (this.events == null) {
			// Retrieve events from server
			try {
				SocketHelper.getInstance().emit("channel_log", 
						new JSONArray().put(
								new JSONObject().put("channel", this.name)
						)
				);
			}
			catch (JSONException e) {
				e.printStackTrace();
				Log.d("SocketHelper", String.format("Could not retrieve log of channel '%s': %s", this.name, e.getMessage()));
			}

			// While loading, return an empty list
			this.events = new ArrayList<String>();
		}

		return this.events;
	}

	/**
	 * Event handlers
	 */
	public void addEvent(String event) {
		if (this.events == null) {
			this.events = new ArrayList<String>();
		}

		this.events.add(event);
	}
}
