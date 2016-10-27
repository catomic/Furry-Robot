package com.ferusgrim.furrybot.util;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConfigFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFile.class);

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final ConfigurationNode root;
    private final Path path;

    public ConfigFile(final ConfigurationLoader<CommentedConfigurationNode> loader,
                      final ConfigurationNode root, final Path path) {
        this.loader = loader;
        this.root = root;
        this.path = path;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
        return this.loader;
    }

    public ConfigurationNode getNode(final Object... path) {
        return path.length == 0 ? this.root : this.root.getNode(path);
    }

    public Path getPath() {
        return this.path;
    }
}
