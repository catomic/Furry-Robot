package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class ChangeAvatar extends FurryCommand {

    public ChangeAvatar(final FurryBot bot, final IUser user, final IChannel channel, final String[] args) {
        super(bot, user, channel, args);
    }

    @Override
    public void execute() {
        if (this.getArgs().length == 0) {
            DiscordUtil.sendMessage(this.getChannel(), "You gotta enter an avatar, bro!");
            return;
        }

        final String link = this.getArgs()[0];

        try {
            if (link.endsWith(".png")) {
                this.getBot().getClient().changeAvatar(Image.forUrl("png", link));
            } else if (link.endsWith(".jpg")) {
                this.getBot().getClient().changeAvatar(Image.forUrl("jpg", link));
            } else {
                DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " Only accepting images in `.png` or `.jpg` format!");
                return;
            }
        } catch (final DiscordException e) {
            FurryBot.LOGGER.error("Encountered error trying to change avatar!: {}", link, e);
            DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " Whoops. I broke trying to change my avatar!");
        } catch (final RateLimitException e) {
            DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " You can't change me that often!");
        }

        DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " Done! :D");
    }
}
