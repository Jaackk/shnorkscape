package modern947;

import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950NpcViewport;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real entity/registry/tick-wheel state; independent950 retained bytes and addition offsets. */
public final class Native950NpcForceVisibilityTest {
    @Test public void activeForceDefersNewNpcButKeepsRetainedMasksAndRemovals() throws Exception {
        withFixture(f->{
            NPC existing=f.npc(3201,3200,0), newcomer=f.npc(3203,3200,0);
            AtomicBoolean speak=new AtomicBoolean(false);
            Native950NpcViewport view=new Native950NpcViewport(24,250,
                    (viewer,npc,added)->speak.get() && npc==existing ? new Native950NpcMasks.Update().say("Hi") : null);
            view.frame(f.player,7,false,Arrays.asList(existing));
            f.begin();speak.set(true);
            // Existing actor still receives a real mask; newcomer is withheld on original force-mask tick.
            assertArrayEquals(hex("01 9f ff e0 00 00 40 48 69 00"),
                    view.frame(f.player,7,false,Arrays.asList(existing,newcomer)).payload());
            assertArrayEquals(new int[]{existing.getIndex()},view.snapshot().indices);
            f.player.resetMasks();assertTrue(f.player.isNative950ForceMovementActive());speak.set(false);
            assertArrayEquals(hex("01 00"),view.frame(f.player,7,false,Arrays.asList(existing,newcomer)).payload());
            World.removeNative950Npc(existing);
            assertArrayEquals(hex("01 e0"),view.frame(f.player,7,false,Arrays.asList(newcomer)).payload());
            assertEquals(0,view.snapshot().indices.length);
            view.close();
        });
    }

    @Test public void terminalArrivalReleasesNewNpcUsingAuthoritativeFinalOffsets() throws Exception {
        withFixture(f->{
            NPC newcomer=f.npc(3204,3200,0);
            Native950NpcViewport view=f.view();f.begin();
            assertArrayEquals(hex("00"),view.frame(f.player,7,false,Arrays.asList(newcomer)).payload());
            f.player.resetMasks();f.wheel.tick();
            assertTrue(f.player.isNative950ForceMovementActive());
            assertArrayEquals(hex("00"),view.frame(f.player,7,false,Arrays.asList(newcomer)).payload());
            f.wheel.tick();assertFalse(f.player.isNative950ForceMovementActive());
            f.commitQueuedTile();
            byte[] body=view.frame(f.player,7,false,Arrays.asList(newcomer)).payload();
            assertArrayEquals(new int[]{newcomer.getIndex()},view.snapshot().indices);
            // retained8/index16/dy7/immediate1/facing3/type16/plane2/mask1/dx7.
            assertEquals(0,bits(body,0,8));assertEquals(newcomer.getIndex(),bits(body,8,16));
            assertEquals(0,bits(body,24,7));assertEquals(2,bits(body,54,7));
            assertEquals(3202,f.player.getX());view.close();
        });
    }

    @Test public void rebuildDuringForceDefersReaddsAndPlaneTeleportCancellationReleasesThem() throws Exception {
        withFixture(f->{
            NPC oldPlane=f.npc(3201,3200,0), newPlane=f.npc(3201,3200,1);
            Native950NpcViewport view=f.view();view.frame(f.player,7,false,Arrays.asList(oldPlane));
            f.begin();
            assertArrayEquals(hex("00"),view.frame(f.player,7,true,Arrays.asList(oldPlane,newPlane)).payload());
            assertEquals(0,view.snapshot().indices.length);
            f.player.setNextWorldTile(new WorldTile(3200,3200,1));
            assertFalse(f.player.isNative950ForceMovementActive());f.commitQueuedTile();
            view.frame(f.player,7,true,Arrays.asList(oldPlane,newPlane));
            assertArrayEquals(new int[]{newPlane.getIndex()},view.snapshot().indices);
            f.wheel.tick();f.wheel.tick();assertEquals(1,f.player.getPlane());
            assertArrayEquals(hex("01 00"),view.frame(f.player,7,false,Arrays.asList(oldPlane,newPlane)).payload());
            view.close();
        });
    }

    @Test public void reusedIndexIsRemovedThenDeferredUntilExplicitForceClear() throws Exception {
        withFixture(f->{
            NPC old=f.npc(3201,3200,0);int index=old.getIndex();
            Native950NpcViewport view=f.view();view.frame(f.player,7,false,Arrays.asList(old));f.begin();
            World.removeNative950Npc(old);NPC replacement=f.npc(3201,3200,0);assertEquals(index,replacement.getIndex());
            assertArrayEquals(hex("01 e0"),view.frame(f.player,7,false,Arrays.asList(replacement)).payload());
            assertArrayEquals(hex("00"),view.frame(f.player,7,false,Arrays.asList(replacement)).payload());
            f.player.setNextForceMovement(null);assertFalse(f.player.isNative950ForceMovementActive());
            // Current cancellation deliberately queues a normal reposition at the authoritative
            // tile before its stationary mask. Commit that request before checking old callbacks.
            assertNotNull(f.player.getNextWorldTile());
            assertEquals(3200,f.player.getNextWorldTile().getX());
            assertEquals(3200,f.player.getNextWorldTile().getY());
            f.commitQueuedTile();
            view.frame(f.player,7,false,Arrays.asList(replacement));
            assertArrayEquals(new int[]{index},view.snapshot().indices);
            f.wheel.tick();f.wheel.tick();assertNull(f.player.getNextWorldTile());view.close();
        });
    }

    private interface Exercise { void run(Fixture fixture); }
    private static void withFixture(Exercise exercise) throws Exception {
        Field scheduler=CoresManager.class.getDeclaredField("native947Scheduler");scheduler.setAccessible(true);
        Object prior=scheduler.get(null);Fixture fixture=new Fixture();
        try {assertTrue(World.getNPCs().isEmpty());scheduler.set(null,fixture.wheel);exercise.run(fixture);}
        finally {
            fixture.player.setNextForceMovement(null);
            for(NPC npc:fixture.npcs)World.removeNative950Npc(npc);
            scheduler.set(null,prior);fixture.channel.finishAndReleaseAll();
        }
    }
    private static final class Fixture {
        final EmbeddedChannel channel=new EmbeddedChannel();
        final Player player=Player.createNative950("npc-force",new WorldTile(3200,3200,0),channel);
        final Native950TickScheduler wheel=new Native950TickScheduler();
        final List<NPC> npcs=new ArrayList<NPC>();
        Fixture(){player.setActive(true);player.setRunning(true);}
        NPC npc(int x,int y,int plane){NPC npc=NPC.createNative950(494,new WorldTile(x,y,plane),1);npcs.add(npc);World.addNative950Npc(npc);return npc;}
        void begin(){player.setNextForceMovement(new ForceMovement(new WorldTile(3202,3200,0),2,ForceMovement.EAST));}
        void commitQueuedTile(){WorldTile tile=player.getNextWorldTile();assertNotNull(tile);player.setLocation(tile);player.setNextWorldTile(null);}
        Native950NpcViewport view(){return new Native950NpcViewport(24,250,Native950NpcViewport.NO_MASKS);}
    }
    private static int bits(byte[] data,int start,int length){int result=0;for(int i=0;i<length;i++){int p=start+i;result=result<<1|((data[p>>>3]>>>(7-(p&7)))&1);}return result;}
    private static byte[] hex(String value){String s=value.replace(" ","");byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return b;}
}
