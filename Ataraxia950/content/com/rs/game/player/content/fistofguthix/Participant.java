package com.rs.game.player.content.fistofguthix;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.FOGController;
import com.rs.utils.Utils;

/**
 * The fist of guthix participant.
 * Animations: 1584 //Earning Points - 1585 //Taking Damage - 1586 //Winning Circle
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 30, 2017 at 8:21:22 PM.
 */
public class Participant {
	
	/**
	 * Whether the {@link Participant} has switched side to hunter/prey.
	 */
	private boolean switchedSides = false;

	/**
	 * The participant that has the hunterRole role.
	 */
	private Player hunterRole;
	
	/**
	 * Gets the hunterRole {@link Participant}.
	 * @return
	 */
	Player hunter() {
		return this.hunterRole;
	}
	
	/**
	 * The participant that has the preyRole role, being hunted.
	 */
	private Player preyRole;
	
	/**
	 * Gets the chased/hunter {@link Participant}.
	 * @return the preyRole
	 */
	public Player chased() {
		return this.preyRole;
	}
	
	/**
	 * The number of game ticks.
	 */
	private long gameTicks;
	
	/**
	 * Gets the number of game ticks.
	 * @return the gameTicks
	 */
	private long gameTicks() {
		return gameTicks;
	}
	
	/**
	 * The last base entered (0).
	 */
	private long lastEnteredBase = 0;
	
	/**
	 * Gets the last base [small house] entered.
	 * @return the lastEnteredBase
	 */
	long lastEnteredBase() {
		return lastEnteredBase;
	}

	/**
	 * Sets the last base entered.
	 * @param ticks The ticks.
	 */
	void lastEnteredBase(long ticks) {
		this.lastEnteredBase = ticks;
	}

	/**
	 * The constructor of the {@link Participant}.
	 * @param hunterRole The role of the hunter (chase).
	 * @param preyRole The role of the prey (run).
	 */
	Participant(Player hunterRole, Player preyRole) {
		this.hunterRole = hunterRole;
		this.preyRole = preyRole;
		this.gameTicks = 0;
	}
	
	/**
	 * Switches the side of the player to either the hunter or prey role corresponding to the previous role.
	 */
	private void switchSides() {
		Player participant = this.hunterRole;
		this.hunterRole = this.preyRole;
		this.preyRole = participant;
		if (this.preyRole.getControlerManager().getControler() != null) {
			((FOGController) this.preyRole.getControlerManager().getControler()).exit();
			this.preyRole.getControlerManager().getControler().start();
		}
		if (this.hunterRole.getControlerManager().getControler() != null) {
			((FOGController) this.hunterRole.getControlerManager().getControler()).exit();
			this.hunterRole.getControlerManager().getControler().start();
		}
		this.gameTicks = 0;
	}
	
	/**
	 * Increases the {@link FistOfGuthix} charges.
	 */
	private void increaseCharges() {
		if (chased().getEquipment().getWeaponId() == 12845 && !FOGManager.get().getFOGInstance().isInsideBarrier(preyRole))
			chased().fogCharge(chased().fogCharge() + FOGManager.get().getFOGInstance().determinePoints(chased()));
	}

