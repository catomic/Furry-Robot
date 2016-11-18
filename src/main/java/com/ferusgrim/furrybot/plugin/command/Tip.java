package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.plugin.TipJar;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.ParseUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tip extends FurryCommand {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("'$'0.00");

    private final List<String> modRoles;

    public Tip(final CommandManager manager,
               final IDiscordClient bot,
               final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);

        this.modRoles = ParseUtil.getList(rawConfig.getNode("mod-roles"));
    }

    @Override
    public String getName() {
        return "tip";
    }

    @Override
    public String getDescription() {
        return "Lets me show you how many tips you've acquired!";
    }

    @Override
    public String getSyntax() {
        return "tip [top/@user]";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        if (args.length == 0) {
            return this.forUser(user);
        }

        if (args[0].equalsIgnoreCase("top")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("words")) {
                return this.topWords(channel);
            }

            return this.topUsers(channel);
        }

        if (args[0].equalsIgnoreCase("words")) {
            return this.topUserWords(user);
        }

        if (args[0].equalsIgnoreCase("del")) {
            return this.deleteUser(channel, user, ParseUtil.removeFirstElement(args));
        }

        final DiscordUtil.Mention mention = DiscordUtil.getMention(args[0]);

        if (mention == null) {
            return this.forUser(user);
        }

        final IUser mentioned = DiscordUtil.getUser(channel.getGuild(), mention.getId());

        if (mentioned == null) {
            return "Whoops! That's an invalid user!";
        }

        if (mentioned == this.getBot().getOurUser()) {
            return "I don't have any tips!";
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("words")) {
            return this.topUserWords(mentioned);
        }

        return this.forUser(mentioned);
    }

    private String deleteUser(final IChannel channel, final IUser user, final String[] args) {
        boolean allow = false;
        for (final IRole role : user.getRolesForGuild(channel.getGuild())) {
            if (this.modRoles.contains(role.getID())) {
                allow = true;
            }
        }

        if (!allow) {
            return ":interrobang: Looks like you don't have permission to do this!";
        }

        if (args.length < 1) {
            return "You didn't specify anyone!";
        }


        final DiscordUtil.Mention mention = DiscordUtil.getMention(args[0]);

        if (mention == null) {
            return "Whoops! That's an invalid user!";
        }

        final IUser victim = DiscordUtil.getUser(channel.getGuild(), mention.getId());

        TipJar.deleteUser(victim.getID());

        final String grammar;
        if (victim.getName().endsWith("'s") || victim.getName().endsWith("'")) {
            grammar = "";
        } else if (victim.getName().endsWith("s")) {
            grammar = "'";
        } else {
            grammar = "'s";
        }

        return "Deleted " + victim.getName() + grammar + " acquired tips!";
    }

    private String topUserWords(final IUser mentioned) {
        final LinkedHashMap<String, Integer> sorted = TipJar.compileWordUserLeaderboard(mentioned.getID());

        final int maxLength = this.getHighestLength(sorted.keySet(), 6);
        int count = 1;

        final StringBuilder builder = new StringBuilder("```Markdown\n#TipJar Word Leadboard (for ").append(mentioned.getName()).append(")\n");

        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            builder.append(String.format("%s. %-" + maxLength + "s : %s\n", count, entry.getKey(), entry.getValue()));
            count++;

            if (count >= 6) {
                break;
            }
        }

        builder.append("```\n");
        return builder.toString();
    }

    private String topWords(final IChannel channel) {
        final LinkedHashMap<String, Integer> sorted = TipJar.compileWordLeaderboard(channel);

        int count = 1;
        final int maxLength = this.getHighestLength(sorted.keySet(), 6);

        final StringBuilder builder = new StringBuilder("```MarkDown\n#TipJar Word Leaderboards\n");

        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            builder.append(String.format("%s. %-" + maxLength + "s : %s\n", count, entry.getKey(), entry.getValue()));

            count++;
            if (count >= 6) {
                break;
            }
        }

        builder.append("```\n");
        return builder.toString();
    }

    private String topUsers(final IChannel channel) {
        final LinkedHashMap<String, Double> sorted =  TipJar.compileUserLeaderboard(channel);

        int count = 1;
        final int maxLength = this.getHighestLength(sorted.keySet(), 6);

        // TODO: AESTHETIC SPACING
        final StringBuilder builder = new StringBuilder("```Markdown\n#TipJar User Leaderboards\n");

        for (Map.Entry<String, Double> entry : sorted.entrySet()) {
            builder.append(String.format("%s. %-" + maxLength + "s : %s\n", count,
                    DiscordUtil.getUser(channel.getGuild(), entry.getKey()).getName(),
                    DEC_FORMAT.format(entry.getValue())));

            count++;

            if (count >= 6) {
                break;
            }
        }

        builder.append("```\n");
        return builder.toString();
    }

    private String forUser(final IUser mention) {
        return "```Markdown\n" +
                mention.getName() + " has " +
                DEC_FORMAT.format(TipJar.getValueOf(mention.getID())) + " in tips!\n" +
                "```\n";
    }

    private int getHighestLength(final Set<String> words, final int loop) {
        int count = 0;
        int maxLength = 0;

        for (final String str : words) {
            if (str.length() > maxLength) {
                maxLength = str.length();
            }

            count++;
            if (count >= loop) {
                break;
            }
        }

        return maxLength + 1;
    }
}
