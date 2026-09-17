package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


public class ObjectDefinitions {

	private static final ConcurrentHashMap<Integer, ObjectDefinitions> OBJECT_DEFINITIONS = new ConcurrentHashMap<Integer, ObjectDefinitions>();

	public boolean ignoreClipOnAlternativeRoute;
	public boolean aBool617;
	public byte[] shapes;
	public int[][] models;
	public int anInt618;
	short[] recolorSrc;
	public short[] recolorDst;
	byte[] aByteArray621;
	public int[] anIntArray622;
	public short[] retextureDst;
	int offsetX;
	public int[] quests;
	byte aByte626;
	byte aByte627;
	public int sizeX;
	public int sizeY;
	public int mapSprite;
	public boolean projectileClip;
	public int anInt630;
	public boolean aBool631;
	int adjustValue;
	public boolean aBool633;
	short[] retextureSrc;
	public int anInt636;
	public String name = "null";
	int offsetY;
	public int anInt638;
	public int getID;
	int contrast;
	public String[] options;
	public int cursor1;
	public int cursor;
	public int cursor1op;
	public int cursor2op;
	byte adjustType;
	public int anInt644;
	public boolean adjustMapSpriteRotation;
	public int mapSpriteRotation;
	public boolean flipedMapSprite;
	int ambient;
	int resizeX;
	int resizeY;
	int resizeZ;
	public int anInt653;
	public int anInt654;
	int offsetZ;
	int anInt656;
	int anInt657;
	int anInt658;
	public boolean aBool659;
	public boolean aBool660;
	public int anInt661;
	public boolean aBool662;
	public int[] transforms;
	public int configFileId;
	public int configId;
	public int anInt666;
	public int clipType;
	public boolean isMirrored;
	public int anInt667;
	public boolean aBool668;
	public int anInt669;
	public int anInt670;
	public boolean isAnimating;
	public boolean aBool671;
	byte aByte672;
	public int[] objectAnimation;
	byte aByte674;
	public boolean aBool675;
	public int mapSpriteType;
	public boolean aBool677;
	int[] anIntArray678;
	public int anInt679;
	public int anInt680;
	public int anInt681;
	public boolean aBool682;
	public int id;
	public int cflag;

