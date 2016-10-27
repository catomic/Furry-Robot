package com.ferusgrim.furrybot.plugin.command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public abstract class FurryCommand {

    private final IUser user;
    private final IChannel channel;
    private final String[] args;

    public FurryCommand(final IUser user, final IChannel channel, final String[] args) {
        this.user = user;
        this.channel = channel;
        this.args = args;
    }

    public IUser getUser() {
        return this.user;
    }

    public IChannel getChannel() {
        return this.channel;
    }

    public String[] getArgs() {
        return this.args;
    }

    public abstract void execute();
}
