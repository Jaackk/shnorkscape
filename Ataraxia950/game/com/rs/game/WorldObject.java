package com.rs.game;

import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class WorldObject extends WorldTile {

	public Player owner;
	private int id;
	private final int type;
	private int rotation;
	private int life;
	/** Original map-placement transform, retained only by the native flat-cache loader. */
	private byte[] native950MapTransform;
	public byte[] getNative950MapTransform() { return native950MapTransform == null ? null : native950MapTransform.clone(); }
	public void setNative950MapTransform(byte[] data) { native950MapTransform = data == null ? null : data.clone(); }
	private int amount = Utils.random(1, 10);

	public WorldObject(int id, int type, int rotation, int x, int y, int plane) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane, int life) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = life;
	}

	public WorldObject(int id, int type, int rotation, WorldTile tile) {
		super(tile.getX(), tile.getY(), tile.getPlane());
		if (tile instanceof WorldObject) native950MapTransform = ((WorldObject) tile).getNative950MapTransform();
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public WorldObject(WorldObject object) {
		super(object.getX(), object.getY(), object.getPlane());
		this.id = object.id;
		this.type = object.type;
		this.rotation = object.rotation;
		this.life = object.life;
		this.native950MapTransform = object.getNative950MapTransform();
	}

	public WorldObject(int id, int type, int rotation, int x, int y, int plane, Player owner) {
		super(x, y, plane);
		this.owner = owner;
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public void decrementObjectLife() {
		this.life--;
	}

	public ObjectDefinitions getDefinitions() {
		return ObjectDefinitions.getObjectDefinitions(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getType() {
		return type;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void decrementAmount() {
		this.amount--;
	}

	public Player getOwner() {
		return owner;
	}

    @Override
    public String toString() {
        return "WorldObject [id=" + id + ", type=" + type + ", rotation=" + rotation + "], WorldTile "+"[ " + getX() + ", " + getY() + ", " + getPlane() + " ]";
    }
	
	
}