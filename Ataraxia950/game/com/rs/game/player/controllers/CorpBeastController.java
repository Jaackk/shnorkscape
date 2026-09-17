package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CorpBeastController extends Controller {

	@Override
	public boolean login() {
		return false; // so doesnt remove script
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void magicTeleported(int type) {
		removeControler();
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 37929 || object.getId() == 38811) {
			removeControler();
			player.stopAll();
			player.setNextWorldTile(new WorldTile(2970, 4384, player.getPlane()));
			return false;
		}
		return true;
	}

	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
        return !s.equals("b") && !s.contains("bank");
    }
	
	@Override
	public boolean sendDeath() {
		if (player.isGroupIronman()) {
			player.gimTracker.incrementDeaths("<#player> died while fighting the Corporeal Beast");
		}
		final boolean safe = player.getTemporaryAttributtes().remove("safedeath") != null;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
					player.deathItemsManager.handleDeath();
				} else if (loop == 1) {
					player.sendMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					player.getPackets().sendMusicEffect(90);
					player.getDeathManager().reset();
					player.getDeathManager().setDeathCoordinates(new WorldTile(Utils.random(2955,  2960), Utils.random(4380, 4385), 2));
					player.reset();
					player.unlock();
					player.setNextAnimation(new Animation(-1));
					stop();
				} else if (loop == 4) {
					removeControler();
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void start() {

	}
}