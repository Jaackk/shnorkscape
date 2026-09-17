package com.rs.cores;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.network.codec.update.FileDescriptor;
import com.rs.network.codec.update.JS5Priority;
import com.rs.network.codec.update.JS5State;
import com.rs.network.codec.update.JS5UpdateQueue;
import com.rs.network.handler.message.tail.impl.JS5UpdateWriteEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class JS5UpdateThread extends Thread {

	private static final long UPLINK = 1024 * 1024 * 300, UPLINK_MAX = 1024 * 1024 * 2000; // 200 mbps & 2 gbps respectively
	private static final int PRIORITY_CAP = 750, DEFAULT_CAP = 500;

	private static final JS5UpdateThread WORKER_SINGLETON;
	private static final Queue<ChannelHandlerContext> SESSION_QUEUE;

	private static final AttributeKey<Queue<JS5UpdateQueue>> DEFAULT_JS5_KEY, PRIORITY_JS5_KEY, REALTIME_JS5_KEY;
	public static final AttributeKey<Byte> JS5_XOR_KEY;
	public static final AttributeKey<JS5State> JS5_STATE;

	static {
		WORKER_SINGLETON = new JS5UpdateThread();
		WORKER_SINGLETON.start();

		SESSION_QUEUE = new ConcurrentLinkedDeque<>();

		DEFAULT_JS5_KEY = AttributeKey.valueOf("js5_default_queue");
		PRIORITY_JS5_KEY = AttributeKey.valueOf("js5_priority_queue");
		REALTIME_JS5_KEY = AttributeKey.valueOf("js5_realtime_queue");

		JS5_XOR_KEY = AttributeKey.valueOf("js5_xor");
		JS5_STATE = AttributeKey.valueOf("status");
	}

	private JS5UpdateThread() {
	}

	@Override
	public void run() {
		long limit = UPLINK;
		long last_sleep = Utils.currentTimeMillis();

		while (!CoresManager.shutdown) {
			try {
				long t_start = Utils.currentTimeMillis();
				int processed = 0;
				for (ChannelHandlerContext ctx : SESSION_QUEUE) {
					if (ctx == null)
						continue;

					if (!ctx.channel().isActive()) {
						removeSession(ctx);
						continue;
					}

					if (process(limit, ctx) <= 0)
						continue;

					processed++;
				}

				long now = Utils.currentTimeMillis();
				if (processed < 1 || ((now - last_sleep) > 100)) {
					Thread.sleep(1);
					last_sleep = Utils.currentTimeMillis();
				}

				long t_took = Utils.currentTimeMillis() - t_start;
				if (t_took < 1)
					t_took = 1;

				if (processed < 1)
					limit = UPLINK * t_took;
				else
					limit = (UPLINK * t_took) / processed;

				if (limit > UPLINK_MAX)
					limit = UPLINK_MAX;
			} catch (Throwable t) {
				Logger.getGlobal().catching(t);
			}
		}
	}

	public void addFile(ChannelHandlerContext ctx, JS5UpdateQueue message) {
		FileDescriptor descriptor = message.getDescriptor();
		if (descriptor.getType() == 255 && descriptor.getFile() != 255) {
			if (Cache.getJs5Index(descriptor.getFile()) == null) {
				Logger.getGlobal().info("SHIT 1");
				return;
			}
		} else if (descriptor.getType() != 255) {
			Index index = Cache.getJs5Index(descriptor.getType());
			if (index == null || descriptor.getFile() < 0 || !index.archiveExists(descriptor.getFile())) {
				Logger.getGlobal().info("SHIT 2");
				return;
			}
		}

		if (ctx.channel().attr(JS5_STATE).get() == null) {
			removeSession(ctx);
			return;
		}

		Attribute<Queue<JS5UpdateQueue>> queueAttribute = ctx.channel().attr(getAttributeForPriority(message.getPlacementPriority()));
		Queue<JS5UpdateQueue> queue = queueAttribute.get();

		synchronized (queue) {
			queue.offer(message);
		}
	}

	public void addSession(ChannelHandlerContext ctx) {
		ctx.channel().attr(DEFAULT_JS5_KEY).set(new ArrayBlockingQueue<JS5UpdateQueue>(DEFAULT_CAP));
		ctx.channel().attr(PRIORITY_JS5_KEY).set(new ArrayBlockingQueue<JS5UpdateQueue>(PRIORITY_CAP));
		ctx.channel().attr(REALTIME_JS5_KEY).set(new ArrayBlockingQueue<JS5UpdateQueue>(PRIORITY_CAP));
		ctx.channel().attr(JS5_XOR_KEY).set((byte) 0);

		SESSION_QUEUE.add(ctx);
	}

	public void removeSession(ChannelHandlerContext ctx) {
		SESSION_QUEUE.remove(ctx);

		ctx.channel().attr(DEFAULT_JS5_KEY).set(null);
		ctx.channel().attr(PRIORITY_JS5_KEY).set(null);
		ctx.channel().attr(REALTIME_JS5_KEY).set(null);
		ctx.channel().attr(JS5_XOR_KEY).set(null);
		ctx.channel().attr(JS5_STATE).set(null);

	}

	private long process(long limit, ChannelHandlerContext ctx) {
		long total = 0;
		Attribute<Byte> xorAttribute = ctx.channel().attr(JS5_XOR_KEY);
		byte xor = xorAttribute.get();

		Attribute<Queue<JS5UpdateQueue>> realtimeAttribute = ctx.channel().attr(REALTIME_JS5_KEY);
		Queue<JS5UpdateQueue> realtimeQueue = realtimeAttribute.get();
		total += processQueue(limit, xor, ctx, realtimeQueue);

		Attribute<Queue<JS5UpdateQueue>> priorityAttribute = ctx.channel().attr(PRIORITY_JS5_KEY);
		Queue<JS5UpdateQueue> priorityQueue = priorityAttribute.get();
		total += processQueue(limit, xor, ctx, priorityQueue);

		Attribute<Queue<JS5UpdateQueue>> defaultAttribute = ctx.channel().attr(DEFAULT_JS5_KEY);
		Queue<JS5UpdateQueue> defaultQueue = defaultAttribute.get();
		total += processQueue(limit, xor, ctx, defaultQueue);

//		if (total > 0)
//			ctx.channel().flush();

		return total;
	}

	private long processQueue(long limit, byte xor, ChannelHandlerContext ctx, Queue<JS5UpdateQueue> queue) {
		long total = 0;
		synchronized (queue) {
			while (!queue.isEmpty() && total < limit) {
				JS5UpdateQueue msg = queue.peek();
				ByteBuf buf = msg.create(xor);

				if (buf == null) {
					queue.remove();
					continue;
				}

				total += msg.getSent();
//				Logger.getGlobal().info("WRITE: " + msg.getDescriptor().getType() + ", " + msg.getDescriptor().getFile());
				ctx.channel().writeAndFlush(new JS5UpdateWriteEvent(buf));
			}
		}

		return total;
	}

	private static AttributeKey<Queue<JS5UpdateQueue>> getAttributeForPriority(JS5Priority priority) {
		switch (priority) {
		case LOW_PRIORITY:
			return DEFAULT_JS5_KEY;
		case HIGH_PRIORITY:
			return PRIORITY_JS5_KEY;
		case REALTIME:
			return REALTIME_JS5_KEY;

		}
		throw new IllegalStateException("Invalid or null priority requested?");
	}

	public static JS5UpdateThread getSingleton() {
		return WORKER_SINGLETON;
	}
}
