package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.player.content.fistofguthix.FOGManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * The controller for the fist of guthix minigame.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 30, 2017 at 8:09:32 PM.
 */
public class FOGController extends Controller {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.controllers.Controller#start()
	 */
	@Override
	public void start() {
		player.getInventory().addItem(12850, 1000);
		player.getInventory().addItem(12851, 300);
		player.getInventory().addItem(12853, 1);
		player.getInventory().addItem(12853, 1);
		player.getInventory().addItem(12853, 1);
		player.getInventory().addItem(12853, 1);
		player.getInventory().addItem(12853, 1);
		player.getInventory().addItem(12855, 1);
		player.reset(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rs.game.player.controllers.Controller#processMagicTeleport(com.rs.
	 * game.WorldTile)
	 */
	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You cannot leave this minigame at this time!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rs.game.player.controllers.Controller#processItemTeleport(com.rs.game
	 * .WorldTile)
	 */
	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You cannot leave this minigame at this time!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rs.game.player.controllers.Controller#processObjectTeleport(com.rs.
	 * game.WorldTile)
	 */
	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You cannot leave this minigame at this time!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rs.game.player.controllers.Controller#processPlayerOption1(com.rs.game.Entity)
	 */
	@Override
	public boolean processPlayerOption1(Entity target) {
        return FOGManager.get().getFOGInstance().getTeam(player).chased() == target;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.controllers.Controller#keepCombating(com.rs.game.
	 * Entity)
	 */
	@Override
	public boolean keepCombating(boolean mainHand, Entity target) {
		if (player.getTemporaryAttributtes().get("canFight") == Boolean.FALSE) {
			player.getPackets().sendGameMessage("The game hasn't started yet.");
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.controllers.Controller#sendDeath()
	 */
	@Override
	public boolean sendDeath() {
		player.lock(7);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.reset();
					FOGManager.get().getFOGInstance().processDeath(player);
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.controllers.Controller#logout()
	 */
	@Override
	public boolean logout() {
		exit(true);
		if (FOGManager.get() != null && FOGManager.get().getFOGInstance() != null && player != null && FOGManager.get().getFOGInstance().getTeam(player) != null)
			FOGManager.get().getFOGInstance().getTeam(player).forfeit(player);
		return true;
	}

	/**
	 * Exits the FOG controller.
	 */
	public void exit() {
		exit(false);
	}

	/**
	 * Exits the fist of guthix minigame.
	 * 
	 * @param hasTeleported
	 *            The teleport to utilize.
	 */
	public void exit(boolean hasTeleported) {
		if (hasTeleported)
			player.setNextWorldTile(new WorldTile(new WorldTile(1698, 5600, 0), 3));
		player.getInventory().deleteItem(12850, 1000);
		player.getInventory().deleteItem(12851, 300);
		player.getInventory().deleteItem(12853, 1);
		player.getInventory().deleteItem(12853, 1);
		player.getInventory().deleteItem(12853, 1);
		player.getInventory().deleteItem(12853, 1);
		player.getInventory().deleteItem(12853, 1);
		player.getInventory().deleteItem(12855, 1);
		player.getInventory().deleteItem(12845, 1);
		player.getEquipment().deleteItem(12845, 1);
	}

}
