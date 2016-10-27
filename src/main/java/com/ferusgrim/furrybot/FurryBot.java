package com.ferusgrim.furrybot;

import com.ferusgrim.furrybot.plugin.CommandRouter;
import com.ferusgrim.furrybot.plugin.Greeter;
import com.ferusgrim.furrybot.plugin.Leaver;
import com.ferusgrim.furrybot.util.ConfigFile;
import com.ferusgrim.furrybot.util.ConfigUtil;
import com.ferusgrim.furrybot.util.ConfigurationException;
import com.ferusgrim.furrybot.util.LoggerSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FurryBot {

    public static final Logger LOGGER;
    public static final Path PROGRAM_DIR;
    public static final ConfigFile CONFIG_FILE;

    static {
        LoggerSetup.setup();
        LOGGER = LoggerFactory.getLogger(FurryBot.class);
        PROGRAM_DIR = Paths.get("");
        CONFIG_FILE = ConfigUtil.ofResource(PROGRAM_DIR.resolve("config.conf").toAbsolutePath(), "/config.conf", true);
    }

    private final FurConf config;
    private final IDiscordClient client;

    private FurryBot() throws ConfigurationException, DiscordException {
        LOGGER.info("Logging to: {}", LoggerSetup.LOG_LOCATION);
        LOGGER.info("Launched in directory: {}", PROGRAM_DIR.toAbsolutePath().toString());

        this.config = FurConf.of(CONFIG_FILE.getNode());

        LOGGER.info("Launching bot with TOKEN: {}", this.config.getToken());
        this.client = this.getClient(this.config.getToken(), true);

        this.client.getDispatcher().registerListener(new Greeter(this));
        this.client.getDispatcher().registerListener(new Leaver(this));
        this.client.getDispatcher().registerListener(new CommandRouter(this));
    }

    public FurConf getConfig() {
        return this.config;
    }

    public IDiscordClient getClient() {
        return this.client;
    }

    private IDiscordClient getClient(final String token, boolean login) throws DiscordException {
        final ClientBuilder builder = new ClientBuilder();

        builder.withToken(token);

        return login ? builder.login() : builder.build();
    }

    public static void main(String[] args) throws ConfigurationException, DiscordException {
        new FurryBot();
    }
}
