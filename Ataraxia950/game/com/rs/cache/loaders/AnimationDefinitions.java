package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationDefinitions {

	private static final ConcurrentHashMap<Integer, AnimationDefinitions> animDefs = new ConcurrentHashMap<Integer, AnimationDefinitions>();
	public int anInt2136;
	public int anInt2137;
	public int[] anIntArray2139;
	public int anInt2140;
	public boolean aBoolean2141 = false;
	public int anInt2142;
	public int leftHandItem;
	public int rightHandItem = -1;
	public int[][] handledSounds;
	public boolean[] aBooleanArray2149;
	public int[] anIntArray2151;
	public boolean aBoolean2152;
	public int[] anIntArray2153;
	public int anInt2155;
	public boolean aBoolean2158;
	public boolean aBoolean2159;
	public int anInt2162;
	public int anInt2163;
	// added
	public int[] soundMinDelay;
	public int[] soundMaxDelay;
	public int[] anIntArray1362;
	public boolean effect2Sound;
	public int id;
	public HashMap<Integer, Object> clientScriptData;

	// --- Fields that exist only on the 950 SeqType. Every offset below is a field of the 950
	// client's SeqType object; the defaults are the ones its constructor writes at
	// 950 0x1402e5500 .. 0x1402e55b8. The 950 addresses that decode them are cited on each
	// opcode in readValues. Meanings are deliberately not guessed - see the javadoc there.
	/** 950 opcode 25 -> SeqType +0xa40 (unsigned short). Client default -1 (950 0x1402e5565). */
	public int modernInt25 = -1;
	/** 950 opcode 26, first unsigned short -> SeqType +0xa58. Client default 0 (950 0x1402e5585). */
	public int modernInt26a;
	/**
	 * 950 opcode 26, second unsigned short -> SeqType +0xa5c. Client default 0 (950 0x1402e5529).
	 * After decoding, the client adds the sum of every frame duration to it (950 0x14035af4a),
	 * so it behaves like a base for the animation's total length.
	 */
	public int modernInt26b;
	/**
	 * 950 opcode 27 -> SeqType +0xab0, read with movsx, i.e. a SIGNED byte (950 0x14035aaac).
	 * Client default -1 (950 0x1402e55ae). The only values in the shipped 950 cache are -1 (37x)
	 * and -2 (553x), which is independent confirmation of the sign: unsigned they would be 255/254.
	 */
	public int modernInt27 = -1;

	public AnimationDefinitions() {
		anInt2136 = 99;
		leftHandItem = -1;
		anInt2140 = -1;
		aBoolean2152 = false;
		anInt2142 = 5;
		aBoolean2159 = false;
		anInt2163 = -1;
		anInt2155 = 2;
		aBoolean2158 = false;
		anInt2162 = -1;
	}
	
	public static String findDesciption(int id) {
        String[] description;
        BufferedReader br = null;
        String temp;
        try {
            br = new BufferedReader(new FileReader(new File("C:/Users/Kris/Desktop/860 anims.txt")));
        } catch (FileNotFoundException io) {
            Logger.getGlobal().info("File not found.");
        }
        try {
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("anim " + id + " ")) {
                    description = temp.split(" - ");
                    if (description.length > 1) {
                    	Logger.getGlobal().info("Anim: " + id + " - " +  description[1]);
                    	return "Anim " + id +  " - " + description[1];
                    } else
                    	return "Not a player animation!";
                } else if (temp.startsWith("animGFX " + id + " ")) {
                    temp.split("animGFX " + id);
                    description = temp.split(" ");
                    int graphicID = Integer.parseInt(description[2]);
                    description = temp.split("- ");
                    Logger.getGlobal().info("AnimGFX " + id + " " + graphicID + " - " + description[1]);
                    return "AnimGFX " + id + " " + graphicID + " - " + description[1];
                }
            }
        } catch (IOException ioex) {
            System.out.print("Error loading file.");
        }
        return "Not a player animation!";
    }

	/** Non-null when the strict modern (950) decode of this id failed; the definition then only holds defaults. */
	public String decodeFailure;

	/** Ids whose modern (950) cache file exists but could not be decoded strictly (id -> reason). */
	private static final ConcurrentHashMap<Integer, String> DECODE_FAILURES = new ConcurrentHashMap<Integer, String>();

	/** Every modern (950) animation that failed strict decoding so far (unknown opcode, truncation, trailing bytes). */
	public static Map<Integer, String> getDecodeFailures() {
		return Collections.unmodifiableMap(DECODE_FAILURES);
	}

	/** Reason the given animation failed strict modern (950) decoding, or null. */
	public static String getDecodeFailure(int emoteId) {
		return DECODE_FAILURES.get(emoteId);
	}

	/**
	 * Strict decode of a raw index-20 file (probe/tests): throws IllegalArgumentException
	 * ("... opcode N at offset" / "Trailing bytes ...") instead of silently skipping.
	 *
	 * <p>The "947" in the name is historical - this is the MODERN path, i.e. the one
	 * {@link Cache#isFlatReadOnly()} selects, and its width table was re-derived against the
	 * <b>950</b> client (see {@link #readValues}). The name is kept only so the existing tests
	 * and ModernDefinitionSweep keep compiling.
	 *
	 * <p>950 opcodes 3, 25, 26, 27, 112, 119 and 120 are now decoded from the client's own
	 * ladder. Anything the ladder has no case for - 4, 17, 21, 255 and the rest - is still
	 * refused rather than guessed, and aborts the definition.
	 *
	 * <p>When trace is non-null every opcode is appended as "opcode@offset ".
	 */
	public static AnimationDefinitions decodeStrict947(int emoteId, byte[] data, StringBuilder trace) {
		AnimationDefinitions defs = new AnimationDefinitions();
		defs.id = emoteId;
		defs.readValueLoop(new InputStream(data, true), true, trace);
		defs.method2394();
		return defs;
	}

	public static final AnimationDefinitions getAnimationDefinitions(int emoteId) {
		try {
			AnimationDefinitions defs = animDefs.get(emoteId);
			if (defs != null)
				return defs;
			byte[] data = Cache.STORE.getIndexes()[20].getFile(emoteId >>> 7, emoteId & 0x7f);
			defs = new AnimationDefinitions();
			defs.id = emoteId;
			if (data != null) {
				if (Cache.isFlatReadOnly())
					defs.readValueLoop(new InputStream(data, true), true, null); // 950 flat cache: fail closed
				else
					defs.readValueLoop(new InputStream(data));
			}
			defs.method2394();
			animDefs.put(emoteId, defs);
			return defs;
		} catch (Throwable t) {
			if (Cache.isFlatReadOnly() && !(t instanceof VirtualMachineError)) {
				// 950 flat cache: serve a defaults-only definition that carries the failure instead of
				// null (legacy contract) or a misaligned partial decode.
				AnimationDefinitions failed = markDecodeFailure(emoteId, t);
				animDefs.put(emoteId, failed);
				return failed;
			}
			return null;
		}
	}

	/** Builds the well-defined failed definition for a modern (950) animation and records it in the registry. */
	private static AnimationDefinitions markDecodeFailure(int emoteId, Throwable failure) {
		String reason = failure.getMessage() == null ? failure.toString() : failure.getMessage();
		AnimationDefinitions failed = new AnimationDefinitions();
		failed.id = emoteId;
		failed.method2394();
		failed.decodeFailure = reason;
		if (DECODE_FAILURES.put(emoteId, reason) == null)
			Logger.getGlobal().warn("modern animation " + emoteId + " rejected by strict decoder: " + reason);
		return failed;
	}

	private void readValueLoop(InputStream stream) {
		readValueLoop(stream, false, null);
	}

	private void readValueLoop(InputStream stream, boolean strict, StringBuilder trace) {
		for (;;) {
			int offset = stream.getOffset();
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			if (trace != null)
				trace.append(opcode).append('@').append(offset).append(' ');
			readValues(stream, opcode, strict);
		}
		if (strict && stream.getRemaining() != 0)
			throw new IllegalArgumentException("Trailing bytes in modern animation " + id + " at " + stream.getOffset());
	}

	/** Native950 frame durations use the proven 20ms client clock. Opcode26 adds its duration base. */
	public int getNative950EmoteTime() {
		long cycles = Math.max(0, modernInt26b);
		if (anIntArray2153 != null) for (int frame : anIntArray2153) cycles += frame;
		return (int)Math.min(Integer.MAX_VALUE, cycles * 20L);
	}

	public int getEmoteTime() {
		if (anIntArray2153 == null)
			return 0;
		int ms = 0;
		for (int i : anIntArray2153)
			ms += i;
		return ms * 10;
	}

	public int getEmoteClientCycles() {
		if (anIntArray2153 == null)
			return 0;
		int r = 0;
		for (int i = 0; i < anIntArray2153.length - 3; i++) {
			r += anIntArray2153[i];
		}
		return r;
	}

	/*
	 * private void readValues(InputStream stream, int opcode) { if ((opcode ^
	 * 0xffffffff) == -2) { int i = stream.readUnsignedShort(); anIntArray2153 =
	 * new int[i]; for (int i_16_ = 0; (i ^ 0xffffffff) < (i_16_ ^ 0xffffffff);
	 * i_16_++) anIntArray2153[i_16_] = stream.readUnsignedShort();
	 * anIntArray2139 = new int[i]; for (int i_17_ = 0; (i_17_ ^ 0xffffffff) >
	 * (i ^ 0xffffffff); i_17_++) anIntArray2139[i_17_] =
	 * stream.readUnsignedShort(); for (int i_18_ = 0; i_18_ < i; i_18_++)
	 * anIntArray2139[i_18_] = ((stream.readUnsignedShort() << 16) +
	 * anIntArray2139[i_18_]); } else if ((opcode ^ 0xffffffff) != -3) { if
	 * ((opcode ^ 0xffffffff) != -4) { if ((opcode ^ 0xffffffff) == -5)
	 * aBoolean2152 = true; else if (opcode == 5) anInt2142 =
	 * stream.readUnsignedByte(); else if (opcode != 6) { if ((opcode ^
	 * 0xffffffff) == -8) leftHandItem = stream.readUnsignedShort(); else if
	 * ((opcode ^ 0xffffffff) != -9) { if (opcode != 9) { if ((opcode ^
	 * 0xffffffff) != -11) { if ((opcode ^ 0xffffffff) == -12) anInt2155 =
	 * stream.readUnsignedByte(); else if (opcode == 12) { int i =
	 * stream.readUnsignedByte(); anIntArray2151 = new int[i]; for (int i_19_ =
	 * 0; ((i_19_ ^ 0xffffffff) > (i ^ 0xffffffff)); i_19_++)
	 * anIntArray2151[i_19_] = stream.readUnsignedShort(); for (int i_20_ = 0; i
	 * > i_20_; i_20_++) anIntArray2151[i_20_] = ((stream.readUnsignedShort() <<
	 * 16) + anIntArray2151[i_20_]); } else if ((opcode ^ 0xffffffff) != -14) {
	 * if (opcode != 14) { if (opcode != 15) { if (opcode == 16) aBoolean2158 =
	 * true; // added opcode else if (opcode == 17) {
	 * 
	 * @SuppressWarnings("unused") int anInt2145 = stream.readUnsignedByte(); //
	 * added opcode } else if (opcode == 18) { effect2Sound = true; } else if
	 * (opcode == 19) { if (anIntArray1362 == null) { anIntArray1362 = new
	 * int[handledSounds.length]; for (int index = 0; index <
	 * handledSounds.length; index++) anIntArray1362[index] = 255; }
	 * anIntArray1362[stream.readUnsignedByte()] = stream.readUnsignedByte(); //
	 * added opcode } else if (opcode == 20) { if ((soundMaxDelay == null) ||
	 * (soundMinDelay == null)) { soundMaxDelay = (new
	 * int[handledSounds.length]); soundMinDelay = (new
	 * int[handledSounds.length]); for (int i_34_ = 0; (i_34_ <
	 * handledSounds.length); i_34_++) { soundMaxDelay[i_34_] = 256;
	 * soundMinDelay[i_34_] = 256; } } int index = stream.readUnsignedByte();
	 * soundMaxDelay[index] = stream.readUnsignedShort(); soundMinDelay[index] =
	 * stream.readUnsignedShort(); } else if (opcode == 22) { //rs3 added
	 * stream.readUnsignedByte(); //idk } else if (opcode == 23) { //rs3 added
	 * stream.readUnsignedByte(); //unused } else if (opcode == 24) { //rs3
	 * added stream.readUnsignedShort(); //idk, calls for data from cache. idx
	 * 2, 77 } else if (opcode == 249) { int length = stream.readUnsignedByte();
	 * if (clientScriptData == null) clientScriptData = new HashMap<Integer,
	 * Object>(length); for (int index = 0; index < length; index++) { boolean
	 * stringInstance = stream.readUnsignedByte() == 1; int key =
	 * stream.read24BitInt(); Object value = stringInstance ?
	 * stream.readString() : stream.readInt(); clientScriptData.put(key, value);
	 * } }
	 * 
	 * } else aBoolean2159 = true; } else aBoolean2141 = true; } else { //
	 * opcode 13 int i = stream.readUnsignedShort(); handledSounds = new
	 * int[i][]; for (int i_21_ = 0; i_21_ < i; i_21_++) { int i_22_ =
	 * stream.readUnsignedByte(); if ((i_22_ ^ 0xffffffff) < -1) {
	 * handledSounds[i_21_] = new int[i_22_]; handledSounds[i_21_][0] =
	 * stream.readUnsignedShort(); for (int i_23_ = 1; ((i_22_ ^ 0xffffffff) <
	 * (i_23_ ^ 0xffffffff)); i_23_++) { handledSounds[i_21_][i_23_] =
	 * stream.readUnsignedShort(); } } } } } else anInt2162 =
	 * stream.readUnsignedByte(); } else anInt2140 = stream.readUnsignedByte();
	 * } else anInt2136 = stream.readUnsignedByte(); } else rightHandItemId =
	 * stream.readUnsignedShort(); } else { aBooleanArray2149 = new
	 * boolean[256]; int i = stream.readUnsignedByte(); for (int i_24_ = 0; (i ^
	 * 0xffffffff) < (i_24_ ^ 0xffffffff); i_24_++)
	 * aBooleanArray2149[stream.readUnsignedByte()] = true; } } else anInt2163 =
	 * stream.readUnsignedShort(); }
	 */

	/**
	 * SeqType (cache index 20) field decoder.
	 *
	 * <p><b>strict = true means the modern flat cache</b>, i.e. the 950 one - it is set exactly
	 * when {@link Cache#isFlatReadOnly()} is true. (The "947" in the surrounding method names is
	 * historical: this loader was ported from the 947 project and the names were kept so the
	 * existing tests and the sweep still compile. Everything below was re-derived against the
	 * <b>950</b> client.) strict rejects an opcode whose layout is not established instead of
	 * skipping it, because a skipped opcode silently desynchronises every following field.
	 *
	 * <p>The 950 widths come from the client's own SeqType decoder, a cmp/jne opcode ladder that
	 * is one function occupying <b>950 0x14035a380 .. 0x14035aeed</b> (arguments: rcx = SeqType,
	 * rdx = buffer, r8d = opcode; the opcode is copied to ebp and the buffer to rdi at
	 * 950 0x14035a396). The buffer's read cursor is the qword at buffer+0x18 and its base is the
	 * qword at buffer+0x10, so every "lea rax, [rcx + n]; mov [rdi+0x18], rax" in that ladder is
	 * an n-byte field. Individual opcodes are cited inline.
	 * NOTE: 947 addresses do not carry over - they disassemble cleanly in the 950 binary but land
	 * on unrelated code, so every address in this file was re-derived against the 950 image.
	 *
	 * <p>The complete opcode set the 950 client accepts is:
	 *   1 2 3 5 6 7 8 9 10 11 12 13 14 15 16 18 19 20 22 23 24 25 26 27 112 119 120 249.
	 * That ladder is exhaustive - it is a single function, read end to end - so in the 950 client
	 * every other opcode falls through to the epilogue at 950 0x14035aecb and consumes no bytes
	 * at all. This decoder still refuses them rather than copying that, because an opcode the
	 * encoder wrote a payload for would then desync the rest of the record.
	 *
	 * <p>Of the accepted set the shipped 950 cache uses all except 3, 19, 20 and 23. 19 and 20
	 * have been fully replaced by their wide-index forms 119 and 120; 3 and 23 do not occur at
	 * all, so their (derived) branches below are never exercised by shipped data.
	 *
	 * <p>Verification: all 38,084 SeqType definitions in the 950 cache were parsed with this exact
	 * width table; every one terminates on opcode 0 at exactly its byte length, with no unknown
	 * opcode and no over- or under-read. Each newly derived width was also perturbed by +-1 byte
	 * and every perturbation breaks thousands of records, so the data pins the widths as well as
	 * the disassembly does.
	 */
	private void readValues(InputStream buffer, int opcode, boolean strict) {
		if (opcode == 1) {
			// 950 0x14035a3a9: an unsigned short count, then three passes of count unsigned
			// shorts - durations (950 0x14035a460), then the low half (950 0x14035a520) and the
			// high half (950 0x14035a560) of a packed int. 2 + 6n bytes.
			int i_3_ = buffer.readUnsignedShort();
			anIntArray2153 = new int[i_3_];
			for (int i_4_ = 0; i_4_ < i_3_; i_4_++)
				anIntArray2153[i_4_] = buffer.readUnsignedShort();
			anIntArray2139 = new int[i_3_];
			for (int i_5_ = 0; i_5_ < i_3_; i_5_++)
				anIntArray2139[i_5_] = buffer.readUnsignedShort();
			for (int i_6_ = 0; i_6_ < i_3_; i_6_++)
				anIntArray2139[i_6_] = (buffer.readUnsignedShort() << 16) + anIntArray2139[i_6_];
		} else if (opcode == 2)
			// 950 0x14035a5a0: one unsigned short into SeqType +0xa44, whose client default is -1
			// (950 0x1402e5565) - not the 99 this class initialises anInt2136 to. See the note on
			// opcodes 8/11 below: 99 is opcode 8's default, not opcode 2's.
			anInt2136 = buffer.readUnsignedShort();
		else if (5 == opcode)
			anInt2142 = buffer.readUnsignedByte(); // 950 0x14035a656: one byte, SeqType +0xa48.
		else if (opcode == 6)
			rightHandItem = strict ? readModernItemRef(buffer) : buffer.readBigSmart();
		else if (opcode == 7)
			leftHandItem = strict ? readModernItemRef(buffer) : buffer.readBigSmart();
		// LATENT (width is right, destination is not): 950 opcode 8 writes SeqType +0xa54
		// (950 0x14035a6ff, client default 99 at 950 0x1402e5585) and 950 opcode 11 writes
		// SeqType +0xa68 (950 0x14035a76e, client default 2 at 950 0x1402e55a4) - two DIFFERENT
		// fields. Both land in anInt2155 here, whose default of 2 is opcode 11's, so in the 123
		// definitions of the 950 cache that carry both, whichever comes second wins and the other
		// value is lost. Deliberately not rewired: it cannot desync (both are one byte) and
		// nothing in the engine reads anInt2155, so naming the two fields is a separate job.
		else if (8 == opcode)
			anInt2155 = buffer.readUnsignedByte(); // 950 0x14035a6eb: one byte.
		else if (9 == opcode)
			anInt2140 = buffer.readUnsignedByte(); // 950 0x14035a710: one byte, SeqType +0xa60.
		else if (opcode == 10)
			anInt2162 = buffer.readUnsignedByte(); // 950 0x14035a735: one byte, SeqType +0xa64.
		else if (11 == opcode)
			anInt2155 = buffer.readUnsignedByte(); // 950 0x14035a75a: one byte.
		else if (12 == opcode) {
			// 950 0x14035ada5: a one-byte count, then the shared body at 950 0x14035adba.
			// Opcode 112 below is the same record with a two-byte count.
			int i_7_ = buffer.readUnsignedByte();
			anIntArray2151 = new int[i_7_];
			for (int i_8_ = 0; i_8_ < i_7_; i_8_++)
				anIntArray2151[i_8_] = buffer.readUnsignedShort();
			for (int i_9_ = 0; i_9_ < i_7_; i_9_++)
				anIntArray2151[i_9_] = (buffer.readUnsignedShort() << 16) + anIntArray2151[i_9_];
		} else if (13 == opcode) {
			// 950 0x14035a797: an unsigned SHORT entry count (950 0x14035a7b2), then per entry a
			// one-byte length (950 0x14035a800) and, when it is non-zero, a 24-bit big-endian
			// value (950 0x14035a89c reads three bytes and recombines them) followed by
			// length - 1 unsigned shorts (950 0x14035a8e0). The short count matters: opcodes
			// 119/120 index this table and the shipped 950 cache indexes it up to 1166.
			int i_10_ = buffer.readUnsignedShort();
			handledSounds = new int[i_10_][];
			for (int i_11_ = 0; i_11_ < i_10_; i_11_++) {
				int i_12_ = buffer.readUnsignedByte();
				if (i_12_ > 0) {
					handledSounds[i_11_] = new int[i_12_];
					handledSounds[i_11_][0] = buffer.read24BitInt();
					for (int i_13_ = 1; i_13_ < i_12_; i_13_++)
						handledSounds[i_11_][i_13_] = buffer.readUnsignedShort();
				}
			}
		}
		// 950 0x14035a933 ("lea eax, [r8 - 0xe]; cmp eax, 2; jbe") makes 14, 15 and 16
		// payload-less, and 950 0x14035a940 does the same for 18.
		else if (opcode == 14)
			aBoolean2158 = true;
		else if (opcode == 15)
			aBoolean2159 = true;
		else if (16 != opcode && opcode != 18) {
			if (strict && (opcode == 19 || opcode == 119 || opcode == 20 || opcode == 120)
					&& handledSounds == null) {
				// All four of these index the per-sound arrays that opcode 13 sizes, so without an
				// opcode-13 table there is nowhere to put the value. The 950 client has the same
				// dependency and does not check it: at 950 0x14035acf0 (19/119) and
				// 950 0x14035ab45 (20/120) it frees the zero-length allocation and then stores
				// through the resulting null pointer. Reporting a layout problem for this
				// definition is the fail-soft equivalent - the WIDTHS are known either way, this
				// is a missing destination, not an underived layout.
				// Unreachable on the shipped 950 cache: all 45,415 occurrences of 119/120 across
				// the 38,084 definitions are preceded by an opcode 13 in the same record, and
				// 19/20 do not occur at all.
				throw new IllegalArgumentException("Unsupported modern animation " + id + " opcode " + opcode + " without sound table at " + (buffer.getOffset() - 1));
			} else if (opcode == 19) {
				if (null == anIntArray1362) {
					anIntArray1362 = new int[handledSounds.length];
					for (int i_14_ = 0; i_14_ < handledSounds.length; i_14_++)
						anIntArray1362[i_14_] = 255;
				}
				anIntArray1362[buffer.readUnsignedByte()] = buffer.readUnsignedByte();
			} else if (strict && opcode == 119) {
				// 950 0x14035a952 dispatches 119 to the SAME body as 19 (950 0x14035ac82); the
				// only difference is the index width, chosen at 950 0x14035ad28: opcode 19 reads
				// one byte (950 0x14035ad2d), opcode 119 reads an unsigned short
				// (950 0x14035ad40). The value after it is one byte for both (950 0x14035ad64).
				// So 119 is 3 bytes where 19 is 2. The shipped cache needs the wide index: the
				// largest 119 index in it is 1166, which does not fit in a byte at all.
				if (null == anIntArray1362) {
					anIntArray1362 = new int[handledSounds.length];
					for (int i = 0; i < handledSounds.length; i++)
						anIntArray1362[i] = 255;
				}
				anIntArray1362[buffer.readUnsignedShort()] = buffer.readUnsignedByte();
			} else if (20 == opcode) {
				if (soundMaxDelay == null || null == soundMinDelay) {
					soundMaxDelay = new int[handledSounds.length];
					soundMinDelay = new int[handledSounds.length];
					for (int i_15_ = 0; i_15_ < handledSounds.length; i_15_++) {
						soundMaxDelay[i_15_] = 256;
						soundMinDelay[i_15_] = 256;
					}
				}
				int i_16_ = buffer.readUnsignedByte();
				soundMaxDelay[i_16_] = buffer.readUnsignedShort();
				soundMinDelay[i_16_] = buffer.readUnsignedShort();
			} else if (strict && opcode == 120) {
				// 950 0x14035a964 dispatches 120 to the SAME body as 20 (950 0x14035aad2); the
				// index width is chosen at 950 0x14035abf6 - opcode 20 reads one byte
				// (950 0x14035abfb), opcode 120 reads an unsigned short (950 0x14035ac0e). The
				// two shorts after it are identical for both (950 0x14035ac2e, 0x14035ac53).
				// So 120 is 6 bytes where 20 is 5. As with 119 the shipped cache needs the wide
				// index (largest is 1166).
				if (soundMaxDelay == null || null == soundMinDelay) {
					soundMaxDelay = new int[handledSounds.length];
					soundMinDelay = new int[handledSounds.length];
					for (int i = 0; i < handledSounds.length; i++) {
						soundMaxDelay[i] = 256;
						soundMinDelay[i] = 256;
					}
				}
				int index = buffer.readUnsignedShort();
				soundMaxDelay[index] = buffer.readUnsignedShort();
				soundMinDelay[index] = buffer.readUnsignedShort();
			} else if (22 == opcode)
				buffer.readUnsignedByte(); // 950 0x14035a972: one byte.
			else if (23 == opcode)
				// 950 0x14035a99b is a bare "add qword ptr [rdx + 0x18], 2": two bytes the client
				// advances past without decoding. No definition in the 950 cache uses it.
				buffer.readUnsignedShort();
			else if (24 == opcode)
				buffer.readUnsignedShort(); // 950 0x14035a9aa: one unsigned short.
			else if (opcode == 249) {
				// 950 0x14035aabc dispatches to the shared params reader at 950 0x1403f13e0 - the
				// same one LocType opcode 249 uses, and the same layout that parsed all 140,252
				// LocTypes: a byte count, then per entry a type byte, a 24-bit key and either a
				// null-terminated string or a four-byte int.
				int length = buffer.readUnsignedByte();
				if (clientScriptData == null)
					clientScriptData = new HashMap<Integer, Object>(length);
				for (int index = 0; index < length; index++) {
					boolean stringInstance = buffer.readUnsignedByte() == 1;
					int key = buffer.read24BitInt();
					Object value = stringInstance ? buffer.readString() : buffer.readInt();
					clientScriptData.put(key, value);
				}
			} else if (strict && opcode == 25) {
				// 950 0x14035a9fa: one unsigned short into SeqType +0xa40. Width only - the field
				// is used as a -1-sentinelled id that the client shifts left by 8 and ORs into a
				// composite key (950 0x14035afb8 tests it against -1, 950 0x14035b0b4 shifts it),
				// which is not enough to name it, so it is stored and not interpreted.
				// 3,463 definitions in the 950 cache carry it.
				modernInt25 = buffer.readUnsignedShort();
			} else if (strict && opcode == 26) {
				// 950 0x14035aa32: TWO unsigned shorts, into SeqType +0xa58 (950 0x14035aa5f) and
				// +0xa5c (950 0x14035aa8b). Four bytes.
				// This opcode was NOT in the sweep's missing list and is the reason to read the
				// whole ladder rather than only the opcodes that were reported: it occurs in
				// exactly the same 3,463 definitions as opcode 25 and always AFTER it, so opcode
				// 25 aborted every one of those records before 26 was ever reached.
				// Width only; the client later adds the sum of the frame durations to +0xa5c
				// (950 0x14035af4a .. 0x14035af7a), so it reads like a duration base.
				modernInt26a = buffer.readUnsignedShort();
				modernInt26b = buffer.readUnsignedShort();
			} else if (strict && opcode == 27) {
				// 950 0x14035aa97: ONE byte into SeqType +0xab0, read with movsx at
				// 950 0x14035aaac, so it is signed. Width and sign only: the client compares it
				// against -1 and -2 as sentinels selecting between two other byte sources
				// (950 0x14035b05e / 0x14035b06e) and the meaning of the non-sentinel case is
				// undecided. 590 definitions carry it, all with -1 or -2.
				modernInt27 = buffer.readByte();
			} else if (strict && opcode == 112) {
				// 950 0x14035a78e dispatches 112 into the SAME body as opcode 12
				// (shared from 950 0x14035adba): a count, then count unsigned shorts, then count
				// more unsigned shorts that are shifted left 16 and OR'd into the first
				// (950 0x14035ae50 and 950 0x14035ae90). Only the COUNT differs - opcode 12 reads
				// a byte (950 0x14035ada5), opcode 112 an unsigned short (950 0x14035ad7f).
				// So 112 is 2 + 4n bytes where 12 is 1 + 4n. Only 8 definitions use it, and all 8
				// fail if the count is read as a byte.
				int count = buffer.readUnsignedShort();
				anIntArray2151 = new int[count];
				for (int i = 0; i < count; i++)
					anIntArray2151[i] = buffer.readUnsignedShort();
				for (int i = 0; i < count; i++)
					anIntArray2151[i] = (buffer.readUnsignedShort() << 16) + anIntArray2151[i];
			} else if (strict && opcode == 3) {
				// 950 0x14035a5d8: an unsigned smart count, then that many unsigned smarts, all
				// of which the client reads and throws away - it only advances the cursor
				// (950 0x14035a630: one byte, or two when the first is >= 0x80). Kept aligned and
				// discarded. NOT exercised by shipped data: no definition in the 950 cache uses
				// opcode 3, so this branch is derived from the client alone.
				int count = buffer.readUnsignedSmart();
				for (int i = 0; i < count; i++)
					buffer.readUnsignedSmart();
			} else if (strict) {
				// Anything left is an opcode the 950 ladder at 0x14035a380 has no case for, so
				// its payload size is unknown and skipping it would desync every following field.
				// This still fires for e.g. 4, 17, 21 and 255. 255 in particular is NOT a real
				// SeqType opcode: it used to show up in scans only because reading opcodes 6/7 as
				// bigSmarts mis-parsed 0xFFFF, which readModernItemRef below fixes.
				throw new IllegalArgumentException("Unsupported modern animation " + id + " opcode " + opcode + " at " + (buffer.getOffset() - 1));
			}
		}
	}

	/**
	 * Layout of opcodes 6/7 (right/left hand item) on the modern cache: a plain unsigned short
	 * with 65535 as "none", exactly as the pre-bigSmart revisions encoded it.
	 *
	 * <p>Re-confirmed against the 950 client: opcode 6 at 950 0x14035a676 and opcode 7 at
	 * 950 0x14035a6ae both do "lea rax, [rcx + 2]", a two-byte read, into SeqType +0xa4c and
	 * +0xa50 respectively, whose client defaults are both -1 (950 0x1402e557a). The client stores
	 * the raw 0xFFFF; mapping it to -1 here only matches this class's own -1 convention.
	 *
	 * <p>The legacy (non-flat) table reads a bigSmart, which turns every 0xFFFF into a four-byte
	 * read and desynchronises thousands of modern animations - that was the origin of the
	 * spurious "opcode 255" entries in earlier scans (947 DefinitionScanProbe: 4,796 failures
	 * carried a 6/7 followed by a high byte). 255 is not a SeqType opcode in 950 either: the
	 * ladder has no case for it and no definition in the 950 cache contains one.
	 */
	private static int readModernItemRef(InputStream buffer) {
		int value = buffer.readUnsignedShort();
		return value == 65535 ? -1 : value;
	}

	/**
	 * Post-decode fixup. NOT re-derived - left exactly as the legacy table had it, and it does
	 * NOT match the 950 client, whose equivalent is 950 0x14035aef0:
	 * <ul>
	 *   <li>the client's "unset" sentinels are 4 for the opcode-9 field (+0xa60) and 3 for the
	 *       opcode-10 field (+0xa64), not -1 - those are the constructor defaults at
	 *       950 0x1402e5590 / 0x1402e559a. (Safe here in practice: no definition in the 950 cache
	 *       encodes a 9 above 3 or a 10 above 2, so an explicit value is never mistaken for the
	 *       sentinel either way.)</li>
	 *   <li>the client resolves the sentinel to 2 or 0 by testing a byte on the resource that
	 *       opcode 24 loads (950 0x14035aefb reads SeqType +0x9f0, then tests its byte at +0x70),
	 *       not by testing whether opcode 3 was present. Since no 950 definition carries an
	 *       opcode 3, aBooleanArray2149 is always null here and this always resolves to 0, while
	 *       the client can resolve to 2 - 3,141 of the 38,084 definitions carry an opcode 24.</li>
	 * </ul>
	 * Fixing this needs the meaning of that resource byte, which has not been established, so it
	 * is reported rather than guessed.
	 */
	public void method2394() {
		if (anInt2140 == -1) {
			if (aBooleanArray2149 == null)
				anInt2140 = 0;
			else
				anInt2140 = 2;
		}
		if (anInt2162 == -1) {
			if (aBooleanArray2149 == null)
				anInt2162 = 0;
			else
				anInt2162 = 2;
		}
	}

}
