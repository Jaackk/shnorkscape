package com.rs.game.player;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.VarBitDefinitions;

/**
 * The one authoritative per-player varp cache.
 *
 * {@code VarBitManager} used to keep a second int[12000] copy with an inverted
 * force check, so {@code getBitValue} could disagree depending on which manager
 * wrote last; it now delegates here. On the read-only flat 947 cache the array
 * is sized from the varplayer archive (index 2 archive 60, 12,798 ids) instead
 * of the legacy 12,000 cap that silently dropped ids 12,000..12,797; the legacy
 * 910 cache keeps int[12000].
 *
 * Native 947 players cannot use {@code Player.getPackets()} (it throws by
 * design). Their varp writes go to a {@link VarpSink} installed by the packet
 * facade; until one is installed the write is cached, counted and warned about
 * once, never sent through a guessed packet.
 */
public class VarsManager {

	public static final int[] masklookup = new int[32];

	/** Legacy 910 array size; kept exactly for the non-flat cache. */
	public static final int LEGACY_CAPACITY = 12000;
	/** Index 2 archive holding the varplayer definitions that bound valid varp ids. */
	public static final int VARPLAYER_ARCHIVE = 60;

	static {
		int i = 2;
		for (int i2 = 0; i2 < 32; i2++) {
			masklookup[i2] = i - 1;
			i += i;
		}
	}

	/** Destination for native 947 varp writes (VARP_LARGE); installed by the packet facade. */
	public interface VarpSink {
		void varp(int id, int value);
	}

	private final int[] values;
	private final Player player;
	private volatile VarpSink nativeSink;
	private long nativeUnsent, droppedWrites;
	private boolean warnedUnsent, warnedDropped;

	public VarsManager(Player player) {
		this.player = player;
		values = new int[capacity()];
	}

	/**
	 * Varp id bound: the 947 varplayer archive's file count when the flat cache is
	 * selected, otherwise the legacy 12,000. Falls back to legacy when index 2 is
	 * unavailable so a half-initialized cache never yields a zero-length array.
	 */
	public static int capacity() {
		if (Cache.isFlatReadOnly() && Cache.STORE.getIndexes() != null && Cache.STORE.getIndexes().length > 2) {
			Index index = Cache.STORE.getIndexes()[2];
			int count = index == null ? -1 : index.getLastFileId(VARPLAYER_ARCHIVE) + 1;
			if (count > 0)
				return count;
		}
		return LEGACY_CAPACITY;
	}

	public int size() {
		return values.length;
	}

	public void sendVar(int id, int value) {
		sendVar(id, value, false);
	}

	public void forceSendVar(int id, int value) {
		sendVar(id, value, true);
	}

	private void sendVar(int id, int value, boolean force) {
		if (!inRange(id))
			return;
		// Skip only when not forced AND the value is already what's stored.
		// Original logic had the force branch inverted (`force || ...`),
		// which made forceSendVar a no-op - any caller relying on it
		// (e.g. BeastOfBurden#131 sending the BoB familiar var) silently
		// failed to push the var to the client.
		if (!force && values[id] == value)
			return;
		setVar(id, value);
		sendClientVarp(id);
	}

	public void setVar(int id, int value) {
		if (!inRange(id))
			return;
		values[id] = value;
	}

	public int getValue(int id) {
		return id >= 0 && id < values.length ? values[id] : 0;
	}

	public void forceSendVarBit(int id, int value) {
		updateVarBit(id, value, true, true);
	}

	public void sendVarBit(int id, int value) {
		updateVarBit(id, value, true, false);
	}

	public void setVarBit(int id, int value) {
		updateVarBit(id, value, false, false);
	}

	public int getBitValue(int id) {
		VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(id);
		return getValue(defs.baseVar) >> defs.startBit & masklookup[defs.endBit - defs.startBit];
	}

	/**
	 * Writes a varbit into its base varp. Returns true when the varp changed (or
	 * {@code force} was set); {@code send} pushes the whole varp to the client,
	 * which on 947 is the VARP_LARGE recompute that covers values above 255.
	 */
	public boolean updateVarBit(int id, int value, boolean send, boolean force) {
		if (id == -1) // temporarly
			return false;
		VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(id);
		if (!inRange(defs.baseVar))
			return false;
		int mask = masklookup[defs.endBit - defs.startBit];
		if (value < 0 || value > mask)
			value = 0;
		mask <<= defs.startBit;
		int varpValue = (values[defs.baseVar] & (mask ^ 0xffffffff) | value << defs.startBit & mask);
		if (force || varpValue != values[defs.baseVar]) {
			setVar(defs.baseVar, varpValue);
			if (send)
				sendClientVarp(defs.baseVar);
			return true;
		}
		return false;
	}

	/** Installs (or clears with null) the native 947 varp destination. */
	public void setNativeVarpSink(VarpSink sink) {
		this.nativeSink = sink;
	}

	/** Native varp writes cached while no sink was installed. */
	public long nativeUnsentVarps() {
		return nativeUnsent;
	}

	/** Writes to ids outside the cache's varp range (legacy: silently dropped, now counted). */
	public long droppedWrites() {
		return droppedWrites;
	}

	private boolean inRange(int id) {
		if (id >= 0 && id < values.length)
			return true;
		droppedWrites++;
		if (!warnedDropped && player != null && player.isNative950()) {
			warnedDropped = true;
			System.out.println("[Ataraxia950] varp " + id + " is outside the cache-derived varplayer range 0.." + (values.length - 1) + "; write dropped and counted");
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void sendClientVarp(int id) {
		if (player != null && player.isNative950()) {
			VarpSink sink = nativeSink;
			if (sink != null) {
				sink.varp(id, values[id]);
			} else {
				nativeUnsent++;
				if (!warnedUnsent) {
					warnedUnsent = true;
					System.out.println("[Ataraxia950] varp " + id + " cached for a native player before a VARP_LARGE sink was installed; counted, not sent");
				}
			}
			return;
		}
		player.getPackets().sendVar(id, values[id]);
	}
}
