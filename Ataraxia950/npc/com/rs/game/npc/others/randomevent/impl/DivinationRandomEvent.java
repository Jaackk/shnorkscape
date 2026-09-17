package com.rs.game.npc.others.randomevent.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.randomevent.RandomEventNPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.DivinationHarvest;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Sep 16, 2018.
 */
public class DivinationRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = 8419238716884007416L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public DivinationRandomEvent(WorldTile tile, Player target) {
		super(18204, tile, target);
		setNextForceTalk(null);
	}

	@Override
	public void giveReward(Player player) {
		if (player.isLocked())
			return;
		if (player != randomEventTarget) {
			player.sendMessage(Colors.RED + "<shad=000000>This isn't your Chronicle to capture.");
			return;
		}
		player.lock();
		int amount = 1;
		if (DivinationHarvest.hasElderDivinationOutfit(player) && Utils.random(100) < 7) {
			amount = 2;
			player.sendMessage("Your elder divination set split the chronicle in two, and you catch them both.");
		} else if (DivinationHarvest.hasDivinationOutfit(player) && Utils.random(100) < 5) {
			amount = 2;
		}
		player.getInventory().addItemDrop(new Item(29293, amount));
		player.getSkills().addXp(Skills.HUNTER, player.getSkills().getLevelForXp(Skills.HUNTER) * 10 * amount);
		player.sendMessage("You catch the chronicle fragment.");
		randomEventTarget.setCurrentRandomEventNPC(null);
		player.unlock();
		finish();
	}

	@Override
	public void processNPC() {
		if (randomEventTarget.hasFinished() || getCreateTime() + 60000 < Utils.currentTimeMillis()) {
			randomEventTarget.setCurrentRandomEventNPC(null);
			finish();
		}
	}

}
