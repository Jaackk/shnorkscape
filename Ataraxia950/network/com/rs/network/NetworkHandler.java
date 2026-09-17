package com.rs.network;

import com.rs.Settings;
import com.rs.network.codec.handshake.HandshakeDecoder;
import com.rs.network.codec.handshake.HandshakeEncoder;
import com.rs.network.handler.ReadEventProducer;
import com.rs.utils.Logger;
import com.rs.utils.WorldInformation;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Oct 31, 2018.
 */
public class NetworkHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        String address = ch.remoteAddress().getAddress().getHostAddress();
        if (Settings.WORLDS_INFORMATION == null && Settings.DEBUG)
            Settings.WORLDS_INFORMATION = new WorldInformation[] { new WorldInformation(1, 0, "World 1", 0, 0x2 | 0x8, Settings.SERVER_NAME+" | LIVE", "127.0.0.1") };
        Logger.getGlobal().info("Received connection from {}", address);

        ChannelPipeline pipeline = ch.pipeline();
        if (pipeline == null)
            return;

        pipeline.addLast("decoder", new HandshakeDecoder());
        pipeline.addLast("encoder", new HandshakeEncoder());
        pipeline.addLast("producer", new ReadEventProducer());
    }

}
