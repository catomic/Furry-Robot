package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.SqLiteUtil;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.nio.file.Path;
import java.util.Map;

public class TipJar {

    private static final Path DATABASE_FILE = FurryBot.PROGRAM_DIR.resolve("tipjar.db").toAbsolutePath();

    private static final String VAR_CURSER = "{curser}";
    private static final String VAR_CURSE = "{curse}";

    private static final String COL_CURSE = "naughty_word";
    private static final String COL_USED = "used";

    private static final String EX_CREATE = "CREATE TABLE IF NOT EXISTS `{curser}` (`naughty_word` STRING UNIQUE, `used` INTEGER)";
    private static final String EX_INSERT = "INSERT OR IGNORE INTO `{curser}` VALUES('{curse}', 0)";
    private static final String EX_UPDATE = "UPDATE `{curser}` SET `used` = `used` + 1 WHERE `naughty_word` = '{curse}'";
    private static final String QU_ALL = "SELECT * FROM `{curser}`";

    private TipJar(){}

    @EventSubscriber
    public void onUserMessage(final MessageReceivedEvent event) {
        final String[] parts = event.getMessage().getContent().split("\\s+");

        for (final String part : parts) {
            if (DiscordUtil.getMention(part) != null) {
                continue; // Ignore curse words in usernames.
            }

            final Naughties naughty = Naughties.of(part, false);

            if (naughty == Naughties.CLEAN) {
                continue; // No results - clean.
            }

            final String id = event.getMessage().getAuthor().getID();

            SqLiteUtil.execute(DATABASE_FILE,
                    replace(EX_CREATE, id),
                    replace(EX_INSERT, id, naughty.main),
                    replace(EX_UPDATE, id, naughty.main));
        }
    }

    private static String replace(final String query, final String id) {
        return query.replace(VAR_CURSER, id);
    }

    private static String replace(final String query, final String id, final String word) {
        return query.replace(VAR_CURSER, id).replace(VAR_CURSE, word);
    }

    public static double getValueOf(final String id) {
        final Map<Naughties, Integer> useMap = Maps.newHashMap();

        SqLiteUtil.execute(DATABASE_FILE, replace(EX_CREATE, id));

        final String query = replace(QU_ALL, id);

        SqLiteUtil.query(DATABASE_FILE, replace(QU_ALL, id), results -> {
            double temp = 0.0;

            while(results.next()) {
                final Naughties naughty = Naughties.of(results.getString(COL_CURSE), true);
                final int used = results.getInt(COL_USED);

                useMap.put(naughty, used);
            }
        });

        double value = 0.0;
        for (final Map.Entry<Naughties, Integer> entry : useMap.entrySet()) {
            value = value + (entry.getKey().worth * entry.getValue());
        }

        return value;
    }

    public static TipJar configure(final ConfigurationNode node) {
        if (!node.getNode("use").getBoolean(false)) {
            FurryBot.LOGGER.warn("You've disabled the 'TipJar' plugin from \"tipjar.use\"!");
            return null;
        }

        return new TipJar();
    }

    private enum Naughties {
        CLEAN(0, false, null),
        FUCK(.5, true, "fuck"),
        SHIT(.25, true, "shit"),
        BEPIS(.25, true, "bepis"),
        BLOWJOB(.2, true, "blowjob"),
        CUM(.2, false, "cum", "cumslut"),
        RIMJOB(.2, true, "rimjob"),
        WHORE(.15, true, "whore"),
        SCHLONG(.1, true, "schlong"),
        SKANK(.1, true, "skank"),
        DICK(.1, true, "dick"),
        PISS(.1, true, "piss"),
        PUSSY(.1, true, "pussy"),
        BITCH(.1, true, "bitch"),
        SLUT(.1, true, "slut"),
        HANDJOB(.1, true, "handjob"),
        TITS(.1, true, "tits"),
        TWAT(.1, true, "twat"),
        WANK(.1, true, "wank"),
        ARSE(.1, false, "arse", "arses", "areshole"),
        ASS(.1, false, "ass", "asshat", "assbag", "dumbass", "assbite", "assclown", "asses", "asshole", "asslick", "asspirate", "asswipe"),
        COCK(.1, false, "cock", "cockbite", "cockhead", "cocksucker"),
        JERKOFF(.05, true, "jerkoff"),
        DOUCHE(.05, true, "douche"),
        CLIT(.05, true, "clit"),
        BONER(.05, true, "boner"),
        JIZZ(.05, true, "jizz"),
        BASTARD(.05, true, "bastard"),
        DILDO(.02, true, "dildo"),
        BUTTPLUG(.02, true, "buttplug"),
        CHODE(.02, true, "chode"),
        DAMN(.01, true, "damn"),
        HELL(.01, false, "hell")
        ;

        private final double worth;
        private final boolean strict;
        private final String main;
        private final String[] variations;

        Naughties(final double worth, final boolean strict, final String main, final String... variations) {
            this.worth = worth;
            this.strict = strict;
            this.main = main;
            this.variations = variations;
        }

        public static Naughties of(final String word, final boolean fromDatabase) {
            for (final Naughties naughty : Naughties.values()) {
                if (naughty == CLEAN) {
                    continue;
                }

                if (fromDatabase) {
                    if (naughty.main.equals(word)) {
                        return naughty;
                    }

                    continue;
                }

                if (naughty.strict) {
                    if (word.toLowerCase().contains(naughty.main)) {
                        return naughty;
                    }

                    continue;
                }

                if (!word.toLowerCase().contains(naughty.main)) {
                    continue;
                }

                for (final String variant : naughty.variations) {
                    if (word.toLowerCase().contains(variant)) {
                        return naughty;
                    }
                }
            }

            return CLEAN;
        }
    }
}
