package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Packs the NPC drops data.
 *
 * @author Emperor
 */
public final class NPCDropPacker {

	/**
	 * The path of the packed NPC drop tables.
	 */
	private final static String PATH = "data/npcs/packedDrops.d";

	/**
	 * The path of the packed NPC drop tables.
	 */
	private final static String UNPACKED = "data/npcs/drop_tables_up.txt";

	/**
	 * The currently loaded NPC drop tables.
	 */
	private static final HashMap<Integer, NPCDrop[]> DROP_TABLES = new HashMap<Integer, NPCDrop[]>();

	/**
	 * Unpacks the NPC drops.
	 */
	private static final boolean UNPACK = false;

	/**
	 * If we should name the NPC drops when unpacking.
	 */
	private static final boolean NAME = true;

	/**
	 * Loads the packed NPC drop tables.
	 */
	private static void load() {
		try {
			RandomAccessFile in = new RandomAccessFile(PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			int dropSize = buffer.getShort() & 0xffff;
			DROP_TABLES.clear();
			for (int i = 0; i < dropSize; i++) {
				int npcId = buffer.getShort() & 0xffff;
				NPCDrop[] drops = new NPCDrop[buffer.getShort() & 0xffff];
				for (int d = 0; d < drops.length; d++) {
					if (buffer.get() == 0) {
						drops[d] = new NPCDrop(buffer.getShort() & 0xffff, buffer.getDouble(), buffer.getInt(),
								buffer.getInt());
					} else {
						drops[d] = new NPCDrop(0, 0, 0, 0);
					}
				}
				DROP_TABLES.put(npcId, drops);
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            The arguments cast on runtime.
	 * @throws Throwable
	 *             When an exception occurs.
	 */
	public static void main(String[] args) throws Throwable {
		if (UNPACK) {
			Logger.getGlobal().info("Loading drop tables...");
			load();
			Logger.getGlobal().info("Loaded " + DROP_TABLES.size() + " drop tables!");
			Logger.getGlobal().info("Unpacking...");
			unpack(NAME);
			Logger.getGlobal().info("succesfully unpacked!");
			return;
		} else {
			Logger.getGlobal().info("Packing drop tables...");
			pack();
			Logger.getGlobal().info("Succesfully packed!");
		}
	}

	/**
	 * Packs the NPC drop tables.
	 *
	 * @throws Throwable
	 *             When an exception occurs.
	 */
	@SuppressWarnings("resource")
	private static void pack() throws Throwable {
		File f = new File(UNPACKED);
		if (!f.exists()) {
			throw new IllegalStateException("Unpacked NPC drops [path=" + UNPACKED + "] does not exist!");
		}
		Map<Integer, List<NPCDrop>> cache = new HashMap<Integer, List<NPCDrop>>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		while ((s = br.readLine()) != null) {
			if (s.startsWith("//") || s.startsWith(" ") || s.equals("")) {
				continue;
			}
			if (s.length() < 5) {
				continue;
			}
			String[] arg = s.split(":");
			int npcId = Integer.parseInt(arg[0].replace("[", "").replace("]", ""));
			List<NPCDrop> drops = cache.get(npcId);
			if (drops == null) {
				drops = new ArrayList<NPCDrop>();
				cache.put(npcId, drops);
			}
			arg = arg[1].replaceAll(" ", "").split(",");
			int itemId = Integer.parseInt(arg[0]);
			double rate = Double.parseDouble(arg[1]);
			int min = Integer.parseInt(arg[2]);
			int max = Integer.parseInt(arg[3]);
			drops.add(new NPCDrop(itemId, rate, min, max));
		}
		new File(PATH).delete();
		RandomAccessFile out = new RandomAccessFile(PATH, "rw");
		ByteBuffer buffer = ByteBuffer.allocate(1 << 22);
		buffer.putShort((short) cache.size());
		for (Map.Entry<Integer, List<NPCDrop>> entry : cache.entrySet()) {
			int npcId = entry.getKey();
			NPCDrop[] drops = entry.getValue().toArray(new NPCDrop[0]);
			DROP_TABLES.put(npcId, drops);
			buffer.putShort((short) npcId);
			buffer.putShort((short) drops.length);
			for (NPCDrop d : drops) {
				buffer.put((byte) 0);
				buffer.putShort((short) d.getItemId());
				buffer.putDouble(d.getRate());
				buffer.putInt(d.getMinAmount());
				buffer.putInt(d.getMaxAmount());
			}
		}
		buffer.flip();
		out.getChannel().write(buffer);
		out.getChannel().close();
		out.close();
	}

	/**
	 * Unpacks the NPC drop tables.
	 *
	 * @param named
	 *            If we should add naming comments.
	 * @throws Throwable
	 *             When an exception occurs.
	 */
	private static void unpack(boolean named) throws Throwable {
		if (named) {
			Cache.init();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(UNPACKED));
		bw.append("//Unpacked NPC drops dump - by Emperor");
		bw.newLine();
		bw.append("//When setting \"isRare\" to true; it will create a drop with all values set to 0.");
		bw.newLine();
		bw.append("//Format: [npcId]: itemId, rate, min-amt, max-amt, isRare");
		bw.newLine();
		for (Map.Entry<Integer, NPCDrop[]> entry : DROP_TABLES.entrySet()) {
			Integer npcId = entry.getKey();
			NPCDrop[] table = entry.getValue();
			for (NPCDrop drop : table) {
				StringBuilder sb = new StringBuilder("[").append(npcId).append("]: ").append(drop.getItemId())
						.append(", ").append(drop.getRate()).append(", ").append(drop.getMinAmount()).append(", ")
						.append(drop.getMaxAmount()).append(", ");
				if (named) {
					val npc = NPCDefinitions.getNPCDefinitions(npcId);
					sb.append(" //").append("NPC name=").append(npc == null ? "null" : npc.getName())
							.append(", item name=")
							.append(ItemDefinitions.getItemDefinitions(drop.getItemId()).getName());
				}
				bw.append(sb.toString());
				bw.newLine();
			}
		}
		bw.flush();
		bw.close();
	}
}