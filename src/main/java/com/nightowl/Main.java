package com.nightowl;

public class Main {
    public static void main(String[] args) {
        MainApp.main(args);
    }
    @Override
public void stop() {
    DatabaseManager.getInstance().shutdown();
}
}
