package modern947;
import com.rs.game.player.client.ClientProfile;
import com.rs.game.player.client.Native950DevelopmentCommands;
import java.net.InetSocketAddress;
import org.junit.Test;
import static org.junit.Assert.*;
public class Native950DevelopmentCommandsTest {
    @Test public void bugTestControlsArePassiveDiagnostics() {
        assertTrue(Native950DevelopmentCommands.preservesGameplay(";;bug animation missing"));
        assertTrue(Native950DevelopmentCommands.preservesGameplay("::bugtest"));
        assertFalse(Native950DevelopmentCommands.preservesGameplay(";;dummy"));
        assertFalse(Native950DevelopmentCommands.preservesGameplay("hello"));
    }
    @Test public void onlyExplicitCommandNamespaceIsIntercepted() {
        assertTrue(Native950DevelopmentCommands.isCommand("::nxt"));
        assertTrue(Native950DevelopmentCommands.isCommand(";;NXT effects"));
        assertTrue(Native950DevelopmentCommands.isCommand("::tele 3217 3258"));
        for(String value:new String[]{null,"nxt","hello ::nxt","::nxtother","::guidemount 715 712"})
            assertFalse(Native950DevelopmentCommands.isCommand(value));
    }
    @Test public void requiresOptInExactRevisionAndLoopback() {
        InetSocketAddress local=new InetSocketAddress("127.0.0.1",1234);
        assertTrue(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_950,local));
        assertTrue(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_950,new InetSocketAddress("::1",1234)));
        assertFalse(Native950DevelopmentCommands.allowed(false,ClientProfile.NATIVE_950,local));
        assertFalse(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_947,local));
        assertFalse(Native950DevelopmentCommands.allowed(true,ClientProfile.LEGACY_910,local));
        assertFalse(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_950,new InetSocketAddress("192.0.2.1",1234)));
        assertFalse(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_950,InetSocketAddress.createUnresolved("localhost",1234)));
        assertFalse(Native950DevelopmentCommands.allowed(true,ClientProfile.NATIVE_950,null));
    }
}
