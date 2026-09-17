package com.rs.network;

import com.rs.Settings;
import com.rs.utils.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class NetworkBootstrapper {

	public static void initAndStart() {
		NioEventLoopGroup boss = new NioEventLoopGroup();
		NioEventLoopGroup worker = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(boss, worker);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new NetworkHandler());
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

		ChannelFuture future = bootstrap.bind(new InetSocketAddress(Settings.SERVER_PORT));
		try {
			if (future.await(3000))// 3 seconds
				future.sync();
		} catch (InterruptedException e) {
			Logger.getGlobal().catching(e);
			System.exit(0);
		}
	}

}
