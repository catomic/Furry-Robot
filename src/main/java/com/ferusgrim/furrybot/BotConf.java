package com.ferusgrim.furrybot;

import com.ferusgrim.furrybot.util.ConfigurationException;
import ninja.leaping.configurate.ConfigurationNode;

public class BotConf {
        private final String token;

        private BotConf(final String token) {
            this.token = token;
        }

        public String getToken() {
            return this.token;
        }

        public static BotConf of(final ConfigurationNode node) throws ConfigurationException {
            final String token = node.getNode("token").getString("");

            if (token.isEmpty()) {
                throw new ConfigurationException("Authentication Token can NOT be blank!");
            }

            return new BotConf(token);
        }
}
