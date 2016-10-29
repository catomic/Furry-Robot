package com.ferusgrim.furrybot;

import com.ferusgrim.furrybot.plugin.command.CommandManager;
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

    static {
        LoggerSetup.setup();
        LOGGER = LoggerFactory.getLogger(FurryBot.class);
        PROGRAM_DIR = Paths.get("");
    }

    private final IDiscordClient client;

    private FurryBot() throws ConfigurationException, DiscordException {
        LOGGER.info("Logging to: {}", LoggerSetup.LOG_LOCATION);
        LOGGER.info("Launched in directory: {}", PROGRAM_DIR.toAbsolutePath().toString());

        final ConfigFile config = ConfigUtil.ofResource(PROGRAM_DIR.resolve("config.conf").toAbsolutePath(), "/config.conf", true);
        final String token = config.getNode("authentication", "token").getString("");
        if (token.isEmpty()) {
            LOGGER.error("Cannot ignore field \"authentication.token\"");
            this.client = null;
            return;
        }

        LOGGER.info("Launching bot with TOKEN: {}", token);
        this.client = this.getClient(token, true);

        final Greeter greeter = Greeter.configure(config.getNode("welcome"));
        if (greeter != null) {
            this.client.getDispatcher().registerListener(greeter);
        }

        final Leaver leaver = Leaver.configure(config.getNode("leaving"));
        if (leaver != null) {
            this.client.getDispatcher().registerListener(leaver);
        }

        final CommandManager commandManager = new CommandManager(this.client, config.getNode("commands"));
        commandManager.registerCommands();
        this.client.getDispatcher().registerListener(commandManager);
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
