package nl.stevenhack.chatter.activities;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.stevenhack.chatter.R;
import nl.stevenhack.chatter.helpers.ChannelHelper;
import nl.stevenhack.chatter.helpers.SocketHelper;
import nl.stevenhack.chatter.listeners.UpdatesListener;
import nl.stevenhack.chatter.models.Channel;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity implements UpdatesListener {

	private Channel channel;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		// Retrieve the channel name from the intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			channel = ChannelHelper.getChannel(extras.getString("channel_name"));
		}

		// If we do not have a channel, there is nothing to do here (Nearly impossible)
		if (channel == null) {
			finish();
		}

		// Retrieve the listview
		ListView listView = ((ListView)findViewById(R.id.listView));

		// Create an adapter which will be filled with channel names
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, channel.getEvents());
		listView.setAdapter(adapter);

		// Make sure the listview shows the latest messages
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView.setStackFromBottom(true);

		// Subscribe to any new updates from the channel
		ChannelHelper.subscribe(this);

		// Attach a listener to the send button
		Button btn_send = (Button)findViewById(R.id.btn_send);
		btn_send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Retrieve message
				String message = ((EditText)findViewById(R.id.txt_input)).getText().toString().trim();

				// Only continue if there is an actual message
				if (message.length() > 0) {
					// Send message to the server
					try {
						// Workaround for a bug in the Socket.IO library
						// TODO : Fix this bug!
						SocketHelper.getInstance().emit("msg", new JSONArray()
							.put(new JSONObject()
								.put("content", message)
								.put("channel", channel.getName())
							)
							//.put(new JSONObject().put("content", message))
							//.put(new JSONObject().put("channel", channel.getName()))
						);
					}
					catch (JSONException e) {
						e.printStackTrace();
						Log.d("ChatActivity", String.format("Could not send message to channel '%s': %s", channel.getName(), e.getMessage()));
					}

					// Clear the text field
					((EditText)findViewById(R.id.txt_input)).setText("");
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		ChannelHelper.unsubscribe(this);
		super.onDestroy();
	}

	@Override
	public void ChannelUpdated(Channel updatedChannel) {
		// Only process updates from the currently visible channel
		if (updatedChannel.getName().equals(channel.getName())) {
			// Update the adapter
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void ChannelsUpdated(Map<String, Channel> updatedChannels) { }
}
