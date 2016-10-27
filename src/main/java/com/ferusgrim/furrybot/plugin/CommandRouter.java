package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.plugin.command.FurryCommand;
import com.ferusgrim.furrybot.plugin.command.Gif;
import com.ferusgrim.furrybot.plugin.command.Pong;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CommandRouter extends FurryBotPlugin {

    private final String prefix;

    public CommandRouter(final FurryBot bot) {
        super(bot);

        this.prefix = bot.getConfig().getCommandPrefix();
    }

    @EventSubscriber
    public void onMessage(final MessageReceivedEvent event) {
        if (!event.getMessage().getContent().startsWith(this.prefix)) {
            return;
        }

        final IUser author = event.getMessage().getAuthor();
        final IChannel channel = event.getMessage().getChannel();
        String[] content = this.split(event.getMessage().getContent());
        final String command = content[0].replaceFirst(Pattern.quote(this.prefix), "");

        content = this.delimit(content);

        FurryCommand cmd = null;
        if (command.equalsIgnoreCase("ping")) {
            cmd = new Pong(author, channel, content);
        } else if (command.equalsIgnoreCase("gif")) {
            cmd = new Gif(author, channel, content);
        }

        if (cmd == null) {
            return;
        }

        cmd.execute();
    }

    public String[] split(final String content) {
        return content.split("\\s+");
    }

    public String[] delimit(final String[] old) {
        return Arrays.copyOfRange(old, 1, old.length);
    }
}