    public boolean loaded;
    public static void main(String[] args) throws IOException {
        Cache.init();
        //DBOperation.init();
        //database = DBOperation.getDatabase();
        //for (int i = 0; i < 113274; i++) {
        ObjectDefinitions defs = getObjectDefinitions(93017);
        /*
         * for (int i = 0; i < defs.options.length; i++) if (defs.options[i] != null)   
         * Logger.getGlobal().info("[" + i + "]:" + defs.options[i]);
         */
        /*WorldObject object = new WorldObject(defs.id, 0, 0, 0, 0, 0);
        Logger.getGlobal().info(defs.id + " " + defs.name + ObjectExamines.getExamine(object));
        try {
            String id = String.valueOf(defs.id);
            String name = defs.name;
            String examine = ObjectExamines.getExamine(object);
            database.addObject(id, name, examine);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Logger.getGlobal().catching(e);
        }*/
        Logger.getGlobal().info(defs.name);
        Logger.getGlobal().info(defs.objectAnimation);
        Logger.getGlobal().info(defs.configId);
        Logger.getGlobal().info("Var config: " + defs.configFileId + ", " + defs.configId);
        Logger.getGlobal().info("SizeX: " + defs.sizeX + ", SizeY: " + defs.sizeY);
        Logger.getGlobal().info(Arrays.toString(defs.transforms));
        //}
    }
	/**
	 * LocType (cache index 16) field decoder.
	 *
	 * Everything guarded by {@link Cache#isFlatReadOnly()} is 950-only and was derived from the
	 * 950 client's own LocType decoder, a cmp/jne opcode ladder occupying
	 * 950 0x140363ed0 .. 0x140366119 (arguments: rcx = LocType, rdx = buffer, r8d = opcode; the
	 * buffer's read position is the qword at buffer+0x18, so every "lea rax, [rcx + n]; mov
	 * [rdx+0x18], rax" in that ladder is an n-byte field). Individual opcodes are cited inline.
	 * NOTE: 947 addresses do not carry over - they disassemble cleanly in the 950 binary but land
	 * on unrelated code, so every address below was re-derived against the 950 image.
	 *
	 * The complete opcode set the 950 client accepts is:
	 *   1 2 14 15 17 18 19 21 22 23 24 27 28 29 30-34 39 40 41 42 44 45 62 64 65-67 69 70-72 73
	 *   74 75 77 78 79 81 82 88 89 91 92 93 94 95 97 98 101 102 103 104 105 106 107 108-111
	 *   150-154 160 162 163 164-166 167 170 171 173 177 178 179 186 188 189 190-195 196 197 198
	 *   199 200 201 202 203 204 205 206 207 208 209 249 250 251 252 253 254 255
	 * Of those, the shipped 950 cache actually uses all except 42, 77, 92, 101, 105, 179, 195,
	 * 200, 205, 250, 251, 252, 253, 254 and 255; those are implemented from the client only.
	 * The 950 client has no case for 99, 100, 168 or 169 (kept below for the legacy cache; they
	 * are payload-less either way, so they cannot desync a modern record).
	 *
	 * Verification: every one of the 140,252 LocType definitions in the 950 cache was parsed with
	 * this exact width table; all terminate on opcode 0 at exactly their record length, with no
	 * unknown opcode and no over- or under-read.
	 */
	public void readValues(InputStream buffer, int opcode) {
		if (opcode == 1) {
			int i_1_ = buffer.readUnsignedByte();
			shapes = new byte[i_1_];
			models = new int[i_1_][];
			for (int i_2_ = 0; i_2_ < i_1_; i_2_++) {
				shapes[i_2_] = (byte) buffer.readByte();
				int i_3_ = buffer.readUnsignedByte();
				models[i_2_] = new int[i_3_];
				for (int i_4_ = 0; i_4_ < i_3_; i_4_++) {
					models[i_2_][i_4_] = buffer.readBigSmart();
				}
			}
		} else if (2 == opcode) {
			name = buffer.readString().intern();
		} else if (opcode == 14) {
			sizeX = buffer.readUnsignedByte();
		} else if (opcode == 15) {
			sizeY = buffer.readUnsignedByte();
		} else if (opcode == 17) {
			clipType = 0;
			projectileClip = false;
		} else if (opcode == 18) {
			projectileClip = false;
		} else if (opcode == 19) {
			anInt630 = buffer.readUnsignedByte();
		} else if (opcode == 21) {
			this.adjustType = (byte) 1;
		} else if (opcode == 22) {
			aBool633 = true;
		} else if (23 == opcode) {
			anInt681 = 1;
		} else if (24 == opcode) {
			int i_5_ = buffer.readBigSmart();
			if (-1 != i_5_) {
				this.objectAnimation = new int[] { i_5_ };
			}
		} else if (opcode == 27) {
			clipType = 1;
		} else if (28 == opcode) {
			anInt638 = buffer.readUnsignedByte() << 2;
		} else if (29 == opcode) {
			this.ambient = buffer.readByte();
		} else if (opcode == 39) {
			this.contrast = buffer.readByte();
		} else if (opcode >= 30 && opcode < 35) {
			options[opcode - 30] = buffer.readString().intern();
		} else if (40 == opcode) {
			int i_6_ = buffer.readUnsignedByte();
			recolorSrc = new short[i_6_];
			recolorDst = new short[i_6_];
			for (int i_7_ = 0; i_7_ < i_6_; i_7_++) {
				recolorSrc[i_7_] = (short) buffer.readUnsignedShort();
				recolorDst[i_7_] = (short) buffer.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int i_8_ = buffer.readUnsignedByte();
			retextureSrc = new short[i_8_];
			retextureDst = new short[i_8_];
			for (int i_9_ = 0; i_9_ < i_8_; i_9_++) {
				retextureSrc[i_9_] = (short) buffer.readUnsignedShort();
				retextureDst[i_9_] = (short) buffer.readUnsignedShort();
			}
		} else if (42 == opcode) {
			int i_10_ = buffer.readUnsignedByte();
			this.aByteArray621 = new byte[i_10_];
			for (int i_11_ = 0; i_11_ < i_10_; i_11_++) {
				this.aByteArray621[i_11_] = (byte) buffer.readByte();
			}
		} else if (44 == opcode) {
			int i_13_ = buffer.readUnsignedShort();
			int i_14_ = 0;
			for (int i_15_ = i_13_; i_15_ > 0; i_15_ >>= 1) {
				i_14_++;
			}
			byte[] aByteArray5627 = new byte[i_14_];
			byte i_16_ = 0;
			for (int i_17_ = 0; i_17_ < i_14_; i_17_++) {
				if ((i_13_ & 1 << i_17_) > 0) {
					aByteArray5627[i_17_] = i_16_;
					i_16_++;
				} else {
					aByteArray5627[i_17_] = (byte) -1;
				}
			}
		} else if (45 == opcode) {
			int i_18_ = buffer.readUnsignedShort();
			int i_19_ = 0;
			for (int i_20_ = i_18_; i_20_ > 0; i_20_ >>= 1) {
				i_19_++;
			}
			byte[] aByteArray5622 = new byte[i_19_];
			byte i_21_ = 0;
			for (int i_22_ = 0; i_22_ < i_19_; i_22_++) {
				if ((i_18_ & 1 << i_22_) > 0) {
					aByteArray5622[i_22_] = i_21_;
					i_21_++;
				} else {
					aByteArray5622[i_22_] = (byte) -1;
				}
			}
		} else if (opcode == 62) {
			isMirrored = true;
		} else if (64 == opcode) {
			aBool617 = false;
		} else if (opcode == 65) {
			this.resizeX = buffer.readUnsignedShort();
		} else if (opcode == 66) {
			this.resizeY = buffer.readUnsignedShort();
		} else if (opcode == 67) {
			this.resizeZ = buffer.readUnsignedShort();
		} else if (69 == opcode) {
			cflag = buffer.readUnsignedByte();
		} else if (70 == opcode) {
			this.offsetX = buffer.readShort() << 2;
		} else if (opcode == 71) {
			this.offsetY = buffer.readShort() << 2;
		} else if (opcode == 72) {
			this.offsetZ = buffer.readShort() << 2;
		} else if (opcode == 73) {
			aBool659 = true;
		} else if (opcode == 74) {
			ignoreClipOnAlternativeRoute = true;
		} else if (opcode == 75) {
			anInt661 = buffer.readUnsignedByte();
		} else if (opcode == 77 || 92 == opcode) {
			this.configFileId = buffer.readUnsignedShort();
			if (this.configFileId == 65535) {
				this.configFileId = -1;
			}
			this.configId = buffer.readUnsignedShort();
			if (this.configId == 65535) {
				this.configId = -1;
			}
			int i_12_ = -1;
			if (opcode == 92) {
				i_12_ = buffer.readBigSmart();
			}
			int i_13_ = Cache.isFlatReadOnly() ? buffer.readUnsignedSmart() : buffer.readUnsignedByte();
			transforms = new int[2 + i_13_];
			for (int i_14_ = 0; i_14_ <= i_13_; i_14_++) {
				transforms[i_14_] = buffer.readBigSmart();
			}
			transforms[1 + i_13_] = i_12_;
		} else if (opcode == 78) {
			anInt666 = buffer.readUnsignedShort();
			anInt653 = buffer.readUnsignedByte();
		} else if (opcode == 79) {
			anInt669 = buffer.readUnsignedShort();
			anInt670 = buffer.readUnsignedShort();
			anInt653 = buffer.readUnsignedByte();
			// The count is a plain unsigned byte in 950 as well as in the legacy cache: the 950
			// client reads a single byte at 950 0x140364b6e, with no smart-encoding branch. (It
			// was previously read as an unsigned smart on the modern cache; that only happened to
			// work because no 950 record uses a count above 127 - the largest in the shipped
			// cache is 25 - but a byte >= 0x80 would have consumed two bytes and desynced.)
			int i_15_ = buffer.readUnsignedByte();
			anIntArray622 = new int[i_15_];
			for (int i_16_ = 0; i_16_ < i_15_; i_16_++) {
				anIntArray622[i_16_] = buffer.readUnsignedShort();
			}
		} else if (81 == opcode) {
			this.adjustType = (byte) 2;
			this.adjustValue = buffer.readUnsignedByte() * 256;
		} else if (opcode == 82) {
			aBool662 = true;
		} else if (88 == opcode) {
			aBool631 = false;
		} else if (opcode == 89) {
			aBool660 = false;
		} else if (91 == opcode) {
			aBool675 = true;
		} else if (93 == opcode) {
			this.adjustType = (byte) 3;
			this.adjustValue = buffer.readUnsignedShort();
		} else if (94 == opcode) {
			this.adjustType = (byte) 4;
		} else if (opcode == 95) {
			this.adjustType = (byte) 5;
			this.adjustValue = buffer.readShort();
		} else if (97 == opcode) {
			adjustMapSpriteRotation = true;
		} else if (98 == opcode) {
			aBool677 = true;
		} else if (opcode == 99) {
			// Used for RS2
		} else if (opcode == 100) {
			// Used for RS2
		} else if (101 == opcode) {
			mapSpriteRotation = buffer.readUnsignedByte();
		} else if (102 == opcode) {
			mapSpriteType = buffer.readUnsignedShort();
		} else if (opcode == 103) {
			anInt681 = 0;
		} else if (opcode == 104) {
			anInt667 = buffer.readUnsignedByte();
		} else if (opcode == 105) {
			flipedMapSprite = true;
		} else if (opcode == 106) {
			int i_17_ = buffer.readUnsignedByte();
			int i_18_ = 0;
			this.objectAnimation = new int[i_17_];
			this.anIntArray678 = new int[i_17_];
			for (int i_19_ = 0; i_19_ < i_17_; i_19_++) {
				this.objectAnimation[i_19_] = buffer.readBigSmart();
				i_18_ += this.anIntArray678[i_19_] = buffer.readUnsignedByte();
			}
			for (int i_20_ = 0; i_20_ < i_17_; i_20_++) {
				this.anIntArray678[i_20_] = 65535 * this.anIntArray678[i_20_] / i_18_;
			}
		} else if (107 == opcode) {
			mapSprite = buffer.readUnsignedShort();
		} else if (opcode >= 150 && opcode < 155) {
			options[opcode - 150] = buffer.readString();
		} else if (160 == opcode) {
			// Plain byte count, confirmed at 950 0x14036505a - there is no smart-encoding branch.
			// (Largest count in the shipped 950 cache is 2.)
			int i_21_ = buffer.readUnsignedByte();
			quests = new int[i_21_];
			for (int i_22_ = 0; i_22_ < i_21_; i_22_++) {
				quests[i_22_] = buffer.readUnsignedShort();
			}
		} else if (162 == opcode) {
			this.adjustType = (byte) 3;
			this.adjustValue = buffer.readInt();
		} else if (163 == opcode) {
			this.aByte674 = (byte) buffer.readByte();
			this.aByte672 = (byte) buffer.readByte();
			this.aByte626 = (byte) buffer.readByte();
			this.aByte627 = (byte) buffer.readByte();
		} else if (opcode == 164) {
			this.anInt656 = buffer.readShort();
		} else if (165 == opcode) {
			this.anInt657 = buffer.readShort();
		} else if (opcode == 166) {
			this.anInt658 = buffer.readShort();
		} else if (opcode == 167) {
			anInt636 = buffer.readUnsignedShort();
		} else if (opcode == 168) {
			aBool668 = true;
		} else if (opcode == 169) {
			aBool671 = true;
		} else if (opcode == 170) {
			anInt644 = buffer.readSmart();
		} else if (171 == opcode) {
			anInt618 = buffer.readSmart();
		} else if (opcode == 173) {
			anInt679 = buffer.readUnsignedShort();
			anInt680 = buffer.readUnsignedShort();
		} else if (177 == opcode) {
			isAnimating = true;
		} else if (178 == opcode) {
			anInt654 = buffer.readUnsignedByte();
		} else if (opcode == 186) {
			buffer.readUnsignedByte();
		} else if (189 == opcode) {
			aBool682 = true;
		} else if (opcode >= 190 && opcode < 196) {
			buffer.readUnsignedShort();
		} else if (opcode == 196) {
			buffer.readUnsignedByte();
		} else if (opcode == 197) {
			buffer.readUnsignedByte();
		} else if (opcode == 200) {

		} else if (opcode == 201) {
			buffer.readUnsignedSmart();
			buffer.readUnsignedSmart();
			buffer.readUnsignedSmart();
			buffer.readUnsignedSmart();
			buffer.readUnsignedSmart();
			buffer.readUnsignedSmart();
		
			
		} else if (Cache.isFlatReadOnly()
				&& (opcode == 179 || opcode == 188 || opcode == 198 || opcode == 199 || opcode == 203)) {
			// Modern boolean appearance/click-zone flags. Each one only sets a byte in the 950
			// client and reads nothing: 179 at 950 0x14036530d, 188 at 950 0x14036534d,
			// 198 at 950 0x1403654b0, 199 at 950 0x1403654c5, 203 at 950 0x140365787.
			// Collision remains controlled by dimensions and the independent 17/18/27 flags.
		} else if (Cache.isFlatReadOnly() && opcode == 202) {
			buffer.readUnsignedByte(); // 950 0x14036575e: one byte.
		} else if (Cache.isFlatReadOnly() && opcode == 204) {
			// 950 0x14036579c: byte count, then that many 27-byte particle placement records
			// (short, byte, then two 12-byte float triples read by 950 0x14010cef0).
			buffer.skip(buffer.readUnsignedByte() * 27);
		} else if (Cache.isFlatReadOnly() && (opcode == 205 || opcode == 209)) {
			// 950 0x140365912 dispatches 205 and 209 to the same handler at 950 0x140365d5e.
			readModernModelMorphs(buffer, opcode == 209);
		} else if (Cache.isFlatReadOnly()
				&& (opcode == 250 || opcode == 251 || opcode == 253 || opcode == 254)) {
			// Single-byte fields on the loc's modern sub-record: 250 at 950 0x1403659ac,
			// 251 at 950 0x1403659d6, 253 at 950 0x140365ac4, 254 at 950 0x140365aee.
			// 251 and 254 are booleans (compared against 1); the width is a byte either way.
			buffer.readUnsignedByte();
		} else if (Cache.isFlatReadOnly() && opcode == 252) {
			// 950 0x140365a03: three unsigned shorts, same shape as opcode 255.
			buffer.readUnsignedShort();
			buffer.readUnsignedShort();
			buffer.readUnsignedShort();
		} else if (249 == opcode) {
			int i_23_ = buffer.readUnsignedByte();
			for (int i_25_ = 0; i_25_ < i_23_; i_25_++) {
				boolean bool = buffer.readUnsignedByte() == 1;
				buffer.read24BitInt();
				if (bool) {
					buffer.readString();
				} else {
					buffer.readInt();
				}
			}
		} else if (Cache.isFlatReadOnly()
				&& (opcode == 108 || opcode == 109 || opcode == 110 || opcode == 111)) {
			// Added in 950; payload-less flags, no collision effect. Each only sets a byte in the
			// 950 client: 950 0x140364f71, 0x140364f83, 0x140364f95, 0x140364fa7.
		} else if (Cache.isFlatReadOnly() && opcode == 206) {
			// 950 0x140365924: a three-byte header whose last byte is the record count, then that
			// many 37-byte records (950 0x140368f50 advances 1+4+4+4+1+4+3+2+2+4+4+4 = 37).
			buffer.readUnsignedByte();
			buffer.readUnsignedByte();
			buffer.skip(buffer.readUnsignedByte() * 37);
		} else if (Cache.isFlatReadOnly() && (opcode == 207 || opcode == 208)) {
			// 950's replacements for 77 and 92: the varbit id widened to 24 bits.
			// 950 0x140365989 dispatches both to the shared body at 950 0x140365bdd.
			configFileId = buffer.read24BitInt();
			if (configFileId == 0xffffff) configFileId = -1;
			configId = buffer.readUnsignedShort();
			if (configId == 65535) configId = -1;
			int fallback = opcode == 208 ? buffer.readBigSmart() : -1;
			// The count is unsigned-smart, including records with more than 128 morphs.
			int transformCount = buffer.readUnsignedSmart();
			transforms = new int[transformCount + 2];
			for (int index = 0; index <= transformCount; index++) transforms[index] = buffer.readBigSmart();
			transforms[transformCount + 1] = fallback;
		} else if (Cache.isFlatReadOnly() && opcode == 255) {
			// 950 0x140365b1b: three unsigned shorts.
			buffer.readUnsignedShort();
			buffer.readUnsignedShort();
			buffer.readUnsignedShort();
		} else {
			if (Cache.isFlatReadOnly()) throw new IllegalArgumentException("Unsupported modern object " + id + " opcode " + opcode + " at " + (buffer.getOffset() - 1));
		}
	}

	/**
	 * LocType opcodes 205 and 209 in the 950 cache. Both are the same record; 209 only widens the
	 * varbit id from 16 to 24 bits, exactly as 207/208 widen it for 77/92.
	 *
	 * Layout re-derived from the 950 client, not from the earlier RS3 (rsmv) description, which
	 * gets the flag-16 section wrong. The opcode ladder at 950 0x140365d5e consumes two bytes and
	 * then calls the shared reader at 950 0x1403b1d20 with a "wide varbit" boolean that is set for
	 * 209 and clear for 205 (tested at 950 0x1403b1d33).
	 *
	 * Only the widths are derived here; the meaning of the per-entry values is not, so everything
	 * is read and discarded. Keeping the stream aligned is the whole job.
	 */
	private static void readModernModelMorphs(InputStream buffer, boolean wideVarbit) {
		buffer.readUnsignedShort(); // 950 0x140365d5e: two bytes the client skips without decoding.
		if (wideVarbit) buffer.read24BitInt(); // 950 0x1403b1d5d (opcode 209).
		else buffer.readUnsignedShort();       // 950 0x1403b1d9e (opcode 205).
		buffer.readUnsignedShort(); // 950 0x1403b1dcf: varp id.
		int flags = buffer.readUnsignedByte(); // 950 0x1403b1e13.
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
			if ((flags & bit) == 0) continue;
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

	public static ObjectDefinitions getObjectDefinitions(int id) {
		ObjectDefinitions def = OBJECT_DEFINITIONS.get(id);
		if (def == null) {
			def = new ObjectDefinitions();
			def.id = id;

			try {
				com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[16];
				byte[] data = null;

				// Safely verify Index 16 exists before asking for the object file
				if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
					data = index.getFile(id >>> -1135990488, id & 0xff);
				}

				if (data != null) {
					def.loaded = true;
					def.readValueLoop(new InputStream(data, Cache.isFlatReadOnly()));
				} else {
					if (Cache.isFlatReadOnly()) throw new IllegalArgumentException("Missing modern object definition " + id);
					// Fallback for missing 921 cache objects
					def.name = "Missing Object (" + id + ")";
				}
			} catch (Exception e) {
				if (Cache.isFlatReadOnly()) throw new IllegalStateException("Cannot decode modern object " + id, e);
				// Silently catch any missing files or index out-of-bounds errors.
				def.name = "Error Object (" + id + ")";
			}

			OBJECT_DEFINITIONS.put(id, def);
		}
		return def;
	}

	private void readValueLoop(InputStream stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(stream, opcode);
		}
		if (Cache.isFlatReadOnly() && stream.getRemaining() != 0)
			throw new IllegalArgumentException("Trailing bytes in modern object " + id + " at " + stream.getOffset());
	}

