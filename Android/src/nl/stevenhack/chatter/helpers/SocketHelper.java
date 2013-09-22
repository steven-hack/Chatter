package nl.stevenhack.chatter.helpers;

import java.net.URI;

import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import nl.stevenhack.chatter.models.Channel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.SocketIOClient;

/**
 * 
 * @author Steven
 *
 * This Helper class handles the Socket.IO connection with the server.
 * All incoming and outgoing events go through this class.
 * Incoming events are redirected towards the correct target.
 * 
 */
public class SocketHelper implements SocketIOClient.Handler {

	/**
	 * This variable is used for sending notifications to the UI thread.
	 * It has to be final because of memory references.
	 */
	//private final Channel channel = null;

	/**
	 * Singleton instance of the SocketIOClient class.
	 */
	private static SocketIOClient client = null;

	/**
	 * Returns the singleton instance of the SocketIOClient class.
	 * If it is not created yet, it does so before returning.
	 */
	public static SocketIOClient getInstance() {
		if (client == null)
			client = new SocketIOClient(URI.create("http://chatter-7482.onmodulus.net"), new SocketHelper());

		return client;
	}

	@Override
	public void onConnect() {
		// Retrieve a list of all subscribed channels
		try {
			client.emit("channel_list", 
				new JSONArray().put(
					new JSONObject().put("identifier", DataHelper.getIdentifier())
				)
			);
		}
		catch (JSONException e) {
			e.printStackTrace();
			Log.d("SocketHelper", String.format("Could not retrieve channels: %s", e.getMessage()));
		}
	}

	@Override
	public void on(String event, JSONArray arguments) {
		// The global event 'nickchanged' is handled seperately.
		if (event.equals("nickchanged")) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DataHelper.getContext());
			String local_nickname = prefs.getString("preference_nickname", "");
			String server_nickname = DataHelper.getStringFromJSONArray(arguments, "nickname");

			if (local_nickname.equals("")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("preference_nickname", server_nickname);
				editor.commit();
			}
			else if (!local_nickname.equals(server_nickname)) {
				// Notify the server of the local nickname
				try {
					client.emit("nickchange", 
						new JSONArray().put(
							new JSONObject().put("nickname", local_nickname)
						)
					);
				}
				catch (JSONException e) {
					e.printStackTrace();
					Log.d("SocketHelper", String.format("Could not update nickname: %s", e.getMessage()));
				}
			}

			return;
		}

		// The global event 'channel_list' is handled seperately.
		if (event.equals("channel_list")) {
			try {
				// Retrieve channels
				JSONObject data = DataHelper.getObjectFromJSONArray(arguments);
				JSONArray channels = (JSONArray) data.get("channels");

				// Loop through channels and add them
				for (int i = 0; i < channels.length(); i++) {
					String name = DataHelper.getStringFromJSONObject(channels.getJSONObject(i), "channel");
					ChannelHelper.addChannel(new Channel(name));
				}

				// Update subscribers on main thread
				android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					public void run() {
						ChannelHelper.notifySubscribers();
					}
				});
			}
			catch (JSONException e) {
				e.printStackTrace();
			}

			return;
		}

		// Every other event is attached to a channel. Retrieve it.
		JSONObject data = DataHelper.getObjectFromJSONArray(arguments);
		String name = DataHelper.getStringFromJSONObject(data, "channel");

		// Retrieve the channel from list of active channels
		// Make this variable final so it can be referenced to the UI Thread
		final Channel channel = ChannelHelper.getChannel(name);

		// If the channel could not be retrieved, there is no reason to go any further
		if (channel == null) {
			Log.d("SocketHelper", String.format("No channel given in event '%s': %s", event, arguments.toString()));
			return;
		}

		// Direct the event to the channel in a proper way
		if (event.equals("log")) {
			// Retrieve the history array
			JSONArray history = DataHelper.getArrayFromJSONObject(data, "history");

			// Loop through all events and add them (Backwards ofcourse)
			for (int i = history.length() - 1; i >= 0; i--) {
				JSONObject log = DataHelper.getObjectFromJSONArray(history, i);

				String type      = DataHelper.getStringFromJSONObject(log, "type");
				//String createdAt = DataHelper.getStringFromJSONObject(log, "createdAt");

				if (type.equals("msg")) {
					String message  = DataHelper.getStringFromJSONObject(log, "message");
					String nickname = DataHelper.getStringFromJSONObject(log, "nickname");
					channel.addEvent("[" + nickname + "] " + message);
				}
				else if (type.equals("join")) {
					String nickname = DataHelper.getStringFromJSONObject(log, "nickname");
					channel.addEvent(nickname + " joined the room.");
				}
				else if (type.equals("part")) {
					String nickname = DataHelper.getStringFromJSONObject(log, "nickname");
					channel.addEvent(nickname + " left the room.");
				}
				else if (type.equals("nickchange")) {
					String old_nick = DataHelper.getStringFromJSONObject(data, "message");
					String new_nick = DataHelper.getStringFromJSONObject(data, "nickname");
					channel.addEvent(old_nick + " renamed to " + new_nick);
				}
			}

			// Update subscribers on main thread
			android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					ChannelHelper.notifySubscribers(channel);
				}
			});
		}
		else if (event.equals("msg")) {
			String nickname = DataHelper.getStringFromJSONObject(data, "nickname");
			String content  = DataHelper.getStringFromJSONObject(data, "content");
			channel.addEvent("[" + nickname + "] " + content);

			// Update subscribers on main thread
			android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					ChannelHelper.notifySubscribers(channel);
				}
			});
		}
		else if (event.equals("user_list")) {
			// TODO
		}
		else if (event.equals("user_joined")) {
			String nickname = DataHelper.getStringFromJSONObject(data, "nickname");
			channel.addEvent(nickname + " joined the room.");

			// Update subscribers on main thread
			android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					ChannelHelper.notifySubscribers(channel);
				}
			});
		}
		else if (event.equals("user_parted")) {
			String nickname = DataHelper.getStringFromJSONObject(data, "nickname");
			channel.addEvent(nickname + " left the room.");

			// Update subscribers on main thread
			android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					ChannelHelper.notifySubscribers(channel);
				}
			});
		}
		else if (event.equals("user_nickchanged")) {
			String old_nick = DataHelper.getStringFromJSONObject(data, "old_nick");
			String new_nick = DataHelper.getStringFromJSONObject(data, "new_nick");
			channel.addEvent(old_nick + " renamed to " + new_nick);

			// Update subscribers on main thread
			android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					ChannelHelper.notifySubscribers(channel);
				}
			});
		}
	}

	@Override
	public void onJSON(JSONObject json) { Log.d("SocketHelper", String.format("New JSON: %s", json.toString())); }

	@Override
	public void onMessage(String message) { Log.d("SocketHelper", String.format("New message: %s", message)); }

	@Override
	public void onDisconnect(int code, String reason) {
		Log.d("SocketHelper", String.format("Disconnected! Code: %d Reason: %s", code, reason));
	}

	@Override
	public void onError(Exception error) {
		Log.e("SocketHelper", "Uknown error:", error);
	}
}
