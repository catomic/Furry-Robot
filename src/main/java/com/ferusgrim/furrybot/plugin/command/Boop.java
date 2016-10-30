package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.ConfigUtil;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.DiscordUtil.Mention;
import com.ferusgrim.furrybot.util.SqLiteUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.nio.file.Path;
import java.util.List;

public class Boop extends FurryCommand {

    private static final Path DATABASE_FILE = FurryBot.PROGRAM_DIR.resolve("boops.db").toAbsolutePath();

    private static final String VAR_BOOPER = "{booper}";
    private static final String VAR_VICTIM = "{victim}";

    private static final String COL_VICTIM = "victim";
    private static final String COL_COUNT = "boops";

    private static final String EX_CREATE = "CREATE TABLE IF NOT EXISTS `{booper}` (victim STRING UNIQUE, boops INTEGER)";
    private static final String EX_INSERT = "INSERT OR IGNORE INTO `{booper}` VALUES('{victim}', 0)";
    private static final String EX_UPDATE = "UPDATE `{booper}` SET boops = boops + 1 WHERE victim = '{victim}'";
    private static final String QU_BOOPNO = "SELECT boops FROM `{booper}` WHERE victim = '{victim}'";
    private static final String QU_TOPFIV = "SELECT * FROM `{booper}` ORDER BY boops DESC LIMIT 5";

    public Boop(final CommandManager manager,
                final IDiscordClient bot,
                final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);

        ConfigUtil.createLocalFile(DATABASE_FILE);
    }

    @Override
    public String getName() {
        return "boop";
    }

    @Override
    public String getDescription() {
        return "Let me boop someone for you!";
    }

    @Override
    public String getSyntax() {
        return "boop [user]";
    }

    @Override
    public String execute(final IChannel channel, final IUser booper, final String[] args) {
        if (args.length == 0) {
            return this.getBoopTopFive(channel, booper);
        }

        final Mention mention = DiscordUtil.getMention(args[0]);

        if (mention == null) {
            return this.getBoopTopFive(channel, booper);
        }

        final IUser mentioned = DiscordUtil.getUser(channel.getGuild(), mention.getId());

        if (mentioned == null) {
            return "Whoops! That's an invalid user!";
        }

        if (mentioned == booper) {
            return "You can't boop yourself!";
        }

        return this.boopUser(channel, booper, mentioned);
    }

    private String boopUser(final IChannel channel, final IUser booper, final IUser victim) {
        final String booperId = booper.getID();
        final String victimId = victim.getID();
        SqLiteUtil.execute(DATABASE_FILE,
                this.replace(EX_CREATE, booperId),
                this.replace(EX_INSERT, booperId, victimId),
                this.replace(EX_UPDATE, booperId, victimId));

        final StringBuilder builder = new StringBuilder();
        builder.append("You've booped ")
                .append(victim.getDisplayName(channel.getGuild()));

        SqLiteUtil.query(DATABASE_FILE, this.replace(QU_BOOPNO, booperId, victimId), results -> {
            if (!results.next()) {
                builder.append(" ERROR times!");
            } else {
                final int count = results.getInt(COL_COUNT);
                builder.append(" ").append(count).append(" time").append(count == 1 ? "!" : "s!");
            }
        });

        return builder.toString();
    }

    private String getBoopTopFive(final IChannel channel, final IUser booper) {
        final List<String> ids = Lists.newArrayList();
        final List<Integer> counts = Lists.newArrayList();
        SqLiteUtil.query(DATABASE_FILE, this.replace(QU_TOPFIV, booper.getID()), results -> {
            while(results.next()) {
                ids.add(results.getString(COL_VICTIM));
                counts.add(results.getInt(COL_COUNT));
            }
        });

        final StringBuilder builder = new StringBuilder();

        builder.append("```fix\n");

        if (ids.isEmpty()) {
            builder.append("You haven't booped anyone!\n```\n");
            return builder.toString();
        }

        for (int i = 1; i < 6; i++) {
            if (ids.size() < i || counts.size() < i) {
                break;
            }

            final String id = ids.get(i - 1);
            final int count = counts.get(i - 1);

            builder.append(i).append(": You've booped ")
                    .append(DiscordUtil.getUser(channel.getGuild(), id)
                            .getDisplayName(channel.getGuild())).append(" ")
                    .append(count).append(" time")
                    .append(count == 1 ? "!" : "s!")
                    .append("\n");
        }

        builder.append("```\n");

        return builder.toString();
    }

    private String replace(final String query, final String booper) {
        return query.replace(VAR_BOOPER, booper);
    }

    private String replace(final String query, final String booper, final String victim) {
        return query.replace(VAR_BOOPER, booper).replace(VAR_VICTIM, victim);
    }
}
