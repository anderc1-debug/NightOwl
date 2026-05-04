package com.nightowl;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:derby:nightowlDB;create=true";

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ── Connection ────────────────────────────────────────────────────────────

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void initializeDatabase() {
        var createTable = """
                CREATE TABLE WELLNESS_LOG (
                    ID      INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                    LOG_DATE DATE NOT NULL,
                    MOOD    INTEGER NOT NULL,
                    SLEEP   INTEGER NOT NULL,
                    STRESS  INTEGER NOT NULL,
                    STUDY   INTEGER NOT NULL,
                    PRIMARY KEY (ID)
                )
                """;

        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(createTable);
            System.out.println("[DB] WELLNESS_LOG table created.");
        } catch (SQLException e) {
            // X0Y32 means table already exists in Derby — that's fine
            if (!e.getSQLState().equals("X0Y32")) {
                System.err.printf("[DB] Setup error: %s%n", e.getMessage());
            }
        }
    }

    // ── Insert ────────────────────────────────────────────────────────────────

    public void saveWellnessEntry(int mood, int sleep, int stress, int study) {
        var sql = """
                INSERT INTO WELLNESS_LOG (LOG_DATE, MOOD, SLEEP, STRESS, STUDY)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, mood);
            stmt.setInt(3, sleep);
            stmt.setInt(4, stress);
            stmt.setInt(5, study);
            stmt.executeUpdate();

            System.out.println("[DB] Wellness entry saved.");

        } catch (SQLException e) {
            System.err.printf("[DB] Insert error: %s%n", e.getMessage());
        }
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    public List<WellnessEntry> getRecentEntries(int limit) {
        var sql = """
                SELECT ID, LOG_DATE, MOOD, SLEEP, STRESS, STUDY
                FROM WELLNESS_LOG
                ORDER BY LOG_DATE DESC
                FETCH FIRST ? ROWS ONLY
                """;

        var entries = new ArrayList<WellnessEntry>();

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(new WellnessEntry(
                        rs.getInt("ID"),
                        rs.getDate("LOG_DATE").toLocalDate(),
                        rs.getInt("MOOD"),
                        rs.getInt("SLEEP"),
                        rs.getInt("STRESS"),
                        rs.getInt("STUDY")
                ));
            }

        } catch (SQLException e) {
            System.err.printf("[DB] Query error: %s%n", e.getMessage());
        }

        return entries;
    }

    // ── Check for today's entry ───────────────────────────────────────────────

    public boolean hasEntryForToday() {
        var sql = "SELECT COUNT(*) FROM WELLNESS_LOG WHERE LOG_DATE = ?";

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.printf("[DB] Check error: %s%n", e.getMessage());
            return false;
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    public void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Derby always throws XJ015 on clean shutdown — that's expected
            if (!e.getSQLState().equals("XJ015")) {
                System.err.printf("[DB] Shutdown error: %s%n", e.getMessage());
            }
        }
    }
}
