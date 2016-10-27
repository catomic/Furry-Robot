package com.ferusgrim.furrybot.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerSetup {

    public static String LOG_LOCATION;

    public static String setup() {
        if (LOG_LOCATION != null) {
            return LOG_LOCATION;
        }

        final String timestamp = new SimpleDateFormat("MM-dd-yyyyHH-mm-ss.SSS").format(new Date());
        LOG_LOCATION = Paths.get("", "logs", timestamp + ".log").toAbsolutePath().toString();

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setName("logfile");
        fileAppender.setFile("logs/" + timestamp + ".log");

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss} [%thread - %class{0}] %-5p %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        root.addAppender(fileAppender);

        return LOG_LOCATION;
    }
}
