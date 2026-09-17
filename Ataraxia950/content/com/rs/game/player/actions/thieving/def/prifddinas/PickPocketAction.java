package com.rs.game.player.actions.thieving.def.prifddinas;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.ThievingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Pots;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Prifddinas NPC Pickpocketing - Thieving Skill.
 *
 * @author Noel
 */
public class PickPocketAction extends Action {

	/**
	 * The Pickpocketing animation.
	 */
	private static final Animation PICKPOCKETING_ANIMATION = new Animation(24887);
	/**
	 * Pick pocketing NPC.
	 */
	private final NPC npc;
	/**
	 * Data of an NPC.
	 */
	private final PickPocketableNPC npcData;
	/**
	 * The initial ticks as Integer used for Prifddinas Elves.
	 */
	private int ticks = 15;

	/**
	 * Constructs a new {@code PickpocketAction} {@code Object}.
	 *
	 * @param npc
	 *            The npc to whom the player is pickpocketing.
	 * @param npcData
	 *            Data of an npc.
	 */
	public PickPocketAction(NPC npc, PickPocketableNPC npcData) {
		this.npc = npc;
		this.npcData = npcData;
	}

	/**
	 * Checks everything before starting.
	 *
	 * @param player
	 *            The player.
	 * @return
	 */
	private boolean checkAll(Player player) {
		if (npcData.equals(PickPocketableNPC.IORWERTH_WORKER)) {
			if (player.thievIorwerth > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.ITHELL_WORKER)) {
			if (player.thievIthell > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.CADARN_WORKER)) {
			if (player.thievCadarn > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.AMLODD_WORKER)) {
			if (player.thievAmlodd > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.TRAHAEARN_WORKER)) {
			if (player.thievTrahaearn > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.HEFIN_WORKER)) {
			if (player.thievHefin > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.CRWYS_WORKER)) {
			if (player.thievCrwys > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (npcData.equals(PickPocketableNPC.MEILYR_WORKER)) {
			if (player.thievMeilyr > 0) {
				player.sendMessage(
						"I should not steal from them, they've warned the rest of " + npcData.getMessage() + ".");
				return false;
			}
		}
		if (player.getSkills().getLevel(Skills.THIEVING) < npcData.getThievingLevel()) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You need a thieving level of " + npcData.getThievingLevel() + " to steal from this npc.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("You don't have enough space in your inventory.", true);
			return false;
		}
		if (player.getAttackedBy() != null && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
			player.sendMessage("You can't do this while you're under combat.", true);
			return false;
		}
		if (npc.getAttackedBy() != null && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("The npc is under combat.", true);
			return false;
		}
		if (npc.isDead()) {
			player.getPackets().sendGameMessage("Too late, the npc is dead.", true);
			return false;
		}
		return true;
	}

	/**
	 * Gets the animation to perform.
	 *
	 * @param player
	 *            The player.
	 * @return The animation.
	 */
	private Animation getAnimation() {
		return PICKPOCKETING_ANIMATION;
	}

	/**
	 * Gets the increased chance for succesfully pickpocketing.
	 *
	 * @param player
	 *            The player.
	 * @return The amount of increased chance.
	 */
	private int getIncreasedChance(Player player) {
		int chance = 0;
		if (Equipment.getItemSlot(Equipment.SLOT_HANDS) == 10075)
			chance += 12;
		player.getEquipment();
		if (Equipment.getItemSlot(Equipment.SLOT_CAPE) == 15349)
			chance += 15;
		if(player.jujuPotions.isActive(Pots.Effects.SCENTLESS_JUJU)) {
			chance += 15;
		}
		if (npc.getDefinitions().name.contains("H.A.M")) {
			for (Item item : player.getEquipment().getItems().getItems()) {
				if (item != null && item.getDefinitions().getName().contains("H.A.M"))
					chance += 3;
			}
		}
		if (player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND))
			chance += 30;
		return chance;
	}

	/**
	 * Checks if the Player has the Thieving suit.
	 *
	 * @param player
	 *            The player to check.
	 * @return if has the Suit.
	 */
	private boolean hasTheivingSuit(Player player) {
        return player.getEquipment().getHatId() == 21482 && player.getEquipment().getChestId() == 21480
                && player.getEquipment().getLegsId() == 21481 && player.getEquipment().getBootsId() == 21483;
    }

