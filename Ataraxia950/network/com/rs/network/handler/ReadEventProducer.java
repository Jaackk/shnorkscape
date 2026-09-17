package com.rs.network.handler;

import com.google.common.collect.ImmutableSet;
import com.rs.game.player.Player;
import com.rs.network.handler.message.head.ReadMessageEvent;
import com.rs.network.handler.message.head.impl.GamePacketReadEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import com.rs.network.handler.producer.ChildProducerRepository;
import com.rs.network.handler.producer.ChildReadEventProducer;
import com.rs.utils.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class ReadEventProducer extends SimpleChannelInboundHandler<ReadMessageEvent> {

    private static final ImmutableSet<String> IGNORED = ImmutableSet.of("An established connection was aborted by the software in your host machine", "An existing connection was forcibly closed by the remote host", "Connection reset by peer", "GamePacketDecoder.decode() did not read anything but decoded a message.");
    public static final AttributeKey<ReadMessageEvent> CURRENT_READ_EVENT = AttributeKey.valueOf("readEvent");

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().flush();
    }

    @Override
	protected void channelRead0(ChannelHandlerContext ctx, ReadMessageEvent msg) throws Exception {
		ChildReadEventProducer<ReadMessageEvent> producer = ChildProducerRepository.getReadEventProducer(msg);
		if (producer == null)
			return;

		// this is done just as extra measures.
		if (msg.isTerminated())
			return;
        ctx.channel().attr(CURRENT_READ_EVENT).set(msg);
		WriteMessageEvent writeEvent = producer.process(ctx, msg);
		msg.terminate();
		
		if (writeEvent == null)
			return;

		if (writeEvent.isTerminated())
			return;
		
		ctx.channel().writeAndFlush(writeEvent);
		writeEvent.terminate();
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String causeMsg = cause.getMessage();
        if (causeMsg != null && IGNORED.contains(causeMsg)) {
            return;
        }
        Logger.getGlobal().catching(cause);
        // The player should probably be disconnected here lol, but don't want to change
        // functionality.
    }


}
