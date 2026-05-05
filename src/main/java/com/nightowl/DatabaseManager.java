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
        String createWellness = """
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

        String createUsers = """
                CREATE TABLE USER_PROFILES (
                    ID          INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                    USERNAME    VARCHAR(100) NOT NULL UNIQUE,
                    PASSWORD    VARCHAR(100) NOT NULL,
                    SCHOOL      VARCHAR(200),
                    MAJOR       VARCHAR(200),
                    CLASS_YEAR  VARCHAR(50),
                    RESOURCE_PREFS VARCHAR(500),
                    PRIMARY KEY (ID)
                )
                """;

        try (var conn = getConnection(); var stmt = conn.createStatement()) {
            try { stmt.execute(createWellness); System.out.println("[DB] WELLNESS_LOG created."); }
            catch (SQLException e) { if (!e.getSQLState().equals("X0Y32")) System.err.println("[DB] " + e.getMessage()); }

            try { stmt.execute(createUsers); System.out.println("[DB] USER_PROFILES created."); }
            catch (SQLException e) { if (!e.getSQLState().equals("X0Y32")) System.err.println("[DB] " + e.getMessage()); }
        } catch (SQLException e) {
            System.err.printf("[DB] Setup error: %s%n", e.getMessage());
        }
    }

    // ── User Auth ─────────────────────────────────────────────────────────────

    public UserProfile login(String username, String password) {
        String sql = "SELECT * FROM USER_PROFILES WHERE USERNAME = ? AND PASSWORD = ?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserProfile(
                    rs.getInt("ID"),
                    rs.getString("USERNAME"),
                    rs.getString("SCHOOL"),
                    rs.getString("MAJOR"),
                    rs.getString("CLASS_YEAR"),
                    rs.getString("RESOURCE_PREFS")
                );
            }
        } catch (SQLException e) {
            System.err.println("[DB] Login error: " + e.getMessage());
        }
        return null;
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM USER_PROFILES WHERE USERNAME = ?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public UserProfile createUser(String username, String password) {
        String sql = "INSERT INTO USER_PROFILES (USERNAME, PASSWORD, SCHOOL, MAJOR, CLASS_YEAR, RESOURCE_PREFS) VALUES (?, ?, '', '', '', '')";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            var keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return new UserProfile(keys.getInt(1), username, "", "", "", "");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Create user error: " + e.getMessage());
        }
        return null;
    }

    public void updateUserProfile(int userId, String school, String major, String classYear, String resourcePrefs) {
        String sql = "UPDATE USER_PROFILES SET SCHOOL=?, MAJOR=?, CLASS_YEAR=?, RESOURCE_PREFS=? WHERE ID=?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, school);
            stmt.setString(2, major);
            stmt.setString(3, classYear);
            stmt.setString(4, resourcePrefs);
            stmt.setInt(5, userId);
            stmt.executeUpdate();
            System.out.println("[DB] Profile updated for user " + userId);
        } catch (SQLException e) {
            System.err.println("[DB] Update error: " + e.getMessage());
        }
    }

    public boolean isProfileComplete(UserProfile profile) {
        return profile.getSchool() != null && !profile.getSchool().isEmpty()
            && profile.getMajor() != null && !profile.getMajor().isEmpty()
            && profile.getClassYear() != null && !profile.getClassYear().isEmpty();
    }

    // ── Wellness ──────────────────────────────────────────────────────────────

    public void saveWellnessEntry(int mood, int sleep, int stress, int study) {
        var sql = """
                INSERT INTO WELLNESS_LOG (LOG_DATE, MOOD, SLEEP, STRESS, STUDY)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
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

    public List<WellnessEntry> getRecentEntries(int limit) {
        var sql = """
                SELECT ID, LOG_DATE, MOOD, SLEEP, STRESS, STUDY
                FROM WELLNESS_LOG
                ORDER BY LOG_DATE DESC
                FETCH FIRST ? ROWS ONLY
                """;
        var entries = new ArrayList<WellnessEntry>();
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
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

    public boolean hasEntryForToday() {
        var sql = "SELECT COUNT(*) FROM WELLNESS_LOG WHERE LOG_DATE = ?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    public void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("XJ015")) {
                System.err.printf("[DB] Shutdown error: %s%n", e.getMessage());
            }
        }
    }
}
