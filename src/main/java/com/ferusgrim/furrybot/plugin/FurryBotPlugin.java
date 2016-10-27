package com.ferusgrim.furrybot.plugin;

import com.ferusgrim.furrybot.FurryBot;

public class FurryBotPlugin {

    private final FurryBot bot;

    public FurryBotPlugin(final FurryBot bot) {
        this.bot = bot;
    }

    public FurryBot getBot() {
        return this.bot;
    }
}
