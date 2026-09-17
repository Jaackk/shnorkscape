package com.rs.cores;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.rs.utils.Logger;
import com.rs.utils.mysql.SQLThread;
import lombok.Data;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The CoresManager is responsbile for initializing thread behaviour.
 * <p>
 * The key things to remember about the game engine are as follows:<br/>
 * <br/>
 * <p>
 * The main thread handles game-tick based tasks, such as {@code WorldTask}s and general game actions. Therefore, if performing a scheduled
 * or delayed execution based on game ticks, use the {@code WorldTasksManager.schedule(...)} approach.<br/>
 * <br/>
 * <p>
 * The {@code slowExecutor} manages a pool of threads dedicated to running either continuously repeated tasks, or
 * {@link FixedLengthRunnable} objects. In either of these cases, the frequency of the {@code run()} call is not bound to game ticks - it
 * runs for the specified interval with a specified time unit.<br/>
 * <br/>
 * <p>
 * The {@code fastExecutor} manages a pool of threads dedicated to running single-execution {@link Runnable}s immediately after they are
 * submitted for exeuction. Like the {@code slowExecutor}, the start delay of the {@code run()} call is not bound to game ticks - it will
 * run as soon as the thread pool supplies a thread to run it.<br/>
 * <br/>
 * <p>
 * "then it (the {@code fastExecutor}) shouldn't carry the downfalls of the timer based system. "<br/>
 *
 * <pre>
 *     - Noele, when (indirectly) talking about the inconsistencies
 *       of mixing threading APIs in a fully multi-threaded system.
 * </pre>
 * <p>
 * <p>
 * Exactly! But, with the way we have implemented it, it (the {@code fastExecutor}) doesn't! The underlying thread pool executor objects
 * ({@code slowExecuter} and {@code fastExecutor}) are of a different type; one is a subclass of a {@link ScheduledExecutorService}
 * ({@code slowExecuter}) and the other is a subclasss of {@link ExecutorService} ({@code fastExecutor}).<br/>
 * <br/>
 * <p>
 * The {@code fastExecutor} can only call {@code execute(Runnable r)}, {@code call(Callable c)}, and {@code submit(Runnable r)}, all of
 * which do the same thing: run a {@link Runnable} once and only once, and as soon as a thread is supplied from the thread pool.<br/>
 * <br/>
 * <p>
 * The {@code slowExecuter} also has {@code execute(Runnable r)}, but also has things like {@code schedule(...)},
 * {@code scheduleWithFixedDelay(...)}, ... , which allow it to repeat tasks, or start tasks after a delay.<br/>
 * <br/>
 * <p>
 * These methods might be common knowledge, but it is important to stress the fundamental reason that both the {@code slowExecuter} and
 * {@code fastExecutor} exist; each has a separate thread pool maintaining them. Unique thread pools mapped to a unique type of threaded
 * service. Good for organization, good for resource management.<br/>
 * <br/>
 * <p>
 * Furthermore, with the {@link ServiceProvider}, all of this functionality is wrapped in a class which is responsible for choosing the
 * correct executor service to use.<br/>
 * <br/>
 * <p>
 * For those of you TL;DR nerds: we no longer use a {@link java.util.Timer} object for the {@code fastExecutor}, as its functionality will
 * be deprecated in Java 9, and quite frankly, because it is ancient history when compared to the {@code Executors} framework. We instead
 * use a custom manager called a {@code ServiceProvider} to use the executor services.<br/>
 * <br/>
 * <p>
 * What you used to do with a {@code TimerTask}:<br/>
 *
 * <pre>
 *     CoresManager.fastExecutor.scheduleAtFixedRate(new TimerTask {
 *     		int someStuff;
 *     		boolean stop;
 *
 *     		public void run() { if(stop) cancel(); }
 *
 *     }, delay, freq);
 * </pre>
 * <p>
 * What we do with the {@link ServiceProvider}
 *
 * <pre>
 * CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
 * 	boolean stop;
 *
 * 	public boolean repeat() {
 * 		// do logic
 * 		if (someCondition)
 * 			stop = true;
 * 		else
 * 			stop = false;
 * 		return stop;
 *    }
 * }, 0, 1, TimeUnit.SECONDS);
 * </pre>
 */
