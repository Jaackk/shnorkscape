package modern947;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.hitbar.HitBar;
import com.rs.game.hitbar.impl.EntityHitBar;
import com.rs.game.hitbar.impl.HitBarTimer;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950Hitbars;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Engine HP ratios and viewer-independent admission, with literal native 950 wire fixtures. */
public final class Native950HitbarsTest {
    @Test public void halfHealthUses255ScaleAndTheDistinctPlayerNpcTransforms() {
        Native950Hitbars.Result r = bars(50, 100, 1);
        assertEquals(0, r.refusals());
        // floor(50*255/100)=127; player sends -127, NPC sends 127+128.
        assertArrayEquals(hex("40 80 ff 00 00 00 81 00"), player(r));
        assertArrayEquals(hex("00 00 20 00 01 00 00 00 ff 00"), npc(r));
    }

    @Test public void zeroFullAndOverMaximumHealthKeepExactEndpoints() {
        assertArrayEquals(hex("40 80 ff 00 00 00 00 00"), player(bars(0, 100, 1)));
        assertArrayEquals(hex("40 80 ff 00 00 00 01 00"), player(bars(100, 100, 1)));
        assertArrayEquals(player(bars(100, 100, 1)), player(bars(150, 100, 1)));
        assertArrayEquals(player(bars(0, 100, 1)), player(bars(0, 0, 1)));
    }

    @Test public void largeNpcHealthKeepsAFullBarWithoutIntegerOverflow() {
        assertEquals(255, new EntityHitBar(new Owner(20_000_000, 20_000_000, 3)).getPercentage());
        assertEquals(255, new EntityHitBar(new Owner(Integer.MAX_VALUE, Integer.MAX_VALUE, 3)).getPercentage());
        Native950Hitbars.Result large = bars(20_000_000, 20_000_000, 3);
        assertEquals(0, large.refusals());
        assertArrayEquals(npc(bars(100, 100, 3)), npc(large));
    }

    @Test public void largeNpcPartialHealthKeepsItsRatioAndNativeWireValue() {
        assertEquals(127, new EntityHitBar(new Owner(10_000_000, 20_000_000, 1)).getPercentage());
        assertEquals(254, new EntityHitBar(new Owner(Integer.MAX_VALUE - 1, Integer.MAX_VALUE, 1)).getPercentage());
        Native950Hitbars.Result half = bars(10_000_000, 20_000_000, 1);
        assertEquals(0, half.refusals());
        assertArrayEquals(hex("00 00 20 00 01 00 00 00 ff 00"), npc(half));
        assertArrayEquals(hex("40 80 ff 00 00 00 81 00"), player(half));
    }

    @Test public void entitySizeSelectsOnlyTheThreeProvenBarDefinitions() {
        assertArrayEquals(hex("40 80 ff 00 00 00 01 00"), player(bars(100, 100, 2)));
        assertArrayEquals(hex("40 80 ff 04 00 00 01 00"), player(bars(100, 100, 3)));
        assertArrayEquals(hex("40 80 ff 04 00 00 01 00"), player(bars(100, 100, 4)));
        assertArrayEquals(hex("40 80 ff 03 00 00 01 00"), player(bars(100, 100, 5)));
    }

    @Test public void subclassVisibilityAndOtherBarFamiliesRemainRefused() {
        Owner owner = new Owner(50, 100, 1);
        EntityHitBar viewerDependent = new EntityHitBar(owner) {
            @Override public boolean display(Player viewer) { throw new AssertionError("must not inspect per-viewer subclasses"); }
        };
        Native950Hitbars.Result r = Native950Hitbars.from(Arrays.asList(
                new EntityHitBar(owner), viewerDependent, new HitBarTimer(30), null));
        assertEquals(3, r.refusals());
        assertEquals(1, r.playerBars().size());
        assertEquals(1, r.npcBars().size());
        assertArrayEquals(player(bars(50, 100, 1)), player(r));
    }

    @Test public void snapshotDoesNotClearTheQueueOrChangeWithLaterHealth() {
        Owner owner = new Owner(50, 100, 1);
        List<HitBar> queued = new ArrayList<>(); queued.add(new EntityHitBar(owner));
        Native950Hitbars.Result r = Native950Hitbars.from(queued);
        owner.hp = 1;
        assertEquals(1, queued.size());
        assertArrayEquals(hex("40 80 ff 00 00 00 81 00"), player(r));
        try { r.playerBars().clear(); fail("snapshot must be immutable"); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test public void invalidLegacyRatiosAreRefusedAndExcessEntriesDoNotOverflowTheCount() {
        assertEquals(1, bars(-10, 100, 1).refusals());
        EntityHitBar bar = new EntityHitBar(new Owner(50, 100, 1));
        Native950Hitbars.Result r = Native950Hitbars.from(Collections.nCopies(256, bar));
        assertEquals(255, r.playerBars().size()); assertEquals(255, r.npcBars().size());
        assertEquals(1, r.refusals());
    }

    @Test public void runtimeWithoutAnInitializedCacheRefusesEvenAnOrdinaryHpBar() {
        Store previous = Cache.STORE;
        try {
            Cache.STORE = null;
            Owner owner = new Owner(50, 100, 1);
            owner.getNextHitBars().add(new EntityHitBar(owner));
            Native950Hitbars.Result result = Native950Hitbars.fromRunningCache(owner);
            assertEquals(1, result.refusals());
            assertTrue(result.playerBars().isEmpty()); assertTrue(result.npcBars().isEmpty());
            assertEquals(1, owner.getNextHitBars().size());
        } finally { Cache.STORE = previous; }
    }

    private static Native950Hitbars.Result bars(int hp, int max, int size) {
        return Native950Hitbars.from(Collections.singletonList(new EntityHitBar(new Owner(hp, max, size))));
    }
    private static byte[] player(Native950Hitbars.Result r) {
        return Native950PlayerMasks.encode(Native950PlayerMasks.builder()
                .hits(Collections.<Native950PlayerMasks.Hit>emptyList(), r.playerBars()).build());
    }
    private static byte[] npc(Native950Hitbars.Result r) {
        return Native950NpcMasks.maskBlock(new Native950NpcMasks.Update().hits(
                new Native950NpcMasks.Hit[0], r.npcBars().toArray(new Native950NpcMasks.Hitbar[0])));
    }
    private static byte[] hex(String text) {
        String s=text.replace(" ", ""); byte[] out=new byte[s.length()/2];
        for(int i=0;i<out.length;i++) out[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);
        return out;
    }
    private static final class Owner extends Entity {
        int hp; final int max, size;
        Owner(int hp, int max, int size) { super(new WorldTile(3200,3200,0)); this.hp=hp;this.max=max;this.size=size;nextHitBars=new ArrayList<>(); }
        @Override public int getHitpoints(){return hp;}
        @Override public int getMaxHitpoints(){return max;}
        @Override public int getSize(){return size;}
        @Override public void finish(){ }
        @Override public double getMagePrayerMultiplier(){return 1;}
        @Override public double getMeleePrayerMultiplier(){return 1;}
        @Override public double getRangePrayerMultiplier(){return 1;}
        @Override public void handleIngoingHit(Hit hit){ }
        @Override public boolean canMove(int dir){return true;}
        @Override public void sendDeath(Entity source){ }
        @Override public String getName(){return "HP fixture";}
        @Override public int getCombatLevel(){return 1;}
    }
}