	/**
	 * Processes the fist of guthix activity actions (barrier damage, etc).
	 */
	void process() {
		gameTicks++;
		if (gameTicks() % 4 == 0) {
			increaseCharges();
			if (FOGManager.get().getFOGInstance().isInsideBarrier(preyRole))
				preyRole.applyHit(new Hit(preyRole, preyRole.getSkills().getLevel(Skills.HITPOINTS), HitLook.REGULAR_DAMAGE, 0));
			preyRole.setNextGraphics(new Graphics(1584));
		}

		if (!(hunter().getControlerManager().getControler() instanceof FOGController))
			hunter().getControlerManager().startControler("FOGController");

		if (!(hunter().getControlerManager().getControler() instanceof FOGController))
			chased().getControlerManager().startControler("FOGController");

		if (chased().getInterfaceManager().containsInterface(731))
		    hunter().getInterfaceManager().removeMinigameHudInterface();

		if (hunter().getInterfaceManager().containsInterface(731))
	         hunter().getInterfaceManager().removeMinigameHudInterface();

		if (!chased().getInterfaceManager().containsInterface(730))
			hunter().getInterfaceManager().sendMinigameHudInterface(730);

		if (!hunter().getInterfaceManager().containsInterface(730))
			hunter().getInterfaceManager().sendMinigameHudInterface(730);

		chased().getPackets().sendIComponentText(730, 1, "Charge: " + chased().fogCharge());
		hunter().getPackets().sendIComponentText(730, 1, "Charge: " + hunter().fogCharge());

		hunter().getPackets().sendIComponentText(730, 2, "Hunting:");
		hunter().getPackets().sendIComponentText(730, 3, chased().getDisplayName());
		hunter().getPackets().sendIComponentText(730, 7, "");

		chased().getPackets().sendIComponentText(730, 2, "Hunted by:");
		chased().getPackets().sendIComponentText(730, 3, hunter().getDisplayName());

		if (chased().getInventory().containsItem(12845, 1) || chased().getEquipment().getWeaponId() == 12845)
			chased().getPackets().sendIComponentText(730, 7, "");
		else
			chased().getPackets().sendIComponentText(730, 7, "<col=ff0000>Please pick up a stone!");

		hunter().getPackets().sendIComponentText(730, 7, "");

		hunter().getHintIconsManager().addHintIcon(chased(), 0, -1, false);
		chased().getHintIconsManager().addHintIcon(hunter(), 0, -1, false);

		chased().getPackets().sendConfigByFile(5774, ((int) gameTicks()));
		hunter().getPackets().sendConfigByFile(5774, ((int) gameTicks()));

		if (gameTicks == 1000) {
			switchSides();
			chased().getPackets().sendGameMessage("You ran out of time" + (switchedSides ? ", <col=0a8451>the second round of the game is about to begin.</col>" : ", ending game."));
			hunter().getPackets().sendGameMessage("You ran out of time" + (switchedSides ? ", <col=0a8451>the second round of the game is about to begin.</col>" : ", ending game."));
		}
	}
	
	/**
	 * Ends the fist of guthix game with a winner and a loser.
	 */
	private void endGame() {
		hunter().setNextWorldTile(new WorldTile(new WorldTile(1698, 5600, 0), 3));
		chased().setNextWorldTile(new WorldTile(new WorldTile(1698, 5600, 0), 3));
		if (chased().getControlerManager().getControler() != null)
			((FOGController) chased().getControlerManager().getControler()).exit();
		if (hunter().getControlerManager().getControler() != null)
			((FOGController) hunter().getControlerManager().getControler()).exit();
		hunter().getControlerManager().removeControlerWithoutCheck();
		chased().getControlerManager().removeControlerWithoutCheck();
		Player winner = (chased().fogCharge() > hunter().fogCharge() ? preyRole : hunterRole);
		Player loser = (hunter() == winner ? preyRole : hunterRole);
		int tokens = (int) Math.floor(Math.pow(Math.log(500), 2.5) * 4);
		winner.getPackets().sendGameMessage("<col=0a8451>Congratulations, you won!</col> You had " + winner.fogCharge() + " charges and your opponent had " + loser.fogCharge() + ".");
		winner.getPackets().sendGameMessage("You have gained 10 rating and " + tokens + " tokens.");
		winner.getInventory().addItem(12852, tokens);
		winner.fogRating(winner.fogRating() + 10);
		winner.setCanPvp(false);
		loser.setCanPvp(false);
		winner.setNextAnimation(new Animation(8996));
		winner.getHintIconsManager().removeAll();
		winner.getHintIconsManager().removeUnsavedHintIcon();
		loser.getPackets().sendGameMessage("<col=AB484A>You lost.</col> You had " + loser.fogCharge() + " and your opponent had " + winner.fogCharge() + ".");
		loser.getPackets()
				.sendGameMessage("You have lost " + (loser.fogRating() >= 4 ? 4 : loser.fogRating()) + " ratings and have gained 1 Fist of Guthix token for good effort.");
		loser.fogRating(loser.fogRating() - (loser.fogRating() >= 4 ? 4 : loser.fogRating()));
		loser.getInventory().addItem(12852, 1);
		loser.getHintIconsManager().removeAll();
		loser.fogCharge(0);
		winner.fogCharge(0);
		winner.getControlerManager().removeControlerWithoutCheck();
		loser.getControlerManager().removeControlerWithoutCheck();
		loser.getHintIconsManager().removeUnsavedHintIcon();
		FOGManager.get().getFOGInstance().gameMembers().remove(this);
	}
	
