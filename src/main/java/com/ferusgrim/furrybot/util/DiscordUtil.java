package com.ferusgrim.furrybot.util;

import com.ferusgrim.furrybot.FurryBot;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public final class DiscordUtil {

    public static void sendMessage(final IChannel channel, final String message) {
        try {
            channel.sendMessage(message);
        } catch (final MissingPermissionsException | RateLimitException | DiscordException e) {
            FurryBot.LOGGER.error("Failed to send message: {}", message, e);
        }
    }
}
