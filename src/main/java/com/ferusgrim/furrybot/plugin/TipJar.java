package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.SqLiteUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final String QU_MASTER = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";

    private TipJar(){}

    @EventSubscriber
    public void onUserMessage(final MessageReceivedEvent event) {
        if (event.getMessage().getChannel().isPrivate()) {
            return; // Don't allow PMs.
        }

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

    public static LinkedHashMap<String, Double> compileUserLeaderboard(final IChannel channel) {
        final List<String> ids = getStoredIds();
        final Map<String, Double> users = Maps.newHashMap();
        for (final String id : ids) {
            users.put(id, getValueOf(id));
        }

        return users.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static LinkedHashMap<String, Integer> compileWordLeaderboard(final IChannel channel) {
        final List<String> ids = getStoredIds();
        final Map<String, Integer> counts = Maps.newHashMap();
        for (final String id : ids) {
            final Map<Naughties, Integer> user = getUser(id);

            for (Map.Entry<Naughties, Integer> count : user.entrySet()) {
                counts.put(count.getKey().main,
                        counts.containsKey(count.getKey().main) ?
                                counts.get(count.getKey().main) + count.getValue() :
                                count.getValue());
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static LinkedHashMap<String, Integer> compileWordUserLeaderboard(final String id) {
        final Map<String, Integer> user = Maps.newHashMap();

        for (Map.Entry<Naughties, Integer> entry : getUser(id).entrySet()) {
            user.put(entry.getKey().main, entry.getValue());
        }

        return user.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static double getValueOf(final String id) {
        final Map<Naughties, Integer> useMap = getUser(id);

        double value = 0.0;
        for (final Map.Entry<Naughties, Integer> entry : useMap.entrySet()) {
            value = value + (entry.getKey().worth * entry.getValue());
        }

        return value;
    }

    private static Map<Naughties, Integer> getUser(final String id) {
        final Map<Naughties, Integer> useMap = Maps.newHashMap();

        SqLiteUtil.execute(DATABASE_FILE, replace(EX_CREATE, id));

        SqLiteUtil.query(DATABASE_FILE, replace(QU_ALL, id), results -> {
            while(results.next()) {
                final Naughties naughty = Naughties.of(results.getString(COL_CURSE), true);
                final int used = results.getInt(COL_USED);

                useMap.put(naughty, used);
            }
        });

        return useMap;
    }

    private static List<String> getStoredIds() {
        final List<String> ids = Lists.newArrayList();

        SqLiteUtil.query(DATABASE_FILE, QU_MASTER, results -> {
            while (results.next()) {
                ids.add(results.getString(1));
            }
        });

        return ids;
    }

    public static TipJar configure(final ConfigurationNode node) {
        if (!node.getNode("use").getBoolean(false)) {
            FurryBot.LOGGER.warn("You've disabled the 'TipJar' plugin from \"tipjar.use\"!");
            return null;
        }

        return new TipJar();
    }

    private enum Naughties {
        CLEAN(0, false, "ERROR"),
        CUNT(1, true, "cunt"),
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

        public static Naughties of(String word, final boolean fromDatabase) {
            for (final Naughties naughty : Naughties.values()) {
                if (naughty == CLEAN) {
                    continue; // Don't ever check the "CLEAN" enum.
                }

                word = word
                        .replaceAll("[^a-zA-Z]", "")
                        .toLowerCase();

                if (fromDatabase) {
                    if (naughty.main.equals(word)) {
                        return naughty; // Found a match.
                    }

                    continue;
                }

                if (naughty.strict) {
                    if (word.contains(naughty.main)) {
                        return naughty; // Found a match.
                    }

                    continue; // This is a strict check, so we only care about 'main'
                }

                if (!word.contains(naughty.main)) {
                    continue; // This is NOT a strict check, and the word doesn't contain 'main'
                }

                if (word.equals(naughty.main)) {
                    return naughty; // Found a match.
                }

                // This is NOT a strict check, and the word doesn't EQUAL 'main'.

                for (final String variant : naughty.variations) {
                    if (word.contains(variant)) {
                        return naughty; // Found a match.
                    }
                }

                // This is NOT a strict check, and the word doesn't EQUAL 'main' or CONTAIN 'variations'.
            }

            return CLEAN;
        }
    }
}
