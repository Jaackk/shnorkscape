package com.rs.game.player.client;

import java.util.Objects;

/**
 * Per-connection protocol identity. Player stores this transiently so a saved
 * character never selects the protocol of a future connection.
 */
public final class ClientSession {
    private final ClientProfile profile;

    private ClientSession(ClientProfile profile) {
        this.profile = profile;
    }

    /** Validate before modifying the player, lobby membership, or channel. */
    public static ClientSession beginLegacyGameplay(ClientProfile profile) {
        Objects.requireNonNull(profile, "profile").requireLegacyGameplay("Ataraxia gameplay login");
        return new ClientSession(profile);
    }

    /** Only the dedicated native world bootstrap may use this connection identity. */
    public static ClientSession beginNative950Gameplay() {
        return new ClientSession(ClientProfile.NATIVE_950);
    }

    public ClientProfile getProfile() {
        return profile;
    }
}
