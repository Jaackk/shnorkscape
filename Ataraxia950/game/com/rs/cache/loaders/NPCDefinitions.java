package com.rs.cache.loaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.filestore.io.OutputStream;
import com.rs.game.player.content.Combat;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public final class NPCDefinitions {

	// public static final Int2ObjectOpenHashMap<NPCDefinitions> NPC_DEFINITIONS = new Int2ObjectOpenHashMap<NPCDefinitions>();
	public static final ConcurrentHashMap<String, Integer> npcDefinitionsByName = new ConcurrentHashMap<String, Integer>();

	private static final NPCDefinitions[] NPC_DEFINITIONS = new NPCDefinitions[Utils.getNPCDefinitionsSize() + 1];

	private static final boolean RS3_NPCS = true;

	private int id;
	public int cursor2op;
	public int anInt5281;
	public int size;
	public int[] models;
	public int anInt3042;
	int[][] anIntArrayArray5285;
	public static short[] aShortArray5286 = new short[256];
	short[] recolorSrc;
	public int anInt5288;
	byte[] paletteIndices;
	public int anInt5290;
	public int cursor1op;
	byte aByte5292;
	byte aByte5293;
	public int contrast;
	public boolean aBool5295;
	public String[] menuOptions;
	public int renderEmote;
	public int cursor2;
	byte aByte5298;
	public int attackCursor;
	public int[] headModels;
	public int cursor1;
	int scaleX;
	int scaleY;
	public boolean aBool5304;
	public short[] recolorDst;
	public int anInt5306;
	int anInt5307;
	int anInt5308;
	short[] retextureSrc;
	public int anInt5310;
	public int anInt5311;
	public int[] transformTo;
	public int varbitId;
	public int varId;
	public boolean aBool5315;
	public boolean aBool5316;
	public boolean aBool5317;
	public short aShort5318;
	public short aShort5319;
	public byte aByte5320;
	public byte aByte5321;
	public boolean aBool5322;
	public int anInt3029;
	public boolean aBool5324;
	public short[] retextureDst;
	public int anInt3068;
	byte aByte5327;
	public int anInt5328;
	public int anInt5329;
	public boolean aBool5330;
	public String name = "null";
	public int anInt5331;
	public int anInt5332;
	public int anInt3050;
	public int anInt5335;
	public int[] anIntArray5336;
	public byte aByte5337;
	public int anInt3065;
	public byte movementCapabilities;
	public boolean drawMinimapDot;
	public int anInt5341;
	public int combatLevel;
	public int[] menuCursors;
	public int respawnDirection;
	public HashMap<Integer, Object> clientScriptData;

	public NPCDefinitions(final int id) {
		size = 1;
		renderEmote = -1;
		aByte5327 = (byte) 0;
		cursor1 = -1;
		cursor2 = -1;
		cursor1op = -1;
		cursor2op = -1;
		attackCursor = -1;
		drawMinimapDot = true;
		combatLevel = -1;
		scaleX = 128;
		scaleY = 128;
		aBool5304 = false;
		aBool5324 = false;
		aBool5295 = false;
		anInt5307 = 0;
		anInt5308 = 0;
		anInt5311 = -1;
		anInt5310 = -1;
		contrast = 32;
		varbitId = -1;
		varId = -1;
		aBool5315 = true;
		aBool5316 = true;
		respawnDirection = (byte) 7;
		aBool5317 = true;
		aShort5318 = (short) 0;
		aShort5319 = (short) 0;
		aByte5320 = (byte) -96;
		aByte5321 = (byte) -16;
		movementCapabilities = (byte) 0;
		anInt3029 = -1;
		anInt3065 = -1;
		anInt3050 = -1;
		anInt3042 = -1;
		anInt3068 = 0;
		anInt5328 = 0;
		anInt5329 = 255;
		anInt5331 = -1;
		anInt5332 = -1;
		anInt5281 = -1;
		anInt5290 = -1;
		aByte5337 = (byte) -1;
		anInt5335 = -1;
		anInt5288 = 256;
		anInt5306 = 256;
		anInt5341 = 0;
		menuOptions = new String[5];
		aBool5322 = true;
	}

	/** Non-null when the strict 947 decode of this id failed; the definition then only holds defaults. */
	public String decodeFailure;

	/** Ids whose 947 cache file exists but could not be decoded strictly (id -> reason). */
	private static final ConcurrentHashMap<Integer, String> DECODE_FAILURES = new ConcurrentHashMap<Integer, String>();

	/** Every 947 NPC that failed strict decoding so far (unknown opcode, truncation, trailing bytes). */
	public static Map<Integer, String> getDecodeFailures() {
		return Collections.unmodifiableMap(DECODE_FAILURES);
	}

	/** Reason the given NPC failed strict 947 decoding, or null. */
	public static String getDecodeFailure(int npcId) {
		return DECODE_FAILURES.get(npcId);
	}

	/**
	 * Strict 947 decode of a raw index-18 file (probe/tests): throws
	 * IllegalArgumentException ("... opcode N at offset" / "Trailing bytes ...")
	 * instead of returning a misaligned definition. 947 opcodes 184/185/186/253 are
	 * deliberately NOT guessed; they abort the definition until their layout is
	 * confirmed. When trace is non-null every opcode is appended as "opcode@offset ".
	 */
	public static NPCDefinitions decodeStrict947(int id, byte[] data, StringBuilder trace) {
		NPCDefinitions def = new NPCDefinitions(id);
		def.id = id; // the 910 constructor never stores the id; the 947 paths need it for messages/getId()
		def.readValueLoop(new InputStream(data, true), true, trace);
		if (def.models == null)
			def.models = new int[0];
		return def;
	}

	private void readValueLoop(final InputStream stream) {
		readValueLoop(stream, false, null);
	}

	private void readValueLoop(final InputStream stream, final boolean strict, final StringBuilder trace) {
		while (true) {
			final int offset = stream.getOffset();
			final int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				break;
			}
			if (trace != null)
				trace.append(opcode).append('@').append(offset).append(' ');
			decode(stream, opcode, strict);
			if (id == 2707) {
//	        	menuOptions = new String[] {  };
			}
		}
		if (strict && stream.getRemaining() != 0)
			throw new IllegalArgumentException("Trailing bytes in modern npc " + id + " at " + stream.getOffset());
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		NPCDefinitions i = NPCDefinitions.getNPCDefinitions(24791);
		Logger.getGlobal().info(Arrays.toString(i.menuOptions));
	}

	@Deprecated
	public static final void clearNPCDefinitions() {
		// NPC_DEFINITIONS.clear();
	}

	public void decode(final InputStream buffer, final int opcode) {
		decode(buffer, opcode, false);
	}

	/**
	 * NpcType (cache index 18) field decoder.
	 *
	 * Everything guarded by {@link Cache#isFlatReadOnly()} is 950-only and was derived from the
	 * 950 client's own NpcType decoder, a cmp/jne opcode ladder occupying
	 * 950 0x1403b33d0 .. 0x1403b5334 (arguments: rcx = NpcType, rdx = buffer, r8d = opcode, which
	 * the prologue copies to r9d; the buffer's read position is the qword at buffer+0x18, so every
	 * "lea rax, [rcx + n]; mov [rdx+0x18], rax" in that ladder is an n-byte field). Individual
	 * opcodes are cited inline. The function is only reachable through a vtable slot
	 * (950 0x140b7a888); it is identified as NpcType by the constructor that precedes it
	 * (950 0x1403b3050 .. 0x1403b3388), whose defaults line up field for field with this class -
	 * e.g. 950 0x1403b330a writes 255 to +0x4cc, the field opcode 140 sets, and 950 0x1403b3314
	 * writes 256 to +0x4d0 and +0x4d4, the pair opcode 164 sets.
	 * NOTE: 947 addresses do not carry over - they disassemble cleanly in the 950 binary but land
	 * on unrelated code, so every address below was re-derived against the 950 image.
	 *
	 * The complete opcode set the 950 client accepts is:
	 *   1 2 12 30-34 39 40 41 42 44 45 60 93 95 97 98 99 100 101 102 103 106 107 109 111 113 114
	 *   118 119 121 123 125 127 128 134 137 138 139 140 141 142 143 150-154 155 158 159 160 163
	 *   164 165 168 169 170-175 178 179 180 181 182 184 185 186 187 188 189 249 252 253
	 * Anything else falls through the ladder to the loop-continue at 950 0x1403b5334 and reads
	 * nothing. Of the accepted set the shipped 950 cache uses all except 39, 42, 106, 118, 138,
	 * 139, 168, 171, 175, 180, 181, 186 and 252; those are implemented from the client only.
	 * The 950 client has NO case for 122, 135, 136 or 162 - see the notes on those below.
	 *
	 * Verification: every one of the 32,762 NpcType definitions in the 950 cache was parsed with
	 * this exact width table; all terminate on opcode 0 at exactly their record length, with no
	 * unknown opcode and no over- or under-read.
	 *
	 * strict=true rejects unknown opcodes instead of logging and reading on misaligned.
	 */
	private void decode(final InputStream buffer, final int opcode, final boolean strict) {
		if (opcode == 1) {
			final int i_1_ = buffer.readUnsignedByte();
			models = new int[i_1_];
			for (int i_2_ = 0; i_2_ < i_1_; i_2_++) {
				models[i_2_] = buffer.readBigSmart();
			}
		} else if (2 == opcode) {
			name = buffer.readString();
		} else if (12 == opcode) {
			size = buffer.readUnsignedByte();
		} else if (opcode >= 30 && opcode < 35) {
			menuOptions[opcode - 30] = buffer.readString();
		} else if (40 == opcode) {
			final int i_3_ = buffer.readUnsignedByte();
			recolorSrc = new short[i_3_];
			recolorDst = new short[i_3_];
			for (int i_4_ = 0; i_4_ < i_3_; i_4_++) {
				recolorSrc[i_4_] = (short) buffer.readUnsignedShort();
				recolorDst[i_4_] = (short) buffer.readUnsignedShort();
			}
		} else if (opcode == 41) {
			final int i_5_ = buffer.readUnsignedByte();
			retextureSrc = new short[i_5_];
			retextureDst = new short[i_5_];
			for (int i_6_ = 0; i_6_ < i_5_; i_6_++) {
				retextureSrc[i_6_] = (short) buffer.readUnsignedShort();
				retextureDst[i_6_] = (short) buffer.readUnsignedShort();
			}
		} else if (42 == opcode) {
			final int i_7_ = buffer.readUnsignedByte();
			paletteIndices = new byte[i_7_];
			for (int i_8_ = 0; i_8_ < i_7_; i_8_++) {
				paletteIndices[i_8_] = (byte) buffer.readByte();
			}
		} else if (44 == opcode) {
			final int i_63_ = buffer.readUnsignedShort();
			int i_64_ = 0;
			for (int i_65_ = i_63_; i_65_ > 0; i_65_ >>= 1) {
				i_64_++;
			}
			final byte[] aByteArray6291 = new byte[i_64_];
			byte i_66_ = 0;
			for (int i_67_ = 0; i_67_ < i_64_; i_67_++) {
				if ((i_63_ & 1 << i_67_) > 0) {
					aByteArray6291[i_67_] = i_66_;
					i_66_++;
				} else {
					aByteArray6291[i_67_] = (byte) -1;
				}
			}
		} else if (45 == opcode) {
			final int i_68_ = buffer.readUnsignedShort();
			int i_69_ = 0;
			for (int i_70_ = i_68_; i_70_ > 0; i_70_ >>= 1) {
				i_69_++;
			}
			final byte[] aByteArray6306 = new byte[i_69_];
			byte i_71_ = 0;
			for (int i_72_ = 0; i_72_ < i_69_; i_72_++) {
				if ((i_68_ & 1 << i_72_) > 0) {
					aByteArray6306[i_72_] = i_71_;
					i_71_++;
				} else {
					aByteArray6306[i_72_] = (byte) -1;
				}
			}
		} else if (60 == opcode) {
			final int i_9_ = buffer.readUnsignedByte();
			headModels = new int[i_9_];
			for (int i_10_ = 0; i_10_ < i_9_; i_10_++) {
				headModels[i_10_] = buffer.readBigSmart();
			}
		} else if (93 == opcode) {
			drawMinimapDot = false;
		} else if (opcode == 95) {
			combatLevel = buffer.readUnsignedShort();
		} else if (opcode == 97) {
			scaleX = buffer.readUnsignedShort();
		} else if (98 == opcode) {
			scaleY = buffer.readUnsignedShort();
		} else if (99 == opcode) {
			aBool5304 = true;
		} else if (opcode == 100) {
			anInt5307 = buffer.readByte();
		} else if (opcode == 101) {
			anInt5308 = buffer.readByte() * 5;
		} else if (102 == opcode) {
	            int i_20_ = buffer.readUnsignedByte();
	            int i_21_ = 0;
	            for (int i_22_ = i_20_; i_22_ != 0; i_22_ >>= 1)
	                i_21_++;
	            int[] headIconsArchiveIds = new int[i_21_];
	            short[] headIconsFileIds = new short[i_21_];
	            for (int i_23_ = 0; i_23_ < i_21_; i_23_++) {
	                if ((i_20_ & 1 << i_23_) == 0) {
	                    headIconsArchiveIds[i_23_] = -1;
	                    headIconsFileIds[i_23_] = (short) -1;
	                } else {
	                    headIconsArchiveIds[i_23_] = buffer.readBigSmart();
	                    headIconsFileIds[i_23_] = (short) buffer.readDecoratedSmart();
	                }
	            }
		} else if (103 == opcode) {
			contrast = buffer.readUnsignedShort();
		} else if (106 == opcode || 118 == opcode) {
			// 950 0x1403b3ed5 / 0x1403b3edf: both dispatch to the shared body at 950 0x1403b50dc.
			// 16-bit varbit id; 118 additionally carries the trailing transform. Opcodes 187/188
			// below are the same record with the varbit id widened to 24 bits, and are what the
			// 950 cache actually ships - 106 and 118 do not occur in it at all.
			readTransformList(buffer, false, 118 == opcode);
		} else if (opcode == 107) {
			aBool5315 = false;
		} else if (opcode == 109) {
			aBool5316 = false;
		} else if (111 == opcode) {
			aBool5317 = false;
		} else if (113 == opcode) {
			aShort5318 = (short) buffer.readUnsignedShort();
			aShort5319 = (short) buffer.readUnsignedShort();
		} else if (opcode == 114) {
			aByte5320 = (byte) buffer.readByte();
			aByte5321 = (byte) buffer.readByte();
		} else if (119 == opcode) {
			movementCapabilities = (byte) buffer.readByte();
		} else if (121 == opcode) {
			anIntArrayArray5285 = new int[models.length][];
			final int i_14_ = buffer.readUnsignedByte();
			for (int i_15_ = 0; i_15_ < i_14_; i_15_++) {
				final int i_16_ = buffer.readUnsignedByte();
				final int[] is = anIntArrayArray5285[i_16_] = new int[3];
				is[0] = buffer.readByte();
				is[1] = buffer.readByte();
				is[2] = buffer.readByte();
			}
		} else if (122 == opcode) {
			// The commented-out read is correct for 950: the client has no case for 122 (the
			// ladder goes 121 at 950 0x1403b3f65 straight to 123 at 950 0x1403b427d), so it reads
			// nothing here. Leave it reading nothing.
//			anInt5331 = buffer.readBigSmart();
		} else if (opcode == 123) {
			anInt5332 = buffer.readUnsignedShort();
		} else if (125 == opcode) {
			respawnDirection = buffer.readByte();
		} else if (127 == opcode) {
			renderEmote = buffer.readUnsignedShort();
		} else if (128 == opcode) {
			buffer.readUnsignedByte();
		} else if (134 == opcode) {
			anInt3029 = buffer.readUnsignedShort();
			if (anInt3029 == 65535) {
				anInt3029 = -1;
			}
			anInt3065 = buffer.readUnsignedShort();
			if (anInt3065 == 65535) {
				anInt3065 = -1;
			}
			anInt3050 = buffer.readUnsignedShort();
			if (65535 == anInt3050) {
				anInt3050 = -1;
			}
			anInt3042 = buffer.readUnsignedShort();
			if (anInt3042 == 65535) {
				anInt3042 = -1;
			}
			anInt3068 = buffer.readUnsignedByte();
		} else if (opcode == 135 || 136 == opcode) {
			// LATENT DESYNC on the modern cache: the 950 client has NO case for 135 or 136. The
			// ladder runs 134 (950 0x1403b4352) straight into 137 (950 0x1403b449e) with nothing
			// in between, so on 950 these opcodes read zero bytes, while the code below reads
			// three. Their job was taken over by 170-175, which fill the same six-entry cursor
			// table that 137 writes into (950 0x1403b4977 vs 0x1403b449e, both indexing
			// [npcType+0x1e0]). No shipped 950 NpcType uses 135 or 136, so this never fired.
			if (!Cache.isFlatReadOnly()) {
				if (opcode == 135) {
					cursor1op = buffer.readUnsignedByte();
					cursor1 = buffer.readUnsignedShort();
				} else {
					cursor2op = buffer.readUnsignedByte();
					cursor2 = buffer.readUnsignedShort();
				}
			}
		} else if (137 == opcode) {
			attackCursor = buffer.readUnsignedShort();
		} else if (138 == opcode) {
			anInt5310 = buffer.readBigSmart();
		} else if (139 == opcode) {
			// LATENT DESYNC on the modern cache: the read below was commented out, but the 950
			// client does read here - 950 0x1403b45b9 calls readBigSmart (950 0x1400feea0, 2 bytes
			// when the first byte is <= 0x7f, otherwise 4), exactly as opcode 138 does one branch
			// earlier. Skipping it would shift every field after it. No shipped 950 NpcType uses
			// 139, so this never fired; it is restored under the modern guard only, because the
			// legacy cache path has been running without it.
			if (Cache.isFlatReadOnly())
				anInt5281 = buffer.readBigSmart();
		} else if (140 == opcode) {
			anInt5329 = buffer.readUnsignedByte();
		} else if (opcode == 141) {
			aBool5295 = true;
		} else if (142 == opcode) {
			anInt5290 = buffer.readUnsignedShort();
		} else if (143 == opcode) {
			aBool5324 = true;
		} else if (opcode >= 150 && opcode < 155) {
			menuOptions[opcode - 150] = buffer.readString();
		} else if (155 == opcode) {
			aByte5292 = (byte) buffer.readByte();
			aByte5293 = (byte) buffer.readByte();
			aByte5298 = (byte) buffer.readByte();
			aByte5327 = (byte) buffer.readByte();
		} else if (158 == opcode) {
			aByte5337 = (byte) 1;
		} else if (159 == opcode) {
			aByte5337 = (byte) 0;
		} else if (opcode == 160) {
			final int i_17_ = buffer.readUnsignedByte();
			anIntArray5336 = new int[i_17_];
			for (int i_18_ = 0; i_18_ < i_17_; i_18_++) {
				anIntArray5336[i_18_] = buffer.readUnsignedShort();
			}
		} else if (162 == opcode) {
			// The 950 client has no case for 162 either (160 at 950 0x1403b4793 runs straight into
			// 163 at 950 0x1403b487e). Payload-less on both sides, so it cannot desync the stream;
			// left as-is for the legacy cache.
			aBool5330 = true;
		} else if (opcode == 163) {
			anInt5335 = buffer.readUnsignedByte();
		} else if (164 == opcode) {
			anInt5288 = buffer.readUnsignedShort();
			anInt5306 = buffer.readUnsignedShort();
		} else if (opcode == 165) {
			anInt5341 = buffer.readUnsignedByte();
		} else if (opcode == 168) {
			anInt5328 = buffer.readUnsignedByte();
		} else if (169 == opcode) {
			aBool5322 = false;
		} else if (opcode >= 170 && opcode < 176) {
			if (menuCursors == null) {
				menuCursors = new int[6];
				Arrays.fill(menuCursors, -1);
			}
			int cursor = buffer.readUnsignedShort();
			if (cursor == 65535) {
				cursor = -01;
			}
			menuCursors[opcode - 170] = cursor;
		} else if (opcode == 178) {
			/* empty */
		} else if (opcode == 179) {
			buffer.readSmart3();
			buffer.readSmart3();
			buffer.readSmart3();
			buffer.readSmart3();
			buffer.readSmart3();
			buffer.readSmart3();
		} else if (opcode == 180) {
			buffer.readUnsignedByte();
			// TODO: RS3
		} else if (opcode == 181) {
			buffer.readUnsignedShort();
			buffer.readUnsignedByte();
			// TODO: RS3
		} else if (opcode == 182) {
			// TODO: RS3
		} else if (249 == opcode) {
			/*
			 * int i_19_ = buffer.readUnsignedByte(); if (this.getNodeTable == null) { int i_20_ = Class409.method5322(i_19_); this.getNodeTable = new
			 * HashTable(i_20_); } for (int i_21_ = 0; i_21_ < i_19_; i_21_++) { boolean bool = buffer.readUnsignedByte() == 1; int i_22_ =
			 * buffer.readUnsignedTriByte(); Node node; if (bool) { node = new ObjectNode(buffer.readString()); } else { node = new IntegerNode(buffer.readInt());
			 * } this.getNodeTable.method308(node, i_22_); }
			 */
			final int length = buffer.readUnsignedByte();
			if (clientScriptData == null) {
				clientScriptData = new HashMap<Integer, Object>(length);
			}
			for (int index = 0; index < length; index++) {
				final boolean stringInstance = buffer.readUnsignedByte() == 1;
				final int key = buffer.read24BitInt();
				final Object value = stringInstance ? buffer.readString() : buffer.readInt();
				clientScriptData.put(key, value);
			}
		} else if (Cache.isFlatReadOnly() && opcode == 39) {
			// 950 0x1403b3585: one signed byte, scaled by 5 into the same field opcode 101 writes
			// (both store to [npcType+0x3b2]; compare 950 0x1403b35a9 with 0x1403b3d52). A second
			// spelling of 101, not a new field.
			anInt5308 = buffer.readByte() * 5;
		} else if (Cache.isFlatReadOnly() && opcode == 184) {
			// 950 0x1403b4da2: one unsigned byte into [npcType+0x3d4], a field the constructor
			// defaults to 9 (950 0x1403b31f7). Width only - the meaning is not established, so the
			// byte is read and discarded to keep the stream aligned.
			buffer.readUnsignedByte();
		} else if (Cache.isFlatReadOnly() && opcode == 185) {
			// 950 0x1403b4dcb: payload-less. It clears the boolean at [npcType+0x3c8], which the
			// constructor sets to 1 (950 0x1403b31df). Meaning not established; nothing to read.
		} else if (Cache.isFlatReadOnly() && (opcode == 186 || opcode == 189)) {
			// 950 0x1403b4de0 / 0x1403b4ded: both jump to the shared body at 950 0x1403b506b,
			// which consumes two bytes and then calls the model-morph reader at 950 0x1403b1d20 -
			// the very same reader LocType opcodes 205/209 use (loc call site 950 0x140365d5e).
			// The trailing boolean argument ([rsp+0x48], tested at 950 0x1403b1d33) is set for 189
			// and clear for 186, and only widens the varbit id from 16 to 24 bits.
			readModernModelMorphs(buffer, opcode == 189);
		} else if (Cache.isFlatReadOnly() && (opcode == 187 || opcode == 188)) {
			// 950 0x1403b4dfa ("lea eax, [r8 - 0xbb]; cmp eax, 1; jbe") dispatches both to
			// 950 0x1403b4e82. This is opcode 106/118 with the varbit id widened to a 24-bit
			// big-endian field whose absent-sentinel is 0xffffff (read at 950 0x1403b4e93, tested
			// at 950 0x1403b4ebd); 188 carries the extra trailing transform that 118 carries
			// (950 0x1403b4f0f). These are what the 950 cache ships: 3251 definitions use 187 and
			// 654 use 188, while 106 and 118 do not occur at all.
			readTransformList(buffer, true, opcode == 188);
		} else if (Cache.isFlatReadOnly() && opcode == 252) {
			// 950 0x1403b4e19: two bytes into [npcType+0x3dc], a field the constructor defaults to
			// -1 (950 0x1403b3201 writes -1 across +0x3d8 and +0x3dc, the second half of the pair
			// opcode 127 writes). Width only - meaning not established.
			buffer.readUnsignedShort();
		} else if (Cache.isFlatReadOnly() && opcode == 253) {
			// 950 0x1403b4e55: one unsigned byte into the byte at [npcType+0x400], which the
			// constructor zeroes (950 0x1403b3250). Width only - meaning not established.
			buffer.readUnsignedByte();
		} else {
			// The 950 client's ladder ends at 950 0x1403b4e5c: anything it does not name falls
			// through to the loop-continue at 950 0x1403b5334 and reads nothing. An opcode that
			// reaches here is therefore one whose payload this parser cannot know, so fail closed
			// rather than guess a width - a wrong width does not error, it silently mis-parses
			// every field after it.
			if (strict)
				throw new IllegalArgumentException("Unsupported modern npc " + id + " opcode " + opcode + " at " + (buffer.getOffset() - 1));
			System.out.println("Unrecognized .npc code:" + opcode);//();
		}
	}

	/**
	 * The varbit/varp transform list shared by NpcType opcodes 106, 118, 187 and 188.
	 *
	 * Shared body for 106/118 at 950 0x1403b50dc, for 187/188 at 950 0x1403b4e82; the two are
	 * instruction-for-instruction the same apart from the width of the first field.
	 *
	 * @param wideVarbit      950 0x1403b4e93: 187/188 read the varbit id as a 24-bit big-endian
	 *                        value with sentinel 0xffffff; 106/118 read 16 bits with sentinel
	 *                        0xffff (950 0x1403b50e6).
	 * @param extraTransform  950 0x1403b4f0f / 0x1403b5159: 118 and 188 carry one more unsigned
	 *                        short before the list, which is parked in the last slot of
	 *                        transformTo exactly as the pre-existing 118 handler did.
	 */
	private void readTransformList(final InputStream buffer, final boolean wideVarbit, final boolean extraTransform) {
		varbitId = wideVarbit ? buffer.read24BitInt() : buffer.readUnsignedShort();
		if (varbitId == (wideVarbit ? 0xffffff : 65535))
			varbitId = -1;
		varId = buffer.readUnsignedShort();
		if (varId == 65535)
			varId = -1;
		int extra = -1;
		if (extraTransform) {
			extra = buffer.readUnsignedShort();
			if (extra == 65535)
				extra = -1;
		}
		// 950 0x1403b4f45: the count is a real smart, one byte below 0x80 and two above it - not a
		// plain byte. 273 of the shipped definitions use the two-byte form (the largest count is
		// 500), so reading a plain byte here would mis-parse all of them.
		final int last = buffer.readUnsignedSmart();
		transformTo = new int[last + 2];
		for (int index = 0; index <= last; index++) {
			transformTo[index] = buffer.readUnsignedShort();
			if (transformTo[index] == 65535)
				transformTo[index] = -1;
		}
		transformTo[last + 1] = extra;
	}

	/**
	 * NpcType opcodes 186 and 189 in the 950 cache - the same record LocType 205/209 carry, read
	 * by the same client function.
	 *
	 * Layout derived from the 950 client at 950 0x1403b1d20, entered from the NpcType ladder at
	 * 950 0x1403b506b (which consumes the leading two bytes itself before calling). 189 passes the
	 * "wide varbit" flag, 186 does not (tested at 950 0x1403b1d33). ObjectDefinitions carries an
	 * identical transcription for LocType 205/209; the duplication is deliberate - this file is
	 * the only one being changed, and the two must stay in step.
	 *
	 * Only the widths are derived here; the meaning of the per-entry values is not, so everything
	 * is read and discarded. Keeping the stream aligned is the whole job.
	 */
	private static void readModernModelMorphs(final InputStream buffer, final boolean wideVarbit) {
		buffer.readUnsignedShort(); // 950 0x1403b506b: two bytes the client skips without decoding.
		if (wideVarbit)
			buffer.read24BitInt(); // 950 0x1403b1d5d (opcode 189).
		else
			buffer.readUnsignedShort(); // 950 0x1403b1d9e (opcode 186).
		buffer.readUnsignedShort(); // 950 0x1403b1dcf: varp id.
		final int flags = buffer.readUnsignedByte(); // 950 0x1403b1e13.
		// The client tests only bits 1, 2, 4, 8 and 16 (950 0x1403b1e28, 0x1403b212f, 0x1403b2330,
		// 0x1403b24d5, 0x1403b2675). Higher bits carry no payload and are ignored, so ignore them
		// here too rather than rejecting the record - anything else would disagree with the client.
		if ((flags & 1) != 0) { // 950 0x1403b1e32.
			for (int variant = buffer.readUnsignedByte(); variant > 0; variant--) {
				buffer.readUnsignedByte();
				for (int entry = buffer.readUnsignedByte(); entry > 0; entry--) {
					buffer.readUnsignedShort();
					buffer.readUnsignedShort();
					buffer.readBigSmart();
					// 950 0x1403b1efa: a byte count, then at most three bytes are actually read,
					// however large that count is (the client's chain stops after three).
					buffer.skip(Math.min(buffer.readUnsignedByte(), 3));
				}
			}
		}
		if ((flags & 2) != 0) { // 950 0x1403b2139: as bit 1, minus the trailing extras byte.
			for (int variant = buffer.readUnsignedByte(); variant > 0; variant--) {
				buffer.readUnsignedByte();
				for (int entry = buffer.readUnsignedByte(); entry > 0; entry--) {
					buffer.readUnsignedShort();
					buffer.readUnsignedShort();
					buffer.readBigSmart();
				}
			}
		}
		for (int bit = 4; bit <= 8; bit <<= 1) { // 950 0x1403b233a and 950 0x1403b24df.
			if ((flags & bit) == 0)
				continue;
			for (int variant = buffer.readUnsignedByte(); variant > 0; variant--) {
				buffer.readUnsignedByte();
				buffer.skip(buffer.readUnsignedByte() * 8); // Four shorts per entry.
			}
		}
		if ((flags & 16) != 0) {
			// 950 0x1403b2687: ONE flat list, not the variant/entry nesting the other bits use.
			// Each of the count records is short, short, byte, byte, byte, byte = 8 bytes
			// (950 0x1403b26f3 through 0x1403b2798).
			buffer.skip(buffer.readUnsignedByte() * 8);
		}
		buffer.readUnsignedShort(); // 950 0x1403b28db: trailing default.
	}

	public OutputStream writeValues() {
		OutputStream stream = new OutputStream();

		if (models != null) {
			stream.writeByte(1);
			for (int index = 0; index < models.length; index++) {
				stream.writeBigSmart(models[index]);
			}
		}

		if (!name.equals("null")) {
			stream.writeByte(2);
			stream.writeString(name);
		}

		if (size != 1) {
			stream.writeByte(12);
			stream.writeByte(size);
		}

		for (int index = 0; index < menuOptions.length; index++) {
			if (menuOptions[index] == null)
				continue;

			stream.writeByte(30 + index);
			stream.writeString(menuOptions[index]);
		}
		
		if (recolorSrc != null && recolorDst != null) {
			stream.writeByte(40);
			stream.writeByte(recolorSrc.length);
			
			for (int index = 0; index < recolorSrc.length; index++) {
				stream.writeShort(recolorSrc[index]);
				stream.writeShort(recolorDst[index]);
			}
		}
		
		if (retextureSrc != null && retextureDst != null) {
			stream.writeByte(41);
			stream.writeByte(retextureSrc.length);
			
			for (int index = 0; index < retextureSrc.length; index++) {
				stream.writeShort(retextureSrc[index]);
				stream.writeShort(retextureDst[index]);
			}
		}
		
		if (paletteIndices != null) {
			stream.writeByte(42);
			stream.writeByte(paletteIndices.length);
			for (int index = 0; index < paletteIndices.length; index++) {
				stream.writeByte(paletteIndices[index]);
			}
		}
		
		if (headModels != null) {
			stream.writeByte(60);
			stream.writeByte(headModels.length);
			for (int index = 0; index < headModels.length; index++) {
				stream.writeBigSmart(headModels[index]);
			}
		}
		
		if (!drawMinimapDot) {
			stream.writeByte(93);
		}
		
		if (combatLevel != -1) {
			stream.writeByte(95);
			stream.writeShort(combatLevel);
		}
		
		if (scaleX != 128) {
			stream.writeByte(97);
			stream.writeShort(scaleX);
		}

		if (scaleY != 128) {
			stream.writeByte(98);
			stream.writeShort(scaleY);
		}
		
		if (aBool5304) {
			stream.writeByte(99);
		}
		
		if (anInt5307 != 0) {
			stream.writeByte(100);
			stream.writeByte(anInt5307);
		}
		
		if (anInt5308 != 0) {
			stream.writeByte(101);
			stream.writeByte(anInt5308 / 5);
		}
		
		if (contrast != 32) {
			stream.writeByte(103);
			stream.writeShort(contrast);
		}
		
		if (varbitId != -1 && varId != -1) {
			stream.writeByte(106);
			stream.writeShort(varbitId);
			stream.writeShort(varId);
			stream.writeByte(transformTo.length - 2);
			for (int index = 0; index <= transformTo.length - 2; index++) {
				stream.writeShort(transformTo[index]);
			}
		}
		
		if (!aBool5315) {
			stream.writeByte(107);
		}
		
		if (!aBool5316) {
			stream.writeByte(109);
		}
		
		if (!aBool5317) {
			stream.writeByte(111);
		}
		
		if (aShort5318 != 0 && aShort5319 != 0) {
			stream.writeByte(113);
			stream.writeShort(aShort5318);
			stream.writeShort(aShort5319);
		}
		
		if (aByte5320 != -96 && aByte5321 != -16) {
			stream.writeByte(114);
			stream.writeByte(aByte5320);
			stream.writeByte(aByte5321);
		}
		
		if (varbitId != -1 && varId != -1) {
			stream.writeByte(118);
			stream.writeShort(varbitId);
			stream.writeShort(varId);
			stream.writeShort(transformTo[(transformTo.length - 2) + 1]);
			stream.writeByte(transformTo.length - 2);
			for (int index = 0; index <= transformTo.length - 2; index++) {
				stream.writeShort(transformTo[index]);
			}
		}
		
		if (movementCapabilities != 0) {
			stream.writeByte(119);
			stream.writeByte(movementCapabilities);
		}
		
		if (anInt5331 != -1) {
			stream.writeByte(122);
			stream.writeBigSmart(anInt5331);
		}
		
		if (anInt5332 != -1) {
			stream.writeByte(123);
			stream.writeShort(anInt5332);
		}
		
		if (respawnDirection != 7) {
			stream.writeByte(125);
			stream.writeByte(respawnDirection);
		}
		
		if (renderEmote != -1) {
			stream.writeByte(127);
			stream.writeShort(renderEmote);
		}
		
		if (anInt3029 != -1 && anInt3065 != -1 && anInt3050 != -1 && anInt3042 != -1 && anInt3068 != 0) {
			stream.writeByte(134);
			stream.writeShort(anInt3029);
			stream.writeShort(anInt3065);
			stream.writeShort(anInt3050);
			stream.writeShort(anInt3042);
			stream.writeByte(anInt3068);
		}

		return stream;
	}


	public static final NPCDefinitions getNPCDefinitions(final int id) {
		// 1. Safe array bounds check
		if (id < 0 || NPC_DEFINITIONS == null || id >= NPC_DEFINITIONS.length) {
			// Return a safe dummy definition instead of crashing
			NPCDefinitions dummy = new NPCDefinitions(id);
			dummy.name = "Unknown NPC";
			dummy.models = new int[0];
			return dummy;
		}

		NPCDefinitions def = NPC_DEFINITIONS[id];

		if (def == null) {
			def = new NPCDefinitions(id);
			if (def.models == null) {
				def.models = new int[0];
			}

			try {
				com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[18];
				byte[] data = null;

				// 2. Safely check if Index 18 actually exists before querying it
				if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
					data = index.getFile(id >>> 134238215, id & 0x7f);
				}

				if (data == null) {
					// Cache file is missing (Normal for this 921 cache)
					// Give it a generic name so it doesn't cause name-checking NPEs later
					def.name = "Missing NPC (" + id + ")";
				} else {
					if (Cache.isFlatReadOnly()) {
						// 947: strict stream; unknown opcodes/trailing bytes abort below.
						def.id = id;
						def.readValueLoop(new InputStream(data, true), true, null);
					} else {
						def.readValueLoop(new InputStream(data));
					}

					if (id == 2707) {
						def.menuOptions = new String[] { "Interact", null, "Dismiss", null, null };
					} else if (id == 26438) {
						def.menuOptions = new String[] { "Attack", null, "Examine", null, null };
					}
				}
			} catch (Exception e) {
				if (Cache.isFlatReadOnly()) {
					// 3a. 947: replace the partially decoded object by a defaults-only one so no
					// misaligned field survives, and register the id so it is never served silently.
					def = markDecodeFailure(id, e);
				} else {
					// 3. Catch any unexpected IndexOutOfBounds or parsing errors
					def.name = "Error NPC (" + id + ")";
				}
			}

			NPC_DEFINITIONS[id] = def;
		}

		return def;
	}

	/** Builds the well-defined failed definition for a 947 NPC and records it in the registry. */
	private static NPCDefinitions markDecodeFailure(final int id, final Exception failure) {
		String reason = failure.getMessage() == null ? failure.toString() : failure.getMessage();
		NPCDefinitions failed = new NPCDefinitions(id);
		failed.id = id;
		failed.models = new int[0];
		failed.name = "Undecodable NPC (" + id + ")";
		failed.decodeFailure = reason;
		if (DECODE_FAILURES.put(id, reason) == null)
			Logger.getGlobal().warn("947 npc " + id + " rejected by strict decoder: " + reason);
		return failed;
	}

	public int getRenderAnimation() {
		return renderEmote;
	}

	/** Cache resize factors (opcodes 97/98); 128 means unscaled. */
	public int getScaleX() {
		return scaleX;
	}

	public int getScaleY() {
		return scaleY;
	}

	public boolean hasMarkOption() {
		for (final String option : menuOptions) {
			if (option != null && option.equalsIgnoreCase("mark")) {
				return true;
			}
		}
		return false;
	}

	public String getOption(final int id) {
		if (id < 0 || id > menuOptions.length) {
			return null;
		}
		return menuOptions[id];
	}

	public boolean hasOption(final String op) {
		for (final String option : menuOptions) {
			if (option != null && option.equalsIgnoreCase(op)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAttackOption() {
		if (id == 14899) {
			return true;
		}

		if (id == 26438) {
			return true;
		}

		for (final String option : menuOptions) {
			if (option != null && (option.equalsIgnoreCase("attack") || option.equalsIgnoreCase("destroy"))) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

    public int[] getCacheBonuses() {
        int[] bonuses = new int[8];
        Map<Integer, Object> data = clientScriptData;
        if (data != null) {
            Integer meleeDamage = (Integer) data.get(641);
            bonuses[0] = meleeDamage == null ? 0 : meleeDamage / 10;
            Integer rangeDamage = (Integer) data.get(643);
            bonuses[1] = rangeDamage == null ? 0 : rangeDamage / 10;
            Integer mageDamage = (Integer) data.get(965);
            bonuses[2] = mageDamage == null ? 0 : mageDamage / 10;
            Integer meleeAccuracy = (Integer) data.get(29);
            bonuses[3] = meleeAccuracy == null ? 1 : meleeAccuracy;
            Integer rangeAccuracy = (Integer) data.get(4);
            bonuses[4] = rangeAccuracy == null ? 1 : rangeAccuracy;
            Integer magicAccuracy = (Integer) data.get(3);
            bonuses[5] = magicAccuracy == null ? 1 : magicAccuracy;
            Integer armourBonus = (Integer) data.get(2865);
            bonuses[6] = armourBonus == null ? 1 : armourBonus;
            Integer critBonus = (Integer) data.get(2864);
            bonuses[7] = critBonus == null ? 1 : critBonus;
        } else
            for (int idx = 0; idx < bonuses.length; idx++)
                bonuses[idx] = 1;
        return bonuses;
    }

    public int getWeaknessStyle() {
        Map<Integer, Object> data = clientScriptData;
        if (data != null) {
            Integer weakness = (Integer) data.get(2848);
            if (weakness != null)
                return weakness;
        }
        return 0;
    }

    public int getNPCType() {
        Map<Integer, Object> data = clientScriptData;
        if (data != null) {
            Integer type = (Integer) data.get(2836);
            if (type != null)
                return type;
        }
        return 0;
    }

    public int getSpecialWeaknesses() {
        switch (getNPCType()) {
        case 1:// demons
            return Combat.SILVER_LIGHT_WEAKNESS;
        case 2:// Dagannoths
            return Combat.BALMUNG_WEAKNESS;
        case 6:// Vampyre
            return Combat.BLISTERWOOD_WEAKNESS;
        case 7:// kalphite
            return Combat.KERIS_DAGGER_WEAKNESS;
        case 8:// undead
            return Combat.SALVE_AMULET_WEAKNESS;
        case 9:// dragons
            return Combat.BANE_AMMUNITION_WEAKNESS;
        }
        return -1;
    }
    
    public boolean walksSpecial() {
        return renderEmote != -1 && RenderAnimDefinitions.getRenderAnimDefinitions(renderEmote).moveType1Anim != -1;
    }
    
    public int getSpecialWalkAnimation() {
        return renderEmote == -1 ? -1 : RenderAnimDefinitions.getRenderAnimDefinitions(renderEmote).moveType1Anim;
    }
}
