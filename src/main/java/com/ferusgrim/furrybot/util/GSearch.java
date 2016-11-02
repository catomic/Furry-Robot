package com.ferusgrim.furrybot.util;

import com.ferusgrim.furrybot.FurryBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public final class GSearch {

    private static final String API_STR = "https://www.googleapis.com/customsearch/v1?key={key}&cx={engine}&safe={level}&alt=json&searchType=image&q={query}";
    private static final String LEVEL_SFW = "high";
    private static final String LEVEL_NSFW = "off";

    public static String getImage(final String apiToken, final String engineToken, final String[] args, final List<String> imageTypes, boolean nsfw, final String userMention) {

        final URL url = convertArgs(args, nsfw ? LEVEL_NSFW : LEVEL_SFW, apiToken, engineToken);
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
            if (imageTypes.isEmpty()) {
                if (link.endsWith(".gif")
                        || link.endsWith(".gifv")
                        || link.endsWith(".jpg")
                        || link.endsWith(".jpeg")
                        || link.endsWith(".jif")
                        || link.endsWith(".jfif")
                        || link.endsWith(".jp2")
                        || link.endsWith(".jpx")
                        || link.endsWith(".j2k")
                        || link.endsWith(".j2c")
                        || link.endsWith(".tif")
                        || link.endsWith(".tiff")
                        || link.endsWith(".png")) {
                    json = item.getAsJsonObject();
                    break;
                }
            } else {
                for (final String match : imageTypes) {
                    if (link.endsWith(match)) {
                        json = item.getAsJsonObject();
                        break;
                    }
                }
            }
        }

        if (json == null) {
            return "Couldn't find any IMAGES!";
        }

        final String link = json.getAsJsonPrimitive("link").getAsString();
        final int height = json.getAsJsonObject("image").getAsJsonPrimitive("height").getAsInt();
        final int width = json.getAsJsonObject("image").getAsJsonPrimitive("width").getAsInt();

        final DecimalFormat decFormat = new DecimalFormat("#.00");
        final double size = json.getAsJsonObject("image").getAsJsonPrimitive("byteSize").getAsDouble() / 1000 / 1000;

        return userMention + " Here's your image!: " + link
                + "\n[" + width + "x" + height + " : " + (size < 1 ? 0 : "") + decFormat.format(size) + "MB]";
    }

    private static URL convertArgs(final String[] args, final String level, final String apiToken, final String engineToken) {
        final String url = API_STR
                .replace("{key}", apiToken)
                .replace("{engine}", engineToken)
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
