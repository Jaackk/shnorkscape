package modern947;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.cores.Native950TickScheduler;
import com.rs.cores.TrackedRunnable;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * The tick wheel replaces the slow/fast executor pools for the native 947 JVM. These tests
 * drive a private scheduler instance with a fake tick loop, so they never depend on the
 * Native950World thread or on wall-clock time.
 */
public final class Native950TickSchedulerTest {

    @Test
    public void delaysRoundUpToWholeTicksWithAMinimumOfOne() {
        assertEquals(1, Native950TickScheduler.toTicks(0, TimeUnit.MILLISECONDS));
        assertEquals(1, Native950TickScheduler.toTicks(-5, TimeUnit.SECONDS));
        assertEquals(1, Native950TickScheduler.toTicks(1, TimeUnit.MILLISECONDS));
        assertEquals(1, Native950TickScheduler.toTicks(600, TimeUnit.MILLISECONDS));
        assertEquals(2, Native950TickScheduler.toTicks(601, TimeUnit.MILLISECONDS));
        assertEquals(2, Native950TickScheduler.toTicks(1200, TimeUnit.MILLISECONDS));
        assertEquals(9, Native950TickScheduler.toTicks(5, TimeUnit.SECONDS)); // 5000 / 600 = 8.33
        assertEquals(100, Native950TickScheduler.toTicks(1, TimeUnit.MINUTES));
        assertEquals(6000, Native950TickScheduler.toTicks(1, TimeUnit.HOURS));
    }

    @Test
    public void executeWithDelayFiresAfterCeilOfDelayOverTickLength() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger now = new AtomicInteger(), twoTicks = new AtomicInteger(), fiveTicks = new AtomicInteger();
        wheel.executeNow(now::incrementAndGet);
        wheel.executeWithDelay(twoTicks::incrementAndGet, 1200, TimeUnit.MILLISECONDS); // exactly 2 ticks
        wheel.executeWithDelay(fiveTicks::incrementAndGet, 5);
        assertEquals(3, wheel.pendingCount());

