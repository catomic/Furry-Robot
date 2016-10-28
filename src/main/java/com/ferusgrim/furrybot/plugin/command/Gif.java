package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.ParseUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gif extends FurryCommand {

    private static final String API_STR = "https://www.googleapis.com/customsearch/v1?key={key}&cx={engine}&safe={level}&alt=json&searchType=image&q={query}";
    private static final String LEVEL_SFW = "high";
    private static final String LEVEL_NSFW = "off";

    private final String apiToken;
    private final String engineToken;
    private final List<String> sfwChannels;
    private final List<String> nsfwChannels;

    public Gif(final CommandManager manager,
               final IDiscordClient bot,
               final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);

        this.apiToken = rawConfig.getNode("api-token").getString("");
        this.engineToken = rawConfig.getNode("engine-token").getString("");

        if (this.apiToken.isEmpty()) {
            FurryBot.LOGGER.warn("\"gif.api-token\" isn't configured! Disabling 'Gif' command...");
        }

        if (this.engineToken.isEmpty()) {
            FurryBot.LOGGER.warn("\"gif.engine-token\" isn't configured! Disabling 'Gif' command...");
        }

        this.sfwChannels = ParseUtil.getList(this.getRawConfig().getNode("sfw-channelId-whitelist"));
        this.nsfwChannels = ParseUtil.getList(this.getRawConfig().getNode("nsfw-channelId-whitelist"));
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
        return "gif";
    }

    @Override
    public String getDescription() {
        return "Send me out into the wilderness to collect your gifs!";
    }

    @Override
    public String getSyntax() {
        return "gif <keyword> [keyword ... ]";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        final URL url = this.convertArgs(args, this.nsfwChannels.contains(channel.getID()) ? LEVEL_NSFW : LEVEL_SFW);
        if (url == null) {
            return "Looks like I messed up the URL somehow!";
        }

        final URLConnection conn;
        try {
            conn = url.openConnection();
        } catch (IOException e) {
            FurryBot.LOGGER.error("Failed to connect to URL: {}", url, e);
            return "Looks like I messed up connecting to the URL somehow!";
        }

        String line;
        StringBuilder builder = new StringBuilder();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (final IOException ignored) {
            return "We reached our limit, sorry! Try again, later. :)";
        }

        final JsonArray items = new JsonParser().parse(builder.toString()).getAsJsonObject().getAsJsonArray("items");

        if (items == null) {
            return "No results found!";
        }

        JsonObject json = null;
        for (final JsonElement item : items) {
            final String link = item.getAsJsonObject().getAsJsonPrimitive("link").getAsString();
            if (link.endsWith(".gif") || link.endsWith(".gifv")) {
                json = item.getAsJsonObject();
                break;
            }
        }

        if (json == null) {
            return "Couldn't find any GIFs!";
        }

        final String link = json.getAsJsonPrimitive("link").getAsString();
        final int height = json.getAsJsonObject("image").getAsJsonPrimitive("height").getAsInt();
        final int width = json.getAsJsonObject("image").getAsJsonPrimitive("width").getAsInt();

        final DecimalFormat decFormat = new DecimalFormat("#.00");
        final double size = json.getAsJsonObject("image").getAsJsonPrimitive("byteSize").getAsDouble() / 1000 / 1000;

        return user.mention(true) + " Here's your image!: " + link
                + "\n[" + width + "x" + height + " : " + (size < 1 ? 0 : "") + decFormat.format(size) + "MB]";
    }

    private URL convertArgs(final String[] args, final String level) {
        final String url = API_STR
                .replace("{key}", this.apiToken)
                .replace("{engine}", this.engineToken)
                .replace("{level}", level)
                .replace("{query}", String.join("%20", Arrays.asList(args)));
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            FurryBot.LOGGER.error("Failed to form URL: {}", url, e);
        }

        return null;
    }
}
