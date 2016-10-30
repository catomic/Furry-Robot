package com.ferusgrim.furrybot.plugin.command;

import ninja.leaping.configurate.ConfigurationNode;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class HelpMe extends FurryCommand {

    public HelpMe(final CommandManager manager,
                  final IDiscordClient bot,
                  final ConfigurationNode rawConfig) {
        super(manager, bot, rawConfig);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays this help message!";
    }

    @Override
    public String getSyntax() {
        return "help [command]";
    }

    @Override
    public String execute(final IChannel channel, final IUser user, final String[] args) {
        StringBuilder builder = new StringBuilder();
        final String prefix = this.getManager().getRawConfig().getNode("prefix").getString();

        builder.append("```fix\n");

        if (args.length == 0) {
            builder = this.loopCommands(builder, prefix);
            return builder.append("\n```\n").toString();
        }

        final FurryCommand command = this.getManager().getCommand(args[0]);

        if (command == null) {
            builder.append("Command by name \"").append(args[0]).append("\" doesn't exist!\n\n");
            builder = this.loopCommands(builder, prefix);
            return builder.append("\n```\n").toString();
        }

        builder = this.appendCommand(command, true, builder, prefix);
        return builder.append("\n```\n").toString();
    }

    private StringBuilder loopCommands(StringBuilder builder, final String prefix) {
        for (final FurryCommand command : this.getManager().getCommands()) {
            builder = this.appendCommand(command, false, builder, prefix);
            builder.append("\n");
        }

        return builder;
    }

    private StringBuilder appendCommand(final FurryCommand command,
                                        final boolean specific,
                                        final StringBuilder builder,
                                        final String prefix) {
        final String nameSynMix = this.mix(command.getSyntax());

        builder.append(prefix).append(nameSynMix);

        if (specific) {
            builder.append("\n").append(command.getDescription());
        }

        return builder;
    }

    private String mix(final String syntax) {
        final String[] parts = syntax.split("\\s+");
        parts[0] = parts[0].toUpperCase();
        return String.join(" ", parts);
    }
}
