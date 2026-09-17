package com.rs.network.handler.message.tail.impl;

import com.rs.network.codec.ResultMessage;
import com.rs.network.codec.handshake.HandshakeType;
import com.rs.network.handler.message.tail.WriteMessageEvent;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class HandshakeWriteEvent extends WriteMessageEvent {

	private final HandshakeType handshakeType;
	private final ResultMessage result;

	public HandshakeWriteEvent(HandshakeType handshakeType, ResultMessage result) {
		this.handshakeType = handshakeType;
		this.result = result;
	}

	public HandshakeType getHandshakeType() {
		return handshakeType;
	}

	public ResultMessage getResult() {
		return result;
	}

}
