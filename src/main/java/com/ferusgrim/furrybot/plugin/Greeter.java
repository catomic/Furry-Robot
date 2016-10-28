package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.ParseUtil;
import javafx.concurrent.Task;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Greeter {

    private final String channelId;
    private final String rulesChannelId;
    private final List<String> helpList;
    private final List<String> messages;

    private Greeter(final String channelId,
                   final String rulesChannelId,
                   final List<String> helpList,
                   final List<String> messages) {
        this.channelId = channelId;
        this.rulesChannelId = rulesChannelId;
        this.helpList = helpList;
        this.messages = messages;
    }

    @EventSubscriber
    public void onUserJoin(final UserJoinEvent event) {
        final IChannel channel = DiscordUtil.getChannel(event.getGuild(), this.channelId);
        final IChannel rulesChannel = DiscordUtil.getChannel(event.getGuild(), this.rulesChannelId);

        if (channel == null) {
            FurryBot.LOGGER.error("Greet Channel can't be found!: {}", this.channelId);
            return;
        }

        boolean showRulesMessage = true;
        if (rulesChannel == null) {
            FurryBot.LOGGER.error("Rules Channel can't be found!: {}", this.rulesChannelId);
            showRulesMessage = false;
        }

        final String helpStr = this.getHelpStr(event.getGuild());
        boolean showHelpersMessage = true;
        if (this.helpList.isEmpty()) {
            showHelpersMessage = false;
        }


        final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.schedule(new GreeterTask(
                exec, channel, this.messages, showRulesMessage, showHelpersMessage,
                event.getUser().mention(true), event.getGuild().getName(), helpStr,
                rulesChannel == null ? "" : rulesChannel.mention()
        ), 3, TimeUnit.SECONDS);
    }

    private String getHelpStr(final IGuild guild) {
        final StringBuilder builder = new StringBuilder();
        String sep = "";

        for (int i = 0; i < this.helpList.size(); i++) {
            final IRole sorter = guild.getRoleByID(this.helpList.get(i));

            if (i + 1 == this.helpList.size()) {
                builder.append(" or a ");
                sep = "";
            }

            builder.append(sep);
            sep = ", ";

            if (sorter == null) {
                builder.append(this.helpList.get(i));
            } else if (sorter.isMentionable()) {
                builder.append(sorter.mention());
            } else {
                builder.append(sorter.getName());
            }
        }

        return builder.toString();
    }

    public static Greeter configure(final ConfigurationNode node) {
        if (!node.getNode("use").getBoolean(false)) {
            FurryBot.LOGGER.warn("You've disabled the 'Greeter' plugin from \"welcome.use\"!");
            return null;
        }

        final String channelId = node.getNode("channelId").getString("");
        if (channelId.isEmpty()) {
            FurryBot.LOGGER.error("\"welcome.channelId\" wasn't configured. Disabling 'Greeter' plugin.");
            return null;
        }

        final List<String> messages = ParseUtil.getList(node.getNode("messages"));
        if (messages.isEmpty()) {
            FurryBot.LOGGER.error("\"welcome.messages\" wasn't configured. Disabling 'Greeter' plugin.");
            return null;
        }

        final String rulesChannelId = node.getNode("rulesChannelId").getString("");
        if (rulesChannelId.isEmpty()) {
            FurryBot.LOGGER.warn("\"welcome.rulesChannelId\" wasn't configured.");
        }

        final List<String> helpList = ParseUtil.getList(node.getNode("ping-for-help"));
        if (helpList.isEmpty()) {
            FurryBot.LOGGER.warn("\"welcome.ping-for-help\" wasn't configured.");
        }

        return new Greeter(channelId, rulesChannelId, helpList, messages);
    }

    private class GreeterTask implements Runnable {
        private final ScheduledExecutorService exec;
        private final IChannel channel;
        private final List<String> messages;
        private final boolean showRulesMessage;
        private final boolean showHelperMessage;
        private final String user;
        private final String server;
        private final String helpStr;
        private final String rules;

        GreeterTask(final ScheduledExecutorService exec,
                    final IChannel channel,
                    final List<String> messages,
                    final boolean showRulesMessage,
                    final boolean showHelperMessage,
                    final String user,
                    final String server,
                    final String helpStr,
                    final String rules) {
            this.exec = exec;
            this.channel = channel;
            this.messages = messages;
            this.showRulesMessage = showRulesMessage;
            this.showHelperMessage = showHelperMessage;
            this.user = user;
            this.server = server;
            this.helpStr = helpStr;
            this.rules = rules;
        }

        @Override
        public void run() {
            for (final String message : this.messages) {
                if (!this.showHelperMessage && message.contains("{helpers}")
                        || !this.showRulesMessage && message.contains("{rules}")) {
                    continue;
                    // Ignore lines with {helpers} if there are none configured.
                    // Ignore lines with {rules} if rules channel didn't return.
                }

                DiscordUtil.sendMessage(channel, message
                        .replace("{user}", this.user)
                        .replace("{server}", this.server)
                        .replace("{helpers}", this.helpStr)
                        .replace("{rules}", this.rules));
            }

            exec.shutdown();
        }
    }
}
