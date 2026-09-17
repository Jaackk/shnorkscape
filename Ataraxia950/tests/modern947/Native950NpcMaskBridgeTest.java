package modern947;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.client.Native950EntityMasks;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises engine state -> 950 masks, using fixtures independently derived from the client. */
public final class Native950NpcMaskBridgeTest {
    private static NPC npc() { return NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1); }

    @Test public void nativeNpcAnimationReachesTheWireInsteadOfBeingCountedAsRefused() {
        NPC npc = npc();
        // Negative first slot avoids loading cache definitions in this engine-only test.
        npc.setNextAnimation(new Animation(-1, 1, 32767, 855, 17));
        long refused = Native950EntityMasks.refusals();
        assertArrayEquals(hex("00 00 08 7f ff 00 01 80 00 7f ff 03 57 11"),
                Native950NpcMasks.maskBlock(Native950EntityMasks.npcMasks(npc, false)));
        assertEquals(refused, Native950EntityMasks.refusals());
    }

    @Test public void actualFaceTargetAndClearAreForwarded() {
        NPC npc = npc(), target = npc();
        target.setIndex(0x1234);
        npc.setNextFaceEntity(target);
        assertArrayEquals(hex("00 00 02 12 01 34"),
                Native950NpcMasks.maskBlock(Native950EntityMasks.npcMasks(npc, false)));
        npc.setNextFaceEntity(null);
        assertArrayEquals(hex("00 00 02 ff ff ff"),
                Native950NpcMasks.maskBlock(Native950EntityMasks.npcMasks(npc, false)));
    }

    @Test public void unverifiedGraphicsAreRefusedButSlotClearsNeedNoDefinition() {
        NPC npc = npc();
        npc.setNextGraphics(new Graphics(0x1234, 0x3456, 0x1234, 5));
        npc.setNextGraphics(new Graphics(-1));
        long refused = Native950EntityMasks.refusals();
        assertArrayEquals(hex("00 00 10 20 80 01 00 81"
                + " 01 ff 7f 00 00 00 00 80 fb 1f ff"),
                Native950NpcMasks.maskBlock(Native950EntityMasks.npcMasks(npc, false)));
        assertEquals(refused + 1, Native950EntityMasks.refusals());
    }

    @Test public void delayedNpcMeleeNeedsExplicitClockAndCacheAdmission() {
        NPC npc=npc();
        npc.getNextHits().add(new Hit(null,2000,Hit.HitLook.MELEE_DAMAGE,1));
        long before=Native950EntityMasks.refusals();
        assertNull(Native950EntityMasks.npcMasks(npc,false));
        assertEquals(before+1,Native950EntityMasks.refusals());
    }

    @Test public void invalidDamageAndUnknownGraphicsFlagsAreCountedWithoutCorruptingFrames() {
        NPC npc = npc();
        npc.getNextHits().add(new Hit(2560, Hit.HitLook.MELEE_DAMAGE));
        npc.setNextGraphics(new Graphics(1, 0, 0, 0, true));
        long before = Native950EntityMasks.refusals();
        assertNull(Native950EntityMasks.npcMasks(npc, false));
        assertEquals(before + 2, Native950EntityMasks.refusals());
    }

    private static byte[] hex(String text) {
        String s = text.replace(" ", ""); byte[] bytes = new byte[s.length()/2];
        for (int i=0; i<bytes.length; i++) bytes[i]=(byte)Integer.parseInt(s.substring(2*i,2*i+2),16);
        return bytes;
    }
}
