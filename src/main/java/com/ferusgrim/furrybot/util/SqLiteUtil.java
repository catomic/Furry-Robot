package com.ferusgrim.furrybot.util;

import com.ferusgrim.furrybot.FurryBot;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public final class SqLiteUtil {

    public static String execute(final Path dbFile, final String exec, final String... additionalExec) {
        Connection conn = null;
        Statement state = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath().toString());
            state = conn.createStatement();

            state.execute(exec);

            for (final String add : additionalExec) {
                state.execute(add);
            }
        } catch (final SQLException e) {
            FurryBot.LOGGER.error("Failed to execute commands: {} {}", exec, Arrays.asList(additionalExec));
            return "Failed to execute command because of a database failure.";
        } finally {
            close(conn, state, null);
        }

        return "";
    }

    public static void query(final Path dbFile, final String query, final ResultSetAction action) {
        Connection conn = null;
        Statement state = null;
        ResultSet result = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath().toString());
            state = conn.createStatement();
            result = state.executeQuery(query);

            action.execute(result);
        } catch (final SQLException e) {
            FurryBot.LOGGER.error("Failed to execute command: {}", query, e);
        } finally {
            close(conn, state, result);
        }
    }

    private static void close(final Connection conn, final Statement state, final ResultSet result) {
        if (result != null) try {
            result.close();
        } catch (final SQLException e) {
            FurryBot.LOGGER.error("Failed to close ResultSet.", e);
        }

        if (state != null) try {
            state.close();
        } catch (final SQLException e) {
            FurryBot.LOGGER.error("Failed to close Statement.", e);
        }

        if (conn != null) try {
            conn.close();
        } catch (final SQLException e) {
            FurryBot.LOGGER.error("Failed to close Connection.", e);
        }
    }

    public interface ResultSetAction {
        void execute(final ResultSet results) throws SQLException;
    }
}