        wheel.tick();
        assertEquals(1, now.get());
        assertEquals(0, twoTicks.get());
        wheel.tick();
        assertEquals(1, twoTicks.get());
        assertEquals(0, fiveTicks.get());
        wheel.tick();
        wheel.tick();
        assertEquals(0, fiveTicks.get());
        wheel.tick();
        assertEquals(1, fiveTicks.get());
        for (int i = 0; i < 5; i++) wheel.tick();
        assertEquals("one-shot tasks never repeat", 1, now.get());
        assertEquals(1, twoTicks.get());
        assertEquals(1, fiveTicks.get());
        assertEquals(0, wheel.pendingCount());
        assertEquals(3, wheel.executed());
        assertEquals(0, wheel.failed());
    }

    @Test
    public void delayIsMeasuredFromTheTickThatSubmitsIt() {
        final Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger inner = new AtomicInteger();
        wheel.executeNow(new Runnable() {
            @Override
            public void run() {
                wheel.executeWithDelay(inner::incrementAndGet, 2); // submitted during tick 1
            }
        });
        wheel.tick(); // tick 1: outer runs
        wheel.tick(); // tick 2
        assertEquals(0, inner.get());
        wheel.tick(); // tick 3 = 1 + 2
        assertEquals(1, inner.get());
    }

    @Test
    public void repeatingTaskFiresEveryPeriodAndStopsWhenCancelled() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final List<Long> fired = new ArrayList<Long>();
        final Native950TickScheduler w = wheel;
        Future<?> handle = wheel.scheduleRepeatingTask2(new Runnable() {
            @Override
            public void run() { fired.add(w.currentTick()); }
        }, 600, 1800, TimeUnit.MILLISECONDS); // start 1 tick, period 3 ticks
        for (int i = 0; i < 8; i++) wheel.tick();
        assertEquals("[1, 4, 7]", fired.toString());
        assertTrue(handle.cancel(false));
        assertTrue(handle.isCancelled());
        for (int i = 0; i < 8; i++) wheel.tick();
        assertEquals("[1, 4, 7]", fired.toString());
        assertEquals(0, wheel.pendingCount());
        assertEquals(1, wheel.cancelled());
    }

    @Test
    public void fixedLengthTaskLeavesTheWheelWhenRepeatReturnsFalse() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger runs = new AtomicInteger();
        wheel.scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                return runs.incrementAndGet() < 3;
            }
        }, 1, 1, TimeUnit.MILLISECONDS); // legacy sub-tick period coarsens to one tick
        for (int i = 0; i < 10; i++) wheel.tick();
        assertEquals(3, runs.get());
        assertEquals(0, wheel.pendingCount());

        final AtomicInteger stopped = new AtomicInteger();
        FixedLengthRunnable early = new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                stopped.incrementAndGet();
                return true;
            }
        };
        wheel.scheduleFixedLengthTask(early, 0, 1); // seconds overload: start 1 tick, period ceil(1000/600) = 2 ticks
        wheel.tick();
        wheel.tick();
        wheel.tick();
        assertEquals("fires at ticks 1 and 3", 2, stopped.get());
        early.stopNow(false);
        for (int i = 0; i < 4; i++) wheel.tick();
        assertEquals("stopNow cancels through the assigned future", 2, stopped.get());
        assertEquals(0, wheel.pendingCount());
    }

    @Test
    public void exceptionsAreCountedAndDoNotStopTheWheel() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger healthy = new AtomicInteger(), broken = new AtomicInteger();
        wheel.executeNow(new Runnable() {
            @Override
            public void run() { throw new IllegalStateException("one-shot boom"); }
        });
        wheel.scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                broken.incrementAndGet();
                throw new IllegalArgumentException("repeating boom");
            }
        }, 600, 600, TimeUnit.MILLISECONDS); // every tick
        wheel.scheduleRepeatingTask(healthy::incrementAndGet, 1, 1); // seconds overload: every 2 ticks (ticks 2,4,6,8)
        wheel.executeWithDelay(new Runnable() {
            @Override
            public void run() { throw new AssertionError("error boom"); }
        }, 1);
        for (int i = 0; i < 8; i++) wheel.tick();
        assertEquals("healthy repeating task kept its 2-tick cadence", 4, healthy.get());
        assertEquals("a repeating task that throws keeps its schedule (legacy parity)", 8, broken.get());
        assertEquals(1 + 8 + 1, wheel.failed());
        assertEquals("only the two repeating entries remain", 2, wheel.pendingCount());
        assertEquals(1 + 8 + 4 + 1, wheel.executed());
    }

    @Test
    public void repeatingTaskThatThrowsAnErrorIsRemoved() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger runs = new AtomicInteger();
        wheel.scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                runs.incrementAndGet();
                throw new OutOfMemoryError("simulated");
            }
        }, 1, 1, TimeUnit.SECONDS);
        for (int i = 0; i < 3; i++) wheel.tick();
        assertEquals(1, runs.get());
        assertEquals(1, wheel.failed());
        assertEquals(0, wheel.pendingCount());
    }

    @Test
    public void gameTasksRunInSubmissionOrderAtTheNextDrain() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final List<String> order = new ArrayList<String>();
        wheel.addGameTask(new Runnable() { @Override public void run() { order.add("a"); } });
        wheel.addGameTask(new Runnable() { @Override public void run() { throw new RuntimeException("b"); } });
        wheel.addGameTask(new Runnable() { @Override public void run() { order.add("c"); } });
        assertEquals(3, wheel.gameTaskCount());
        wheel.tick();
        assertEquals("[a, c]", order.toString());
        assertEquals(0, wheel.gameTaskCount());
        assertEquals(1, wheel.failed());
        assertEquals(3, wheel.gameTasksRun());
    }

    @Test
    public void trackedTasksAreCancelledByKeyAndDuplicateKeysAreRefused() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger runs = new AtomicInteger();
        TrackedRunnable tracked = new TrackedRunnable() {
            @Override
            public void run() { runs.incrementAndGet(); }
        };
        wheel.scheduleAndTrackRepeatingTask(tracked, 600, 600, TimeUnit.MILLISECONDS);
        wheel.scheduleAndTrackRepeatingTask(tracked, 600, 600, TimeUnit.MILLISECONDS); // duplicate: not scheduled
        wheel.tick();
        wheel.tick();
        assertEquals(2, runs.get());
        wheel.cancelTrackedTask(tracked.getTrackingKey(), true);
        wheel.tick();
        assertEquals(2, runs.get());
        assertEquals(0, wheel.pendingCount());
        wheel.cancelTrackedTask("missing", false); // harmless
    }

    @Test
    public void callablesCompleteTheirFutureAndConsumersRunOnTheDrainingThread() throws Exception {
        Native950TickScheduler wheel = new Native950TickScheduler();
        Future<Integer> answer = wheel.submitNow(new Callable<Integer>() {
            @Override
            public Integer call() { return 42; }
        });
        final AtomicReference<Thread> consumerThread = new AtomicReference<Thread>();
        final AtomicReference<String> consumed = new AtomicReference<String>();
        wheel.submitNow(new Callable<String>() {
            @Override
            public String call() { return "value"; }
        }, value -> { consumed.set(value); consumerThread.set(Thread.currentThread()); });
        Future<?> failing = wheel.runNow(new Runnable() {
            @Override
            public void run() { throw new IllegalStateException("runNow surfaces failures"); }
        });
        assertFalse(answer.isDone());
        wheel.tick();
        assertEquals(Integer.valueOf(42), answer.get(1, TimeUnit.SECONDS));
        assertEquals("value", consumed.get());
        assertSame(Thread.currentThread(), consumerThread.get());
        assertTrue(failing.isDone());
        try {
            failing.get();
            fail("runNow must surface the exception through the future");
        } catch (java.util.concurrent.ExecutionException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        }
        assertEquals(1, wheel.failed());
    }

    @Test
    public void blockingOnAFutureFromTheOwnerThreadRunsTheTaskInlineInsteadOfDeadlocking() throws Exception {
        Native950TickScheduler wheel = new Native950TickScheduler();
        wheel.tick(); // binds this thread as the owner
        final AtomicInteger runs = new AtomicInteger();
        Future<?> future = wheel.executeNow(runs::incrementAndGet);
        assertNull(future.get(1, TimeUnit.SECONDS));
        assertEquals(1, runs.get());
        assertEquals(1, wheel.inlineGets());
        wheel.tick();
        wheel.tick();
        assertEquals("the wheel copy was dropped", 1, runs.get());
        assertEquals(0, wheel.pendingCount());
    }

    @Test
    public void tickFromASecondThreadIsRejected() throws Exception {
        final Native950TickScheduler wheel = new Native950TickScheduler();
        wheel.tick();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        Thread other = new Thread(new Runnable() {
            @Override
            public void run() {
                try { wheel.tick(); } catch (Throwable t) { failure.set(t); }
            }
        }, "not-the-world");
        other.start();
        other.join(5000);
        assertTrue(failure.get() instanceof IllegalStateException);
        assertSame(Thread.currentThread(), wheel.owner());
    }

    @Test
    public void worldTaskScheduledForTwoTicksFiresOnTheThirdDrainOfTheFakeTickLoop() {
        Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicInteger fired = new AtomicInteger();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() { fired.incrementAndGet(); }
        }, 2);
        for (int drain = 1; drain <= 3; drain++) {
            // The same pair Native950World.pumpSchedulers() calls every 600 ms, in the same order.
            wheel.tick();
            WorldTasksManager.processTasks();
            assertEquals("drain " + drain, drain == 3 ? 1 : 0, fired.get());
        }
        for (int i = 0; i < 3; i++) {
            wheel.tick();
            WorldTasksManager.processTasks();
        }
        assertEquals("a one-shot WorldTask is removed after it runs", 1, fired.get());
    }

    @Test
    public void nativeInitInstallsADelegatingProviderWithoutTheLegacyWorldThread() throws Exception {
        Native950TickScheduler mine = new Native950TickScheduler();
        boolean installed;
        if (CoresManager.getServiceProvider() == null) {
            installed = CoresManager.initNative950(mine);
            assertTrue(installed);
        } else {
            installed = false; // another test in this JVM (Native950World) installed the wheel first
        }
        assertTrue(CoresManager.isNative950());
        assertNotNull(CoresManager.getNative950Scheduler());
        assertNull("initNative950 never constructs WorldThread", CoresManager.worldThread);
        assertNull("initNative950 never constructs the fast pool", CoresManager.fastExecutorV2);
        CoresManager.ServiceProvider provider = CoresManager.getServiceProvider();
        assertNotNull(provider);
        assertNotSame("the native JVM must never hold a pool-backed provider", CoresManager.ServiceProvider.class, provider.getClass());
        assertTrue(CoresManager.ServiceProvider.class.isAssignableFrom(provider.getClass()));
        assertFalse("same scheduler is idempotent", CoresManager.initNative950(CoresManager.getNative950Scheduler()));
        try {
            CoresManager.initNative950(new Native950TickScheduler());
            fail("two tick wheels must not coexist");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("native 947"));
        }
        try {
            CoresManager.init();
            fail("the legacy WorldThread must never start in the native JVM");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("WorldThread"));
        }

        // Every non-final method the 910 code can call on ServiceProvider must be overridden.
        List<String> missing = new ArrayList<String>();
        for (Method method : CoresManager.ServiceProvider.class.getDeclaredMethods()) {
            int mods = method.getModifiers();
            if (method.isSynthetic() || Modifier.isStatic(mods) || Modifier.isPrivate(mods) || Modifier.isFinal(mods)) continue;
            try {
                Method override = provider.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
                if (override.getDeclaringClass() == CoresManager.ServiceProvider.class) missing.add(method.toString());
            } catch (NoSuchMethodException notOverridden) {
                missing.add(method.toString());
            }
        }
        assertEquals("ServiceProvider methods without a native delegate: " + missing, 0, missing.size());

        if (installed) {
            // The provider really reaches the wheel: schedule through the static facade and observe
            // the entries on the wheel. The GLOBAL wheel is never ticked here: this JVM is shared
            // with Native950WorldLifecycleTest, whose world thread must be the only owner, and a
            // drain from the JUnit thread would bind it here and make every world tick throw.
            final AtomicInteger runs = new AtomicInteger();
            provider.executeWithDelay(runs::incrementAndGet, 1);
            assertEquals(1, mine.pendingCount());
            provider.addGameTask(runs::incrementAndGet);
            assertEquals(1, mine.gameTaskCount());
            assertEquals(2, mine.submitted());
            assertEquals(0, runs.get());
            assertNull("the JUnit thread must not own the global wheel", mine.owner());
        }
        assertNotSame("the global wheel is owned by the world thread or nobody, never by JUnit",
                Thread.currentThread(), CoresManager.getNative950Scheduler().owner());
    }

    @Test
    public void explicitOwnerBindingWinsOverTheFirstCallerAndIsNeverRebound() throws Exception {
        final Native950TickScheduler wheel = new Native950TickScheduler();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        final AtomicInteger ticks = new AtomicInteger();
        Thread world = new Thread(new Runnable() {
            @Override
            public void run() {
                try { wheel.tick(); ticks.incrementAndGet(); } catch (Throwable t) { failure.set(t); }
            }
        }, "fake-world");
        wheel.bindOwner(world); // bound before the thread starts, exactly like Native950World
        wheel.bindOwner(world); // idempotent for the same thread
        assertSame(world, wheel.owner());
        try {
            wheel.tick(); // the JUnit thread drained "first" but is not the owner
            fail("a bound wheel must reject every other thread");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("fake-world"));
        }
        try {
            wheel.bindOwner(Thread.currentThread());
            fail("a bound wheel must never be rebound to another thread");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("cannot be rebound"));
        }
        world.start();
        world.join(5000);
        assertNull(failure.get());
        assertEquals(1, ticks.get());
        assertEquals(1, wheel.currentTick());
        assertSame(world, wheel.owner());
    }
}
