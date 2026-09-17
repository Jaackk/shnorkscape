package com.rs.game.player.client;

import com.rs.game.WorldObject;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950ObjectsViewTest {
    private final List<Packet> packets = new ArrayList<Packet>();
    private final Native950ObjectsView view = new Native950ObjectsView(Thread.currentThread(), packets::add);
    private WorldObject object(int id) { return new WorldObject(id,10,0,3217,3258,0); }
    private Native950ObjectsView.Change change(WorldObject current, WorldObject original) {
        return new Native950ObjectsView.Change(current,original);
    }
    private void publish(Native950ObjectsView.Change... changes) { view.publish(3200,3200,0,104,Arrays.asList(changes)); }
    private void assertOps(int... opcodes) {
        assertEquals(opcodes.length,packets.size());
        for(int i=0;i<opcodes.length;i++) assertEquals(opcodes[i],packets.get(i).type().opcode());
    }
    private int addId(Packet packet) {
        byte[] b=packet.payload();
        return ((b[2]&255)<<24)|((b[1]&255)<<16)|((b[4]&255)<<8)|(b[3]&255);
    }
    @Test public void treeBecomesStumpAndTimerRestoresOriginalExactlyOnce() {
        WorldObject tree=object(38760),stump=object(40350);
        publish(change(stump,tree));
        assertOps(96,11); assertEquals(40350,addId(packets.get(1)));
        assertEquals(1,view.publishedObjects()); packets.clear();
        publish(change(stump,tree)); assertTrue(packets.isEmpty());
        publish(); assertOps(96,11); assertEquals(38760,addId(packets.get(1)));
        assertEquals(0,view.publishedObjects()); packets.clear();
        publish(); assertTrue(packets.isEmpty());
    }
    @Test public void fireCreationAndExpiryUseAddThenDelete() {
        publish(change(object(70755),null)); assertOps(96,11);
        packets.clear(); publish(); assertOps(96,26);
    }
    @Test public void removedMapObjectAndReturnUseDeleteThenOriginalAdd() {
        publish(change(null,object(38760))); assertOps(96,26);
        packets.clear(); publish(); assertOps(96,11); assertEquals(38760,addId(packets.get(1)));
    }
    @Test public void lateViewerReceivesCurrentStumpAndRemovedTree() {
        WorldObject original=object(38760);
        WorldObject other=new WorldObject(38761,10,0,3218,3258,0);
        publish(change(object(40350),original),change(null,other));
        assertOps(96,11,96,26); assertEquals(2,view.publishedObjects());
    }
    @Test public void changeToAnotherDefinitionInSameSlotDoesNotSendDeleteFirst() {
        WorldObject original=object(38760);
        publish(change(object(40350),original)); packets.clear();
        publish(change(object(40351),original)); assertOps(96,11); assertEquals(40351,addId(packets.get(1)));
    }
    @Test public void replacementAfterDeletionAndDeletionAfterReplacementShareOneSlot() {
        WorldObject original=object(38760);
        publish(change(null,original)); packets.clear();
        publish(change(object(40350),original)); assertOps(96,11); packets.clear();
        publish(change(null,original)); assertOps(96,26);
    }
    @Test public void rebuildRestoresPreviousOriginBeforeReapplyingNewOrigin() {
        Native950ObjectsView.Change stump=change(object(40350),object(38760));
        publish(stump); packets.clear(); view.beforeRebuild();
        assertOps(96,11); assertEquals(38760,addId(packets.get(1)));
        assertArrayEquals(new byte[]{7,(byte)0xfe,(byte)0x80},packets.get(0).payload());
        packets.clear(); view.publish(3216,3248,0,104,Arrays.asList(stump));
        assertOps(96,11); assertEquals(40350,addId(packets.get(1)));
        assertArrayEquals(new byte[]{1,0,(byte)0x80},packets.get(0).payload());
    }
    @Test public void viewCopiesObjectIdentityBeforeMutableSourceChanges() {
        WorldObject source=object(40350),original=object(38760);
        Native950ObjectsView.Change first=change(source,original);
        source.setId(40351); original.setId(1); publish(first);
        assertEquals(40350,addId(packets.get(1))); packets.clear(); publish();
        assertEquals(38760,addId(packets.get(1)));
    }
    @Test public void mapTransformSurvivesDepletionCopiesAndRestoration() {
        WorldObject original=object(38760);
        byte[] transform={16,0,(byte)160}; original.setNative950MapTransform(transform);
        transform[2]=0;
        WorldObject stump=new WorldObject(40350,10,0,original);
        assertArrayEquals(new byte[]{16,0,(byte)160},stump.getNative950MapTransform());
        publish(change(stump,original));
        assertEquals(9,packets.get(1).payload().length);
        assertEquals(0x28,packets.get(1).payload()[5]&255);
        packets.clear(); publish();
        assertEquals(38760,addId(packets.get(1)));
        assertArrayEquals(new byte[]{16,0,(byte)160},Arrays.copyOfRange(packets.get(1).payload(),6,9));
    }
    @Test public void independentSlotsOnSameTileDoNotReplaceEachOther() {
        publish(change(object(70755),null),change(new WorldObject(123,0,1,3217,3258,0),null));
        assertOps(96,11,96,11); assertEquals(2,view.publishedObjects());
    }
    @Test public void outOfSceneAndOtherPlaneAreNotPublished() {
        publish(change(new WorldObject(1,10,0,3199,3258,0),null),
                change(new WorldObject(1,10,0,3304,3258,0),null),
                change(new WorldObject(1,10,0,3217,3258,1),null));
        assertTrue(packets.isEmpty());
    }
    @Test public void sameAsMapBaselineDoesNotCreateAnOverride() {
        publish(change(object(38760),object(38760))); assertTrue(packets.isEmpty());
    }
    @Test public void refusesCrossSlotMutationAndInvalidShape() {
        try { change(new WorldObject(1,0,0,3217,3258,0),object(1));fail(); } catch(IllegalArgumentException expected) { }
        try { change(new WorldObject(1,23,0,3217,3258,0),null);fail(); } catch(IllegalArgumentException expected) { }
    }
    @Test public void enforcesWorldOwnerThread() throws Exception {
        try { CompletableFuture.runAsync(()->view.beforeRebuild()).get(); fail(); }
        catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
    }
}
