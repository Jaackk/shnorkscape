package com.rs.game.activities.rots.dialogues;

import com.rs.game.World;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.16
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class RiseOfTheSixEnterD extends Dialogue {

	private transient RiseOfTheSix instance;
	
	@Override
	public void start() {
		if (player.getFamiliar() != null) {
			player.sendMessage("You need to dismiss your familiar before you can do that.");
			end();
			return;
		}
		sendOptionsDialogue(Colors.LPURPLE + "Barrows: Rise of the Six", "Start an instance", "Join an existing instance", "Nevermind");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				sendOptionsDialogue("Are you sure you're ready to start?", "Yes", "No");
				break;
			case OPTION_2:
				player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
					@Override
					public void run(Player player) {
						final String value = getString();
						Player instancePlayer = World.getPlayerByDisplayName(value);
						if (instancePlayer == null) {
							player.getDialogueManager().startDialogue("SimpleMessage", "Could not find a player by the name of " + Utils.formatPlayerNameForDisplay(value) + ".");
							return;
						}
						instance = instancePlayer.getControlerManager().getControler() instanceof RiseOfTheSixController ? ((RiseOfTheSixController) instancePlayer.getControlerManager().getControler()).getInstance() : null;
						if (instance == null) {
							player.getDialogueManager().startDialogue("SimpleMessage", Utils.formatPlayerNameForDisplay(value) + " does not currently have an active instance.");
							return;
						}
						if (instance.initiatedFight()) {
							player.sendMessage("The fight has already began.");
							return;
						}
						if (instance.getPlayers().size() == 4) {
							if (instance.getPlayers().contains(player))
								instance.getPlayers().remove(player);
							else {
								player.getDialogueManager().startDialogue("SimpleMessage", "The instance you're attempting to join is currently full.");
								return;
							}
						}
						if (instance.getPassword() != null) {
							player.sendInputName("Enter the password.", new InputNameEvent() {
								@Override
								public void run(Player player) {
									final String value = getString();
									if (instance.initiatedFight()) {
										player.sendMessage("The fight has already began.");
										return;
									}
									if (instance.getPlayers().size() == 4) {
										if (instance.getPlayers().contains(player))
											instance.getPlayers().remove(player);
										else {
											player.getDialogueManager().startDialogue("SimpleMessage", "The instance you're attempting to join is currently full.");
											return;
										}
									}
									if (instance.getPassword() == null || instance.getPassword().equalsIgnoreCase(value)) {
										player.getTemporaryAttributtes().put("rotsinstance", instance);
										player.getControlerManager().startControler("RiseOfTheSixController");
									} else {
										player.sendMessage("Incorrect password. Access denied.");
									}
								}
							});
						} else {
							player.getTemporaryAttributtes().put("rotsinstance", instance);
							player.getControlerManager().startControler("RiseOfTheSixController");
						}
					}
				});
				end();
				return;
			case OPTION_3:
				end();
				return;
			}
			break;
		case 0:
			if (componentId == OPTION_1)
				sendOptionsDialogue("Would you like to set a password?", "Yes", "No");
			else
				end();
			break;
		case 1:
			if (componentId == OPTION_2)
				enter();
			else
				player.sendInputName("Enter the password you wish to add:", new InputNameEvent() {
					@Override
					public void run(Player player) {
						final String value = getString();
						if (player.getInventory().containsItem(30004, 1)) {
							player.getInventory().deleteItem(30004, 1);
							player.sendMessage("A barrows totem is consumed as you begin the instance.");
							player.getTemporaryAttributtes().put("rotspassword", value);
							player.getControlerManager().startControler("RiseOfTheSixController");
							end();
						} else
							sendItemDialogue(30004, 1, "You need a barrows totem to start an instance!");
					}
				});
			break;
		case 2:
			end();
			break;
		}
		stage++;
	}
	
	private final void enter() {
		if (player.getInventory().containsItem(30004, 1)) {
			end();
			player.getInventory().deleteItem(30004, 1);
			player.sendMessage("A barrows totem is consumed as you begin the instance.");
			player.getControlerManager().startControler("RiseOfTheSixController");
		} else
			sendItemDialogue(30004, 1, "You need a barrows totem to start an instance!");
	}

	@Override
	public void finish() {}

}
