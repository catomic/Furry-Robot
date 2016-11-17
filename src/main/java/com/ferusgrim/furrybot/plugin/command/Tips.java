package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.plugin.TipJar;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.text.DecimalFormat;

public class Tips extends FurryCommand {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("'$'0.00");

    public Tips(final CommandManager manager,
                final IDiscordClient bot,
                final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
    }

    @Override
    public String getName() {
        return "tips";
    }

    @Override
    public String getDescription() {
        return "Lets me show you how many tips you've acquired!";
    }

    @Override
    public String getSyntax() {
        return "tips";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        return "You've accumulated: " + DEC_FORMAT.format(TipJar.getValueOf(user.getID()));
    }
}
