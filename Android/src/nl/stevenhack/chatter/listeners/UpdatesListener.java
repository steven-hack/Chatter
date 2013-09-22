package nl.stevenhack.chatter.listeners;

import java.util.Map;

import nl.stevenhack.chatter.models.Channel;

public interface UpdatesListener {

	public void ChannelUpdated(Channel updatedChannel);

	public void ChannelsUpdated(Map<String, Channel> updatedChannels);
}
