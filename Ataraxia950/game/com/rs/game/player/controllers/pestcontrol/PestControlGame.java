package com.rs.game.player.controllers.pestcontrol;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.activites.pest.PestControlObject;
import com.rs.game.npc.pest.Brawler;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class PestControlGame extends Controller {

	private PestControl control;
	private double points;

	@Override
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		if (control.getBrawlers() != null) {
			for (Brawler npc : control.getBrawlers()) {
				if (npc == null || npc.isDead() || npc.hasFinished())
					continue;
				if (Utils.colides(nextX, nextY, player.getSize(), npc.getX(), npc.getY(), npc.getSize()))
					return false;
			}
		}
		return true;

	}
	
	public PestControl getGame() {
		return control;
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
		return s.contains("ticket");
	}

	@Override
	public boolean canSummonFamiliar() {
		player.getPackets().sendGameMessage("You feel it's best to keep your Familiar away during this game.");
		return false;
	}

	@Override
	public void forceClose() {
		control.leave(player, false);
		removeControler();
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}

	@Override
	public boolean login() {
		removeControler();
		player.setNextWorldTile(new WorldTile(Settings.RESPAWN_PLAYER_LOCATION));
		return false;
	}

	@Override
	public boolean logout() {
		control.leave(player, true);
		removeControler();
		return false;
	}

	@Override
	public void magicTeleported(int teleType) {
		control.leave(player, false);
		removeControler();
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean sendDeath() {
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
					player.setNextWorldTile(control.getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))));
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

	@Override
	public void sendInterfaces() {
		updatePestPoints();
		player.getInterfaceManager().sendMinigameHudInterface(408);
	}

	@Override
	public void start() {
		control = (PestControl) getArguments()[0];
		setArguments(null);
		setPoints(0.0D);
		sendInterfaces();
		player.setForceMultiArea(true);
	}

	@Override
	public void trackXP(int skillId, int addedXp) {
		if (skillId == 3) // hp
			setPoints(getPoints() + ((addedXp) * 2.5));
		updatePestPoints();
	}

	private void updatePestPoints() {
		boolean isGreen = getPoints() > 500;
		player.getPackets().sendIComponentText(408, 11, (isGreen ? "<col=75AE49>" : "") + (int) getPoints() + "</col>");
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 91332) {
			World.removeObject(object);
			object.setId(object.getId() + 4);
			World.spawnObject(object);
			return false;
		}
		if (object.getId() == 91336) {
			World.removeObject(object);
			object.setId(object.getId() - 4);
			World.spawnObject(object);
			return false;
		}
		if (object.getId() >= 91332 && object.getId() <= 91339) {
			player.getPackets().sendGameMessage(
					"The door is damaged and can't be " + (object.getId() >= 91337 ? "closed" : "opened") + ".");
			return false;
		}
		if (object.getId() == 14296) {
			int rot = object.getRotation();
			boolean outSide = rot == 0 ? player.getY() >= object.getY()
					: rot == 1 ? player.getX() >= object.getX() : player.getX() <= object.getX();
			WorldTile toTile = new WorldTile(
					rot == 0 ? object.getX()
							: (outSide ? (rot == 1 ? object.getX() - 1 : object.getX() + 1)
									: (rot == 1 ? object.getX() + 1 : object.getX() - 1)),
					rot == 0 ? (outSide ? object.getY() - 1 : object.getY() + 1) : object.getY(), object.getPlane());
			player.useStairs(outSide ? 828 : 827, toTile, 1, 2);
			return false;
		}
		if (object.getId() == 91455) {
			takeLogs(1);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick3(WorldObject object) {
		if (object.getId() == 91455) {
			takeLogs(5);
			return false;
		}
		if (object instanceof PestControlObject) {
			PestControlObject pestControlObject = (PestControlObject) object;
			if (pestControlObject.isBeingRepaired())
				return false;
			if (!player.getInventory().containsItem(1511, 1)) {
				player.getPackets().sendGameMessage("You will need some logs to repair that.");
				return false;
			}
			final int id = pestControlObject.getId();
			boolean gate = id >= 91332 && id <= 91339;
			pestControlObject.setBeingRepaired(true);// gate 159
			player.setNextAnimation(new Animation(gate ? 3676 : 3683));
			player.lock();
			player.getInventory().deleteItem(1511, 1);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.unlock();
					setPoints(getPoints() + 50);
					updatePestPoints();
					pestControlObject.setId(gate ? id - 1 : id - 3);
					World.spawnObject(pestControlObject);
					pestControlObject.setBeingRepaired(false);
				}

			}, 1);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick4(WorldObject object) {
		if (object.getId() == 91455) {
			takeLogs(10);
			return false;
		}
		return true;
	}

	public void takeLogs(int amount) {
		if (player.getInventory().getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("You don't have enough space in your inventory.");
			return;
		}
		player.lock();
		player.setNextAnimation(new Animation(827));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getInventory().addItem(1511, amount);
				player.unlock();
			}
		});
	}

}