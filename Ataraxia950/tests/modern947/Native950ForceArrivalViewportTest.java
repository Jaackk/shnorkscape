package modern947;

import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Actor;
import com.rs.network.protocol.modern950.Native950PlayerInfo.ViewState;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/** Per-viewer force delivery owns waypoint XY; plane and normal moves still need their deltas. */
public final class Native950ForceArrivalViewportTest {
    private static final long GENERATION = 41;
    private static final int X = 3207, Y = 3214;

    @Test public void deliveredTerminalEndpointIsNotAppliedTwiceAndNextWalkStillMovesOnce() {
        ViewState view = initial(1, plain(1, X, Y));
        // Native movement queues final XY at WALK speed without snapping the rendered origin.
        // The force then uses final XY as its base: first Y=-2, final Y=0.
        assertArrayEquals(hex("f2 00 20 7f f4 00 00 01 80 fe 00 80 00 80 00 00 00 3c 00 00"),
                frame(view, forced(1, X, Y, GENERATION)));
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK,view.speedToken(1));
        assertEquals(GENERATION, view.emittedForceGeneration(1));
        assertArrayEquals(hex("00 7f f4"), frame(view, arrival(1, X, Y, X, Y+2, GENERATION)));
        assertEquals(GENERATION, view.emittedForceGeneration(1));
        Actor nextWalk = Actor.builder(1, X, Y+3, 0).previous(X, Y+2, 0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_WALK).build();
        assertArrayEquals(passes("1 0 01 110 0","0 11 "+binary(2045,11)),frame(view,nextWalk));
    }

    @Test public void unseenAndDifferentGenerationsKeepTheOrdinaryArrivalDelta() {
        ViewState unseen = initial(1, plain(1, X, Y));
        assertArrayEquals(terminalDelta(0, 2), frame(unseen, arrival(1, X, Y, X, Y+2, GENERATION)));
        ViewState different = initial(1, plain(1, X, Y));
        frame(different, forced(1, X, Y, GENERATION));
        assertArrayEquals("A different generation still computes its delta from the already-known native base",
                terminalDelta(0, 0), frame(different, arrival(1, X, Y, X, Y+2, GENERATION+1)));
    }

    @Test public void aViewerJoiningAfterTheMaskStillReceivesTheEndpointMovement() {
        ViewState original = initial(1, plain(1, X, Y));
        frame(original, forced(1, X, Y, GENERATION));
        Actor observer = plain(2, X-1, Y);
        ViewState joining = initial(2, plain(1, X, Y), observer);
        frame(joining, plain(1, X, Y), observer); // add actor1 after its force mask tick
        assertTrue(joining.isLocal(1));
        assertEquals(0, joining.emittedForceGeneration(1));
        Actor arrived = arrival(1, X, Y, X, Y+2, GENERATION);
        assertEquals("The original viewer receives no second movement", 0, bit(frame(original, arrived),0,1));
        byte[] late = frame(joining, arrived, observer);
        // Actor1 is first in the retained list: needs update, no mask, relative teleport.
        assertEquals(0b10110, bit(late,0,5));
        assertEquals(0x4002, bit(late,5,15));
    }

    @Test public void metadataWithoutAnEmittedForceBlockCannotSuppressAnArrival() {
        ViewState view = initial(1, plain(1, X, Y));
        Actor refusedMask = Actor.builder(1, X, Y, 0).forceMovement(GENERATION)
                .masks(() -> Native950PlayerMasks.builder().forceTalk("other mask",0)).build();
        frame(view, refusedMask);
        assertEquals(0, view.emittedForceGeneration(1));
        assertArrayEquals(terminalDelta(0,2), frame(view, arrival(1,X,Y,X,Y+2,GENERATION)));
    }

    @Test public void intermediateWaypointUsesTheNativeFinalBaseAndKeepsTheSameGeneration() {
        ViewState view = initial(1, plain(1, X, Y));
        frame(view, forced(1, X, Y, GENERATION));
        assertArrayEquals(hex("00 7f f4"), frame(view, arrival(1,X,Y,X,Y+1,GENERATION)));
        assertEquals(Y+2,view.logicalY(1));
        assertEquals(GENERATION, view.emittedForceGeneration(1));
        assertArrayEquals(hex("00 7f f4"), frame(view, arrival(1,X,Y+1,X,Y+2,GENERATION)));
        assertEquals(GENERATION, view.emittedForceGeneration(1));
    }

    @Test public void stationaryCancellationInvalidatesTheOldClientPromise() {
        ViewState view = initial(1, plain(1,X,Y));
        frame(view, forced(1,X,Y,GENERATION));
        Actor cancelled = Actor.builder(1,X,Y,0).previous(X,Y,0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT)
                .masks(() -> Native950PlayerMasks.builder().forceMovement(
                Native950PlayerMasks.ForceMovement.of(0,0,0,0,0,0,0,1,0))).build();
        byte[] correction=frame(view,cancelled);
        assertEquals(0b11110,bit(correction,0,5)); // movement with pending cancellation mask
        assertEquals("Cancel from logical endpoint, not stale server origin",0x401e,bit(correction,5,15));
        assertEquals(Y,view.logicalY(1));
        assertEquals(0,view.emittedForceGeneration(1));
        assertArrayEquals(terminalDelta(0,2),frame(view,arrival(1,X,Y,X,Y+2,GENERATION)));
    }

    @Test public void terminalArrivalFoldedIntoSkipRunUpdatesRegionAndDoesNotHideTheNextWalk() {
        Actor observer=plain(1,X,3262);
        ViewState view=initial(1,observer,plain(2,X,3263));
        frame(view,observer,plain(2,X,3263)); // establish a rendered remote actor first
        frame(view,observer,forced(2,X,3263,GENERATION));
        assertEquals(GENERATION,view.emittedForceGeneration(2));
        frame(view,observer,plain(2,X,3263)); // both actors join the same skip pass
        // Both local actors can share one no-update run: 0 + selector01 + skip1.
        assertArrayEquals(hex("21 7f f0"),frame(view,observer,arrival(2,X,3263,X,3265,GENERATION)));
        assertEquals(Native950PlayerInfo.regionHash(X,3265,0),view.regionHash(2));
        assertEquals(GENERATION,view.emittedForceGeneration(2));
        Actor walk=Actor.builder(2,X,3266,0).previous(X,3265,0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_WALK).build();
        byte[] after=frame(view,observer,walk);
        assertEquals("Observer skips only itself",0,bit(after,0,3));
        assertEquals("Actor2 receives ordinary compact walk",0b1001,bit(after,3,4));
        assertEquals("North exactly one tile",6,bit(after,7,3));
    }

    @Test public void matchedForceWaypointStillUpdatesTheActorPlane() {
        ViewState view=initial(1,plain(1,X,Y));
        frame(view,forced(1,X,Y,GENERATION));
        Actor climbed=Actor.builder(1,X,Y+2,1).identity(101).previous(X,Y,0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT).forceMovementArrival(GENERATION).build();
        assertArrayEquals(passes("1 0 11 0 "+binary(0x4400,15),"0 11 "+binary(2045,11)),frame(view,climbed));
        assertEquals(Native950PlayerInfo.regionHash(X,Y+2,1),view.regionHash(1));
    }

    @Test public void nextForceMaskUsesTheOldLogicalBaseBeforePublishingItsNewEndpoint() {
        ViewState view=initial(1,plain(1,X,Y));
        frame(view,forced(1,X,Y,GENERATION));
        Actor replacement=Actor.builder(1,X,Y+2,0).identity(101).previous(X,Y,0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT).forceMovementArrival(GENERATION)
                .forceMovement(GENERATION+1,X,Y+4).masks(() -> Native950PlayerMasks.builder().forceMovement(
                        Native950PlayerMasks.ForceMovement.of(0,0,0,2,0,0,0,60,0))).build();
        byte[] body=frame(view,replacement);
        assertEquals(0b11110,bit(body,0,5));
        assertEquals("Queue B's final XY from A's known endpoint, at WALK speed",0x2002,bit(body,5,15));
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK,view.speedToken(1));
        assertEquals(GENERATION+1,view.emittedForceGeneration(1));
        assertEquals(Y+4,view.logicalY(1));
        assertArrayEquals(hex("00 7f f4"),frame(view,arrival(1,X,Y+2,X,Y+4,GENERATION+1)));
    }

    @Test public void removalForgetReinitializationAndNewOccupancyDiscardOldForceState() {
        Actor observer=plain(1,X-1,Y), start=forced(2,X,Y,GENERATION);
        for(int mode=0;mode<4;mode++) {
            ViewState view=initial(1,observer,plain(2,X,Y));
            frame(view,observer,plain(2,X,Y)); // add before the original force frame
            frame(view,observer,start);
            assertEquals(GENERATION,view.emittedForceGeneration(2));
            if(mode==0) frame(view,observer); // actual local removal
            else if(mode==1) view.forget(2);
            else if(mode==2) Native950PlayerInfo.initialScene(view,world(observer,plain(2,X,Y)),7,0,0,0);
            else frame(view,observer,Actor.builder(2,X,Y,0).identity(999).build());
            assertEquals("lifecycle mode "+mode,0,view.emittedForceGeneration(2));
        }
    }

    @Test public void newlyVisibleActorDefersOnlyItsOriginalForceMaskFrame() {
        Actor observer=plain(1,X-1,Y);
        ViewState view=initial(1,observer,plain(2,X,Y));
        assertArrayEquals(hex("00 7f f4"),frame(view,observer,forced(2,X,Y,GENERATION)));
        assertFalse(view.isLocal(2));
        assertEquals(0,view.emittedForceGeneration(2));
        frame(view,observer,plain(2,X,Y));
        assertTrue(view.isLocal(2));
        assertEquals(0,view.emittedForceGeneration(2));
        byte[] arrival=frame(view,observer,arrival(2,X,Y,X,Y+2,GENERATION));
        assertEquals("Viewer skips only itself",0,bit(arrival,0,3));
        assertEquals("A viewer without the force receives the ordinary endpoint move",0b10110,bit(arrival,3,5));
        assertEquals(0x4002,bit(arrival,8,15));
    }

    @Test public void negativeForceGenerationsAreRejected() {
        try {Actor.builder(1,X,Y,0).forceMovement(-1);fail();}catch(IllegalArgumentException expected) { }
        try {Actor.builder(1,X,Y,0).forceMovementArrival(-1);fail();}catch(IllegalArgumentException expected) { }
    }

    private static Actor plain(int index,int x,int y) {
        return Actor.builder(index,x,y,0).identity(index+100).build();
    }
    private static Actor forced(int index,int x,int y,long generation) {
        return Actor.builder(index,x,y,0).identity(index+100).forceMovement(generation,x,y+2)
                .masks(() -> Native950PlayerMasks.builder().forceMovement(
                        Native950PlayerMasks.ForceMovement.of(0,0,0,2,0,0,0,60,0))).build();
    }
    private static Actor arrival(int index,int fromX,int fromY,int x,int y,long generation) {
        return Actor.builder(index,x,y,0).identity(index+100).previous(fromX,fromY,0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT).forceMovementArrival(generation).build();
    }
    private static ViewState initial(int viewer,Actor... actors) {
        ViewState state=new ViewState(viewer);
        Native950PlayerInfo.initialScene(state,world(actors),7,0,0,0);
        return state;
    }
    private static Actor[] world(Actor... actors) {
        Actor[] world=new Actor[Native950PlayerInfo.SLOTS];
        for(Actor actor:actors) world[actor.index]=actor;
        return world;
    }
    private static byte[] frame(ViewState state,Actor... actors) {
        return Native950PlayerInfo.frame(state,world(actors)).payload();
    }
    private static byte[] terminalDelta(int dx,int dy) {
        int packed=(dy&31)|((dx&31)<<5)|0x4000;
        return passes("1 0 11 0 "+binary(packed,15),"0 11 "+binary(2045,11));
    }
    private static byte[] passes(String... passes) {
        ByteArrayOutputStream result=new ByteArrayOutputStream();
        for(String pass:passes) {
            String text=pass.replace(" ","");byte[] bytes=new byte[(text.length()+7)/8];
            for(int i=0;i<text.length();i++) if(text.charAt(i)=='1') bytes[i/8]|=1<<(7-i%8);
            result.write(bytes,0,bytes.length);
        }
        return result.toByteArray();
    }
    private static int bit(byte[] bytes,int from,int length) {
        int value=0;for(int i=from;i<from+length;i++)value=(value<<1)|((bytes[i/8]>>(7-i%8))&1);return value;
    }
    private static String binary(int value,int width) {
        StringBuilder text=new StringBuilder();for(int i=width-1;i>=0;i--)text.append((value>>>i)&1);return text.toString();
    }
    private static byte[] hex(String value) {
        String text=value.replace(" ","");byte[] bytes=new byte[text.length()/2];
        for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Integer.parseInt(text.substring(i*2,i*2+2),16);return bytes;
    }
}