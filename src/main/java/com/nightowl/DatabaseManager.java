package com.nightowl;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:derby:nightowlDB;create=true";

    private static DatabaseManager instance;
    private DatabaseManager() { initializeDatabase(); }
    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void initializeDatabase() {
        String[] tables = {
            """
            CREATE TABLE WELLNESS_LOG (
                ID       INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                LOG_DATE DATE NOT NULL,
                MOOD     INTEGER NOT NULL,
                SLEEP    INTEGER NOT NULL,
                STRESS   INTEGER NOT NULL,
                STUDY    INTEGER NOT NULL,
                PRIMARY KEY (ID)
            )
            """,
            """
            CREATE TABLE USER_PROFILES (
                ID            INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                USERNAME      VARCHAR(100) NOT NULL UNIQUE,
                PASSWORD      VARCHAR(100) NOT NULL,
                SCHOOL        VARCHAR(200),
                MAJOR         VARCHAR(200),
                CLASS_YEAR    VARCHAR(50),
                RESOURCE_PREFS VARCHAR(500),
                IS_ADMIN      SMALLINT DEFAULT 0,
                PRIMARY KEY (ID)
            )
            """,
            """
            CREATE TABLE BOOKMARKS (
                ID             INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                USER_ID        INTEGER NOT NULL,
                RESOURCE_TITLE VARCHAR(200) NOT NULL,
                PRIMARY KEY (ID)
            )
            """,
            """
            CREATE TABLE RESOURCE_TIPS (
                ID            INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                SUBMITTED_AT  TIMESTAMP NOT NULL,
                USERNAME      VARCHAR(100),
                TIP_TYPE      VARCHAR(50) NOT NULL,
                RESOURCE_NAME VARCHAR(200),
                DESCRIPTION   VARCHAR(2000) NOT NULL,
                CONTACT_EMAIL VARCHAR(200),
                STATUS        VARCHAR(50) DEFAULT 'PENDING',
                PRIMARY KEY (ID)
            )
            """
        };

        try (var conn = getConnection(); var stmt = conn.createStatement()) {
            for (String ddl : tables) {
                try { stmt.execute(ddl); }
                catch (SQLException e) {
                    if (!e.getSQLState().equals("X0Y32"))
                        System.err.println("[DB] Setup: " + e.getMessage());
                }
            }
            // Add IS_ADMIN to existing DBs that don't have it
            try { stmt.execute("ALTER TABLE USER_PROFILES ADD COLUMN IS_ADMIN SMALLINT DEFAULT 0"); }
            catch (SQLException ignored) {}
        } catch (SQLException e) {
            System.err.println("[DB] Init error: " + e.getMessage());
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    public UserProfile login(String username, String password) {
        String sql = "SELECT * FROM USER_PROFILES WHERE USERNAME = ? AND PASSWORD = ?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            var rs = stmt.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { System.err.println("[DB] Login: " + e.getMessage()); }
        return null;
    }

    public boolean usernameExists(String username) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM USER_PROFILES WHERE USERNAME = ?")) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public UserProfile createUser(String username, String password) {
        String sql = "INSERT INTO USER_PROFILES (USERNAME, PASSWORD, SCHOOL, MAJOR, CLASS_YEAR, RESOURCE_PREFS, IS_ADMIN) VALUES (?, ?, '', '', '', '', 0)";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            var keys = stmt.getGeneratedKeys();
            if (keys.next()) return new UserProfile(keys.getInt(1), username, "", "", "", "", false);
        } catch (SQLException e) { System.err.println("[DB] CreateUser: " + e.getMessage()); }
        return null;
    }

    public void updateUserProfile(int userId, String school, String major, String classYear, String resourcePrefs) {
        String sql = "UPDATE USER_PROFILES SET SCHOOL=?, MAJOR=?, CLASS_YEAR=?, RESOURCE_PREFS=? WHERE ID=?";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, school); stmt.setString(2, major);
            stmt.setString(3, classYear); stmt.setString(4, resourcePrefs);
            stmt.setInt(5, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] UpdateProfile: " + e.getMessage()); }
    }

    public boolean isProfileComplete(UserProfile p) {
        return p.getSchool() != null && !p.getSchool().isEmpty()
            && p.getMajor()  != null && !p.getMajor().isEmpty()
            && p.getClassYear() != null && !p.getClassYear().isEmpty();
    }

    private UserProfile mapUser(ResultSet rs) throws SQLException {
        int isAdmin = 0;
        try { isAdmin = rs.getInt("IS_ADMIN"); } catch (SQLException ignored) {}
        return new UserProfile(
            rs.getInt("ID"), rs.getString("USERNAME"),
            rs.getString("SCHOOL"), rs.getString("MAJOR"),
            rs.getString("CLASS_YEAR"), rs.getString("RESOURCE_PREFS"),
            isAdmin == 1
        );
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    public List<UserProfile> getAllUsers() {
        var list = new ArrayList<UserProfile>();
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM USER_PROFILES ORDER BY ID")) {
            var rs = stmt.executeQuery();
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) { System.err.println("[DB] getAllUsers: " + e.getMessage()); }
        return list;
    }

    public void setAdmin(int userId, boolean admin) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("UPDATE USER_PROFILES SET IS_ADMIN = ? WHERE ID = ?")) {
            stmt.setInt(1, admin ? 1 : 0);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] setAdmin: " + e.getMessage()); }
    }

    public List<String[]> getAllTips() {
        var list = new ArrayList<String[]>();
        String sql = "SELECT ID, SUBMITTED_AT, USERNAME, TIP_TYPE, RESOURCE_NAME, DESCRIPTION, CONTACT_EMAIL, STATUS FROM RESOURCE_TIPS ORDER BY ID DESC";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("ID"),
                    String.valueOf(rs.getTimestamp("SUBMITTED_AT")),
                    rs.getString("USERNAME"),
                    rs.getString("TIP_TYPE"),
                    rs.getString("RESOURCE_NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("CONTACT_EMAIL"),
                    rs.getString("STATUS")
                });
            }
        } catch (SQLException e) { System.err.println("[DB] getAllTips: " + e.getMessage()); }
        return list;
    }

    public void updateTipStatus(int tipId, String status) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("UPDATE RESOURCE_TIPS SET STATUS = ? WHERE ID = ?")) {
            stmt.setString(1, status);
            stmt.setInt(2, tipId);
            stmt.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] updateTipStatus: " + e.getMessage()); }
    }

    // ── Bookmarks ─────────────────────────────────────────────────────────────

    public boolean isBookmarked(int userId, String title) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM BOOKMARKS WHERE USER_ID = ? AND RESOURCE_TITLE = ?")) {
            stmt.setInt(1, userId); stmt.setString(2, title);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean addBookmark(int userId, String title) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("INSERT INTO BOOKMARKS (USER_ID, RESOURCE_TITLE) VALUES (?, ?)")) {
            stmt.setInt(1, userId); stmt.setString(2, title);
            stmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public boolean removeBookmark(int userId, String title) {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("DELETE FROM BOOKMARKS WHERE USER_ID = ? AND RESOURCE_TITLE = ?")) {
            stmt.setInt(1, userId); stmt.setString(2, title);
            stmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public List<String> getBookmarks(int userId) {
        var list = new ArrayList<String>();
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT RESOURCE_TITLE FROM BOOKMARKS WHERE USER_ID = ? ORDER BY ID DESC")) {
            stmt.setInt(1, userId);
            var rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("RESOURCE_TITLE"));
        } catch (SQLException e) { System.err.println("[DB] getBookmarks: " + e.getMessage()); }
        return list;
    }

    // ── Resource Tips ─────────────────────────────────────────────────────────

    public boolean submitTip(String username, String tipType, String resourceName,
                              String description, String contactEmail) {
        String sql = "INSERT INTO RESOURCE_TIPS (SUBMITTED_AT, USERNAME, TIP_TYPE, RESOURCE_NAME, DESCRIPTION, CONTACT_EMAIL) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username); stmt.setString(2, tipType);
            stmt.setString(3, resourceName); stmt.setString(4, description);
            stmt.setString(5, contactEmail);
            stmt.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("[DB] submitTip: " + e.getMessage()); return false;
        }
    }

    // ── Wellness ──────────────────────────────────────────────────────────────

    public void saveWellnessEntry(int mood, int sleep, int stress, int study) {
        String sql = "INSERT INTO WELLNESS_LOG (LOG_DATE, MOOD, SLEEP, STRESS, STUDY) VALUES (?, ?, ?, ?, ?)";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, mood); stmt.setInt(3, sleep);
            stmt.setInt(4, stress); stmt.setInt(5, study);
            stmt.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] saveWellness: " + e.getMessage()); }
    }

    public List<WellnessEntry> getRecentEntries(int limit) {
        var list = new ArrayList<WellnessEntry>();
        String sql = "SELECT ID, LOG_DATE, MOOD, SLEEP, STRESS, STUDY FROM WELLNESS_LOG ORDER BY LOG_DATE DESC FETCH FIRST ? ROWS ONLY";
        try (var conn = getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            var rs = stmt.executeQuery();
            while (rs.next()) list.add(new WellnessEntry(
                rs.getInt("ID"), rs.getDate("LOG_DATE").toLocalDate(),
                rs.getInt("MOOD"), rs.getInt("SLEEP"),
                rs.getInt("STRESS"), rs.getInt("STUDY")
            ));
        } catch (SQLException e) { System.err.println("[DB] getEntries: " + e.getMessage()); }
        return list;
    }

    public boolean hasEntryForToday() {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM WELLNESS_LOG WHERE LOG_DATE = ?")) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    public void shutdown() {
        try { DriverManager.getConnection("jdbc:derby:;shutdown=true"); }
        catch (SQLException e) {
            if (!e.getSQLState().equals("XJ015"))
                System.err.println("[DB] Shutdown: " + e.getMessage());
        }
    }
}