public final class CoresManager {

    public static WorldThread worldThread;

    public static SQLThread sqlThread;

    private static ListeningScheduledExecutorService slowExecutor;

    public static ExecutorService fastExecutorV2;

    public static volatile boolean shutdown;

    private static ServiceProvider serviceProvider;

    /** Non-null only in the native 947 JVM; see {@link #initNative950(Native950TickScheduler)}. */
    private static Native950TickScheduler native947Scheduler;

    public static void init() {
        if (native947Scheduler != null) {
            // Fail closed: the 947 world owns the only game thread in its JVM (Native950World).
            throw new IllegalStateException("CoresManager.init() must not start the legacy WorldThread in the native 947 JVM");
        }
        worldThread = new WorldThread();
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        fastExecutorV2 = new FastThreadPoolExecutor(/*availableProcessors >= 12 ? 4 : availableProcessors >= 6 ? 2 :*/ 1,
                new FastThreadFactory(new FastThreadHandler()));
        slowExecutor = MoreExecutors.listeningDecorator(new SlowThreadPoolExecutor(availableProcessors >= 12 ? 4 : availableProcessors >= 6 ? 2 : 1,
                new SlowThreadFactory(new SlowThreadHandler())));
        serviceProvider = new ServiceProvider(false);
        worldThread.start();

        serviceProvider.scheduleAndTrackRepeatingTask(new TrackedRunnable() {
            @Override
            public void run() {
                Logger.getGlobal().info(serviceProvider.log("Service Provider status report"));
                Logger.getGlobal().info("Total requests received since server start: " + serviceProvider.requests);
                Logger.getGlobal().info("Tracked future keys:");
                serviceProvider.trackedFutures.keySet().forEach(Logger.getGlobal()::info);
                Logger.getGlobal().info("Total tracked futures: " + serviceProvider.trackedFutures.size());
                Logger.getGlobal().info(serviceProvider.log("Status: Healthy"));
            }
        }, 30, 3600, TimeUnit.SECONDS);
    }

    /**
     * Schedules a {@link FixedLengthRunnable} task with the slow executor service.
     *
     * @param r - the runnable to be repeated
     * @param startDelay - time delay before first execution
     * @param delayCount - time interval between executions
     * @param unit - the {@link TimeUnit}
     * @deprecated Use {@link ServiceProvider#scheduleFixedLengthTask(FixedLengthRunnable, long, long, TimeUnit)}
     */
    @Deprecated
    public static void scheduleRepeatedTask(final FixedLengthRunnable r, final long startDelay, final long delayCount,
                                            final TimeUnit unit) {
        if (native947Scheduler != null) {
            native947Scheduler.scheduleFixedLengthTask(r, startDelay, delayCount, unit);
            return;
        }
        final Future<?> f = slowExecutor.scheduleWithFixedDelay(r, startDelay, delayCount, unit);
        r.assignFuture(f);
    }

