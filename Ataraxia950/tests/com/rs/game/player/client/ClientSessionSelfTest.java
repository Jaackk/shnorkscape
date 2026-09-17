package com.rs.game.player.client;

/** Dependency-free regression check for the legacy/modern login boundary. */
public final class ClientSessionSelfTest {
    public static void main(String[] args) {
        String previous = System.getProperty(ClientProfile.REVISION_PROPERTY);
        try {
            System.clearProperty(ClientProfile.REVISION_PROPERTY);
            check(ClientProfile.configured() == ClientProfile.LEGACY_910, "Existing launches must remain 910");
            ClientSession legacy = ClientSession.beginLegacyGameplay(ClientProfile.configured());
            check(legacy.getProfile() == ClientProfile.LEGACY_910, "Session lost its selected revision");

            System.setProperty(ClientProfile.REVISION_PROPERTY, "947");
            check(ClientProfile.configured() == ClientProfile.NATIVE_947, "947 must be explicitly recognizable");
            expect(UnsupportedOperationException.class,
                    () -> ClientSession.beginLegacyGameplay(ClientProfile.configured()),
                    "947 must never silently enter the 910 gameplay path");
            check(legacy.getProfile() == ClientProfile.LEGACY_910, "Configuration changed an existing session");

            System.setProperty(ClientProfile.REVISION_PROPERTY, "949");
            expect(IllegalArgumentException.class, ClientProfile::configured,
                    "An unimplemented revision must never fall back to 910");
            expect(NullPointerException.class, () -> ClientSession.beginLegacyGameplay(null),
                    "A session must have an explicit profile");
            System.out.println("Client session boundary checks passed.");
        } finally {
            if (previous == null) {
                System.clearProperty(ClientProfile.REVISION_PROPERTY);
            } else {
                System.setProperty(ClientProfile.REVISION_PROPERTY, previous);
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Throwable> expected, Runnable operation, String message) {
        try {
            operation.run();
        } catch (Throwable failure) {
            if (expected.isInstance(failure)) {
                return;
            }
            throw new AssertionError(message, failure);
        }
        throw new AssertionError(message);
    }
}
