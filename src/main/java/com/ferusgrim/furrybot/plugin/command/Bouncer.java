package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.google.common.collect.Lists;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.stream.Collectors;

public class Bouncer extends FurryCommand {

    public Bouncer(final FurryBot bot, final IUser user, final IChannel channel, final IMessage message, final String[] args) {
        super(bot, user, channel, message, args);
    }

    @Override
    public void execute() {
        boolean allow = false;
        for (final IRole role : this.getUser().getRolesForGuild(this.getChannel().getGuild())) {
            if (this.getBot().getConfig().getBouncerIds().contains(role.getID())) {
                allow = true;
                break;
            }
        }

        if (!allow) {
            DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + "How about no?");
            return;
        }

        if (this.getArgs().length < 1) {
            DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " If you're not more specific, I'm ganna bounce YOU.");
            return;
        }

        final boolean ofAge;
        if (this.getArgs()[0].equalsIgnoreCase("18+")) {
            ofAge = true;
        } else {
            ofAge = false;
        }

        if (this.getMessage().getMentions().size() == 0) {
            DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " You forget to mention people!");
            return;
        }

        final List<IRole> rolesToAdd = Lists.newArrayList();
        rolesToAdd.addAll(this.getChannel().getGuild().getRoles().stream()
                .filter(role -> this.getBot().getConfig().getRolesToBeAddedWhenBounced().contains(role.getID()))
                .collect(Collectors.toList()));

        if (ofAge) {
            rolesToAdd.addAll(this.getChannel().getGuild().getRoles().stream()
                    .filter(role -> this.getBot().getConfig().getAgeGatedRoles().contains(role.getID()))
                    .collect(Collectors.toList()));
        }

        if (rolesToAdd.isEmpty()) {
            DiscordUtil.sendMessage(this.getChannel(), "This bot isn't setup to add roles when bounced!");
            return;
        }

        for (final IUser user : this.getMessage().getMentions()) {
            for (final IRole role : rolesToAdd) {
                try {
                    user.addRole(role);
                } catch (final MissingPermissionsException | RateLimitException | DiscordException e) {
                    DiscordUtil.sendMessage(this.getChannel(), "Whoops, I hit an error!");
                    FurryBot.LOGGER.error("Hit exception while adding role to user: u={} r={}", user.getID(), role.getID(), e);
                }
            }
        }

        DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " Done!");
    }
}
