package com.rs.network.handler.producer.impl;

import com.rs.network.handler.message.head.impl.GamePacketReadEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import com.rs.network.handler.producer.ChildReadEventProducer;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 *
 *         Created on Nov 2, 2018.
 */
public class GamePacketProducer implements ChildReadEventProducer<GamePacketReadEvent> {

	@Override
	public WriteMessageEvent process(ChannelHandlerContext ctx, GamePacketReadEvent message) throws ClassNotFoundException {
		message.getRepository().processPackets(message.getPacketId(), message.getPacketSize(), message.getPayload());
		return null;
	}

}
