package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;

/**
 * Used to handle Fancy-dress shop owner's Custom fur clothing.
 *
 * @author Noel
 */
public class CustomFurClothing {

	private static final Item[] ITEMS = new Item[] { new Item(10065), new Item(10067), new Item(10053), new Item(10055),
			new Item(10057), new Item(10059), new Item(10061), new Item(10063), new Item(10075), new Item(10069),
			new Item(10045), new Item(10043), new Item(10041), new Item(10051), new Item(10049), new Item(10047),
			new Item(10039), new Item(10037), new Item(10035), new Item(10071) };

	private final static ItemsContainer<Item> rewards = new ItemsContainer<>(21, false);

	private static void prepareInterface() {
		rewards.addAll(ITEMS);
	}

	public static void openInterface(Player player) {
		prepareInterface();
		player.getInterfaceManager().sendInterface(477);
		player.getPackets().sendItems(482, rewards);
		player.getPackets().sendUnlockIComponentOptionSlots(477, 1, 0, 30, 0,1,2);
	}

	/**
	 * Represents fur clothing.
	 *
	 * @author Noel
	 */
	public enum FurClothing {
		POLAR(new Item[][] { { new Item(10117, 2), new Item(10065), new Item(995, 20) },
				{ new Item(10117, 2), new Item(10067), new Item(995, 20) } }, 0,
				1), COMMON(
						new Item[][] { { new Item(10121, 2), new Item(10053), new Item(995, 20) },
								{ new Item(10121, 2), new Item(10055), new Item(995, 20) } },
						2, 3), FELDIP(
								new Item[][] { { new Item(10119, 2), new Item(10057), new Item(995, 20) },
										{ new Item(10119, 2), new Item(10059), new Item(995, 20) } },
								4, 5), DESERT(
										new Item[][] { { new Item(10123, 2), new Item(10061), new Item(995, 20) },
												{ new Item(10123, 2), new Item(10063), new Item(995, 20) } },
										6, 7), LARUPIA(
												new Item[][] { { new Item(10095), new Item(10045), new Item(995, 500) },
														{ new Item(10095), new Item(10043), new Item(995, 100) },
														{ new Item(10095), new Item(10041), new Item(995, 100) } },
												10, 11,
												12), GRAAHK(new Item[][] {
														{ new Item(10099), new Item(10051), new Item(995, 750) },
														{ new Item(10099), new Item(10049), new Item(995, 150) },
														{ new Item(10099), new Item(10047), new Item(995, 150) } }, 13,
														14, 15), KYATT(
																new Item[][] {
																		{ new Item(10103), new Item(10039),
																				new Item(995, 1000) },
																		{ new Item(10103), new Item(10037),
																				new Item(995, 250) },
																		{ new Item(10103), new Item(10035),
																				new Item(995, 250) } },
																16, 17, 18), DARK_KEBBIT(
																		new Item[][] { { new Item(10115, 2),
																				new Item(10075), new Item(995, 600) } },
																		8), SPOTTED_KEBBIT(
																				new Item[][] { { new Item(10125, 2),
																						new Item(10069),
																						new Item(995, 400) } },
																				9), DASHING_KEBBIT(
																						new Item[][] {
																								{ new Item(10127, 2),
																										new Item(10071),
																										new Item(995,
																												800) } },
																						19);

		/**
		 * Represents the items of the clothing in paralled to childs.
		 */
		private final Item[][] clothing;

		/**
		 * Represents the child ids of the clothing set.
		 */
		private final int[] childs;

		/**
		 * Constructs a new {@code FancyDressOwnerDialogue} {@code Object}.
		 *
		 * @param clothing
		 *            the clothing.
		 * @param childs
		 *            the childs.
		 */
		FurClothing(final Item[][] clothing, final int... childs) {
			this.clothing = clothing;
			this.childs = childs;
		}

