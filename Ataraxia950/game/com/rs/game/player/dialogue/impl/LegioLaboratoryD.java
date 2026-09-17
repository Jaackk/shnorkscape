package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * Handles the Legio boss doors.
 *
 * @author Noel
 */
public class LegioLaboratoryD extends Dialogue {

	/**
	 * Represents the Laboratory door World Object.
	 */
	private WorldObject door;

	@Override
	public void start() {
		door = (WorldObject) parameters[0];
		if (player.getSkills().getLevel(Skills.SLAYER) < 95) {
			sendDialogue("Legio Bosses require at least a Slayer level of 95 to fight.");
			stage = 99;
			return;
		}
		sendDialogue("The Legiones are significantly more dangerous than the other Ascended.",
				"Make sure you are prepared before entering the labs.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			if (!hasKeystone(false)) {
				sendDialogue("You do not have the requirred keystone in order to enter the boss room.");
				stage = 99;
				return;
			}
			sendOptionsDialogue("Are you sure you want to enter? This will use up your keystone!", "Yes, I am ready.",
					"No, I need to prepare.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				end();
				if (hasKeystone(true))
					return;
				player.sendMessage("ERROR;");
				break;
			case OPTION_2:
				end();
				break;
			}
			break;

		case 99:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

	/**
	 * Checks if the player has the Keystone to enter boss room.
	 *
	 * @return if the Player has the requirred Keystone.
	 */
	private boolean hasKeystone(boolean remove) {
	    int doorIndex = door.getId() - 84726;
	    int keyId = 28445 + (doorIndex * 2);
        if (remove) {
            startInstance(Boss.values()[Boss.LEGIO_PRIMUS.ordinal() + doorIndex]);
            return player.getInventory().deleteOneItem(new Item(keyId));
        }
        return player.getInventory().containsOneItem(keyId);
	}
	
	public void startInstance(Boss boss) {
         player.setLastBossInstanceSettings(new InstanceSettings(boss));
	     InstanceSettings settings = player.getLastBossInstanceSettings();
	     settings.setSpawnSpeed(BossInstance.FAST);
	     settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
	     settings.setMinCombat(1);
	     settings.setProtection(BossInstance.FFA);
	     settings.setCreationTime(Utils.currentTimeMillis());
	     BossInstanceHandler.createInstance(player, settings);
	}
}