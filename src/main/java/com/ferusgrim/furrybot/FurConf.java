package com.ferusgrim.furrybot;

import com.ferusgrim.furrybot.util.ConfigurationException;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.List;

public class FurConf {

    private final String token;
    private final List<String> sorters;
    private final String rulesChannel;
    private final String greetChannel;
    private final List<String> greetMessage;
    private final List<String> leaveMessage;
    private final String commandPrefix;
    private final List<String> imageWhitelist;

    private FurConf(final String token,
                    final List<String> sorters,
                    final String rulesChannel,
                    final String greetChannel,
                    final List<String> greetMessage,
                    final List<String> leaveMessage,
                    final String commandPrefix,
                    final List<String> imageWhitelist) {
        this.token = token;
        this.sorters = sorters;
        this.rulesChannel = rulesChannel;
        this.greetChannel = greetChannel;
        this.greetMessage = greetMessage;
        this.leaveMessage = leaveMessage;
        this.commandPrefix = commandPrefix;
        this.imageWhitelist = imageWhitelist;
    }

    public String getToken() {
        return this.token;
    }

    public List<String> getSorters() {
        return this.sorters;
    }

    public String getRulesChannel() {
        return this.rulesChannel;
    }

    public String getGreetChannel() {
        return this.greetChannel;
    }

    public List<String> getGreetMessage() {
        return this.greetMessage;
    }

    public List<String> getLeaveMessage() {
        return this.leaveMessage;
    }

    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    public List<String> getImageWhitelist() {
        return this.imageWhitelist;
    }

    public static FurConf of(final ConfigurationNode node) throws ConfigurationException {
        final String token = node.getNode("authentication", "token").getString("");
        final List<String> sorters = getList(node, "sorters");
        final String rulesChannel = node.getNode("rules-channel").getString("");
        final String greetChannel = node.getNode("greet-channel").getString("");
        final List<String> greetMessage = getList(node, "greet-message");
        final List<String> leaveMessage = getList(node, "leave-message");
        final String commandPrefix = node.getNode("command-prefix").getString("");
        final List<String> imageWhitelist = getList(node, "image-channel-whitelist");

        if (token.isEmpty()) {
            throw new ConfigurationException("Token was blank!");
        }

        if (sorters.isEmpty()) {
            throw new ConfigurationException("No sorters specified!");
        }

        if (rulesChannel.isEmpty()) {
            throw new ConfigurationException("No rules channel specified!");
        }

        if (greetChannel.isEmpty()) {
            throw new ConfigurationException("Greet channel wasn't specified!");
        }

        if (greetMessage.isEmpty()) {
            throw new ConfigurationException("No greet message!");
        }

        if (leaveMessage.isEmpty()) {
            throw new ConfigurationException("No leave message!");
        }

        if (commandPrefix.isEmpty()) {
            throw new ConfigurationException("No command prefix specified!");
        }

        return new FurConf(token, sorters, rulesChannel, greetChannel, greetMessage, leaveMessage, commandPrefix, imageWhitelist);
    }

    private static List<String> getList(final ConfigurationNode node, final String... path) {
        final List<String> list = Lists.newArrayList();

        list.addAll(node.getNode(path).getList(o -> o instanceof String ? (String) o : "", Lists.newArrayList()));

        list.removeAll(Collections.singletonList(""));

        return list;
    }
}
