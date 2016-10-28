package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.DiscordUtil.Mention;
import com.ferusgrim.furrybot.util.ParseUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

import static com.ferusgrim.furrybot.util.DiscordUtil.Mention.Type.USER;
import static com.ferusgrim.furrybot.util.DiscordUtil.Mention.Type.USER_NICKNAME;

public class Bouncer extends FurryCommand {

    private final List<String> normalRoles;
    private final List<String> ageGateRoles;

    public Bouncer(final CommandManager manager,
                   final IDiscordClient bot,
                   final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);

        this.normalRoles = ParseUtil.getList(rawConfig.getNode("add-roles"));
        this.ageGateRoles = ParseUtil.getList(rawConfig.getNode("add-roles-18"));
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public String getName() {
        return "bounce";
    }

    @Override
    public String getDescription() {
        return "Sorts members into their roles.";
    }

    @Override
    public String getSyntax() {
        return "bounce [18+] <@user> [@user ... ]";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, String[] args) {
        final boolean ofAge;
        if (args[0].equalsIgnoreCase("18+")) {
            ofAge = true;
            args = ParseUtil.removeFirstElement(args);
        } else {
            ofAge = false;
        }

        if (args.length < 1) {
            return ":interrobang: Whoops! Let's try that again?: `"
                    + this.getManager().getRawConfig().getNode("prefix").getString("")
                    + this.getSyntax() + "`";
        }

        final List<IRole> normal = DiscordUtil.getRoles(channel.getGuild(), this.normalRoles);
        final List<IRole> age = DiscordUtil.getRoles(channel.getGuild(), this.ageGateRoles);

        for (final String str : args) {
            final Mention mention = DiscordUtil.getMention(str);

            if (mention == null
                    || mention.getType() != USER_NICKNAME && mention.getType() != USER) {
                continue;
            }

            IUser mentioned = DiscordUtil.getUser(channel.getGuild(), mention.getId());
            for (final IRole role : normal) {
                try {
                    mentioned.addRole(role);
                } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
                    DiscordUtil.sendMessage(channel, "Failed to add role: " + role.getName());
                }
            }

            if (ofAge) {
                for (final IRole role : age) {
                    try {
                        mentioned.addRole(role);
                    } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
                        DiscordUtil.sendMessage(channel, "Failed to add role: " + role.getName());
                    }
                }
            }
        }

        return "Done! :D";
    }
}
