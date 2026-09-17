package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.instances.VindictaInstance;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class VindictaInstanceD extends Dialogue {

	
	private Instance instance;
	private int duration, speed, limit, password = -1, bossId;
	private boolean isInInstance;
	
	@Override
	public void start() {
		this.bossId = (int) parameters[0];
		if (parameters.length > 1) {
			this.speed = (int) parameters[1];
			if (parameters.length > 2)
				this.limit = (int) parameters[2];
			if (parameters.length > 3)
				this.password = (int) parameters[3];
		}
		if (parameters.length > 3) {
			sendOptionsDialogue("Select the duration of the instance.",
					"Half an hour.",
					"One hour.",
					"An hour and a half.",
					"Two hours.");
			stage = 13;
			return;
		} else if (parameters.length > 2) {
			sendOptionsDialogue("Protect instance with a password?",
					"Yes, add a password.",
					"No, don't add a password.");
			stage = 12;
			return;
		}
		for (int i = 0; i < World.getInstances().size(); i++) {
			if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())){
				instance = World.getInstances().get(i);
				break;
			}
		}
		if (instance == null)
		sendOptionsDialogue("What would you like to do?",
				"Start an instance",
				"Join an instance");
		else {
			if (player.withinDistance(new WorldTile(3111, 6895, 1), 10))
			sendOptionsDialogue("What would you like to do?",
					"Start an instance",
					"Join an instance",
					"Enter my instance");
			else
				sendOptionsDialogue("What would you like to do?",
						"Start an instance",
						"Join an instance",
						"Enter my instance",
						"Leave my instance");
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			switch(componentId) {
			case OPTION_1:
				if (isInInstance) {
					end();
					player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
						@Override
						public void run(Player player) {
							final String value = getString();
							player.getPackets().sendGlobalString(356, Utils.formatPlayerNameForDisplay(value));
							Instance instance = null, hardInstance = null;
							for (int i = 0; i < World.getInstances().size(); i++) {
								if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(value)) {
									if (World.getInstances().get(i).isHardMode()) {
										hardInstance = World.getInstances().get(i);
									} else {
										instance = World.getInstances().get(i);
									}
								}
							}
							if (instance == null && hardInstance == null) {
								player.getDialogueManager().startDialogue("SimpleMessage", Utils.formatPlayerNameForDisplay(value) + " does not currently have an active instance.");
								return;
							} else if (instance != null && hardInstance != null) {
								player.getDialogueManager().startDialogue("InstanceJoiningD", instance, hardInstance);
								return;
							}
							if (instance != null) {
								instance.enterInstance(player);
							} else {
								hardInstance.enterInstance(player);
							}
						}
					});
					return;
				}
				stage = 9;
				sendOptionsDialogue("Select the respawn speed",
						"Slow",
						"Medium",
						"Fast",
						"Very fast");
				break;
			case OPTION_2:
				end();
				player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
					@Override
					public void run(Player player) {
						final String value = getString();
						player.getPackets().sendGlobalString(356, Utils.formatPlayerNameForDisplay(value));
						Instance instance = null, hardInstance = null;
						for (int i = 0; i < World.getInstances().size(); i++) {
							if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(value)) {
								if (World.getInstances().get(i).isHardMode()) {
									hardInstance = World.getInstances().get(i);
								} else {
									instance = World.getInstances().get(i);
								}
							}
						}
						if (instance == null && hardInstance == null) {
							player.getDialogueManager().startDialogue("SimpleMessage", Utils.formatPlayerNameForDisplay(value) + " does not currently have an active instance.");
							return;
						} else if (instance != null && hardInstance != null) {
							player.getDialogueManager().startDialogue("InstanceJoiningD", instance, hardInstance);
							return;
						}
						if (instance != null) {
							instance.enterInstance(player);
						} else {
							hardInstance.enterInstance(player);
						}
					}
				});
				break;
			case OPTION_3:
				Instance instance = null;
				for (int i = 0; i < World.getInstances().size(); i++) {
					if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName()))
						instance = World.getInstances().get(i);
				}
				if (instance != null)
					instance.enterInstance(player);
				end();
				break;
			case OPTION_4:
				end();
				player.setNextWorldTile(new WorldTile(3113, 6897, 1));
				player.sendMessage("You leave your instance and return to the lobby.");
				player.setForceMultiArea(false);
				if (player.getCurrentInstance() != null)
					player.getCurrentInstance().removePlayer(player);
				break;
			}
			break;
		case 10:
			switch(componentId) {
			case OPTION_1:
				speed = 120;
				break;
			case OPTION_2:
				speed = 60;
				break;
			case OPTION_3:
				speed = 30;
				break;
			case OPTION_4:
				speed = 5;
				break;
			}
			sendOptionsDialogue("Select the players limit",
					"No limit",
					"3",
					"5",
					"10",
					"Custom amount");
			break;
		case 11:
			switch(componentId) {
			case OPTION_1:
				limit = 0;
				break;
			case OPTION_2:
				limit = 3;
				break;
			case OPTION_3:
				limit = 5;
				break;
			case OPTION_4:
				limit = 10;
				break;
			case OPTION_5:
				player.sendInputInteger("How many players will you limit the instance to?", new InputIntegerEvent() {
					@Override
					public void run(Player player) {
						player.getDialogueManager().startDialogue("VindictaInstanceD", bossId, speed, getInteger());
					}
				});
				end();
				break;
			}
			if (componentId != OPTION_5) {
				sendOptionsDialogue("Protect instance with a password?",
						"Yes, add a password.",
						"No, don't add a password.");
			}
			break;
		case 12:
			if (componentId == OPTION_1) {
				player.sendInputInteger("Select a password in digits.", new InputIntegerEvent() {
					@Override
					public void run(Player player) {
						player.getDialogueManager().startDialogue("VindictaInstanceD", bossId, speed, limit, getInteger());
					}
				});
				end();
				return;
			}
			password = -1;
			if (componentId != OPTION_1) {
				sendOptionsDialogue("Select the duration of the instance.",
						"Half an hour.",
						"One hour.",
						"An hour and a half.",
						"Two hours.");
			}
			break;
		case 13:
			switch (componentId) {
			case OPTION_1:
				duration = 30;
				break;
			case OPTION_2:
				duration = 60;
				break;
			case OPTION_3:
				duration = 90;
				break;
			case OPTION_4:
				duration = 120;
				break;
			}
			instance = null;
			for (int i = 0; i < World.getInstances().size(); i++) {
				if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())){
					instance = World.getInstances().get(i);
				}
			}
			if (instance != null) {
				player.sendMessage("You already have an active instance to this boss.");
				end();
				return;
			}

			sendOptionsDialogue("The instance will cost " + 25000 * duration + " coins to build.<br>Are you sure you wish to create this instance?",
					"Yes, create the instance.",
					"No, I've changed my mind.");
			break;
		case 14:
			if (componentId == OPTION_1) {
				if (player.getMoneyPouch().removeAmount(25000 * duration)) {
					instance = new VindictaInstance(player, duration, speed, limit, password, bossId, false);
					instance.constructInstance();
				} else {
					player.sendMessage("You don't have enough coins to build this instance.");
				}
			}
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

	
	
}
