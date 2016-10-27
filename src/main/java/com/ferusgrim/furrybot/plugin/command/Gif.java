package com.ferusgrim.furrybot.plugin.command;

import com.ferusgrim.furrybot.FurryBot;
import com.ferusgrim.furrybot.util.DiscordUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Gif extends FurryCommand {

    public static final Path GIF_STORAGE = Paths.get("", "gif_store");
    public static final String API_STR = "https://www.googleapis.com/customsearch/v1?key=AIzaSyBGPk2fW6KbcUnRiZSNb9FVTLkitixQkBQ&cx=004052943258175816510:inol9mv-x3m&safe=off&alt=json&searchType=image&q=";
    public static final String ERROR_MESSAGE = "Couldn't get your image, sorry! <3";

    static {
        if (Files.exists(GIF_STORAGE)) {
            try {
                Files.createDirectories(GIF_STORAGE);
            } catch (final IOException e) {
                FurryBot.LOGGER.error("Failed to create gif store!", e);
            }
        }
    }

    public Gif(final IUser user, final IChannel channel, final String[] args) {
        super(user, channel, args);
    }

    @Override
    public void execute() {
        final URL url = this.convertArgs(this.getArgs());

        if (url == null) {
            DiscordUtil.sendMessage(this.getChannel(), ERROR_MESSAGE);
            return;
        }

        final URLConnection conn;
        try {
            conn = url.openConnection();
        } catch (final IOException e) {
            FurryBot.LOGGER.error("Failed to connect to URL: {}", url, e);
            DiscordUtil.sendMessage(this.getChannel(), ERROR_MESSAGE);
            return;
        }

        String line;
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (final IOException e) {
            FurryBot.LOGGER.error("Failed to connect to URL: {}", url, e);
            DiscordUtil.sendMessage(this.getChannel(), ERROR_MESSAGE);
            return;
        }

        final JsonArray items = new JsonParser().parse(builder.toString()).getAsJsonObject().getAsJsonArray("items");

        JsonObject json = null;
        for (final JsonElement item : items) {
            final String link = item.getAsJsonObject().getAsJsonPrimitive("link").getAsString();
            if (link.endsWith(".gif") || link.endsWith(".gifv")) {
                json = item.getAsJsonObject();
                break;
            }
        }

        if (json == null) {
            DiscordUtil.sendMessage(this.getChannel(), "Couldn't find any GIFs!");
            return;
        }

        final String link = json.getAsJsonPrimitive("link").getAsString();
        final int height = json.getAsJsonObject("image").getAsJsonPrimitive("height").getAsInt();
        final int width = json.getAsJsonObject("image").getAsJsonPrimitive("width").getAsInt();

        final DecimalFormat decFormat = new DecimalFormat("#.00");
        final double size = json.getAsJsonObject("image").getAsJsonPrimitive("byteSize").getAsDouble() / 1000 / 1000;

        DiscordUtil.sendMessage(this.getChannel(), this.getUser().mention(true) + " Here's your image!: " + link
                + "\n[" + width + "x" + height + " : " + (size < 1 ? 0 : "") + decFormat.format(size) + "MB]");
    }

    public URL convertArgs(final String[] args) {
        final String url = API_STR + "gif%20" + String.join("%20", Arrays.asList(args));
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            FurryBot.LOGGER.error("Failed to form URL: {}", url, e);
        }

        return null;
    }
}
