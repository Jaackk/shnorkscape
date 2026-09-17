package com.rs.game.activites.dnd.eviltree.entity;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeRewards;
import com.rs.game.activites.dnd.eviltree.EvilTreeType;
import com.rs.game.activites.dnd.eviltree.action.ChopEvilTreeAction;
import com.rs.game.activites.dnd.eviltree.action.LightEvilTreeAction;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.rs.game.activites.dnd.eviltree.entity.EvilRootObject.RootSpot;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeObject extends WorldObject {

    private static final int DEATH_TIMER = Settings.TEST_SERVER_MODE ? 250 : 1000;
    private static final int CHOP_AMT = 25;
    private static final int LIGHT_AMT = 50;

    private static int getIdForState(EvilTree tree, int id, int state) {
        EvilTreeType type = tree.getType();
        switch (state) {
            case 0: // 100% HP.
                return type.aliveId;
            case 3: // Dead.
                return type.lootId;
            default:
                return id;
        }
    }

    private final EvilTree tree;
    private final int state;
    private final EvilTreeRewards rewards;
    private int hitpoints;
    private final int totalFighters;
    private final Multiset<String> damageSet;
    private WorldTask deathTask;

    private EvilTreeObject(EvilTree tree, int oldId, int hitpoints, int totalFighters, int state, Multiset<String> damageSet) {
        super(getIdForState(tree, oldId, state), 10, 0, tree.getTreeTile());
        this.tree = tree;
        this.hitpoints = hitpoints;
        this.totalFighters = totalFighters;
        this.state = state;
        this.damageSet = damageSet;
        rewards = new EvilTreeRewards(tree);
    }

    public EvilTreeObject(EvilTree tree) {
        this(tree, tree.getType().aliveId, tree.getType().hitpoints, 0, 0, HashMultiset.create());
    }

    public void chop(Player player) {
        player.getActionManager().setAction(new ChopEvilTreeAction(tree, this));
    }

    public void lightFire(Player player) {
        player.getActionManager().setAction(new LightEvilTreeAction(tree, this));
    }

    public void inspect(Player player) {
        if (isDead()) {
            if (deathTask != null) {
                long minutesLeft = TimeUnit.MINUTES.convert(deathTask.getTaskInfo().getRemainingTicks() * 600,
                        TimeUnit.MILLISECONDS);
                String text = minutesLeft > 1 ? minutesLeft + " minutes" : "a few seconds";
                player.sendMessage(Colors.DARK_RED + "This Evil Tree is dead. You have " + text + " to claim your loot before it rots away.");
            } else {
                player.sendMessage("The Evil Tree is dead.");
            }
        } else {
            player.sendMessage(Colors.DARK_RED + "The Evil Tree has " + getHealthPercent() + "% of its health left.");
        }
    }

    public void giveRewards(Player player) {
        rewards.giveRewards(player);
    }

    public void kill() {
        if (deathTask == null) {
            tree.stopTreeRestore();
            deathTask = new WorldTask() {
                @Override
                public void run() {
                    tree.clear();
                    stop();
                }
            };
            hitpoints = 0;
            tree.clearRoots();
            tree.clearFire();
            updateState(3);
            WorldTasksManager.schedule(deathTask, DEATH_TIMER);
        }
    }

    public void killAndClear() {
        if (deathTask != null) {
            deathTask.stop();
        }
        hitpoints = 0;
        tree.clear();
    }

    public void registerLightAction(Player player) {
        if (isDead() || checkRoots(player)) {
            return;
        }
        int amount = player.bloodSerumDelay > 0 ? LIGHT_AMT * 2 : LIGHT_AMT;
        decrementDamage(player, amount);
        if (getHealthPercent() <= 60) {
            tree.setOnFire();
        }
    }

    public void registerChopAction(Player player) {
        if (isDead() || checkRoots(player)) {
            return;
        }
        int amount = player.bloodSerumDelay > 0 ? CHOP_AMT * 2 : CHOP_AMT;
        decrementDamage(player, amount);
    }

    private void decrementDamage(Player player, int amount) {
        if (player.instantKillEvilTree) {
            amount = 10_000;
        }
        if (amount > hitpoints) {
            amount = hitpoints;
        }
        if (removeHitpoints(amount)) {
            damageSet.add(player.getUsername(), amount);
            EvilTree.updateInter(player, tree);
        }
    }

    private boolean onRoot(Player player, RootSpot spot) {
        EvilRootObject root = tree.getRoot(spot);
        if (root == null)
            return false;
        return player.withinDistance(root, root.getDefinitions().getSize());
    }

    public boolean checkRoots(Player player) {
        int xDiff = player.getX() - tree.getTreeTile().getX();
        int yDiff = player.getY() - tree.getTreeTile().getY();
        int repelDist = 1;
        String msg = "You are stunned by the roots!";
        int chance = player.bloodSerumDelay > 0 ? (ThreadLocalRandom.current().nextBoolean() ? 22 : 23) : 15;
        if (ThreadLocalRandom.current().nextInt(chance) == 0) {
            if (yDiff > 0 || onRoot(player, RootSpot.NORTH)) {
                if (tree.getNorthRoot() != null && tree.getNorthRoot().isAlive()) {
                    player.sendMessage(msg);
                    tree.stunAndRepel(player, repelDist);
                    return true;
                }
            } else if (yDiff < 0 || onRoot(player, RootSpot.SOUTH)) {
                if (tree.getSouthRoot() != null && tree.getSouthRoot().isAlive()) {
                    player.sendMessage(msg);
                    tree.stunAndRepel(player, repelDist);
                    return true;
                }
            } else if (xDiff > 0 || onRoot(player, RootSpot.EAST)) {
                if (tree.getEastRoot() != null && tree.getEastRoot().isAlive()) {
                    player.sendMessage(msg);
                    tree.stunAndRepel(player, repelDist);
                    return true;
                }
            } else if (xDiff < 0 || onRoot(player, RootSpot.WEST)) {
                if (tree.getWestRoot() != null && tree.getWestRoot().isAlive()) {
                    player.sendMessage(msg);
                    tree.stunAndRepel(player, repelDist);
                    return true;
                }
            }
        }
        return false;
    }

    public void clearDamage(Player player) {
        damageSet.remove(player.getUsername(), Integer.MAX_VALUE);
    }

    public boolean removeHitpoints(int amount) {
        if (deathTask != null || hitpoints <= 0) {
            return false;
        }
        hitpoints -= amount;
        if (hitpoints <= 0) {
            kill();
            return true;
        }
        int hpPercent = getHealthPercent();
        if (hpPercent <= 25 && state == 1) {
            updateState(2);
        } else if (hpPercent <= 60 && state == 0) {
            updateState(1);
        }
        return true;
    }

    private void updateState(int newState) {
        if (newState > state) {
            World.removeObject(this);

            val treeObject = new EvilTreeObject(tree, getId(), hitpoints, damageSet.entrySet().size(), newState, damageSet);
            tree.setTreeObject(treeObject);
            World.spawnObject(treeObject);
        }
    }

    public int getHealthPercent() {
        double currentHp = hitpoints;
        double maxHp = tree.getType().hitpoints;
        double res = (currentHp / maxHp) * 100.0;
        int intRes = (int) res;
        if (!isDead() && intRes <= 0) {
            intRes = 1;
        }
        return intRes;
    }

    public void reduceHealthPercent(double newHealthPercent) {
        double mod = newHealthPercent / 100.0;
        double maxHp = tree.getType().hitpoints;
        int newHp = (int) (maxHp * mod);
        int diff = hitpoints - newHp;
        if (diff > 0) {
            removeHitpoints(diff);
        }
    }

    public Multiset<String> getDamageSet() {
        return damageSet;
    }

    public int getTotalFighters() {
        if (totalFighters == -1)
            throw new IllegalStateException("Hasn't been initialized yet.");
        return totalFighters;
    }

    public int getState() {
        return state;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public EvilTreeRewards getRewards() {
        return rewards;
    }

    public boolean isDead() {
        return hitpoints <= 0;
    }
}
