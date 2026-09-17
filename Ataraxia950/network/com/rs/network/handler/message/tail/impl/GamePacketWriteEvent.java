package com.rs.network.handler.message.tail.impl;

import com.rs.network.handler.message.tail.WriteMessageEvent;
import io.netty.buffer.ByteBuf;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class GamePacketWriteEvent extends WriteMessageEvent {

	private final ByteBuf buffer;

	public GamePacketWriteEvent(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

}
