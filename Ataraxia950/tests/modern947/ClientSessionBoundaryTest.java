package modern947;

import com.rs.game.player.client.ClientSessionSelfTest;
import org.junit.Test;

public final class ClientSessionBoundaryTest {
    @Test
    public void legacyAndModernLoginsStaySeparated() {
        ClientSessionSelfTest.main(new String[0]);
    }
}
