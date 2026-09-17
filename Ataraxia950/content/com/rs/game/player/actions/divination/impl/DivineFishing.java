package com.rs.game.player.actions.divination.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.FishingRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class DivineFishing extends Action {

    private final DivineFishingSpots spot;
    private final WorldObject spott;
    private final WorldTile tile;
    private int fishId;

    public DivineFishing(DivineFishingSpots spot, int x, int y, int plane, WorldObject spott) {
        this.spot = spot;
        this.spott = spott;
        this.tile = new WorldTile(x, y, plane);
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        fishId = getRandomFish(player);
        setActionDelay(player, getFishingDelay(player));
        return true;
    }

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(22899));
		if (Utils.random(1000) == 0 && player.hasRandomEvent()) {
			if (!player.followedByRandomEventNPC()) {
				NPC npc = new FishingRandomEvent(player, player);
				if (npc.withinDistance(player, 14)) {
					player.setCurrentRandomEventNPC(npc);
					player.sendMessage("<col=ff0000>A River Troll emerges from the fishing spot");
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

    private int getFishingDelay(Player player) {
        int playerLevel = player.getSkills().getLevel(Skills.FISHING);
        int fishLevel = spot.getFish()[fishId].getLevel();
        int modifier = spot.getFish()[fishId].getLevel();
        int randomAmt = Utils.random(4);
        double cycleCount = 1, otherBonus = 0;
        if (player.getFamiliar() != null)
            otherBonus = getSpecialFamiliarBonus(player.getFamiliar().getId());
        cycleCount = Math.ceil(((fishLevel + otherBonus) * 50 - playerLevel * 10) / modifier * 0.25 - randomAmt * 4);
        if (cycleCount < 1)
            cycleCount = 1;
        int delay = (int) cycleCount + 1;
        delay /= player.getAuraManager().getFishingAccurayMultiplier();
        return delay;
    }

    private int getSpecialFamiliarBonus(int id) {
        switch (id) {
            case 6796:
            case 6795:// rock crab
                return 1;
        }
        return -1;
    }

    private int getRandomFish(Player player) {
        int random = Utils.random(spot.getFish().length);
        int difference = player.getSkills().getLevel(Skills.FISHING) - spot.getFish()[random].getLevel();
        if (difference < -1)
            return random = 0;
        if (random < -1)
            return random = 0;
        return random;
    }

    @Override
    public int processWithDelay(Player player) {
        addFish(player);
        if (player.getDailyManager().getTask() != null) {
            if (spot.getFish()[fishId].getId() == player.getTaskItemId()) {
                player.getDailyManager().processTask();
            }
        }
        return getFishingDelay(player);
    }

    private void addFish(Player player) {
        Item hItem = new Item(spot.getFish()[fishId].getId(), 1);
        fishId = getRandomFish(player);
        double totalXp = spot.getFish()[fishId].getXp() * fishingSuit(player);
        int roll = Utils.random(100);
        Player owner = spott.getOwner();
        String fishName = hItem.getName().toLowerCase();
        player.getInventory().addItem(hItem);
        player.getSkills().addXp(Skills.FISHING, totalXp);
        player.addFishCaught(hItem.getAmount());
        player.sendMessage("You catch a " + fishName + "; fish caught: " + Colors.RED
                + Utils.getFormattedNumber(player.getFishCaught()) + "</col>.", true);
        DivineObject.handleHarvest(player);
        if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
            if (Utils.random(100) >= 75)
                owner.gathered++;
            if (!hItem.getDefinitions().isStackable())
                hItem.setId(hItem.getDefinitions().getCertId());
            owner.getInventory().addItem(hItem);
            owner.sendMessage(player.getDisplayName() + " caught a " + fishName + " for you.", true);
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
        if (player.getSkills().getLevel(Skills.FISHING) < spot.getFish()[fishId].getLevel()) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "You need a Fishing level of " + spot.getFish()[fishId].getLevel() + " to fish here.");
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
        setActionDelay(player, 2);
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    private double fishingSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 24427)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 24428)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 24429)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 24430)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 24427 && player.getEquipment().getChestId() == 24428
                && player.getEquipment().getLegsId() == 24429 && player.getEquipment().getBootsId() == 24430)
            xpBoost *= 1.01;
        return xpBoost;
    }

    public enum Fish {

        CRAYFISH(13435, 1, 10),

        HERRING(345, 10, 30),

        TROUT(335, 20, 50),

        SALMON(331, 30, 70),

        LOBSTER(377, 40, 90),

        SWORDFISH(371, 50, 100),

        SHARK(383, 76, 110),

        CAVEFISH(15264, 85, 300),

        ROCKTAIL(15270, 90, 385);

        private final int id, level;
        private final double xp;

        Fish(int id, int level, double xp) {
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

    public enum DivineFishingSpots {

        DIVINE_CRAYFISH(90223, Fish.CRAYFISH),

        DIVINE_HERRING(90224, Fish.HERRING),

        DIVINE_TROUT(90225, Fish.TROUT),

        DIVINE_SALMON(90226, Fish.SALMON),

        DIVINE_LOBSTER(90227, Fish.LOBSTER),

        DIVINE_SWORDFISH(90228, Fish.SWORDFISH),

        DIVINE_SHARK(90229, Fish.SHARK),

        DIVINE_CAVEFISH(90230, Fish.CAVEFISH),

        DIVINE_ROCKTAIL(90231, Fish.ROCKTAIL),;

        private final Fish[] fish;
        private final int id;

        DivineFishingSpots(int id, Fish... fish) {
            this.id = id;
            this.fish = fish;
        }

        public Fish[] getFish() {
            return fish;
        }

        public int getId() {
            return id;
        }
    }
}