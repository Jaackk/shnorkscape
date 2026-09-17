package modern947;

import com.rs.cores.Native950TickScheduler;
import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import com.rs.game.ForceMovement;
import com.rs.game.NewForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.player.client.Native950ForceMovement;
import com.rs.game.player.client.Native950ForceMovement.Plan;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;

/** Source-content examples + independently derived native bytes; no cache or sockets; entity cases restore their isolated scheduler fixture. */
public final class Native950ForceMovementTest {
    @Test public void actionBarSurgeUsesSixHundredMillisAndBothDueArrivalsInOneTick() {
        WorldTile origin=tile(3200,3200), end=tile(3210,3200);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(origin,0,end,1,ForceMovement.EAST));
        assertArrayEquals(hex("01 80 00 f6 80 00 80 0000 001e 0030"),encode(plan,origin));
        assertEquals(0,plan.firstArrivalMillis()); assertEquals(600,plan.secondArrivalMillis());
        Native950TickScheduler wheel=new Native950TickScheduler();
        List<WorldTile> arrived=new ArrayList<WorldTile>();
        Native950ForceMovement.schedule(plan,wheel,()->true,arrived::add);
        assertTrue(arrived.isEmpty()); wheel.tick();
        assertEquals(2,arrived.size()); assertTile(origin,arrived.get(0)); assertTile(end,arrived.get(1));
        assertEquals(0,wheel.pendingCount()); wheel.tick(); assertEquals(2,arrived.size());
    }

    @Test public void wildernessDitchUsesTwoAbsoluteDeadlinesNotSummedDurations() {
        WorldTile origin=tile(3200,3521), end=tile(3200,3519);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(origin,1,end,2,ForceMovement.SOUTH));
        assertArrayEquals(hex("01 80 00 00 7e 00 80 1e00 003c 0000"),encode(plan,origin));
        assertEquals(600,plan.firstArrivalMillis()); assertEquals(1200,plan.secondArrivalMillis());
        Native950TickScheduler wheel=new Native950TickScheduler(); List<WorldTile> arrived=new ArrayList<WorldTile>();
        Native950ForceMovement.schedule(plan,wheel,()->true,arrived::add);
        wheel.tick(); assertEquals(1,arrived.size()); assertTile(origin,arrived.get(0));
        wheel.tick(); assertEquals(2,arrived.size()); assertTile(end,arrived.get(1));
    }

    @Test public void godWarsSingleEndpointBecomesOneLegWithoutReturningToOrigin() {
        WorldTile origin=tile(2860,5219), end=tile(2863,5219);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(end,2,ForceMovement.EAST));
        assertArrayEquals(hex("01 80 00 fd 80 00 80 0000 003c 0030"),encode(plan,origin));
        assertFalse(plan.hasSecondTile()); assertNull(plan.secondTile());
        assertEquals(1200,plan.firstArrivalMillis()); assertEquals(0,plan.secondArrivalMillis());
        Native950TickScheduler wheel=new Native950TickScheduler(); List<WorldTile> arrived=new ArrayList<WorldTile>();
        Native950ForceMovement.schedule(plan,wheel,()->true,arrived::add);
        wheel.tick(); assertTrue(arrived.isEmpty()); wheel.tick();
        assertEquals(1,arrived.size()); assertTile(end,arrived.get(0)); assertEquals(0,wheel.pendingCount());
    }

    @Test public void runespanNewForceMovementKeepsItsAngleAndThirtyFiveTickArrival() {
        WorldTile origin=new WorldTile(3200,3200,1), end=new WorldTile(3212,3197,1);
        Plan plan=Native950ForceMovement.plan(origin,new NewForceMovement(origin,1,end,35,0x2345));
        assertArrayEquals(hex("01 80 00 f4 7d 00 80 1e00 041a 4523"),encode(plan,origin));
        assertEquals(600,plan.firstArrivalMillis()); assertEquals(21000,plan.secondArrivalMillis());
    }

    @Test public void preciseRequestsKeepNativeCyclesAndCatchUpBothStagesOnTheWorldWheel() {
        WorldTile origin=tile(3200,3200), first=new WorldTile(3201,3198,1), end=new WorldTile(3203,3196,2);
        ForceMovement request=new NewForceMovement(first,17,end,29,0x1234);
        request.setForceMovementPrecise(true);
        Plan plan=Native950ForceMovement.plan(origin,request);
        assertArrayEquals(hex("01 7f fe fd 7c ff 7e 1100 001d 3412"),encode(plan,origin));
        assertEquals(340,plan.firstArrivalMillis()); assertEquals(580,plan.secondArrivalMillis());
        Native950TickScheduler wheel=new Native950TickScheduler(); List<WorldTile> arrived=new ArrayList<WorldTile>();
        Native950ForceMovement.schedule(plan,wheel,()->true,arrived::add);
        wheel.tick(); assertEquals(2,arrived.size()); assertTile(first,arrived.get(0)); assertTile(end,arrived.get(1));
        assertEquals(0,wheel.pendingCount());
    }

    @Test public void equalDeadlinesGainOneClientCycleAndRoundUpOnlyAtTheWorldBoundary() {
        WorldTile origin=tile(3200,3200), end=tile(3202,3200);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(origin,1,end,1,ForceMovement.NORTH));
        assertArrayEquals(hex("01 80 00 fe 80 00 80 1e00 001f 0020"),encode(plan,origin));
        assertEquals(600,plan.firstArrivalMillis()); assertEquals(620,plan.secondArrivalMillis());
        Native950TickScheduler wheel=new Native950TickScheduler(); List<WorldTile> arrived=new ArrayList<WorldTile>();
        Native950ForceMovement.schedule(plan,wheel,()->true,arrived::add);
        wheel.tick(); assertEquals(1,arrived.size()); wheel.tick(); assertEquals(2,arrived.size());
        assertTile(end,arrived.get(1));
    }

    @Test public void immediateSingleEndpointHasOneNonzeroNativeInterval() {
        WorldTile origin=tile(3200,3200), end=tile(3200,3202);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(end,0,ForceMovement.NORTH));
        assertArrayEquals(hex("01 80 00 00 82 00 80 0000 0001 0020"),encode(plan,origin));
        assertEquals(20,plan.lastArrivalMillis());
    }

    @Test public void allLegacyCompassConstantsUseTheirActualFourteenBitAngles() {
        WorldTile origin=tile(3200,3200);
        String[] angles={"0020","0030","0000","0010","0028","0018","0008","0038"};
        for(int compass=0;compass<8;compass++) {
            Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(origin,0,tile(3201,3200),1,compass));
            assertArrayEquals("compass "+compass,hex("01 80 00 ff 80 00 80 0000 001e "+angles[compass]),encode(plan,origin));
        }
    }

    @Test public void capturedTilesStayImmutableAndMasksRebaseAgainstTheFrameTile() {
        WorldTile origin=tile(3200,3200), first=tile(3200,3200), end=tile(3210,3200);
        Plan plan=Native950ForceMovement.plan(origin,new ForceMovement(first,0,end,1,ForceMovement.EAST));
        origin.setLocation(5000,5000,3); first.setLocation(4000,4000,2); end.setLocation(6000,6000,1);
        plan.firstTile().setLocation(1000,1000,0); plan.secondTile().setLocation(1001,1001,0);
        assertArrayEquals(hex("01 81 00 f7 80 00 80 0000 001e 0030"),encode(plan,tile(3201,3200)));
        assertTile(tile(3200,3200),plan.firstTile()); assertTile(tile(3210,3200),plan.secondTile());
        rejects(()->plan.mask(tile(3400,3200)));
    }

    @Test public void cancellationOrReplacementPreventsEveryQueuedEndpoint() {
        WorldTile origin=tile(3200,3200); Plan plan=Native950ForceMovement.plan(origin,
                new ForceMovement(origin,1,tile(3203,3200),2,ForceMovement.EAST));
        AtomicBoolean current=new AtomicBoolean(true); Native950TickScheduler wheel=new Native950TickScheduler();
        List<WorldTile> arrived=new ArrayList<WorldTile>(); Native950ForceMovement.schedule(plan,wheel,current::get,arrived::add);
        current.set(false); wheel.tick(); wheel.tick();
        assertTrue(arrived.isEmpty()); assertEquals(0,wheel.pendingCount());
    }

    @Test public void invalidAndOverflowingRequestsNeverTruncateIntoAValidNativeBlock() {
        WorldTile base=tile(3200,3200), end=tile(3201,3200);
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(null,1,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(end,-1,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(end,Integer.MAX_VALUE,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(end,2185,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(tile(3328,3200),1,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(tile(3071,3200),1,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(new WorldTile(3200,3200,-1),1,ForceMovement.EAST)));
        rejects(()->Native950ForceMovement.plan(base,new NewForceMovement(base,0,end,1,16384)));
        rejects(()->Native950ForceMovement.plan(base,new NewForceMovement(base,0,end,1,-1)));
        rejects(()->Native950ForceMovement.plan(base,new ForceMovement(base,2,end,1,ForceMovement.EAST)));
        ForceMovement equal=new ForceMovement(base,65535,end,65535,ForceMovement.EAST); equal.setForceMovementPrecise(true);
        rejects(()->Native950ForceMovement.plan(base,equal));
        long refused=Native950ForceMovement.refusals();
        assertNull(Native950ForceMovement.adapt(base,new ForceMovement(end,-1,ForceMovement.EAST)));
        assertEquals(refused+1,Native950ForceMovement.refusals());
        assertNull(Native950ForceMovement.adapt(base,null)); assertEquals(refused+1,Native950ForceMovement.refusals());
    }

    @Test public void lastRepresentableArrivalsArePreservedWithoutSixteenMillisecondScaling() {
        WorldTile base=tile(3200,3200), end=tile(3201,3200);
        Plan normal=Native950ForceMovement.plan(base,new ForceMovement(end,2184,ForceMovement.EAST));
        assertEquals(1310400L,normal.lastArrivalMillis());
        ForceMovement exact=new ForceMovement(end,65535,ForceMovement.EAST); exact.setForceMovementPrecise(true);
        Plan precise=Native950ForceMovement.plan(base,exact); assertEquals(1310700L,precise.lastArrivalMillis());
        assertArrayEquals(hex("01 80 00 ff 80 00 80 0000 ffff 0030"),encode(precise,base));
    }

    @Test public void existingWorldTilePlaneNormalizationIsRespected() {
        WorldTile base=tile(3200,3200), source=new WorldTile(3201,3200,4);
        assertEquals(3,source.getPlane()); // legacy WorldTile publicly clamps high planes
        Plan plan=Native950ForceMovement.plan(base,new ForceMovement(source,1,ForceMovement.EAST));
        assertEquals(3,plan.firstTile().getPlane());
        assertArrayEquals(hex("01 80 00 ff 80 00 7d 0000 001e 0030"),encode(plan,base));
    }

    @Test public void entityForceActivitySurvivesMaskResetAndBothStagesThenEnds() throws Exception {
        withEntity((player,wheel)->{
            WorldTile first=tile(3201,3200), end=tile(3203,3200);
            player.setNextForceMovement(new ForceMovement(first,1,end,2,ForceMovement.EAST));
            assertTrue(player.isNative950ForceMovementActive());
            assertNotNull(player.getNextNative950ForceMovement());
            player.resetMasks(); assertNull(player.getNextNative950ForceMovement());
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick(); assertTile(first,player.getNextWorldTile());
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick(); assertTile(end,player.getNextWorldTile());
            assertFalse(player.isNative950ForceMovementActive());
            assertEquals(0,wheel.failed());
        });
    }

    @Test public void unrelatedTeleportCancelsEveryOldForceArrivalAndSnapshot() throws Exception {
        withEntity((player,wheel)->{
            WorldTile destination=tile(4500,4500);
            player.setNextForceMovement(new ForceMovement(player,1,tile(3212,3200),35,ForceMovement.EAST));
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick(); assertTrue(player.isNative950ForceMovementActive());
            player.setNextWorldTile(destination);
            assertFalse(player.isNative950ForceMovementActive());
            assertNull(player.getNextForceMovement());
            assertArrayEquals(hex("01 80 00 00 80 00 80 0000 0001 0000"),
                    encode(player.getNextNative950ForceMovement(),destination));
            // Another teleport before the frame must rebase the same cancellation to its new endpoint.
            destination=tile(4600,4600); player.setNextWorldTile(destination);
            assertArrayEquals(hex("01 80 00 00 80 00 80 0000 0001 0000"),
                    encode(player.getNextNative950ForceMovement(),destination));
            for(int i=0;i<36;i++) wheel.tick();
            assertTile(destination,player.getNextWorldTile());
            assertEquals(0,wheel.pendingCount());
        });
    }

    @Test public void explicitClearSupersessionAndOneLegCompletionManageActivity() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3202,3200),2,ForceMovement.EAST));
            player.setNextForceMovement(null);
            assertFalse(player.isNative950ForceMovementActive());
            assertArrayEquals(hex("01 80 00 00 80 00 80 0000 0001 0000"),encode(player.getNextNative950ForceMovement(),player));
            player.resetMasks(); assertNull(player.getNextNative950ForceMovement());
            wheel.tick(); wheel.tick(); assertTile(tile(3200,3200),player.getNextWorldTile());
            player.setNextWorldTile(null); // consume the cancellation reposition in this scheduler-only fixture
            player.setNextForceMovement(new ForceMovement(player,1,tile(3209,3200),4,ForceMovement.EAST));
            player.setNextForceMovement(new ForceMovement(tile(3203,3200),2,ForceMovement.EAST));
            wheel.tick(); assertTrue(player.isNative950ForceMovementActive()); assertTile(tile(3200,3200),player.getNextWorldTile());
            wheel.tick(); assertFalse(player.isNative950ForceMovementActive()); assertTile(tile(3203,3200),player.getNextWorldTile());
            wheel.tick(); wheel.tick(); assertTile(tile(3203,3200),player.getNextWorldTile());
        });
    }

    @Test public void invalidReplacementCancelsOldClientPlanWithoutSchedulingAnotherArrival() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(player,1,tile(3209,3200),4,ForceMovement.EAST));
            player.setNextForceMovement(new ForceMovement(tile(3202,3200),-1,ForceMovement.EAST));
            assertFalse(player.isNative950ForceMovementActive()); assertNull(player.getNextForceMovement());
            assertArrayEquals(hex("01 80 00 00 80 00 80 0000 0001 0000"),encode(player.getNextNative950ForceMovement(),player));
            for(int i=0;i<5;i++) wheel.tick();
            assertTile(tile(3200,3200),player.getNextWorldTile());
        });
    }

    @Test public void bothWaypointsCarryTheOriginalMaskGenerationButOnlyCompletionEndsActivity() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3201,3200),1,tile(3203,3200),2,ForceMovement.EAST));
            long generation=player.getNative950ForceMaskGeneration();
            assertTrue(generation>0);
            assertEquals(0,player.getNative950ForceArrivalGeneration());
            player.resetMasks();
            assertEquals(0,player.getNative950ForceMaskGeneration());
            wheel.tick();
            assertTile(tile(3201,3200),player.getNextWorldTile());
            assertEquals("Native logical XY is endpoint2 even during stage one",generation,player.getNative950ForceArrivalGeneration());
            assertTrue(player.isNative950ForceMovementActive());
            wheel.tick();
            assertTile(tile(3203,3200),player.getNextWorldTile());
            assertEquals(generation,player.getNative950ForceArrivalGeneration());
            assertFalse(player.isNative950ForceMovementActive());
            player.resetMasks();
            assertEquals(0,player.getNative950ForceArrivalGeneration());
        });
    }

    @Test public void cancellationAndSupersessionDoNotAdvertiseAForceMaskOrInventArrivals() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3202,3200),2,ForceMovement.EAST));
            long original=player.getNative950ForceMaskGeneration();
            player.setNextForceMovement(null);
            assertEquals("Stationary cancellation is not a new client-owned movement",0,player.getNative950ForceMaskGeneration());
            assertEquals(0,player.getNative950ForceArrivalGeneration());
            wheel.tick();wheel.tick();
            assertTile(tile(3200,3200),player.getNextWorldTile());
            player.setNextWorldTile(null); // cancellation reposition consumed before the replacement
            player.setNextForceMovement(new ForceMovement(tile(3204,3200),2,ForceMovement.EAST));
            long replacement=player.getNative950ForceMaskGeneration();
            assertTrue(replacement>original);
            player.resetMasks();wheel.tick();wheel.tick();
            assertEquals(replacement,player.getNative950ForceArrivalGeneration());
            player.setNextWorldTile(tile(4000,4000));
            assertEquals("An external teleport replaces the completed force arrival",0,player.getNative950ForceArrivalGeneration());
            assertEquals(0,player.getNative950ForceMaskGeneration());
        });
    }

    @Test public void clearingAfterCompletionPreservesTheAlreadyQueuedAuthoritativeArrival() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3202,3200),1,ForceMovement.EAST));
            long generation=player.getNative950ForceMaskGeneration();
            player.resetMasks();wheel.tick();
            assertEquals(generation,player.getNative950ForceArrivalGeneration());
            player.setNextForceMovement(null);
            assertTile(tile(3202,3200),player.getNextWorldTile());
            assertEquals("Clearing future interpolation does not replace the pending tile",generation,
                    player.getNative950ForceArrivalGeneration());
        });
    }

    @Test public void newPlanAfterCompletedArrivalPreservesOldTagUntilAnExplicitTileReplacement() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3202,3200),1,ForceMovement.EAST));
            long oldGeneration=player.getNative950ForceMaskGeneration();
            player.resetMasks();wheel.tick();
            assertEquals(oldGeneration,player.getNative950ForceArrivalGeneration());
            player.setNextForceMovement(new ForceMovement(tile(3204,3200),2,ForceMovement.EAST));
            assertTrue(player.getNative950ForceMaskGeneration()>oldGeneration);
            assertEquals(oldGeneration,player.getNative950ForceArrivalGeneration());
            assertTile(tile(3202,3200),player.getNextWorldTile());
            player.setNextWorldTile(null);
            assertEquals(0,player.getNative950ForceArrivalGeneration());
        });
    }

    @Test public void activeCancellationQueuesARepositionBeforeItsStationaryMask() throws Exception {
        withEntity((player,wheel)->{
            player.setNextForceMovement(new ForceMovement(tile(3208,3200),4,ForceMovement.EAST));
            player.resetMasks();wheel.tick();
            assertNull(player.getNextWorldTile());
            player.setNextForceMovement(null);
            assertTile(tile(3200,3200),player.getNextWorldTile());
            assertEquals(0,player.getNative950ForceArrivalGeneration());
            assertEquals(0,player.getNative950ForceMaskGeneration());
            assertNotNull(player.getNextNative950ForceMovement());
            assertFalse(player.isNative950ForceMovementActive());
            for(int i=0;i<5;i++) wheel.tick();
            assertTile(tile(3200,3200),player.getNextWorldTile());
        });
    }

    @Test public void finalPrefixBaseIsValidatedBeforeSchedulingWideTwoWaypointContent() throws Exception {
        WorldTile base=tile(3200,3200);
        // Each endpoint individually fits the old origin base, but their 254-tile span cannot
        // fit the signed first-waypoint offset after the native prefix selects endpoint2.
        ForceMovement tooWide=new ForceMovement(tile(3073,3200),1,tile(3327,3200),2,ForceMovement.EAST);
        rejects(()->Native950ForceMovement.plan(base,tooWide));
        withEntity((player,wheel)->{
            long refusals=Native950ForceMovement.refusals();
            player.setNextForceMovement(tooWide);
            assertEquals(refusals+1,Native950ForceMovement.refusals());
            assertFalse(player.isNative950ForceMovementActive());
            assertNull(player.getNextNative950ForceMovement());
            assertEquals(0,player.getNative950ForceMaskGeneration());
            assertEquals(0,wheel.pendingCount());
        });
    }

    private interface EntityExercise { void run(Player player,Native950TickScheduler wheel); }
    private static void withEntity(EntityExercise exercise) throws Exception {
        Field field=CoresManager.class.getDeclaredField("native947Scheduler");field.setAccessible(true);
        Object previous=field.get(null); EmbeddedChannel channel=new EmbeddedChannel();
        Native950TickScheduler wheel=new Native950TickScheduler();
        try {
            field.set(null,wheel);
            Player player=Player.createNative950("force-test",tile(3200,3200),channel);
            player.setActive(true);player.setRunning(true);
            exercise.run(player,wheel);
        } finally { field.set(null,previous);channel.finishAndReleaseAll(); }
    }

    private static WorldTile tile(int x,int y) { return new WorldTile(x,y,0); }
    private static void assertTile(WorldTile expected,WorldTile actual) {
        assertEquals(expected.getX(),actual.getX()); assertEquals(expected.getY(),actual.getY()); assertEquals(expected.getPlane(),actual.getPlane());
    }
    private static byte[] encode(Plan plan,WorldTile origin) {
        return Native950PlayerMasks.encode(Native950PlayerMasks.builder().forceMovement(plan.mask(origin)).build());
    }
    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid movement accepted"); } catch(IllegalArgumentException expected) { }
    }
    private static byte[] hex(String text) {
        String s=text.replace(" ",""); byte[] b=new byte[s.length()/2];
        for(int i=0;i<b.length;i++) b[i]=(byte)Integer.parseInt(s.substring(2*i,2*i+2),16); return b;
    }
}
