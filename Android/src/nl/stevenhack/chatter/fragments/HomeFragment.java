package nl.stevenhack.chatter.fragments;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.stevenhack.chatter.R;
import nl.stevenhack.chatter.activities.ChatActivity;
import nl.stevenhack.chatter.helpers.ChannelHelper;
import nl.stevenhack.chatter.helpers.SocketHelper;
import nl.stevenhack.chatter.listeners.UpdatesListener;
import nl.stevenhack.chatter.models.Channel;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class HomeFragment extends ListFragment implements UpdatesListener {

	private ArrayList<String> lstChannels;
	private ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Implement a menu for joining new channels
		setHasOptionsMenu(true);

		// This array list will hold all channels
		lstChannels = new ArrayList<String>();

		// Create an adapter which will be filled with channel names
		adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_selectable_list_item, lstChannels);
		setListAdapter(adapter);

		// Subscribe to channel updates
		ChannelHelper.subscribe(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Implement the ability to select multiple items
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_selectable_list_item, lstChannels));
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.fragment_home, menu);
				setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, lstChannels));
				return true;
			}
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
		            case R.id.menu_delete:
		            	DeleteChannels();
		                mode.finish();
		                return true;
		            default:
		                return false;
		        }
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				mode.setTitle(getListView().getCheckedItemCount() + " selected");
			}
		});
	};

	@Override
	public void onDestroy() {
		ChannelHelper.unsubscribe(this);
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Start up the ChannelActivity with the selected name
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		intent.putExtra("channel_name", lstChannels.get(position));
		startActivity(intent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
		inflater.inflate(R.menu.activity_home, menu);
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_new:
				// Set an EditText view to get user input 
				final EditText input = new EditText(this.getActivity());

				AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
				alert.setTitle(R.string.popup_new_title);
				alert.setMessage(R.string.popup_new_msg);
				alert.setView(input);

				alert.setPositiveButton("Join", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int button) {
						String value = "#" + input.getText().toString();

						// Create a new channel object
						Channel channel = new Channel(value);
						ChannelHelper.addChannel(channel);
						ChannelHelper.notifySubscribers();

						// Notify the server
						try {
							SocketHelper.getInstance().emit("join", 
								new JSONArray().put(
									new JSONObject().put("channel", value)
								)
							);
						}
						catch (JSONException e) {
							e.printStackTrace();
							Log.d("HomeFragment", String.format("Could not join channel '%s': %s", value, e.getMessage()));
						}
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int button) { }
				});

				alert.show();
				return true;
			default:
				return false;
		}
	};

	@Override
	public void ChannelUpdated(Channel updatedChannel) { }

	@Override
	public void ChannelsUpdated(Map<String, Channel> updatedChannels) {
		// Clear the old list
		lstChannels.clear();

		// Loop through the channels
		for (Channel channel : updatedChannels.values()) {
			lstChannels.add(channel.getName());
		}

		// Notify adapter that the collection has been changed
		adapter.notifyDataSetChanged();
	}

	public void DeleteChannels() {
		SparseBooleanArray selection = getListView().getCheckedItemPositions();

		if (selection != null && selection.size() > 0) {
			// Loop through the selection and leave the channels
			for (int i = 0; i < selection.size(); i++) {
				if (selection.valueAt(i)) {
					// Retrieve channel name
					String item = getListView().getAdapter().getItem(selection.keyAt(i)).toString();
					ChannelHelper.removeChannel(item);
	
					// Tell server that the client parted
					try {
						SocketHelper.getInstance().emit("part", 
							new JSONArray().put(
								new JSONObject().put("channel", item)
							)
						);
					}
					catch (JSONException e) {
						e.printStackTrace();
						Log.d("HomeFragment", String.format("Could not part channel '%s': %s", item, e.getMessage()));
					}
				}
			}
	
			// Notify subscribers that the collection has been changed
			ChannelHelper.notifySubscribers();
		}
	}
}