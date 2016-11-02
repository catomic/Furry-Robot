package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.GSearch;
import com.ferusgrim.furrybot.util.ParseUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

public class Img extends FurryCommand {

    private static final List<String> ACCEPTED_IMAGE_EXTENSIONS = Lists.newArrayList();

    private final String apiToken;
    private final String engineToken;
    private final List<String> sfwChannels;
    private final List<String> nsfwChannels;

    public Img(final CommandManager manager,
               final IDiscordClient bot,
               final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);

        this.apiToken = rawConfig.getNode("api-token").getString("");
        this.engineToken = rawConfig.getNode("engine-token").getString();

        if (this.apiToken.isEmpty()) {
            FurryBot.LOGGER.warn("\"img.api-token\" isn't configured! Disabling 'Img' command...");
        }

        if (this.engineToken.isEmpty()) {
            FurryBot.LOGGER.warn("\"img.engine-token\" isn't configured! Disabling 'Img' command...");
        }

        this.sfwChannels = ParseUtil.getList(rawConfig.getNode("sfw-channelId-whitelist"));
        this.nsfwChannels = ParseUtil.getList(rawConfig.getNode("nsfw-channelId-whitelist"));
    }

    @Override
    public boolean isActive() {
        return this.getRawConfig().getNode("use").getBoolean(false)
                && !(this.apiToken.isEmpty()
                || this.engineToken.isEmpty());
    }

    @Override
    public List<String> isAllowedInChannel(final IChannel channel) {
        final List<String> ids = new ArrayList<>(this.sfwChannels);
        ids.addAll(this.nsfwChannels);

        if (ids.contains(channel.getID())) {
            return Lists.newArrayList();
        }

        return ids;
    }

    @Override
    public String getName() {
        return "img";
    }

    @Override
    public String getDescription() {
        return "I'll get your sexy (or tame) image for you!";
    }

    @Override
    public String getSyntax() {
        return "img <keyword> [keyword...]";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        return GSearch.getImage(
                this.apiToken,
                this.engineToken,
                args,
                ACCEPTED_IMAGE_EXTENSIONS,
                this.nsfwChannels.contains(channel.getID()),
                user.mention(true)
        );
    }
}
