package com.rs.game.player.actions.firemaking;

import com.rs.Settings;
import com.rs.game.player.client.Native950Firemaking;
import com.rs.game.Animation;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.duel.DuelControler;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.others.randomevent.impl.FiremakingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.Firelighter;
import com.rs.game.player.content.skillingcontracts.impl.FiremakingContractList;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.InventoryOptionsHandler;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Firemaking.java | 11:27:22 AM
 *
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class Firemaking extends Action {

    private final Log log;
    private Firelighter fl;
    private Native950Firemaking.Attempt native950Attempt;
    private static final WorldTile southWest = new WorldTile(5002, 704, 1);
    private static final WorldTile northEast = new WorldTile(5041, 757, 1);

    public Firemaking(Log log) {
        this.log = log;
    }

    public Firemaking(Log log, Firelighter fl) {
        this.log = log;
        this.fl = fl;
    }

    public static double increasedExperience(Player player, double totalXp) {
        if (player.getEquipment().getGlovesId() == 13660)
            totalXp *= 1.02;
        if (player.getEquipment().getRingId() == 13659)
            totalXp *= 1.02;
        if (player.getEquipment().getGlovesId() == 13660 && player.getEquipment().getRingId() == 13659)
            totalXp *= 1.01;
        if (Wilderness.isAtWild(player) && player.getEquipment().getGlovesId() == 13851)
            totalXp *= 1.01;
        return totalXp;
    }

    public static boolean isLog(int itemId) {
        for (Log log : Log.values()) {
            if (log.getLogId() == itemId)
                return true;
        }

        return false;
    }

    public static boolean isFiremaking(Player player, int logId) {
        for (Log log : Log.values()) {
            if (log.getLogId() == logId) {
                player.getActionManager().setAction(new Firemaking(log));
                return true;
            }
        }
        return false;
    }

    public static boolean isFiremaking(Player player, int logId, Firelighter fl) {
        if (fl == null)
            return false;
        for (Log log : Log.values()) {
            if (log.getLogId() == logId) {
                player.getActionManager().setAction(new Firemaking(log, fl));
                return true;
            }
        }
        return false;
    }

    public static boolean isFiremaking(Player player, Item item1, Item item2) {
        Item log = InventoryOptionsHandler.contains(590, item1, item2);
        if (log == null)
            return false;
        return isFiremaking(player, log.getId());
    }

    public static void startFamiliarFire(Player player, Familiar familiar, Log log) {
        if (player.getFamiliar().getId() == 7378 || player.getFamiliar().getId() == 7377) {
            // TODO
        }
    }

    public boolean checkAll(Player player) {
        if (player.isNative950()) return native950Attempt != null && native950Attempt.process();
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("pyro-matic") || weapon.getName().toLowerCase().contains("crystal tinderbox"));

        if (!hasAugmentedTool && !player.getInventory().containsItem(590, 1) && !player.getToolBelt().contains(590)) {
            player.sendMessage("You do not have the required items to light this.");
            return false;
        }

        if (player.getSkills().getLevel(Skills.FIREMAKING) < log.getLevel()) {
            player.sendMessage("You do not have the required level to light this.");
            return false;
        }
        if (!World.canMoveNPC(player.getPlane(), player.getX(), player.getY(), 1)
                || World.getObjectWithSlot(player, Region.OBJECT_SLOT_FLOOR) != null
                || !locationAllowsFire(player)) {
            player.sendMessage("You can't light a fire here; find a different area.");
            return false;
        }
        return true;
    }

    /** Ordinary location restrictions shared with the native action boundary. */
    public static boolean locationAllowsFire(Player player) {
        if (player.getControlerManager().getControler() instanceof DuelArena
                || player.getControlerManager().getControler() instanceof DuelControler
                || player.getRegionId() == 15174 || player.getRegionId() == 16475
                || player.getRegionId() == Settings.MARKET_REGION_ID || player.getRegionId() == 9770
                || player.getRegionId() == 9265 || player.getRegionId() == 14388 || player.getRegionId() == 9282)
            return false;
        return !(player.getX() >= southWest.getX() && player.getX() <= northEast.getX()
                && player.getY() >= southWest.getY() && player.getY() <= northEast.getX()
                && player.getPlane() == southWest.getPlane());
    }

    @Override
    public boolean process(Player player) {
        if (player.isNative950()) return checkAll(player);
        if (Utils.random(350) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new FiremakingRandomEvent(player, player);
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>A fire spirit emerges from the bonfire.");
                }
            }
        }
        return checkAll(player);
    }

    @Override
    public int processWithDelay(final Player player) {
        if (player.isNative950()) {
            if (native950Attempt != null) native950Attempt.finish();
            return -1;
        }
        if (player.getSkillingTask() == 11) {
            if (player.getDailyManager().getTask() != null) {
                if (log.getLogId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }
        }
        final WorldTile tile = new WorldTile(player);
        if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1))
            if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1))
                if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1))
                    player.addWalkSteps(player.getX(), player.getY() - 1, 1);

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                final FloorItem item = World.getRegion(tile.getRegionId()).getGroundItem(log.getLogId(), tile, player);
                if (item == null)
                    return;
                if (!World.removeGroundItem(player, item, false))
                    return;
                // player.getPackets().sendSound(2594, 0, 1); //TODO find fire
                // sound

                int fireId = fl == null ? log.getFireId() : fl.fireId;
                if (fl != null) {
                    player.getInventory().deleteItem(fl.itemId, 1);
                }
                World.spawnTempGroundObject(new WorldObject(fireId, 10, 0, tile.getX(), tile.getY(), tile.getPlane()), 592, log.getLife(), true);
                FiremakingContractList.listen(player, log);
                if (log == Log.PROTEAN) {
                    double xp = increasedExperience(player, player.getSkills().getLevelForXp(Skills.FIREMAKING) * 7.7);
                    Perk tinker = tinderBox != null ? player.getInventionManager().hasPerk(tinderBox, Perks.TINKER) : null;
                    boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
                    if (tinkerActive) {
                        xp *= 1.25;
                        player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
                    }
                    player.getInventionManager().processSkillXp(Skills.FIREMAKING, xp / 2.00, tinderBox);
                    player.getSkills().addXp(Skills.FIREMAKING, xp);
                } else {
                    double xp = increasedExperience(player, log.getExperience());
                    Perk tinker = tinderBox != null ? player.getInventionManager().hasPerk(tinderBox, Perks.TINKER) : null;
                    boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
                    if (tinkerActive) {
                        xp *= 1.25;
                        player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
                    }
                    player.getInventionManager().processSkillXp(Skills.FIREMAKING, xp, tinderBox);
                    player.getSkills().addXp(Skills.FIREMAKING, xp);
                }
                player.setNextFaceWorldTile(tile);
                player.addLogsBurned();
                player.getPackets().sendGameMessage("The fire catches and the logs begin to burn; logs burned " + Colors.RED + Utils.getFormattedNumber(player.getLogsBurned()) + "</col>.", true);
                ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
            }
        }, 1);
        player.getTemporaryAttributtes().put("Fire", Utils.currentTimeMillis() + 1800);
        return -1;
    }

    private Item tinderBox;

    @Override
    public boolean start(Player player) {
        if (player.isNative950()) {
            if (fl != null) { player.sendMessage("Coloured firelighters are not available yet."); return false; }
            native950Attempt = Native950Firemaking.begin(player,log);
            if (native950Attempt == null) return false;
            setActionDelay(player,native950Attempt.delay());
            return true;
        }
        if (!checkAll(player))
            return false;
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("pyro-matic") || weapon.getName().toLowerCase().contains("crystal tinderbox"));
        if (hasAugmentedTool)
            tinderBox = weapon;
        player.stopAll();
        player.getPackets().sendGameMessage("You attempt to light the logs.", true);
        player.getInventory().deleteItem(log.getLogId(), 1);
        World.addGroundItem(new Item(log.getLogId(), 1), new WorldTile(player), player, true, 60, 0, false);
        Long time = (Long) player.getTemporaryAttributtes().remove("Fire");
        boolean quickFire = time != null && time > Utils.currentTimeMillis();
        setActionDelay(player, quickFire ? 1 : 2);
        if (!quickFire)
            player.setNextAnimation(new Animation(hasAugmentedTool ? (tinderBox.getName().toLowerCase().contains("pyro-matic") ? 30200 : 25005) : 16700));
        return true;
    }

    @Override
    public void stop(final Player player) {
        if (player.isNative950() && native950Attempt != null) native950Attempt.stop();
        setActionDelay(player, 3);
    }

}