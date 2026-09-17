package com.rs.network.codec.update;

import com.rs.network.handler.message.tail.impl.JS5UpdateWriteEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateEncoder extends MessageToByteEncoder<JS5UpdateWriteEvent> {

	@Override
	protected void encode(ChannelHandlerContext ctx, JS5UpdateWriteEvent msg, ByteBuf out) throws Exception {
		if (!out.isWritable())
			return;

		if (msg.getBuffer() == null)
			return;

		out.writeBytes(msg.getBuffer());
	}

}
