package com.rs.game.activites.soulwars;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

/**
 * @author Savions Sw
 */
public class GameController extends Controller {

	private int activity = 100;

	private int team;

	@Override
	public void start() {
		team = (int) getArguments()[0];
		setAreaIndex(-1);
		sendInterfaces();
		player.getTemporaryAttributtes().put("sw_safe_zone", Boolean.TRUE);
		player.setNextWorldTile(World.soulWars.calculateRandomLocation(Teams.values()[team], PlayerType.IN_GAME));
		player.setCanPvp(true);
		player.getMusicsManager().playMusic(598);
		player.getDialogueManager().startDialogue("SimpleNPCMessage", team == 0 ? 8528 : 8526, "The time is now, Crush their souls!");
		// World.sendWorldMessage("<img=6><shad=000000><col="+
		// World.colors[Utils.random(World.colors.length)]+">"
		// + "Server: A new game of Soul Wars has just started!", false); //TODO
		player.getInterfaceManager().sendOverlay(836, false);
		sendInterfaces();
	}

	@Override
	public boolean processPlayerOption1(Entity target) {
		if (target instanceof Player) {
			if (target.getTemporaryAttributtes().get("sw_safe_zone") != null) {
				Boolean bool = (Boolean) target.getTemporaryAttributtes().get("sw_safe_zone");
				if (bool.equals(Boolean.TRUE))
					return false;
			}
			if (canHit(target))
				return true;
			player.sendMessage("You can't attack your team.");
			return false;
		}
		return canHit(target);
	}

	@Override
	public boolean handleItemOnObject(WorldObject object, Item item) {
		if (object.getId() == 42010 && item.getId() == SoulWarsManager.SOUL_FRAGMENT) {
			return !World.soulWars.depositSoulFragments(player);
		}
		return true;
	}

	@Override
	public boolean canEquip(int slotId, int itemId) {
		if (slotId == Equipment.SLOT_CAPE) {
			player.sendMessage("You can't remove your team's colours.");
			return false;
		}
		return true;
	}

