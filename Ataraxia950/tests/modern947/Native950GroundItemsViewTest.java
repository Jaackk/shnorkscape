package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950GroundItemsViewTest {
    private final EmbeddedChannel ownerChannel = new EmbeddedChannel();
    private final Player owner = Player.createNative950("owner", new WorldTile(3217,3258,0), ownerChannel);
    private final List<Packet> packets = new ArrayList<Packet>();
    private final Native950GroundItemsView view = new Native950GroundItemsView(Thread.currentThread(), packets::add);
    @After public void close() { ownerChannel.finishAndReleaseAll(); }

    @Test public void privateDropsAreOnlyPublishedToTheOwner() {
        FloorItem privateDrop = item(995, 10, 3217, 3258, 0, true);
        publish("other", privateDrop);
        assertTrue(packets.isEmpty());
        publish("OWNER", privateDrop);
        assertOps(96,51);
        assertEquals(1, view.publishedPiles());
        assertTrue(Native950GroundItemsView.visibleTo(privateDrop,"owner"));
        assertFalse(Native950GroundItemsView.visibleTo(privateDrop,"other"));
    }

    @Test public void privateToPublicTransitionPublishesToLateViewersWithoutOwnerDuplicates() {
        FloorItem privateDrop = item(995, 10, 3217, 3258, 0, true);
        publish("other",privateDrop);
        privateDrop.setInvisible(false);
        publish("other",privateDrop);
        assertOps(96,51);
        packets.clear();
        publish("other",privateDrop);
        assertTrue(packets.isEmpty());
        assertTrue(Native950GroundItemsView.visibleTo(privateDrop,"anyone"));
    }

    @Test public void equalIdStacksAggregateWithoutMutatingRealAmountsOrOverflow() {
        FloorItem first=item(995,Integer.MAX_VALUE,3217,3258,0,false);
        FloorItem second=item(995,Integer.MAX_VALUE,3217,3258,0,false);
        publish("owner",first,second);
        assertOps(96,51);
        assertEquals(65535,addAmount(packets.get(1)));
        assertEquals(1,view.publishedPiles());
        assertEquals(Integer.MAX_VALUE,first.getAmount());
        assertEquals(Integer.MAX_VALUE,second.getAmount());
        packets.clear();
        first.setAmount(10); second.setAmount(20);
        publish("owner",first,second);
        assertOps(96,70);
        assertArrayEquals(new byte[]{0x12,0,3,(byte)0xe3,(byte)0xff,(byte)0xff,0,30},packets.get(1).payload());
    }

    @Test public void pickupAndExpiryUpdateExistingAggregateThenRemoveExactlyOnce() {
        FloorItem first=item(995,10,3217,3258,0,false);
        FloorItem second=item(995,20,3217,3258,0,false);
        publish("owner",first,second);
        assertEquals(30,addAmount(packets.get(1)));
        packets.clear();
        publish("owner",second);
        assertOps(96,70);
        packets.clear();
        publish("owner");
        assertOps(96,109);
        assertEquals(0,view.publishedPiles());
        packets.clear();
        publish("owner");
        assertTrue(packets.isEmpty());
    }

    @Test public void sceneClippingAndPlaneVisibilityPreventSuppressedNativeAddTracking() {
        publish("owner",item(995,1,3199,3258,0,false),item(995,1,3304,3258,0,false),
                item(995,1,3217,3304,0,false),item(995,1,3217,3258,1,false),item(995,1,3200,3200,0,false));
        assertOps(96,51);
        assertEquals(1,view.publishedPiles());
        assertArrayEquals(new byte[]{0,0,(byte)0x80},packets.get(0).payload());
    }

    @Test public void rebuildRemovesOldPilesBeforeNewSceneAndRepublishesOverlap() {
        FloorItem pile=item(995,10,3217,3258,0,false);
        publish("owner",pile);
        packets.clear();
        view.beforeRebuild();
        assertOps(96,109);
        assertArrayEquals(new byte[]{7,(byte)0xfe,(byte)0x80},packets.get(0).payload());
        packets.clear();
        // The session queues REBUILD_NORMAL between these calls.
        view.publish("owner",3216,3248,0,104,Arrays.asList(pile));
        assertOps(96,51);
        assertArrayEquals(new byte[]{1,0,(byte)0x80},packets.get(0).payload());
    }

    @Test public void farSceneChangeRemovesWithOldOriginAndNewPlaneUsesItsOwnPrefix() {
        publish("owner",item(995,10,3217,3258,0,false));
        packets.clear();
        view.beforeRebuild();
        assertArrayEquals(new byte[]{7,(byte)0xfe,(byte)0x80},packets.get(0).payload());
        packets.clear();
        view.publish("owner",8000,8000,2,104,Arrays.asList(item(995,10,8001,8002,2,false)));
        assertOps(96,51);
        assertArrayEquals(new byte[]{0,0,(byte)0x82},packets.get(0).payload());
    }

    @Test public void ownerlessPrivateZeroQuantityAndInvalidItemsAreNeverPublished() {
        FloorItem ownerless=new FloorItem(new Item(995,10),new WorldTile(3217,3258,0),null,false,true);
        publish("owner",ownerless,item(995,0,3217,3258,0,false),item(-1,10,3217,3258,0,false),
                item(0xffffff,10,3217,3258,0,false));
        assertTrue(packets.isEmpty());
    }

    @Test public void differentTilesAndIdsDoNotCollapseIntoOnePile() {
        publish("owner",item(995,10,3217,3258,0,false),item(526,1,3217,3258,0,false),item(995,20,3218,3258,0,false));
        assertOps(96,51,96,51,96,51);
        assertEquals(3,view.publishedPiles());
    }

    @Test public void pickupGateRequiresPublishedIdentityAndCurrentScene() throws Exception {
        java.lang.reflect.Field loaded=com.rs.game.Entity.class.getDeclaredField("lastLoadedMapRegionTile");
        loaded.setAccessible(true);
        loaded.set(owner,new WorldTile(3217,3258,0));
        assertEquals(0,owner.getMapSize()); // server interest width104; native wire scene remains256 with base3088,3128
        FloorItem pile=item(995,10,3217,3258,0,false);
        assertFalse(view.canTake(owner,995,3217,3258));
        view.publish("owner",3088,3128,0,256,Arrays.asList(pile));
        assertTrue(view.canTake(owner,995,3217,3258));
        assertFalse(view.canTake(owner,526,3217,3258));
        assertFalse(view.canTake(owner,995,3218,3258));
        loaded.set(owner,new WorldTile(4000,4000,0));
        assertFalse(view.canTake(owner,995,3217,3258));
        loaded.set(owner,new WorldTile(3217,3258,0));
        view.beforeRebuild();
        assertFalse(view.canTake(owner,995,3217,3258));
    }

    @Test public void legacyUnmarkedGroundItemsAreNotAdmittedToTheNativeProjection() {
        FloorItem legacy=new FloorItem(new Item(995,10),new WorldTile(3217,3258,0),false);
        assertFalse(legacy.isNative950());
        publish("owner",legacy);
        assertTrue(packets.isEmpty());
    }

    @Test public void wrongThreadCannotPublishOrClearTheView() throws Exception {
        try { CompletableFuture.runAsync(() -> publish("owner")).get(); fail("Expected owner-thread guard"); }
        catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        try { CompletableFuture.runAsync(view::beforeRebuild).get(); fail("Expected owner-thread guard"); }
        catch (ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertTrue(packets.isEmpty());
    }

    private FloorItem item(int id,int amount,int x,int y,int plane,boolean hidden) {
        return new FloorItem(new Item(id,amount),new WorldTile(x,y,plane),owner,false,hidden);
    }
    private void publish(String viewer,FloorItem... items) { view.publish(viewer,3200,3200,0,104,Arrays.asList(items)); }
    private int addAmount(Packet packet) { byte[] b=packet.payload();return ((b[4]&255)<<8)|(b[5]&255); }
    private void assertOps(int... expected) {
        int[] actual=new int[packets.size()];for(int i=0;i<actual.length;i++)actual[i]=packets.get(i).type().opcode();
        assertArrayEquals(expected,actual);
    }
}
