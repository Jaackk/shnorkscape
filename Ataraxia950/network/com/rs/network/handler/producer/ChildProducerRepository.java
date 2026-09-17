package com.rs.network.handler.producer;

import com.google.common.collect.ImmutableMap;
import com.rs.network.handler.message.head.ReadMessageEvent;
import com.rs.network.handler.message.head.impl.GamePacketReadEvent;
import com.rs.network.handler.message.head.impl.HandshakeReadEvent;
import com.rs.network.handler.message.head.impl.JS5UpdateReadEvent;
import com.rs.network.handler.message.head.impl.LoginReadEvent;
import com.rs.network.handler.producer.impl.GamePacketProducer;
import com.rs.network.handler.producer.impl.HandshakeProducer;
import com.rs.network.handler.producer.impl.JS5UpdateProducer;
import com.rs.network.handler.producer.impl.LoginProducer;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class ChildProducerRepository {

    private static final ImmutableMap<Class<?>, ChildReadEventProducer<?>> PRODUCERS = ImmutableMap.of(
            // handshake read event
            HandshakeReadEvent.class, new HandshakeProducer(),
            // js5 update read event
            JS5UpdateReadEvent.class, new JS5UpdateProducer(),
            // login read event
            LoginReadEvent.class, new LoginProducer(),
            // game packet read event
            GamePacketReadEvent.class, new GamePacketProducer());

    @SuppressWarnings("unchecked")
    public static ChildReadEventProducer<ReadMessageEvent> getReadEventProducer(ReadMessageEvent msg) {
        return (ChildReadEventProducer<ReadMessageEvent>) (PRODUCERS.get(msg.getClass()));
    }

}
