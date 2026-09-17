package com.rs.game.activities.rots;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import java.util.concurrent.TimeUnit;

/**
 * RoTs controller for Ataraxia-use only!
 * 		@author Noele
 * Modified by Kris
 */
public class RiseOfTheSixController extends Controller {

	private static final WorldTile ENTRANCE = new WorldTile(3540, 3311, 0);
	private static final WorldTile GRAVEYARD = new WorldTile(2391, 6062, 1);
	private transient RiseOfTheSix instance;
	
	private ItemsContainer<Item> rewards;
	
	public RiseOfTheSixMap getMap() {
		return instance.getMap();
	}
	
	public RiseOfTheSix getInstance() {
		return instance;
	}
	
	@Override
	public boolean canSummonFamiliar() {
		return false;
	}
	
    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.sendMessage("The only way out is victory or death!");
        return false;
    }
	
	public void leave() {
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void start() {
				sendItemDialogue(30004, 1, "Are you positive you wish to leave? If the instance has no more players in it, it will be destroyed! You will not receive the totem used to create the instance back.");
			}

			@Override
			public void run(int interfaceId, int componentId) {
				if (stage == -1) {
					sendOptionsDialogue("Leave the instance?", "Yes, leave.", "No, stay.");
				} else if (stage == 0) {
					if (componentId == OPTION_1) {
						player.lock();
						player.getReceivedDamage().clear();
						FadingScreen.fade(player, 6, new Runnable() {
							@Override
							public void run() {
								player.unlock();
								instance.removePlayer(player);
								player.getControlerManager().forceStop();
								player.setNextWorldTile(ENTRANCE);
								player.setNextFaceWorldTile(new WorldTile(3541, 3311, 0));
								player.setNextAnimation(new Animation(21922));
							}
						});
					}
					end();
				}
				stage++;
			}

			@Override
			public void finish() {}
		});
	}
	
	@Override
	public void start() {
		if (player.getTemporaryAttributtes().get("rotsinstance") != null)
			instance = (RiseOfTheSix) player.getTemporaryAttributtes().remove("rotsinstance");
		else
			instance = new RiseOfTheSix((String) player.getTemporaryAttributtes().remove("rotspassword"));
		instance.addPlayer(player);
		player.setNextAnimation(new Animation(21924));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				FadingScreen.fade(player, 6, new Runnable() {
					@Override
					public void run() {
						player.unlock();
						player.setNextAnimation(new Animation(-1));
						final WorldTile rope = instance.getMap().getTile("rope");
						if (player.getFamiliar() != null)
							player.getFamiliar().sendDeath(null);
						player.setNextWorldTile(new WorldTile(rope.getX() + 1, rope.getY(), rope.getPlane()));
					}
				});
			}
		}, 2);
	}

	@Override
	public boolean logout() {
		player.setLocation(new WorldTile(3540, 3308, 0));
		if (instance != null)
			instance.getPlayers().remove(player);
		removeControler();
		return true;
	}
	
	@Override
	public boolean sendDeath() {
		if (getInstance() != null)
			getInstance().removePlayer(player);
		if (player.isGroupIronman()) {
			player.gimTracker.incrementDeaths("<#player> died while doing Rise of the Six");
	}
		player.lock(7);
		player.stopAll();
		player.getControlerManager().forceStop();
		player.getInterfaceManager().closeOverlay(false);
		player.getPackets().sendStopCameraShake();
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					World.sendGraphics(player, new Graphics(4399), new WorldTile(player));
					player.setNextWorldTile(new WorldTile(GRAVEYARD, 5));
					player.reset();
					int amount = player.getInventory().getAmountOf(30026);
					if (amount != 0) {
						player.getInventory().deleteItem(new Item(30026, amount));
						player.sendMessage("The malevolent energy becomes too unstable to keep.");
					}
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}
	
	@Override
	public boolean processNPCClick1(NPC npc) {
		if (player.isROTSLocked())
			return false;
		if (npc.getId() == 18552 && !npc.isLocked() && !npc.hasFinished()) {
			player.lock();
			npc.setLocked(true);
			WorldTasksManager.schedule(new WorldTask() {
				private int ticks;
				@Override
				public void run() {
					if (ticks == 0) 
						player.setNextAnimation(new Animation(21917));
					 else if (ticks == 1) {
						WorldTile toLocation = npc.getX() < instance.getWorldTile(35, 19).getX() ? instance.getWorldTile(42, 20) : instance.getWorldTile(25, 20);
						player.setNextWorldTile(new WorldTile(toLocation, 5));
						player.setNextAnimation(new Animation(21914));
						instance.incrementPortalDashTeleports();
						player.applyHit(new Hit(null, instance.getPortalDashTeleports() * 50, HitLook.REGULAR_DAMAGE));
						player.unlock();
						npc.finish();
						npc.setLocked(false);
						stop();
						return;
					}
					ticks++;
				}
			}, 0, 0);
		}
		return false;
	}
	
	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (instance == null)
			return true;
		if (player.isROTSLocked())
			return false;
		switch (object.getId()) {
		case 87994:
			player.getDialogueManager().startDialogue("RiseOfTheSixDisruptionD");
			break;
		case 87998: // leave
			if (instance.initiatedFight()) {
				player.getInterfaceManager().closeOverlay(false);
				player.getPackets().sendStopCameraShake();
				player.applyHit(new Hit(null, player.getMaxHitpoints() - player.getHitpoints(), HitLook.HEALED_DAMAGE));
				int amount = player.getInventory().getAmountOf(30026);
				if (amount != 0) {
					player.getInventory().deleteItem(new Item(30026, amount));
					player.getInventory().addItem(new Item(30027, amount));
					player.sendMessage("The malevolent energy stabilises as you leave the cavern.");
				}
				instance.removePlayer(player);
				player.setNextWorldTile(ENTRANCE);
				player.setNextFaceWorldTile(new WorldTile(3541, 3311, 0));
				player.setNextAnimation(new Animation(21922));
				player.getControlerManager().forceStop();
			} else
				leave();
			return false;
		case 88001: // enter boss
			if (object.getRotation() == 2) {
				if (object.getY() > player.getY())
					player.addWalkSteps(player.getX(), object.getY() + 1, 2, false);
				else {
					player.sendMessage("The aura of the arena forbids you to leave it.");
					return true;
				}
			} else if (object.getRotation() == 3) {
				if (object.getX() < player.getX()) {
					player.sendMessage("The aura of the arena forbids you to leave it.");
					return true;
				} else
					player.addWalkSteps(object.getX() + 1, player.getY(), 2, false);
			}
			break;
		case 87996:
			if (player.hasLootedROTS)
				takeLoot();
			else
				player.getDialogueManager().startDialogue("RiseOfTheSixLootingD");
			break;
		case 88091:
			player.lock(7);
			player.setNextAnimation(new Animation(15461));
			player.setNextFaceWorldTile(new WorldTile(player.getX() < object.getX() ? player.getX() + 6 : player.getX() - 6, player.getY(), player.getPlane()));
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
				@Override
				public void run() {
					player.setNextAnimation(new Animation(-1));
					player.setNextWorldTile(new WorldTile(player.getX() < object.getX() ? player.getX() + 4 : player.getX() - 4, player.getY(), player.getPlane()));
				}
			}, 3, TimeUnit.SECONDS);
			break;
		}
		return true;
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
		if (s.toLowerCase().contains("staffmenu"))
			return true;
		if (s.contains("answer"))
			return true;
		if (s.contains("kick"))
			return true;
		if (s.contains("fkick"))
			return true;
		return s.contains("forcekick");
	}
	
	public final void takeLoot() {
		if (rewards == null) 
			rewards = RiseOfTheSixReward.getRewards(player);
		else if (rewards.getUsedSlots() == 0) {
			player.sendMessage("The chest is empty!");
			return;
		}
		player.increaseKillStatistics("rise of the six", true);
		ContractHandler.updateNonNpcContract(player, 100001);
		HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.BARROWS_RISE_OF_THE_SIX);
		sendRewards();
	}
	
	private final void sendRewards() {
		player.getInterfaceManager().sendInterface(1284);
		player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 3, "Take", "Bank", "Discard", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 10, 0, 1, 2, 3);
		if (rewards != null)
			player.getPackets().sendItems(99, rewards);
	}
	
	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 1284) {
			if (rewards == null)
				return false;
			if (componentId == 7) {
				if (rewards.getSize() < slotId + 1)
					return false;
				if (rewards.get(slotId) == null)
					return false;
				if (rewards.get(slotId).getId() != slotId2)
					return false;
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					if (!rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= rewards.get(slotId).getAmount() || rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().containsItem(rewards.get(slotId)) || rewards.get(slotId).getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(rewards.get(slotId));
						rewards.remove(slotId, rewards.get(slotId));
						rewards.shift();
						player.getPackets().sendItems(99, rewards);
					} else 
						player.sendMessage("You don't have enough free space in inventory to hold any more items.");
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					if (rewards.get(slotId) != null && rewards.get(slotId).getId() == 30026) {
						player.sendMessage("You cannot bank this item.");
						return false;
					}
					player.getBank().addItem(rewards.get(slotId), true);
					rewards.remove(slotId, rewards.get(slotId));
					rewards.shift();
					player.getPackets().sendItems(99, rewards);
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					rewards.remove(slotId, rewards.get(slotId));
					rewards.shift();
					player.getPackets().sendItems(99, rewards);
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					player.sendMessage(ItemExaminesDataParser.getExamine(rewards.get(slotId)));
					player.sendMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(new Item(slotId2))) + ".");
				}
			} else if (componentId == 8) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				if (rewards.getUsedSlots() == 1 && rewards.containsOne(new Item(30026, 1))) {
					player.sendMessage("Malevolent energies cannot be sent to your bank.");
					return false;
				}
				for (Item items : rewards.getItems()) {
					if (items == null)
						continue;
					if (items.getId() == 30026)
						continue;
					player.getBank().addItem(items, true);
				}
				for (int i = 0; i < rewards.getSize(); i++)
					if (rewards.get(i) != null && rewards.get(i).getId() != 30026)
						rewards.remove(rewards.get(i));
				rewards.shift();

				if (rewards.containsOne(new Item(30026, 1)))
					player.sendMessage("All the rewards except for malevolent energies were sent to your bank.");
				else
					player.sendMessage("All the rewards were sent to your bank.");
				player.getPackets().sendItems(99, rewards);
				player.getBank().refreshItems();
			} else if (componentId == 10) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				boolean outOfSpace = false;
				for (Item items : rewards.getItems()) {
					if (items == null)
						continue;
					if (!items.getDefinitions().isStackable() && player.getInventory().getFreeSlots() >= items.getAmount() || items.getDefinitions().isStackable() && player.getInventory().containsItem(items) || items.getDefinitions().isStackable() && player.getInventory().hasFreeSlots()) {
						player.getInventory().addItem(items);
						rewards.remove(items);
					} else {
						outOfSpace = true;
						continue;
					}
				}
				if (outOfSpace)
					player.sendMessage("You don't have enough free space in inventory to hold any more items.");
				rewards.shift();
				player.getPackets().sendItems(99, rewards);
			} else if (componentId == 9) {
				if (rewards.freeSlots() == rewards.getSize()) {
					player.sendMessage("There's nothing left in the chest.");
					return false;
				}
				rewards.clear();
				player.getPackets().sendItems(99, rewards);
			}
		}
		return true;
	}

}
