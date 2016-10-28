package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.ferusgrim.furrybot.util.ParseUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HereAttacker {

    private final List<String> safeChannels;
    private final String message;
    private final List<Victim> victims;

    private HereAttacker(final List<String> safeChannels, final String message) {
        this.safeChannels = safeChannels;
        this.message = message;
        this.victims = Lists.newArrayList();

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(new HereEnforcer(this), 0, 10, TimeUnit.SECONDS);
    }

    @EventSubscriber
    public void onMessage(final MessageReceivedEvent event) {
        if (!event.getMessage().getContent().contains("@here")
                && !event.getMessage().getContent().contains("@everyone")) {
            return;
        }

        if (this.safeChannels.contains(event.getMessage().getChannel().getID())) {
            FurryBot.LOGGER.error("BLACKLISTED CHANNEL!");
            return;
        }

        final IUser user = event.getMessage().getAuthor();

        DiscordUtil.sendMessage(event.getMessage().getChannel(), user.mention(true) + "You're _really_ going to regret that...");

        final Victim victim = this.getVictim(event.getMessage().getGuild(), user.getID());

        if (victim == null) {
            this.victims.add(new Victim(event.getMessage().getGuild(), user.getID(), System.currentTimeMillis()));
        } else {
            victim.startedAttack = System.currentTimeMillis();
            victim.active = true;
        }
    }

    private Victim getVictim(final IGuild guild, final String id) {
        for (final Victim victim : this.victims) {
            if (victim.guild == guild && victim.id.equals(id)) {
                return victim;
            }
        }

        return null;
    }

    public static HereAttacker configure(final ConfigurationNode node) {
        if (!node.getNode("use").getBoolean(false)) {
            FurryBot.LOGGER.warn("You've disabled the 'HereAttacker' plugin from \"here-attacker.use\". This is probably a good idea, actually.");
            return null;
        }

        final List<String> channelBlackList = ParseUtil.getList(node.getNode("safe-channels"));
        if (channelBlackList.isEmpty()) {
            FurryBot.LOGGER.warn("No channel is safe from 'HereAttacker'!");
        }

        final String message = node.getNode("message").getString("");

        return new HereAttacker(channelBlackList, message);
    }

    private class HereEnforcer implements Runnable {

        private final HereAttacker attacker;

        HereEnforcer(final HereAttacker attacker) {
            this.attacker = attacker;
        }

        @Override
        public void run() {
            try {Thread.sleep(1000); // Pause for 1 second
            } catch (final InterruptedException e) {
                FurryBot.LOGGER.error("Killed self while attacking people.", e);
            }

            for (final Victim victim : new ArrayList<>(this.attacker.victims)) {
                if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - victim.startedAttack) > 5) {
                    victim.active = false; // Only spam for 5 minutes
                }

                if (!victim.active) {
                    continue;
                }

                final IUser user = DiscordUtil.getUser(victim.guild, victim.id);

                if (user == null) {
                    victim.active = false;
                    continue;
                }

                try {
                    user.getOrCreatePMChannel().sendMessage(this.attacker.message.isEmpty() ?
                            "It's payback time!" : this.attacker.message);
                } catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
                    FurryBot.LOGGER.error("Failed to create PM with user: {}:{}", user.getName(), user.getID());
                    victim.active = false;
                }
            }
        }
    }

    private class Victim {

        private final IGuild guild;
        private final String id;
        private long startedAttack;
        private boolean active;

        private Victim(final IGuild guild, final String id, long startedAttack) {
            this.guild = guild;
            this.id = id;
            this.startedAttack = startedAttack;
            this.active = true;
        }
    }
}
