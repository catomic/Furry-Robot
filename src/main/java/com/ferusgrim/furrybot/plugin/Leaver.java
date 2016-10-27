package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.obj.IChannel;

public class Leaver extends FurryBotPlugin {

    public Leaver(final FurryBot bot) {
        super(bot);
    }

    @EventSubscriber
    public void onUserLeave(final UserLeaveEvent event) {
        final IChannel channel = this.getBot().getClient().getChannelByID(this.getBot().getConfig().getGreetChannel());

        final String user = event.getUser().mention();
        final String server = event.getGuild().getName();

        for (String message : this.getBot().getConfig().getLeaveMessage()) {
            message = message
                    .replace("{user}", user)
                    .replace("{server}", server);


            DiscordUtil.sendMessage(channel, message);
        }
    }
}
