package com.rs.game;

public final class Animation {

	private final int[] ids;
	private final int speed;

	public Animation(int id) {
		this(id, 0);
	}

	public Animation(int id, int speed) {
		this(id, id, id, id, id, speed);
	}

	public Animation(int id1, int id2, int id3, int id4, int speed) {
		this.ids = new int[] { id1, id2, id3, id4, id1 };
		this.speed = speed;
	}

	public Animation(int id1, int id2, int id3, int id4, int id5, int speed) {
		this.ids = new int[] { id1, id2, id3, id4, id5 };
		this.speed = speed;
	}

	public int[] getIds() {
		return ids;
	}

	public int getSpeed() {
		return speed;
	}
}