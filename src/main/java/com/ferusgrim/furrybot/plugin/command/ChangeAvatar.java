package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class ChangeAvatar extends FurryCommand {

    public ChangeAvatar(final CommandManager manager,
                        final IDiscordClient bot,
                        final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public String getName() {
        return "avatar";
    }

    @Override
    public String getDescription() {
        return "Changes my avatar!";
    }

    @Override
    public String getSyntax() {
        return "avatar <url>";
    }

    @Override
    public String execute(IChannel channel, IUser user, String[] args) {
        final String link = args[0];

        try {
            if (link.endsWith(".png")) {
                this.getBot().changeAvatar(Image.forUrl("png", link));
            } else if (link.endsWith(".jpg")) {
                this.getBot().changeAvatar(Image.forUrl("jpg", link));
            } else {
                return "Ferus is lazy and has only added support for `.png` and `.jpg` images!";
            }
        } catch (DiscordException e) {
            FurryBot.LOGGER.error("Encounted error while changing avatar!: {}", link, e);
            return "Whoops! Something went wrong while changing my avatar!";
        } catch (RateLimitException e) {
            return "Looks like you tried to change me too often!";
        }

        return "I hope you like it! :D";
    }
}
