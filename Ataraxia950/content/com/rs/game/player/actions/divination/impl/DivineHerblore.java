package com.rs.game.player.actions.divination.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.HerbloreRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.content.skillingcontracts.impl.FarmingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Divine location Herblore patch.
 *
 * @author Noel
 */
public class DivineHerblore extends Action {

    private final DivineHerbSpots spot;
    private final WorldObject spott;
    private final WorldTile tile;
    private int herbId;

    public DivineHerblore(DivineHerbSpots spot, int x, int y, int plane, WorldObject spott) {
        this.spot = spot;
        this.spott = spott;
        this.tile = new WorldTile(x, y, plane);
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        herbId = getRandomHerb(player);
        setActionDelay(player, 1);
        return true;
    }

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(spot.getAnimation());
		if (Utils.random(1000) == 0 && player.hasRandomEvent()) {
			if (!player.followedByRandomEventNPC()) {
				NPC npc = new HerbloreRandomEvent(player, player);
				if (npc.withinDistance(player, 14)) {
					player.setCurrentRandomEventNPC(npc);
					player.sendMessage("<col=ff0000>Eli appears from nowhere.", true);
				}
			}
		}
		player.faceObject(spott);
		if (!DivineObject.canHarvest(player)) {
			player.setNextAnimation(new Animation(-1));
			return false;
		}
		return checkAll(player);
	}

    private int getRandomHerb(Player player) {
        int random = Utils.random(spot.getHerbs().length);
        int difference = player.getSkills().getLevel(Skills.FARMING) - spot.getHerbs()[random].getLevel();
        if (difference < -1)
            return random = 0;
        if (random < -1)
            return random = 0;
        return random;
    }

    @Override
    public int processWithDelay(Player player) {
        addHerb(player);
        if (player.getDailyManager().getTask() != null) {
            if (spot.getHerbs()[herbId].getId() == player.getTaskItemId()) {
                player.getDailyManager().processTask();
            }
        }
        return Utils.random(2, 3);
    }

    private void addHerb(Player player) {
        Item hItem = new Item(spot.getHerbs()[herbId].getId(), 1);
        FarmingContractList.listen(player, spot.getHerbs()[herbId]);
        herbId = getRandomHerb(player);
        double totalXp = spot.getHerbs()[herbId].getXp();
        player.getSkills().addXp(Skills.FARMING, totalXp);
        int roll = Utils.random(100);
        Player owner = spott.getOwner();
        player.getInventory().addItem(hItem);
        String herbName = hItem.getDefinitions().getName().toLowerCase();
        player.addProduceGathered();
        player.sendMessage("You harvest a " + herbName + "; produce gathered: " + Colors.RED
                + Utils.getFormattedNumber(player.getProduceGathered()) + "</col>.", true);
        DivineObject.handleHarvest(player);
        if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
            if (Utils.random(100) >= 75)
                owner.gathered++;
            if (!hItem.getDefinitions().isStackable())
                hItem.setId(hItem.getDefinitions().getCertId());
            owner.getInventory().addItem(hItem);
            owner.sendMessage(player.getDisplayName() + " harvested a " + herbName + " for you.", true);
        }
        DeathsBounty.dropUrn(player);
    }

    private boolean checkAll(Player player) {
        Player owner = spott.getOwner();
        if (owner == null)
            owner = player;
        if (!DivineObject.canUseDivineObj(player, owner)) {
           return false;
        }
        if (player.getSkills().getLevel(Skills.FARMING) < spot.getHerbs()[herbId].getLevel()) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "You need a Farming level of " + spot.getHerbs()[herbId].getLevel() + " to harvest here.");
            return false;
        }
        if (!player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return false;
        }
        return World.containsObjectWithId(tile, spot.getId());
    }

    @Override
    public void stop(final Player player) {
        setActionDelay(player, 3);
    }

    public enum Herbs {

        GUAM(199, 9, 100),

        TARRONMIN(203, 14, 15),

        MARRENTILL(201, 19, 15),

        HARRALANDER(205, 26, 48),

        RANARR(207, 32, 60),

        TOADFLAX(3049, 38, 68),

        SPIRIT_WEED(12174, 36, 68),

        IRIT(209, 44, 98),

        WERGALI(14836, 46, 104),

        AVANTOE(211, 50, 120),

        KWUARM(213, 56, 150),

        SNAPDRAGON(3051, 62, 200),

        CADANTINE(215, 67, 140),

        LANTADYME(2485, 73, 300),

        DWARF_WEED(217, 79, 400),

        TORSTOL(219, 85, 480),

        FELLSTALK(21626, 91, 640);

        private final int id, level;
        private final double xp;

        Herbs(int id, int level, double xp) {
            this.id = id;
            this.level = level;
            this.xp = xp;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public double getXp() {
            return xp;
        }
    }

    public enum DivineHerbSpots {

        DIVINE_HERB_I(87280, new Animation(21237), Herbs.GUAM, Herbs.MARRENTILL, Herbs.TARRONMIN, Herbs.HARRALANDER,
                Herbs.RANARR, Herbs.SPIRIT_WEED, Herbs.TOADFLAX),

        DIVINE_HERB_II(87281, new Animation(21237), Herbs.IRIT, Herbs.WERGALI, Herbs.AVANTOE, Herbs.KWUARM,
                Herbs.SNAPDRAGON),

        DIVINE_HERB_III(87282, new Animation(21237), Herbs.CADANTINE, Herbs.LANTADYME, Herbs.DWARF_WEED, Herbs.TORSTOL,
                Herbs.FELLSTALK);

        private final Herbs[] herbs;
        private final int id;
        private final Animation animation;

        DivineHerbSpots(int id, Animation animation, Herbs... herbs) {
            this.id = id;
            this.animation = animation;
            this.herbs = herbs;
        }

        public Herbs[] getHerbs() {
            return herbs;
        }

        public int getId() {
            return id;
        }

        public Animation getAnimation() {
            return animation;
        }
    }
}