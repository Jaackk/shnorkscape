package com.rs.network.handler.producer;

import com.rs.network.handler.message.head.ReadMessageEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 * Created on Oct 31, 2018.
 */
public interface ChildReadEventProducer<R extends ReadMessageEvent> {
	
	WriteMessageEvent process(ChannelHandlerContext ctx, R message) throws ClassNotFoundException;

}
