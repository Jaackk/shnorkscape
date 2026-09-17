package com.rs.utils.mysql;

public enum DatabaseDetails {
    GIM_LOCAL(DatabaseCredential.LOCAL, "ancientx"),
    GIM_LIVE(DatabaseCredential.LIVE, "ancientx"),
    LOGS_LIVE(DatabaseCredential.LIVE, "logs"),
    HISCORES_LIVE(DatabaseCredential.LIVE, "ancientx"),
    VOTE_LIVE(DatabaseCredential.LIVE, "ancientx"),
    MAIN_LIVE(DatabaseCredential.LIVE, "ancientx");

    public static final DatabaseDetails[] VALUES = values();
    private final DatabaseCredential auth;
    private final String database;

    DatabaseDetails(final DatabaseCredential auth, final String database) {
        this.auth = auth;
        this.database = database;
    }

    public DatabaseCredential getAuth() {
        return auth;
    }

    public String getDatabase() {
        return database;
    }
}
