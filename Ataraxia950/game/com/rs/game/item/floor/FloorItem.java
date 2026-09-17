package com.rs.game.item.floor;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

public class FloorItem extends Item {

	private static final long serialVersionUID = -2287633342490535089L;

	private WorldTile tile;
	private String ownerName;
	// 0 visible, 1 invisible, 2 visible and reappears 30sec after taken
	private int type;
	private boolean lootbeam;
	// Keep native lifecycle identity even when its owner logs out before expiry.
	private boolean native950;
	public boolean isNative950() { return native950; }
	/** Marks an admitted native world item without requiring an online owner (e.g. fire ashes). */
	public void markNative950() { native950 = true; }
	// Stamped from the existing ItemConstants rules before native world publication.
	private boolean publicTransferAllowed = true;
	public boolean isPublicTransferAllowed() { return publicTransferAllowed; }
	public void setPublicTransferAllowed(boolean allowed) { publicTransferAllowed = allowed; }
	
	public void setLootbeam(boolean value) {
		this.lootbeam = value;
	}
	
	public boolean hasLootbeam() {
		return lootbeam;
	}

	public FloorItem(int id) {
		super(id);
	}

	public FloorItem(Item item, WorldTile tile, Player owner, boolean underGrave, boolean invisible) {
		super(item.getId(), item.getAmount(), item.getCharges());
		this.tile = tile;
		this.native950 = owner != null && owner.isNative950();
		if (owner != null)
			this.ownerName = owner.getUsername();
		this.type = invisible ? 1 : 0;
		setAttributes(item.getAttributes());
	}

	@Deprecated
	public FloorItem(Item item, WorldTile tile, boolean appearforever) {
		super(item.getId(), item.getAmount(), item.getCharges());
		this.tile = tile;
		this.type = appearforever ? 2 : 0;
	    setAttributes(item.getAttributes());
	}

	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public WorldTile getTile() {
		return tile;
	}

	public boolean isInvisible() {
		return type == 1;
	}

	public void setInvisible(boolean invisible) {
		type = invisible ? 1 : 0;
	}

	public boolean isForever() {
		return type == 2;
	}

	public String getOwner() {
		return ownerName;
	}

	public boolean hasOwner() {
		return ownerName != null;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (lootbeam ? 1231 : 1237);
        result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
        result = prime * result + ((tile == null) ? 0 : tile.hashCode());
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FloorItem other = (FloorItem) obj;
        if (lootbeam != other.lootbeam)
            return false;
        if (ownerName == null) {
            if (other.ownerName != null)
                return false;
        } else if (!ownerName.equals(other.ownerName))
            return false;
        if (tile == null) {
            if (other.tile != null)
                return false;
        } else if (!tile.equals(other.tile))
            return false;
		return type == other.type;
	}
	
	
	
}