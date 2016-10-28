package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.ParseUtil;
import com.ferusgrim.furrybot.util.DiscordUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.util.List;

public class Leaver {

    private final String channelId;
    private final List<String> messages;

    private Leaver(final String channelId,
                   final List<String> messages) {
        this.channelId = channelId;
        this.messages = messages;
    }

    @EventSubscriber
    public void onUserLeave(final UserLeaveEvent event) {
        final IChannel channel = DiscordUtil.getChannel(event.getGuild(), this.channelId);

        if (channel == null) {
            FurryBot.LOGGER.error("Channel can't be found!: {}", this.channelId);
            return;
        }

        final String user = event.getUser().mention(true);
        final String server = event.getGuild().getName();

        for (String message : this.messages) {
            DiscordUtil.sendMessage(channel, message
                    .replace("{user}", user)
                    .replace("{server}", server));
        }
    }

    public static Leaver configure(final ConfigurationNode node) {
        if (!node.getNode("use").getBoolean(false)) {
            FurryBot.LOGGER.warn("You've disabled the 'Leaver' plugin from \"leaving.use\"!");
            return null;
        }

        final String channelId = node.getNode("channelId").getString("");
        if (channelId.isEmpty()) {
            FurryBot.LOGGER.error("\"leaving.channelId\" wasn't configured. Disabling 'Leaver' plugin.");
            return null;
        }

        final List<String> messages = ParseUtil.getList(node.getNode("messages"));
        if (messages.isEmpty()) {
            FurryBot.LOGGER.error("\"leaving.messages\" wasn't configured. Disabling 'Leaver' plugin.");
            return null;
        }

        return new Leaver(channelId, messages);
    }
}
