package com.rs.game.player;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DialogueOptionEvent;
import com.rs.utils.FileLogger;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Dec 18, 2018.
 */
public class DicingManager implements Serializable {

	private static final String[] phrases = new String[] { "The best throw of the dice is to throw them away", "Viva Las Vegas? More like Viva Lost Wages!", "The quickest way to double your money is to fold it in half and put it back in your pocket.", "It's alright to root for the underdog, but just don't bet on him.", "If you can't tell who the sucker is, it's you.", "The many must lose in order that the few may win.", "It's hard to walk away from a winning streak" };
	private static final int[] emotes = new int[] { 862, 864, 861, 866, 857, 859, 860 };

	/**
	 * 
	 */
	private static final long serialVersionUID = 8924443958237183827L;

	private static final FileLogger fileLogger = new FileLogger("dicing_logs.txt");
	private final Map<String, Item> dicings;
	private transient Player player;

	/**
	 * Constructs a new class.
	 */
	public DicingManager() {
		dicings = new HashMap<String, Item>();
	}

	public void addDicing(NPC npc) {
		player.getTemporaryAttributtes().put("ADDING_DICE", true);
		
		player.sendInputName("Who would you like to roll against?", new InputNameEvent() {

			@Override
			public void run(Player player) {
				String name = getString();
				if (name == null) {
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}

				if (player.getCurrentFriendChat() == null) {
					player.sendMessage("You need to be in a Friends chat channel to use this option.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}

				Player other = World.getPlayerByDisplayName(name);
				if (other == null || !other.isRunning()) {
					player.sendMessage("That player is currently offline, or has privacy mode enabled.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}
				
				if (other == player) {
					player.sendMessage("You can't dice against yourself.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}

				if (!player.withinDistance(npc, 10)) {
					player.sendMessage("You are too far away from Hops.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}

				if (!other.withinDistance(npc, 10)) {
					player.sendMessage("The other player is too far away from Hops.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}

				if(!player.isOwner()) {
					if (player.getCurrentFriendChat() == null || !player.getCurrentFriendChatOwner().equals("gambling")) {
						player.sendMessage("You need to be in the 'Gambling' Friends chat to roll a dice.");
						player.getTemporaryAttributtes().remove("ADDING_DICE");
						return;
					}

					if (other.getCurrentFriendChat() == null || !other.getCurrentFriendChatOwner().equals("gambling")) {
						player.sendMessage("The other player needs to be in the 'Gambling' Friends chat.");
						player.getTemporaryAttributtes().remove("ADDING_DICE");
						return;
					}
				}

				if (dicings.containsKey(name)) {
					player.sendMessage("You already have an active dicing for that player.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}
				
				if (other.getTemporaryAttributtes().get("ADDING_DICE") != null) {
					player.sendMessage("The other player is too busy adding creating a new dice activity.");
					player.getTemporaryAttributtes().remove("ADDING_DICE");
					return;
				}
				
				player.sendInputInteger("How many shards to place? " + (other.dicingManager.getDicings().containsKey(player.getUsername()) ? other.getDisplayName() + " placed a bet of " + Utils.formatNumber(other.dicingManager.getDicings().get(player.getUsername()).getAmount()) + " shards." : ""), new InputIntegerEvent() {

					@Override
					public void run(Player player) {
						if (other == null || !other.isRunning()) {
							player.sendMessage("That player is currently offline, or has privacy mode enabled.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}
						if (other == player) {
							player.sendMessage("You can't dice against yourself.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}
						
						if (!player.withinDistance(npc, 10)) {
							player.sendMessage("You are too far away from Hops.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}

						if (!other.withinDistance(npc, 10)) {
							player.sendMessage("The other player is too far away from Hops.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}

						if(!player.isOwner()) {
							if (player.getCurrentFriendChat() == null || !player.getCurrentFriendChatOwner().equals("gambling")) {
								player.sendMessage("You need to be in the 'Gambling' Friends chat to roll a dice.");
								player.getTemporaryAttributtes().remove("ADDING_DICE");
								return;
							}

							if (other.getCurrentFriendChat() == null || !other.getCurrentFriendChatOwner().equals("gambling")) {
								player.sendMessage("The other player needs to be in the 'Gambling' Friends chat.");
								player.getTemporaryAttributtes().remove("ADDING_DICE");
								return;
							}
						}

						if (other.getTemporaryAttributtes().get("ADDING_DICE") != null) {
							player.sendMessage("The other player is too busy adding creating a new dice activity.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}

						int amount = getInteger();
						
						if (amount < 40000) {
							player.sendMessage("Your dice offer has to be a minimum of 40,000 spirit shards.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}

						if (amount + player.getTotalItemCount(12183) > Integer.MAX_VALUE) {
							player.sendMessage("Your shard amount goes over the limit of " + NumberFormat.getInstance(Locale.US).format(Integer.MAX_VALUE) + " when added together with all your shards.");
						}

						if (other.dicingManager.getDicings().containsKey(player.getUsername())) {
							if (amount != other.dicingManager.getDicings().get(player.getUsername()).getAmount()) {
								player.sendMessage("Your dice offer has to match your opponents. You are asked to input the amount for confirmation purposes.");
								player.getTemporaryAttributtes().remove("ADDING_DICE");
								return;
							}
						}

						if (other.dicingManager.getDicings().containsKey(player.getUsername())) {
							int amount2 = other.dicingManager.getDicings().get(player.getUsername()).getAmount();
							if (amount < amount2) {
								player.sendMessage("Your dice offer has to be atleast what the other player offered");
								player.getTemporaryAttributtes().remove("ADDING_DICE");
								return;
							}
						}

						if (!player.getInventory().containsItem(12183, amount)) {
							player.sendMessage("You do not have enough spirit shards in your inventory to cover that amount.");
							player.getTemporaryAttributtes().remove("ADDING_DICE");
							return;
						}
						
						player.sendOptionsDialogue("Confirm amount? " + Utils.formatNumber(amount), new String[] { "Yes.", "No."}, new DialogueOptionEvent() {
							
							@Override
							public void run(Player player) {
								if (getOption() == OPTION_1) {
									player.getTemporaryAttributtes().remove("ADDING_DICE");

									if (player.getInventory().containsItem(12183, amount)) {
										player.getInventory().deleteItem(12183, amount);// TODO add gambling items
										dicings.put(other.getUsername(), new Item(12183, amount));
										checkDicing(npc, other, dicings.get(name));
									}
								} else {
									player.getTemporaryAttributtes().remove("ADDING_DICE");
								}
							}
						});
					}
				});
				
				player.getTemporaryAttributtes().remove("ADDING_DICE");
			}
		});
		player.getTemporaryAttributtes().remove("ADDING_DICE");
	}

	public void openCurrentActiveDicings() {
		resetActiveDicingsInterface();

		int index = 13;
		player.getPackets().sendIComponentText(1245, 330, "Active Dices (Up To 10)");
		for (Entry<String, Item> dice : dicings.entrySet()) {
			if (dice == null)
				continue;

			if (index >= 23)
				break;

			player.getPackets().sendIComponentText(1245, index++, Utils.formatPlayerNameForDisplay(dice.getKey()) + " at " + Utils.formatNumber(dice.getValue().getAmount()));
		}

		player.getInterfaceManager().sendInterface(1245);
	}

	private void resetActiveDicingsInterface() {
		for (int i = 13; i <= 23; i++)
			player.getPackets().sendIComponentText(1245, i, "");
	}

	public void clearActiveDicings() {
		if (dicings.isEmpty()) {
			player.sendMessage("You have no active dices to return back to you.", true);
			return;
		}
		
		for (Entry<String, Item> dice : dicings.entrySet()) {
			if (dice == null)
				continue;

			player.getInventory().addItemMoneyPouch(dicings.remove(dice.getKey()));
		}
		dicings.clear();
		player.sendMessage("All of your active dices have been cleared and returned back to you.", true);
	}

	private void checkDicing(NPC npc, Player other, Item item) {
		if (other == null || !other.isRunning()) {
			player.sendMessage("That player is currently offline, or has privacy mode enabled.");
			return;
		}

		if (other.dicingManager.getDicings().containsKey(player.getUsername())) {
			player.lock();
			other.lock();

			npc.setNextForceTalk(new ForceTalk(phrases[Utils.random(phrases.length)]));
			player.setNextAnimation(new Animation(11900));
			player.setNextGraphics(new Graphics(2075));
			other.setNextAnimation(new Animation(11900));
			other.setNextGraphics(new Graphics(2075));
			player.sendMessage("Rolling the dice...");
			other.sendMessage("Rolling the dice...");

			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					npc.setNextAnimation(new Animation(emotes[Utils.random(emotes.length)]));
					int roll1 = Utils.random(1, 100);// this player
					int roll2 = Utils.random(1, 100);// that player
					while (roll1 == roll2) {
						roll1 = Utils.random(1, 100);// this player
						roll2 = Utils.random(1, 100);
					}

					Player winner = (Math.max(roll1, roll2) == roll1 ? player : other);
					player.getCurrentFriendChat().sendDiceMessage(player, player.getDisplayName() + " rolled a <col=db3535>" + roll1 + "</col> and " + other.getDisplayName() + " rolled a <col=db3535>" + roll2 + "</col>. The win goes to " + winner.getDisplayName() + ".");
					player.getCurrentFriendChat().sendMessage(player, player.getDisplayName() + " rolled a " + roll1 + " and " + other.getDisplayName() + " rolled a " + roll2 + ". The win belong to " + winner.getDisplayName() + ".");

					int rewardAmount = (player.dicingManager.getDicings().remove(other.getUsername()).getAmount() + other.dicingManager.getDicings().remove(player.getUsername()).getAmount());

					Item reward = new Item(12183, rewardAmount);
					winner.sendMessage("Congratulations on your winnings! " + Utils.formatNumber(reward.getAmount()) + " spirit shards have been sent directly to you!", true);
					winner.addItem(new Item(reward));
					
					player.unlock();
					other.unlock();

					fileLogger.logMessage(player.getUsername() + ", " + roll1 + ", " + other.getUsername() + ", " + roll2 + ", Winner: " + winner.getUsername() + ", Reward: " + item.getId() + ", " + item.getAmount());
				}

			}, 6);
		}
	}

	private String now(final String dateFormat) {
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	public Map<String, Item> getDicings() {
		return dicings;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}
