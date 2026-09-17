package com.rs.game.player.dialogue.impl;

import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class BossInstanceD extends Dialogue {

	private Boss boss;

	@Override
	public void start() {
		boss = (Boss) parameters[0];
		if (boss.hasPublicVersion()) {
			stage = 0;
			sendOptionsDialogue("Choose an Option", "Start an Instance", "Join an existing instance",
					"Re-join your Instance");
		} else
			sendCustomEncounter();
	}

	private void sendCustomEncounter() {
		stage = 0;
		sendOptionsDialogue("Choose an Option", "Start", "Join", "Rejoin");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				BossInstanceHandler.joinInstance(player, boss, "", false);
				end();
				break;
			default:
				sendCustomEncounter();
				break;
			}
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				stage = 2;
				player.setLastBossInstanceSettings(new InstanceSettings(boss)); // the
																				// settings
				if (player.getLastBossInstanceSettings() == null) {
					end();
					return;
				}
				player.getLastBossInstanceSettings().setPractiseMode(componentId == OPTION_1);
				if (boss.isHasHM()) {
					stage = 2;
					sendOptionsDialogue("Enable hard mode?", "Yes.", "No.");
				} else
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
				// You do not have a battle to rejoin.
				break;
			}
			break;
		case 2:
			if (player.getLastBossInstanceSettings() == null) {
				end();
				return;
			}
			player.getLastBossInstanceSettings().setHardMode(componentId == OPTION_1);
			sendSelectMaxPlayers();
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
		settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
		settings.setMinCombat(1);
		settings.setSpawnSpeed(BossInstance.FASTEST);
		settings.setProtection(BossInstance.FFA);
		startInstance();
	}

	public void startInstance() {
		InstanceSettings settings = player.getLastBossInstanceSettings();
		if (settings == null)
			return;
		if (settings.getBoss() == Boss.Vorago && settings.isHardMode()) {
			if (!player.isCanStartHardModeVorago(true))
				return;
		}
		int initialCost = settings.getBoss().getInitialCost();
		if (!player.hasMoney(initialCost)) {
			player.getPackets().sendGameMessage("You don't have enough coins to start this battle.");
			player.setLastBossInstanceSettings(null);
			return;
		}
		if (initialCost > 0)
			player.takeMoney(initialCost);
		settings.setCreationTime(Utils.currentTimeMillis());
		BossInstanceHandler.createInstance(player, settings);
	}

	@Override
	public void finish() {
	}
}