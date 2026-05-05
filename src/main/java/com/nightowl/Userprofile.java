package com.nightowl;

public class UserProfile {
    private int id;
    private String username;
    private String school;
    private String major;
    private String classYear;
    private String resourcePrefs;

    public UserProfile(int id, String username, String school, String major, String classYear, String resourcePrefs) {
        this.id = id;
        this.username = username;
        this.school = school;
        this.major = major;
        this.classYear = classYear;
        this.resourcePrefs = resourcePrefs;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getSchool() { return school; }
    public String getMajor() { return major; }
    public String getClassYear() { return classYear; }
    public String getResourcePrefs() { return resourcePrefs; }
}
