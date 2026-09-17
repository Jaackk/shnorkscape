package com.rs.game.player.content.dungeoneering.skills.construction;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class DungeoneeringConstruction {

	public static final void handleInterface(Player player, int componentId) {
		switch(componentId) {
		case 112:
			buildObject(player, 0);
			//gatestone
			break;
		case 23:
			buildObject(player, 1);
			//photo booth
			break;
		case 95:
			buildObject(player, 2);
			//farming patch
			break;
		case 78:
			buildObject(player, 3);
			//cooking range
			break;
		case 61:
			buildObject(player, 4);
			//prayer altar
			break;
		case 44:
			buildObject(player, 5);
			//Skillcape stand
			break;
		}
	}
	
	private static final void buildObject(Player player, int ordinal) {
		DungeoneeringConstructionData data = DungeoneeringConstructionData.values()[ordinal];
		WorldObject object = (WorldObject) player.getTemporaryAttributtes().get("dgconstreplacement");
		/*for (int i = 0; i < data.getMaterials().length; i++) {
			if (!player.getInventory().containsItem(data.getMaterials()[i])) {
				player.sendMessage("You don't have the necessary items to build this.");
				return;
			}
		}*/
		player.lock(3);
		player.setNextAnimation(new Animation(3683));
		int type = getType(player.getDungeoneeringManager().getParty().getFloor());
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getSkills().addXp(Skills.CONSTRUCTION, data.getExperience());
				World.removeObject(object);
				int rotation = 1 + player.getDungeoneeringManager().getParty().getDungeon().getRoom(player.getDungeoneeringManager().getParty().getDungeon().getCurrentRoomReference(player)).getRotation();
				World.spawnObject(new WorldObject(data.getObjectIds()[type], data == DungeoneeringConstructionData.PHOTO_BOOTH ? 0 : 10, rotation, object));
				for (int i = 0; i < data.getMaterials().length; i++)
					player.getInventory().deleteItem(data.getMaterials()[i]);
				for (int i = 0; i < 3; i++)
					World.spawnObject(new WorldObject(51175, 0, rotation, new WorldTile(object.getX() + i, object.getY(), object.getPlane())));
			}
		}, 2);
	}
	
	private static final int getType(int floor) {
		if (floor < 12)
			return 0;
		else if (floor < 18)
			return 1;
		else if (floor < 30)
			return 2;
		else if (floor < 36)
			return 1;
		else if (floor < 48)
			return 3;
		else
			return 4;
	}
	
}
