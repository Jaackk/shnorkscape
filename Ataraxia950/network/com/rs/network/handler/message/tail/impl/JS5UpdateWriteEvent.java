package com.rs.network.handler.message.tail.impl;

import com.rs.network.handler.message.tail.WriteMessageEvent;
import io.netty.buffer.ByteBuf;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateWriteEvent extends WriteMessageEvent {

	private final ByteBuf buffer;

	public JS5UpdateWriteEvent(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

}
