package modern947;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950NpcViewport;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * NPC inherits WorldTile's coordinate-only equals/hashCode (Entity overrides hashCode but not
 * equals), so two distinct NPCs occupying one tile - a companion, a familiar, a target standing
 * on another actor - are "equal" to each other. {@link Native950NpcViewport#nearbyNpcs} must
 * publish both as separate candidates, never collapse the second into the first.
 */
public final class Native950NpcViewportIdentityTest {
    @Test public void distinctNpcsSharingATileAreBothReportedAsNearby() {
        WorldTile tile = new WorldTile(3217, 3257, 0);
        EmbeddedChannel channel = new EmbeddedChannel();
        Player viewer = Player.createNative950("viewport-identity", new WorldTile(tile), channel);
        NPC first = NPC.createNative950(494, new WorldTile(tile), 1);
        NPC second = NPC.createNative950(494, new WorldTile(tile), 1); // same definition, same tile
        try {
            assertTrue("test fixture must exercise the exact coordinate-only collision",
                    first.equals(second));
            assertNotSame(first, second);

            viewer.getMapRegionsIds().add(tile.getRegionId());
            World.addNative950Npc(first);
            World.updateEntityRegion(first);
            World.addNative950Npc(second);
            World.updateEntityRegion(second);

            List<NPC> nearby = Native950NpcViewport.nearbyNpcs(viewer);
            assertEquals("both distinct NPCs sharing a tile must be published", 2, nearby.size());
            assertTrue(containsIdentity(nearby, first));
            assertTrue(containsIdentity(nearby, second));
        } finally {
            World.removeNative950Npc(first);
            World.removeNative950Npc(second);
            channel.finishAndReleaseAll();
        }
    }

    private static boolean containsIdentity(List<NPC> list, NPC target) {
        for (NPC npc : list) if (npc == target) return true;
        return false;
    }
}
