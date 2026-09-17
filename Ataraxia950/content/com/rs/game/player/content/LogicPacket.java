package com.rs.game.player.content;

import com.rs.network.io.InputStream;

public class LogicPacket {

	byte[] data;
	private final int id;

	public LogicPacket(int id, int size, InputStream stream) {
		this.id = id;
		data = new byte[size];
		stream.getBytes(data, 0, size);
	}

	public byte[] getData() {
		return data;
	}

	public int getId() {
		return id;
	}

}
