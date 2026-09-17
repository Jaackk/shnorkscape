package com.rs.game.player.controllers;

import java.util.List;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.npc.dungeonnering.Blink;
import com.rs.game.npc.dungeonnering.DivineSkinweaver;
import com.rs.game.npc.dungeonnering.DungeonFishSpot;
import com.rs.game.npc.dungeonnering.DungeonSlayerNPC;
import com.rs.game.npc.dungeonnering.FrozenAdventurer;
import com.rs.game.npc.dungeonnering.Gravecreeper;
import com.rs.game.npc.dungeonnering.MastyxTrap;
import com.rs.game.npc.dungeonnering.NightGazerKhighorahk;
import com.rs.game.npc.dungeonnering.RuneboundBehemoth;
import com.rs.game.npc.dungeonnering.Stomp;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.WaterFilling;
import com.rs.game.player.actions.divination.DivinationConvert;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.MiningBase.PickaxeDefinitions;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.HatchetDefinitions;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.WeaponPoison;
import com.rs.game.player.content.dungeoneering.Door;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.dungeoneering.DungeonConstants.KeyDoors;
import com.rs.game.player.content.dungeoneering.DungeonConstants.SkillDoors;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.DungeonResourceShop;
import com.rs.game.player.content.dungeoneering.DungeonUtils;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.game.player.content.dungeoneering.Room;
import com.rs.game.player.content.dungeoneering.RoomReference;
import com.rs.game.player.content.dungeoneering.VisibleRoom;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.PoltergeistRoom;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringFarming;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringFarming.Harvest;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringFishing;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringMining;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringMining.DungeoneeringRocks;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringTraps;
import com.rs.game.player.content.dungeoneering.skills.construction.DungeoneeringConstruction;
import com.rs.game.player.content.dungeoneering.skills.cooking.DungeoneeringCooking;
import com.rs.game.player.content.dungeoneering.skills.crafting.DungeoneeringCrafting;
import com.rs.game.player.content.dungeoneering.skills.firemaking.DungeoneeringFiremaking;
import com.rs.game.player.content.dungeoneering.skills.fletching.DungeoneeringFletching;
import com.rs.game.player.content.dungeoneering.skills.herblore.DungeoneeringHerbCleaning;
import com.rs.game.player.content.dungeoneering.skills.herblore.DungeoneeringHerblore;
import com.rs.game.player.content.dungeoneering.skills.smithing.DungeoneeringSmithing;
import com.rs.game.player.content.dungeoneering.skills.woodcutting.DungeoneeringTreeDefinitions;
import com.rs.game.player.content.dungeoneering.skills.woodcutting.DungeoneeringWoodcutting;
import com.rs.game.player.dialogue.impl.ThreeStatuesStoneD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class DungeonController extends Controller {

	private DungeonManager dungeon;
	private WorldTile gatestone;
	// private int deaths;
	private int voteStage;
	private boolean killedBossWithLessThan10HP;
	private int damageReceived;
	private int meleeDamage, rangeDamage, mageDamage;
	private int healedDamage;
	private boolean showBar;

	@Override
	public void start() {
		dungeon = (DungeonManager) getArguments()[0];
		Magic.resetSwapSpellBook(player);
		setArguments(null); // because arguments save on char and we dont want
		// to save dungmanager
		showDeaths();
		refreshDeaths();
		player.setForceMultiArea(true);
	}

	public void showDeaths() {
		player.getInterfaceManager().sendOverlay(945, true);
	}

	public void showBar() {
		player.getPackets().sendHideIComponent(945, 6, !showBar);
	}

	private void hideBar() {
		showBar(false, null);
	}

	public void showBar(final boolean show, final String name) {
		if (showBar == show) {
			return;
		}
		showBar = show;
		showBar();
		if (show) {
			player.getPackets().sendGlobalString(2381, name);
		}
	}

	public void sendBarPercentage(final int percentage) {
		player.getPackets().sendGlobalConfig(1233, percentage * 2);
	}
	
	public void reset() {
		//deaths = 0;
		voteStage = 0;
		gatestone = null;
		killedBossWithLessThan10HP = false;
		damageReceived = 0;
		meleeDamage = 0;
		rangeDamage = 0;
		mageDamage = 0;
		healedDamage = 0;
		refreshDeaths();
		showDeaths();
		hideBar();
		player.getAppearence().setRenderEmote(-1);
	}
	
	@Override
	public boolean processCommand(final String s, final boolean b, final boolean c) {
		if (player.isOwner() || Settings.DEBUG) {
			return true;
		}
		if(s.contains("ticket") || s.contains("staffmenu")) {
			return true;
		}
		if(s.contains("home") || s.contains("respawn") || s.contains("llet") || s.contains("sang")) {
			player.sendMessage(Colors.RED+"You cannot use teleport-style commands in dungeoneering!");
			return false;
		} else {
			player.sendMessage(Colors.RED+"Commands cannot be used in dungeoneering, PM an admin if you need help!");
		}
		return false;
	}

	@Override
	public boolean canMove(final int dir) {

		final VisibleRoom vr = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		final WorldTile to = new WorldTile(player.getX() + Utils.DIRECTION_DELTA_X[dir], player.getY() + Utils.DIRECTION_DELTA_Y[dir], 0);
		if(vr != null && !vr.canMove(player, to)) {
			return false;
		}

		final Room room = dungeon.getRoom(dungeon.getCurrentRoomReference(player));
		if (room != null) {
			if (room.getRoom() == DungeonUtils.getBossRoomWithChunk(DungeonConstants.FROZEN_FLOORS, 26, 624)) {
				if (player.getGorajanTrailblazer().getExperienceBoost() > 1) {
					return dungeon != null && !dungeon.isAtRewardsScreen();
				}
				if (!player.isCantWalk() && World.getObjectWithType(new WorldTile(player.getX() + Utils.DIRECTION_DELTA_X[dir], player.getY() + Utils.DIRECTION_DELTA_Y[dir], 0), 22) == null) {
					player.getAppearence().setRenderEmote(1429);
					player.setRun(true);
					player.lockROTS();
				}
				if (player.isROTSLocked()) {
					final WorldTile nextStep = new WorldTile(player.getX() + (Utils.DIRECTION_DELTA_X[dir] * 2), player.getY() + (Utils.DIRECTION_DELTA_Y[dir] * 2), 0);
					final NPC boss = getNPC(player, 9929);
					final boolean collides = boss != null && Utils.colides(nextStep.getX(), nextStep.getY(), player.getSize(), boss.getX(), boss.getY(), boss.getSize());
					player.resetWalkSteps();
					final WorldObject object = World.getObjectWithType(new WorldTile(nextStep.getX(), nextStep.getY(), 0), 22);
					if (collides || ((object != null && (object.getId() == 49331 || object.getId() == 49333))) || !player.addWalkSteps(nextStep.getX(), nextStep.getY(), 1)) {
						player.unlockROTS();
						player.getAppearence().setRenderEmote(-1);
					}
				}
			}
		}
		return dungeon != null && !dungeon.isAtRewardsScreen();
	}

	public int getHealedDamage() {
		return healedDamage;
	}

	/*
	 *the dmg you receiving
	 */
/*	@Override
	public void processIngoingHit(Hit hit) {
		damageReceived += hit.getDamage();
	}

	/*
	 * the dmg you doing
	 */
	/*@Override
	public void processIncommingHit(Hit hit, Entity target) {
		if (hit.getLook() == HitLook.MELEE_DAMAGE)
			meleeDamage += hit.getDamage();
		else if (hit.getLook() == HitLook.RANGE_DAMAGE)
			rangeDamage += hit.getDamage();
		if (hit.getLook() == HitLook.MAGIC_DAMAGE)
			mageDamage += hit.getDamage();
	}*/
	
	public void addMeleeDamage(final int damage) {
		meleeDamage += damage;
	}
	
	public void addRangedDamage(final int damage) {
		rangeDamage += damage;
	}
	
	public void addMagicDamage(final int damage) {
		mageDamage += damage;
	}
	
	public void addDamageReceived(final int amount) {
		damageReceived += amount;
	}

	public int getDamageReceived() {
		return damageReceived;
	}

	public int getMeleeDamage() {
		return meleeDamage;
	}

	public int getRangeDamage() {
		return rangeDamage;
	}

	public int getMageDamage() {
		return mageDamage;
	}

	public int getDamage() {
		return meleeDamage + rangeDamage + mageDamage;
	}

	@Override
	public void sendInterfaces() {
		if (dungeon != null && dungeon.isAtRewardsScreen()) {
			return;
		}
		showDeaths();
	}
	
	public void setKilledBossWithLessThan10Hp() {
		killedBossWithLessThan10HP = true;
	}

	/*@Override
	public void processNPCDeath(NPC npc) {
		if (npc instanceof DungeonBoss) {
			if (player.getHitpoints() <= 10)
				killedBossWithLessThan10HP = true;
		}
	}*/

	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		player.getPackets().sendMusicEffect(418);
		if (player.getInventory().containsItem(DungeonConstants.GROUP_GATESTONE, 1)) {
			final WorldTile tile = new WorldTile(player);
			dungeon.setGroupGatestone(new WorldTile(player));
			World.addDungeoneeringGroundItem(dungeon.getParty(), new Item(DungeonConstants.GROUP_GATESTONE), tile, false);
			player.getInventory().deleteItem(DungeonConstants.GROUP_GATESTONE, 1);
			player.getPackets().sendGameMessage("Your group gatestone drops to the floor as you die.");
		}
		if (player.getInventory().containsItem(DungeonConstants.GATESTONE, 1)) {
			final WorldTile tile = new WorldTile(player);
			setGatestone(new WorldTile(player));
			World.addGroundItem(new Item(DungeonConstants.GATESTONE), tile);
			player.getInventory().deleteItem(DungeonConstants.GATESTONE, 1);
			player.getPackets().sendGameMessage("Your gatestone drops to the floor as you die.");
		}
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you are dead!");
					if (dungeon != null) {
						for (final Player p2 : dungeon.getParty().getTeam()) {
							if (p2 == player) {
								continue;
							}
							p2.getPackets().sendGameMessage(player.getDisplayName() + " has died.");
						}
					}
				} else if (loop == 3) {
					player.resetReceivedDamage();
					if (dungeon != null && dungeon.getParty().getTeam().contains(player)) {
						if (dungeon.isAtBossRoom(player, 26, 672, true)) {
							final NPC npc = getNPC(player, 11872);
							if (npc != null) {
								npc.setNextForceTalk(new ForceTalk("Another kill for the Thunderous!"));
								npc.playSoundEffect(1928);
							}
						}
						final WorldTile startRoom = dungeon.getHomeTile();
						player.setNextWorldTile(startRoom);
						dungeon.playMusic(player, dungeon.getCurrentRoomReference(startRoom));
						increaseDeaths();
						player.reset();
					}
					player.setNextAnimation(new Animation(-1));
					player.getAppearence().setRenderEmote(-1);
					hideBar();
				} else if (loop == 4) {
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	private void refreshDeaths() {
		player.getPackets().sendConfigByFile(7554, getDeaths());
		//player.getVarsManager().sendVarBit(2365, getDeaths()); //deaths
	}

	private void increaseDeaths() {
		Integer deaths = dungeon.getPartyDeaths().get(player.getUsername());
		if (deaths == null) {
			deaths = 0;
		} else if (deaths == 15) {
			return;
		}
		dungeon.getPartyDeaths().put(player.getUsername(), deaths + 1);
		//deaths++
		refreshDeaths();
	}

	public int getDeaths() {
		if (dungeon == null) {
			return 0;
		}
		final Integer deaths = dungeon.getPartyDeaths().get(player.getUsername());
		return deaths == null ? 0 : deaths;//deaths;
	}

	@Override
	public boolean processMagicTeleport(final WorldTile toTile) {
		if (dungeon == null || !player.getCombatDefinitions().isDungeonneringSpellBook() || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		return player.getTemporaryAttributtes().get("dungtele") == null;
	}

	@Override
	public boolean processItemTeleport(final WorldTile toTile) {
		return false;
	}

	@Override
	public boolean canTakeItem(final FloorItem item) {
		if (dungeon.isKeyShare()) {
			for (final KeyDoors key : DungeonConstants.KeyDoors.values()) {
				if (item.getId() == key.getKeyId()) {
					dungeon.setKey(key, true);
					World.removeGroundItem(player, item, false);
					return false;
				}
			}
		}
		if (item.getId() == DungeonConstants.GROUP_GATESTONE) {
			dungeon.setGroupGatestone(null);
			return true;
		} else if (item.getId() == DungeonConstants.GATESTONE) {
			if (!item.hasOwner()) {
				World.removeGroundItem(player, item);
				return false;
			} else if (!item.getOwner().equals(player.getUsername())) {
				player.getPackets().sendGameMessage("This isn't your gatestone!");
				return false;
			}
			setGatestone(null);
			return true;
		} else if (!World.isFloorFree(item.getTile().getPlane(), item.getTile().getX(), item.getTile().getY())) {
			player.setNextAnimation(new Animation(833));
			player.setNextFaceWorldTile(item.getTile());
			player.lock(1);
		}
		return true;
	}

	private void openSkillDoor(final SkillDoors s, final WorldObject object, final Room room, final int floorType) {
		final int index = room.getDoorIndexByRotation(object.getRotation());
		if (index == -1) {
			return;
		}
		final Door door = room.getDoor(index);
		if (door == null || door.getType() != DungeonConstants.SKILL_DOOR) {
			return;
		}
		if (door.getLevel() > player.getSkills().getLevel(s.getSkillId())) {
			player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[s.getSkillId()] + " level of " + door.getLevel() + " to remove this " + object.getDefinitions().name.toLowerCase() + ".");
			return;
		}
		int openAnim = -1;
		if (s.getSkillId() == Skills.FIREMAKING) {
			if (!player.getInventory().containsItem(DungeonConstants.TINDERBOX, 1) && !player.getDungeoneeringToolbelt().containsTool(DungeonConstants.TINDERBOX)) {
				player.getPackets().sendGameMessage("You need a tinderbox to do this.");
				return;
			}
		} else if (s.getSkillId() == Skills.MINING) {
			final PickaxeDefinitions defs = Mining.getPickaxeDefinitions(player, true);
			if (defs == null) {
				player.getPackets().sendGameMessage("You do not have a pickaxe or do not have the required level to use the pickaxe.");
				return;
			}
			openAnim = defs.getAnimationId();
		} else if (s.getSkillId() == Skills.MINING) {
			final HatchetDefinitions defs = Woodcutting.getHatchetDefinitions(player, true);
			if (defs == null) {
				player.getPackets().sendGameMessage("You do not have a hatchet or do not have the required level to use the hatchet.");
				return;
			}
			openAnim = defs.getEmoteId();
		}
		final boolean fail = Utils.random(100) <= 10;
		player.lock(3);
		if (s.getOpenAnim() != -1) {
			player.setNextAnimation(new Animation(openAnim != -1 ? openAnim : fail && s.getFailAnim() != -1 ? s.getFailAnim() : s.getOpenAnim()));
		}
		if (s.getOpenGfx() != -1 || s.getFailGfx() != -1) {
			player.setNextGraphics(new Graphics(fail ? s.getFailGfx() : s.getOpenGfx()));
		}
		if (s.getOpenObjectAnim() != -1 && !fail) {
			World.sendObjectAnimation(object, new Animation(s.getOpenObjectAnim()));
		}
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (s.getFailAnim() == -1) {
					player.setNextAnimation(new Animation(-1));
				}
				if (!fail) {
					if (room.getDoor(index) == null) {
						return;
					}
					player.getSkills().addXp(s.getSkillId(), door.getLevel() * 5 + 10);
					room.setDoor(index, null);
					final int openId = s.getOpenObject(floorType);
					if (openId == -1) {
						World.removeObject(object);
					} else {
						dungeon.setDoor(dungeon.getCurrentRoomReference(object), -1, openId, object.getRotation());
					}
				} else {
					player.getPackets().sendGameMessage(s.getFailMessage());
					int damage = door.getLevel() * 4;
					damage -= (damage * ((float)player.getRingOfKinship().getBoost(RingOfKinship.GATHERER) / 100));
					damage -= (damage * ((float)player.getRingOfKinship().getBoost(RingOfKinship.ARTISAN) / 100));
					player.applyHit(new Hit(player, damage, HitLook.REGULAR_DAMAGE));
					if (room.getDoor(index) == null) {
						return;
					}
					if (s.getFailObjectAnim() != -1) {
						World.sendObjectAnimation(object, new Animation(s.getFailObjectAnim()));
					}
				}

			}

		}, 2);

	}
	
	@Override
	public boolean processItemOnNPC(final NPC npc, final Item item){
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom vRoom = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		return vRoom != null && vRoom.processItemOnNPC(player, npc, item);
	}

	@Override
	public boolean processNPCClick1(final NPC npc) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom vRoom = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (vRoom == null || !vRoom.processNPCClick1(player, npc)) {
			return false;
		}
		if (npc.getId() == DungeonConstants.FISH_SPOT_NPC_ID) {
			player.getActionManager().setAction(new DungeoneeringFishing((DungeonFishSpot) npc));
			return false;
		} else if (npc.getId() == 10023) {
			final FrozenAdventurer adventurer = (FrozenAdventurer) npc;
			adventurer.getFrozenPlayer().getAppearence().transformIntoNPC(-1);
			return false;
		} else if (npc.getId() == DungeonConstants.SMUGGLER) {
			npc.faceEntity(player);
			player.getDialogueManager().startDialogue("SmugglerD", dungeon.getParty().getComplexity());
			return false;
		} else if (npc.getId() >= 11076 && npc.getId() <= 11085) {
			DungeoneeringTraps.removeTrap(player, (MastyxTrap) npc, dungeon);
			return false;
		} else if (npc.getId() >= 11096 && npc.getId() <= 11105) {
			DungeoneeringTraps.skinMastyx(player, npc);
			return false;
		} else if (npc instanceof DivineSkinweaver) {
			((DivineSkinweaver) npc).talkTo(player);
			return false;
		}
		return true;
	}

	@Override
	public boolean processNPCClick2(final NPC npc) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom room = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (room == null) {
			return false;
		}
		if (!room.processNPCClick2(player, npc)) {
			return false;
		}
		if (npc instanceof Familiar) {
			final Familiar familiar = (Familiar) npc;
			if (player.getFamiliar() != familiar) {
				player.getPackets().sendGameMessage("That isn't your familiar.");
				return false;
			} else if (familiar.getDefinitions().hasOption("Take")) {
				familiar.takeBob();
				return false;
			}
			return true;
		} else if (npc.getDefinitions().hasOption("Mark")) {
			if (!dungeon.getParty().isLeader(player)) {
				player.getPackets().sendGameMessage("Only your party's leader can mark a target!");
				return false;
			}
			dungeon.setMark(npc, !player.getHintIconsManager().hasHintIcon(6)); //6th slot
			return false;
		} else if (npc.getId() == DungeonConstants.SMUGGLER) {
			DungeonResourceShop.openResourceShop(player, dungeon.getParty().getComplexity());
			return false;
		}
		return true;
	}

	@Override
	public boolean processNPCClick3(final NPC npc) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom room = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (room == null) {
			return false;
		}
		if (!room.processNPCClick3(player, npc)) {
			return false;
		} else if (npc.getId() == DungeonConstants.SMUGGLER) {
			player.getDungeoneeringBinds().sendInterface();
			return false;
		}
		return true;
	}

	public static NPC getNPC(final Entity entity, final int id) {
		final List<Integer> npcsIndexes = World.getRegion(entity.getRegionId()).getNPCsIndexes();
		if (npcsIndexes != null) {
			for (final int npcIndex : npcsIndexes) {
				final NPC npc = World.getNPCs().get(npcIndex);
				if (npc.getId() == id) {
					return npc;
				}
			}
		}
		return null;
	}

	public static NPC getNPC(final Entity entity, final String name) {
		final List<Integer> npcsIndexes = World.getRegion(entity.getRegionId()).getNPCsIndexes();
		if (npcsIndexes != null) {
			for (final int npcIndex : npcsIndexes) {
				final NPC npc = World.getNPCs().get(npcIndex);
				if (npc.getName().equals(name)) {
					return npc;
				}
			}
		}
		return null;
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		Room room = dungeon.getRoom(dungeon.getCurrentRoomReference(player));
		final VisibleRoom vr = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (vr == null || !vr.processObjectClick1(player, object)) {
			return false;
		}
		final int floorType = DungeonUtils.getFloorType(dungeon.getParty().getFloor());
		for (final SkillDoors s : SkillDoors.values()) {
			if (s.getClosedObject(floorType) == object.getId()) {
				if (!player.withinDistance(object, 2)) {
					player.sendMessage("You can't reach that.");
					return false;
				}
				openSkillDoor(s, object, room, floorType);
				return false;
			}
		}
		if (object.getId() >= 54439 && object.getId() <= 54456 && object.getDefinitions().containsOption(0, "Cleanse")) {
			@SuppressWarnings("deprecation")
			final
			NPC boss = dungeon.getTemporaryBoss();//getNPC(player, 11708);
			if (boss == null || !(boss instanceof Gravecreeper)) {
				return false;
			}
			return ((Gravecreeper) boss).cleanseTomb(player, object);
		}
		if (object.getDefinitions().getName().equalsIgnoreCase("energy rift")) {
            player.getActionManager().setAction(new DivinationConvert(player, new Object[]{player.getConvertMode()}));
            player.closeInterfaces();
			return false;
		}
		if (object.getDefinitions().getName().equals("Anvil")) {
			final int barId = DungeoneeringSmithing.getBestBar(player);
			if (barId == 0) {
				player.getPackets().sendGameMessage("You have no bars which you have smithing level to use.");
			} else {
				player.getTemporaryAttributtes().put("dganvil", new WorldTile(object));
				DungeoneeringSmithing.sendInterface(player, barId);
			}
			return false;
		} else if (object.getDefinitions().getName().equals("Furnace")) {
			player.getDialogueManager().startDialogue("DungeoneeringSmeltingD");
			return false;
		}
		if (object.getId() == 49265) {
			final NPC boss = getNPC(player, 9725);
			if (boss == null) {
				player.getPackets().sendGameMessage("You don't need to light anymore.");
				return false;
			}
			((NightGazerKhighorahk) boss).lightPillar(player, object);
			return false;
		} else if (object.getId() >= 49274 && object.getId() <= 49279) {
			if (player.getHash() != player.getDungeoneeringManager().getParty().getDungeon().getRotatedTile(player.getDungeoneeringManager().getParty().getDungeon().getCurrentRoomReference(player), 10, 9).getHash() && player.getHash() != player.getDungeoneeringManager().getParty().getDungeon().getRotatedTile(player.getDungeoneeringManager().getParty().getDungeon().getCurrentRoomReference(player), 5, 9).getHash()) {
				player.sendMessage("You can't reach that.");
				return false;
			}
			final NPC boss = getNPC(player, 9782);
			if (boss != null) {
				((Stomp) boss).chargeLodeStone(player, (object.getId() & 0x1));
			}
			return false;
		} else if (object.getId() == 49268) {
			final PickaxeDefinitions defs = Mining.getPickaxeDefinitions(player, true);
			if (defs == null) {
				player.getPackets().sendGameMessage("You do not have a pickaxe or do not have the required level to use the pickaxe.");
				return false;
			}
			player.setNextAnimation(new Animation(defs.getAnimationId()));
			player.lock(4);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.setNextAnimation(new Animation(-1));
					World.removeObject(object);
				}
			}, 3);
			return false;
		} else if (object.getId() >= 49286 && object.getId() <= 49288) {
			final NPC boss = getNPC(player, 10058);
			if (boss != null) {
				((DivineSkinweaver) boss).blockHole(player, object);
			}
			return false;
		} else if (object.getId() == 49297) {
			final Integer value = (Integer) player.getTemporaryAttributtes().get("cursebearerRot");
			if (value != null && value >= 6) {
				final NPC boss = getNPC(player, 10111);
				if (boss != null) {
					player.getPackets().sendGameMessage("You restore your combat stats, and the skeletal archmage is healed in the process. The font lessens the effect of the rot within your body.");
					player.getTemporaryAttributtes().put("cursebearerRot", 1);
					player.getSkills().restoreSkills();
					boss.heal(boss.getMaxHitpoints() / 10);
				}
			} else {
				player.getPackets().sendGameMessage("You can't restore your stats yet.");
			}
			return false;
		} else if (object.getId() >= KeyDoors.getLowestObjectId() && object.getId() <= KeyDoors.getMaxObjectId()) {
			final int index = room.getDoorIndexByRotation(object.getRotation());
			if (index == -1) {
				return false;
			}
			final Door door = room.getDoor(index);
			if (door == null || door.getType() != DungeonConstants.KEY_DOOR) {
				return false;
			}
			final KeyDoors key = KeyDoors.values()[door.getId()];
			if (!dungeon.hasKey(key) && !player.getInventory().containsItem(key.getKeyId(), 1)) {
				player.getPackets().sendGameMessage("You don't have the correct key.");
				return false;
			}
			player.getInventory().deleteItem(key.getKeyId(), 1);
			player.lock(1);
			player.getPackets().sendGameMessage("You unlock the door.");
			player.setNextAnimation(new Animation(13798));// unlock key
			dungeon.setKey(key, false);
			room.setDoor(index, null);
			World.removeObject(object);
			return false;
		} else if (object.getId() == DungeonConstants.DUNGEON_DOORS[floorType] || object.getId() == DungeonConstants.DUNGEON_GUARDIAN_DOORS[floorType] || object.getId() == DungeonConstants.DUNGEON_BOSS_DOORS[floorType] || DungeonUtils.isOpenSkillDoor(object.getId(), floorType) || (object.getId() >= KeyDoors.getLowestDoorId(floorType) && object.getId() <= KeyDoors.getMaxDoorId(floorType)) || (object.getDefinitions().name.equals("Door") && object.getDefinitions().containsOption(0, "Enter")) //theres many ids for challenge doors
				) {
			if (object.getId() == DungeonConstants.DUNGEON_BOSS_DOORS[floorType] && player.isUnderCombat()) {
				player.getPackets().sendGameMessage("This door is too complex to unlock while in combat.");
				return false;
			}
			final Door door = room.getDoorByRotation(object.getRotation());
			if (door == null) {
				openDoor(object);
				return false;
			}
			if (door.getType() == DungeonConstants.GUARDIAN_DOOR) {
				player.getPackets().sendGameMessage("The door won't unlock until all of the guardians in the room have been slain.");
			} else if (door.getType() == DungeonConstants.KEY_DOOR || door.getType() == DungeonConstants.SKILL_DOOR) {
				player.getPackets().sendGameMessage("The door is locked.");
			} else if (door.getType() == DungeonConstants.CHALLENGE_DOOR) {
				boolean hasWarped = false;
				for (final Player players : dungeon.getParty().getTeam()) {
					if (players.getGorajanTrailblazer().hasOutfit(4)) {
						hasWarped = true;
					}
				}
				if (hasWarped && !dungeon.hasSkippedPuzzle()) {
					((PuzzleRoom) vr).setComplete();
					openDoor(object);
					dungeon.setSkippedPuzzle(true);
					dungeon.getParty().getTeam().forEach(t -> t.sendMessage("A member of the party has used the Warped gorajan trailblazer outfit to unlock a puzzle room."));
					return false;
				}
				player.getPackets().sendGameMessage(((PuzzleRoom) vr).getLockMessage());
			}
			return false;
		} else if (object.getId() == DungeonConstants.THIEF_CHEST_LOCKED[floorType]) {
			room = dungeon.getRoom(dungeon.getCurrentRoomReference(player));
			final int type = room.getThiefChest();
			if (type == -1) {
				return false;
			}
			final int level = type == 0 ? 1 : (type * 10);
			if (level > player.getSkills().getLevel(Skills.THIEVING)) {
				player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[Skills.THIEVING] + " level of " + level + " to open this chest.");
				return false;
			}
			room.setThiefChest(-1);
			player.setNextAnimation(new Animation(536));
			player.lock(2);
			final WorldObject openedChest = new WorldObject(object);
			openedChest.setId(DungeonConstants.THIEF_CHEST_OPEN[floorType]);
			World.spawnObject(openedChest);
			player.getInventory().addItemDrop(DungeonConstants.RUSTY_COINS, Utils.random((type + 1) * 10000) + 1);
			if (Utils.random(2) == 0) {
				player.getInventory().addItemDrop(DungeonConstants.CHARMS[Utils.random(DungeonConstants.CHARMS.length)], Utils.random(5) + 1);
			}
			if (Utils.random(3) == 0) {
				player.getInventory().addItemDrop(DungeoneeringFarming.getHerbForLevel(level), Utils.random(1) + 1);
			}
			if (Utils.random(4) == 0) {
				player.getInventory().addItemDrop(DungeonUtils.getArrows(type + 1), Utils.random(100) + 1);
			}
			if (Utils.random(5) == 0) {
				player.getInventory().addItemDrop(DungeonUtils.getRandomWeapon(type + 1), 1);
			}
			player.getSkills().addXp(Skills.THIEVING, DungeonConstants.THIEF_CHEST_XP[type]);
			return false;
		} else if (object.getId() == DungeonConstants.THIEF_CHEST_OPEN[floorType]) {
			player.getPackets().sendGameMessage("You already looted this chest.");
			return false;
		} else if (DungeonUtils.isLadder(object.getId(), floorType)) {
			if (voteStage != 0) {
				player.getPackets().sendGameMessage("You have already voted to move on.");
				return false;
			}
			player.getDialogueManager().startDialogue("DungeonClimbLadder", this);
			return false;
		} else if (object.getId() == 53977 || object.getId() == 53978 || object.getId() == 53979) {
			final int type = object.getId() == 53977 ? 0 : object.getId() == 53979 ? 1 : 2;
			final NPC boss = getNPC(player, "Runebound behemoth");
			if (boss != null) {
				((RuneboundBehemoth) boss).activateArtifact(player, object, type);
			}
			return false;
		}
		if (com.rs.game.player.content.dungeoneering.skills.farming.DungeoneeringFarming.inspectPatch(player, object)) {
			return false;
		}
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
		case "cooking range (with logs)":
			DungeoneeringFiremaking.lightRange(player, object);
			return false;
		case "altar":
			final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
			if (player.getPrayer().getPrayerpoints() < maxPrayer) {
				player.setNextAnimation(new Animation(645));
				player.getPrayer().restorePrayer(maxPrayer);
				player.sendMessage("You've recharged your prayer points.");
			} else {
				player.sendMessage("You already have full prayer points.");
			}
			return false;
			case "dungeon exit":
				player.getDialogueManager().startDialogue("DungeonExit", this);
				return false;
			case "water trough":
				if (!player.getInventory().containsItem(17490, 1)) {
					player.sendMessage("You need an empty vial to do this.");
					return false;
				}
				WaterFilling.isFilling(player, 17490, false);
				return false;
			case "crate of fish":
				player.sendMessage("These fishes aren't yours for taking.");
				return false;
			case "crate of bows":
			case "crate of battleaxes":
				player.sendMessage("The scout doesn't approve of stealing weaponry.");
				return false;
			case "salve nettles":
				DungeoneeringFarming.initHarvest(player, Harvest.SALVE_NETTLES, object);
				return false;
			case "wildercress":
				DungeoneeringFarming.initHarvest(player, Harvest.WILDERCRESS, object);
				return false;
			case "blightleaf":
				DungeoneeringFarming.initHarvest(player, Harvest.BLIGHTLEAF, object);
				return false;
			case "roseblood":
				DungeoneeringFarming.initHarvest(player, Harvest.ROSEBLOOD, object);
				return false;
			case "bryll":
				DungeoneeringFarming.initHarvest(player, Harvest.BRYLL, object);
				return false;
			case "duskweed":
				DungeoneeringFarming.initHarvest(player, Harvest.DUSKWEED, object);
				return false;
			case "soulbell":
				DungeoneeringFarming.initHarvest(player, Harvest.SOULBELL, object);
				return false;
			case "ectograss":
				DungeoneeringFarming.initHarvest(player, Harvest.ECTOGRASS, object);
				return false;
			case "runeleaf":
				DungeoneeringFarming.initHarvest(player, Harvest.RUNELEAF, object);
				return false;
			case "spiritbloom":
				DungeoneeringFarming.initHarvest(player, Harvest.SPIRITBLOOM, object);
				return false;
			case "tangle gum tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.TANGLE_GUM_VINE));
				return false;
			case "seeping elm tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.SEEPING_ELM_TREE));
				return false;
			case "blood spindle tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.BLOOD_SPINDLE_TREE));
				return false;
			case "utuku tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.UTUKU_TREE));
				return false;
			case "spinebeam tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.SPINEBEAM_TREE));
				return false;
			case "bovistrangler tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.BOVISTRANGLER_TREE));
				return false;
			case "thigat tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.THIGAT_TREE));
				return false;
			case "corpsethorn tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.CORPESTHORN_TREE));
				return false;
			case "entgallow tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.ENTGALLOW_TREE));
				return false;
			case "grave creeper tree":
				player.getActionManager().setAction(new DungeoneeringWoodcutting(object, DungeoneeringTreeDefinitions.GRAVE_CREEPER_TREE));
				return false;
			case "novite ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.NOVITE_ORE));
				return false;
			case "bathus ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.BATHUS_ORE));
				return false;
			case "marmaros ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.MARMAROS_ORE));
				return false;
			case "kratonite ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.KRATONIUM_ORE));
				return false;
			case "fractite ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.FRACTITE_ORE));
				return false;
			case "zephyrium ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.ZEPHYRIUM_ORE));
				return false;
			case "argonite ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.AGRONITE_ORE));
				return false;
			case "katagon ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.KATAGON_ORE));
				return false;
			case "gorgonite ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.GORGONITE_ORE));
				return false;
			case "promethium ore":
				player.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.PROMETHIUM_ORE));
				return false;
			case "runecrafting altar":
				player.getDialogueManager().startDialogue("DungeoneeringRunecraftingSelectionD", 0);
				return false;
			case "spinning wheel":
				player.getDialogueManager().startDialogue("DungeoneeringSpinningD");
				return false;
			case "group gatestone portal":
				portalGroupStoneTeleport();
				return false;
			case "sunken pillar":
				final Blink boss = (Blink) getNPC(player, 12865);
				if (boss == null) {
					player.getPackets().sendGameMessage("The mechanism doesn't respond.");
					return false;
				} else if (boss.hasActivePillar()) {
					player.getPackets().sendGameMessage("The mechanism will not respond while the other pillar is raised.");
					return false;
				}
				for (final Entity t : boss.getPossibleTargets()) {
					if (t.matches(object) || boss.matches(object)) {
						player.getPackets().sendGameMessage("The mechanism cannot be activated while someone is standing there.");
						return false;
					}
				}
				boss.raisePillar(object);
				return false;
			default:
				return true;
		}
	}

	@Override
	public boolean processObjectClick2(final WorldObject object) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}

		final VisibleRoom vr = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (vr == null || !vr.processObjectClick2(player, object)) {
			return false;
		}
		if (com.rs.game.player.content.dungeoneering.skills.farming.DungeoneeringFarming.harvestPatch(player, object)) {
			return false;
		}
		if (object.getId() >= DungeonConstants.FARMING_PATCH_BEGINING && object.getId() <= DungeonConstants.FARMING_PATCH_END) {
			if (object.getDefinitions().containsOption("Inspect")) {
				return false;
			}
			final int value = ((object.getId() - 50042) / 3);
			if (value >= Harvest.values().length) {
				return false;
			}
			final Harvest harvest = Harvest.values()[value];
			if (harvest == null) {
				return false;
			}
			DungeoneeringFarming.initHarvest(player, harvest, object);
			return false;
		}
	      if (object.getDefinitions().getName().equalsIgnoreCase("energy rift")) {
	            player.getInterfaceManager().sendInterface(131);
	            return false;
	        }
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
		case "altar":
			final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
			if (player.getPrayer().getPrayerpoints() < maxPrayer) {
				player.getPrayer().restorePrayer(maxPrayer);
				player.setNextAnimation(new Animation(13711));
				player.sendMessage("You've quickly recharged your prayer points.");
			} else {
				player.sendMessage("You already have full prayer points.");
			}
			return false;
		case "energy rift":
			player.getActionManager().setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_ENERGY }));
			return false;
		case "summoning obelisk":
			if (player.getSkills().getLevel(23) < player.getSkills().getLevelForXp(23)) {
				player.lock(5);
				player.sendMessage("You feel the obelisk", true);
				player.setNextAnimation(new Animation(8502));
				player.setNextGraphics(new Graphics(1308));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.getSkills().restoreSummoning();
						player.sendMessage("...and recharge all your summoning.", true);
					}
				}, 2);
			} else {
				player.sendMessage("You already have full summoning points.");
			}
			return false;
		case "runecrafting altar":
			player.getDialogueManager().startDialogue("DungeoneeringRunecraftingSelectionD", 1);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick3(final WorldObject object) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom vr = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (vr == null || !vr.processObjectClick3(player, object)) {
			return false;
		}
		if (com.rs.game.player.content.dungeoneering.skills.farming.DungeoneeringFarming.clearPatch(player, object)) {
			return false;
		}
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
		case "energy rift":
			player.getActionManager().setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_XP }));
			return false;
		case "runecrafting altar":
			player.getDialogueManager().startDialogue("DungeoneeringRunecraftingSelectionD", 2);
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick4(final WorldObject object) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		if (dungeon.getCurrentRoomReference(player) != null && dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player)) != null && !dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player)).processObjectClick4(player, object)) {
			return false;
		}
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
		case "energy rift":
			player.getActionManager().setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_MORE_XP }));
			return false;
			case "runecrafting altar":
				player.getDialogueManager().startDialogue("DungRunecraftingD", 3);
				return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick5(final WorldObject object) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom vr = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (vr == null || !vr.processObjectClick5(player, object)) {
			return false;
		}
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
			case "runecrafting altar":
				player.getDialogueManager().startDialogue("DungRunecraftingD", 4);
				return false;
		}
		return true;
	}

	public void leaveDungeon() {
		if (dungeon == null || !dungeon.hasStarted()) {
			return;
		}
		dungeon.getParty().leaveParty(player, false);
	}

	public void voteToMoveOn() {
		if (dungeon == null || !dungeon.hasStarted() || voteStage != 0) {
			return;
		}
		voteStage = 1;
		dungeon.voteToMoveOn(player);
	}


	@Override
	public boolean handleItemOnObject(final WorldObject object, final Item item) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		final VisibleRoom room = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
		if (room == null) {
			return false;
		}
		if (!room.handleItemOnObject(player, object, item)) {
			return false;
		}
		final String name = object.getDefinitions().name.toLowerCase();
		switch (name) {
		case "cooking range (empty)":
			DungeoneeringFiremaking.addLogs(player, object, item);
			return false;
		case "fire":
		case "cooking range (lit)":
			if (!DungeoneeringCooking.isCookable(player, item.getId(), object.getId())) {
				player.sendMessage("You can't cook this on a fire.");
			}
			return false;
			case "farming patch":
				if (com.rs.game.player.content.dungeoneering.skills.farming.DungeoneeringFarming.plantSeed(player, item, object)) {
					return false;
				}
			case "furnace":
				if (item.getId() >= 17650 && item.getId() <= 17668) {
					DungeoneeringSmithing.sendInterface(player, item.getId());
				} else {
					player.sendMessage("You can't smelt out of that.");
				}
				return false;
			case "anvil":
				player.getTemporaryAttributtes().put("dganvil", new WorldTile(object));
				DungeoneeringSmithing.sendInterface(player, item.getId());
				return false;
		}
		return true;
	}

	@Override
	public boolean canUseItemOnItem(final Item itemUsed, final Item usedWith) {
		if (dungeon == null || !dungeon.hasStarted() || dungeon.isAtRewardsScreen()) {
			return false;
		}
		if (DungeoneeringFletching.isFletchable(player, itemUsed, usedWith)) {
			return false;
		}
		if (DungeoneeringCrafting.isCraftable(player, itemUsed, usedWith)) {
			return false;
		}
		if (DungeoneeringHerblore.isValidPotion(player, itemUsed, usedWith)) {
			return false;
		}
		if (DungeoneeringFiremaking.isFiremaking(player, itemUsed, usedWith)) {
			return false;
		}
		return !WeaponPoison.poisons(player, itemUsed, usedWith, true);
	}

	public void openDoor(final WorldObject object) {
		if (!player.withinDistance(object, 1)) {
			player.sendMessage("You can't reach that.");
			return;
		}
		final RoomReference roomReference = dungeon.getCurrentRoomReference(player);
		if (dungeon.enterRoom(player, roomReference.getX() + Utils.ROTATION_DIR_X[object.getRotation()], roomReference.getY() + Utils.ROTATION_DIR_Y[object.getRotation()])) {
		}
		hideBar();
	}

	/**
	 * called once teleport is performed
	 */
	@Override
	public void magicTeleported(final int type) {
		dungeon.playMusic(player, dungeon.getCurrentRoomReference(player.getNextWorldTile()));
		hideBar();
	}

	@Override
	public boolean keepCombating(boolean mainHand, final Entity target) {
		if (target instanceof DungeonSlayerNPC) {
			final DungeonSlayerNPC npc = (DungeonSlayerNPC) target;

			for (int index = 0; index < DungeonConstants.SLAYER_CREATURES.length; index++) {
				if (npc.getId() == DungeonConstants.SLAYER_CREATURES[index]) {
					final int level = DungeonConstants.SLAYER_LEVELS[index];

					if (player.getSkills().getLevel(Skills.SLAYER) < level) {
						player.getPackets().sendGameMessage("You need a Slayer level of " + level + " in order to attack this monster.");
						return false;
					}
					continue;
				}
			}
		}
		return true;
	}

	/*
	 * return process normaly
	 */

	@Override
	public boolean processButtonClick(final int interfaceId, final int componentId, final int slotId, final int slotId2, final int packetId) {

		if (dungeon == null || !dungeon.hasStarted()) {
			return false;
		}

		if (dungeon.isAtRewardsScreen()) {
			if (interfaceId == 933) {
				if (componentId >= 62 && componentId <= 66) {
					if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
						player.getDialogueManager().startDialogue("DungeonLeave", this);
					} else {
						if (voteStage == 2) {
							return false;
						}
						voteStage = 2;
						dungeon.ready(player);
					}
				}
			}
			return false;
		} else {
			if (interfaceId == 995) {
				DungeoneeringConstruction.handleInterface(player, componentId);
				return false;
			} else if (interfaceId == 746 && componentId == 238) {
				Magic.sendNormalTeleportSpell(player, 0, 0, dungeon.getHomeTile());
				player.getTemporaryAttributtes().put("dungtele", true);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.getTemporaryAttributtes().remove("dungtele");
					}
				}, 4);
				return false;
			}
			if (interfaceId == 548) {
				if (componentId == 148) {
					if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
						player.getPackets().sendGameMessage("Please finish what you're doing before opening the dungeon map.");
						return false;
					}
					dungeon.openMap(player);
					return false;
				}
				return false;
			} else if (interfaceId == Inventory.INVENTORY_INTERFACE) {
				final Item item = player.getInventory().getItem(slotId);
				if (item == null || item.getId() != slotId2) {
					return false;
				}
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					if (DungeoneeringFletching.isFletchable(player, item)) {
						return false;
					}
					if (DungeoneeringCrafting.isCraftable(player, item)) {
						return false;
					}
					if (DungeoneeringHerbCleaning.clean(player, item)) {
						return false;
					}
					if (item.getId() == 17415) {
						player.getDialogueManager().startDialogue("ThreeStatuesStoneD");
						return false;
					}

					for (int index = 0; index < DungeoneeringTraps.ITEM_TRAPS.length; index++) {
						if (item.getId() == DungeoneeringTraps.ITEM_TRAPS[index]) {
							DungeoneeringTraps.placeTrap(player, dungeon, index);
							return false;
						}
					}
					for (int i = 0; i < PoltergeistRoom.HERBS.length; i++) {
						if (item.getId() == PoltergeistRoom.HERBS[i]) {
							final VisibleRoom room = dungeon.getVisibleRoom(dungeon.getCurrentRoomReference(player));
							if (room == null) {
								return false;
							}
							if (!(room instanceof PoltergeistRoom)) {
								player.getPackets().sendGameMessage("You need to be closer to the poltergeist to cleanse this herb.");
								return false;
							}
							((PoltergeistRoom) room).consecrateHerbs(player, item.getId());
							return false;
						}
					}
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					if (DungeoneeringFiremaking.isFiremaking(player, item.getId())) {
						return false;
					}
					if (item.getId() == 17415) {
						ThreeStatuesStoneD.craftWeapon(player, 2);
						return false;
					}
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					if (item.getId() == 17415) {
						ThreeStatuesStoneD.craftWeapon(player, 3);
						return false;
					}
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					if (item.getId() == 17415) {
						ThreeStatuesStoneD.craftWeapon(player, 4);
						return false;
					}
				}
				return true;
			} else if (interfaceId == DungeonResourceShop.RESOURCE_SHOP) {
				// Accept grid clicks from 24 or 25 (and 23 as a fallback)
				if (componentId == 24 || componentId == 25 || componentId == 23) {
					int quantity = -2; // sentinel
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
						// "Value"
						quantity = -1;
					} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
						quantity = 1;
					} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
						quantity = 5;
					} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
						quantity = 10;
					} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
						quantity = 50;
					}
					if (quantity != -2) {
						DungeonResourceShop.handlePurchaseOptions(player, slotId, quantity);
					}
				}
				return false;
			} else if (interfaceId == DungeonResourceShop.RESOURCE_SHOP_INV) {
				if (componentId == 0) {
					int quantity = -2; // sentinel
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
						// "Value" on inventory -> we don't buy here; do nothing special
						quantity = -1; // this will show the value via handleSellOptions
					} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
						quantity = 1;
					} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
						quantity = 5;
					} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
						quantity = 10;
					} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
						quantity = 50;
					}
					if (quantity != -2) {
						DungeonResourceShop.handleSellOptions(player, slotId, slotId2, quantity);
					}
				}
				return false;
			}
			return true;
		}
	}



	public void createGatestone(final boolean skipDialogue) {
		if (player.getLunarDelay() > Utils.currentTimeMillis()) {
			return;
		}
		if (!canCreateGatestone()) {
			if (!skipDialogue) {
				player.getDialogueManager().startDialogue("DestroyCurrentGatestoneD");
				return;
			} else {
				player.getInventory().deleteItem(DungeonConstants.GATESTONE, 1);
				if (gatestone != null) {
					final FloorItem item = World.getRegion(gatestone.getRegionId()).getGroundItem(DungeonConstants.GATESTONE, gatestone, player);
					if (item == null) {
						return;
					}
					World.removeGroundItem(player, item, false);
					setGatestone(null);
				}
			}
		}
//		if (!Magic.checkCombatSpell(player, 38, -1, false)) {
//			return;
//		}
		player.getInventory().addItem(DungeonConstants.GATESTONE, 1);
		player.setLunarDelay(2500);
//		Magic.checkCombatSpell(player, 38, -1, true);
		player.getSkills().addXp(Skills.MAGIC, 43.5);
		player.setNextAnimation(new Animation(713));
		player.setNextGraphics(new Graphics(113));
	}

	@Override
	public boolean canDropItem(final Item item) {
		if (item.getDefinitions().isRingOfKinship()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You cannot destroy that here.");
			return false;
		}
		if (item.getDefinitions().isDestroyItem()) {
			return true;
		}
		final WorldTile currentTile = new WorldTile(player);
		player.stopAll(false);
		player.getInventory().deleteItem(item);
		if (item.getId() == DungeonConstants.GROUP_GATESTONE) {
			dungeon.setGroupGatestone(currentTile);
		} else if (item.getId() == DungeonConstants.GATESTONE) {
			setGatestone(currentTile);
			World.addGroundItem(item, currentTile, player, true, -1, 2, -1, false);
			player.getPackets().sendGameMessage("You place the gatestone. You can teleport back to it at any time.");
			return false;
		}
		World.addGroundItem(item, currentTile);
		return false;
	}

	@SuppressWarnings("unused")
    private void stoneTeleport(final boolean group) {
		WorldTile tile = group ? dungeon.getGroupGatestone() : gatestone;
		if (tile == null && group) {
			tile = new WorldTile(dungeon.getParty().getLeaderPlayer());
		}
		/*if (dungeon.isAtBossRoom(player, 26, 626, true) || (dungeon.isAtBossRoom(player, 26, 672, true) && !YkLagorThunderous.isBehindPillar(player, dungeon, dungeon.getCurrentRoomReference(new WorldTile(player))))) {
			player.getPackets().sendGameMessage("You can't teleport here.");
			return;
		} else */if (tile == null) { // Shouldn't happen for group gatestone
			player.getPackets().sendGameMessage("You currently do not have an active gatestone.");
			return;
		}
		
		player.stopAll();

//		if (!group) {
//			final FloorItem item = World.getRegion(gatestone.getRegionId()).getGroundItem(DungeonConstants.GATESTONE, tile, player);
//			if (item == null) {
//				return;
//			}
//			World.removeGroundItem(player, item);
//			player.getInventory().deleteItem(item);
//			setGatestone(null);
//		}
		/*if (!Magic.checkRunes(player, true, true, group ? DungeonConstants.GROUP_GATESTONE_RUNES : new int[] {}))
			return;*/
//		if (group) {
//			Magic.checkSpellRequirements(player, 64, true, 17792, 3);
//		}
		if (group)
			Magic.sendTeleportSpell(player, 13288, 13285, 2516, 2517, group ? 64 : 32, 0, tile, 3, false, Magic.MAGIC_TELEPORT, 17792, 3);
		else
			Magic.sendTeleportSpell(player, 13288, 13285, 2516, 2517, group ? 64 : 32, 0, tile, 3, false, Magic.MAGIC_TELEPORT);
		player.getTemporaryAttributtes().put("dungtele", true);
		if (!group) {
			//player.unlock(); TODO Fix the smuggle this unlocks which lets players be able to be kicked
			player.setCantWalk(true);
			player.getEmotesManager().setNextEmoteEnd(3000); //prevents dropping etc
			final FloorItem item = World.getRegion(gatestone.getRegionId()).getGroundItem(DungeonConstants.GATESTONE, tile, player);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setCantWalk(false);
					if (item == null) {
						return;
					}
					World.removeGroundItem(player, item);
					player.getInventory().deleteItem(item);
					setGatestone(null);
				}
			}, 4);
		}
		
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("dungtele");
			}
			
		}, 4);
	}

	private void portalGroupStoneTeleport() {
		final WorldTile tile = dungeon.getGroupGatestone();
		if (tile == null) {
			return;
		}
		player.getTemporaryAttributtes().put("dungtele", true);
		Magic.sendTeleportSpell(player, 14279, 13285, 2518, 2517, 1, 0, tile, 1, false, Magic.OBJECT_TELEPORT);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("dungtele");
			}
		}, 4);
	}

	private boolean canCreateGatestone() {
		if (player.getInventory().containsItem(DungeonConstants.GATESTONE, 1)) {
			player.getPackets().sendGameMessage("You already have a gatestone in your pack.");
			return false;
		}
		if (gatestone != null) {
			player.getPackets().sendGameMessage("You already have an active gatestone.");
			return false;
		} 
		/*else if (!Magic.checkRunes(player, true, true, 17789, 3))
			return false;*/
		return true;
	}

	public void leaveDungeonPermanently() {
		final int index = dungeon.getParty().getIndex(player);
		leaveDungeon();
		for (final Player p2 : dungeon.getParty().getTeam()) {
			p2.getPackets().sendGlobalConfig(1397 + index, 2);
		}
	}

	@Override
	public void forceClose() {
		if (dungeon != null) {
			dungeon.getParty().leaveParty(player, false);
		} else { //rollback prevent taking items
			player.getInventory().reset();
			player.getEquipment().reset();
			player.setForceMultiArea(false);
			/**
			 * So it loads the familiar properly, sends nullpointer otherwise.
			 */
			if (player.getFamiliar() != null) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						if (player.getFamiliar() != null) {
							player.getFamiliar().sendDeath(player);
							player.sendMessage("Your familiar vanishes as it left the depths of Daemonheim.");
						}
					}

				}, 5);
			}
		}
	}

	@Override
	public boolean logout() {
		return false;
	}

	private void setGatestone(final WorldTile gatestone) {
		this.gatestone = gatestone;
	}

	public boolean isKilledBossWithLessThan10HP() {
		return killedBossWithLessThan10HP;
	}
}
