package com.rs.game.activites.creations;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

public class CreationSkillsAction extends Action {

	private final Animation animation;
	private final WorldObject object;
	private final int objectIndex;
	private CreationObjects definitions;
	private final Item itemUsed;
	private final int baseId;
	private final int skillId;

	public CreationSkillsAction(WorldObject object, Animation animation, Item bestItem, int baseId, int objectIndex,
			int skillId) {
		this.skillId = skillId;
		this.object = object;
		this.animation = animation;
		this.itemUsed = bestItem;
		this.baseId = baseId;
		this.objectIndex = objectIndex;
	}

	// 50 == class 4

	public int getSkillTimer(Player player, int skillId) {
		int playerLevel = player.getSkills().getLevel(skillId);
		int fishLevel = definitions.level;
		int modifier = getToolLevelModifier();
		int randomAmt = Utils.random(4);
		double cycleCount = 1, otherBonus = 0;
		cycleCount = Math.ceil(((fishLevel + otherBonus) * 50 - playerLevel * 10) / modifier * 0.25 - randomAmt * 4);
		if (cycleCount < 1)
			cycleCount = 1;
		int delay = (int) cycleCount + 1;
		if (skillId == Skills.FISHING)
			delay /= player.getAuraManager().getFishingAccurayMultiplier();
		return delay;
	}

	private int getToolLevelModifier() {
		if (itemUsed == null || itemUsed.getId() < 0 || baseId < 0) {
			return 1;
		}
		int offset = itemUsed.getId() - baseId;
		if (offset < 0 || offset % 2 != 0) {
			return 1;
		}
		int toolIndex = offset / 2;
		return toolIndex <= 0 ? 1 : Math.min(80, toolIndex * 20);
	}

	@Override
	public boolean process(Player player) {
		if (itemUsed == null)
			return false;
		if (animation != null)
			player.setNextAnimation(animation);
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, drop something.");
			return -1;
		} else if (Utils.getRandom(definitions.getRandomLife()) == 0) {
		//	Logger.getGlobal().info("Empty");
		}
		if (player.getInventory().addItem(new Item(StealingCreation.SACRED_CLAY[objectIndex], 1))) {
			StealingCreation.recordGathering(player, objectIndex);
		}
		return getSkillTimer(player, skillId);
	}

	@Override
	public boolean start(Player player) {
		definitions = CreationObjects.forIndex(objectIndex);
		if (player.getSkills().getLevel(skillId) < definitions.getLevel() || itemUsed == null)
			return false;
		setActionDelay(player, getSkillTimer(player, skillId));
		player.setNextFaceWorldTile(object);
		return true;
	}

	@Override
	public void stop(Player player) {
		this.setActionDelay(player, 3);
	}

	public enum CreationObjects {

		CLASS_1(10, -1, 1, 1), // doesnt run out copper

		CLASS_2(20, 200, 6, 20), // silver

		CLASS_3(25, 300, 12, 40), // mithril

		CLASS_4(30, 400, 16, 60), // adamant

		CLASS_5(35, 500, 20, 80); // rune

		private final int baseTime;
        private final int randomTime;
        private final int randomLife;
        private final int level;

		CreationObjects(int baseTime, int randomLife, int randomTime, int level) {
			this.baseTime = baseTime;
			this.randomTime = randomTime;
			this.randomLife = randomLife;
			this.level = level;
		}

		public int getBaseTime() {
			return baseTime;
		}

		public int getLevel() {
			return level;
		}

		public int getRandomLife() {
			return randomLife;
		}

		public int getRandomTime() {
			return randomTime;
		}

		private static CreationObjects forIndex(int index) {
			CreationObjects[] values = values();
			if (index < 0) {
				return CLASS_1;
			}
			if (index >= values.length) {
				return CLASS_5;
			}
			return values[index];
		}
	}
}
