package com.ferusgrim.furrybot.util;

import com.ferusgrim.furrybot.FurryBot;
import com.google.common.collect.Lists;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DiscordUtil {

    private static final Pattern PATTERN_MENTION = Pattern.compile("<(#|@!|@&|@)([0-9]+)>");

    public static void sendMessage(final IChannel channel, final String message) {
        try {
            channel.sendMessage(message);
        } catch (final MissingPermissionsException | RateLimitException | DiscordException e) {
            FurryBot.LOGGER.error("Failed to send message: {}", message, e);
        }
    }

    public static IChannel getChannel(final IGuild guild, final String channelId) {
        return guild.getChannelByID(channelId);
    }

    public static List<IChannel> getChannels(final IGuild guild, List<String> channelIds) {
        final List<IChannel> channels = Lists.newArrayList();
        channels.addAll(channelIds.stream().map(id -> getChannel(guild, id)).collect(Collectors.toList()));
        return channels;
    }

    public static IUser getUser(final IGuild guild, final String userId) {
        return guild.getUserByID(userId);
    }

    public static IRole getRole(final IGuild guild, final String roleId) {
        return guild.getRoleByID(roleId);
    }

    public static List<IRole> getRoles(final IGuild guild, final List<String> roleIds) {
        final List<IRole> roles = Lists.newArrayList();
        roles.addAll(roleIds.stream().map(id -> getRole(guild, id)).collect(Collectors.toList()));
        return roles;
    }

    public static Mention getMention(final String original) {
        final Matcher matcher = PATTERN_MENTION.matcher(original);
        if (!matcher.find()) {
            return null;
        }

        if (matcher.groupCount() < 2) {
            return null;
        }

        final Mention.Type type = Mention.Type.of(matcher.group(1));

        if (type == null) {
            return null;
        }

        final String id = matcher.group(2);

        return new Mention(type, original, id);
    }

    public static class Mention {
        public enum Type {
            USER,
            USER_NICKNAME,
            ROLE,
            CHANNEL;

            public static Type of(final String seq) {
                switch (seq) {
                    case "@":
                        return USER;
                    case "@!":
                        return USER_NICKNAME;
                    case "@&":
                        return ROLE;
                    case "#":
                        return CHANNEL;
                    default:
                        return null;
                }
            }
        }

        private final Type type;
        private final String original;
        private final String id;

        public Mention(final Type type, final String original, final String id) {
            this.type = type;
            this.original = original;
            this.id = id;
        }

        public Type getType() {
            return this.type;
        }

        public String getOriginal() {
            return this.original;
        }

        public String getId() {
            return this.id;
        }
    }
}
