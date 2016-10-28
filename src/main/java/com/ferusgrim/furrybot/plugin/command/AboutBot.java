package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.util.DiscordUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class AboutBot extends FurryCommand {

    private static final String ABOUT_MESSAGE = "```\n" +
            "FurryBot the Anthropomorphic Sexy-Time Assistant\n" +
            "================================================\n\n" +
            "Version: 1.0-SNAPSHOT\n" +
            "Author: @FerusGrim#0376\n" +
            "Source: https://github.com/FerusGrim/FurryBot\n" +
            "```\n";

    public AboutBot(final CommandManager manager,
                    final IDiscordClient bot,
                    final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
    }

    @Override
    public String getName() {
        return "about";
    }

    @Override
    public String getDescription() {
        return "Displays information about FurryBot!";
    }

    @Override
    public String getSyntax() {
        return "about";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        return ABOUT_MESSAGE;
    }
}
