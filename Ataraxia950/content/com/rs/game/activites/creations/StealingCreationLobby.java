package com.rs.game.activites.creations;

import com.rs.game.WorldTile;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Richard
 * @author Khaled
 */
public class StealingCreationLobby extends Controller {

	private boolean interfacesReady;

	@Override
	public void forceClose() {
		interfacesReady = false;
		super.forceClose();
		StealingCreation.resetPlayerInterface(player);
		if (!StealingCreation.isTransferringToGame()) {
			StealingCreation.removeFromTeams(player);
		}
	}

	@Override
	public void magicTeleported(int type) {
		player.getControlerManager().forceStop();
	}

	// TODO object click for exit

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage",
				"A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage",
				"A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage",
				"A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public void sendInterfaces() {
		if (!interfacesReady) {
			return;
		}
		player.getInterfaceManager().sendOverlay(StealingCreation.LOBBY_INTERFACE, false);
		StealingCreation.updateInterfaces();
	}

	@Override
	public void start() {
		interfacesReady = false;
		final boolean inRedTeam = getArguments() != null && getArguments().length > 0 && (boolean) getArguments()[0];
		StealingCreation.addToTeam(player, inRedTeam);
		player.setNextWorldTile(StealingCreation.getLobbyTile(inRedTeam));
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;

			@Override
			public void run() {
				if (player == null || player.hasFinished() || player.getControlerManager() == null
						|| player.getControlerManager().getControler() != StealingCreationLobby.this) {
					stop();
					return;
				}
				if (!player.clientHasLoadedMapRegion() && ticks++ < 10) {
					return;
				}
				StealingCreation.resetPlayerInterface(player);
				interfacesReady = true;
				sendInterfaces();
				stop();
			}
		}, 1, 1);
	}

	@Override
	public boolean logout() {
		StealingCreation.removeFromTeams(player);
		return true;
	}
}
