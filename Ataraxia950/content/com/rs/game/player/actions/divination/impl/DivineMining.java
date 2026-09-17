package com.rs.game.player.actions.divination.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Divine location Mining.
 *
 * @author Noel
 */
public final class DivineMining extends MiningBase {

    private final WorldObject rock;
    private final RockDefinitions definitions;
    private int oreId;

    public DivineMining(WorldObject rock, RockDefinitions definitions) {
        this.rock = rock;
        this.definitions = definitions;
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        setActionDelay(player, getMiningDelay(player));
        return true;
    }

    private int getMiningDelay(Player player) {
        int summoningBonus = 0;
        if (player.getFamiliar() != null) {
            if (player.getFamiliar().getId() == 7342 || player.getFamiliar().getId() == 7342)
                summoningBonus += 10;
            else if (player.getFamiliar().getId() == 6832 || player.getFamiliar().getId() == 6831)
                summoningBonus += 1;
        }
        int mineTimer = definitions.getOreBaseTime() - (player.getSkills().getLevel(Skills.MINING) + summoningBonus)
                - Utils.getRandom(pickaxeTime);
        if (mineTimer < 1 + definitions.getOreRandomTime())
            mineTimer = 1 + Utils.getRandom(definitions.getOreRandomTime());
        mineTimer /= player.getAuraManager().getMiningAccurayMultiplier();
        return mineTimer;
    }

    private boolean checkAll(Player player) {
        Player owner = rock.getOwner();
        if (owner == null)
            owner = player;
        if (!DivineObject.canUseDivineObj(player, owner)) {
            return false;
        }
        player.closeInterfaces();
        if (!hasPickaxe(player)) {
            player.getPackets().sendObjectMessage(0, 15263739, rock,
                    "You need a pickaxe to mine the " + rock.getDefinitions().name + ".");
            return false;
        }
        if (!setPickaxe(player)) {
            player.sendMessage("You dont have the required level to use this pickaxe.");
            return false;
        }
        if (!hasMiningLevel(player))
            return false;
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return false;
        }
        return true;
    }

    private boolean hasMiningLevel(Player player) {
        if (definitions.getLevel() > player.getSkills().getLevel(Skills.MINING)) {
            player.getPackets()
                    .sendGameMessage("You need a mining level of " + definitions.getLevel() + " to mine this rock.");
            return false;
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        player.setNextAnimation(new Animation(emoteId));
        player.faceObject(rock);
        if (!DivineObject.canHarvest(player)) {
            player.setNextAnimation(new Animation(-1));
            return false;
        }
        return checkRock(player);
    }

    @Override
    public int processWithDelay(Player player) {
        addOre(player);
        if (player.getDailyManager().getTask() != null) {
            if (definitions.getOreId() == player.getTaskItemId()) {
                player.getDailyManager().processTask();
            }
        }
        if (!player.getInventory().hasFreeSlots() && definitions.getOreId() != -1) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return -1;
        }
        return getMiningDelay(player);
    }

    public void addOre(Player player) {
        oreId = definitions.getOreId();
        Item hItem = new Item(oreId);
        if (hItem.getId() == 436)
            hItem.setId(Utils.random(100) > 50 ? 438 : 436);
        int roll = Utils.random(100);
        Player owner = rock.getOwner();
        String oreName = hItem.getName().toLowerCase();
        player.getInventory().addItem(hItem);
        double totalXp = definitions.getXp() * miningSuit(player);
        player.getSkills().addXp(Skills.MINING, totalXp);
        player.addOresMined();

        // TODO Divine mining impl?
  /*     com.rs.game.player.actions.mining.defs.RockDefinitions normal = RockDefinitions.getNormal(definitions);
        if (normal != null) {
            MiningContractList.listen(player, normal);
        }*/
        player.getPackets().sendGameMessage("You mine some " + oreName + "; ores mined: " + Colors.RED
                + Utils.getFormattedNumber(player.getOresMined()) + "</col>.", true);
        DivineObject.handleHarvest(player);
        if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
            if (Utils.random(100) >= 75)
                owner.gathered++;
            if (!hItem.getDefinitions().isStackable())
                hItem.setId(hItem.getDefinitions().getCertId());
            owner.getInventory().addItem(hItem);
            owner.sendMessage(player.getDisplayName() + " mined some " + oreName + " for you.", true);
        }
        DeathsBounty.dropUrn(player);
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    private double miningSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 20789)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 20791)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 20790)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 20788)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 20789 && player.getEquipment().getChestId() == 20791
                && player.getEquipment().getLegsId() == 20790 && player.getEquipment().getBootsId() == 20788)
            xpBoost *= 1.01;
        return xpBoost;
    }

    private boolean checkRock(Player player) {
        return World.containsObjectWithId(rock, rock.getId());
    }

    public enum RockDefinitions {

        DIVINE_RUNE_ORE(85, 125, 451, 5, 5), DIVINE_ADAMANTITE_ORE(70, 95, 449, 5, 5), DIVINE_MITHRIL_ORE(55, 80, 447,
                5, 5), DIVINE_COAL_ORE(30, 50, 453, 5,
                5), DIVINE_IRON_ORE(15, 35, 440, 5, 1), DIVINE_BRONZE_ORE(1, 17.5, 436, 5, 1);

        private final int level;
        private final double xp;
        private final int oreId;
        private final int oreBaseTime;
        private final int oreRandomTime;

        RockDefinitions(int level, double xp, int oreId, int oreBaseTime, int oreRandomTime) {
            this.level = level;
            this.xp = xp;
            this.oreId = oreId;
            this.oreBaseTime = oreBaseTime;
            this.oreRandomTime = oreRandomTime;
        }

        public static com.rs.game.player.actions.mining.defs.RockDefinitions getNorma(RockDefinitions def) {
            switch (def) {
                case DIVINE_IRON_ORE:
                    return com.rs.game.player.actions.mining.defs.RockDefinitions.Iron_Ore;
                case DIVINE_COAL_ORE:
                    return com.rs.game.player.actions.mining.defs.RockDefinitions.Coal_Ore;
                case DIVINE_MITHRIL_ORE:
                    return com.rs.game.player.actions.mining.defs.RockDefinitions.Mithril_Ore;
                case DIVINE_ADAMANTITE_ORE:
                    return com.rs.game.player.actions.mining.defs.RockDefinitions.Adamant_Ore;
                case DIVINE_RUNE_ORE:
                    return com.rs.game.player.actions.mining.defs.RockDefinitions.Runite_Ore;
            }
            return null;
        }

        public int getLevel() {
            return level;
        }

        public double getXp() {
            return xp;
        }

        public int getOreId() {
            return oreId;
        }

        public int getOreBaseTime() {
            return oreBaseTime;
        }

        public int getOreRandomTime() {
            return oreRandomTime;
        }
    }
}