	@Override
	public void sendInterfaces() {
	    
		player.getInterfaceManager().sendOverlay(836, false);
		GameTask task = (GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME);
		final int index = (int) getArguments()[0];
		final Avatar BLUE = task.getAvatars()[0];
		final Avatar RED = task.getAvatars()[1];
		player.getPackets().sendHideIComponent(836, 47, index == -1 || index != 1);
		player.getPackets().sendHideIComponent(836, 49, index == -1 || index != 2);
		player.getPackets().sendHideIComponent(836, 50, index == -1 || index != 0);
		player.getPackets().sendGlobalConfig(641, task.getAvatarSlayerLevel(Teams.BLUE));
		player.getPackets().sendGlobalConfig(642, task.getAvatarSlayerLevel(Teams.RED));
		player.getPackets().sendGlobalConfig(643, task.getAvatarDies(Teams.BLUE));
		player.getPackets().sendGlobalConfig(644, task.getAvatarDies(Teams.RED));
		player.getPackets().sendGlobalConfig(636, 20 - (SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() - 3));
		player.getPackets().sendGlobalConfig(640, BLUE == null || BLUE.hasFinished() || BLUE.isDead() ? -1 : (BLUE.getHitpoints() / 100));
		player.getPackets().sendGlobalConfig(639, RED == null || RED.hasFinished() || RED.isDead() ? -1 : (RED.getHitpoints() / 100));
		for (int objectIndex = 0; objectIndex < 3; objectIndex++) {
			final int config = objectIndex == 0 ? 649 : objectIndex == 1 ? 645 : 647;
			final int value = task.getTeamAreas()[objectIndex] == null ? 15 : (task.getTeamAreas()[objectIndex].equals(Teams.BLUE) ? 15 - task.getTeamAreaValues()[objectIndex] : 15 + task.getTeamAreaValues()[objectIndex]);
			player.getPackets().sendGlobalConfig(config, value);
		}
		player.getPackets().sendConfig(1380, activity * 10);
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() < 4)
					super.stop();
				if (loop == 0)
					player.setNextAnimation(new Animation(836));
				else if (loop == 1)
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				else if (loop == 3) {
					final Player killer = player.getMostDamageReceivedSourcePlayer();
					final int amount = player.getInventory().getAmountOf(SoulWarsManager.SOUL_FRAGMENT);
					final int bones = player.getInventory().getAmountOf(SoulWarsManager.BONES);
					if (killer != null)
						World.updateGroundItem(new Item(3187), player.getLastWorldTile(), killer, 60, 0, false);
					if (amount > 0) {
						player.getInventory().deleteItem(SoulWarsManager.SOUL_FRAGMENT, amount);
						World.updateGroundItem(new Item(SoulWarsManager.SOUL_FRAGMENT, amount), new WorldTile(player), killer == null ? player : killer, 60, 0, false);
					}
					if (bones > 0) {
						player.getInventory().deleteItem(SoulWarsManager.BONES, bones);
						for (int index = 0; index < bones; index++)
							World.updateGroundItem(new Item(SoulWarsManager.BONES, 1), new WorldTile(player), killer == null ? player : killer, 60, 0, false);
					}
					player.getTemporaryAttributtes().put("sw_safe_zone", Boolean.TRUE);
					player.reset();
					player.setNextWorldTile(((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).getRespawnLocation(player, team));
					increaseAvtivity(40);
					player.resetReceivedDamage();
					player.setNextAnimation(new Animation(-1));
					player.getAppearence().transformIntoNPC(SoulWarsManager.GHOST);
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
				} else if (loop == 20) {
					player.setNextAnimation(null); // TODO
					player.getAppearence().transformIntoNPC(-1);
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	private void increaseAvtivity(int i) {
		activity = activity + i > 100 ? 100 : activity + i;
		player.getPackets().sendConfig(1380, activity * 10);
	}

	private void handleBandage(Item item) {
		World.soulWars.useBandage(player);
	}

	public void setAreaIndex(int index) {
		getArguments()[0] = index;
	}

	@Override
	public boolean canMove(int dir) {
		final WorldTile location = player.transform(Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], 0);
		player.getTemporaryAttributtes().put("soul_wars_walked", location);
		return ((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).containsBarricade(location);
	}

	public void decreaseActivity() {
		if (player.getActionManager().getAction() != null) {
			increaseAvtivity(5);
			return;
		}
		if (player.getRights() < 2 && --activity == 0) {
			World.soulWars.resetPlayer(player, PlayerType.IN_GAME, false);
			player.getTemporaryAttributtes().put("soulwars_kicked", true);
			player.getDialogueManager().startDialogue("SimpleNPCMessage", team == 0 ? 8528 : 8526, "You were removed from the game due to your activity.");
		}
	}

	@Override
	public void forceClose() {
		player.getInterfaceManager().closeOverlay(false);
		((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).getPlayers().remove(player);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.sendMessage("You can't just leave like that!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.sendMessage("You can't just leave like that!");
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		forceClose();
		removeControler();
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 590 & componentId == 8) {
			player.sendMessage("This is a battleground, not a circus.");
			return false;
		}
		if (interfaceId == 387 && componentId == 9) {
			player.sendMessage("You can't remove your team's colours.");
			return false;
		}
		if (interfaceId == 667 && componentId == 9) {
			player.sendMessage("You can't remove your team's colours.");
			return false;
		}
		if (interfaceId == Inventory.INVENTORY_INTERFACE || interfaceId == 670) {
			Item item = player.getInventory().getItem(slotId);
			if (item == null)
				return false;
			if (item.getId() == 4053) {
				World.soulWars.placeHeldBarricade(player);
				return false;
			} else if (item.getId() == 3187) {
				World.soulWars.buryBones(player);
				increaseAvtivity(10);
				return false;
			} else if (item.getId() == SoulWarsManager.TEAM_CAPE_INDEX || item.getId() == SoulWarsManager.TEAM_CAPE_INDEX + 1) {
				player.sendMessage("You can't remove your team's colours.");
				return false;
			} else if (item != null && item.getId() == SoulWarsManager.BANDAGE_ID && interfaceId == Inventory.INVENTORY_INTERFACE) {
				handleBandage(item);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canHit(Entity target) {
		if (target instanceof NPC) {
			int requiredSlayerLevel = 0;
			switch (((NPC) target).getId()) {
			case SoulWarsManager.PYREFRIEND:
				requiredSlayerLevel = 30;
				break;
			case SoulWarsManager.JELLY:
				requiredSlayerLevel = 52;
				break;
			case SoulWarsManager.AVATAR_INDEX:
			case SoulWarsManager.AVATAR_INDEX + 1:
				if (Teams.values()[((NPC) target).getId() - SoulWarsManager.AVATAR_INDEX].equals(Teams.values()[player.getEquipment().getCapeId() - SoulWarsManager.TEAM_CAPE_INDEX])) {
					player.sendMessage("You cannot attack the avatar representing your own team!");
					return false;
				}
				requiredSlayerLevel = ((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).getAvatarSlayerLevel(Teams.values()[((NPC) target).getId() - SoulWarsManager.AVATAR_INDEX]);
			}
			if (player.getSkills().getLevel(Skills.SLAYER) < requiredSlayerLevel) {
				player.sendMessage("Your slayer level is not high enough to fight this monster.");
				return false;
			}
			return true;
		}
		return ((Player) target).getEquipment().getCapeId() != player.getEquipment().getCapeId();
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity victim) {
		if (player.getCombatDefinitions().getSpellBook() != 0 && player.getCombatDefinitions().getSpellId(!mainHand) > 0) {
			player.sendMessage("You can only use modern spells during this war!");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch (object.getId()) {
		case 42015:
		case 42018:
		case 42019:
		case 42020:
			World.soulWars.passBarrier(PlayerType.IN_GAME, player, object);
			return false;
		case 42021:
		case 42022:
			forceClose();
			removeControler();
			World.soulWars.resetPlayer(player, PlayerType.IN_GAME, false);
			player.getDialogueManager().startDialogue("SimpleNPCMessage", team == 0 ? 8528 : 8526, "You have left the game, therefore you will not be rewarded with any zeals.");
			return false;
		case 42023:
		case 42024:
			if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You can't take while being in combat!");
				return false;
			}
			player.getInventory().addItem(SoulWarsManager.BANDAGE_ID, 1);
			return false;
		case 42025:
		case 42026:
			if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You can't take while being in combat!");
				return false;
			}
			if (player.getInventory().getAmountOf(4053) > 0) {
				player.getPackets().sendGameMessage("You can only have one barricade in your inventory!");
				return false;
			}
			player.getInventory().addItem(4053, 1);
			return false;
		case 42027:
		case 42028:
			if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You can't take while being in combat!");
				return false;
			}
			player.getInventory().addItem(14644, 1);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(WorldObject object) {
		switch (object.getId()) {
		case 42023:
		case 42024:
		case 42025:
		case 42026:
		case 42027:
		case 42028:
			if (player.getActionManager().getAction() != null) {
				player.getPackets().sendGameMessage("You can't take while being in combat!");
				return false;
			}
			if (!player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("You don't have any space left for supplies!");
				return false;
			}
			player.sendInputInteger("How many would you like?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					if (SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() < 4)
						return;
					final WorldTile walked = (WorldTile) player.getTemporaryAttributtes().remove("soul_wars_walked");
					if (walked != null && !player.matches(walked))
						return;
					player.getInventory().addItem(object.getId() == 42023 || object.getId() == 42024 ? SoulWarsManager.BANDAGE_ID : object.getId() == 42025 || object.getId() == 42026 ? 4053 : 14644, getInteger());
				}
			});
			return false;
		}
		return true;
	}

	@Override
	public boolean processCommand(String command, boolean b, boolean c) {
		if (player.isOwner() || Settings.DEBUG) {
			return true;
		}
		if (command.equalsIgnoreCase("ticket")) {
			return true;
		}
		player.sendMessage(Colors.RED + "Commands cannot be used during a game of Soul Wars.");
		return false;
	}

	@Override
	public boolean login() {
		player.getControlerManager().startControler("AreaController");
		return false;
	}

	@Override
	public boolean logout() {
		World.soulWars.resetPlayer(player, PlayerType.IN_GAME, true);
		return false;
	}
}
