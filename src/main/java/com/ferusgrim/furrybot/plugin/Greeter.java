package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import javafx.concurrent.Task;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.List;

public class Greeter extends FurryBotPlugin {

    public Greeter(final FurryBot bot) {
        super(bot);
    }

    @EventSubscriber
    public void onUserJoin(final UserJoinEvent event) {
        try {
            // Sometimes the bot can re-act quicker than Discord.
            // This task is done on a three (3) second delay, to ensure
            // the user is viewing channels before posting the welcome message.
            final Thread thread = new Thread(new GreeterTask(this.getBot(), event));
            thread.start();
        } catch (final Exception e) {
            FurryBot.LOGGER.error("Encountered error while greeting new member: ", e);
        }
    }

    private static class GreeterTask extends Task<Void> {

        private final FurryBot furryBot;
        private final UserJoinEvent event;

        public GreeterTask(final FurryBot furryBot, final UserJoinEvent event) {
            this.furryBot = furryBot;
            this.event = event;
        }

        @Override
        protected Void call() throws Exception {
            Thread.sleep(3000);
            final IChannel channel = this.furryBot.getClient().getChannelByID(this.furryBot.getConfig().getGreetChannel());

            final String user = event.getUser().mention();
            final String server = event.getGuild().getName();
            final String sorters = this.getSorterString(event.getGuild());
            final String rules = event.getGuild().getChannelByID(this.furryBot.getConfig().getRulesChannel()).mention();

            for (String message : this.furryBot.getConfig().getGreetMessage()) {
                message = message
                        .replace("{user}", user)
                        .replace("{server}", server)
                        .replace("{sorters}", sorters)
                        .replace("{rules}", rules);

                DiscordUtil.sendMessage(channel, message);
            }

            return null;
        }

        private String getSorterString(final IGuild guild) {
            final List<String> sortList = this.furryBot.getConfig().getSorters();

            final StringBuilder builder = new StringBuilder();
            String sep = "";

            for (int i = 0; i < sortList.size(); i++) {
                final IRole sorter = guild.getRoleByID(sortList.get(i));

                if (i + 1 == sortList.size()) {
                    builder.append(" or a ");
                    sep = "";
                }

                builder.append(sep);
                sep = ", ";

                if (sorter == null) {
                    builder.append(sortList.get(i));
                } else if (sorter.isMentionable()) {
                    builder.append(sorter.mention());
                } else {
                    builder.append(sorter.getName());
                }
            }

            return builder.toString();
        }
    }
}
