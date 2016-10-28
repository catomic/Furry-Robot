package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.util.DiscordUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Pong extends FurryCommand {

    public Pong(final CommandManager manager,
                final IDiscordClient bot,
                final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Says \"Pong!\"";
    }

    @Override
    public String getSyntax() {
        return "ping";
    }

    @Override
    public String execute(IChannel channel, IUser user, String[] args) {
        DiscordUtil.sendMessage(channel, user.mention(true) + " Pong!");
        return "";
    }
}
