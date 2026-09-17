package com.rs.utils.mysql;

public enum DatabaseCredential {
    LOCAL("ataraxia.database.local"),
    LIVE("ataraxia.database.live"),
    ;

    private final String host;
    private final String user;
    private final String pass;

    /** No database endpoints or credentials are shipped in the portable package. */
    DatabaseCredential(final String propertyPrefix) {
        this.host = System.getProperty(propertyPrefix + ".host", "");
        this.user = System.getProperty(propertyPrefix + ".user", "");
        this.pass = System.getProperty(propertyPrefix + ".password", "");
    }

    public boolean isConfigured() {
        return !host.isEmpty() && !user.isEmpty() && !pass.isEmpty();
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }
}
