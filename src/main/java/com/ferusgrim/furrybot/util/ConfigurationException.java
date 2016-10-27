package com.ferusgrim.furrybot.util;

public class ConfigurationException extends Throwable {

    public ConfigurationException() {
        super("Error while loading Configuration.");
    }

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final Throwable ex) {
        super(ex);
    }

    public ConfigurationException(final String message, final Throwable ex) {
        super(message, ex);
    }
}
