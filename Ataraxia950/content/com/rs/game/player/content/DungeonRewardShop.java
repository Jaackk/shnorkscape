package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class DungeonRewardShop {

    public static final int REWARD_SHOP = 940;

    public static void openRewardShop(final Player player) {
        player.getInterfaceManager().sendInterface(REWARD_SHOP);
        player.getPackets().sendIComponentSettings(940, 149, 0, 500, 1278);
        refreshPoints(player);
        player.setCloseInterfacesEvent(new Runnable() {

            @Override
            public void run() {
                player.getTemporaryAttributtes().remove(Key.DUNGEON_REWARD_SLOT);
            }
        });
    }

    public static void purchase(Player player) {
        if (!canPurchase(player))
            return;
        removeConfirmationPurchase(player);
        int slot = (int) player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT);
        DungeonReward reward = DungeonReward.forId(slot);
        if (Settings.DEBUG)
            Logger.getGlobal().info(slot + " id;");
        if (reward != null) {
            player.getInventory().addItemDrop(reward.getId(), 1);
            player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() - reward.getCost());
        }
        refreshPoints(player);
    }

    public static void sendConfirmationPurchase(Player player) {
        if (!canPurchase(player))
            return;
        player.getPackets().sendHideIComponent(REWARD_SHOP, 42, false);
    }

    public static void removeConfirmationPurchase(Player player) {
        player.getPackets().sendHideIComponent(REWARD_SHOP, 42, true);
    }

    public static void select(Player player, int slot) {
        if (slot == 0) {
            player.getPackets().sendGameMessage("You already have boosted experience!");
            return;
        }
        player.getTemporaryAttributtes().put(Key.DUNGEON_REWARD_SLOT, slot);
    }

    private static boolean canPurchase(Player player) {
        if (player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT) == null)
            return false;
        int slot = (int) player.getTemporaryAttributtes().get(Key.DUNGEON_REWARD_SLOT);
        DungeonReward reward = DungeonReward.forId(slot);
        if (reward == null) {
            player.getTemporaryAttributtes().remove(Key.DUNGEON_REWARD_SLOT);
            player.getPackets().sendGameMessage("[Undefined slotId] Item not found. " + (player.getRights() == 2 ? slot : ""));
            return false;
        } else {
            player.getTemporaryAttributtes().remove(Key.DUNGEON_REWARD_SLOT);
            player.getTemporaryAttributtes().put("dungReward", reward);
            player.getPackets().sendGameMessage(reward.getName() + " requires a level of atleast " + reward.getRequirement() + " in Dungeoneering and costs " + Utils.getFormattedNumber(reward.getCost()) + " Dungeoneering tokens.");
        }
        int dungeoneeringLevel = reward.getRequirement(), price = reward.getCost();
        if (!(player.getRights() == 2) && (player.getSkills().getLevel(Skills.DUNGEONEERING) < dungeoneeringLevel || player.getDungeoneeringManager().getTokens() < price)) {
            player.getPackets().sendGameMessage("You do not meet the requirements to purchase this item.");
            return false;
        } else {
            player.getDialogueManager().startDialogue("DungRewardConfirm", reward);
        }
        return true;
    }

    public static void refreshPoints(Player player) {
        player.getPackets().sendIComponentText(940, 151, "" + Utils.formatNumber(player.getDungeoneeringManager().getTokens()));
    }

    public static void buyXP(Player player, int amount) {
        final int tokens = player.getDungeoneeringManager().getTokens();
        if (player.getDungeoneeringManager().getTokens() >= amount) {
            player.getDungeoneeringManager().setTokens(tokens - amount);
            player.getTemporaryAttributtes().put("dungxpbuy", true);
            player.getSkills().addXp(Skills.DUNGEONEERING, amount);
            player.sendMessage("You have bought " + amount + " dungeoneering xp!");
        } else {
            player.sendMessage(Colors.RED + "How did you plan on buying more XP than you had tokens?");
        }
    }

    public static void handleButtons(Player player, int componentId, int slotId, int packetId) {
        if (componentId == 172 && packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
            if (player.getTemporaryAttributtes().get("dungReward") != null) {
                DungeonReward reward = (DungeonReward) player.getTemporaryAttributtes().get("dungReward");
                if (reward != null) {
                    if (player.getSkills().getLevelForXp(Skills.DUNGEONEERING) < reward.getRequirement()) {
                        player.sendMessage("You need " + reward.getRequirement() + " dungeoneering to buy this reward.");
                        return;
                    }
                    if (player.getDungeoneeringManager().getTokens() < reward.getCost()) {
                        player.sendMessage("You need " + Utils.getFormattedNumber(reward.getCost()) + " dungeoneering tokens to buy this reward.");
                        return;
                    }
                    player.getDialogueManager().startDialogue("DungRewardConfirm", reward);
                } else
                    player.sendMessage("You must choose a reward before trying to buy something.");
            }
            return;
        }
        if (componentId == 149) {
            DungeonReward reward = DungeonReward.forId(slotId);
            if (slotId == 0) {
                if (player.getSkills().getLevel(Skills.DUNGEONEERING) >= 77) {
                    player.closeInterfaces();
                    player.sendInputInteger("How much XP would you like to buy? Max amount : (" + player.getDungeoneeringManager().getTokens() + ")", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            DungeonRewardShop.buyXP(player, getInteger());
                        }
                    });
                } else
                    player.sendMessage("You need level 77 dungeoneering to use this!");
                return;
            }
            if (reward == null && slotId != 0) {
                player.getTemporaryAttributtes().remove(Key.DUNGEON_REWARD_SLOT);
                player.getTemporaryAttributtes().remove("dungReward");
                player.sendMessage(Colors.RED + "This reward is not added! [" + slotId + "]");
                return;
            } else {
                DungeonReward.init();
                if (Settings.DEBUG)
                    player.sendMessage(Colors.RED + "" + reward.getName());
                player.getTemporaryAttributtes().put("dungReward", reward);
            }
        }
    }

    public enum DungeonReward {
        // id, slotid, req, amount
        BONECRUSHER(18337, 16, 21, 34000),

        HERBICIDE(19675, 20, 21, 34000),

        SCROLL_OF_LIFE(18336, 28, 25, 10000),

        GEM_BAG(18338, 32, 25, 2000),

        ARCANE_PULSE_NECKLACE(18333, 36, 30, 6500),

        FARSIGHT_QUICK_SHOT_NECKLACE(31443, 40, 30, 6500),

        BRAWLERS_JAB_NECKLACE(31446, 44, 30, 6500),

        TWISTEDNECKLACE(19886, 48, 30, 8500),

        COAL_BAG(18339, 12, 35, 4000),

        SHIELDBOW_SIGHT(18330, 64, 45, 10000),

        LAW_STAFF(18342, 68, 45, 10000),

        GRAVITE_RAPIER(18365, 72, 45, 40000),

        GRAVITE_LONGSWORD(18367, 80, 45, 40000),

        GRAVITE_2H(18369, 88, 45, 40000),

        GRAVITE_STAFF(18371, 92, 45, 40000),

        GRAVITE_SHORTBOW(18373, 96, 45, 40000),

        GRAVITE_KNIFE(36877, 100, 45, 40000),

        GRAVITE_WAND(36873, 108, 45, 40000),

        GRAVITE_ORB(36875, 112, 45, 20000),

        TOME_OF_FROST(18346, 116, 48, 43000),

        AMULET_OF_ZEALOTS(19892, 120, 48, 40000),

        SCROLL_OF_CLEANSING(19890, 124, 49, 20000),

        SPIRIT_CAPE(19893, 128, 50, 45000),

        NATURE_STAFF(18341, 132, 1, 12500),

        SCROLL_OF_EFFICIENCY(19670, 136, 55, 20000),

        ARCANE_BLAST_NECKLACE(18334, 140, 50, 15500),

        FARSIGHT_SNAP_SHOT_NECKLACE(31444, 144, 50, 15500),

        BRAWLERS_HOOK_NECKLACE(31447, 148, 50, 15500),

        DRAGONTOOTHNECKLACE(19887, 152, 60, 17000),

        ANTI_POISON_TOTEM(40681, 156, 60, 44000),

        RING_OF_VIGOUR(19669, 180, 62, 50000),

        SCROLL_OF_RENEWAL(18343, 184, 65, 107000),

        MERCENARY_GLOVES(18347, 192, 73, 48500),

        CHAOTIC_SPIKE(27068, 196, 80, 20000),

        CHAOTIC_REMNANT(31449, 200, 70, 100000),

        CHAOTIC_RAPIER(18349, 204, 80, 200000),

        CHAOTIC_LONGSWORD(18351, 208, 80, 200000),

        CHAOTIC_MAUL(18353, 212, 80, 200000),

        CHAOTIC_STAFF(18355, 216, 80, 200000),

        CHAOTIC_CROSSBOW(18357, 220, 80, 200000),

        OFF_HAND_CRAPIER(25991, 224, 80, 100000),

        OFF_HAND_CLONG(25993, 228, 80, 100000),

        OFF_HAND_CCB(25995, 232, 80, 100000),

        SNEAKERPEEPER(19894, 236, 80, 85000),

        CHAOTIC_KITESHIELD(18359, 240, 80, 200000),

        EAGLE_EYE_KITESHIELD(18361, 244, 80, 200000),

        FARSEER_KITESHIELD(18363, 248, 80, 200000),

        CHAOTIC_SPLINT(36164, 252, 0, 150000),

        DEMONHORNNECKLACE(19888, 256, 90, 35000),

        FROSTY(31459, 260, 85, 250000),

        MINI_BLINK(31457, 264, 1, 500000),

        HOPE_NIBBLER(31458, 268, 101, 1000000);

        private static Map<Integer, DungeonReward> rewards;
        private final int id;
        private final int req;
        private final int cost;
        private final int slotId;
        private final String name;

        DungeonReward(int id, int slotId, int req, int cost) {
            this.id = id;
            this.req = req;
            this.cost = cost;
            this.slotId = slotId;
            this.name = ItemDefinitions.getItemDefinitions(id).getName();
        }

        public static DungeonReward forId(int id) {
            if (rewards == null)
                init();
            return rewards.get(id);
        }

        static void init() {
            rewards = new HashMap<Integer, DungeonReward>();
            for (DungeonReward dr : DungeonReward.values())
                rewards.put(dr.slotId, dr);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public int getSlotId() {
            return slotId;
        }

        public int getRequirement() {
            return req;
        }
    }
}