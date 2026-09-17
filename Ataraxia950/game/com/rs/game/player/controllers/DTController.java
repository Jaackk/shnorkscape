package com.rs.game.player.controllers;

import com.rs.game.WorldObject;
import com.rs.game.npc.NPC;
import com.rs.game.player.DominionTower;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;

public class DTController extends Controller {

	private NPC[] bosses;

	private int onArena;

	private int playerHp, bossHp;

	public boolean bossesDead() {
		for (NPC n : bosses)
			if (!n.hasFinished())
				return false;
		return true;
	}

	public int getBossesHPPercentage() {
		int totalHp = 0;
		int totalMaxHp = 0;
		for (NPC n : bosses) {
			totalHp += n.getHitpoints();
			totalMaxHp += n.getMaxHitpoints();
		}
		return totalHp * 100 / totalMaxHp;
	}

	public int getMode() {
		return (Integer) getArguments()[0];
	}

	public int getPlayerHPPercentage() {
		return player.getHitpoints() * 100 / player.getMaxHitpoints();
	}

	@Override
	public void start() {
		// TODO Auto-generated method.
	}

	@Override
	public boolean login() {
		if (player.isDead() || getArguments().length == 0)
			return true;
		player.getDominionTower().selectBoss();
		player.getDominionTower().createArena(getMode());
		return false;
	}

	@Override
	public boolean logout() {
		player.getDominionTower().destroyArena(true, getMode());
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		if (type != Magic.OBJECT_TELEPORT)
			player.getDominionTower().destroyArena(false, getMode());
	}

	@Override
	public void process() {
		if (bosses != null) {
			if (bossesDead()) {
				bosses = null;
				player.getDominionTower().win(getMode());
				return;
			}
			int playerHp = getPlayerHPPercentage();
			int bossHp = getBossesHPPercentage();

			if (bossHp != this.bossHp) {
				this.bossHp = bossHp;
				player.getPackets().sendGlobalConfig(1672, bossHp);
			}
			if (playerHp != this.playerHp) {
				this.playerHp = playerHp;
				player.getPackets().sendGlobalConfig(1673, playerHp);
			}
		}
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 62682 || object.getId() == 62683 || object.getId() == 62690) {
			player.getDominionTower().destroyArena(false, getMode());
			return false;
		} else if (object.getId() == 62684 || object.getId() == 62685) {
			if (bosses == null && onArena == 0) {
				bosses = player.getDominionTower().createBosses();
				onArena = 1;
				if (bosses.length > 1 || player.getDominionTower().getNextBoss().isForceMulti()) {
					player.setForceMultiArea(true);
					for (NPC n : bosses)
						n.setForceMultiArea(true);
				}
				player.getDominionTower().startFight(bosses);
			}
			return false;
		} else if (object.getId() == 62686 || object.getId() == 62687) {
			if (bosses == null && onArena == 1) {
				onArena = 2;
				player.lock(2);
				player.stopAll();
				player.addWalkSteps(player.getX() + 1, player.getY(), 1, false);
				player.setForceMultiArea(false);
				return false;
			}
		} else if (object.getId() == 62691) {
			if (getMode() == DominionTower.ENDURANCE) {
				player.sendMessage("You can't do this on endurance mode.");
				return false;
			}
			if (!player.promptList())
				player.getBank().openBank();
			else
				player.getDialogueManager().startDialogue("BankList", false);
			return false;
		} else if (object.getId() == 62689) {
			if (player.getDominionTower().getProgress() == 0) {
				player.getDominionTower().openModes();
				return false;
			}
			if (getMode() == DominionTower.ENDURANCE)
				player.getDominionTower().openEnduranceMode();
			// else
			// player.getDominionTower().openClimberMode();
			return false;
		} else if (object.getId() == 62677) {
			player.getDominionTower().talkToFace();
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		if (object.getId() == 62691) {
			if (getMode() == DominionTower.ENDURANCE) {
				player.sendMessage("You can't do this on endurance mode.");
				return false;
			}
			player.getGEManager().openCollectionBox();
			return false;
		}
		if (object.getId() == 62688) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You've a Dominion Factor of " + player.getDominionTower().dominionFactor + ".");
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		if (bosses != null) {
			for (NPC n : bosses)
				n.finish();
		}
		bosses = null;
		player.getDominionTower().loss(getMode());
		return false;
	}

	@Override
	public void sendInterfaces() {
		if (bosses != null) {
		    player.getInterfaceManager().closeOverlay(false);
		    player.getInterfaceManager().sendMinigameHudInterface(1159);
			player.getPackets().sendHideIComponent(1159, 14, true);
			player.getPackets().sendIComponentText(1159, 32,
					"Endurance" + ". Floor " + (player.getDominionTower().getProgress() + 1));
			player.getPackets().sendIComponentText(1159, 40, player.getDisplayName());
			player.getPackets().sendIComponentText(1159, 41, player.getDominionTower().getNextBoss().getName());
		}
	}
	
	public static boolean isInsideDominionTower(Player player) {
	    return player.getControlerManager().getControler() instanceof DTController || (player.getX() >= 3717 && player.getY() >= 6402 && player.getX() <= 3770 && player.getY() <= 6452);
	}
}