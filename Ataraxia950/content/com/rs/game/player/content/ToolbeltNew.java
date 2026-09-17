package com.rs.game.player.content;

import java.io.Serializable;
import java.util.Map;

import com.rs.game.player.Player;

/**
 * Handles a {@link Player}'s NEW rs3 toolbelt.
 *
 * @author Noel.
 */
public class ToolbeltNew implements Serializable {

	private static final long serialVersionUID = 2054084250525451553L;
	@SuppressWarnings("unused")
    private transient Player player;
	private Map<Integer, Boolean> items;

	public ToolbeltNew(Player player) {
		this.player = player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

    public Map<Integer, Boolean> getItems() {
        return items;
    }
}