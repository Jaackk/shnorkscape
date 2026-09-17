package com.rs.network.codec.game;

import com.rs.network.handler.message.tail.impl.GamePacketWriteEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class GamePacketEncoder extends MessageToByteEncoder<GamePacketWriteEvent> {

	@Override
	protected void encode(ChannelHandlerContext ctx, GamePacketWriteEvent msg, ByteBuf out) throws Exception {
		if (!out.isWritable() || msg.getBuffer() == null)
			return;

		out.writeBytes(msg.getBuffer());
	}

}
