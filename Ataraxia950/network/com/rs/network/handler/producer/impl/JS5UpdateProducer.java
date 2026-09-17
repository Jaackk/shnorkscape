package com.rs.network.handler.producer.impl;

import com.rs.cores.JS5UpdateThread;
import com.rs.network.codec.update.FileDescriptor;
import com.rs.network.codec.update.JS5UpdateQueue;
import com.rs.network.handler.message.head.impl.JS5UpdateReadEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import com.rs.network.handler.producer.ChildReadEventProducer;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateProducer implements ChildReadEventProducer<JS5UpdateReadEvent> {

	@Override
	public WriteMessageEvent process(ChannelHandlerContext ctx, JS5UpdateReadEvent message) {
		FileDescriptor descriptor = message.getDescriptor();

		JS5UpdateThread.getSingleton().addFile(ctx, new JS5UpdateQueue(descriptor, message.getPlacement(), message.getPriority(), false));

		return null;
		//new JS5UpdateWriteEvent(container, descriptor, message.getPriority());
	}

}
