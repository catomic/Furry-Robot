package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.ParseUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

import static com.ferusgrim.furrybot.FurryBot.LOGGER;

public class CommandManager {

    private final IDiscordClient client;
    private final ConfigurationNode rawConfig;
    private final List<FurryCommand> commands;

    public CommandManager(final IDiscordClient client, final ConfigurationNode rawConfig) {
        this.client = client;
        this.rawConfig = rawConfig;
        this.commands = Lists.newArrayList();
    }

    public void registerCommands() {
        if (!this.commands.isEmpty()) {
            LOGGER.error("Already attempted to register commands!");
        }

        this.commands.add(new Pong(this, this.client, this.rawConfig.getNode("ping")));
        this.commands.add(new Bouncer(this, this.client, this.rawConfig.getNode("bounce")));
        this.commands.add(new ChangeAvatar(this, this.client, this.getRawConfig().getNode("avatar")));
        this.commands.add(new Gif(this, this.client, this.getRawConfig().getNode("gif")));
        this.commands.add(new AboutBot(this, this.client, this.getRawConfig().getNode("about")));
        this.commands.add(new HelpMe(this, this.client, this.getRawConfig().getNode("help")));
        this.commands.add(new Boop(this, this.client, this.getRawConfig().getNode("boop")));
    }

    public IDiscordClient getClient() {
        return this.client;
    }

    public ConfigurationNode getRawConfig() {
        return this.rawConfig;
    }

    public List<FurryCommand> getCommands() {
        return this.commands;
    }

    @EventSubscriber
    public void onMessage(final MessageReceivedEvent event) {
        String[] args = event.getMessage().getContent().split("\\s+");

        if (args.length == 0) {
            return; // Not sure what would cause this, but it can't hurt to check.
        }

        if (!args[0].startsWith(this.rawConfig.getNode("prefix").getString(""))) {
            return;
        }

        final FurryCommand command = this.getCommand(args[0].substring(1));

        if (command == null) {
            return; // No command by the name
        }

        args = ParseUtil.removeFirstElement(args);

        final IChannel channel = event.getMessage().getChannel();
        final IUser user = event.getMessage().getAuthor();

        if (!command.isActive()) {
            DiscordUtil.sendMessage(channel, ":interrobang: Command isn't active!");
            return;
        }

        if (!this.hasPermission(event.getMessage().getGuild(), user, command.getRequiredRoles())) {
            DiscordUtil.sendMessage(channel, ":interrobang: Looks like you don't have permission to do this!");
            return;
        }

        final List<String> channels = command.isAllowedInChannel(channel);

        if (!channels.isEmpty()) { // Not allowed in this channel!
            DiscordUtil.sendMessage(channel, "That command isn't allowed in this channel!");
            DiscordUtil.sendMessage(channel, "Try: " + this.getChannelStr(
                    DiscordUtil.getChannels(event.getMessage().getGuild(), channels)));
            return;
        }

        if (args.length < command.getRequiredArguments()) {
            DiscordUtil.sendMessage(channel, ":interrobang: Whoops! Let's try that again?: `"
                    + this.rawConfig.getNode("prefix").getString("") + command.getSyntax() + "`");
            return;
        }

        final String response = command.execute(channel, user, args);

        if (response == null || !response.isEmpty()) {
            DiscordUtil.sendMessage(channel, response);
        }
    }

    private String getChannelStr(final List<IChannel> channels) {
        final StringBuilder builder = new StringBuilder();
        String sep = "";

        for (final IChannel channel : channels) {
            builder.append(sep);
            sep = " ";
            builder.append(channel.mention());
        }

        return builder.toString();
    }

    FurryCommand getCommand(final String cmd) {
        for (final FurryCommand check : this.commands) {
            if (check.getName().equalsIgnoreCase(cmd)) {
                return check;
            }
        }

        return null;
    }

    private boolean hasPermission(final IGuild guild, final IUser user, final List<String> required) {
        if (required.isEmpty()) {
            return true; // Assume no roles are required.
        }

        for (final IRole role : user.getRolesForGuild(guild)) {
            if (required.contains(role.getID())) {
                return true;
            }
        }

        return false;
    }
}
