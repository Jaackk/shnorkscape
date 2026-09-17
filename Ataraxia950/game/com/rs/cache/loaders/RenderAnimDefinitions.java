package com.rs.cache.loaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.filestore.io.InputStream;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class RenderAnimDefinitions {

	public boolean aBool7015;
	public int[] loopAnimations;
	public int[] loopAnimDurations;
	int anInt7018;
	public int anInt7019;
	public int moveType1Anim;
	public int runRotate90CounterAnimation;
	public int rotate180Animation;
	public int rotate90Animation;
	public int rotate90CounterAnimation;
	public int runAnimation;
	public int[] anIntArray7026;
	public int runRotate90Animation;
	public int anInt7028;
	public int standAnimation;
	public int type1_180;
	public int type1_90;
	public int type1_90_counter;
	public int anInt7033;
	public int anInt7034;
	public int anInt7035;
	public int anInt7036;
	public int anInt7037;
	public int anInt7038;
	public int anInt7039;
	public int anInt7040;
	public int anInt7041;
	public int anInt7042;
	public int[][] anIntArrayArray7043;
	public int[][] anIntArrayArray7044;
	public int anInt7046;
	public int[] anIntArray7047;
	public int anInt7048;
	public int walkAnimation;
	public int walkUpwardsAnimation;
	public int anInt7051;
	public int anInt7052;
	public int anInt7053;
	public int anInt7054;
	public int runRotate180Animation;
	public int anInt7056;
	public int anInt7057;
	public int anInt7058;

	private static final ConcurrentHashMap<Integer, RenderAnimDefinitions> renderAimDefs = new ConcurrentHashMap<Integer, RenderAnimDefinitions>();

	/** Id of this BAS entry (index 2 archive 32 file id); set by the loaders, -1 for a bare instance. */
	public int id = -1;
	/** Non-null when the strict 947 decode of this id failed; the definition then only holds defaults. */
	public String decodeFailure;
	/*
	 * 947 BAS layout finding (DefinitionScanProbe, notes/P3-definition-scan.md):
	 * the 910 table reads opcode 52 entries as (bigSmart animation, u8 duration),
	 * but every 947 file carrying opcode 52 uses (bigSmart animation, 2 bytes).
	 * Reading one byte desynchronised 278 of the 399 files and made the stray
	 * duration bytes look like new opcodes 10/20/128/131/133/135/136/137 - the
	 * "string/u8/u24" opcodes hypothesised by the assessment. Those are therefore
	 * NOT added; the modern loop below fixes opcode 52 instead and every other
	 * unknown opcode still fails closed.
	 */
	/** 947 only: second byte of each 2-byte opcode-52 duration field (0 in every sampled file); null on the 910 path. */
	public int[] loopAnimDurationsLow;

	/** Ids whose 947 cache file exists but could not be decoded strictly (id -> reason). */
	private static final ConcurrentHashMap<Integer, String> DECODE_FAILURES = new ConcurrentHashMap<Integer, String>();

	/** Every 947 BAS entry that failed strict decoding so far (unknown opcode, truncation, trailing bytes). */
	public static Map<Integer, String> getDecodeFailures() {
		return Collections.unmodifiableMap(DECODE_FAILURES);
	}

	/** Reason the given BAS entry failed strict 947 decoding, or null. */
	public static String getDecodeFailure(int renderAnimationId) {
		return DECODE_FAILURES.get(renderAnimationId);
	}

	/**
	 * Strict 947 decode of a raw index-2/archive-32 file (probe/tests): throws
	 * IllegalArgumentException ("... opcode N at offset" / "Trailing bytes ...")
	 * instead of reading through EOF zeroes. When trace is non-null every opcode is
	 * appended as "opcode@offset ".
	 */
	public static RenderAnimDefinitions decodeStrict947(int renderAnimationId, byte[] data, StringBuilder trace) {
		RenderAnimDefinitions defs = new RenderAnimDefinitions();
		defs.id = renderAnimationId;
		defs.readModernValueLoop(new com.rs.network.io.InputStream(data, true), trace);
		return defs;
	}

	public static final RenderAnimDefinitions getRenderAnimDefinitions(int renderAnimationId) {
		RenderAnimDefinitions defs = renderAimDefs.get(renderAnimationId);
		if (defs != null)
			return defs;
		if (renderAnimationId == -1)
			return null;
		byte[] data = Cache.STORE.getIndexes()[2].getFile(32, renderAnimationId);
		defs = new RenderAnimDefinitions();
		defs.id = renderAnimationId;
		if (data != null) {
			if (Cache.isFlatReadOnly()) {
				// 947: strict stream; a failure yields a defaults-only definition that
				// carries the reason and is registered, never a misaligned partial decode.
				try {
					defs.readModernValueLoop(new com.rs.network.io.InputStream(data, true), null);
				} catch (RuntimeException failure) {
					defs = markDecodeFailure(renderAnimationId, failure);
				}
			} else {
				defs.readValueLoop(new InputStream(data));
			}
		}
		renderAimDefs.put(renderAnimationId, defs);
		return defs;
	}

	/** Builds the well-defined failed definition for a 947 BAS entry and records it in the registry. */
	private static RenderAnimDefinitions markDecodeFailure(int renderAnimationId, RuntimeException failure) {
		String reason = failure.getMessage() == null ? failure.toString() : failure.getMessage();
		RenderAnimDefinitions failed = new RenderAnimDefinitions();
		failed.id = renderAnimationId;
		failed.decodeFailure = reason;
		if (DECODE_FAILURES.put(renderAnimationId, reason) == null)
			Logger.getGlobal().warn("947 render animation " + renderAnimationId + " rejected by strict decoder: " + reason);
		return failed;
	}

	public int[] anIntArray1246;
	public int[][] anIntArrayArray1217;

	/** Legacy 910 loop: tolerant filestore stream, unknown opcodes ignored exactly as before. */
	private void readValueLoop(InputStream stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(new LegacyReader(stream), opcode);
		}
	}

	/** Strict 947 loop: 947-only opcodes first, then the shared table, else fail closed. */
	private void readModernValueLoop(com.rs.network.io.InputStream stream, StringBuilder trace) {
		ModernReader reader = new ModernReader(stream);
		for (;;) {
			int offset = stream.getOffset();
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			if (trace != null)
				trace.append(opcode).append('@').append(offset).append(' ');
			if (opcode == 52) {
				// 947: count x (bigSmart animation, 2-byte duration field). Confirmed by
				// exact-consumption decoding of all 399 files that carry this opcode.
				int count = stream.readUnsignedByte();
				loopAnimations = new int[count];
				loopAnimDurations = new int[count];
				loopAnimDurationsLow = new int[count];
				for (int index = 0; index < count; index++) {
					loopAnimations[index] = stream.readBigSmart();
					int field = stream.readUnsignedShort();
					loopAnimDurations[index] = field >>> 8;
					loopAnimDurationsLow[index] = field & 0xff;
					anInt7018 += loopAnimDurations[index];
				}
			} else if (!readValues(reader, opcode)) {
				throw new IllegalArgumentException("Unsupported modern render animation " + id + " opcode " + opcode + " at " + offset);
			}
		}
		if (stream.getRemaining() != 0)
			throw new IllegalArgumentException("Trailing bytes in modern render animation " + id + " at " + stream.getOffset());
	}

	/** Read surface shared by the tolerant 910 filestore stream and the strict 947 stream. */
	private interface OpcodeReader {
		int readUnsignedByte();
		int readShort();
		int readUnsignedShort();
		int readBigSmart();
	}

	private static final class LegacyReader implements OpcodeReader {
		private final InputStream stream;
		LegacyReader(InputStream stream) { this.stream = stream; }
		public int readUnsignedByte() { return stream.readUnsignedByte(); }
		public int readShort() { return stream.readShort(); }
		public int readUnsignedShort() { return stream.readUnsignedShort(); }
		public int readBigSmart() { return stream.readBigSmart(); }
	}

	private static final class ModernReader implements OpcodeReader {
		private final com.rs.network.io.InputStream stream;
		ModernReader(com.rs.network.io.InputStream stream) { this.stream = stream; }
		public int readUnsignedByte() { return stream.readUnsignedByte(); }
		public int readShort() { return stream.readShort(); }
		public int readUnsignedShort() { return stream.readUnsignedShort(); }
		public int readBigSmart() { return stream.readBigSmart(); }
	}

	/** Shared 910 opcode table; returns false for an opcode it does not know. */
	private boolean readValues(OpcodeReader buffer, int opcode) {
		if (1 == opcode) {
			standAnimation = buffer.readBigSmart();
			walkAnimation = buffer.readBigSmart();
		} else if (2 == opcode) {
			moveType1Anim = buffer.readBigSmart();
		} else if (3 == opcode) {
			type1_180 = buffer.readBigSmart();
		} else if (4 == opcode) {
			type1_90 = buffer.readBigSmart();
		} else if (opcode == 5) {
			type1_90_counter = buffer.readBigSmart();
		} else if (opcode == 6) {
			runAnimation = buffer.readBigSmart();
		} else if (opcode == 7) {
			runRotate180Animation = buffer.readBigSmart();
		} else if (opcode == 8) {
			runRotate90Animation = buffer.readBigSmart();
		} else if (opcode == 9) {
			runRotate90CounterAnimation = buffer.readBigSmart();
		} else if (opcode == 26) {
			anInt7039 = (short) (buffer.readUnsignedByte() * 4);
			anInt7040 = (short) (buffer.readUnsignedByte() * 4);
		} else if (opcode == 27) {
			int count = buffer.readUnsignedByte();
			if (null == anIntArrayArray7043) {
				anIntArrayArray7043 = new int[1 + count][];
			} else if (count >= anIntArrayArray7043.length) {
				anIntArrayArray7043 = Arrays.copyOf(anIntArrayArray7043, count + 1);
			}
			anIntArrayArray7043[count] = new int[6];
			for (int index = 0; index < 6; index++) {
				anIntArrayArray7043[count][index] = buffer.readShort();
			}
		} else if (opcode == 28) {
			int count = buffer.readUnsignedByte();
			anIntArray7047 = new int[count];

			for (int index = 0; index < count; index++) {
				anIntArray7047[index] = buffer.readUnsignedByte();
				if (255 == anIntArray7047[index]) {
					anIntArray7047[index] = -1;
				}
			}
		} else if (opcode == 29) {
			anInt7048 = buffer.readUnsignedByte();
		} else if (30 == opcode) {
			anInt7028 = buffer.readUnsignedShort();
		} else if (31 == opcode) {
			anInt7054 = buffer.readUnsignedByte();
		} else if (32 == opcode) {
			anInt7037 = buffer.readUnsignedShort();
		} else if (33 == opcode) {
			anInt7019 = buffer.readShort();
		} else if (opcode == 34) {
			anInt7053 = buffer.readUnsignedByte();
		} else if (35 == opcode) {
			anInt7051 = buffer.readUnsignedShort();
		} else if (36 == opcode) {
			anInt7052 = buffer.readShort();
		} else if (opcode == 37) {
			anInt7056 = buffer.readUnsignedByte();
		} else if (opcode == 38) {
			walkUpwardsAnimation = buffer.readBigSmart();
		} else if (opcode == 39) {
			anInt7046 = buffer.readBigSmart();
		} else if (40 == opcode) {
			rotate180Animation = buffer.readBigSmart();
		} else if (opcode == 41) {
			rotate90Animation = buffer.readBigSmart();
		} else if (42 == opcode) {
			rotate90CounterAnimation = buffer.readBigSmart();
		} else if (opcode == 43) {
			buffer.readUnsignedShort();
		} else if (opcode == 44) {
			buffer.readUnsignedShort();
		} else if (opcode == 45) {
			anInt7057 = buffer.readUnsignedShort();
		} else if (opcode == 46) {
			anInt7033 = buffer.readBigSmart();
		} else if (47 == opcode) {
			anInt7034 = buffer.readBigSmart();
		} else if (48 == opcode) {
			anInt7035 = buffer.readBigSmart();
		} else if (49 == opcode) {
			anInt7036 = buffer.readBigSmart();
		} else if (opcode == 50) {
			anInt7041 = buffer.readBigSmart();
		} else if (51 == opcode) {
			anInt7038 = buffer.readBigSmart();
		} else if (opcode == 52) {
			int count = buffer.readUnsignedByte();
			loopAnimations = new int[count];
			loopAnimDurations = new int[count];

			for (int index = 0; index < count; index++) {
				loopAnimations[index] = buffer.readBigSmart();
				int i_8_ = buffer.readUnsignedByte();

				loopAnimDurations[index] = i_8_;
				anInt7018 += i_8_;
			}
		} else if (53 == opcode) {
			aBool7015 = false;
		} else if (54 == opcode) {
			anInt7058 = (buffer.readUnsignedByte() << 6);
			anInt7042 = (buffer.readUnsignedByte() << 6);
		} else if (55 == opcode) {
			int index = buffer.readUnsignedByte();
			if (null == anIntArray7026) {
				anIntArray7026 = new int[index + 1];
			} else if (index >= anIntArray7026.length) {
				anIntArray7026 = Arrays.copyOf(anIntArray7026, 1 + index);
			}

			anIntArray7026[index] = buffer.readUnsignedShort();
		} else if (opcode == 56) {
			int count = buffer.readUnsignedByte();
			if (null == anIntArrayArray7044) {
				anIntArrayArray7044 = new int[count + 1][];
			} else if (count >= anIntArrayArray7044.length) {
				anIntArrayArray7044 = Arrays.copyOf(anIntArrayArray7044, count + 1);
			}

			anIntArrayArray7044[count] = new int[3];
			for (int index = 0; index < 3; index++) {
				anIntArrayArray7044[count][index] = buffer.readShort();
			}
		} else {
			return false;
		}
		return true;
	}

	private Object getValue(Field field) throws Throwable {
		field.setAccessible(true);
		Class<?> type = field.getType();
		if (type == int[][].class) {
			return Arrays.toString((int[][]) field.get(this));
		} else if (type == int[].class) {
			return Arrays.toString((int[]) field.get(this));
		} else if (type == byte[].class) {
			return Arrays.toString((byte[]) field.get(this));
		} else if (type == short[].class) {
			return Arrays.toString((short[]) field.get(this));
		} else if (type == double[].class) {
			return Arrays.toString((double[]) field.get(this));
		} else if (type == float[].class) {
			return Arrays.toString((float[]) field.get(this));
		} else if (type == Object[].class) {
			return Arrays.toString((Object[]) field.get(this));
		}
		return field.get(this);
	}

	/**
	 * Prints all fields in this class.
	 */
	public void printFields() {
		for (Field field : getClass().getDeclaredFields()) {
			if ((field.getModifiers() & 8) != 0) {
				continue;
			}
			try {
				Logger.getGlobal().info(field.getName() + ": " + getValue(field));
			} catch (Throwable e) {
				Logger.getGlobal().catching(e);
			}
		}
		Logger.getGlobal().info("-- end of " + getClass().getSimpleName() + " fields --");
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		File file = new File("data/world/npcs/anims dump.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			RenderAnimDefinitions defs = RenderAnimDefinitions
					.getRenderAnimDefinitions(NPCDefinitions.getNPCDefinitions(i).renderEmote);
			if (defs != null) {
				writer.write("ID: " + i + ", Run: " + defs.runAnimation + ", Walk: " + defs.walkAnimation + ", Stand: "
						+ defs.standAnimation);
				writer.newLine();
				writer.flush();
			}
		}
		writer.close();

		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			RenderAnimDefinitions defs = RenderAnimDefinitions
					.getRenderAnimDefinitions(NPCDefinitions.getNPCDefinitions(i).renderEmote);
			if (defs != null) {
				if (defs.walkAnimation == 3830)
					Logger.getGlobal().info(NPCDefinitions.getNPCDefinitions(i).renderEmote);
			}
		}

	}

	public RenderAnimDefinitions() {
		walkUpwardsAnimation = -1;
		anInt7046 = 0;
		walkAnimation = -1;
		rotate180Animation = -1;
		rotate90Animation = -1;
		rotate90CounterAnimation = -1;
		runAnimation = -1;
		runRotate180Animation = 0;
		runRotate90Animation = 0;
		runRotate90CounterAnimation = 0;
		moveType1Anim = 0;
		type1_180 = 0;
		type1_90 = 0;
		type1_90_counter = 0;
		anInt7033 = 0;
		anInt7034 = 0;
		anInt7035 = 0;
		anInt7036 = 0;
		anInt7041 = 0;
		anInt7038 = 0;
		anInt7056 = 0;
		anInt7057 = 0;
	}

}
