package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.Trex.Trex;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.List;

public class TrexController extends BossInstanceController {//then the controller for solak and lost grove area for other npcs
	private transient Trex current;


	@Override
	public void start() {
		sendInterfaces();

	}

	@Override
	public boolean login() {
		player.setNextWorldTile(new WorldTile(5418, 2339, 0));
		removeControler();//this is disabled for testing saves me running bk to solak all the time i can just spawn back when logging in
		return true;
	}




	@Override
	public void sendInterfaces() {
		if (current == null)
			return;


			updateInterface();
			player.getInterfaceManager().sendOverlay(1648, true);



	}


	public void updateInterface() {
		if (current == null)
			return;


		player.getPackets().sendConfig(5776, current.getBossMapId());
		player.getPackets().sendIComponentText(1648,27, "Trex Health");
		player.getVarBitManager().sendVarBit(32672, current.getMaxHitpoints() * 10);
		player.getVarBitManager().sendVarBit(28663, current.getHitpoints() * 10);
	}

	@Override
	public boolean canHit(Entity entity) {
		if (entity instanceof Trex && current != entity) {
			current = (Trex) entity;
			player.getInterfaceManager().closeOverlay(true);
			sendInterfaces();
		}
		if (current != null && current instanceof Trex && current.getId() != 26435) {
			current = null;
			player.getInterfaceManager().closeOverlay(true);
			return false;
		}


		return super.canHit(entity);
	}

	@Override
	public boolean logout() {
		return false;
	}
	private transient List<Trex.Flame> flames;
	private Trex trex;

	@Override
	public void magicTeleported(int teleType) {
		player.getInterfaceManager().closeOverlay(true);

		player.getControlerManager().forceStop();
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 84909 && !player.isUnderCombat()) {
			player.lock(1);
			if (object.getX() == 3087 && object.getY() == 6175) {
				player.getInterfaceManager().closeOverlay(true);


				removeControler();//this is disabled for testing saves me running bk to solak all the time i can just spawn back when logging in
				player.setNextWorldTile(new WorldTile(5427, 2342, 0));
				player.getControlerManager().forceStop();
				return false;
			}
		}
		return true;
	}
	

	@Override
	public boolean sendDeath() {


		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
					player.sendMessage("Oh dear, you have died.");
				}
				if (loop == 3) {
					player.setNextWorldTile(new WorldTile(5427, 2342, 0));
					player.setNextAnimation(new Animation(-1));
					player.getControlerManager().forceStop();
					player.getInterfaceManager().closeOverlay(true);

					removeControler();//this is disabled for testing saves me running bk to solak all the time i can just spawn back when logging in
					player.getPackets().sendMusicEffect(90);

					player.reset();
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity target) {
		if (target instanceof Trex && current != target) {
			current = (Trex) target;
			player.getInterfaceManager().closeOverlay(true);
			sendInterfaces();
		}
		if (current != null && current instanceof Trex && current.getId() != 26435) {
			current = null;
			player.getInterfaceManager().closeOverlay(true);
			return false;
		}
		return super.keepCombating(mainHand, target);
	}

	@Override
	public void process() {
		if (current == null)
			return;
		if (current != null && current.getCombat().getTarget() == null) {
			if (current != null && current instanceof Trex && current.getId() != 26435) {
				current = null;
				player.getInterfaceManager().closeOverlay(true);
				return;
			}
			if (current.isDead() || current.hasFinished()) {
				current = null;
				player.getInterfaceManager().closeOverlay(true);
			}
		}
	}
}

