package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.SqLiteUtil;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.nio.file.Path;
import java.text.DecimalFormat;
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

            final Naughties naughty = Naughties.of(part);

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
                final Naughties naughty = Naughties.of(results.getString(COL_CURSE));
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

        CUNT(1.0, "cunt", "cunts"),
        FUCK(.5, "fuck", "fucker", "fucks", "fucking"),
        MFER(.5, "motherfucker", "motherfucking"),
        FAG(.5, "fag", "faggot", "faggots"),
        SHIT(.25, "shit", "shitty", "shits", "shitter", "shitting"),
        BULL(.25, "bullshit"),
        DICK(.1, "dick", "dicks", "dicking"),
        TITS(.1, "tits"),
        AHOLE(.1, "asshole", "assholes"),
        COCK(.1, "cock", "cocks"),
        ASS(.05, "ass", "asses"),
        BITCH(.05, "bitch", "bitching", "bitcher"),
        PISS(.05, "piss", "pisses", "pisser", "pissing"),
        DAMN(.01, "damn", "damned", "goddamn", "goddamned"),
        CLEAN(0, "clean")
        ;

        private final double worth;
        private final String main;
        private final String[] variations;

        Naughties(final double worth, final String main, final String... variations) {
            this.worth = worth;
            this.main = main;
            this.variations = variations;
        }

        public static Naughties of(final String word) {
            for (final Naughties naughty : Naughties.values()) {
                if (naughty == CLEAN) {
                    continue;
                }

                if (naughty.main.equalsIgnoreCase(word)) {
                    return naughty;
                }

                for (final String variation : naughty.variations) {
                    if (variation.equalsIgnoreCase(word)) {
                        return naughty;
                    }
                }
            }

            return CLEAN;
        }
    }
}