    /**
     * Returns the core's {@code ServiceProvider} used for accessing the executor services.
     *
     * @return the core {@link ServiceProvider}
     */
    public static ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Native 947 initialisation: installs a {@link ServiceProvider} whose every scheduling method
     * delegates to the supplied tick wheel, WITHOUT constructing or starting {@link WorldThread}
     * or the fast/slow thread pools. {@link #init()} is untouched for the legacy 910 server.
     * <p>
     * Idempotent for the same scheduler (returns false); a different scheduler, or a JVM where
     * {@link #init()} already ran, is rejected so the two schedulers can never coexist.
     *
     * @return true when this call installed the provider
     */
    public static synchronized boolean initNative950(final Native950TickScheduler scheduler) {
        Objects.requireNonNull(scheduler, "scheduler");
        if (worldThread != null || slowExecutor != null || fastExecutorV2 != null) {
            throw new IllegalStateException("CoresManager.init() already started the legacy executors; the native 947 scheduler cannot share this JVM");
        }
        if (serviceProvider != null) {
            if (native947Scheduler == scheduler) return false;
            throw new IllegalStateException("CoresManager already has a native 947 scheduler; only one tick wheel may own the world thread");
        }
        native947Scheduler = scheduler;
        serviceProvider = new ServiceProvider.Native950ServiceProvider(scheduler);
        Logger.getGlobal().info("ServiceProvider => native 947 tick wheel installed; legacy WorldThread and pools are not started.");
        return true;
    }

    /** @return true once {@link #initNative950(Native950TickScheduler)} installed the tick wheel */
    public static boolean isNative950() {
        return native947Scheduler != null;
    }

    /** @return the tick wheel behind {@link #getServiceProvider()} in the native JVM, else null */
    public static Native950TickScheduler getNative950Scheduler() {
        return native947Scheduler;
    }

    /**
     * Drains the native tick wheel on the calling thread: game tasks first, then every due
     * scheduled task. Native950World calls this once per 600 ms tick, before
     * {@code WorldTasksManager.processTasks()}, mirroring {@link WorldThread#run()} lines 31-32.
     */
    public static void drainNativeTick() {
        final Native950TickScheduler scheduler = native947Scheduler;
        if (scheduler == null) {
            throw new IllegalStateException("CoresManager.initNative950 has not installed a tick scheduler");
        }
        scheduler.tick();
    }

    public static void shutdown() {
        if (slowExecutor != null) slowExecutor.shutdown();
        if (fastExecutorV2 != null) fastExecutorV2.shutdown();
        shutdown = true;
    }

    private CoresManager() {

    }

    static void purgeSlowExecutor() {
        //((SlowThreadPoolExecutor) slowExecutor).purge();
    }

    /**
     * Serves as a centralized hub for executor services in the context of the game engine. New developers should not have to know which
     * executor to use, but should rather be able to call wrapper methods with generic names and descriptions, and let the
     * {@code ServiceProvider} choose the correct {@link java.util.concurrent.ExecutorService};
     *
     * @author David O'Neill
     */
    public static class ServiceProvider {

        private volatile int taskIndex;
        @Getter
        private final List<TaskTracker> scheduledTasks = new CopyOnWriteArrayList<TaskTracker>();

        private final Map<String, Future<?>> trackedFutures;
        private int requests = 0;
        private final boolean verbose;
        private final Queue<Runnable> gameTasks = new ConcurrentLinkedQueue<>();

        private ServiceProvider(final boolean verbose) {
            trackedFutures = new ConcurrentHashMap<>();
            this.verbose = verbose;
            Logger.getGlobal().info("ServiceProvider active and waiting for requests.");
        }

        public final void dumpTasksInformation() {
			/*try {
				val writer = new BufferedWriter(new FileWriter(new File("Task information.txt")));
				for (int i = scheduledTasks.size() - 1; i >= 0; i--) {
					writer.write(scheduledTasks.get(i).toString());
					writer.newLine();
				}
				writer.close();
			} catch (final Exception e) {
				Logger.getGlobal().catching(e);
			}*/
        }

        void runGameTasks() {
            for (; ; ) {
                Runnable next = gameTasks.poll();
                if (next == null) {
                    break;
                }
                try {
                    next.run();
                } catch (Exception e) {
                    Logger.getGlobal().error("Error running game task!", e);
                }
            }
        }

        public void addGameTask(Runnable action) {
            gameTasks.add(action);
        }

        /**
         * Schedules a {@code Runnable} to be executed after the supplied start delay, and continuously executed thereafter at some
         * specified frequency. This method should be used when there is no intention of stopping the task before server shutdown.<br/>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r a {@link Runnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         * @param unit the specified time unit
         */
        public void scheduleRepeatingTask(final Runnable r, final long startDelay, final long delayCount, final TimeUnit unit) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {
                    //val info = tracker.schedule();
                    r.run();
                    //info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, unit);
            requests++;
        }

        /**
         * Schedules a {@code Runnable} to be executed after the supplied start delay, and continuously executed thereafter at some
         * specified frequency. This method should be used when there is no intention of stopping the task before server shutdown.<br/>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r a {@link Runnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         * @param unit the specified time unit
         */
        public Future<?> scheduleRepeatingTask2(final Runnable r, final long startDelay, final long delayCount, final TimeUnit unit) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            requests++;
            return CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {
                    //	val info = tracker.schedule();
                    r.run();
                    //	info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, unit);
        }

        /**
         * Schedules a {@code Runnable} to be executed after the supplied start delay, and continuously executed thereafter at some
         * specified frequency. This method should be used when there is no intention of stopping the task before server shutdown.<br/>
         * The start delay and repetition frequency time unit is assumed to be {@link TimeUnit#SECONDS}.
         *
         * @param r a {@link Runnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         */
        public void scheduleRepeatingTask(final Runnable r, final long startDelay, final long delayCount) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);

            CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {
                    //	val info = tracker.schedule();
                    r.run();
                    //	info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, TimeUnit.SECONDS);
            requests++;
        }

        /**
         * Schedules a {@link FixedLengthRunnable} to be executed after the supplied start delay, and continuously executed thereafter until
         * {@link FixedLengthRunnable#repeat()} returns false. This method should be used when there is absolute certainty the task will
         * stop executing based on a future condition.<br/>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r a {@link FixedLengthRunnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         * @param unit the specified time unit
         */
        public void scheduleFixedLengthTask(final FixedLengthRunnable r, final long startDelay, final long delayCount,
                                            final TimeUnit unit) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            final Future<?> f = CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {

                    //val info = tracker.schedule();
                    r.run();
                    //info.executionEndTime = System.nanoTime();

                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, unit);
            r.assignFuture(f);
            requests++;
        }

        /**
         * Schedules a {@link FixedLengthRunnable} to be executed after the supplied start delay, and continuously executed thereafter until
         * {@link FixedLengthRunnable#repeat()} returns false. This method should be used when there is absolute certainty the task will
         * stop executing based on a future condition.<br/>
         * The start delay and repetition frequency time unit is assumed to be {@link TimeUnit#SECONDS}.
         *
         * @param r a {@link FixedLengthRunnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         */
        public void scheduleFixedLengthTask(final FixedLengthRunnable r, final long startDelay, final long delayCount) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            final Future<?> f = CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {
                    //	val info = tracker.schedule();
                    r.run();
                    //info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, TimeUnit.SECONDS);
            r.assignFuture(f);
            requests++;
        }

        /**
         * Schedules a {@link TrackedRunnable} to be executed after the supplied start delay, and continuously executes it thereafter at
         * some specified frequency. Furthermore, the associated {@link Future} is registered with the {@code ServiceProvider} via the
         * runnables tracking key. The {@link Future} can then be accessed with the key at a later time. This method should be used when the
         * task will not necessarily be cancelled after a fixed iteration period, but may need to be shutdown at a later, unknown time. In
         * order to retrieve the tracking key, you must have a reference to the {@link TrackedRunnable}, so using an anonymous first
         * argument is discouraged.<br/>
         * If the String key supplied is already registered with the {@code ServiceProvider}, the task will NOT be scheduled!<br/>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r a {@link Runnable} to repeat
         * @param startDelay time delay before execution begins
         * @param delayCount frequency at which the {@code run()} method is called.
         * @param unit the specified time unit
         */
        public void scheduleAndTrackRepeatingTask(final TrackedRunnable r, final long startDelay, final long delayCount,
                                                  final TimeUnit unit) {
            if (trackedFutures.containsKey(r.getTrackingKey())) {
                Logger.getGlobal().warn(log("Attempted to add Future to tracking map, but duplicate key was found. Aborting."));
                return;
            }
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            final Future<?> future = CoresManager.slowExecutor.scheduleWithFixedDelay(() -> {
                try {
                    //val info = tracker.schedule();
                    r.run();
                    //info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, delayCount, unit);
            trackedFutures.put(r.getTrackingKey(), future);
            if (verbose) {
                Logger.getGlobal().info(log("Tracking new future with key: " + r.getTrackingKey()));
            }
            requests++;
        }

        /**
         * Attempts to retrieve a {@link Future} mapped to the supplied key. If the {@link Future} is present in the {@code ServiceProvider}
         * mapping, it will be cancelled and purged from the executor pool.
         *
         * @param key the String key (acquired via {@link TrackedRunnable#getTrackingKey()} to lookup a mapped {@link Future}
         * @param interrupt whether or not the executor service should stop the current execution of the {@link Future}'s associated
         * {@link Runnable} if an execution is in progress.
         */
        public void cancelTrackedTask(final String key, final boolean interrupt) {
            final Future<?> future = trackedFutures.remove(key);
            if (future != null) {
                future.cancel(interrupt);
                CoresManager.purgeSlowExecutor();
                if (verbose) {
                    Logger.getGlobal().info(log("Cancelled future with key: " + key));
                }
            }
        }

        /**
         * Schedules a {@code Runnable} for a one-time execution, but only after a specified start delay. The start delay time unit must be
         * supplied.
         *
         * @param r a {@link Runnable} to execute once
         * @param startDelay time delay before execution begins
         * @param unit the specified time unit
         */
        public void executeWithDelay(final Runnable r, final long startDelay, final TimeUnit unit) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            CoresManager.slowExecutor.schedule(() -> {
                try {
                    //	val info = tracker.schedule();
                    r.run();
                    //	info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, startDelay, unit);
            requests++;
        }

        /**
         * Schedules a {@code Runnable} for a one-time execution, but only after a specified start delay. The start delay time unit is
         * "ticks" by default, meaning units of 600ms. Calling {@code executeWithDelay(() -> stuff(), 2);} would execute {@code stuff()}
         * after 2 ticks = 600 ms * 2 = 1200 ms.
         *
         * @param r a {@link Runnable} to execute once
         * @param ticks the time delay in ticks before execution begins
         */
        public void executeWithDelay(final Runnable r, final int ticks) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            CoresManager.slowExecutor.schedule(() -> {
                try {
                    //	val info = tracker.schedule();
                    r.run();
                    //info.executionEndTime = System.nanoTime();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }, ticks * 600, TimeUnit.MILLISECONDS);
            requests++;
        }

        /**
         * Immediately (as soon as a thread from the thread pool is provided) performs a one-time exeuction of a supplied {@code Runnable}.
         *
         * @param r a {@link Runnable} to execute once
         */
        public Future<?> executeNow(final Runnable r) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            try {
                return CoresManager.slowExecutor.submit(() -> {
                    try {
                        //	val info = tracker.schedule();
                        r.run();
                        //	info.executionEndTime = System.nanoTime();
                    } catch (final Exception e) {
                        Logger.getGlobal().catching(e);
                    }
                });
            } finally {
                requests++;
            }
        }

        public Future<?> runNow(final Runnable r) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            try {
                return CoresManager.slowExecutor.submit(r);
            } finally {
                requests++;
            }
        }

        public <T> ListenableFuture<T> submitNow(final Callable<T> r) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            try {
                return CoresManager.slowExecutor.submit(r);
            } finally {
                requests++;
            }
        }

        public <T> void submitNow(final Callable<T> r, Consumer<T> result) {
            //val tracker = new TaskTracker(new Exception("Stack trace: " + taskIndex), taskIndex++);
            try {
                val future = CoresManager.slowExecutor.submit(r);
                future.addListener(() -> {
                    try {
                        result.accept(future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.getGlobal().catching(e);
                    }
                }, slowExecutor);
            } finally {
                requests++;
            }
        }

        private String log(final String message) {
            final String prefix = "ServiceProvider => ";
            return prefix + message;
        }

        /**
         * {@link ServiceProvider} for the native 947 JVM. Every public scheduling method is
         * overridden to delegate to the {@link Native950TickScheduler}; nothing here touches the
         * (null) executor pools. Native950TickSchedulerTest asserts by reflection that every
         * non-final method declared on {@link ServiceProvider} is overridden, so a method added
         * to the parent without a delegate fails the test instead of NPE-ing on the pool.
         */
        static final class Native950ServiceProvider extends ServiceProvider {

            private final Native950TickScheduler wheel;

            Native950ServiceProvider(final Native950TickScheduler wheel) {
                super(false);
                this.wheel = Objects.requireNonNull(wheel, "wheel");
            }

            Native950TickScheduler wheel() {
                return wheel;
            }

            @Override
            public List<TaskTracker> getScheduledTasks() {
                return super.getScheduledTasks();
            }

            @Override
            void runGameTasks() {
                wheel.runGameTasks();
            }

            @Override
            public void addGameTask(final Runnable action) {
                wheel.addGameTask(action);
            }

            @Override
            public void scheduleRepeatingTask(final Runnable r, final long startDelay, final long delayCount, final TimeUnit unit) {
                wheel.scheduleRepeatingTask(r, startDelay, delayCount, unit);
            }

            @Override
            public Future<?> scheduleRepeatingTask2(final Runnable r, final long startDelay, final long delayCount, final TimeUnit unit) {
                return wheel.scheduleRepeatingTask2(r, startDelay, delayCount, unit);
            }

            @Override
            public void scheduleRepeatingTask(final Runnable r, final long startDelay, final long delayCount) {
                wheel.scheduleRepeatingTask(r, startDelay, delayCount);
            }

            @Override
            public void scheduleFixedLengthTask(final FixedLengthRunnable r, final long startDelay, final long delayCount,
                                                final TimeUnit unit) {
                wheel.scheduleFixedLengthTask(r, startDelay, delayCount, unit);
            }

            @Override
            public void scheduleFixedLengthTask(final FixedLengthRunnable r, final long startDelay, final long delayCount) {
                wheel.scheduleFixedLengthTask(r, startDelay, delayCount);
            }

            @Override
            public void scheduleAndTrackRepeatingTask(final TrackedRunnable r, final long startDelay, final long delayCount,
                                                      final TimeUnit unit) {
                wheel.scheduleAndTrackRepeatingTask(r, startDelay, delayCount, unit);
            }

            @Override
            public void cancelTrackedTask(final String key, final boolean interrupt) {
                wheel.cancelTrackedTask(key, interrupt);
            }

            @Override
            public void executeWithDelay(final Runnable r, final long startDelay, final TimeUnit unit) {
                wheel.executeWithDelay(r, startDelay, unit);
            }

            @Override
            public void executeWithDelay(final Runnable r, final int ticks) {
                wheel.executeWithDelay(r, ticks);
            }

            @Override
            public Future<?> executeNow(final Runnable r) {
                return wheel.executeNow(r);
            }

            @Override
            public Future<?> runNow(final Runnable r) {
                return wheel.runNow(r);
            }

            @Override
            public <T> ListenableFuture<T> submitNow(final Callable<T> r) {
                return wheel.submitNow(r);
            }

            @Override
            public <T> void submitNow(final Callable<T> r, final Consumer<T> result) {
                wheel.submitNow(r, result);
            }
        }

        @Data
        public static final class TaskTracker {

            public TaskTracker(final Exception exception, final int index) {
                this.exception = exception;
                elements = exception.getStackTrace();
                id = index;
                CoresManager.getServiceProvider().scheduledTasks.add(this);
            }

            private final int id;
            private final Exception exception;
            private final StackTraceElement[] elements;
            private final List<TaskInformation> information = new ArrayList<>();

            private TaskInformation schedule() {
                val info = new TaskInformation();
                information.add(info);
                return info;
            }

            @Override
            public String toString() {
                val builder = new StringBuilder(1000);
                builder.append("Stacktrace for task " + id + "\n");
                for (val element : elements) {
                    builder.append(element.getClassName() + ", " + element.getMethodName() + ", " + element.getLineNumber() + "\n");
                }
                builder.append("Information: \n");
                for (val info : information) {
                    builder.append(info.toString() + "\n");
                }
                builder.append("\n");
                return builder.toString();
            }

            private static final class TaskInformation {

                public TaskInformation() {
                    executionStartTime = System.nanoTime();
                    val runtime = Runtime.getRuntime();
                    maxMemory = runtime.maxMemory();
                    memoryAllocated = runtime.totalMemory();
                    memoryFree = runtime.freeMemory();
                }

                private final long maxMemory, memoryAllocated, memoryFree;
                private final long executionStartTime;
                private long executionEndTime;

                @Override
                public String toString() {
                    return "Memory: M/A/F " + maxMemory + " / " + memoryAllocated + " / " + memoryFree + "\n"
                            + "Start: " + executionStartTime + "\nEnd: " + executionEndTime;
                }

            }

        }

    }
}