package com.rs.game.player.client;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real World/FloorItem timers, isolated from other test worlds' pending tasks. */
public final class Native950LootLifecycleTest {
    private EmbeddedChannel channel;
    private Player owner;
    private WorldTile tile;
    private Region region;
    private Queue<Object> active, ready;
    private List<Object> savedActive,savedReady;
    private List<FloorItem> savedItems;

    @SuppressWarnings("unchecked")
    private static Queue<Object> tasks(String name) throws Exception {
        Field field=WorldTasksManager.class.getDeclaredField(name);field.setAccessible(true);
        return (Queue<Object>)field.get(null);
    }
    @Before public void setup() throws Exception {
        active=tasks("activeTasks");ready=tasks("ready");
        savedActive=new ArrayList<Object>(active);savedReady=new ArrayList<Object>(ready);
        active.clear();ready.clear();
        channel=new EmbeddedChannel();tile=new WorldTile(14500,14500,0);
        owner=Player.createNative950("loot-lifecycle",tile,channel);
        region=World.getRegion(tile.getRegionId());savedItems=new ArrayList<FloorItem>(region.getGroundItemsSafe());
        region.getGroundItemsSafe().clear();
    }
    @After public void cleanup() {
        if(region!=null){region.getGroundItemsSafe().clear();region.getGroundItemsSafe().addAll(savedItems);}
        if(channel!=null)channel.finishAndReleaseAll();
        if(active!=null){active.clear();active.addAll(savedActive);}
        if(ready!=null){ready.clear();ready.addAll(savedReady);}
    }
    @Test public void nativeDropUsesExistingSixtySecondPrivateThenPublicLifetime() {
        FloorItem floor=add(60,60);assertTrue(floor.isNative950());assertTrue(floor.isInvisible());
        assertEquals(owner.getUsername(),floor.getOwner());assertTrue(stored(floor));
        drain(100);assertTrue(floor.isInvisible());assertTrue(stored(floor));
        drain(1);assertFalse(floor.isInvisible());assertTrue(stored(floor));
        drain(100);assertTrue(stored(floor));drain(1);assertFalse(stored(floor));
    }
    @Test public void pickingOneEqualPileCannotRemoveItsNeighbor() {
        FloorItem first=add(-1,-1),second=add(-1,-1);assertEquals(first,second);
        assertTrue(World.removeGroundItem(second));assertTrue(stored(first));assertFalse(stored(second));
        assertFalse(World.removeGroundItem(second));assertTrue(stored(first));
    }
    @Test public void staleHiddenTaskCannotPublishAnEqualReplacementPile() {
        FloorItem old=add(1,-1);assertTrue(World.removeGroundItem(old));
        FloorItem replacement=add(-1,-1);assertEquals(old,replacement);
        drain(2);assertTrue(replacement.isInvisible());assertTrue(stored(replacement));
        assertTrue(old.isInvisible());
    }
    @Test public void staleExpiryTaskCannotDeleteAnEqualReplacementPile() {
        FloorItem old=add(-1,-1);World.turnPublic(old,1);assertFalse(old.isInvisible());
        assertTrue(World.removeGroundItem(old));
        FloorItem replacement=add(-1,-1);World.turnPublic(replacement,-1);assertEquals(old,replacement);
        drain(2);assertTrue(stored(replacement));
    }
    @Test public void nativeIdentityMarkerSurvivesOfflineOwnerAndDoesNotTagLegacyDrops() {
        FloorItem nativeItem=add(-1,-1);channel.close();
        assertTrue(nativeItem.isNative950());assertTrue(World.removeGroundItem(nativeItem));
        FloorItem legacy=new FloorItem(new Item(995,1),tile,null,false,true);
        assertFalse(legacy.isNative950());
    }
    private FloorItem add(int privateSeconds,int publicSeconds) {
        return World.addGroundItem(new Item(995,10),tile,owner,true,privateSeconds,2,publicSeconds,false);
    }
    private boolean stored(FloorItem expected) {
        for(FloorItem item:region.getGroundItemsSafe())if(item==expected)return true;return false;
    }
    private static void drain(int count) {for(int i=0;i<count;i++)WorldTasksManager.processTasks();}
}