		/**
		 * Method used to get the data by the slot.
		 *
		 * @param slot
		 *            the slot.
		 * @return the clothing & the current index.
		 */
		public static Object[] getData(int slot) {
			for (FurClothing cloth : values()) {
				for (int i = 0; i < cloth.getChilds().length; i++) {
					if (cloth.getChilds()[i] == slot) {
						return new Object[] { cloth, i };
					}
				}
			}
			return null;
		}

		/**
		 * Method used to buy a clothing piece.
		 *
		 * @param player
		 *            the player.
		 * @param index
		 *            the index.
		 * @param amount
		 *            the amount.
		 */
		public void buy(final Player player, final int index, int amount) {
			if (!player.hasMoney(getCoins(index).getAmount())) {
				player.sendMessage("You don't have enough coins.");
				return;
			}
			if (!isAnyFur(index) ? !player.getInventory().containsItem(getFur(index).getId(), getFur(index).getAmount())
					: (!player.getInventory().containsItem(getFur(index, true).getId(),
							getFur(index, true).getAmount()))
							&& !player.getInventory().containsItem(getFur(index).getId(), getFur(index).getAmount())) {
				player.sendMessage("You don't have the material required to make this item.");
				return;
			}
			final Item fur = !isAnyFur(index) ? getFur(index)
					: !player.getInventory().containsItem(getFur(index).getId(), getFur(index).getAmount())
							? getFur(index, true) : getFur(index);
			int inventoryAmount = player.getInventory().getAmountOf(fur.getId());
			if (amount > inventoryAmount) {
				amount = inventoryAmount;
			}
			final Item coins = new Item(995, getCoins(index).getAmount() * amount);
			if (!player.hasMoney(coins.getAmount())) {
				player.sendMessage("You don't have enough coins.");
				return;
			}
			if (player.takeMoney(fur.getAmount() * amount)) {
				player.getInventory().addItem(new Item(getProduct(index).getId(), amount));
			}
		}

		/**
		 * Gets the title child.
		 *
		 * @return the title.
		 */
		public int getTitleChild() {
			return childs[0];
		}

		/**
		 * Gets the fur item.
		 *
		 * @param index
		 *            the index.
		 * @param all
		 *            if both furs.
		 * @return the fur.
		 */
		public Item getFur(int index, boolean all) {
			return all ? (new Item(clothing[index][0].getId() - 2)) : clothing[index][0];
		}

		/**
		 * Gets the fur item.
		 *
		 * @param index
		 *            the index.
		 * @return the fur.
		 */
		public Item getFur(int index) {
			return getFur(index, false);
		}

		/**
		 * Gets the product item.
		 *
		 * @param index
		 *            the index.
		 * @return the product.
		 */
		public Item getProduct(int index) {
			return clothing[index][1];
		}

		/**
		 * Gets the coins item.
		 *
		 * @param index
		 *            the index.
		 * @return the coins.
		 */
		public Item getCoins(int index) {
			return clothing[index][2];
		}

		/**
		 * Checks if it takes any type of fur(tatty or not).
		 *
		 * @param index
		 *            the index.
		 * @return {@code True} if so.
		 */
		public boolean isAnyFur(int index) {
			return index > 0 && ordinal() > 3 && ordinal() < 7;
		}

		/**
		 * Gets the value message.
		 *
		 * @param index
		 *            the index.
		 * @return the value message.
		 */
		public String getValueMessage(int index) {
			return getProduct(index).getName() + ": Requires " + (isAnyFur(index) ? "any " : "")
					+ getFur(index).getAmount() + " " + getFur(index).getName().toLowerCase()
					+ (getFur(index).getAmount() > 1 ? "s" : "") + " and " + getCoins(index).getAmount() + " coins.";
		}

		/**
		 * Gets the clothing.
		 *
		 * @return the clothing.
		 */
		public Item[][] getClothing() {
			return clothing;
		}

		/**
		 * Gets the childs.
		 *
		 * @return the childs.
		 */
		public int[] getChilds() {
			return childs;
		}
	}

}