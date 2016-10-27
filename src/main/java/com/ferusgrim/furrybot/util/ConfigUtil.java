package com.ferusgrim.furrybot.util;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public final class ConfigUtil {


    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    public static ConfigFile ofResource(final Path file, final String resource, final boolean save) {
        if (!save) {
            final ConfigurationLoader<CommentedConfigurationNode> loader = fromResource(resource);
            return new ConfigFile(loader, load(loader), file);
        }

        if (!Files.exists(file)) {
            LOGGER.info("Configuration couldn't be found, and is now being created: {}", file.getFileName().toString());
            createLocalFile(file);
        }

        final ConfigurationLoader<CommentedConfigurationNode> loader = fromPath(file);
        return new ConfigFile(loader, load(loader, fromResource(resource)), file);
    }

    public static ConfigFile ofFile(final Path file, final boolean mustExist) {
        if (!Files.exists(file)) {
            Preconditions.checkArgument(!mustExist,
                    "File is required to exist, but doesn't!: {}", file.toAbsolutePath().toString());
            createLocalFile(file);
        }

        final ConfigurationLoader<CommentedConfigurationNode> loader = fromPath(file);
        return new ConfigFile(loader, load(loader), file);
    }

    private static ConfigurationLoader<CommentedConfigurationNode> fromPath(final Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    private static ConfigurationLoader<CommentedConfigurationNode> fromResource(final String resource) {
        return HoconConfigurationLoader.builder().setURL(ConfigUtil.class.getResource(resource)).build();
    }

    @SafeVarargs
    private static ConfigurationNode load(final ConfigurationLoader<CommentedConfigurationNode> loader,
                                          final ConfigurationLoader<CommentedConfigurationNode>... mergers) {
        ConfigurationNode root = null;
        try {
            root = loader.load();
            for (ConfigurationLoader<CommentedConfigurationNode> merge : mergers) {
                root.mergeValuesFrom(merge.load());
            }

            loader.save(root);
        } catch (final IOException e) {
            LOGGER.error("Failed to load and merge all configurations:");
            LOGGER.error("Main: ", loader);
            LOGGER.error("Merge: ", Arrays.toString(mergers));
            LOGGER.error("Error: ", e);
        }

        if (root == null) {
            LOGGER.warn("After merging (did we fail?), configuration is NULL. Returning blank node.");
            root = loader.createEmptyNode();
        }

        return root;
    }

    private static void createLocalFile(final Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        } catch (final IOException e) {
            LOGGER.error("Failed to create configuration file: {}", file.toAbsolutePath().toString(), e);
        }
    }

    private static ConfigFile createEmptyConfiguration() {
        final ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().build();
        return new ConfigFile(loader, load(loader), Paths.get("plzdontsaveme.conf"));
    }
}
