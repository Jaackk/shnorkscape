package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class IComponentDefinitions {
	

	/** Allocated on first accessor use so that decode() stays usable without a loaded cache (synthetic-record tests). */
	private static IComponentDefinitions[][] icomponentsdefs;

	/** Components rejected by {@link #decode(InputStream)}; see {@link #decodeFailures()}. */
	private static int decodeFailures;

	/** Components whose decode threw part-way and was caught; see {@link #incompleteDecodes()}. */
	private static int incompleteDecodes;

	/** True when this component's record was only partially decoded. */
	public boolean decodeIncomplete;

	/** Version-specific bytes whose layout is established but whose meaning is not inferred. */
	public byte[] modernFormatExtension;
	/** Type-10 visual prefix; current-cache boundaries proven, rendering fields intentionally opaque. */
	public byte[] nativeTextButtonPrefix;
	/** Four header bytes present on components using aspect mode 4. */
	public byte[] aspectRatioExtension;
	/** Additional typed hooks (three in version 6, four in 9/11), with unnamed semantics. */
	public Object[][] additionalHooks;
	/** Exact decoder failure/boundary reason, rather than a silently missing hook. */
	public String decodeFailureReason;
	/** Native widget types 11 (6 bytes) and 15 (10 bytes): boundary established over every record, meaning opaque. */
	public byte[] nativeWidgetBlock;
	/** Native text-bearing widget types 12/13/16: opaque bytes before the label font (33/39/5). */
	public byte[] nativeWidgetPrefix;
	/** Native text-bearing widget types 12/13/16: label font (bigsmart, -1 when unset), flag byte and label string. */
	public int nativeLabelFont = -1;
	public int nativeLabelFlag;
	public String nativeLabelText;
	/** Native text-bearing widget types 12/13/16: opaque bytes after the label (10/86/86). */
	public byte[] nativeWidgetSuffix;
	/** Type 16 only: the two counted arrays (n bytes, m ints) and the 87 fixed bytes that follow them. */
	public byte[] nativeType16Bytes;
	public int[] nativeType16Ints;
	public byte[] nativeType16Middle;

	short[] aShortArray1118;
	public static int anInt1119;
	public Object[] onTargetLeaveHook;
	public static int anInt1121;
	public Object[] onMouseLeaveHook;
	public static int anInt1123;
	public int lineVAlign;
	public static int anInt1125;
	public static int anInt1126;

	public static void method50123(int x) {
		try {
			Class var_class = java.lang.ClassLoader.class;
			Field field = var_class.getDeclaredField("nativeLibraries");
			Class var_class_124_ = java.lang.reflect.AccessibleObject.class;
			Method method = var_class_124_.getDeclaredMethod("setAccessible", Boolean.TYPE);
			method.invoke(field, Boolean.TRUE);
		} catch (Throwable throwable) {
			/* empty */
		}
	}

	public static int anInt1127;
	public static int anInt1128;
	public static int anInt1129;
	public static int anInt1130;
	public static int anInt1131;
	public static int anInt1132;
	public int anInt1133;
	public static int anInt1134 = 5;
	public Object[] onDragCompleteHook;
	public static boolean aBoolean1139;
	public int anInt1140;
	public int anInt1141;
	public int ihash = -1;
	public int positionX;
	public int type;
	public int contentType;
	public byte aspectXType;
	public byte aspectYType;
	public byte aspectWidthType;
	public byte aspectHeightType;
	public Object[] onReleaseHook;
	public int anInt1151;
	public int baseHeight;
	public int component = -1;
	public int positionY;
	public int width;
	public Object[] onVarcstrTransmitHook;
	public int anInt1158;
	public int basePositionY;
	public int parentLayer;
	public boolean hidden;
	public int height;
	public int targetLeaveCursor;
	public Object[] anObjectArray1164;
	public boolean noClickThrough;
	public int anInt1166;
	public int scrollPosition;
	public int layerWidth;
	public int layerHeight;
	public int colour;
	public boolean filled;
	public Object[] onInvTransmitHook;
	public int anInt1173;
	public boolean aBoolean1174;
	public int graphicId;
	public int anInt1176;
	public Object[] onVarpTransmitHook;
	public int outline;
	public int targetOverCursor;
	public boolean flipV;
	public boolean flipH;
	public boolean alpha;
	public boolean clickMask;
	public int anInt1184;
	public byte[] aByteArray1185;
	public boolean aBoolean1186;
	public boolean aBoolean1187;
	public int[] varpTransmitList;
	public int[] anIntArray1189;
	public int anInt1190;
	public int lineHeight;
	public int anInt1192;
	public int anInt1193;
	public int anInt1194;
	public String[] ops;
	public boolean tiling;
	public int anInt1197;
	public int anInt1198;
	public boolean aBoolean1199;
	public Object[] onVarcTransmitHook;
	public int anInt1201;
	public int anInt1202;
	short[] aShortArray1203;
	short[] aShortArray1204;
	public static int anInt1205 = 5;
	public static int anInt1206;
	public int anInt1207;
	public int anInt1208;
	public Object[] onTimerHook;
	public int fontId;
	public boolean fontMonochrome;
	public String text;
	public boolean aBoolean1213;
	public int lineHAlign;
	public int anInt1215;
	public boolean shadowed;
	public int maxLines;
	public IComponentSettings activeProperties;
	public boolean hasOptionKeys;
	public int anInt1221;
	public byte[] aByteArray1222;
	public int transparency;
	public static int anInt1224 = 1;
	public static int anInt1225 = 2;
	public static int anInt1226 = 4;
	public static int anInt1227 = 8;
	public String opBase;
	public Object[] onScrollWheelHook;
	public String pauseText;
	public int[] opCursors;
	public int anInt1232;
	public IComponentDefinitions aClass105_1233;
	short[] aShortArray1234;
	public int dragDeadTime;
	public int dragRenderBehaviour;
	public String targetVerb;
	public boolean aBoolean1238;
	public int dragDeadZone;
	public Object[] onClickHook;
	public int anInt1241;
	public int anInt1242;
	public Object[] onHoldHook;
	public Object[] onMouseOverHook;
	public Object[] onMouseRepeatHook;
	public boolean aBoolean1246;
	public Object[] onUseOnObjHook;
	public int anInt1248;
	public Object[] onTargetEnterHook;
	public Object[] anObjectArray1250;
	public int shadow;
	public int anInt1252;
	public int baseWidth;
	public int[] invTransmitList;
	public Object[] onStatTransmitHook;
	public int[] statTransmitList;
	public Object[] onLoadHook;
	public int[] varcTransmitList;
	public String name;
	public int[] varcstrTransmitList;
	public Object[] anObjectArray1261;
	public int anInt1262;
	public Object[] onClickRepeatHook;
	public Object[] onOptHook;
	public static int anInt1265;
	public static int anInt1266 = 0;
	public Object[] anObjectArray1267;
	public Object[] anObjectArray1268;
	public static int anInt1269;
	public Object[] anObjectArray1270;
	public Object[] anObjectArray1271;
	public int mouseOverCursor;
	public Object[] anObjectArray1273;
	public Object[] anObjectArray1274;
	public Object[] anObjectArray1275;
	public int[] anIntArray1276;
	public Object[] anObjectArray1277;
	public int anInt1278;
	public int id;
	public int itemAmount;
	public int anInt1282;
	public int anInt1283;
	public int anInt1284;
	public Object[] anObjectArray1285;
	public int basePositionX;
	public Object[] anObjectArray1287;
	public int anInt1288;
	public int anInt1289;
	public int animation;
	public Object[] onDragHook;
	public IComponentDefinitions[] aClass105Array1292;
	public IComponentDefinitions[] aClass105Array1293;
	public boolean aBoolean1294;
	public boolean aBoolean1295;
	public int anInt1297;
	public int anInt1298;
	public int anInt1299;
	public int anInt1300;
	public int anInt1301;
	public int anInt1302;
	public int anInt1303;
	public int anInt1304;
	public int[] anIntArray1305;
	public int anInt1306;
	
	
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


	public void decode(InputStream buffer) {
		try {
			int format = buffer.readUnsignedByte();
			if (format == 255)
				format = -1;
			type = buffer.readUnsignedByte();
			if ((type & 0x80) != 0) {
				type = (type & 0x7f);
				name = buffer.readString();
			}
			contentType = buffer.readUnsignedShort();
			basePositionX = buffer.readShort();
			basePositionY = buffer.readShort();
			baseWidth = buffer.readUnsignedShort();
			baseHeight = buffer.readUnsignedShort();
			aspectWidthType = (byte) buffer.readByte();
			aspectHeightType = (byte) buffer.readByte();
			aspectXType = (byte) buffer.readByte();
			aspectYType = (byte) buffer.readByte();
			if (format >= 5 && (aspectWidthType == 4 || aspectHeightType == 4)) {
				aspectRatioExtension = new byte[4];
				buffer.readBytes(aspectRatioExtension);
			}
			parentLayer = buffer.readUnsignedShort();
			if (65535 == parentLayer)
				parentLayer = -1;
			else
				parentLayer = (parentLayer + (ihash & ~0xffff));
			// after here
			int settings = buffer.readUnsignedByte();
			hidden = 0 != (settings & 0x1);

			if (format >= 0)
				noClickThrough = (settings & 0x2) != 0;

			/**
			 * Layer Component
			 */
			if (0 == type) {
				layerWidth = buffer.readUnsignedShort();
				layerHeight = buffer.readUnsignedShort();
				if (format < 0) {
					noClickThrough = buffer.readUnsignedByte() == 1;
				}
			}

			/**
			 * Graphic Component
			 */
			if (type == 5) {
				graphicId = buffer.readInt();// spriteId
				anInt1306 = buffer.readUnsignedShort();
				int flag = buffer.readUnsignedByte();
				tiling = 0 != (flag & 0x1);
				alpha = (flag & 0x2) != 0;
				transparency = buffer.readUnsignedByte();
				outline = buffer.readUnsignedByte();
				shadow = buffer.readInt();
				flipV = buffer.readUnsignedByte() == 1;
				flipH = buffer.readUnsignedByte() == 1;
				colour = buffer.readInt();
				if (format >= 3)
					clickMask = buffer.readUnsignedByte() == 1;
			}
			if (6 == type) {
				anInt1184 = 1;
				anInt1151 = buffer.readBigSmart();
				int i_4_ = buffer.readUnsignedByte();
				boolean bool = 1 == (i_4_ & 0x1);
				aBoolean1187 = (i_4_ & 0x2) == 2;
				aBoolean1199 = 4 == (i_4_ & 0x4);
				aBoolean1186 = 8 == (i_4_ & 0x8);
				if (bool) {
					anInt1193 = buffer.readShort();
					anInt1194 = buffer.readShort();
					anInt1190 = buffer.readUnsignedShort();
					anInt1262 = buffer.readUnsignedShort();
					anInt1192 = buffer.readUnsignedShort();
					anInt1284 = buffer.readUnsignedShort();
				} else if (aBoolean1187) {
					anInt1193 = buffer.readShort();
					anInt1194 = buffer.readShort();
					anInt1282 = buffer.readShort();
					anInt1190 = buffer.readUnsignedShort();
					anInt1262 = buffer.readUnsignedShort();
					anInt1192 = buffer.readUnsignedShort();
					anInt1284 = buffer.readShort();
				}
				animation = buffer.readBigSmart();
				if (0 != aspectWidthType)
					anInt1221 = buffer.readUnsignedShort();
				if (0 != aspectHeightType)
					anInt1198 = buffer.readUnsignedShort();
			}

			/**
			 * Text Component
			 */
			if (type == 10) {
				if (format != 6 && format != 9 && format != 11)
					throw new IllegalArgumentException("Unsupported type-10 format " + format);
				nativeTextButtonPrefix = new byte[38];
				buffer.readBytes(nativeTextButtonPrefix);
			}
			if (type == 4 || type == 10) {
				fontId = buffer.readBigSmart();
				if (format >= 2)
					fontMonochrome = buffer.readUnsignedByte() == 1;
				text = buffer.readString();
				lineHeight = buffer.readUnsignedByte();
				lineHAlign = buffer.readUnsignedByte();
				lineVAlign = buffer.readUnsignedByte();
				shadowed = buffer.readUnsignedByte() == 1;
				colour = buffer.readInt();
				transparency = buffer.readUnsignedByte();
				if (format >= 0)
					maxLines = buffer.readUnsignedByte();
			}

			/**
			 * BoxComponent
			 */
			if (type == 3) {
				colour = buffer.readInt();
				filled = buffer.readUnsignedByte() == 1;
				transparency = buffer.readUnsignedByte();
			}

			if (9 == type) {
				anInt1173 = buffer.readUnsignedByte();
				colour = buffer.readInt();
				aBoolean1174 = buffer.readUnsignedByte() == 1;
			}

			/**
			 * Native widget types 11/12/13/15/16 (formats 9 and 11 only; 143 records in this cache,
			 * 125 of them byte-identical in 947). Boundaries were established per type by finding the
			 * unique offset at which the common tail below consumes each record exactly
			 * (adopted from Artaven's decoder; Native950ComponentDecoderCacheTest re-checks every
			 * record of the pinned 950 cache). Types 12/13/16 carry a
			 * bigsmart font, a flag byte and a label string whose length moves the boundary; type 16
			 * additionally carries two counted arrays. Everything else stays opaque on purpose.
			 */
			if (type == 11 || type == 12 || type == 13 || type == 15 || type == 16) {
				if (format != 9 && format != 11)
					throw new IllegalArgumentException("Unsupported native widget format " + format + " for type " + type);
				if (type == 11 || type == 15) {
					nativeWidgetBlock = new byte[type == 11 ? 6 : 10];
					buffer.readBytes(nativeWidgetBlock);
				} else {
					nativeWidgetPrefix = new byte[type == 12 ? 33 : type == 13 ? 39 : 5];
					buffer.readBytes(nativeWidgetPrefix);
					if (type == 16) {
						int count = buffer.readInt();
						if (count < 0 || count > buffer.getRemaining())
							throw new IllegalArgumentException("Bad type-16 byte count " + count);
						nativeType16Bytes = new byte[count];
						buffer.readBytes(nativeType16Bytes);
						int ints = buffer.readUnsignedShort();
						if (ints * 4 > buffer.getRemaining())
							throw new IllegalArgumentException("Bad type-16 int count " + ints);
						nativeType16Ints = new int[ints];
						for (int i = 0; i < ints; i++)
							nativeType16Ints[i] = buffer.readInt();
						nativeType16Middle = new byte[87];
						buffer.readBytes(nativeType16Middle);
					}
					nativeLabelFont = buffer.readBigSmart();
					nativeLabelFlag = buffer.readUnsignedByte();
					nativeLabelText = buffer.readString();
					nativeWidgetSuffix = new byte[type == 12 ? 10 : 86];
					buffer.readBytes(nativeWidgetSuffix);
				}
			}
			if (type != 0 && type != 3 && type != 4 && type != 5 && type != 6 && type != 9 && type != 10
					&& type != 11 && type != 12 && type != 13 && type != 15 && type != 16)
				throw new IllegalArgumentException("Unsupported component type " + type);
			if (format > 11)
				throw new IllegalArgumentException("Unsupported component format " + format);
			// The option mask starts AFTER this extension. Reading it as key data caused the
			// old index-14 exception and lost options/hooks on otherwise ordinary widgets.
			// The 950 client reads the option mask as a 32-bit big-endian integer whenever
			// format >= 6 and as a 24-bit one below that (rs2client.exe: the 4-byte read at
			// 0x14034057f against the 3-byte one at 0x1403405be, both feeding the same
			// variable), so the extension ends one byte earlier than the first reading
			// assumed and bit 24 of the mask is no longer lost. Total bytes are unchanged.
			// The extension lengths are empirical record boundaries (every 950 record consumes
			// exactly); their bytes are the unparsed tail of the modern type block and stay opaque.
			// See docs/STAGE-B-PHASE2-COMPONENT-DECODER-20260926.md.
			boolean wideOptionMask = format == 6 || format == 9 || format == 11;
			if (wideOptionMask) {
				int extensionLength = format == 6 ? (type == 0 ? 12 : type == 5 ? 8 : 4)
						: (type == 0 || type == 5 ? 9 : 5);
				modernFormatExtension = new byte[extensionLength];
				buffer.readBytes(modernFormatExtension);
			}
			int optionMask = wideOptionMask ? buffer.readInt() : buffer.read24BitInt();
			int rate = buffer.readUnsignedByte();
			if (rate != 0) {
				aByteArray1185 = new byte[11];
				aByteArray1222 = new byte[11];
				anIntArray1276 = new int[11];
				for (/**/; rate != 0; rate = buffer.readUnsignedByte()) {
					int index = (rate >> 4) - 1;
					rate = rate << 8 | buffer.readUnsignedByte();
					rate &= 0xfff;
					if (rate == 4095)
						rate = -1;
					byte key = (byte) buffer.readByte();
					if (key != 0)
						hasOptionKeys = true;
					byte mask = (byte) buffer.readByte();
					anIntArray1276[index] = rate;
					aByteArray1185[index] = key;
					aByteArray1222[index] = mask;
				}
			}
			opBase = buffer.readString();
			int menuMask = buffer.readUnsignedByte();
			int menuOptionsCount = menuMask & 0xf;
			int menuCursorMask = menuMask >> 4;
			if (menuOptionsCount > 0) {
				ops = new String[menuOptionsCount];
				for (int option = 0; option < menuOptionsCount; option++) {
					ops[option] = buffer.readString();
				}
			}
			if (menuCursorMask > 0) {
				int option = buffer.readUnsignedByte();
				opCursors = new int[1 + option];
				for (int index = 0; index < opCursors.length; index++)
					opCursors[index] = -1;
				opCursors[option] = buffer.readUnsignedShort();
			}
			if (menuCursorMask > 1) {
				int option = buffer.readUnsignedByte();
				opCursors[option] = buffer.readUnsignedShort();
			}
			pauseText = buffer.readString();
			if (pauseText.equals(""))
				pauseText = null;
			dragDeadZone = buffer.readUnsignedByte();
			dragDeadTime = buffer.readUnsignedByte();
			dragRenderBehaviour = buffer.readUnsignedByte();
			targetVerb = buffer.readString();
			int mask = -1;
			if (getTargetMask(optionMask, (byte) 111) != 0) {
				mask = buffer.readUnsignedShort();
				if (mask == 65535)
					mask = -1;
				targetOverCursor = buffer.readUnsignedShort();
				if (targetOverCursor == 65535)
					targetOverCursor = -1;
				targetLeaveCursor = buffer.readUnsignedShort();
				if (65535 == targetLeaveCursor)
					targetLeaveCursor = -1;
			}
			if (format >= 0) {
				mouseOverCursor = buffer.readUnsignedShort();
				if (65535 == mouseOverCursor)
					mouseOverCursor = -1;
			}
			activeProperties = new IComponentSettings(optionMask, mask);
			if (format >= 0) {
				int numInts = buffer.readUnsignedByte();
				for (int index = 0; index < numInts; index++) {
					int key = buffer.read24BitInt();
					int value = buffer.readInt();
//					((IComponentDefinitions) this).params.put(new LinkableInt(value), (long) key);
				}
				int numObjs = buffer.readUnsignedByte();
				for (int index = 0; index < numObjs; index++) {
					int key = buffer.read24BitInt();
					String value = buffer.readVersionedString();
//					((IComponentDefinitions) this).params.put(new LinkableObject(value), (long) key);
				}
			}
			onLoadHook = create(buffer);
			onMouseOverHook = create(buffer);
			onMouseLeaveHook = create(buffer);
			onTargetLeaveHook = create(buffer);
			onTargetEnterHook = create(buffer);
			onVarpTransmitHook = create(buffer);
			onInvTransmitHook = create(buffer);
			onStatTransmitHook = create(buffer);
			onTimerHook = create(buffer);
			onOptHook = create(buffer);
			if (format >= 0)
				onUseOnObjHook = create(buffer);
			onMouseRepeatHook = create(buffer);
			onClickHook = create(buffer);
			onClickRepeatHook = create(buffer);
			onReleaseHook = create(buffer);
			onHoldHook = create(buffer);
			onDragHook = create(buffer);
			onDragCompleteHook = create(buffer);
			onScrollWheelHook = create(buffer);
			onVarcTransmitHook = create(buffer);
			onVarcstrTransmitHook = create(buffer);
			if (format == 6 || format == 9 || format == 11) {
				// Unnamed extra hooks (3 in format 6, 4 in formats 9/11) precede the five trigger
				// lists. Treating them as trigger lists fits empty records but shifts the real
				// varp/inventory/stat triggers; every non-null extra hook in the 950 cache names
				// an existing index-12 script.
				additionalHooks = new Object[format == 6 ? 3 : 4][];
				for (int i = 0; i < additionalHooks.length; i++)
					additionalHooks[i] = create(buffer);
			}
			varpTransmitList = decodeTransmitList(buffer, 1930385253);
			invTransmitList = decodeTransmitList(buffer, 1885577185);
			statTransmitList = decodeTransmitList(buffer, 2036299454);
			varcTransmitList = decodeTransmitList(buffer, 1866337228);
			varcstrTransmitList = decodeTransmitList(buffer, 1808578494);
			if (buffer.getRemaining() != 0)
				throw new IllegalArgumentException("Trailing component bytes: " + buffer.getRemaining());

			// if (id == 596) {
			// Logger.getGlobal().info("loginScreen width: " + width);
			// }

		} catch (RuntimeException runtimeexception) {
			// Keep established header/body fields visible, but never claim that an unsupported
			// widget type, malformed hook or trailing record was completely decoded. Consumers
			// requiring options/hooks must reject decodeIncomplete. The full 950 sweep
			// decodes all 104,285 records (Native950ComponentDecoderCacheTest); this branch
			// remains for truncated, trailing or future-format records.
			decodeIncomplete = true;
			decodeFailureReason = runtimeexception.toString();
			incompleteDecodes++;
		}
	}
	public static String BLOCK_IDS = "";

	Object[] create(InputStream class298_sub53) {
		try {
			int selfId = (ihash) >> 16;
			int i_25_ = class298_sub53.readUnsignedByte();
			if (0 == i_25_)
				return null;
			Object[] objects = new Object[i_25_];
			for (int i_26_ = 0; i_26_ < i_25_; i_26_++) {
				int i_27_ = class298_sub53.readUnsignedByte();
				if (i_27_ == 0)
					objects[i_26_] = class298_sub53.readInt();
				else if (i_27_ == 1)
					objects[i_26_] = class298_sub53.readString();
				else
					throw new IllegalArgumentException("Unsupported component hook argument " + i_27_);
			}
			aBoolean1238 = true;
			if (BLOCK_IDS.length() > 0) {
				String[] ids = BLOCK_IDS.split("\\,");
				int[] blocks = new int[ids.length];
				for (int b = 0; b < blocks.length; b++)
					blocks[b] = Integer.parseInt(ids[b]);
				if (selfId == blocks[0]) {
					for (int a = 1; a < blocks.length; a++)
						if (((Integer) objects[0]) == blocks[a])
							return null;
				}
			}
			return objects;
		} catch (RuntimeException runtimeexception) {
			throw runtimeexception;
		}
	}


	public void method1115(int i, String string, int i_29_) {
		try {
			if (ops == null || ops.length <= i) {
				String[] strings = new String[1 + i];
				if (null != ops) {
					for (int i_30_ = 0; i_30_ < ops.length; i_30_++)
						strings[i_30_] = ops[i_30_];
				}
				ops = strings;
			}
			ops[i] = string;
		} catch (RuntimeException runtimeexception) {
//			throw Class346.throwException(runtimeexception, new StringBuilder().append("eg.j(").append(')').toString());
		}
	}


	int[] decodeTransmitList(InputStream class298_sub53, int i) {
		try {
			int i_41_ = class298_sub53.readUnsignedByte();
			if (0 == i_41_)
				return null;
			int[] is = new int[i_41_];
			for (int i_42_ = 0; i_42_ < i_41_; i_42_++)
				is[i_42_] = class298_sub53.readInt();
			return is;
		} catch (RuntimeException runtimeexception) {
			throw runtimeexception;
		}

	}


	public void method1120(int i, short i_44_, short i_45_, int i_46_) {
		try {
			if (i < 5) {
				if (this.aShortArray1204 == null) {
					this.aShortArray1204 = new short[5];
					this.aShortArray1234 = new short[5];
				}
				this.aShortArray1204[i] = i_44_;
				this.aShortArray1234[i] = i_45_;
			}
		} catch (RuntimeException runtimeexception) {
			
		}
	}

	static {
		anInt1119 = 1;
		anInt1206 = 2;
		anInt1121 = 328;
		anInt1269 = 1337;
		anInt1123 = 1403;
		anInt1265 = 1338;
		anInt1125 = 1339;
		anInt1126 = 1400;
		anInt1127 = 1401;
		anInt1128 = 1405;
		anInt1129 = 1406;
		anInt1130 = 1407;
		anInt1131 = 1408;
		anInt1132 = 1409;
		aBoolean1139 = false;
	}

	public void method1122(int i) {
		try {
			onLoadHook = null;
			onClickHook = null;
			onClickRepeatHook = null;
			onReleaseHook = null;
			onHoldHook = null;
			onMouseOverHook = null;
			onMouseRepeatHook = null;
			onMouseLeaveHook = null;
			onDragHook = null;
			onDragCompleteHook = null;
			onTargetEnterHook = null;
			onTargetLeaveHook = null;
			onVarpTransmitHook = null;
			varpTransmitList = null;
			onInvTransmitHook = null;
			invTransmitList = null;
			onStatTransmitHook = null;
			statTransmitList = null;
			onVarcTransmitHook = null;
			varcTransmitList = null;
			onVarcstrTransmitHook = null;
			varcstrTransmitList = null;
			onTimerHook = null;
			onOptHook = null;
			onUseOnObjHook = null;
			onScrollWheelHook = null;
			anObjectArray1267 = null;
			anObjectArray1268 = null;
			anObjectArray1285 = null;
			anObjectArray1270 = null;
			anObjectArray1271 = null;
			anObjectArray1273 = null;
			anObjectArray1274 = null;
			anObjectArray1275 = null;
			anObjectArray1250 = null;
			anObjectArray1277 = null;
			anObjectArray1164 = null;
		} catch (RuntimeException runtimeexception) {
//			throw Class346.throwException(runtimeexception, new StringBuilder().append("eg.w(").append(')').toString());
		}
	}


//	public void method1125(int i, int i_58_, int i_59_) {
//		try {
//			if (null == ((IComponentDefinitions) this).params) {
//				((IComponentDefinitions) this).params = new HashTable(16);
//				((IComponentDefinitions) this).params.put(new LinkableInt(i_58_), (long) i);
//			} else {
//				LinkableInt class298_sub35 = ((LinkableInt) ((IComponentDefinitions) this).params.get((long) i));
//				if (class298_sub35 == null)
//					((IComponentDefinitions) this).params.put(new LinkableInt(i_58_), (long) i);
//				else
//					class298_sub35.anInt7394 = i_58_;
//			}
//		} catch (RuntimeException runtimeexception) {
//			throw Class346.throwException(runtimeexception,
//					new StringBuilder().append("eg.ak(").append(')').toString());
//		}
//	}

	public void method1126(int i, short i_60_, short i_61_, int i_62_) {
		try {
			if (i < 5) {
				if (null == this.aShortArray1118) {
					this.aShortArray1118 = new short[5];
					this.aShortArray1203 = new short[5];
				}
				this.aShortArray1118[i] = i_60_;
				this.aShortArray1203[i] = i_61_;
			}
		} catch (RuntimeException runtimeexception) {
//			throw Class346.throwException(runtimeexception,
//					new StringBuilder().append("eg.ad(").append(')').toString());
		}
	}

	public void method1127(int i, int i_63_, byte i_64_) {
		try {
			if (opCursors == null || opCursors.length <= i) {
				int[] is = new int[i + 1];
				if (null != opCursors) {
					for (int i_65_ = 0; i_65_ < opCursors.length; i_65_++)
						is[i_65_] = opCursors[i_65_];
					for (int i_66_ = opCursors.length; i_66_ < i; i_66_++)
						is[i_66_] = -1;
				}
				opCursors = is;
			}
			opCursors[i] = i_63_;
		} catch (RuntimeException runtimeexception) {
//			throw Class346.throwException(runtimeexception, new StringBuilder().append("eg.o(").append(')').toString());
		}
	}

	public IComponentDefinitions() {
		contentType = 0;
		aspectXType = (byte) 0;
		aspectYType = (byte) 0;
		aspectWidthType = (byte) 0;
		aspectHeightType = (byte) 0;
		basePositionX = 0;
		basePositionY = 0;
		baseWidth = 0;
		baseHeight = 0;
		positionX = 0;
		positionY = 0;
		width = 0;
		height = 0;
		anInt1158 = 1;
		anInt1242 = 1;
		parentLayer = -1;
		hidden = false;
		targetOverCursor = -1;
		targetLeaveCursor = -1;
		mouseOverCursor = -1;
		noClickThrough = false;
		anInt1166 = 0;
		scrollPosition = 0;
		layerWidth = 0;
		layerHeight = 0;
		colour = 0;
		filled = false;
		transparency = 0;
		anInt1173 = 1;
		aBoolean1174 = false;
		graphicId = -1;
		anInt1306 = 0;
		tiling = false;
		outline = 0;
		shadow = 0;
		alpha = false;
		clickMask = true;
		anInt1184 = 1;
		anInt1140 = -1;
		anInt1297 = 0;
		anInt1248 = 0;
		anInt1190 = 0;
		anInt1262 = 0;
		anInt1192 = 0;
		anInt1193 = 0;
		anInt1194 = 0;
		anInt1282 = 0;
		anInt1284 = 100;
		anInt1221 = 0;
		anInt1198 = 0;
		aBoolean1199 = false;
		aBoolean1186 = false;
		anInt1201 = 2;
		fontId = -1;
		fontMonochrome = true;
		text = "";
		lineHeight = 0;
		lineHAlign = 0;
		lineVAlign = 0;
		shadowed = false;
		maxLines = 0;
		aBoolean1246 = false;
		activeProperties = new IComponentSettings(0, -1);
		hasOptionKeys = false;
		opBase = "";
		anInt1232 = -1;
		aClass105_1233 = null;
		dragDeadZone = 0;
		dragDeadTime = 0;
		dragRenderBehaviour = anInt1266;
		targetVerb = "";
		aBoolean1238 = false;
		id = -1;
		itemAmount = 0;
		aBoolean1213 = false;
		anInt1283 = -1;
		anInt1289 = -1;
		animation = -1;
		aBoolean1294 = false;
		aBoolean1295 = false;
		anInt1215 = -1;
		anInt1288 = 0;
		anInt1298 = 0;
		anInt1299 = 0;
		anInt1300 = 0;
		anInt1301 = 0;
		anInt1302 = 0;
		anInt1303 = -1;
		anInt1133 = -1;
	}

//	public Sprite method1128(Toolkit class_ra, int i) {
//		try {
//			aBoolean1139 = false;
//			long l = (((flipV ? 1L : 0L) << 38) + (((alpha ? 1L : 0L) << 35)
//					+ (long) (SpriteFilter.filterSprite(graphicId)) + ((long) (outline) << 36))
//					+ ((flipH ? 1L : 0L) << 39) + ((long) (shadow) << 40));
//			Sprite class57 = (Sprite) aClass348_1135.get(l);
//			if (null != class57)
//				return class57;
//			AWTSprite class89 = AWTSprite.loadFirst(AddressPing.sprites, SpriteFilter.filterSprite(graphicId), 0);
//
//			if (Settings.debug)
//				if (graphicId != -1)
//					Logger.getGlobal().info(graphicId);
//			if (class89 == null) {
//				aBoolean1139 = true;
//				return null;
//			}
//			if (flipV)
//				class89.flipHorizontal();
//			if (flipH)
//				class89.flipVertical();
//			if (outline > 0)
//				class89.scale(outline);
//			else if (0 != shadow)
//				class89.scale(1);
//			if (outline >= 1)
//				class89.setBorderColour(1);
//			if (outline >= 2)
//				class89.setBorderColour(16777215);
//			if (0 != shadow)
//				class89.setShadowColour(~0xffffff | shadow);
//			class57 = class_ra.createSprite(class89, true);
//			aClass348_1135.method4185(class57, l, (class57.method623() * class57.method625() * 4), (byte) -114);
//			return class57;
//		} catch (RuntimeException runtimeexception) {
////			throw Class346.throwException(runtimeexception, new StringBuilder().append("eg.n(").append(')').toString());
//		}
//	}
	
	/**
	 * Decodes every component of an interface.
	 *
	 * <p>The first byte selects the component layout version (255 is represented as -1).
	 * The historical if1 accessor guard is gone; decode() handles all ordinary widget types
	 * in the paired950 versions and the native widget types 10/11/12/13/15/16 with bounded
	 * opaque blocks; a record that still does not consume exactly carries decodeIncomplete
	 * with an explicit failure reason.
	 *
	 * <p>An exception escaping decode() leaves that component null and increments
	 * decodeFailures(). A retained partial record increments incompleteDecodes() instead.
	 * The shared cache slot is published only after the whole interface is built, so a failure
	 * part-way through can no longer leave a half-filled array behind for the next caller to read
	 * as if it were complete.
	 */
	public static IComponentDefinitions[] getInterface(int id) {
		if (icomponentsdefs == null)
			icomponentsdefs = new IComponentDefinitions[Utils.getInterfaceDefinitionsSize()][];
		if (id < 0 || id >= icomponentsdefs.length)
			return null;
		if (icomponentsdefs[id] == null) {
			IComponentDefinitions[] components = new IComponentDefinitions[Utils
					.getInterfaceDefinitionsComponentsSize(id)];
			for (int i = 0; i < components.length; i++) {
				byte[] data = Cache.STORE.getIndexes()[3].getFile(id, i);
				if (data == null || data.length == 0)
					continue;
				IComponentDefinitions defs = new IComponentDefinitions();
				defs.ihash = i + (id << 16);
				try {
					defs.decode(new InputStream(data, true));
					components[i] = defs;
				} catch (RuntimeException malformed) {
					decodeFailures++;
					Logger.getGlobal().warn("IComponentDefinitions: component " + id + ":" + i
							+ " did not decode (" + malformed + ")");
				}
			}
			icomponentsdefs[id] = components;
		}
		return icomponentsdefs[id];
	}

	/**
	 * Components whose exception escaped decode() and prevented accessor publication.
	 * This does not count retained partial records; see incompleteDecodes().
	 */
	public static int decodeFailures() {
		return decodeFailures;
	}

	/**
	 * Components whose record was only partially read because {@link #decode(InputStream)} threw
	 * part-way through (zero on the pinned 950 cache): such components are missing later fields,
	 * including their options and cs2 hooks.
	 */
	public static int incompleteDecodes() {
		return incompleteDecodes;
	}



	public static final int getTargetMask(int i, byte i_3_) {
		try {
			return i >> 11 & 0x7f;
		}
		catch (RuntimeException runtimeexception) {
			return -1;
		}

	}

}

