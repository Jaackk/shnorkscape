package com.rs.network.handler.producer.impl;

import com.rs.network.codec.ProtocolSet;
import com.rs.network.codec.ResultMessage;
import com.rs.network.codec.handshake.HandshakeType;
import com.rs.network.handler.message.head.impl.HandshakeReadEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import com.rs.network.handler.message.tail.impl.HandshakeWriteEvent;
import com.rs.network.handler.producer.ChildReadEventProducer;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class HandshakeProducer implements ChildReadEventProducer<HandshakeReadEvent> {

    @Override
    public WriteMessageEvent process(ChannelHandlerContext ctx, HandshakeReadEvent message) {
        ResultMessage result = ResultMessage.SUCCESS;

        if (message.getRevision() == -1 && message.getUpdate() == -1 && message.getToken() == null)
            return new HandshakeWriteEvent(HandshakeType.UPDATE_CONNECTION_WEB, result);
        if (message.getRevision() != ProtocolSet.REVISION || message.getUpdate() != ProtocolSet.UPDATE || !message.getToken().equals(ProtocolSet.TOKEN))
            result = ResultMessage.VERSION_OUTDATED;

        return new HandshakeWriteEvent(HandshakeType.UPDATE_CONNECTION_REGULAR, result);
    }

}