	private ObjectDefinitions() {
		this.aByte627 = (byte) 0;
		sizeX = 1;
		sizeY = 1;
		clipType = 2;
		projectileClip = true;
		options = new String[5];
		anInt630 = -1;
		this.adjustType = (byte) 0;
		this.adjustValue = -1;
		aBool633 = false;
		anInt681 = -1;
		anInt644 = 960;
		anInt618 = 0;
		this.objectAnimation = null;
		this.anIntArray678 = null;
		anInt638 = 64;
		this.ambient = 0;
		this.contrast = 0;
		cursor1 = -1;
		cursor = -1;
		cursor1op = -1;
		cursor2op = -1;
		mapSprite = -1;
		mapSpriteType = -1;
		adjustMapSpriteRotation = false;
		mapSpriteRotation = 0;
		flipedMapSprite = false;
		isMirrored = false;
		aBool617 = true;
		this.resizeX = 128;
		this.resizeY = 128;
		this.resizeZ = 128;
		this.offsetX = 0;
		this.offsetY = 0;
		this.offsetZ = 0;
		this.anInt656 = 0;
		this.anInt657 = 0;
		this.anInt658 = 0;
		aBool659 = false;
		ignoreClipOnAlternativeRoute = false;
		anInt661 = -1;
		anInt636 = 0;
		this.configFileId = -1;
		this.configId = -1;
		anInt666 = -1;
		anInt653 = 0;
		anInt654 = 0;
		anInt667 = 255;
		aBool668 = false;
		anInt669 = 0;
		anInt670 = 0;
		aBool671 = false;
		aBool660 = true;
		aBool662 = false;
		aBool631 = true;
		aBool675 = false;
		aBool677 = false;
		anInt679 = 256;
		anInt680 = 256;
		isAnimating = false;
		aBool682 = false;
	}

