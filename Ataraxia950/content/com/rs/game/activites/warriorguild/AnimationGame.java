package com.rs.game.activites.warriorguild;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.io.Serializable;
import java.util.HashMap;

public class AnimationGame implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8004106287509832157L;
	private static final HashMap<Integer, WarriorsArmour> armours = new HashMap<Integer, WarriorsArmour>();

	static {
		for (WarriorsArmour set : WarriorsArmour.values()) {
			for (int i : set.getArmour())
				armours.put(i, set);
		}
	}

	/**
	 * The instance for the player
	 */
	private final transient Player player;

	public AnimationGame(Player player) {
		this.player = player;
	}

	public boolean containsItem(int id) {
		Item item = new Item(id);
		return containsItem(item);
	}

	public boolean containsItem(Item item) {
		return player.getInventory().getItems().contains(new Item(item.getId(), 1))
				|| player.getEquipment().getItems().contains(new Item(item.getId(), 1));
	}

	public void createWarrior(int armours, final WorldObject object) {
		final WarriorsArmour sets = WarriorsArmour.forId(armours);
		if (sets == null || sets.getArmour() == null)
			return;
		if (sets.getArmour()[0] == armours || sets.getArmour()[1] == armours || sets.getArmour()[2] == armours) {
			if (player.getInventory().containsItem(sets.getArmour()[0], 1)
					&& player.getInventory().containsItem(sets.getArmour()[1], 1)
					&& player.getInventory().containsItem(sets.getArmour()[2], 1)) {
				final boolean running = player.isRunning();
				player.getInventory().deleteItem(sets.getArmour()[0], 1);
				player.getInventory().deleteItem(sets.getArmour()[1], 1);
				player.getInventory().deleteItem(sets.getArmour()[2], 1);
				player.setRun(false);
				player.setRunHidden(false);
				player.addWalkSteps(player.getX(), player.getY() + 5, -1, false);
				player.setNextAnimation(new Animation(827));
				player.lock(4);
				player.getDialogueManager().startDialogue("WAnimator");
				WorldTasksManager.schedule(new WorldTask() {

					boolean secondloop;

					@Override
					public void run() {
						if (!secondloop) {
							secondloop = true;
							player.setRunHidden(running);
						} else {
							stop();
						}
					}
				}, 0, 6);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						final NPC war = new NPC(sets.getNpcId(), new WorldTile(object.getX(), object.getY(), 0), -1,
								true);
						war.addWalkSteps(player.getY(), player.getY());
						player.setNextFaceEntity(war);
						war.setTarget(player);
						war.setNextAnimation(new Animation(4166));
						war.setNextForceTalk(new ForceTalk("I'M ALIVE!"));
						player.getInterfaceManager().closeChatBoxInterface();
						player.getHintIconsManager().addHintIcon(war, 1, -1, false);
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								war.setFinished(true);
								war.setForceWalk(new WorldTile(-1, -1, -1));
								World.removeNPC(war);
								stop();
							}

						}, 60, 60);
						stop();
						return;
					}

				}, 3, 3);
			} else {
				return;
			}
			return;
		}
	}

	/**
	 * Handles the operations when you enter the room.
	 *
	 * @param player
	 */

	public void enterRoom(final Player player) {
		final WorldTile inFightingRoom = new WorldTile(2847, 3536, 2);
		final WorldTile outside = new WorldTile(2846, 3536, 2);
		if (player.getX() == 2846 || player.getX() == 2845) {
			player.setNextWorldTile(inFightingRoom);
		} else {
			player.setNextWorldTile(outside);
		}
	}

	public Item getDefender() {
		int id = 8844;
		if (containsItem(8850) || containsItem(20072)) {
			id = 20072;
		} else if (containsItem(8849) || containsItem(8850)) {
			id = 8850;
		} else if (containsItem(8848)) {
			id = 8849;
		} else if (containsItem(8847)) {
			id = 8848;
		} else if (containsItem(8846)) {
			id = 8847;
		} else if (containsItem(8845)) {
			id = 8846;
		} else if (containsItem(8844)) {
			id = 8845;
		} else {
			id = 8844;
		}
		return new Item(id);
	}

	public boolean hasDefender() {
		return containsItem(8844) || containsItem(8845) || containsItem(8846) || containsItem(8847) || containsItem(8848)
				|| containsItem(8849) || containsItem(8850) || containsItem(20072);
	}
}