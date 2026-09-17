package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.dialogue.Dialogue;

public class CookingD extends Dialogue {

	private Cookables[] cooking;
	private WorldObject object;

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= cooking.length || slot < 0)
			return;
		player.getActionManager().setAction(new Cooking(object, cooking[slot].getRawItem(), PortableStation.isPortableObject(object)));
		end();
	}

	@Override
	public void start() {
		this.cooking = parameters[0] instanceof Cookables ? new Cookables[] { (Cookables) parameters[0] } : parameters[0] instanceof Cookables[] ?  (Cookables[]) parameters[0] : null;
		if (cooking == null) {
		    player.getDialogueManager().startDialogue("SimpleMessage", "You can't use that item on the fire.");
		    return;
		}
		this.object = (WorldObject) parameters[1];
		int[] ids = new int[cooking.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = cooking[i].getRawItem().getId();
		
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.COOK, "Which raw item do you wish to cook?", -1, ids, null, false);
	}

	@Override
	public void finish() {
	}
}