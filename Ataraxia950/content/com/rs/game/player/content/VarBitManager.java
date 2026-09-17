package com.rs.game.player.content;

import com.rs.game.player.Player;
import com.rs.game.player.VarsManager;

/**
 * Content-side facade over the player's single varp cache.
 *
 * Historically this class owned its own int[12000] and had the force check
 * inverted ({@code if (force || values[id] == value) return;}), so
 * {@code forceSendVar} never sent and {@code getBitValue} could disagree with
 * {@code VarsManager}. Every call now delegates to
 * {@link Player#getVarsManager()}: one array, one force rule, and the same
 * native-947 write path. The public surface (including the boolean
 * {@link #sendVarBit}) is unchanged for the 77 content callers.
 */
public class VarBitManager {

	private Player player;

	public VarBitManager(Player player) {
		this.player = player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private VarsManager vars() {
		if (player == null)
			throw new IllegalStateException("VarBitManager has no player; call setPlayer after deserialization");
		return player.getVarsManager();
	}

	public void sendVar(int id, int value) {
		vars().sendVar(id, value);
	}

	public void forceSendVar(int id, int value) {
		vars().forceSendVar(id, value);
	}

	public void setVar(int id, int value) {
		vars().setVar(id, value);
	}

	public int getValue(int id) {
		return vars().getValue(id);
	}

	public void forceSendVarBit(int id, int value) {
		vars().forceSendVarBit(id, value);
	}

	public boolean sendVarBit(int id, int value) {
		return vars().updateVarBit(id, value, true, false);
	}

	public void setVarBit(int id, int value) {
		vars().setVarBit(id, value);
	}

	public int getBitValue(int id) {
		return vars().getBitValue(id);
	}
}
