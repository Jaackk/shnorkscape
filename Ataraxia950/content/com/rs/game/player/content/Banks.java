package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public class Banks {

	public static void makeBanks(Player player) {
		do {
			player.addBank(false, new Bank());
			player.setBankSpace(player.getBankSpace() - Bank.MAX_BANK_SIZE);
		} while (player.getBankSpace() > Bank.MAX_BANK_SIZE);
	}

	public static void fixThisBank(Player player, int index) {
		player.isFixing = false;
		player.sm("You have fixed your bank.");
		List<Item> move = new ArrayList<Item>(); // items to move into bank 1
		Item item;
		int[] slot;
		player.setBank(player.getBanks().get(index));

		for (int i = 0; i <= player.getBank().getBankSize(true) - Bank.MAX_BANK_SIZE; i++) {
			slot = player.getBank().getRealSlot(i);
			item = player.getBank().getItem(slot) == null ? null : player.getBank().getItem(slot);
			if (item != null) {
				move.add(new Item(player.getBank().getItem(slot)));
				player.getBank().removeItem(slot, item.getAmount(), true, Bank.DESTROY_ITEM);
			}
		}

		if (move.size() != 0) {
			player.setBank(player.getBanks().get(index + 1)); // first new bank
			for (Item each : move) {
				if (each != null)
					player.getBank().addItem(each, true);
			}
		}
	}

	public static void fixBank(Player player) {
		List<Item> move = new ArrayList<Item>(); // items to move into bank 1
		Item item;
		int[] slot;

		for (int i = 0; i <= player.getBank().getBankSize(true) - Bank.MAX_BANK_SIZE; i++) {
			slot = player.getBank().getRealSlot(i);
			item = player.getBank().getItem(slot) == null ? null : player.getBank().getItem(slot);
			if (item != null) {
				move.add(new Item(player.getBank().getItem(slot)));
				player.getBank().removeItem(slot, item.getAmount(), true, Bank.DESTROY_ITEM);
			}
		}

		if (move.size() != 0) {
			player.setBank(player.getBanks().get(1)); // first new bank
			for (Item each : move) {
				if (each != null)
					player.getBank().addItem(each, true);
			}
		}
	}

	public static void handleButton(Player player, int component) {
		int[] cids = { 35, 43, 51, 59, 67, 75, 83, 91, 99 };
		for (int cid : cids) {
			if (component == cid) {
				int index = ((cid - 3) / 8) - 4; // cid - 3 to factor of 8,
													// subtract 4 for minimum 0
													// max 8
				if (player.getBanks().size() > index || index == 0) {
					if(index < player.getBanks().size())
							player.setBank(player.getBanks().get(index));
					player.getBank().setPlayer(player);
					player.getBank().openBank();

				} else
					player.sendMessage(Colors.RED + Colors.SHAD + "You do not own this bank currently!", false);
				return;
			}
		}
	}

	public static void sendBankList(Player player) {
		int list = 1312;
		player.closeInterfaces();
		for (int x = 0; x < 9; x++) {
			player.getPackets().sendIComponentText(list, 38 + (8 * x), (player.getBanks().size() > x ?
					Colors.GREEN+player.getBanks().get(x).getName() : Colors.RED+"Bank not owned!"));
		}
		player.getInterfaceManager().sendInterface(list);
	}

}
