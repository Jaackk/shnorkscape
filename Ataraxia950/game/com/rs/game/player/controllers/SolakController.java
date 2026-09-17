package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonBoss;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.solak.Solak;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.player.dialogue.impl.OptionSelectionD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;
import java.util.ArrayList;
import java.util.List;
public class SolakController extends BossInstanceController {//then the controller for solak and lost grove area for other npcs
	private transient Solak current;

	private static void handleSolakReward(Player player, int xp, int tokens) {
		int exp = (int) (xp * (player.isNovice() ? 0.70 : 1));
		player.dungKills++;
		player.getDungeoneeringManager().addTokens((int) (tokens * (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.DUNGEONS_MASTER) ? 1.25 : 1)));
		player.getSkills().addXp(Skills.SLAYER, (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.DUNGEONS_MASTER) ? 1.25 : 1) * exp * (Settings.DUNG_MODIFIER));
		player.getInterfaceManager().sendOverlay(988, false);
		player.getPackets().sendIComponentText(988, 0, String.valueOf(player.dungKills));
		player.getPackets().sendIComponentText(988, 1, "Kills:");
	}

	public static void handleDrop(Player player, NPC npc) {
		switch (npc.getName().toLowerCase()) {
			case "vinecrawler":
				handleSolakReward(player, Utils.random(150, 180), Utils.random(0, 0));
				break;
			case "bulbous crawler":
				handleSolakReward(player, Utils.random(200, 230), Utils.random(0, 0));
				break;
			case "solak":
				handleSolakReward(player, Utils.random(15, 30), Utils.random(0, 0));
				break;
			case "moss golem":
				handleSolakReward(player, Utils.random(250, 280), Utils.random(0, 0));
				break;
		}
	}

	@Override
	public void start() {
		sendInterfaces();

	}

	@Override
	public boolean login() {
		player.setNextWorldTile(new WorldTile(1374, 5543, 0));
		removeControler();//this is disabled for testing saves me running bk to solak all the time i can just spawn back when logging in
		return true;
	}




	@Override
	public void sendInterfaces() {
		if (current == null)
			return;


			updateInterface();
			player.getInterfaceManager().sendOverlay(1648, true);



	}


	public void updateInterface() {
		if (current == null)
			return;


		player.getPackets().sendConfig(5776,current.getBoss1MapId());
		player.getPackets().sendConfig(5776, current.getBossMapId());
		player.getPackets().sendIComponentText(1648,27, "Solak Health");
		player.getVarBitManager().sendVarBit(32672, current.getMaxHitpoints() * 10);
		player.getVarBitManager().sendVarBit(28663, current.getHitpoints() * 10);
	}

	@Override
	public boolean canHit(Entity entity) {
		if (entity instanceof Solak && current != entity) {
			current = (Solak) entity;
			player.getInterfaceManager().closeOverlay(true);
			sendInterfaces();
		}
		if (current != null && current instanceof Solak && current.getId() != 25513 && current.getId() != 25529) {
			current = null;
			player.getInterfaceManager().closeOverlay(true);
			return false;
		}


		return super.canHit(entity);
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public void magicTeleported(int teleType) {
		player.inDungeoneering = false;
		player.getInterfaceManager().closeOverlay(true);

		player.getControlerManager().forceStop();
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 11005) {
			player.lock(1);
			if (object.getX() == 1374 && object.getY() == 5552) {
				player.dungKills = 0;
				player.inDungeoneering = false;
				player.addWalkSteps(1374, 5551, 2, false);
				player.getControlerManager().forceStop();
				return false;
			}
		}
		return true;
	}
	public boolean processNpcClick1(final NPC npc) {

		if (npc.getId() == 25508) {

				npc.faceEntity(player);
				player.faceEntity(npc);
				player.getInterfaceManager().sendOverlay(1366, false);
				player.getPackets().sendIComponentText(1366, 26, "PLAYER TEST");
				player.getPackets().sendIComponentText(1366, 1, "SOLAK TEST:");



		}
		return true;
	}
	@Override
	public boolean processObjectClick3(final WorldObject object) {
		if (object.getId() == 5992) {
			player.lock(1);
			if (object.getX() == 1372 && object.getY() == 5582 || object.getX() == 1371 && object.getY() == 5645) {
				List<String> options = new ArrayList<String>();
				options.add("Solak Teleport");
				options.add("VineCrawler Teleport");
				options.add("Bulbous Crawler Teleport");
				options.add("Moss Golem Teleport");
				options.add("Home Teleport");
				player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
					int page = selector.getPage();
					switch (page) {
						case 0:
							selector.close();
							switch (option) {
								case OptionSelectionD.OptionSelector.OPTION_1:
									FadingScreen.fade(player, 0, () -> {
										player.setNextWorldTile(new WorldTile(1373, 5643, 0));



									});
									break;
								case OptionSelectionD.OptionSelector.OPTION_2:
									FadingScreen.fade(player, 0, () -> {
										player.setNextWorldTile(new WorldTile(1301, 5625, 0));



									});

									return;
								case OptionSelectionD.OptionSelector.OPTION_3:
									FadingScreen.fade(player, 0, () -> {
										player.setNextWorldTile(new WorldTile(1397, 5726, 0));



									});
									return;
								case OptionSelectionD.OptionSelector.OPTION_4:
									FadingScreen.fade(player, 0, () -> {
										player.setNextWorldTile(new WorldTile(1416, 5602, 0));



									});
									return;
								case OptionSelectionD.OptionSelector.OPTION_5:
									Magic.vineTeleport(player, new WorldTile(5431, 2338, 0));
									return;
							}
							return;
					}
				});
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
					player.sendMessage("Oh dear, you have died.");
				}
				if (loop == 3) {
					player.setNextWorldTile(new WorldTile(1374, 5542, 0));
					player.setNextAnimation(new Animation(-1));
					player.getControlerManager().forceStop();
					player.getPackets().sendMusicEffect(90);
					player.dungKills = 0;
					player.inDungeoneering = false;
					player.reset();
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity target) {
		if (target instanceof Solak && current != target) {
			current = (Solak) target;
			player.getInterfaceManager().closeOverlay(true);
			sendInterfaces();
		}
		if (current != null && current instanceof Solak && current.getId() != 25513 && current.getId() != 25529) {
			current = null;
			player.getInterfaceManager().closeOverlay(true);
			return false;
		}
		return super.keepCombating(mainHand, target);
	}

	@Override
	public void process() {
		if (current == null)
			return;
		if (current != null && current.getCombat().getTarget() == null) {
			if (current != null && current instanceof Solak && current.getId() != 25513 && current.getId() != 25529) {
				current = null;
				player.getInterfaceManager().closeOverlay(true);
				return;
			}
			if (current.isDead() || current.hasFinished()) {
				current = null;
				player.getInterfaceManager().closeOverlay(true);
			}
		}
	}
}

