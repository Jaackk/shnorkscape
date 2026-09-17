package com.rs.game.activites.duel;

import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Pots.Pot;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.Utils;

public class DuelArena extends Controller {

	private static final int LOGOUT = 0, TELEPORT = 1, DUEL_END_LOSE = 2, DUEL_END_WIN = 3;
	private static final WorldTile[] LOBBY_TELEPORTS = { new WorldTile(3335, 3235, 0), new WorldTile(3336, 3232, 0), new WorldTile(3339, 3230, 0), new WorldTile(3336, 3227, 0), new WorldTile(3343, 3230, 0), new WorldTile(3347, 3232, 0),
			new WorldTile(3346, 3236, 0) };

	public boolean ifFriendly, isDueling;
	public Player target;

	private ItemsContainer<Item> items;

	private void accept(boolean firstStage) {
		if (target == null || player == null)
			return;
		boolean accepted, targetAccepted;
		try {
			accepted = (Boolean) player.getTemporaryAttributtes().get("acceptedDuel");
			targetAccepted = (Boolean) target.getTemporaryAttributtes().get("acceptedDuel");
		} catch (NullPointerException e) {
			return;
		}
		DuelRules rules = player.getDuelRules();
		if (!rules.canAccept(player.getDuelRules().getStake()))
			return;
		if (accepted && targetAccepted) {
			if (firstStage) {
				if (nextStage())
					if (target != null) {
						((DuelArena) target.getControlerManager().getControler()).nextStage();
					}
			} else {
				player.setCloseInterfacesEvent(null);
				player.closeInterfaces();
				closeDuelInteraction(true, DuelStage.DONE);
			}
			return;
		}
		player.getTemporaryAttributtes().put("acceptedDuel", true);
		refreshScreenMessages(firstStage, ifFriendly);
	}