	/**
	 * Handles the forfeiting of the {@link FistOfGuthix} minigame.
	 * @param player The player to forfeit.
	 */
	public void forfeit(Player player) {
		Player winner = (player == hunterRole ? preyRole : hunterRole);
		Player loser = (preyRole == winner ? hunterRole : preyRole);
		winner.setNextWorldTile(new WorldTile(new WorldTile(1698, 5600, 0), 3));
		if (winner != null) {
			((FOGController) winner.getControlerManager().getControler()).exit();
			winner.getPackets().sendGameMessage("Your parter has left the Fist of Guthix, therefore the victory is yours by default.");
			int tokens = (int) Math.floor(Math.pow(Math.log(500), 2.5) * 4);
			player.setNextGraphics(new Graphics(1586));
			player.setNextAnimation(new Animation(8996));
			winner.getPackets().sendGameMessage("<col=052F11>Congratulations, you won!</col> You had " + winner.fogCharge() + " charges and your opponent had " + loser.fogCharge() + ".");
			winner.getPackets().sendGameMessage("You have gained 10 rating and " + tokens + " tokens.");
			winner.getInventory().addItem(12852, tokens);
			winner.fogRating(winner.fogRating() + 10);
			winner.setCanPvp(false);
			winner.getHintIconsManager().removeAll();
			winner.getHintIconsManager().removeUnsavedHintIcon();
			winner.setNextAnimation(new Animation(8996));
			winner.getControlerManager().removeControlerWithoutCheck();
			winner.fogCharge(0);
		}
		if (loser != null) {
			loser.setCanPvp(false);
			loser.getPackets().sendGameMessage("<col=ff0000>You quit.</col> You had " + loser.fogCharge() + " and your opponent had " + winner.fogCharge() + ".");
			loser.getPackets().sendGameMessage("You have lost " + (loser.fogRating() >= 4 ? 4 : loser.fogRating()) + " ratings and have gained 1 Fist of Guthix token for good effort.");
			loser.fogRating(loser.fogRating() - (loser.fogRating() >= 4 ? 4 : loser.fogRating()));
			loser.getInventory().addItem(12852, 1);
			loser.getHintIconsManager().removeAll();
			loser.getHintIconsManager().removeUnsavedHintIcon();
			FOGManager.get().getFOGInstance().gameMembers().remove(this);
			loser.getControlerManager().removeControlerWithoutCheck();
			loser.fogCharge(0);
		}
	}

	/**
	 * Processes the death of the {@link Participant}.
	 * @param player The player that dies.
	 */
	void processDeath(Player player) {
		if (player == hunterRole) {
			int location = Utils.random(FistOfGuthix.HUNTER_LOCS.length);
			hunterRole.setNextWorldTile(FistOfGuthix.HUNTER_LOCS[location]);
		} else {
			if (!switchedSides) {
				switchedSides = true;
				int location = Utils.random(FistOfGuthix.HUNTER_LOCS.length);
				hunter().setNextWorldTile(FistOfGuthix.HUNTER_LOCS[location]);
				chased().setNextWorldTile(FistOfGuthix.PREY_LOCS[location]);
				switchSides();
			} else {
				endGame();
			}
		}
	}
}