	public String getFirstOption() {
		if (options == null || options.length < 1)
			return "";
		return options[0];
	}

	public String getSecondOption() {
		if (options == null || options.length < 2)
			return "";
		return options[1];
	}

	public String getOption(int option) {
		if (options == null || options.length < option || option == 0)
			return "";
		return options[option - 1];
	}

	public String getThirdOption() {
		if (options == null || options.length < 3)
			return "";
		return options[2];
	}

	public int getId() {
		return id;
	}

	public boolean containsOption(int i, String option) {
		if (options == null || options[i] == null || options.length <= i)
			return false;
		return options[i].equals(option);
	}

	public boolean containsOption(String o) {
		if (options == null)
			return false;
		for (String option : options) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase(o))
				return true;
		}
		return false;
	}

	public int getClipType() {
		return clipType;
	}

	public boolean isProjectileCliped() {
		return id != 51577 && id != 51571 && id != 51577 && id != 51543 && id != 51539 && id != 51572 && id != 51570 && id != 51607 && id != 50076 && id != 49348 && id != 49345 && id != 51578 && id != 51609 && id != 49858 && id != 51059 && id != 51030 && id != 49794 && id != 51061 && id != 49346 && id != 50036 && id != 50997 && id != 51031 && id != 50913 && id != 49852 && id != 49798 && id != 49935 && id != 50037 && id != 52091 && id != 55452 && id != 49776 && id != 49826 && id != 49786 && id != 54112 && id != 49778 && id != 49347 && id != 49768 && id != 52087 && id != 49774 && id != 52123 && id != 49782 && id != 52117 && id != 52118 && id != 52124 && id != 52154 && id != 52007 && id != 55024 && id != 53797 && id != 53844 && id != 53771 && id != 55018 && id != 55019 && id != 53791 && id != 54964 && id != 54963 && id != 53789 && id != 53803 && id != 53783 && id != 55025 && id != 55126 && id != 54962 && id != 54960 && id != 55926 && id != 55994 && id != 55919 && id != 55920 && id != 55921 && id != 55864 && id != 55927 && id != 55992 && id != 55526 && id != 55544 && id != 55516 && id != 55520 && id != 55841 && id != 37232 && id != 37220 && id != 32462 && id != 55530 && id != 55548 && id != 55522 && projectileClip;
	}

	public int getSize() {
		return sizeX * sizeY;
	}
	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getAccessBlockFlag() {
		return cflag;
	}

	public String getName() {
		return name;
	}
	
    public String getActualName() {
        return transforms == null ? name : ObjectDefinitions.getObjectDefinitions(transforms[0]).getName();
    }
    
	public static void clearObjectDefinitions() {
		OBJECT_DEFINITIONS.clear();
	}


}