	/**
	 * Checks if the player is succesfull to thiev or not.
	 *
	 * @param player
	 *            The player.
	 * @return {@code True} if succesfull, {@code false} if not.
	 */
	private boolean isSuccesfull(Player player) {
		int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
		int increasedChance = getIncreasedChance(player);
		int level = Utils.getRandom(thievingLevel + increasedChance);
		double ratio = level / (Utils.random(npcData.getThievingLevel() + 6) + 1);
		if (Math.round(ratio * thievingLevel) < npcData.getThievingLevel()
				/ player.getAuraManager().getThievingAccurayMultiplier())
			return player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND);
		return true;
	}

	@Override
	public boolean process(Player player) {
		npc.resetWalkSteps();
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (npcData.equals(PickPocketableNPC.IORWERTH_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtIorwerth++;
				if (player.caughtIorwerth == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtIorwerth == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtIorwerth == 3) { // Set delay to 20 minutes
													// before player can thiev
													// here again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtIorwerth = 0;
					player.thievIorwerth = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.ITHELL_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtIthell++;
				if (player.caughtIthell == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtIthell == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtIthell == 3) { // Set delay to 20 minutes
												// before player can thiev here
												// again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtIthell = 0;
					player.thievIthell = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.CADARN_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtCadarn++;
				if (player.caughtCadarn == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtCadarn == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtCadarn == 3) { // Set delay to 20 minutes
												// before player can thiev here
												// again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtCadarn = 0;
					player.thievCadarn = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.AMLODD_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtAmlodd++;
				if (player.caughtAmlodd == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtAmlodd == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtAmlodd == 3) { // Set delay to 20 minutes
												// before player can thiev here
												// again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtAmlodd = 0;
					player.thievAmlodd = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.TRAHAEARN_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtTrahaearn++;
				if (player.caughtTrahaearn == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtTrahaearn == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtTrahaearn == 3) { // Set delay to 20 minutes
													// before player can thiev
													// here again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtTrahaearn = 0;
					player.thievTrahaearn = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.HEFIN_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtHefin++;
				if (player.caughtHefin == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtHefin == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtHefin == 3) { // Set delay to 20 minutes before
												// player can thiev here again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtHefin = 0;
					player.thievHefin = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.CRWYS_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtCrwys++;
				if (player.caughtCrwys == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtCrwys == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtCrwys == 3) { // Set delay to 20 minutes before
												// player can thiev here again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtCrwys = 0;
					player.thievCrwys = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (npcData.equals(PickPocketableNPC.MEILYR_WORKER)) {
			if (!isSuccesfull(player) && Utils.random(5) == 0) {
				player.caughtMeilyr++;
				if (player.caughtMeilyr == 1)
					player.sendMessage("You have raised " + npcData.getMessage() + " suspicions. 1 out of 3 strikes.");
				if (player.caughtMeilyr == 2)
					player.sendMessage(npcData.getMessage() + " are starting to notice your sneaky ways. "
							+ "2 out of 3 strikes. They'll catch you next time.");
				if (player.caughtMeilyr == 3) { // Set delay to 20 minutes
												// before player can thiev here
												// again.
					player.sendMessage(
							"Oh dear, they've caught you and they've warned the rest of " + npcData.getMessage() + ".");
					player.caughtMeilyr = 0;
					player.thievMeilyr = 2001;
				}
				failThieve(player);
			} else
				successThieve(player);
		}
		if (Utils.random(75) == 0 && player.hasRandomEvent()) {
			if (!player.followedByRandomEventNPC()) {
				NPC npc = new ThievingRandomEvent(player, player);
				if (npc.withinDistance(player, 14)) {
					player.setCurrentRandomEventNPC(npc);
					player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
				}
			}
		}
		return ticks > 0 ? 4 : -1;
	}

	/**
	 * Successfull Thieve.
	 *
	 * @param player
	 *            The player Thieving.
	 */
	private void successThieve(Player player) {
		ticks--;
		double totalXp = npcData.getExperience();
		if (hasTheivingSuit(player))
			totalXp *= 1.025;
		player.getSkills().addXp(Skills.THIEVING, totalXp);
		Item item = npcData.getLoot()[Utils.random(npcData.getLoot().length)];
		if (item.getId() == 995)
			player.addMoney(item.getAmount());
		else
			player.getInventory().addItem(item.getId(), item.getAmount());
		player.faceEntity(npc);
		player.setNextAnimation(getAnimation());
		player.addTimesStolen();
		player.sendMessage("You succesfully pick the " + npcData.getMessage() + " worker's pocket; " + "times thieved: "
				+ Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
	}

	/**
	 * Fail Thieve.
	 *
	 * @param player
	 *            The player Thieving.
	 */
	private void failThieve(Player player) {
		npc.setNextForceTalk(new ForceTalk("What do you think you're doing?"));
		npc.faceEntity(player);
		player.setNextAnimation(new Animation(424));
		player.setNextGraphics(new Graphics(80, 5, 60));
		ticks = 0;
	}

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			player.faceEntity(npc);
			player.setNextAnimation(getAnimation());
			player.sendMessage("You start to pick the " + npcData.getMessage() + " worker's pocket..", true);
			npc.resetWalkSteps();
			setActionDelay(player, 6);
			return true;
		}
		return false;
	}

	@Override
	public void stop(Player player) {
		npc.setNextFaceEntity(null);
		setActionDelay(player, 6);
	}
}