	public void addItem(int slot, int amount) {
		if (!hasTarget())
			return;
		Item item = player.getInventory().getItem(slot);
		if (item == null)
			return;
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item cannot be staked.");
			return;
		}
		Item[] itemsBefore = player.getDuelRules().getStake().getItemsCopy();
		int maxAmount = player.getInventory().getItems().getNumberOf(item);
		if (amount < maxAmount)
			item = new Item(item.getId(), amount);
		else
			item = new Item(item.getId(), maxAmount);
		player.getDuelRules().getStake().add(item);
		player.getInventory().deleteItem(slot, item);
		refreshItems(itemsBefore);
		refresh(slot);
		cancelAccepted();
	}

	public void addPouchToDuel(int slot, int amount) {
		synchronized (this) {
			synchronized (target.getItemTransaction()) {
				Item item = null;
				Item[] itemsBefore = items.getItemsCopy();
				if (amount >= player.getMoneyPouch().getTotal())
					amount = player.getMoneyPouch().getTotal();
				item = new Item(995, amount);
				if (item.getAmount() + items.getNumberOf(item) < 0 || item.getAmount() + items.getNumberOf(item) < 0) {
					return;
				}
				if (player.getMoneyPouch().removeMoneyMisc(amount)) {
					items.add(item);
					refreshItems(itemsBefore);
					cancelAccepted();
				} else {
					closeDuelInteraction(true, DuelStage.DONE);
				}
			}
		}
	}

	public void addPouch(int slot, int amount) {
		if (!hasTarget())
			return;
		Item[] itemsBefore = player.getDuelRules().getStake().getItemsCopy();
		Item item = player.getInventory().getItem(slot);
		if (amount > player.getMoneyPouch().getTotal())
			amount = player.getMoneyPouch().getTotal();
		if (player.getMoneyPouch().removeMoneyMisc(amount)) {
			item = new Item(995, amount);
			player.getDuelRules().getStake().add(item);
		}
		refreshItems(itemsBefore);
		refresh(slot);
		cancelAccepted();
	}

	private void beginBattle(boolean started) {
		if (started) {
			int random = 0;
			if (player.getDuelRules().getRule(24)) {
				WorldTile[] teleports = getPossibleWorldTilesSummoning();
				random = Utils.getRandom(1);
				player.setNextWorldTile(random == 0 ? teleports[0] : teleports[1]);
				target.setNextWorldTile(random == 0 ? teleports[1] : teleports[0]);
			} else {
				WorldTile[] teleports = getPossibleWorldTiles();
				random = Utils.getRandom(1);
				player.setNextWorldTile(random == 0 ? teleports[0] : teleports[1]);
				target.setNextWorldTile(random == 0 ? teleports[1] : teleports[0]);
			}
		}
		player.stopAll();
		player.lock(3);
		player.reset();
		isDueling = true;
		player.getTemporaryAttributtes().put("startedDuel", true);
		player.getTemporaryAttributtes().put("canFight", false);
		player.setCanPvp(true);
		player.getHintIconsManager().addHintIcon(target, 1, -1, false);
		player.getAppearence().generateAppearenceData();
		if (player.getEquipment().getAuraId() != -1 && player.getAuraManager().ignoreDuelIsActivated())
			player.sendMessage("Your aura has temporarily been deactivated throughout the duel, it'll be reinstated after the end of the duel. The timer will not continue during this time.");
		WorldTasksManager.schedule(new WorldTask() {

			int count = 3;

			@Override
			public void run() {
				if (count > 0)
					player.setNextForceTalk(new ForceTalk("" + count));
				if (count == 0) {
					player.getTemporaryAttributtes().put("canFight", true);
					player.setNextForceTalk(new ForceTalk("FIGHT!"));
					this.stop();
				}
				count--;
			}
		}, 0, 2);
	}

	@Override
	public boolean processPlayerOption1(Entity target) {
		if (player.getTemporaryAttributtes().get("canFight") == Boolean.FALSE) {
			player.getPackets().sendGameMessage("The duel hasn't started yet.", true);
			return false;
		}
		return !player.isDead() && !target.isDead();
	}

	public void cancelAccepted() {
		boolean canceled = false;
		if ((Boolean) player.getTemporaryAttributtes().get("acceptedDuel")) {
			player.getTemporaryAttributtes().put("acceptedDuel", false);
			canceled = true;
		}
		if ((Boolean) target.getTemporaryAttributtes().get("acceptedDuel")) {
			target.getTemporaryAttributtes().put("acceptedDuel", false);
			canceled = true;
		}
		if (canceled)
			refreshScreenMessages(canceled, ifFriendly);
	}

	@Override
	public boolean canEat(Food food) {
		if (player.getDuelRules().getRule(4) && isDueling) {
			player.sendMessage("You cannot eat during this duel.", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canEquip(int slotId, int itemId) {
		Item item = new Item(itemId);
		DuelRules rules = player.getDuelRules();
		if (isDueling) {
			player.getEquipment();
			if (rules.getRule(15) && Equipment.isTwoHandedWeapon(item)) {
				player.getPackets().sendGameMessage("You can't equip " + ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase() + " during this duel.");
				return false;
			}
			if (rules.getRule(10 + slotId)) {
				player.getPackets().sendGameMessage("You can't equip " + ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase() + " during this duel.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canHit(Entity target) {
		Player p2 = (Player) target;
		return !player.isDead() && !p2.isDead();
	}

	@Override
	public boolean canMove(int dir) {
		if (player.getDuelRules().getRule(25) && isDueling) {
			player.getPackets().sendGameMessage("You cannot move during this duel!", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canPot(Pot pot) {
		if (player.getDuelRules().getRule(3) && isDueling) {
			player.getPackets().sendGameMessage("You cannot drink during this duel.", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canSummonFamiliar() {
		if (player.getDuelRules().getRule(24) && isDueling)
			return true;
		player.getPackets().sendGameMessage("Summoning has been disabled during this duel!");
		return false;
	}

	@Override
	public boolean processCommand(String command, boolean console, boolean clientCommand) {
		if (!player.isOwner()) {
			player.getPackets().sendGameMessage("You can't use commands here!");
			return false;
		}
		return true;
	}

	private void reset() {
		target = null;
		player.getTemporaryAttributtes().put("acceptedDuel", false);
	}

	public void closeDuelInteraction(DuelStage stage) {
		synchronized (this) {
			final Player oldTarget = target;
			Controller controler = oldTarget == null ? null : oldTarget.getControlerManager().getControler();
			if (controler == null || !(controler instanceof DuelArena))
				return;
			DuelArena targetConfiguration = (DuelArena) controler;
			synchronized (controler) {
				if (hasTarget() && targetConfiguration.hasTarget()) {
					if (controler instanceof DuelArena) {
						player.setCloseInterfacesEvent(null);
						player.closeInterfaces();
						oldTarget.setCloseInterfacesEvent(null);
						oldTarget.closeInterfaces();
						if (stage != DuelStage.DONE) {
							reset();
							player.getInventory().getItems().addAll(player.getDuelRules().getStake());
							oldTarget.getInventory().getItems().addAll(oldTarget.getDuelRules().getStake());
							oldTarget.getDuelRules().getStake().clear();
							player.getDuelRules().getStake().clear();
							player.getInventory().init();
							oldTarget.getInventory().init();
							WorldTasksManager.schedule(new WorldTask() {

								@Override
								public void run() {
									player.getControlerManager().startControler("DuelControler");
									oldTarget.getControlerManager().startControler("DuelControler");
								}
							}, 1);
						} else {
							removeEquipment();
							targetConfiguration.removeEquipment();
							beginBattle(true);
							targetConfiguration.beginBattle(false);
						}
						if (stage == DuelStage.DONE)
							player.sendMessage("Your battle will begin shortly.");
						else if (stage == DuelStage.SECOND)
							player.sendMessage("<col=ff0000>Please check if these settings are correct.");
						else if (stage == DuelStage.DECLINED)
							oldTarget.sendMessage("<col=ff0000>Other player declined the duel!");
						else if (stage == DuelStage.DECLINED) {
							oldTarget.sendMessage("You do not have enough space to continue!");
							oldTarget.sendMessage("Other player does not have enough space to continue!");
						}
					}
				}
			}
		}
	}

	protected void closeDuelInteraction(boolean started, DuelStage duelStage) {
		Player oldTarget = target;
		if (duelStage != DuelStage.DONE) {
			target = null;
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.getControlerManager().startControler("DuelControler");
				}
			});
			player.getInventory().getItems().addAll(player.getDuelRules().getStake());
			player.getInventory().init();
			player.getDuelRules().getStake().clear();
		} else {
			removeEquipment();
			beginBattle(started);
		}
		if (oldTarget == null)
			return;
		Controller controler = oldTarget.getControlerManager().getControler();
		if (controler == null || !(controler instanceof DuelArena))
			return;
		DuelArena targetConfiguration = (DuelArena) controler;
		if (controler instanceof DuelArena) {
			if (targetConfiguration.hasTarget()) {
				oldTarget.setCloseInterfacesEvent(null);
				oldTarget.closeInterfaces();
				if (duelStage != DuelStage.DONE)
					player.getControlerManager().removeControlerWithoutCheck();
				if (started)
					targetConfiguration.closeDuelInteraction(false, duelStage);
				if (duelStage == DuelStage.DONE)
					player.getPackets().sendGameMessage("Your battle will begin shortly.");
				else if (duelStage == DuelStage.SECOND)
					player.getPackets().sendGameMessage("<col=ff0000>Please check if these settings are correct.");
				else if (duelStage == DuelStage.DECLINED)
					oldTarget.getPackets().sendGameMessage("<col=ff0000>Other player declined the duel!");
				else if (duelStage == DuelStage.DECLINED) {
					oldTarget.getPackets().sendGameMessage("You do not have enough space to continue!");
					oldTarget.getPackets().sendGameMessage("Other player does not have enough space to continue!");
				}
			}
		}
	}

	public void endDuel(final Player victor, final Player loser, boolean isDraw) {
		startEndingTeleport(victor, false);
		startEndingTeleport(loser, false);
		if (!isDueling)
			return;
		Controller controler = target == null ? null : target.getControlerManager().getControler();
		if (controler == null || !(controler instanceof DuelArena))
			return;
		DuelArena targetConfiguration = (DuelArena) controler;
		targetConfiguration.isDueling = false;
		isDueling = false;
		DuelRules rules = victor == null ? loser.getDuelRules() : victor.getDuelRules();
		if (rules != null && !rules.hasRewardGiven()) {
			DuelRules.sendRewardGivenUpdate(victor, loser, true);
			CopyOnWriteArrayList<Item> wonItems = new CopyOnWriteArrayList<Item>();
			for (Item item : victor.getDuelRules().getStake().getItems()) {
				if (item == null)
					continue;
				wonItems.add(item);
				if (item.getId() == 995)
					victor.addMoney(item.getAmount());
				else
					victor.getInventory().addItemDrop(item.getId(), item.getAmount());
				Logger.getGlobal().info(item.getName() + " has been won by " + victor.getUsername() + " from " + loser.getUsername());
			}
			LoggingSystem.logDuelStake(player, victor, wonItems);
			CopyOnWriteArrayList<Item> lostItems = new CopyOnWriteArrayList<Item>();
			for (Item item : loser.getDuelRules().getStake().getItems()) {
				if (item == null)
					continue;
				if (isDraw) {
					if (item.getId() == 995)
						loser.addMoney(item.getAmount());
					else
						loser.getInventory().addItemDrop(item.getId(), item.getAmount());
					Logger.getGlobal().info("Draw happened for: " + victor.getUsername());
				} else {
					lostItems.add(item);
					if (item.getId() == 995)
						victor.addMoney(item.getAmount());
					else
						victor.getInventory().addItemDrop(item.getId(), item.getAmount());
					Logger.getGlobal().info(victor.getUsername() + " has won " + item.getName() + " from " + loser.getUsername());
				}
			}
			LoggingSystem.logLostDuelStake(loser, victor, lostItems);
			sendFinishInterface(victor, loser);
			victor.getDuelRules().getStake().clear();
			loser.getDuelRules().getStake().clear();

			// Clear just incase
			lostItems.clear();
			wonItems.clear();
		}
		loser.sendMessage(isDraw ? "The battle has ended in a draw." : "Oh dear, it seems you have lost to " + victor.getDisplayName() + ".");
		victor.sendMessage(isDraw ? "The battle has ended in a draw." : "Congratulations! You easily defeated " + loser.getDisplayName() + ".");
		loser.setCanPvp(false);
		loser.getHintIconsManager().removeUnsavedHintIcon();
		loser.reset();
		loser.closeInterfaces();
		loser.getControlerManager().removeControlerWithoutCheck();
		victor.setCanPvp(false);
		victor.getHintIconsManager().removeUnsavedHintIcon();
		victor.reset();
		victor.getControlerManager().removeControlerWithoutCheck();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				loser.getControlerManager().startControler("DuelControler");
				victor.getControlerManager().startControler("DuelControler");
			}
		});
	}

	private String getAcceptMessage(boolean firstStage) {
		if (target.getTemporaryAttributtes().get("acceptedDuel") == Boolean.TRUE)
			return "Other player has accepted.";
		else if (player.getTemporaryAttributtes().get("acceptedDuel") == Boolean.TRUE)
			return "Waiting for other player...";
		return firstStage ? "" : "Please look over the agreements to the duel.";
	}

	private WorldTile[] getPossibleWorldTiles() {
		final int arenaChoice = Utils.getRandom(3);
		WorldTile[] locations = new WorldTile[3];
		int[] arenaBoundariesX = { 3345, 3377, 3384, 3377, 3345 };
		int[] arenaBoundariesY = { 3213, 3214, 3232, 3251, 3251 };
		int[] maxOffsetX = { 10, 10, 4, 10, 10 };
		int[] maxOffsetY = { 6, 6, 4, 6, 6 };
		int finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
		int finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		locations[0] = (new WorldTile(finalX, finalY, 0));
		if (player.getDuelRules().getRule(25)) {
			int direction = Utils.getRandom(1);
			if (direction == 0) {
				finalX--;
			} else {
				finalY++;
			}
		} else {
			finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
			finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		}
		locations[1] = (new WorldTile(finalX, finalY, 0));
		return locations;
	}

	private WorldTile[] getPossibleWorldTilesSummoning() {
		final int arenaChoice = Utils.getRandom(1);
		WorldTile[] locations = new WorldTile[2];
		int[] arenaBoundariesX = { 3233, 3233 };
		int[] arenaBoundariesY = { 5151, 5087 };
		int[] maxOffsetX = { 10, 10 };
		int[] maxOffsetY = { 6, 6 };
		int finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
		int finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		locations[0] = (new WorldTile(finalX, finalY, 0));
		if (player.getDuelRules().getRule(25)) {
			int direction = Utils.getRandom(1);
			if (direction == 0)
				finalX--;
			else
				finalY++;
		} else {
			finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
			finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		}
		locations[1] = (new WorldTile(finalX, finalY, 0));
		return locations;
	}

	public Entity getTarget() {
		if (hasTarget())
			return target;
		return null;
	}

	public boolean hasTarget() {
		return target != null;
	}

	public boolean isDueling() {
		return isDueling;
	}

	public boolean isWearingTwoHandedWeapon() {
		Item item = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
		if (item == null)
			return false;
		return Equipment.isTwoHandedWeapon(item);
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity victim) {
		DuelRules rules = player.getDuelRules();
		boolean isRanging = player.getCombatDefinitions().getType(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD) == Combat.RANGE_TYPE;
		if (player.getTemporaryAttributtes().get("canFight") == Boolean.FALSE) {
			player.getPackets().sendGameMessage("The duel hasn't started yet.", true);
			return false;
		}
		if (target != victim)
			return false;
		if (player.getCombatDefinitions().getSpellId(!mainHand) > 0 && rules.getRule(2) && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Magic in this duel!", true);
			return false;
		} else if (isRanging && rules.getRule(0) && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Range in this duel!", true);
			return false;
		} else if (!isRanging && rules.getRule(1) && player.getCombatDefinitions().getSpellId(!mainHand) <= 0 && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Melee in this duel!", true);
			return false;
		}
		return !player.isDead() && !victim.isDead();
	}

	@Override
	public boolean login() {
		return true;
	}

	@Override
	public boolean logout() {
		end(LOGOUT);
		startEndingTeleport(player, true);
		return false;
	}

	@Override
	public void forceClose() {
		end(DUEL_END_LOSE);
	}

	// dupe function
	private void end(int type) {
		if (isDueling) {
			boolean bothDead = player.isDead() && target.isDead();
			if (type == LOGOUT || type == DUEL_END_LOSE || type == TELEPORT)
				endDuel(target, player, bothDead);
			else if (type == DUEL_END_WIN)
				endDuel(player, target, bothDead);
		} else
			closeDuelInteraction(DuelStage.DECLINED);
	}

	@Override
	public void magicTeleported(int type) {
		end(TELEPORT);
	}

	public boolean nextStage() {
		if (!hasTarget())
			return false;
		if (player.getInventory().getItems().getUsedSlots() + target.getDuelRules().getStake().getUsedSlots() > 28) {
			player.setCloseInterfacesEvent(null);
			player.closeInterfaces();
			closeDuelInteraction(true, DuelStage.NO_SPACE);
			player.sendMessage("You do not have enough space in your inventory for the stake!");
			return false;
		}
		player.getTemporaryAttributtes().put("acceptedDuel", false);
		openConfirmationScreen(false);
		player.getInterfaceManager().closeInventoryInterface();
		return true;
	}

	private void openConfirmationScreen(boolean ifFriendly) {
		player.getInterfaceManager().sendInterface(ifFriendly ? 639 : 626);
		refreshScreenMessage(false, ifFriendly);
	}

	private void openDuelScreen(Player target, boolean ifFriendly) {
		synchronized (this) {
			if (!ifFriendly) {
				sendOptions(player);
				player.getDuelRules().getStake().clear();
			}
			if (player.getInterfaceManager().containsChatBoxInter())
				player.getInterfaceManager().closeChatBoxInterface();
			player.getTemporaryAttributtes().put("acceptedDuel", false);
			player.getPackets().sendItems(134, false, player.getDuelRules().getStake());
			player.getPackets().sendItems(134, true, player.getDuelRules().getStake());
			player.getPackets().sendIComponentText(ifFriendly ? 637 : 631, ifFriendly ? 18 : 40, "" + (target.getSkills().getCombatLevel()));
			player.getVarBitManager().sendVar(286, 0);
			player.getTemporaryAttributtes().put("firstScreen", true);
			player.getInterfaceManager().sendInterface(ifFriendly ? 637 : 631);
			setRules(player.getDuelRules(), ifFriendly ? 56 : 25, 0);
			setRules(player.getDuelRules(), ifFriendly ? 56 : 25, 0);
			player.getPackets().sendGlobalString(274, Colors.GREEN + target.getDisplayName());
			if (target != null)
				target.getPackets().sendGlobalString(274, Colors.GREEN + player.getDisplayName());
			refreshScreenMessage(true, ifFriendly);

			player.setCloseInterfacesEvent(new Runnable() {

				@Override
				public void run() {
					closeDuelInteraction(DuelStage.DECLINED);
				}
			});
		}
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		synchronized (this) {
			if (target == null || target.getControlerManager().getControler() == null || !(target.getControlerManager().getControler() instanceof DuelArena) || player.getControlerManager().getControler() == null
					|| !(player.getControlerManager().getControler() instanceof DuelArena))
				return false;
			synchronized (target.getControlerManager().getControler()) {
				DuelRules rules = player.getDuelRules();
				switch (interfaceId) {
				case 590:
					player.sendMessage("You didn't come here to dance.", true);
					return false;
				case 271:
				case 749:
					if (rules.getRule(5)) {
						if (interfaceId == 749 && componentId != 4)
							return true;
						player.getPackets().sendGameMessage("You can't use prayers in this duel.");
						return false;
					}
					return true;
				case 193:
				case 430:
				case 192:
					return !rules.getRule(2) || !isDueling;
					case 884:
					if (componentId == 4) {
						if (rules.getRule(9)) {
							player.getPackets().sendGameMessage("You can't use special attacks in this duel.");
							return false;
						}
					}
					return true;
				case 631:
					switch (componentId) {
					case 53: // Add money pouch
						/**player.sendInputInteger("Your money pouch contains " + Utils.getFormattedNumber(player.getMoneyPouch().getTotal()) + " coins.<br>" + "How many would you like to add?", new InputIntegerEvent() {
							@Override
							public void run(Player player) {
								Integer trade_X_money = 995;
								int value = getInteger();
								if (value <= 0 || trade_X_money == null || player.getMoneyPouch().getTotal() == 0) {
									return;
								}
								if (value >= player.getMoneyPouch().getTotal()) {
									value = player.getMoneyPouch().getTotal();
								}
								player.getTrade().addPouch(trade_X_money, value);
							}
						});*/
						player.sendMessage("This button is currently disabled.");
						return false;
					case 56: // no range
						setRules(rules, componentId, 0);
						return false;
					case 57: // no melee
						setRules(rules, componentId, 1);
						return false;
					case 58: // no magic
						setRules(rules, componentId, 2);
						return false;
					case 59: // fun wep
						setRules(rules, componentId, 8);
						return false;
					case 60: // no forfiet
						setRules(rules, componentId, 7);
						return false;
					case 61: // no drinks
						setRules(rules, componentId, 3);
						return false;
					case 62: // no food
						setRules(rules, componentId, 4);
						return false;
					case 63: // no prayer
						setRules(rules, componentId, 5);
						return false;
					case 64: // no movement
						setRules(rules, componentId, 25);
						if (rules.getRule(6)) {
							rules.setRule(6, false);
							player.sendMessage("You can't have movement without obstacles.");

						}
						return false;
					case 65: // obstacles
						setRules(rules, componentId, 6);
						if (rules.getRule(25)) {
							rules.setRule(25, false);
							player.sendMessage("You can't have obstacles without movement.");
						}
						return false;
					case 66: // enable summoning
						setRules(rules, componentId, 24);
						return false;
					case 67:// no spec
						setRules(rules, componentId, 9);
						return false;
					case 21:// no helm
						setRules(rules, componentId, 10);
						return false;
					case 22:// no cape
						setRules(rules, componentId, 11);
						return false;
					case 23:// no ammy
						setRules(rules, componentId, 12);
						return false;
					case 31:// arrows
						setRules(rules, componentId, 23);
						return false;
					case 24:// weapon
						setRules(rules, componentId, 13);
						return false;
					case 25:// body
						setRules(rules, componentId, 14);
						return false;
					case 26:// shield
						setRules(rules, componentId, 15);
						return false;
					case 27:// legs
						setRules(rules, componentId, 17);
						return false;
					case 28:// ring
						setRules(rules, componentId, 19);
						return false;
					case 29: // bots
						setRules(rules, componentId, 20);
						return false;
					case 30: // gloves
						setRules(rules, componentId, 22);
						return false;
					case 107:
						closeDuelInteraction(DuelStage.DECLINED);
						return false;
					case 46:
						accept(true);
						return false;
					case 47:
						switch (packetId) {
						case PacketRepository.ACTION_BUTTON1_PACKET:
							removeItem(slotId, 1);
							return false;
						case PacketRepository.ACTION_BUTTON2_PACKET:
							removeItem(slotId, 5);
							return false;
						case PacketRepository.ACTION_BUTTON3_PACKET:
							removeItem(slotId, 10);
							return false;
						case PacketRepository.ACTION_BUTTON4_PACKET:
							Item item = player.getInventory().getItems().get(slotId);
							if (item == null)
								return false;
							removeItem(slotId, player.getInventory().getItems().getNumberOf(item));
							return false;
						case PacketRepository.ACTION_BUTTON5_PACKET:
							player.getInventory().sendExamine(slotId);
							return false;
						}
						return false;
					}
				case 628:
					switch (packetId) {
					case PacketRepository.ACTION_BUTTON1_PACKET:
						addItem(slotId, 1);
						return false;
					case PacketRepository.ACTION_BUTTON2_PACKET:
						addItem(slotId, 5);
						return false;
					case PacketRepository.ACTION_BUTTON3_PACKET:
						addItem(slotId, 10);
						return false;
					case PacketRepository.ACTION_BUTTON4_PACKET:
						Item item = player.getInventory().getItems().get(slotId);
						if (item == null)
							return false;
						addItem(slotId, player.getInventory().getItems().getNumberOf(item));
						return false;
					case PacketRepository.ACTION_BUTTON5_PACKET:
						player.getInventory().sendExamine(slotId);
						return false;
					}
				case 626:
					switch (componentId) {
					case 43:
						accept(false);
						return false;
					}
				case 637: // friendly
					switch (componentId) {
					case 25: // no range
						setRules(rules, componentId, 0);
						return false;
					case 26: // no melee
						setRules(rules, componentId, 1);
						return false;
					case 27: // no magic
						setRules(rules, componentId, 2);
						return false;
					case 28: // fun wep
						setRules(rules, componentId, 8);
						return false;
					case 29: // no forfiet
						setRules(rules, componentId, 7);
						return false;
					case 30: // no drinks
						setRules(rules, componentId, 3);
						return false;
					case 31: // no food
						setRules(rules, componentId, 4);
						return false;
					case 32: // no prayer
						setRules(rules, componentId, 5);
						return false;
					case 33: // no movement
						setRules(rules, componentId, 25);
						if (rules.getRule(6)) {
							rules.setRule(6, false);
							player.getPackets().sendGameMessage("You can't have movement without obstacles.");
						}
						return false;
					case 34: // obstacles
						setRules(rules, componentId, 6);
						if (rules.getRule(25)) {
							rules.setRule(25, false);
							player.getPackets().sendGameMessage("You can't have obstacles without movement.");
						}
						return false;
					case 35: // enable summoning
						setRules(rules, componentId, 24);
						return false;
					case 36:// no spec
						setRules(rules, componentId, 9);
						return false;
					case 43:// no helm
						setRules(rules, componentId, 10);
						return false;
					case 44:// no cape
						setRules(rules, componentId, 11);
						return false;
					case 45:// no ammy
						setRules(rules, componentId, 12);
						return false;
					case 53:// arrows
						setRules(rules, componentId, 23);
						return false;
					case 46:// weapon
						setRules(rules, componentId, 13);
						return false;
					case 47:// body
						setRules(rules, componentId, 14);
						return false;
					case 48:// shield
						setRules(rules, componentId, 15);
						return false;
					case 49:// legs
						setRules(rules, componentId, 17);
						return false;
					case 50:// ring
						setRules(rules, componentId, 19);
						return false;
					case 51: // bots
						setRules(rules, componentId, 20);
						return false;
					case 52: // gloves
						setRules(rules, componentId, 22);
						return false;
					case 86:
						closeDuelInteraction(DuelStage.DECLINED);
						return false;
					case 21:
						accept(true);
						return false;
					}
				case 639:
					switch (componentId) {
					case 25:
						accept(false);
						return false;
					}
				}
			}
		}
		return true;
	}

	private void setRules(DuelRules rules, int componentId, int slotId) {
		sendRuleFlash(componentId, slotId);
		rules.setRules(slotId);
	}

	private void sendFlash(int slot) {
		player.getPackets().sendInterFlashScript(ifFriendly ? 637 : 631, 47, 3, 3, slot);
		target.getPackets().sendInterFlashScript(ifFriendly ? 637 : 631, 50, 3, 3, slot);
	}
	
	private static final int[] EQUIPMENT_COMPONENTS = {   };

	private void sendRuleFlash(int componentId, int slot) {
		player.getPackets().sendExecuteScript(4177, 3, 0, Utils.random(10));
		/*player.getPackets().sendInterFlashScript(ifFriendly ? 637 : 631, componentId, 11, 2, slot);
		target.getPackets().sendInterFlashScript(ifFriendly ? 637 : 631, componentId, -1, 5, slot);*/
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public void process() {
		if (!hasTarget() || target.getControlerManager().getControler() != null && !(target.getControlerManager().getControler() instanceof DuelArena)) {
			end(DUEL_END_LOSE);
			return;
		}
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getDefinitions().containsOption("Forfeit")) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You cannot forfeit this duel.");
			return false;
		}
		return false;
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(134, player.getDuelRules().getStake(), slots);
		target.getPackets().sendUpdateItems(134, true, player.getDuelRules().getStake().getItems(), slots);
	}

	private void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = player.getDuelRules().getStake().getItems()[index];
			if (item != null)
				if (itemsBefore[index] != item) {
					changedSlots[count++] = index;
					if (itemsBefore[index] != null && (item == null || item.getId() != itemsBefore[index].getId() || item.getAmount() < itemsBefore[index].getAmount()))
						sendFlash(index);
				}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	private void refreshScreenMessage(boolean firstStage, boolean ifFriendly) {
		player.getPackets().sendIComponentText(firstStage ? (ifFriendly ? 637 : 631) : (ifFriendly ? 639 : 626), firstStage ? (ifFriendly ? 20 : 41) : (ifFriendly ? 23 : 35), "<col=ff0000>" + getAcceptMessage(firstStage));
	}

	private void refreshScreenMessages(boolean firstStage, boolean ifFriendly) {
		refreshScreenMessage(firstStage, ifFriendly);
		if (!ifFriendly) {
			player.getPackets().sendIComponentText(626, 25, "");
			player.getPackets().sendIComponentText(626, 26, "");
		}
		if (target != null) {
			((DuelArena) target.getControlerManager().getControler()).refreshScreenMessage(firstStage, ifFriendly);
		}
	}

	private void removeEquipment() {
		int slot = 0;
		for (int i = 10; i < 23; i++) {
			if (i == 14) {
				if (player.getEquipment().hasTwoHandedWeapon() || isWearingTwoHandedWeapon() == true)
					ButtonHandler.sendRemove(target, 3);
			}
			if (player.getDuelRules().getRule(i)) {
				slot = i - 10;
				ButtonHandler.sendRemove(player, slot);
			}
		}
	}

	public void removeItem(final int slot, int amount) {
		if (!hasTarget())
			return;
		Item item = player.getDuelRules().getStake().get(slot);
		if (item == null)
			return;
		Item[] itemsBefore = player.getDuelRules().getStake().getItemsCopy();
		int maxAmount = player.getDuelRules().getStake().getNumberOf(item);
		if (amount < maxAmount)
			item = new Item(item.getId(), amount);
		else
			item = new Item(item.getId(), maxAmount);
		player.getDuelRules().getStake().remove(slot, item);
		player.getInventory().addItem(item);
		refreshItems(itemsBefore);
		refresh(slot);
		cancelAccepted();
	}

	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				player.stopAll();
				if (loop == 0) {
					player.setNextAnimation(player.getDeathAnimation());
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					end(DUEL_END_LOSE);
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	private void sendFinishInterface(Player player, Player loser) {
		player.getInterfaceManager().sendInterface(634);
		player.getPackets().sendIComponentText(634, 33, "" + loser.getDisplayName());
		player.getPackets().sendIComponentText(634, 32, "" + loser.getSkills().getCombatLevel());
		player.getPackets().sendIComponentText(634, 17, "Close");
		player.getPackets().sendInterSetItemsOptionsScript(634, 28, 136, 6, 4, "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(634, 28, 0, 35, 0, 1, 2, 3, 4, 5);
		if (loser.getDuelRules().getStake() != null)
			player.getPackets().sendItems(136, loser.getDuelRules().getStake());
	}

	private void sendOptions(Player player) {
		player.getInterfaceManager().sendInventoryInterface(628);
		player.getPackets().sendUnlockIComponentOptionSlots(628, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(628, 0, 93, 4, 7, "Stake 1", "Stake 5", "Stake 10", "Stake All", "Examine");

		player.getPackets().sendIComponentSettings(631, 47, 0, 27, 0x2 | 0x4 | 0x8 | 0x10 | 0x20 | 0x40);
	}

	@Override
	public void start() {
		this.target = (Player) getArguments()[0];
		ifFriendly = (boolean) getArguments()[1];
		openDuelScreen(target, ifFriendly);
	}

	private void startEndingTeleport(Player player, boolean loggedOut) {
		WorldTile tile = LOBBY_TELEPORTS[Utils.random(LOBBY_TELEPORTS.length)];
		WorldTile teleTile = tile;
		for (int trycount = 0; trycount < 10; trycount++) {
			teleTile = new WorldTile(tile, 2);
			if (World.isTileFree(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize()))
				break;
			teleTile = tile;
		}
		if (loggedOut) {
			player.setLocation(teleTile);
			return;
		}
		player.setNextWorldTile(teleTile);
	}

	enum DuelStage {
		DECLINED,
		NO_SPACE,
		SECOND,
		DONE
	}
}