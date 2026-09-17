package com.rs.game.activites.duel;

import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Colors;

public class DuelControler extends Controller {

	public static void challenge(Player player) {
		Boolean friendly = (Boolean) player.getTemporaryAttributtes().remove("WillDuelFriendly");
		if (friendly == null)
			return;
		Player target = (Player) player.getTemporaryAttributtes().remove("DuelTarget");
		if (target == null || target.hasFinished() || !target.withinDistance(player, 14)
				|| !(target.getControlerManager().getControler() instanceof DuelControler)) {
			player.getPackets().sendGameMessage("Unable to find " + (target == null ? "your target" : target.getDisplayName()));
			if(target != null)
				target.getPackets().sendGameMessage(Colors.SALMON+"You need to teleport back to the duel arena to do this!");
			return;
		}
		if (player.getFamiliar() != null) {
			player.sendMessage("Please dismiss your familiar before starting a duel.");
			return;
		}
		if (!player.canTrade(target))
			return;
		player.getTemporaryAttributtes().put("DuelChallenged", target);
		player.getTemporaryAttributtes().put("DuelFriendly", friendly);
		player.getPackets().sendGameMessage("Sending " + target.getDisplayName() + " a request...");
		target.getPackets().sendDuelChallengeRequestMessage(player, friendly);
		player.getInterfaceManager().closeScreenInterface();
	}

	/**
	 * Checks if the player is at the Ascensiopns Dungeon.
	 *
	 * @param player
	 *            The player to check for.
	 * @return if is in Monastery of Ascension.
	 */
	public static boolean isAtDuelArena(Player player) {
		int destX = player.getX(), destY = player.getY();
		return /* South West */(destX >= 3330 && destY >= 3224 &&
		/* North East */destX <= 3351 && destY <= 3241);
	}

	@Override
	public boolean canPlayerOption1(final Player target) {
		player.stopAll(false);
		player.faceEntity(target);
		if (player.getInterfaceManager().containsScreenInter() || player.isLocked()) {
			player.getPackets().sendGameMessage("You're too busy.");
			return false;
		}
		if (!player.canTrade(target))
			return false;
		if (target.getTemporaryAttributtes().get("DuelChallenged") == player) {
			player.getControlerManager().removeControlerWithoutCheck();
			target.getControlerManager().removeControlerWithoutCheck();
			target.getTemporaryAttributtes().remove("DuelChallenged");
			player.setLastDuelRules(new DuelRules(player, target));
			target.setLastDuelRules(new DuelRules(target, player));
			player.getControlerManager().startControler("DuelArena", target,
					target.getTemporaryAttributtes().get("DuelFriendly"));
			target.getControlerManager().startControler("DuelArena", player,
					target.getTemporaryAttributtes().remove("DuelFriendly"));
			return false;
		}
		player.getTemporaryAttributtes().put("DuelTarget", target);
		player.getInterfaceManager().sendInterface(640);
		player.getTemporaryAttributtes().put("WillDuelFriendly", true);
		player.getVarBitManager().sendVar(283, 67108864);
		return false;
	}

	@Override
	public void forceClose() {
		remove();
	}

	@Override
	public boolean login() {
		start();
		return false;
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		removeControler();
		remove();
	}

	@Override
	public void moved() {
		if (!isAtDuelArena(player)) {
			removeControler();
			remove();
		}
	}

	public void remove() {
		player.getInterfaceManager().closeOverlay(false);
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendPlayerOption("null", 1, false);
	}

	@Override
	public void sendInterfaces() {
		if (isAtDuelArena(player))
			player.getInterfaceManager().sendOverlay(638, false);
	}

	@Override
	public void start() {
		sendInterfaces();
		player.getPackets().sendPlayerOption("Challenge", 1, false);
		player.getAppearence().generateAppearenceData();
		moved();
	}
}