package com.rs.game.player.quest.impl;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.quest.AbstractQuest;
import com.rs.utils.Utils;

import java.text.NumberFormat;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 2012-10-07
 */
@SuppressWarnings("serial")
public class DwarfCannon extends AbstractQuest<DwarfCannon.Stage, DwarfCannon.Nodes> {

	public DwarfCannon() {
		super("Dwarf Cannon", 2, Stage.START, Nodes.LAWGOF);
	}

	@Override
	public void handleQuest(Player player) {
		switch (getQuestStage()) {
		case FINISHED:
			player.getQuestManager().sendQuestInterface(this.getName(), "<str>THIS QUEST IS COMPLETE!",
					"<str>I HAVE ACCESS TO THE DWARF CANNON NOW!</str>");
			break;
		case FIXED_CANNON:
			player.getQuestManager().sendQuestInterface(this.getName(),
					"I have repaired the cannon! I should talk to the captain now", " to finish the quest.");
			break;
		case FIXING_CANNON:
			player.getQuestManager().sendQuestInterface(this.getName(),
					"I must fix his cannon by having a hammer and a", "toolkit in my inventory, then I should inspect",
					"the cannon. There are three available", "positions to repair the cannon from, and they change",
					"every time I click the cannon. Perhaps standing at one location",
					"the whole time and inspecting the cannon will do the trick...");
			break;
		case REPAIRED_RAILINGS:
			player.getQuestManager().sendQuestInterface(this.getName(), "I must talk to the captain and see what else",
					"he requires of me.");
			break;
		case REPAIRING_RAILINGS:
			player.getQuestManager().sendQuestInterface(this.getName(), "I must repair the railings around his base.");
			break;
		case START:
			player.getQuestManager().sendQuestInterface(this.getName(),
					"Captin Lawgof is located west of Camelot. He requires some help to repair some railings and",
					"fix his cannon! There is a lot of walking involved in this quest.", "", "Requirements:",
					"" + (player.getSkills().getLevel(Skills.SMITHING) >= 40 ? "<str>" : "") + "40 Smithing");
			break;
		}
	}

