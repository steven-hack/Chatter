package nl.stevenhack.chatter.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.stevenhack.chatter.listeners.UpdatesListener;
import nl.stevenhack.chatter.models.Channel;

/**
 *
 * @author Steven
 *
 * This Helper class holds all active channels.
 * Channels can be added, retrieved and removed.
 * 
 */
public class ChannelHelper {

	/**
	 * This array holds all active channels.
	 */
	private static Map<String, Channel> lstChannels = new HashMap<String, Channel>();

	/**
	 * List of subscribers that want to receive updates.
	 */
	private static ArrayList<UpdatesListener> lstSubscribers = new ArrayList<UpdatesListener>();

	/**
	 * Adds the given channel to the list of active channels.
	 * If the channel already exist, false is returned.
	 */
	public static boolean addChannel(Channel channel) {
		// Check if there isn't a channel with that name yet
		if (lstChannels.containsKey(channel.getName()))
			return false;

		// Add the channel to the list
		lstChannels.put(channel.getName(), channel);
		return true;
	}

	/**
	 * Returns the channel matching the given name.
	 * If no channel was found, null is returned
	 */
	public static Channel getChannel(String name) {
		return lstChannels.get(name);
	}

	public static Channel removeChannel(String name) {
		Channel result = lstChannels.remove(name);
		return result;
	}
	/**
	 * Removes the channel and returns the removed object.
	 * If no channel was found, null is returned.
	 */
	public static Channel removeChannel(Channel channel) {
		Channel result = lstChannels.remove(channel.getName());
		return result;
	}

	/**
	 * This method notifies all subscribers about the changes.
	 */
	public static void notifySubscribers() {
		for (UpdatesListener listener : lstSubscribers) {
			listener.ChannelsUpdated(lstChannels);
		}
	}

	/**
	 * This method notifies all subscribers about a update for channel.
	 */
	public static void notifySubscribers(Channel channel) {
		for (UpdatesListener listener : lstSubscribers) {
			listener.ChannelUpdated(channel);
		}
	}

	/**
	 * Subscribes the given listener for updates.
	 */
	public static boolean subscribe(UpdatesListener listener) {
		return lstSubscribers.add(listener);
	}

	/**
	 * Unsubscribes the given listener for updates.
	 */
	public static boolean unsubscribe(UpdatesListener listener) {
		return lstSubscribers.remove(listener);
	}
}
