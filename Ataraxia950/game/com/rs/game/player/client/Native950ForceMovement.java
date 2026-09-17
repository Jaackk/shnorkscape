package com.rs.game.player.client;

import com.rs.cores.Native950TickScheduler;
import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Converts legacy movement requests into one immutable 950 wire/scheduling plan.
 * Evidence: protocol-analysis/player-force-movement-950.md. The native logic clock is
 * 20 ms, not the 16 ms used by the legacy ForceMovement.get*TickTime methods.
 */
public final class Native950ForceMovement {
    public static final int CLIENT_CYCLE_MILLIS = 20;
    public static final int CLIENT_CYCLES_PER_GAME_TICK = 30;
    private static final AtomicLong REFUSALS = new AtomicLong();
    private Native950ForceMovement() { }

    /** Invalid content requests are counted and withheld before touching the native scheduler. */
    public static Plan adapt(WorldTile origin, ForceMovement movement) {
        if (movement == null) return null;
        try { return plan(origin, movement); }
        catch (IllegalArgumentException invalid) { REFUSALS.incrementAndGet(); return null; }
    }

    /**
     * Snapshot while the request is queued: callers often pass their mutable Player as tile 1.
     * Both delays are arrival offsets from request time, not consecutive stage durations.
     * Normal delays are game ticks; precise delays already count native 20 ms cycles.
     */
    public static Plan plan(WorldTile origin, ForceMovement movement) {
        if (movement == null) throw new IllegalArgumentException("Missing force movement");
        WorldTile base = copyValid(origin, "origin");
        WorldTile first = copyValid(movement.getToFirstTile(), "first tile");
        WorldTile second = movement.getToSecondTile() == null ? null
                : copyValid(movement.getToSecondTile(), "second tile");
        int scale = movement.preciseMovement() ? 1 : CLIENT_CYCLES_PER_GAME_TICK;
        int firstCycle = cycles(movement.getFirstTileTicketDelay(), scale);
        int secondCycle = second == null ? 0 : cycles(movement.getSecondTileTicketDelay(), scale);
        int angle = movement.getDirection(); // ForceMovement converts compass constants; NewForceMovement already holds a 14-bit angle.
        if (angle < 0 || angle > 16383) throw new IllegalArgumentException("Direction is not a native 14-bit angle");
        if (second == null) {
            // One destination is origin@0 -> target@arrival. This preserves a single leg and
            // never invents the legacy second-origin/deadline-zero return leg.
            if (firstCycle == 0) firstCycle = 1;
        } else {
            if (secondCycle < firstCycle) throw new IllegalArgumentException("Second arrival precedes first arrival");
            if (secondCycle == firstCycle) {
                if (secondCycle == 65535) throw new IllegalArgumentException("Equal terminal arrivals leave no encodable interval");
                secondCycle++; // one native cycle, not another game tick
            }
        }
        Plan plan = new Plan(base, first, second, firstCycle, secondCycle, angle);
        plan.mask(base); // validate every relative field before admitting the request
        WorldTile finalTile = plan.finalTile();
        // Publication queues final XY first. Its plane stays authoritative until the scheduled
        // arrival, so validate the actual paired mask base before any server callbacks exist.
        plan.mask(new WorldTile(finalTile.getX(), finalTile.getY(), base.getPlane()));
        return plan;
    }

    /**
     * An actor teleport only changes its path; it does not clear the native interpolation
     * deadlines. Replace any active client plan with a stationary 0/1-cycle interval at
     * the authoritative destination. The owning Entity must queue a matching reposition
     * before this mask, because the mask sink otherwise uses the interpolated render base.
     */
    public static Plan stationary(WorldTile destination, int direction) {
        WorldTile tile=copyValid(destination,"cancellation tile");
        if (direction<0 || direction>16383) throw new IllegalArgumentException("Invalid cancellation angle");
        return new Plan(tile, new WorldTile(tile), new WorldTile(tile), 0, 1, direction);
    }

