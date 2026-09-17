package com.rs.game.activites.dnd.eviltree;

import com.rs.Settings;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeObject;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Handles the global {@link EvilTree} instance.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeHandler {

    private static final class SpawnTask extends WorldTask {
        private static final int EXPIRE_RATE = Settings.TEST_SERVER_MODE ? 250 : 1500; // 2.5min/15mins.
        private WorldTask expireTask;

        @Override
        public void run() {
            if (expireTask != null)
                return;
            if (isAlive()) {
                EvilTreeObject treeObject = current.getTreeObject();
                World.sendGraphics(null, new Graphics(665, 0, 100), treeObject);
                EvilTree.sendAllMessage("The Evil Tree will be consumed by lightning if it's not killed soon!");
                int healthPercent = treeObject.getHealthPercent();
                if(healthPercent > 50) {
                   treeObject.reduceHealthPercent(50);
                } else if(healthPercent > 25) {
                    treeObject.reduceHealthPercent(25);
                }
                expireTask = new WorldTask() {
                    @Override
                    public void run() {
                        stop();
                        expireTask = null;
                        if (current.getTreeObject() == null || current.getTreeObject().isDead())
                            return;
                        current.getTreeObject().kill();
                        World.sendGraphics(null, new Graphics(665, 0, 100), current.getTreeObject());
                        EvilTree.sendAllMessage("The Evil Tree was consumed by lightning!");
                    }
                };
                WorldTasksManager.schedule(expireTask, EXPIRE_RATE);
            } else {
                spawn();
            }
        }
    }

    private static final boolean ENABLED = true;
    private static final int SPAWN_RATE = Settings.TEST_SERVER_MODE ? 1000 : 21_300; // 10 minutes/4 hours.
    private static EvilTree current;
    private static SpawnTask spawnTask;

    public static void start() {
        if (!ENABLED)
            return;
        if (spawnTask != null) {
            throw new IllegalStateException("The EvilTreeHandler has already been started.");
        }
        spawnTask = new SpawnTask();
        NPC npc = new NPC(13790, new WorldTile(5026, 743, 1), -1, false);
        npc.setDirection(Utils.getAngle(-1, 0));
        WorldTasksManager.schedule(spawnTask, SPAWN_RATE, SPAWN_RATE);
    }

    public static void clearDamage(Player player, EvilTree tree) {
        if (tree != null && tree.isTree()) {
            current.getTreeObject().clearDamage(player);
            player.sendMessage(Colors.RED + "The Evil Tree Hunter is furious that you perished while fighting the tree. He has reset your reward potential.");
        }
    }

    public static String getFullStatus() {
        if (isAlive()) {
            if(EvilTreeHandler.isActive()) {
                return Colors.WHITE + "- HP: " + EvilTreeHandler.getHP() + "<br>" +
                        Colors.WHITE + "- Type: " + EvilTreeHandler.getTreeType() + "<br>" +
                        Colors.WHITE + "- Location: " + EvilTreeHandler.getDescription() + "<br>" +
                        Colors.WHITE + "- Expires in: " + EvilTreeHandler.getTimeRemaining() + "<br>";
            } else {
                return Colors.WHITE + "- Status: " + EvilTreeHandler.getState() + "<br>" +
                        Colors.WHITE + "- Type: " + EvilTreeHandler.getTreeType() + "<br>" +
                        Colors.WHITE + "- Location: " + EvilTreeHandler.getDescription() + "<br>" +
                        Colors.WHITE + "- Expires in: " + EvilTreeHandler.getTimeRemaining() + "<br>";
            }
        }
        return Colors.WHITE + "- Next in: " + getNextIn() + "<br>";
    }

    public static String getTimeRemaining() {
        if (isAlive()) {
            long nanos;
            if (spawnTask.expireTask == null) {
                nanos = TimeUnit.NANOSECONDS.convert((spawnTask.getTaskInfo().getRemainingTicks() + SpawnTask.EXPIRE_RATE) * 600,
                        TimeUnit.MILLISECONDS);
                return Colors.GREEN + LocalTime.ofNanoOfDay(nanos).format(FORMATTER);
            } else {
                nanos = TimeUnit.NANOSECONDS.convert(spawnTask.expireTask.getTaskInfo().getRemainingTicks() * 600,
                        TimeUnit.MILLISECONDS);
                return Colors.GREEN + LocalTime.ofNanoOfDay(nanos).format(FORMATTER);
            }
        }
        throw new IllegalStateException("Not alive.");
    }

    public static String getNextPhaseIn(EvilTree tree) {
        if (tree.isAlive() && tree.isSapling()) {
            if (tree.getGrowthTask() == null)
                return "";
            long nanos = TimeUnit.NANOSECONDS.convert(tree.getGrowthTask().getTaskInfo().getRemainingTicks() * 600,
                    TimeUnit.MILLISECONDS);
            return Colors.GREEN + LocalTime.ofNanoOfDay(nanos).format(FORMATTER);
        }
        throw new IllegalStateException("Not alive.");
    }

    public static boolean isActive() {
        return current != null && current.isTree();
    }

    public static boolean isAlive() {
        return current != null && current.isAlive();
    }

    public static void startInstance(Player player, EvilTreeType type) {
        InstanceSettings settings = new InstanceSettings(BossInstanceHandler.Boss.Evil_Tree);
        settings.setMaxPlayers(4);
        settings.setProtection(BossInstance.FFA);
        settings.setCreationTime(Utils.currentTimeMillis());
        BossInstanceHandler.createInstance(player, settings);
    }

    public static String getState() {
        if (current != null) {
            if (current.getSaplingObject() != null) {
                return Colors.GREEN + "Alive (Sapling)";
            } else if (current.getTreeObject() != null) {
                if (current.getTreeObject().isDead()) {
                    return Colors.RED + "Dead (Tree)";
                } else {
                    return Colors.GREEN + "Alive (Tree)";
                }
            }
        }
        return Colors.RED + "N/A";
    }

    public static String getHP () {
        return current != null && current.isTree() ? Colors.GREEN + current.getTreeObject().getHealthPercent() +"%" : Colors.RED+"N/A";
    }
    public static String getTreeType() {
        return current != null ? Colors.GREEN + current.getType().formattedName : Colors.RED + "N/A";
    }

    public static String getDescription() {
        return current != null ? Colors.GREEN + current.getLocation().formattedDescription : Colors.RED + "N/A";
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String getNextIn() {
        if (spawnTask == null) {
            return Colors.RED + "Disabled";
        }
        if (current != null && current.isAlive()) {
            throw new IllegalStateException("Tree already exists.");
        }
        long addNanos = TimeUnit.NANOSECONDS.convert(spawnTask.getTaskInfo().getRemainingTicks() * 600, TimeUnit.MILLISECONDS);
        return Colors.GREEN + LocalTime.ofNanoOfDay(addNanos).format(FORMATTER);
    }

    public static EvilTree current() {
        return current;
    }

    public static void spawnNow() {
        spawnTask.getTaskInfo().setRemainingTicks(1);
    }

    private static void spawn() {
        if (current != null && current.getSaplingObject() != null) {
            Logger.getGlobal().warn("Spawn failed! Evil tree sapling persisting too long.");
            return;
        }
        EvilTreeLocation newLocation = EvilTreeLocation.getRandom();
        EvilTreeType newType = EvilTreeType.getRandom();
        current = new EvilTree(newLocation, newType);
        current.spawnSapling();
        EvilTree.sendAllMessage("An Evil Tree [" + newType.formattedName + "] is growing " + newLocation.description + ".");
    }
}