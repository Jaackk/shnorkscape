package com.rs.game.player.actions.mining;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.mining.defs.EssenceDefinitions;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class EssenceMining extends MiningBase {

    private static final long MAX_MINING_TIME_MS = 90_000L; // 90 seconds

    private final WorldObject rock;
    private final EssenceDefinitions definitions;
    private long startedAtMs;


    public EssenceMining(WorldObject rock, EssenceDefinitions definitions) {
        this.rock = rock;
        this.definitions = definitions;
    }

    private boolean hasTimedOut(Player player) {
        if (startedAtMs == 0)
            return false;


        long elapsed = System.currentTimeMillis() - startedAtMs;
        if (elapsed >= MAX_MINING_TIME_MS) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("You stop mining after 90 seconds.", true);
            return true;
        }
        return false;
    }


    private void addOre(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null
                && (weapon.getName().toLowerCase().contains("dragon pickaxe")
                || weapon.getName().toLowerCase().contains("crystal pickaxe"));

        if (!hasAugmentedTool)
            weapon = null;

        double xpBoost = 1.0;
        xpBoost *= miningSuit(player);

        // Calculate ore amount based on mining level
        int miningLevel = player.getSkills().getLevel(Skills.MINING);
        int oreAmount = 1 + (miningLevel / 20); // Adds +1 ore every 20 levels

        if (player.getInventionManager().procGathering(weapon, definitions.getOreId(), Skills.MINING, definitions.getXp() * xpBoost))
            player.getInventory().addItem(definitions.getOreId(), oreAmount);

        player.getInventionManager().processSkillXp(Skills.MINING, definitions.getXp() * xpBoost, weapon);
        player.getSkills().addXp(Skills.MINING, definitions.getXp() * xpBoost);

        String oreName = ItemDefinitions.getItemDefinitions(definitions.getOreId()).getName().toLowerCase();
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);

        // Increment ores mined correctly
        for (int i = 0; i < oreAmount; i++) {
            player.addOresMined();
        }

        player.sendMessage("You mine some " + oreName + "; ores mined: " + Colors.RED + Utils.getFormattedNumber(player.getOresMined()) + "</col>.", true);
    }



    private boolean checkAll(Player player) {
        if (!hasPickaxe(player)) {
            player.getPackets().sendObjectMessage(0, 15263739, rock, "You need a pickaxe to mine the " + rock.getDefinitions().name + ".");
            player.sendMessage("You need a pickaxe to mine this rock.");
            return false;
        }
        if (!setPickaxe(player)) {
            player.sendMessage("You don't have the required level to use this pickaxe.");
            return false;
        }
        if (!hasMiningLevel(player))
            return false;
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        return true;
    }

    private boolean checkRock(Player player) {
        return World.containsObjectWithId(rock, rock.getId());
    }

    private int getMiningDelay(Player player) {
        int mineTimer = definitions.getOreBaseTime() - player.getSkills().getLevel(Skills.MINING) - Utils.getRandom(pickaxeTime);
        if (mineTimer < 1 + definitions.getOreRandomTime())
            mineTimer = 1 + Utils.getRandom(definitions.getOreRandomTime());
        mineTimer /= player.getAuraManager().getMiningAccurayMultiplier();
        if (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_MINER))
            mineTimer /= 1.33;
        return mineTimer;
    }

    private boolean hasMiningLevel(Player player) {
        if (definitions.getLevel() > player.getSkills().getLevel(Skills.MINING)) {
            player.sendMessage("You need a mining level of " + definitions.getLevel() + " to mine this rock.");
            return false;
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
// stop ASAP if timer expired
        if (hasTimedOut(player))
            return false;


        setAnimationAndGFX(player);


        return checkRock(player);
    }

    @Override
    public int processWithDelay(Player player) {

        if (hasTimedOut(player))
            return -1;

        addOre(player);
        if (!player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return -1;
        }
        return getMiningDelay(player);
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;


        startedAtMs = System.currentTimeMillis(); // <-- start timer here


        player.sendMessage("You swing your pickaxe at the rock..", true);
        setActionDelay(player, getMiningDelay(player));
        return true;
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
        if (player.getEquipment().getHatId() == 20789 && player.getEquipment().getChestId() == 20791 && player.getEquipment().getLegsId() == 20790 && player.getEquipment().getBootsId() == 20788)
            xpBoost *= 1.01;
        return xpBoost;
    }

}
