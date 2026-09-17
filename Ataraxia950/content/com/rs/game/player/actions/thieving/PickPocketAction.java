package com.rs.game.player.actions.thieving;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.ThievingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.skillingcontracts.impl.ThievingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles NPC Pickpocketing - Thieving Skill.
 *
 * @author Noel
 */
public class PickPocketAction extends Action {

    private final com.rs.game.player.client.Native950Thieving.Journey native950Journey;

	/**
	 * The npc stun animation.
	 */
	private static final Animation STUN_ANIMATION = new Animation(422),

			/**
			 * The pick pocketing animation.
			 */
			PICKPOCKETING_ANIMATION = new Animation(24887),

			/**
			 * The double loot animation.
			 */
			DOUBLE_LOOT_ANIMATION = new Animation(5074),

			/**
			 * The triple loot animation.
			 */
			TRIPLE_LOOT_ANIMATION = new Animation(5075),

			/**
			 * The quadruple loot animation.
			 */
			QUADRUPLE_LOOT_ANIMATION = new Animation(5078);
	/**
	 * The double loot gfx.
	 */
	private static final Graphics DOUBLE_LOOT_GFX = new Graphics(873),

			/**
			 * The triple loot gfx.
			 */
			TRIPLE_LOOT_GFX = new Graphics(874),

			/**
			 * The quadruple loot gfx.
			 */
			QUADRUPLE_LOOT_GFX = new Graphics(875);
	/**
	 * Pick pocketing npc.
	 */
	private final NPC npc;
	/**
	 * Data of an npc.
	 */
	private final PickPocketableNPC npcData;
	/**
	 * The index to use in the levels required arrays.
	 */
	private int index;

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
        native950Journey = npc.isNative950() ? new com.rs.game.player.client.Native950Thieving.Journey(npc, npcData) : null;
	}

	/**
	 * Checks everything before starting.
	 *
	 * @param player
	 *            The player.
	 * @return
	 */
	private boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.THIEVING) < npcData.getThievingLevels()[0]) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You need a thieving level of " + npcData.getThievingLevels()[0] + " to steal from this npc.");
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
		switch (index) {
		case 0:
			return PICKPOCKETING_ANIMATION;
		case 1:
			return DOUBLE_LOOT_ANIMATION;
		case 2:
			return TRIPLE_LOOT_ANIMATION;
		case 3:
			return QUADRUPLE_LOOT_ANIMATION;
		}
		return null;
	}

	/**
	 * Gets the graphic to perform.
	 *
	 * @param player
	 *            The player.
	 * @return The graphic.
	 */
	private Graphics getGraphics() {
		switch (index) {
		case 0:
			return null;
		case 1:
			return DOUBLE_LOOT_GFX;
		case 2:
			return TRIPLE_LOOT_GFX;
		case 3:
			return QUADRUPLE_LOOT_GFX;
		}
		return null;
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
		return chance;
	}

	/**
	 * Gets the message to send when finishing.
	 *
	 * @param player
	 *            The player.
	 * @return The message.
	 */
	private String getMessage(Player player) {
		player.addTimesStolen();
		ThievingContractList.listenPickpocket(player, npcData, index + 1);
		switch (index) {
		case 0:
			return "You successfully pick the " + npc.getDefinitions().name.toLowerCase() + "'s pocket; "
					+ "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.";
		case 1:
			return "Your lighting-fast reactions allow you to steal double loot; " + "times thieved: " + Colors.RED
					+ Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.";
		case 2:
			return "Your lighting-fast reactions allow you to steal triple loot; " + "times thieved: " + Colors.RED
					+ Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.";
		case 3:
			return "Your lighting-fast reactions allow you to steal quadruple loot; " + "times thieved: " + Colors.RED
					+ Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.";
		}
		return null;
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

		increasedChance += 15;

		int level = Utils.getRandom(thievingLevel + increasedChance);
		double ratio = level / (Utils.random(npcData.getThievingLevels()[0] + 6) + 1);
        return !(Math.round(ratio * thievingLevel) < npcData.getThievingLevels()[0]
                / player.getAuraManager().getThievingAccurayMultiplier());
    }

	@Override
	public boolean process(Player player) {
        if (native950Journey != null) return native950Journey.process(player);
		npc.resetWalkSteps();
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
        if (native950Journey != null) return native950Journey.finish(player);
		if (!isSuccesfull(player) && !player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
			player.sendMessage("You fail to pick the " + npc.getDefinitions().name.toLowerCase() + "'s pocket.", true);
			player.sendMessage("Purchase the Sleight of Hand perk to never fail pickpocketing.", true);
			if (npc.getId() != 1911) {
				npc.setNextAnimation(STUN_ANIMATION);
				if (npcData.equals(PickPocketableNPC.MASTER_FARMER) || npcData.equals(PickPocketableNPC.FARMER))
					npc.setNextForceTalk(new ForceTalk("Cor blimey mate, what are ye doing in me pockets?"));
				else
					npc.setNextForceTalk(new ForceTalk("What do you think you're doing?"));
			}
			npc.faceEntity(player);
			player.setNextAnimation(new Animation(424));
			player.setNextGraphics(new Graphics(80, 5, 60));
			player.applyHit(new Hit(player, npcData.getStunDamage(), HitLook.REGULAR_DAMAGE));
			player.lock(npcData.getStunTime());
			stop(player);
		} else {
			player.sendMessage(getMessage(player), true);
			double totalXp = npcData.getExperience();
			if (hasTheivingSuit(player))
				totalXp *= 1.025;
			player.getSkills().addXp(Skills.THIEVING, totalXp);
			ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
			for (int i = 0; i <= index; i++) {
				Item item = npcData.getLoot()[Utils.random(npcData.getLoot().length)];
				if (item.getId() == 995)
					player.addMoney(item.getAmount());
				else
					player.getInventory().addItem(item.getId(), item.getAmount());
			}
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
		return -1;
	}

	@Override
	public boolean start(Player player) {
        if (native950Journey != null) return native950Journey.start(player);
		if (checkAll(player)) {
			int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
			int agilityLevel = player.getSkills().getLevel(Skills.AGILITY);
			if (Utils.getRandom(50) < 5 && npc.getDefinitions().getId() != 1911) {
				for (int i = 0; i < 4; i++) {
					if (i >= npcData.getThievingLevels().length)
						break;
					if (npcData.getThievingLevels()[i] <= thievingLevel
							&& npcData.getAgilityLevels()[i] <= agilityLevel)
						index = i;
				}
			}
			player.faceEntity(npc);
			player.setNextAnimation(getAnimation());
			player.setNextGraphics(getGraphics());

			player.sendMessage("You attempt to steal from the " + npc.getDefinitions().name.toLowerCase() + "..", true);
			setActionDelay(player, 2);
			return true;
		}
		return false;
	}

	@Override
	public void stop(Player player) {
        if (native950Journey != null) { native950Journey.stop(player); return; }
		npc.setNextFaceEntity(null);
		setActionDelay(player, 3);
	}
}