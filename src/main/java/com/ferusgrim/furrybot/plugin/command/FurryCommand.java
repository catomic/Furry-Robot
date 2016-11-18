package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.util.ParseUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public abstract class FurryCommand {

    private final CommandManager manager;
    private final IDiscordClient bot;
    private final ConfigurationNode rawConfig;

    public FurryCommand(final CommandManager manager,
                        final IDiscordClient bot,
                        final ConfigurationNode rawConfig) {
        this.manager = manager;
        this.bot = bot;
        this.rawConfig = rawConfig;
    }

    public boolean isActive() {
        return this.rawConfig.getNode("use").getBoolean(false);
    }

    public List<String> isAllowedInChannel(final IChannel channel) {
        return Lists.newArrayList();
    }

    public CommandManager getManager() {
        return this.manager;
    }

    public IDiscordClient getBot() {
        return this.bot;
    }

    public ConfigurationNode getRawConfig() {
        return this.rawConfig;
    }

    public int getRequiredArguments() {
        return 0;
    }

    public List<String> getRequiredRoles() {
        return ParseUtil.getList(this.getRawConfig().getNode("role-permissions"));
    }

    public boolean allowsPrivate() {
        return false;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract String execute(final IChannel channel, final IUser user, final String[] args);
}
