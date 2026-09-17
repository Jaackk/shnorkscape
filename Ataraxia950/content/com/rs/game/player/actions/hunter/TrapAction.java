package com.rs.game.player.actions.hunter;

import com.google.common.collect.ImmutableMap;
import com.rs.game.Animation;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.others.HunterTrapNPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.OwnedObjectManager;
import com.rs.game.player.content.skillingcontracts.impl.HunterContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TrapAction extends Action {

    private final Traps trap;
    private final WorldTile tile;

    public TrapAction(Traps trap, WorldTile tile) {
        this.trap = trap;
        this.tile = tile;
    }

    private static int getMaximumTrap(Traps trap, int currentLevel) {
        if (!trap.isItem())
            return 1;
        if (currentLevel >= 80)
            return 5;
        if (currentLevel >= 60)
            return 4;
        if (currentLevel >= 30)
            return 3;
        return 2;
    }

    public static boolean isTrap(Player player, WorldTile tile, int id) {
        for (Traps trap : Traps.values()) {
            if (trap.getIds()[0] != id)
                continue;
            player.getActionManager().setAction(new TrapAction(trap, tile));
            return true;
        }
        return false;
    }

    public static boolean isTrap(Player player, WorldObject o) {
        Traps trap = null;
        boolean marasamawPlant = o.getId() == 56819;
        if (marasamawPlant) {
            trap = Traps.MARASAMAW_PLANT;
        } else {
            for (Traps t : Traps.values()) {
                if ((t.isItem() && (o.getId() == t.getIds()[1] || o.getId() == t.getIds()[2])) || (!t.isItem() && (o.getId() == t.getIds()[2] || o.getId() == t.getIds()[1]))) {
                    trap = t;
                    break;
                }
            }
        }
        HunterNPC captured = null;
        if (trap == null) {
            for (HunterNPC npc : HunterNPC.values()) {
                if (o.getId() == npc.getIds()[0]) {
                    captured = npc;
                    trap = captured.trap;
                    break;
                }
            }
        }
        if (marasamawPlant) {
            JadinkoCatch caught = player.hunterCatch.get(o);
            if (caught != null) {
                captured = caught.getHunterNPC();
            }
        }
        if (trap == null)
            return false;
        else if (!OwnedObjectManager.isPlayerObject(player, o)) {
            player.getPackets().sendGameMessage("This isn't your trap!");
            return true;
        }
        sendTrapAction(player, o, trap, captured);
        return true;
    }

    private static void sendTrapAction(final Player player, final WorldObject o, final Traps trap, final HunterNPC captured) {
        if (player.isLocked())
            return;
        player.lock(3);
        player.setNextAnimation(new Animation(trap.getIds()[trap.isItem() ? 4 : 6]));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (trap.isItem())
                    player.getInventory().addItemDrop(trap.getIds()[0], 1);
                else
                    World.spawnObject(new WorldObject(trap.getIds()[0], o.getType(), o.getRotation(), new WorldTile(o.getTileHash())));
                if (captured != null) {
                    int[] ids = captured.getIds();
                    RewardItem[] rewards = captured.getRewards();
                    if (rewards != null) {
                        boolean huntsmanPerk = player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) && Utils.random(9) == 1;
                        for (val next : rewards) {
                            if (next.isSuccess()) {
                                player.getInventory().addItemDrop(next.toItem());
                            }
                        }
                        if (huntsmanPerk) {
                            for (val next : rewards) {
                                if (next.isSuccess()) {
                                    player.getInventory().addItemDrop(next.toItem());
                                }
                            }
                            player.sendMessage("You receive double loot thanks to your Huntsman perk!");
                        }
                        HunterContractList.listen(player, captured, huntsmanPerk ? 2 : 1);
                    } else {
                        for (int i = 3; i < ids.length; i++)
                            addCapturedReward(player, captured, ids[i]);

                        int amount = 1;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) && Utils.random(9) == 1) {
                            for (int i = 3; i < ids.length; i++)
                                addCapturedReward(player, captured, ids[i]);
                            amount++;
                            player.sendMessage("You receive double loot thanks to your Huntsman perk!");
                        }
                        HunterContractList.listen(player, captured, amount);
                    }
                    player.getSkills().addXp(Skills.HUNTER, captured.getExp());
                    player.addCreaturesCaught();
                }
                player.sendMessage(captured != null ? "You've caught a " + Utils.formatPlayerNameForDisplay(captured.toString()) + "; creatures caught: " + Colors.RED + Utils.getFormattedNumber(player.getCreaturesCaught()) + "</col>." : "You dismantle the trap.", true);
                ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
                OwnedObjectManager.removeObject(player, o);
            }
        }, 1);
    }

    private static void addCapturedReward(Player player, HunterNPC captured, int itemId) {
        if (itemId <= 0)
            return;
        int amount = 1;
        if (captured == HunterNPC.GRENWALL && itemId == 12539)
            amount = Utils.random(18, 22);
        player.getInventory().addItemDrop(itemId, amount);
        if (captured == HunterNPC.PAWYA && itemId == 12535 && Utils.random(32) == 0)
            player.getInventory().addItemDrop(5288, 1);
        if (captured == HunterNPC.GRENWALL && itemId == 12539 && Utils.random(4) == 0)
            player.getInventory().addItemDrop(12535, 1);
    }

    private static int getTrapsCount(Player player, boolean item) {
        int trapsCount = 0;
        for (Traps t : Traps.values()) {
            if (t.isItem() != item)
                continue;
            if (item) {
                trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[1]);
                trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[2]);
                trapsCount += OwnedObjectManager.getObjectsforValues(player, Traps.MARASAMAW_FAILS.values());
            } else
                trapsCount += OwnedObjectManager.getObjectsforValue(player, t.getIds()[0]);
        }
        for (HunterNPC npc : HunterNPC.values()) {
            if (npc.getTrap().isItem() != item)
                continue;
            trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[0]);
            trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[1]);
            trapsCount += OwnedObjectManager.getObjectsforValue(player, npc.getIds()[2]);
        }
        return trapsCount;
    }

    @Override
    public boolean start(Player player) {
        boolean is_item = trap.isItem();
        int levelRequirement = trap.getRequirementLevel(), currentLevel = player.getSkills().getLevel(Skills.HUNTER);
        if (currentLevel < levelRequirement) {
            player.getPackets().sendGameMessage("You need a Hunter level of " + levelRequirement + " in order to place this trap.");
            return false;
        } else {
            if (is_item) {
                if (World.getObjectWithSlot(tile, Region.OBJECT_SLOT_FLOOR) != null) {
                    player.getPackets().sendGameMessage("You cannot place a trap here!");
                    return false;
                }
            } else {
                int[] ids = trap.getIds();
                Item item = new Item(getLogsId(player), ids[4]);
                if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
                    player.getPackets().sendGameMessage("You don't have the neccessary supplies to place this trap.");
                    return false;
                }
            }
            int maxAmount = getMaximumTrap(trap, currentLevel) + (player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) ? 2 : 0);
            if (getTrapsCount(player, is_item) == maxAmount) {
                player.getPackets().sendGameMessage("You cannot place more than " + maxAmount + " traps at once.");
                return false;
            }
        }
        player.lock(3);
        player.setNextAnimation(new Animation(trap.getIds()[is_item ? 3 : 5]));
        player.getPackets().sendGameMessage("You begin setting up the trap.", true);
        if (is_item)
            World.addGroundItem(new Item(trap.getIds()[0], 1), tile, player, true, 180);
        player.getInventory().deleteItem(is_item ? trap.getIds()[0] : getLogsId(player), is_item ? 1 : trap.getIds()[4]);
        setActionDelay(player, 4);
        return true;
    }

    public static int getLogsId(Player player) {
        int[] logIds = {1511, 2862, 1521, 1519, 6333, 1517, 40285, 6332, 10810, 12581, 1515, 1513, 13567, 24121, 29556};
        for (int logId : logIds) {
            if (player.getInventory().containsItem(logId, 1))
                return logId;
        }
        return 1511;
    }

    @Override
    public boolean process(Player player) {
        return true;
    }

    @Override
    public int processWithDelay(Player player) {
        boolean is_item = trap.isItem();
        int[] ids = trap.getIds();
        if (is_item) {
            if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1))
                if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1))
                    if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1))
                        player.addWalkSteps(player.getX(), player.getY() - 1, 1);
            final FloorItem item = World.getRegion(tile.getRegionId()).getGroundItem(ids[0], tile, player);
            if (item == null)
                return -1;
            else if (!World.removeGroundItem(player, item, false))
                return -1;
        }
        OwnedObjectManager.addOwnedObjectManager(player, new WorldObject[]{new WorldObject(ids[1], 10, 0, tile.getX(), tile.getY(), tile.getPlane())}, new long[]{300000});
        return -1;
    }

    @Override
    public void stop(Player player) {
        setActionDelay(player, 3);
    }

    public enum Traps {

        /* itemid, objectid, fail obj id, set emote, remove emote */
        BOX(new int[]{10008, 19187, 19192, 5208, 5208}, 27),

        SNARE(new int[]{10006, 19175, 19174, 5208, 5207}, 1),

        MARASAMAW_PLANT(new int[]{19965, 56806, -1, 5208, 5208}, 70),

        /*
         * obj id, trans id, fail obj id, itemid, itemamount, set emote, remove emote
         */

        BOULDER_TRAP(new int[]{19205, 19206, 19219, 1511, 1, 5208, 5208}, 23);

        /*
         * PITFALL(new int[] { 0, 0, 0, 0, 0 }, 31)
         */

        private final int[] ids;
        private final int requirementLevel;
        public static final ImmutableMap<Integer, Integer> MARASAMAW_FAILS = ImmutableMap.<Integer, Integer>builder()
                /* Jadinko NPC ID -> Failed marasamaw object ID */

                .put(13130, 56807) // Amphibious
                .put(13142, 56808) // Aquatic
                .put(13155, 56809) // Cannibal
                .put(13154, 56810) // Carrion
                .put(13796, 56812) // Draconic
                .put(13119, 56813) // Common
                .put(13164, 56814) // Guthix
                .put(13143, 56815) // Igneous
                .put(13163, 56817) // Saradomin
                .put(13165, 56818) // Zamorak
                .build();

        Traps(int[] ids, int requirementLevel) {
            this.ids = ids;
            this.requirementLevel = requirementLevel;
        }

        public int[] getIds() {
            return ids;
        }

        public int getFailedObjectId(HunterTrapNPC npc) {
            if (this == MARASAMAW_PLANT) {
                Integer value = MARASAMAW_FAILS.get(npc.getId());
                return value != null ? value : ids[2];
            }
            return ids[2];
        }

        public int getRequirementLevel() {
            return requirementLevel;
        }

        public boolean isItem() {
            return ids.length == 5;
        }
    }

    @AllArgsConstructor
    public static final class RewardItem {
        public final int id;
        public final int minAmount;
        public final int maxAmount;
        public final int chance;

        public RewardItem(int id, int amount, int chance) {
            this(id, amount, amount, chance);
        }

        public boolean isSuccess() {
            if (chance == 1) {
                return true;
            }
            if (chance == 0) {
                return false;
            }
            return ThreadLocalRandom.current().nextInt(chance) == 0;
        }

        public Item toItem() {
            int amount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
            return new Item(id, amount);
        }
    }

    public enum HunterNPC {

        WILD_KEBBIT(Traps.BOULDER_TRAP, 23, 128, new int[]{19215, 5275, 5277, 526, 10113}, 5089),

        BARB_TAILED_KEBBIT(Traps.BOULDER_TRAP, 33, 168, new int[]{19215, 5275, 5277, 526, 10129}, 5088),

        PRICKLY_KEBBIT(Traps.BOULDER_TRAP, 37, 204, new int[]{19215, 5275, 5277, 526, 10105}, 5086),

        DISEASED_KEBBIT(Traps.BOULDER_TRAP, 44, 200, new int[]{19215, 5275, 5277, 526, 12567}, 7039),

        SABRE_TOOTHED_KEBBIT(Traps.BOULDER_TRAP, 51, 200, new int[]{19216, 5275, 5277, 526, 10109}, 5087),

        GREY_CHINCHOMPA(Traps.BOX, 53, 198.4, new int[]{28557, 5184, -1, 10033}, 5079),

        RED_CHINCHOMPA(Traps.BOX, 63, 265, new int[]{28558, 5184, -1, 10034}, 5080),

        FERRET(Traps.BOX, 27, 115, new int[]{19189, 5191, 5192}, 5081),

        GECKO(Traps.BOX, 27, 100, new int[]{19190, 8362, 8361}, 7289, 7290, 7291, 7292),

        RACCOON(Traps.BOX, 27, 100, new int[]{19191, 7726, 7727}, 6997, 7276, 7275),

        MONKEY(Traps.BOX, 27, 100, new int[]{28557, 8343, 8345}, 7228, 7229, 7230, 7231, 7232, 7233, 7234, 7235, 7236, 6944),

        CRIMSON_SWIFT(Traps.SNARE, 1, 34, new int[]{19180, 5171, 5172, 10088, 526, 9978}, 5073),

        GOLDEN_WARBLER(Traps.SNARE, 5, 48, new int[]{19184, 5171, 5172, 10090, 526, 9978}, 5075),

        COPPER_LONGTAIL(Traps.SNARE, 9, 61, new int[]{19186, 5171, 5172, 10091, 526, 9978}, 5076),

        CERULEAN_TWITCH(Traps.SNARE, 11, 64.5, new int[]{19182, 5171, 5172, 10089, 526, 9978}, 5074),

        TROPICAL_WAGTAIL(Traps.SNARE, 19, 95.8, new int[]{19178, 5171, 5172, 10087, 526, 9978}, 5072),

        WIMPY_BIRD(Traps.SNARE, 39, 167, new int[]{28930, 5171, 5172, 11525, 526, 9978}, 7031),

        PAWYA(Traps.BOX, 66, 400, new int[]{28928, 8607, 8607, 526, 12535}, 7012),

        GRENWALL(Traps.BOX, 77, 1100, new int[]{28929, 8603, 8602, 526, 12539}, 7010),

        // do rewards seperately
        COMMON_JADINKO(Traps.MARASAMAW_PLANT, 70, 350, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19897, 1, 2, 1), new RewardItem(19907, 1, 2, 3), new RewardItem(19902, 1, 2, 4)}, 13119), // erzille, argway, ugune
        IGNEOUS_JADINKO(Traps.MARASAMAW_PLANT, 74, 465, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19980, 2, 5, 1)}, 13143), // marble vine
        CANNIBAL_JADINKO(Traps.MARASAMAW_PLANT, 75, 475, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19975, 2, 5, 1)}, 13155), // plant teeth
        AQUATIC_JADINKO(Traps.MARASAMAW_PLANT, 76, 475, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19976, 2, 5, 1)}, 13142), // aquatic vine
        AMPHIBIOUS_JADINKO(Traps.MARASAMAW_PLANT, 77, 485, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19972, 2, 5, 1)}, 13130), // oily vine
        CARRION_JADINKO(Traps.MARASAMAW_PLANT, 78, 505, new int[]{56819, 65535, -1}, new RewardItem[]{new RewardItem(19902, 1, 2, 1), new RewardItem(19912, 1, 2, 3), new RewardItem(19917, 1, 2, 4)}, 13154), // ugune, shengo, samaden
        DRACONIC_JADINKO(Traps.MARASAMAW_PLANT, 80, 525, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19973, 2, 5, 1), new RewardItem(19977, 2, 5, 2)}, 13796), // draconic/shadow vine
        SARADOMIN_JADINKO(Traps.MARASAMAW_PLANT, 81, 600, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19981, 1, 3, 1), new RewardItem(19897, 1, 2, 2), new RewardItem(19907, 1, 2, 2), new RewardItem(19902, 1, 2, 2), new RewardItem(19902, 1, 2, 3), new RewardItem(19912, 1, 2, 3), new RewardItem(19917, 1, 2, 4), new RewardItem(32665, 1, 2, 5), new RewardItem(19979, 2, 5, 2)}, 13163), // Saradomin/corrupt vine, any seed
        GUTHIX_JADINKO(Traps.MARASAMAW_PLANT, 81, 600, new int[]{56819, 65535, -1}, new RewardItem[]{new RewardItem(19982, 1, 3, 1), new RewardItem(19897, 1, 2, 2), new RewardItem(19907, 1, 2, 2), new RewardItem(19902, 1, 2, 2), new RewardItem(19902, 1, 2, 3), new RewardItem(19912, 1, 2, 3), new RewardItem(19917, 1, 2, 4), new RewardItem(32665, 1, 2, 5), new RewardItem(19979, 2, 5, 2)}, 13164), // Guthix/corrupt vine, any seed
        ZAMORAK_JADINKO(Traps.MARASAMAW_PLANT, 81, 600, new int[]{56819, 65535, -1, -1}, new RewardItem[]{new RewardItem(19983, 1, 3, 1), new RewardItem(19897, 1, 2, 2), new RewardItem(19907, 1, 2, 2), new RewardItem(19902, 1, 2, 2), new RewardItem(19902, 1, 2, 3), new RewardItem(19912, 1, 2, 3), new RewardItem(19917, 1, 2, 4), new RewardItem(32665, 1, 2, 5), new RewardItem(19979, 2, 5, 2)}, 13165), // Zamorak/corrupt vine, any seed

        COBALT_SKILLCHOMPA(Traps.BOX, 27, 80.7, new int[]{91230, 5184, -1, 31595}, 19440),

        VIRIDIAN_SKILLCHOMPA(Traps.BOX, 46, 119, new int[]{91231, 5184, -1, 31596}, 19441),

        AZURE_SKILLCHOMPA(Traps.BOX, 68, 178, new int[]{91232, 5184, -1, 31597}, 19442),

        CRIMSON_SKILLCHOMPA(Traps.BOX, 89, 382, new int[]{91233, 5184, -1, 31598}, 19443),

        CRYSTAL_SKILLCHOMPA(Traps.BOX, 97, 476, new int[]{97413, 5184, -1, 40995}, 24807),

        ;
        private final Traps trap;
        private final int lureLevel;
        private final double exp;
        private final int[] ids, npcIds;
        @Getter
        private final RewardItem[] rewards;
        public static final HashMap<Integer, HunterNPC> ENTITIES = new HashMap<Integer, HunterNPC>();
        public static final Set<HunterNPC> JADINKO = EnumSet.noneOf(HunterNPC.class);

        static {
            for (HunterNPC ent : HunterNPC.values()) {
                for (int npc : ent.npcIds)
                    ENTITIES.put(npc, ent);
                if (ent.name().contains("JADINKO"))
                    JADINKO.add(ent);
            }
        }

        // IDS = OBJECT ID, 1st animation, 2nd animation (optional) OR object1,object2,object3,weaponid
        HunterNPC(Traps trap, int lureLevel, double exp, int[] ids, int... npcId) {
            this.trap = trap;
            this.lureLevel = lureLevel;
            this.exp = exp;
            this.ids = ids;
            this.npcIds = npcId;
            rewards = null;
        }

        HunterNPC(Traps trap, int lureLevel, double exp, int[] ids, RewardItem[] rewards, int... npcId) {
            this.trap = trap;
            this.lureLevel = lureLevel;
            this.exp = exp;
            this.ids = ids;
            this.npcIds = npcId;
            this.rewards = rewards;
        }

        public Traps getTrap() {
            return trap;
        }

        public int getLureLevel() {
            return lureLevel;
        }

        public double getExp() {
            return exp;
        }

        public int[] getIds() {
            return ids;
        }

        public int[] getNpcIds() {
            return npcIds;
        }

    }

    public static int getSkillChompa(Player player) {
        int weaponId = player.getEquipment().getWeaponId();
        for (int i = HunterNPC.COBALT_SKILLCHOMPA.ordinal(); i <= HunterNPC.CRYSTAL_SKILLCHOMPA.ordinal(); i++) {
            HunterNPC hnpc = HunterNPC.values()[i];
            if (weaponId == hnpc.getIds()[3])
                return weaponId;
        }
        return -1;
    }


}
