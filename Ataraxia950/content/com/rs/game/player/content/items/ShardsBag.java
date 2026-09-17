package com.rs.game.player.content.items;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardsBag {

	private static final int scroll = 132;

	public static Dialogue BagDialogue = new Dialogue() {
		int index;

		@Override
		public void start() {
			index = (int) parameters[0];
			switch (index) {

			case 0: // check shards
				sendOptionsDialogue(Colors.WHITE + Colors.SHAD + "What cape would you like to check?</col></shad>",
						"Combatant's Cape", "Support Cape", "Artisan's Cape", "Gatherer's Cape", "Nevermind");
				stage = 0;
				break;
			case 1:
				sendOptionsDialogue(
						Colors.WHITE + Colors.SHAD + "What cape's shards would you like to withdraw?</col></shad>",
						"Combatant's Cape", "Support Cape", "Artisan's Cape", "Gatherer's Cape", "Nevermind");
				stage = 1;
				break;
			}
		}

		@Override
		public void run(int interfaceId, int componentId) {
			switch (stage) {
			case 0: // check shards
				finish();
				if (componentId != OPTION_5)
					ShardsBag.checkShards(player, componentId);
				break;
			case 1:
				finish();
				if (componentId != OPTION_5)
					ShardsBag.withdrawShards(player, componentId);
				break;
			}
		}

		@Override
		public void finish() {
			player.getInterfaceManager().closeChatBoxInterface();
		}
	};

	/**
	 * Enum for Expert Skillcape Data - contains componentId, the item component
	 * range, and the cape id
	 */
	public enum Capes {
		COMBATANT(Dialogue.OPTION_1, new int[] { 32069, 32076 }, 32053), SUPPORT(Dialogue.OPTION_2,
				new int[] { 32085, 32088 }, 32055), ARTISAN(Dialogue.OPTION_3, new int[] { 32077, 32084 },
						32054), GATHERER(Dialogue.OPTION_4, new int[] { 32063, 32068 }, 32052);

		private static final Map<Integer, Capes> capes = new HashMap<Integer, Capes>();

		static {
			for (Capes cape : Capes.values())
				capes.put(cape.getId(), cape);
		}

		private final int id;
		private final int[] components;
		private final int reward;

		Capes(int id, int[] components, int reward) {
			this.id = id;
			this.components = components;
			this.reward = reward;
		}

		public int getId() {
			return this.id;
		}

		public int[] getComponents() {
			return this.components;
		}

		public int getRange() {
			return this.components[1] - this.components[0];
		}

		public int getReward() {
			return this.reward;
		}

		public String getName() {
			Item item = new Item(this.reward, 1);
			return item.getName();
		}

		public static Capes getCape(int compid) {
			return capes.get(compid);
		}
	}

	public static void shardMapInit(Player player) {
		for (int i = 0; i < 26; i++)
			player.setShard(32063 + i, false);
	}

	public static void checkShards(Player player, int compId) {
		// Initialization of all variables
		Capes cape = Capes.getCape(compId); // get cape stored by its dialogue
											// componentId

		if (player.hasItem(new Item(cape.getReward()))) { // check if player
															// already has this
															// cape
			player.sendMessage(Colors.ORANGE + Colors.SHAD + "You have already acquired this skillcape!", true);
			return;
		}

		List<Integer> missing = new ArrayList<Integer>();
		int sCounter = 0;
		int shard;
		boolean stored;
		String[] payloads = { Colors.DPURPLE + Colors.SHAD + cape.getName() + "</col></shad>",
				Colors.GREEN + Colors.SHAD + "> Stored shards" + "</col></shad>",
				Colors.RED + Colors.SHAD + "> Missing shards" + "</col></shad>" };
		int[][] cids = { { 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45 }, { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 } };

		// do header information
		player.getInterfaceManager().sendInterface(scroll);
		for (int cid : cids[0])
			player.getPackets().sendIComponentText(scroll, cid, "");
		player.getPackets().sendIComponentText(scroll, 47, payloads[0]);

		// do component display
		for (int x = 0; x <= cape.getRange(); x++) {
			shard = cape.getComponents()[0] + x;
			stored = player.shardStored(shard);
			if (stored) {
				player.getPackets().sendIComponentText(scroll, cids[1][sCounter], new Item(shard).getName());
				sCounter++;
			} else
				missing.add(shard);
			if (sCounter != 0 && x == cape.getRange())
				player.getPackets().sendIComponentText(scroll, 3, payloads[1]);
		}
		if (missing.size() != 0)
			player.getPackets().sendIComponentText(scroll, (sCounter == 0 ? 3 : cids[1][sCounter]), payloads[2]);
		for (int x = 0; x < missing.size(); x++)
			player.getPackets().sendIComponentText(scroll, cids[1][sCounter + (sCounter == 0 ? 0 : 1) + x],
					new Item(missing.get(x)).getName());
	}

	public static void withdrawShards(Player player, int cid) {
		Capes cape = Capes.getCape(cid); // get cape stored by its dialogue
											// componentId
		int shard;
		boolean stored;
		List<Integer> shards = new ArrayList<Integer>();

		if (player.hasItem(new Item(cape.getReward(), 1))) {
			player.sendMessage(Colors.ORANGE + Colors.SHAD + "You have already acquired this skillcape!", true);
			return;
		}

		for (int x = 0; x <= cape.getRange(); x++) {
			shard = cape.getComponents()[0] + x;
			stored = player.shardStored(shard);
			if (stored)
				shards.add(shard);
		}

		for (int item : shards) {
			if (player.getInventory().hasFreeSlots()) {
				player.getInventory().addItem(item, 1);
				player.setShard(item, false);
			}
		}
	}

	public static void fillShards(Player player) {
		List<Integer> shards = new ArrayList<Integer>();

		for (int x = 0; x < 26; x++) {
			if (player.getInventory().containsItem(32063 + x, 1) && !player.shardStored(32063 + x))
				shards.add(32063 + x);
		}

		if (shards.size() != 0) {
			for (int shard : shards) {
				player.getInventory().deleteItem(shard, 1);
				player.setShard(shard, true);
			}
			player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + shards.size() + " skillcape shard"
					+ (shards.size() == 1 ? "" : "s") + " to your bag!", true);
		} else
			player.sendMessage(Colors.SALMON + Colors.SHAD + "You don't have any skillcape shards in your inventory!",
					true);
	}

}
