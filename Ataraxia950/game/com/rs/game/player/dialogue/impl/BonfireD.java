package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.dialogue.Dialogue;

/**
 * BonfireD.java | 11:27:52 AM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class BonfireD extends Dialogue {

	private Log[] logs;
	private WorldObject object;

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= logs.length || slot < 0)
			return;
		player.getActionManager().setAction(new Bonfire(logs[slot], object, PortableStation.isPortableObject(object)));
		end();
	}

	@Override
	public void start() {
		this.logs = (Log[]) parameters[0];
		this.object = (WorldObject) parameters[1];
		int[] ids = new int[logs.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = logs[i].getLogId();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.SELECT, "Which logs do you want to add to the bonfire?", -1, ids, null, false);
	}

}