    /**
     * Use the existing world-thread wheel. Two arrivals rounded to the same 600 ms tick are
     * both applied in order at that tick; the last one wins before movement/frame processing.
     * No wall-time poll or extra repeat can delay the second endpoint by another game tick.
     * stillCurrent cancels an explicit clear, superseding request, or finished character.
     */
    public static void schedule(final Plan plan, Native950TickScheduler scheduler,
                                final BooleanSupplier stillCurrent, final Consumer<WorldTile> arrive) {
        schedule(plan, scheduler, stillCurrent, arrive, () -> { });
    }

    /** Complete only after the current plan's terminal arrival, never for a cancelled plan. */
    public static void schedule(final Plan plan, Native950TickScheduler scheduler,
                                final BooleanSupplier stillCurrent, final Consumer<WorldTile> arrive,
                                final Runnable complete) {
        Objects.requireNonNull(plan, "plan"); Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(stillCurrent, "stillCurrent"); Objects.requireNonNull(arrive, "arrive");
        Objects.requireNonNull(complete, "complete");
        scheduler.executeWithDelay(() -> {
            if (stillCurrent.getAsBoolean()) {
                arrive.accept(plan.firstTile());
                if (!plan.hasSecondTile() && stillCurrent.getAsBoolean()) complete.run();
            }
        }, plan.firstArrivalMillis(), TimeUnit.MILLISECONDS);
        if (plan.hasSecondTile()) scheduler.executeWithDelay(() -> {
            if (stillCurrent.getAsBoolean()) {
                arrive.accept(plan.secondTile());
                if (stillCurrent.getAsBoolean()) complete.run();
            }
        }, plan.secondArrivalMillis(), TimeUnit.MILLISECONDS);
    }

    public static long refusals() { return REFUSALS.get(); }

    public static final class Plan {
        private final WorldTile origin, first, second;
        private final int firstCycle, secondCycle, direction;
        private Plan(WorldTile origin, WorldTile first, WorldTile second, int firstCycle, int secondCycle, int direction) {
            this.origin=origin; this.first=first; this.second=second;
            this.firstCycle=firstCycle; this.secondCycle=secondCycle; this.direction=direction;
        }
        public WorldTile firstTile() { return new WorldTile(first); }
        public WorldTile secondTile() { return second == null ? null : new WorldTile(second); }
        public boolean hasSecondTile() { return second != null; }
        /** The native logical XY base throughout interpolation, independent of rendered position. */
        public WorldTile finalTile() { return new WorldTile(second == null ? first : second); }
        public long firstArrivalMillis() { return (long)firstCycle * CLIENT_CYCLE_MILLIS; }
        public long secondArrivalMillis() { return second == null ? 0L : (long)secondCycle * CLIENT_CYCLE_MILLIS; }
        public long lastArrivalMillis() { return second == null ? firstArrivalMillis() : secondArrivalMillis(); }

        /**
         * Rebase against the authoritative tile used by this frame's movement prefix. The
         * 950 sink adds each signed tile delta*512 to that actor/path base, and each plane
         * delta to its plane. Never resend this request after its original mask tick.
         */
        public Native950PlayerMasks.ForceMovement mask(WorldTile frameBase) {
            WorldTile base=copyValid(frameBase,"frame base");
            WorldTile from=second == null ? origin : first;
            WorldTile to=second == null ? first : second;
            return Native950PlayerMasks.ForceMovement.of(
                    from.getX()-base.getX(), from.getY()-base.getY(),
                    to.getX()-base.getX(), to.getY()-base.getY(),
                    from.getPlane()-base.getPlane(), to.getPlane()-base.getPlane(),
                    second == null ? 0 : firstCycle, second == null ? firstCycle : secondCycle, direction);
        }
    }

    private static int cycles(int value,int scale) {
        long cycles=(long)value*scale;
        if (value<0 || cycles>65535) throw new IllegalArgumentException("Force movement arrival does not fit native u16 cycles");
        return (int)cycles;
    }
    private static WorldTile copyValid(WorldTile tile,String label) {
        if (tile==null || tile.getX()<0 || tile.getX()>16383 || tile.getY()<0 || tile.getY()>16383
                || tile.getPlane()<0 || tile.getPlane()>3) throw new IllegalArgumentException("Invalid "+label);
        return new WorldTile(tile);
    }
}
