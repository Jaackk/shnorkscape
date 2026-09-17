package com.rs.game.tasks;

import com.rs.utils.Logger;
import lombok.val;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldTasksManager {

    private static final Queue<WorldTaskInformation> activeTasks = new ConcurrentLinkedQueue<>();
    private static final Queue<WorldTaskInformation> ready = new ConcurrentLinkedQueue<>();

    public static void processTasks() {
        Iterator<WorldTaskInformation> it = activeTasks.iterator();
        while (it.hasNext()) {
            WorldTaskInformation next = it.next();
            try {
                if (next.task.needRemove || next.continueCount == -1) {
                    it.remove();
                    continue;
                }
                if (next.continueCount > 0) {
                    next.continueCount--;
                    continue;
                }
                ready.add(next);
                next.continueCount = next.continueMaxCount;
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }

        // We need this so that tasks can be scheduled within tasks. Doing so without the ready queue will result in
        // a ConcurrentModificationException.
        for (; ; ) {
            WorldTaskInformation next = ready.poll();
            if (next == null) {
                break;
            }
            try {
                next.task.run();
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public static void schedule(final WorldTask task) {
        if (task == null) {
            return;
        }
        val info = new WorldTaskInformation(task, 0, -1);
        task.taskInfo = info;
        activeTasks.add(info);
    }

    public static void schedule(final WorldTask task, final int delayCount) {
        if (task == null || delayCount < 0) {
            return;
        }
        val info = new WorldTaskInformation(task, delayCount, -1);
        task.taskInfo = info;
        activeTasks.add(info);
    }

    public static void schedule(final WorldTask task, final int delayCount, final int periodCount) {
        if (task == null || delayCount < 0 || periodCount < 0) {
            return;
        }
        val info = new WorldTaskInformation(task, delayCount, periodCount);
        task.taskInfo = info;
        activeTasks.add(info);
    }

    public static int getTasksCount() {
        return activeTasks.size();
    }

    public static final class WorldTaskInformation {

        private final WorldTask task;
        private final int continueMaxCount;
        private int continueCount;

        public WorldTaskInformation(final WorldTask task, final int continueCount, final int continueMaxCount) {
            this.task = task;
            this.continueCount = continueCount;
            this.continueMaxCount = continueMaxCount;
        }

        @Override
        public String toString() {
            return "Task: " + task + " (" + task.getClass().getCanonicalName() + ") - cont max count: " + continueMaxCount + " - curr count: " + continueCount;
        }

        public void setRemainingTicks(int amount) {
            this.continueCount = amount;
        }

        public int getRemainingTicks() {
            return continueCount;
        }

        public int getTickDelay() {
            return continueMaxCount;
        }
    }
}