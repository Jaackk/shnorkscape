package com.rs.game.player.content.dungeoneering.skills.woodcutting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.HatchetDefinitions;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public final class DungeoneeringWoodcutting extends Action {

	private final WorldObject tree;
	private final DungeoneeringTreeDefinitions definitions;
	private HatchetDefinitions hatchet;

	public DungeoneeringWoodcutting(WorldObject tree, DungeoneeringTreeDefinitions definitions) {
		this.tree = tree;
		this.definitions = definitions;
	}

	private void addLog(Player player) {
		player.getSkills().addXp(Skills.WOODCUTTING, definitions.getXp());
		String logName = ItemDefinitions.getItemDefinitions(definitions.getLogsId()).getName().toLowerCase();
		player.addLogsChopped();
		player.getPackets().sendGameMessage("You get some " + logName + "; total chopped: " + Colors.RED + Utils.getFormattedNumber(player.getLogsChopped()) + "</col>.", true);
		player.getInventory().addItem(definitions.getLogsId(), 1);
		if (Utils.random(100) < player.getRingOfKinship().getBoost(RingOfKinship.GATHERER))
			if (player.getInventory().addItemDrop(definitions.getLogsId(), 1))
				player.getPackets().sendGameMessage("You manage to cut another " + logName + ".");
	}

	private boolean checkAll(Player player) {
		if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
			player.sendMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		hatchet = Woodcutting.getHatchetDefinitions(player, true);
		if (hatchet == null) {
			player.getPackets().sendObjectMessage(0, 15263739, tree, "You need a hatchet to chop down the " + tree.getDefinitions().name + ".");
			return false;
		}
		if (!hasWoodcuttingLevel(player))
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return false;
		}
		return true;
	}

	private boolean checkTree(Player player) {
		return World.containsObjectWithId(tree, tree.getId());
	}

	private int getWoodcuttingDelay(Player player) {
		int wcTimer = definitions.getLogBaseTime() - player.getSkills().getLevel(8) - Utils.getRandom(hatchet.getAxeTime());
		if (wcTimer < 1 + definitions.getLogRandomTime())
			wcTimer = 1 + Utils.getRandom(definitions.getLogRandomTime());
		return wcTimer;
	}

	private boolean hasWoodcuttingLevel(Player player) {
		if (definitions.getLevel() > player.getSkills().getLevel(8)) {
			player.sendMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.getEmoteId()));
		return checkTree(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addLog(player);
		if (Utils.random(100) > (player.getGorajanTrailblazer().getExperienceBoost() - 1) * 100) {
			if (Utils.getRandom(definitions.getRandomLifeProbability()) == 0) {
				long time = definitions.getRespawnDelay() * 600;
				World.spawnObjectTemporary(new WorldObject(definitions.getStumpId(), tree.getType(), tree.getRotation(), tree.getX(), tree.getY(), tree.getPlane()), time);
				if (tree.getPlane() < 3) {
					WorldObject object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY() - 1, tree.getPlane() + 1));
					if (object == null) {
						object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY() - 1, tree.getPlane() + 1));
						if (object == null) {
							object = World.getStandartObject(new WorldTile(tree.getX() - 1, tree.getY(), tree.getPlane() + 1));
							if (object == null)
								object = World.getStandartObject(new WorldTile(tree.getX(), tree.getY(), tree.getPlane() + 1));
						}
					}
					if (object != null)
						World.removeObjectTemporary(object, time, true);
				}
				player.setNextAnimation(new Animation(-1));
				return -1;
			}
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return -1;
		}
		return getWoodcuttingDelay(player);
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		player.sendMessage("You swing your hatchet at the tree...", true);
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}
}