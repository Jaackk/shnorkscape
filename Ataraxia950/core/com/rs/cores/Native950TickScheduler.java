package com.rs.cores;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.rs.utils.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Tick-driven replacement for the executor pools behind {@link CoresManager.ServiceProvider}.
 * <p>
 * The legacy 910 engine runs {@code scheduleRepeatingTask}, {@code scheduleFixedLengthTask},
 * {@code executeWithDelay} and {@code executeNow} on the {@code slowExecutor} thread pool, i.e.
 * OFF the world thread, and content mutates entities from those threads. The native 947 world
 * owns exactly one thread ({@code Ataraxia-947-world}); every transport and NPC view already
 * throws when touched from any other thread. This class therefore keeps the same public surface
 * but stores every request in a tick wheel that is drained by {@link #tick()} on the calling
 * world thread: no pools, no extra threads, no off-thread entity mutation.
 * <p>
 * Time conversion: a delay or period expressed in wall-clock units is converted to 600 ms game
 * ticks, rounded UP, minimum one tick ({@link #toTicks(long, TimeUnit)}). {@code executeNow} runs
 * at the next drain. Legacy call sites that used 1 ms or 10 ms periods (force movement,
 * vecna timer) therefore run once per tick, which is the granularity the game state changes at
 * anyway; the audit in {@code notes/P0-serviceprovider-audit.md} lists them.
 * <p>
 * Failure policy (documented choice): an {@link Exception} thrown by a task is caught, logged and
 * counted; a one-shot task is simply done, a repeating task keeps its schedule. That matches the
 * legacy wrappers, which catch {@code Exception} around {@code r.run()} and let the executor
 * re-run the task. An {@link Error} is also caught (so the world tick survives) but the task is
 * removed from the wheel, which is what the legacy executor effectively did when an Error
 * escaped a periodic task. Nothing is ever retried silently; every failure increments
 * {@link #failed()}.
 * <p>
 * Thread contract: submissions may come from any thread (netty event loop, JUnit). The owner
 * thread is bound explicitly with {@link #bindOwner(Thread)} (Native950World binds its own
 * thread before starting it) or, for private wheels driven by a fake tick loop in tests, by the
 * first call to {@link #tick()}; a tick from any other thread throws and rebinding is refused,
 * so a wheel that some other thread has already drained can never be adopted by the world
 * silently. Blocking on a returned {@link Future} from the owner thread would deadlock the
 * world, so such a {@code get()} runs the pending task inline instead and is counted in
 * {@link #inlineGets()}.
 */
public final class Native950TickScheduler {

    /** Game tick length in milliseconds; identical to the legacy {@code Settings.WORLD_CYCLE_TIME}. */
    public static final long TICK_MILLIS = 600L;

    private final ConcurrentLinkedQueue<Entry> incoming = new ConcurrentLinkedQueue<Entry>();
    private final PriorityQueue<Entry> wheel = new PriorityQueue<Entry>();
    private final Queue<Runnable> gameTasks = new ConcurrentLinkedQueue<Runnable>();
    private final Map<String, Entry> tracked = new ConcurrentHashMap<String, Entry>();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong submitted = new AtomicLong();
    private final AtomicLong executed = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong cancelled = new AtomicLong();
    private final AtomicLong inlineGets = new AtomicLong();
    private final AtomicLong gameTasksRun = new AtomicLong();
    private volatile long tick;
    private volatile Thread owner;
    private boolean draining;

    /** Converts a wall-clock delay to whole game ticks: round up, never less than one tick. */
    public static long toTicks(long delay, TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        long millis = unit.toMillis(delay);
        if (millis <= 0) return 1L;
        return (millis + TICK_MILLIS - 1) / TICK_MILLIS;
    }

    // ---------------------------------------------------------------- game task queue

    /** Same semantics as the legacy queue: run once, in submission order, at the next drain. */
    public void addGameTask(Runnable action) {
        gameTasks.add(Objects.requireNonNull(action, "action"));
        submitted.incrementAndGet();
    }

    /** Drains the game task queue on the current (owner) thread. Called by {@link #tick()}. */
    public void runGameTasks() {
        for (;;) {
            Runnable next = gameTasks.poll();
            if (next == null) break;
            gameTasksRun.incrementAndGet();
            try {
                next.run();
            } catch (Throwable failure) {
                failed.incrementAndGet();
                Logger.getGlobal().error("[Native950TickScheduler] game task failed: " + describe(next), failure);
            }
        }
    }

    // ---------------------------------------------------------------- one-shot

    public Future<?> executeNow(Runnable r) {
        return submit(new Entry(Objects.requireNonNull(r, "r"), null, 0L, 0L, true));
    }

    /** Legacy {@code runNow} did not swallow exceptions; here they also fail the returned future. */
    public Future<?> runNow(Runnable r) {
        Entry entry = new Entry(Objects.requireNonNull(r, "r"), null, 0L, 0L, false);
        return submit(entry);
    }

    public void executeWithDelay(Runnable r, long startDelay, TimeUnit unit) {
        submit(new Entry(Objects.requireNonNull(r, "r"), null, toTicks(startDelay, unit), 0L, true));
    }

    /** Ticks are already the wheel unit; zero or negative becomes the next drain plus one tick. */
    public void executeWithDelay(Runnable r, int ticks) {
        submit(new Entry(Objects.requireNonNull(r, "r"), null, Math.max(1, ticks), 0L, true));
    }

    public <T> ListenableFuture<T> submitNow(Callable<T> callable) {
        Entry entry = new Entry(null, Objects.requireNonNull(callable, "callable"), 0L, 0L, false);
        submit(entry);
        @SuppressWarnings("unchecked")
        ListenableFuture<T> typed = (ListenableFuture<T>) entry.future;
        return typed;
    }

    /** The consumer runs on the world thread right after the callable, never on a pool thread. */
    public <T> void submitNow(final Callable<T> callable, final Consumer<T> result) {
        Objects.requireNonNull(callable, "callable");
        Objects.requireNonNull(result, "result");
        submit(new Entry(null, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                T value = callable.call();
                result.accept(value);
                return value;
            }
        }, 0L, 0L, false));
    }

    // ---------------------------------------------------------------- repeating

    public void scheduleRepeatingTask(Runnable r, long startDelay, long delayCount, TimeUnit unit) {
        scheduleRepeatingTask2(r, startDelay, delayCount, unit);
    }

    public void scheduleRepeatingTask(Runnable r, long startDelay, long delayCount) {
        scheduleRepeatingTask2(r, startDelay, delayCount, TimeUnit.SECONDS);
    }

    /** Returns a future whose {@code cancel} removes the periodic entry from the wheel. */
    public Future<?> scheduleRepeatingTask2(Runnable r, long startDelay, long delayCount, TimeUnit unit) {
        return submit(new Entry(Objects.requireNonNull(r, "r"), null, toTicks(startDelay, unit), toTicks(delayCount, unit), true));
    }

    /**
     * Honours the {@link FixedLengthRunnable} contract: its {@code run()} calls {@code repeat()}
     * and cancels the assigned future when that returns false, which flags the wheel entry.
     */
    public void scheduleFixedLengthTask(FixedLengthRunnable r, long startDelay, long delayCount, TimeUnit unit) {
        Objects.requireNonNull(r, "r");
        Entry entry = new Entry(r, null, toTicks(startDelay, unit), toTicks(delayCount, unit), true);
        r.assignFuture(entry.future);
        submit(entry);
    }

    public void scheduleFixedLengthTask(FixedLengthRunnable r, long startDelay, long delayCount) {
        scheduleFixedLengthTask(r, startDelay, delayCount, TimeUnit.SECONDS);
    }

    public void scheduleAndTrackRepeatingTask(TrackedRunnable r, long startDelay, long delayCount, TimeUnit unit) {
        Objects.requireNonNull(r, "r");
        Entry entry = new Entry(r, null, toTicks(startDelay, unit), toTicks(delayCount, unit), true);
        if (tracked.putIfAbsent(r.getTrackingKey(), entry) != null) {
            Logger.getGlobal().warn("[Native950TickScheduler] duplicate tracking key " + r.getTrackingKey() + "; task not scheduled");
            return;
        }
        submit(entry);
    }

    public void cancelTrackedTask(String key, boolean interrupt) {
        Entry entry = key == null ? null : tracked.remove(key);
        if (entry != null) entry.future.cancel(interrupt);
    }

    // ---------------------------------------------------------------- ownership

    /**
     * Binds the only thread allowed to {@link #tick()} this wheel. Native950World calls this
     * with its world thread BEFORE starting it, so ownership never depends on which thread
     * happens to drain first (in the shared Gradle test JVM that could be the JUnit thread,
     * which would then throw on every world tick forever). Idempotent for the same thread;
     * fails closed when the wheel is already bound to a different thread, because a wheel
     * another thread has drained may hold entries measured against that thread's tick count.
     *
     * @throws IllegalStateException when the wheel already belongs to another thread
     */
    public synchronized void bindOwner(Thread thread) {
        Objects.requireNonNull(thread, "thread");
        Thread bound = owner;
        if (bound == null) {
            owner = thread;
        } else if (bound != thread) {
            throw new IllegalStateException("Native950TickScheduler is already owned by " + bound.getName()
                    + " (ticks=" + tick + "); it cannot be rebound to " + thread.getName());
        }
    }

    // ---------------------------------------------------------------- draining

    /**
     * Runs the game task queue, then every wheel entry due at this tick, on the calling thread.
     * An unbound wheel is bound to the first caller (private wheels in tests); a bound wheel
     * rejects every other thread so a stray legacy pool can never drain it concurrently.
     */
    public void tick() {
        Thread current = Thread.currentThread();
        Thread bound = owner;
        if (bound == null) {
            bindOwner(current);
            bound = owner;
        }
        if (bound != current) {
            throw new IllegalStateException("Native950TickScheduler is owned by " + bound.getName() + "; tick() called from " + current.getName());
        }
        if (draining) throw new IllegalStateException("Native950TickScheduler.tick() re-entered from a task");
        draining = true;
        try {
            long now = tick + 1;
            tick = now; // tasks submitted during this drain measure their delay from this tick
            runGameTasks();
            for (;;) {
                Entry moved = incoming.poll();
                if (moved == null) break;
                if (!moved.cancelledFlag) wheel.add(moved);
            }
            for (;;) {
                Entry head = wheel.peek();
                if (head == null || head.dueTick > now) break;
                wheel.poll();
                if (head.cancelledFlag) continue;
                run(head);
                if (head.periodTicks > 0 && !head.cancelledFlag) {
                    head.dueTick = now + head.periodTicks;
                    wheel.add(head);
                }
            }
            purgeCancelled();
        } finally {
            draining = false;
        }
    }

    private void purgeCancelled() {
        if (wheel.isEmpty()) return;
        boolean any = false;
        for (Entry entry : wheel) {
            if (entry.cancelledFlag) { any = true; break; }
        }
        if (!any) return;
        PriorityQueue<Entry> kept = new PriorityQueue<Entry>();
        for (Entry entry : wheel) if (!entry.cancelledFlag) kept.add(entry);
        wheel.clear();
        wheel.addAll(kept);
    }

    private void run(Entry entry) {
        executed.incrementAndGet();
        try {
            if (entry.callable != null) {
                entry.future.complete(entry.callable.call());
            } else {
                entry.task.run();
                if (entry.periodTicks == 0) entry.future.complete(null);
            }
        } catch (Exception failure) {
            failed.incrementAndGet();
            Logger.getGlobal().error("[Native950TickScheduler] task failed (tick " + tick + "): " + describe(entry), failure);
            if (entry.periodTicks == 0) {
                if (entry.swallow) entry.future.complete(null);
                else entry.future.fail(failure);
            }
            // repeating tasks keep their schedule, as the legacy executor wrapper did
        } catch (Error failure) {
            failed.incrementAndGet();
            entry.cancelledFlag = true;
            entry.future.fail(failure);
            Logger.getGlobal().error("[Native950TickScheduler] task removed after Error (tick " + tick + "): " + describe(entry), failure);
        }
    }

    private Future<?> submit(Entry entry) {
        entry.dueTick = tick + entry.delayTicks;
        submitted.incrementAndGet();
        incoming.add(entry);
        return entry.future;
    }

    private static String describe(Object task) {
        return task == null ? "null" : task.getClass().getName();
    }

    private String describe(Entry entry) {
        return entry.callable != null ? describe(entry.callable) : describe(entry.task);
    }

    // ---------------------------------------------------------------- observability

    /** Number of drains completed or in progress. */
    public long currentTick() { return tick; }
    public long submitted() { return submitted.get(); }
    public long executed() { return executed.get(); }
    public long failed() { return failed.get(); }
    public long cancelled() { return cancelled.get(); }
    public long inlineGets() { return inlineGets.get(); }
    public long gameTasksRun() { return gameTasksRun.get(); }
    public int gameTaskCount() { return gameTasks.size(); }
    public Thread owner() { return owner; }

    /** Live (not cancelled) entries waiting on the wheel, including ones not yet moved from the inbox. */
    public int pendingCount() {
        int count = 0;
        for (Entry entry : incoming) if (!entry.cancelledFlag) count++;
        for (Entry entry : wheel) if (!entry.cancelledFlag) count++;
        return count;
    }

    @Override
    public String toString() {
        return "Native950TickScheduler[tick=" + tick + ", pending=" + pendingCount() + ", submitted=" + submitted.get()
                + ", executed=" + executed.get() + ", failed=" + failed.get() + ", cancelled=" + cancelled.get()
                + ", inlineGets=" + inlineGets.get() + "]";
    }

    // ---------------------------------------------------------------- internals

    private final class Entry implements Comparable<Entry> {
        final long seq = sequence.incrementAndGet();
        final Runnable task;
        final Callable<?> callable;
        final long delayTicks;
        final long periodTicks;
        /** true: exceptions are logged and the future still completes (legacy executeNow); false: the future fails. */
        final boolean swallow;
        final TickFuture<Object> future = new TickFuture<Object>(this);
        long dueTick;
        volatile boolean cancelledFlag;

        Entry(Runnable task, Callable<?> callable, long delayTicks, long periodTicks, boolean swallow) {
            this.task = task;
            this.callable = callable;
            this.delayTicks = delayTicks;
            this.periodTicks = periodTicks;
            this.swallow = swallow;
        }

        void cancel() {
            if (!cancelledFlag) {
                cancelledFlag = true;
                cancelled.incrementAndGet();
            }
        }

        @Override
        public int compareTo(Entry other) {
            if (dueTick != other.dueTick) return dueTick < other.dueTick ? -1 : 1;
            return seq < other.seq ? -1 : (seq == other.seq ? 0 : 1);
        }
    }

    /** Future handed to callers; cancelling it removes the entry from the wheel. */
    private final class TickFuture<V> extends AbstractFuture<V> {
        private final Entry entry;

        TickFuture(Entry entry) { this.entry = entry; }

        void complete(Object value) {
            @SuppressWarnings("unchecked")
            V typed = (V) value;
            set(typed);
        }

        void fail(Throwable failure) { setException(failure); }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = super.cancel(mayInterruptIfRunning);
            entry.cancel();
            return result;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            runInlineIfOwner();
            return super.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            runInlineIfOwner();
            return super.get(timeout, unit);
        }

        /**
         * A legacy caller blocking on a pool future from the world thread would now wait for
         * its own drain. Run the one-shot task inline instead and count it so the audit can
         * find the caller; periodic entries are never run inline.
         */
        private void runInlineIfOwner() {
            if (isDone() || entry.cancelledFlag || entry.periodTicks > 0) return;
            if (Thread.currentThread() != owner) return;
            inlineGets.incrementAndGet();
            entry.cancelledFlag = true; // drop the wheel copy; the run below is the only execution
            Logger.getGlobal().warn("[Native950TickScheduler] Future.get() on the world thread ran " + describe(entry) + " inline");
            run(entry);
        }
    }
}
