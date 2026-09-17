package com.rs.network.handler.message.head.impl;

import com.rs.network.handler.message.head.ReadMessageEvent;
import com.rs.network.io.InputStream;
import com.rs.network.packet.PacketRepository;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class GamePacketReadEvent extends ReadMessageEvent {

	private final int packetId;
	private final int packetSize;
	private final InputStream payload;
	private final PacketRepository repository;

	public GamePacketReadEvent(int packetId, int packetSize, InputStream payload, PacketRepository repository) {
		this.packetId = packetId;
		this.packetSize = packetSize;
		this.payload = payload;
		this.repository = repository;
	}

	public int getPacketId() {
		return packetId;
	}

	public int getPacketSize() {
		return packetSize;
	}

	public InputStream getPayload() {
		return payload;
	}

	public PacketRepository getRepository() {
		return repository;
	}

}
