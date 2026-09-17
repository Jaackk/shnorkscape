package com.rs.utils.mysql;

public enum DatabaseTopology {

    LOCAL(DatabaseCredential.LOCAL),
    MAIN(DatabaseCredential.LIVE),
    ;

    private final DatabaseCredential[] nodes;

    DatabaseTopology(DatabaseCredential... nodes) {
        this.nodes = nodes;
    }

    public DatabaseCredential[] getNodes() {
        return nodes;
    }
}
