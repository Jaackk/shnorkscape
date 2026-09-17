package com.rs.game.player.cutscenes;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.cutscenes.actions.LookCameraAction;
import com.rs.game.player.cutscenes.actions.PosCameraAction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;

public class RiseOfTheSixCutscene extends Cutscene {

	@Override
	public boolean hiddenMinimap() {
		return true;
	}
	
	private WorldTile posCameraAction;
	private WorldTile lookCameraAction;
	private static final Animation COLLAPSE = new Animation(21960);
	private static final Animation ENGULF = new Animation(21958);

	@Override
	public CutsceneAction[] getActions(Player player) {
		final Controller controller = player.getControlerManager().getControler();
		if (controller == null)
			return null;
		final RiseOfTheSix instance = ((RiseOfTheSixController) controller).getInstance();
		if (instance == null)
			return null;
		player.lock(13);
		final WorldObject northernPillar = World.getObjectWithType(instance.getWorldTile(32, 27), 10);
		final WorldObject southernPillar = World.getObjectWithType(instance.getWorldTile(32, 11), 10);
		final WorldObject portal = World.getObject(instance.getWorldTile(33, 19));
		final ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
		posCameraAction = instance.getWorldTile(31, 23);
		lookCameraAction = instance.getWorldTile(35, 15);
		actionsList.add(new PosCameraAction(posCameraAction.getX(), posCameraAction.getY(), 6000, -1));
		actionsList.add(new LookCameraAction(lookCameraAction.getX(), lookCameraAction.getY(), 8000, 2));
		posCameraAction = instance.getWorldTile(25, 30);
		lookCameraAction = instance.getWorldTile(40, 10);
		actionsList.add(new PosCameraAction(posCameraAction.getX(), posCameraAction.getY(), 3000, -1));
		actionsList.add(new LookCameraAction(lookCameraAction.getX(), lookCameraAction.getY(), 3000, 2));
		posCameraAction = instance.getWorldTile(18, 20);
		lookCameraAction = instance.getWorldTile(28, 20);
		actionsList.add(new PosCameraAction(posCameraAction.getX(), posCameraAction.getY(), 5000, -1));
		actionsList.add(new LookCameraAction(lookCameraAction.getX(), lookCameraAction.getY(), 2500, 4));
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 2) {
					player.getPackets().sendObjectAnimation(northernPillar, COLLAPSE);
					player.getPackets().sendObjectAnimation(southernPillar, COLLAPSE);
				} else if (ticks == 5) {
					player.getPackets().sendObjectAnimation(portal, ENGULF);
				} else if (ticks == 13) {
					player.getPackets().sendDestroyObject(portal);
					player.getPackets().sendSpawnedObject(new WorldObject(88089, northernPillar.getType(), northernPillar.getRotation(), northernPillar.getX(), northernPillar.getY(), northernPillar.getPlane()));
					player.getPackets().sendSpawnedObject(new WorldObject(88089, southernPillar.getType(), southernPillar.getRotation(), southernPillar.getX(), southernPillar.getY(), southernPillar.getPlane()));
					stop();
				}
				ticks++;
			}
		}, 0, 0);
		return actionsList.toArray(new CutsceneAction[actionsList.size()]);
	}
}