	@Override
	public void handleDialogue(Player player, int npcId) {
		switch (npcId) {
		case 208:
			player.getDialogueManager().startDialogue(new Dialogue() {

				private int npcId;

				@Override
				public void start() {
					npcId = 208;
					switch (getQuestStage()) {
					case FINISHED:
						sendOptionsDialogue("Choose an Option", "How are you, captain",
								"Can I buy another cannon? I've lost mine...");
						break;
					case FIXED_CANNON:
						sendNPCDialogue(npcId, CALM,
								"Thank you! You have saved the kingdom of Kandarin from the goblins! Now what would you like to receive as a reward?");
						break;
					case FIXING_CANNON:
						sendNPCDialogue(npcId, CALM,
								"Inspect my cannon while you have the toolkit and a hammer in your inventory onto my dwarf cannon to fix it.");
						break;
					case REPAIRED_RAILINGS:
						sendNPCDialogue(npcId, CALM,
								"In order to repair my cannon, you will need a toolkit and a hammer.");
						break;
					case REPAIRING_RAILINGS:
						sendNPCDialogue(npcId, CALM, "Do you need more railings?");
						break;
					case START:
						sendPlayerDialogue(CALM, "Hello.");
						break;
					default:
						break;
					}
				}

				@Override
				public void run(int interfaceId, int option) {
					// Item[] cannon = new Item[] { new Item(2, 30), new
					// Item(6), new Item(8), new Item(10), new Item(12) };
					switch (getQuestStage()) {
					case FINISHED:
						switch (stage) {
						case -1:
							switch (option) {
							case OPTION_1:
								sendPlayerDialogue(CALM, "How are you, captain?");
								stage = 2;
								break;
							case OPTION_2:
								sendNPCDialogue(npcId, CALM, "Would you like a dwarf cannon or a hand cannon?");
								stage = 0;
								break;
							}
							break;
						case 0:
							sendOptionsDialogue("Choose an Option", "A dwarf cannon, please.",
									"I would like a hand cannon.", "Could I buy hand cannon shots?");
							stage = 1;
							break;
						case 1:
							switch (option) {
							case OPTION_1:
								end();
								player.sendMessage("These are not available quite yet....");
								/*
								 * if (player.takeMoney(500000)) { for (Item
								 * item : cannon) {
								 * player.getInventory().addItem(item); }
								 * sendNPCDialogue(npcId, JUST_LISTEN,
								 * "Thank you for your business!"); stage = -2;
								 * } else { sendPlayerDialogue(SAD,
								 * "I forgot to bring 500,000 coins with me, I will be back soon."
								 * ); }
								 */
								break;
							case OPTION_2:
								if (player.getMoneyPouch().removeMoneyMisc(200000)) {
									player.getInventory().addItem(15241, 1);
									sendNPCDialogue(npcId, CALM, "Thank you for your business!");
									stage = -2;
								} else {
									sendPlayerDialogue(SAD,
											"I forgot to bring 200,000 coins with me, I will be back soon.");
								}
								break;
							case OPTION_3:
								sendOptionsDialogue("Choose an Option", "50 shots, please.", "100 shots, please",
										"500 shots, please", "1000 shots, please");
								stage = 3;
								break;
							}
							break;
						case 2:
							sendNPCDialogue(npcId, CALM, "I am doing fine. I am very busy right now.");
							stage = -2;
							break;
						case 3:
							switch (option) {
							case OPTION_1:
								player.getTemporaryAttributtes().put("buying_shots", new int[] { 50, 50 * 1500 });
								sendNPCDialogue(npcId, CALM, "Very well, that will be "
										+ ((Integer) (player.getTemporaryAttributtes().get("buying_shots")) * 1500)
										+ " coins.");
								stage = 4;
								break;
							case OPTION_2:
								player.getTemporaryAttributtes().put("buying_shots", new int[] { 100, 100 * 1500 });
								sendNPCDialogue(npcId, CALM, "Very well, that will be "
										+ ((Integer) (player.getTemporaryAttributtes().get("buying_shots")) * 1500)
										+ " coins.");
								stage = 4;
								break;
							case OPTION_3:
								player.getTemporaryAttributtes().put("buying_shots", new int[] { 500, 500 * 1500 });
								sendNPCDialogue(npcId, CALM, "Very well, that will be "
										+ ((Integer) (player.getTemporaryAttributtes().get("buying_shots")) * 1500)
										+ " coins.");
								stage = 4;
								break;
							case OPTION_4:
								player.getTemporaryAttributtes().put("buying_shots", new int[] { 1000, 1000 * 1500 });
								sendNPCDialogue(npcId, CALM, "Very well, that will be "
										+ ((Integer) (player.getTemporaryAttributtes().get("buying_shots")) * 1500)
										+ " coins.");
								stage = 4;
								break;
							}
							break;
						case 4:
							int[] array = (int[]) player.getTemporaryAttributtes().get("buying_shots");
							if (player.getMoneyPouch().removeMoneyMisc(array[1])) {
								player.getInventory().addItem(15243, array[0]);
								sendPlayerDialogue(NORMAL, "Thanks!");
								stage = -2;
							} else {
								sendPlayerDialogue(SAD,
										"I forgot to bring " + NumberFormat.getInstance().format(array[1])
												+ " coins with me, I will be back soon.");
								stage = -2;
							}
							break;
						default:
							end();
							break;
						}
						break;
					case FIXED_CANNON:
						switch (stage) {
						case -1:
							sendOptionsDialogue("Choose an Option", "A dwarf cannon with 30 cannonballs.",
									"A hand cannon and 50 shots.");
							stage = 0;
							break;
						case 0:
							switch (option) {
							case OPTION_1:
								end();
								player.sendMessage("These are not available quite yet....");
								break;
							case OPTION_2:
								player.getInventory().addItem(15241, 1);
								player.getInventory().addItem(15243, 50);
								sendNPCDialogue(npcId, CALM,
										"There you go. You may also come back to me to purchase a new cannon whenever you will.");
								setQuestStage(Stage.FINISHED);
								player.getQuestManager().refreshQuests();
								stage = -2;
								break;
							}
							break;
						default:
							end();
							break;
						}
						break;
					case FIXING_CANNON:
						switch (stage) {
						case -1:
							sendOptionsDialogue("Choose an Option", "I have already done that.", "Okay.");
							stage = 0;
							break;
						case 0:
							switch (option) {
							case OPTION_1:
								break;
							case OPTION_2:
								sendPlayerDialogue(CALM, "Okay.");
								stage = -2;
								break;
							}
							break;
						default:
							end();
							break;
						}
						break;
					case REPAIRED_RAILINGS:
						switch (stage) {
						case -1:
							sendPlayerDialogue(CALM, "Where would I get those from?");
							stage = 0;
							break;
						case 0:
							player.getInventory().addItem(1, 1);
							sendNPCDialogue(npcId, CALM,
									"I have given you a toolkit. You must get a hammer from a shop or a player in the world");
							setQuestStage(Stage.FIXING_CANNON);
							break;
						}
						break;
					case REPAIRING_RAILINGS:
						switch (stage) {
						case -1:
							sendOptionsDialogue("Choose an Option", "Yes, please!", "No thank; I'm done!.");
							stage = 0;
							break;
						case 0:
							switch (option) {
							case OPTION_1:
								player.getInventory().addItem(14, 6);
								end();
								break;
							case OPTION_2:
								if (player.getTemporaryAttributtes().get("dwarf_railings_fixed") != null
										&& ((Integer) player.getTemporaryAttributtes()
												.get("dwarf_railings_fixed")) == 6) {
									setQuestStage(Stage.REPAIRED_RAILINGS);
									sendNPCDialogue(npcId, CALM,
											"It seems like you have! Now I need you to repair my dwarf cannon.");
								} else {
									sendNPCDialogue(npcId, SAD, "No you have not, you still have some to complete.");
									end();
								}
								break;
							}
							break;
						}
						break;
					case START:
						switch (stage) {
						case -1:
							if (player.getSkills().getLevel(Skills.SMITHING) >= 40) {
								sendNPCDialogue(npcId, CALM,
										"Guthix be praised, the calvary has arrived! Hero, how would you like to be made a honorary member of the Black Guard?");
								stage = 0;
							} else {
								sendNPCDialogue(npcId, SAD, "Come back to me when you have a smithing level of 40.");
							}
							break;
						case 0:
							sendPlayerDialogue(NORMAL, "The Black Guard, what's that?");
							stage = 1;
							break;
						case 1:
							sendNPCDialogue(npcId, CALM,
									"Hawhaw! 'What's that' he asks, what a sense of humour! The Black Guard is the finest regiment in the dwarven army. Only the best of the best are allowed to join it and then they receive months of rigorous training.");
							stage = 2;
							break;
						case 2:
							sendNPCDialogue(npcId, CALM,
									"However, we are currently in need of a hero, so for a limited time only I'm offering you, a human, a chance to join this prestigious regiment. What do you say?");
							stage = 3;
							break;
						case 3:
							sendOptionsDialogue("Choose an Option", "I'm sorry, I'm too busy mining.",
									"Sure, I'd be honoured to join.");
							stage = 4;
							break;
						case 4:
							switch (option) {
							case OPTION_1:
								end();
								break;
							case OPTION_2:
								sendPlayerDialogue(NORMAL, "Sure, I'd be honoured to join.");
								stage = 5;
								break;
							}
							break;
						case 5:
							sendNPCDialogue(npcId, CALM,
									"That's the spirit! Now trooper, we have no time to waste; the goblins are attacking from the forests to the South. There are so many of them, they are overwhelming my men and breaking through our");
							stage = 6;
							break;
						case 6:
							sendNPCDialogue(npcId, CALM,
									"perimeter defences, could you please try to fix the stochade by replacing the broken rails with these new ones?");
							stage = 7;
							break;
						case 7:
							sendPlayerDialogue(CALM, "Sure, sounds easy enough...");
							stage = 8;
							break;
						case 8:
							sendDialogue("The Dwarf Captain gives you his railings.");
							stage = 9;
							break;
						case 9:
							sendNPCDialogue(npcId, CALM, "Report back to me once you've fixed the railings.");
							setQuestStage(Stage.REPAIRING_RAILINGS);
							player.getInventory().addItem(14, 6);
							stage = -2;
							break;
						default:
							end();
							break;
						}
						break;
					default:
						break;

					}
				}

				@Override
				public void finish() {

				}

			});
			break;
		}
	}

