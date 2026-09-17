package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class KalphiteKingInstanceD extends Dialogue {

	private Boss boss;
	private int startStage;

	@Override
	public void start() {
		boss = Boss.Kalphite_King;
		startStage = parameters.length < 1 ? -1 : (int) parameters[0];
		switch (startStage) {
		case 0:
			stage = 0;
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Fast", "Average", "Slow");
			return;
		}
		sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Start a custom session", "Join an existing session",
				"Rejoin my previous session");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				player.setLastBossInstanceSettings(new InstanceSettings(boss));
				player.getLastBossInstanceSettings().setPractiseMode(false);
				sendSelectMaxPlayers();
				break;
			case OPTION_2:
				end();
				player.sendInputName("Enter the name of a player in a battle you wish to join.", new InputNameEvent() {
					@Override
					public void run(Player player) {
						BossInstanceHandler.joinInstance(player, boss, getString().toLowerCase(), false);
					}
				});
				break;
			default:
				String key = player.getLastBossInstanceKey();
				if (key == null) {
					stage = -2;
					sendDialogue("You do not have a battle to rejoin.");
					return;
				}
				if (BossInstanceHandler.findInstance(boss, key) == null) {

					if (key.equals(player.getUsername()) && player.getLastBossInstanceSettings() != null
							&& player.getLastBossInstanceSettings().getBoss() == boss
							&& player.getLastBossInstanceSettings().hasTimeRemaining()) {
						end();
						// if the instance is null, and its my own player, use
						// the settings to recreate it
						BossInstanceHandler.createInstance(player, player.getLastBossInstanceSettings());
						return;
					}

					stage = -2;
					sendDialogue("You do not have a battle to rejoin.");
					return;
				}
				end();
				BossInstanceHandler.joinInstance(player, boss, key, false);
				break;
			}
			break;
		case 0:
			InstanceSettings settings = player.getLastBossInstanceSettings();
			if (settings == null)
				return;
			settings.setSpawnSpeed(componentId == OPTION_1 ? BossInstance.FASTEST
					: componentId == OPTION_2 ? BossInstance.FAST : BossInstance.STANDARD);
			stage = 1;
			sendOptionsDialogue("Join the battle?", "Yes", "No, i want to leave.");
			break;
		case 1:
			end();
			if (componentId == OPTION_1)
				startInstance();
			break;
		default:
			end();
			break;
		}

	}

	public void sendSelectMaxPlayers() {
		end();
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if (settings == null)
			return;
		settings.setMinCombat(1);
		settings.setProtection(BossInstance.FFA);
		player.sendInputInteger("Choose the maximum number of players. (1-" + boss.getMaxPlayers() + ")", new InputIntegerEvent() {
			@Override
			public void run(Player player) {
				final int value = getInteger();
				if (value <= 0 || boss == null)
					return;
				InstanceSettings settings = player.getLastBossInstanceSettings();
				if (settings == null)
					return;
				if (boss == Boss.Kalphite_King) {
					settings.setMaxPlayers(value);
					player.getDialogueManager().startDialogue("KalphiteKingInstanceD", 0);
					return;
				}
			}
		});
	}

	public void startInstance() {
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if (settings == null)
			return;
		int initialCost = settings.getBoss().getInitialCost();
		if (player.getInventory().getCoinsAmount() < initialCost) {
			player.getPackets().sendGameMessage("You don't have enough coins to start this battle.");
			player.setLastBossInstanceSettings(null);
			return;
		}
		if (initialCost > 0)
			player.getInventory().removeItemMoneyPouch(new Item(995, initialCost));
		settings.setCreationTime(Utils.currentTimeMillis());
		BossInstanceHandler.createInstance(player, settings);
	}

	@Override
	public void finish() {

	}

}
