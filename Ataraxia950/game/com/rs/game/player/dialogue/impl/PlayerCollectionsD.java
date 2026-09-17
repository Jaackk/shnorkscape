package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.dropcollection.DropCollectionInterface;
import com.rs.game.player.content.interfaces.BossTimerInterface;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

public final class PlayerCollectionsD extends Dialogue {

	@Override
	public void start() {
	sendOptionsDialogue("Select an option", "Collection log", "Chest Teleport");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			if(componentId == OPTION_1){
				DropCollectionInterface.sendInterface(player);
				return;
			} else if(componentId == OPTION_2){
				//BossTimerInterface.sendInterface(player);
				val transportTime = 1;
				val sophanemSlayerDungeonTile = new WorldTile(2384, 6793, 3);
				Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2384, 6793, 3));
				player.lock();
				FadingScreen.fade(player, transportTime, () -> {
					player.setNextWorldTile(sophanemSlayerDungeonTile);
					player.getControlerManager().startControler("SophanemSlayerDungeon");
					player.unlock();
				});

			}
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}