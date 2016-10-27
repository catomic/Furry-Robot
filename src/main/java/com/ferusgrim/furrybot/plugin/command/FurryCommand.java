package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.plugin.FurryBotPlugin;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class FurryCommand extends FurryBotPlugin {

    private final IUser user;
    private final IChannel channel;
    private final IMessage message;
    private final String[] args;

    public FurryCommand(final FurryBot bot, final IUser user, final IChannel channel, final IMessage message, final String[] args) {
        super(bot);
        this.user = user;
        this.channel = channel;
        this.message = message;
        this.args = args;
    }

    public IUser getUser() {
        return this.user;
    }

    public IChannel getChannel() {
        return this.channel;
    }

    public IMessage getMessage() {
        return this.message;
    }

    public String[] getArgs() {
        return this.args;
    }

    public abstract void execute();
}
