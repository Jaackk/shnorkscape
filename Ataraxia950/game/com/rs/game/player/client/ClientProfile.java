package com.rs.game.player.client;

/**
 * A pinned client revision, independent of account data and the legacy global
 * ProtocolSet constants. Recognition does not imply gameplay compatibility.
 */
public enum ClientProfile {
    LEGACY_910(910, 1),
    NATIVE_947(947, 3),
    /** The update field is a pure identity tag; 950's update number is not known and unused. */
    NATIVE_950(950, 0);

    public static final String REVISION_PROPERTY = "ataraxia.client.revision";

    private final int revision;
    private final int update;

    ClientProfile(int revision, int update) {
        this.revision = revision;
        this.update = update;
    }

    public int getRevision() {
        return revision;
    }

    public int getUpdate() {
        return update;
    }

    /** Defaults to the existing server protocol unless explicitly selected. */
    public static ClientProfile configured() {
        return parse(System.getProperty(REVISION_PROPERTY, "910"));
    }

    public static ClientProfile parse(String value) {
        if ("910".equals(value)) {
            return LEGACY_910;
        }
        if ("947".equals(value)) {
            return NATIVE_947;
        }
        if ("950".equals(value)) {
            return NATIVE_950;
        }
        throw new IllegalArgumentException("Unsupported " + REVISION_PROPERTY
                + " value '" + value + "'; select 910, 947 or 950 explicitly.");
    }

    /** True for any modern native client, i.e. anything that is not the legacy 910 protocol. */
    public boolean isNativeModern() {
        return this != LEGACY_910;
    }

    /**
     * The native wire implementations are validated independently. Never let one
     * fall through to 910 gameplay packet serializers or interface scripts.
     */
    public void requireLegacyGameplay(String operation) {
        if (this != LEGACY_910) {
            throw new UnsupportedOperationException("Client " + revision + "-" + update
                    + " cannot enter " + operation + ": the modern gameplay adapter is not installed. "
                    + "Use the dedicated native protocol harness; 910 packets must not be sent to this client.");
        }
    }
}
