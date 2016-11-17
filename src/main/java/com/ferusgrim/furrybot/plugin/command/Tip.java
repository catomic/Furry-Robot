package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.plugin.TipJar;
import com.ferusgrim.furrybot.util.DiscordUtil;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class Tip extends FurryCommand {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("'$'0.00");

    public Tip(final CommandManager manager,
               final IDiscordClient bot,
               final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
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
            return this.topFive(channel);
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

        return this.forUser(mentioned);
    }

    private String forUser(final IUser mention) {
        return "```Markdown\n" +
                mention.getName() + " has " +
                DEC_FORMAT.format(TipJar.getValueOf(mention.getID())) + " in tips!\n" +
                "```\n";
    }

    private String topFive(final IChannel channel) {
        final LinkedHashMap<String, Double> sorted =  TipJar.compileLeaderboard(channel);

        int count = 1;
        final StringBuilder builder = new StringBuilder("```Markdown\n#TipJar Leaderboard\n");

        for (Map.Entry<String, Double> entry : sorted.entrySet()) {
            final IUser leader = DiscordUtil.getUser(channel.getGuild(), entry.getKey());
            builder.append(count).append(". ").append(leader.getName()).append(" : ").append(DEC_FORMAT.format(entry.getValue())).append("\n");
            count++;

            if (count >= 6) {
                break;
            }
        }

        builder.append("```\n");
        return builder.toString();
    }
}