	@Override
	public void handleObjectClick(final Player player, WorldObject object, boolean firstClick, boolean secondClick,
			boolean thirdClick) {
		switch (object.getId()) {
		case 15601:
			if (player.getInventory().containsItem(14, 1) && getQuestStage().equals(Stage.REPAIRING_RAILINGS)) {
				if (Utils.random(2) != 2) {
					player.getTemporaryAttributtes().put("dwarf_railings_fixed",
							(player.getTemporaryAttributtes().get("dwarf_railings_fixed") == null ? 1
									: ((Integer) (player.getTemporaryAttributtes().get("dwarf_railings_fixed")) + 1)));
					player.sendMessage("You have fixed " + player.getTemporaryAttributtes().get("dwarf_railings_fixed")
							+ " railings.");
					player.getInventory().deleteItem(14, 1);
				} else {
					player.applyHit(new Hit(player, 10, HitLook.REGULAR_DAMAGE));
				}
			} else {
				player.sendMessage("You need 6 railings to fix this.");
			}
			break;
		case 15597:
			if (player.getInventory().containsItem(1, 1) && player.getInventory().containsItem(2347, 1)
					&& getQuestStage().equals(Stage.FIXING_CANNON)) {
				WorldTile[] tiles = new WorldTile[] { new WorldTile(2564, 3460, 0), new WorldTile(2561, 3461, 0),
						new WorldTile(2562, 3464, 0) };
				int random = Utils.random(tiles.length);
				WorldTile randomTile = tiles[random];
				if (player.getLastWorldTile().getX() == randomTile.getX()
						&& player.getLastWorldTile().getY() == randomTile.getY()) {
					player.getInventory().deleteItem(1, 1);
					setQuestStage(Stage.FIXED_CANNON);
					player.sendMessage("You have fixed the cannon! Talk to the captain to complete the quest.");
				} else {
					player.sendMessage(
							"You are not standing at the correct location! The position changes every time, and there are three possible locations.");
					player.getPackets().sendObjectAnimation(object,
							new Animation(random == 0 ? 305 : random == 1 ? 289 : 182));
				}
			} else {
				player.sendMessage("Bring a toolkit and a hammer to fix the cannon.");
			}
			break;
		}
	}

	@Override
	public boolean hasDialogue() {
		return true;
	}

	@Override
	public boolean hasObjectClick() {
		return false;
	}

	@Override
	public boolean hasInteracted(Nodes node) {
		return false;
	}

	/**
	 * The current nodes of the quest.
	 *
	 * @author Thomas Le Godais <thomaslegodais@live.com>
	 */
	public enum Nodes {
		/*
		 * The chef node.
		 */
		LAWGOF(false);

		/**
		 * The interaction value.
		 **/
		private boolean value;

		/**
		 * Constructs a new Nodes instance.
		 *
		 * @param value
		 *            the value.
		 */
		Nodes(boolean value) {
			this.value = value;
		}

		/**
		 * Gets the value of the node.
		 *
		 * @return the value.
		 */
		public boolean getValue() {
			return value;
		}

		/**
		 * Sets the value of the node.
		 *
		 * @param newValue
		 *            the new value to set.
		 */
		public void setValue(boolean newValue) {
			this.value = newValue;
		}
	}

	public enum Stage {

		START,

		REPAIRING_RAILINGS,

		REPAIRED_RAILINGS,

		FIXING_CANNON,

		FIXED_CANNON,

		FINISHED
	}

}
