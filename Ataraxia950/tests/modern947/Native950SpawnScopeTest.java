package modern947;
import com.rs.game.player.client.Native950SpawnScope;
import org.junit.Test;
import static org.junit.Assert.*;
public class Native950SpawnScopeTest {
    @Test public void lumbridgeScopeDoesNotPopulateAdjacentRegions() {
        Native950SpawnScope scope = Native950SpawnScope.parse("12850");
        assertTrue(scope.allows((3217 >> 6) << 8 | (3257 >> 6)));
        assertTrue(scope.allows((3209 >> 6) << 8 | (3215 >> 6)));
        assertFalse(scope.allows((3294 >> 6) << 8 | (3182 >> 6)));
        assertFalse(scope.allows(12849)); assertFalse(scope.allows(13106));
    }
    @Test public void explicitRegionListAcceptsBoundariesAndDuplicates() {
        Native950SpawnScope scope = Native950SpawnScope.parse(" 0,12850,12850,65535 ");
        assertTrue(scope.allows(0)); assertTrue(scope.allows(65535));
        assertFalse(scope.allows(1)); assertFalse(scope.allows(-1)); assertFalse(scope.allows(65536));
    }
    @Test public void missingScopePreservesExplicitLegacyAllRegionMode() {
        assertTrue(Native950SpawnScope.parse("").allows(13105));
        assertFalse(Native950SpawnScope.parse(null).allows(-1));
    }
    @Test public void malformedScopeCannotSilentlyEnableEveryRegion() {
        for(String text : new String[]{"12850,",",12850","all","*","-1","65536","12850,,12849","12.0"}) {
            try { Native950SpawnScope.parse(text); fail(text); } catch(IllegalArgumentException expected) { }
        }
    }
}
