package com.rs.game.player.actions.divination.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.WoodcuttingRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.HatchetDefinitions;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Divine location Woodcutting.
 *
 * @author Noel
 */
public final class DivineWoodcutting extends Action {

    private final WorldObject tree;
    private final DivineTreeDefinitions definitions;
    private boolean usingBeaver;
    private HatchetDefinitions defs;

    public DivineWoodcutting(WorldObject tree, DivineTreeDefinitions definitions) {
        this.tree = tree;
        this.definitions = definitions;
    }

    @Override
    public boolean start(Player player) {
        defs = Woodcutting.getHatchetDefinitions(player, false);
        if (!checkAll(player))
            return false;
        setActionDelay(player, getWoodcuttingDelay(player));
        return true;
    }

    private int getWoodcuttingDelay(Player player) {
        int summoningBonus = player.getFamiliar() != null
                ? (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 10 : 0 : 0;
        int wcTimer = definitions.getLogBaseTime() - (player.getSkills().getLevel(8) + summoningBonus)
                - Utils.getRandom(defs.getAxeTime());
        if (wcTimer < 1 + definitions.getLogRandomTime())
            wcTimer = 1 + Utils.getRandom(definitions.getLogRandomTime());
        wcTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
        return wcTimer;
    }

    private boolean checkAll(Player player) {
        Player owner = tree.getOwner();
        if (owner == null)
            owner = player;
        if (!DivineObject.canUseDivineObj(player, owner)) {
            return false;
        }
        if (defs == null) {
            player.getPackets().sendObjectMessage(0, 15263739, tree,
                    "You need a hatchet to chop down the " + tree.getDefinitions().name + ".");
            return false;
        }
        if (!hasWoodcuttingLevel(player))
            return false;
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return false;
        }
        return true;
    }

    private boolean hasWoodcuttingLevel(Player player) {
        if (definitions.getLevel() > player.getSkills().getLevel(Skills.WOODCUTTING)) {
            player.sendMessage(
                    "You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
            return false;
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        player.setNextAnimation(new Animation(usingBeaver ? 1 : defs.getEmoteId()));
        if (Utils.random(1000) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new WoodcuttingRandomEvent(player, player);
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>A Nature Spirit emerges from the tree.");
                }
            }
        }
        player.faceObject(tree);
        if (!DivineObject.canHarvest(player)) {
            player.setNextAnimation(new Animation(-1));
            return false;
        }
        return checkTree(player);
    }

    @Override
    public int processWithDelay(Player player) {
        addLog(player);
        if (player.getSkillingTask() == 8) {
            if (player.getDailyManager().getTask() != null) {
                if (definitions.getLogsId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }
        }
        if (!player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return -1;
        }
        return getWoodcuttingDelay(player);
    }

    private void addLog(Player player) {
        Item hItem = new Item(definitions.getLogsId(), 1);
        int roll = Utils.random(100);
        Player owner = tree.getOwner();
        String logName = ItemDefinitions.getItemDefinitions(definitions.getLogsId()).getName().toLowerCase();
        player.getInventory().addItem(hItem);
        player.getSkills().addXp(Skills.WOODCUTTING, definitions.getXp() * Woodcutting.woodcuttingSet(player));
        player.addLogsChopped();
        player.sendMessage("You get some " + hItem.getName().toLowerCase() + "; total chopped: " + Colors.RED
                + Utils.getFormattedNumber(player.getLogsChopped()) + "</col>.", true);
        DivineObject.handleHarvest(player);
        if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
            if (Utils.random(100) >= 75)
                owner.gathered++;
            if (!hItem.getDefinitions().isStackable())
                hItem.setId(hItem.getDefinitions().getCertId());
            owner.getInventory().addItem(hItem);
            owner.sendMessage(player.getDisplayName() + " chopped some " + logName + " for you.", true);
        }
        DeathsBounty.dropUrn(player);
    }

    private boolean checkTree(Player player) {
        return World.containsObjectWithId(tree, tree.getId());
    }

    @Override
    public void stop(Player player) {
        setActionDelay(player, 3);
    }

    public enum DivineTreeDefinitions {

        DIVINE_MAGIC(75, 250, 1513, 5, 5),

        DIVINE_YEW(60, 175, 1515, 5, 5),

        DIVINE_MAPLE(45, 100, 1517, 5, 5),

        DIVINE_WILLOW(30, 67.5, 1519, 5, 5),

        DIVINE_OAK(15, 37.5, 1521, 5, 5),

        DIVINE_NORMAL(1, 25, 1511, 5, 5);

        private final int level;
        private final double xp;
        private final int logsId;
        private final int logBaseTime;
        private final int logRandomTime;

        DivineTreeDefinitions(int level, double xp, int logsId, int logBaseTime, int logRandomTime) {
            this.level = level;
            this.xp = xp;
            this.logsId = logsId;
            this.logBaseTime = logBaseTime;
            this.logRandomTime = logRandomTime;
        }

        public int getLevel() {
            return level;
        }

        public double getXp() {
            return xp;
        }

        public int getLogsId() {
            return logsId;
        }

        public int getLogBaseTime() {
            return logBaseTime;
        }

        public int getLogRandomTime() {
            return logRandomTime;
        }
    }
}