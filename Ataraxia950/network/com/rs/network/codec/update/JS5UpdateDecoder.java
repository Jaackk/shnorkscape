package com.rs.network.codec.update;

import com.rs.cores.JS5UpdateThread;
import com.rs.network.handler.message.head.impl.JS5UpdateReadEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateDecoder extends ReplayingDecoder<JS5State> {

    /**
     * Constructs a new class.
     */
    public JS5UpdateDecoder() {
        super(null);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable())
            return;
        if (state() == null) {
            in.readUnsignedByte();
            checkpoint(JS5State.STATE_SELECTOR);
            return;
        }
        switch (state()) {
        case STATE_SELECTOR:
            int op = in.readUnsignedByte();
            checkpoint(op == 17 || op == 33 ? JS5State.FILE_REQUEST_HIGH_PRIORITY : op == 32 ? JS5State.FILE_REQUEST_LOW_PRIORITY : JS5State.values()[op]);
            break;
        case FILE_REQUEST_HIGH_PRIORITY:
        case FILE_REQUEST_LOW_PRIORITY:
            int type = in.readUnsignedByte();
            int file = in.readInt();
            FileDescriptor desc = new FileDescriptor(type, file);
            JS5Priority priority = state().equals(JS5State.FILE_REQUEST_LOW_PRIORITY) ? JS5Priority.LOW_PRIORITY : JS5Priority.HIGH_PRIORITY;
            out.add(new JS5UpdateReadEvent(desc, ctx.channel().attr(JS5UpdateThread.JS5_STATE).get().equals(JS5State.FILE_REQUEST_ONLINE) ? JS5Priority.values()[priority.ordinal() + 1] : priority, priority));
            checkpoint(JS5State.STATE_SELECTOR);
            break;
        case FILE_REQUEST_IDLE:
        case FILE_REQUEST_ONLINE:
            ctx.channel().attr(JS5UpdateThread.JS5_STATE).set(state());
            in.skipBytes(5);
            checkpoint(JS5State.STATE_SELECTOR);
            break;
        case FILE_REQUEST_COMPLETE:
        case FILE_REQUEST_EMPTY:
            JS5UpdateThread.getSingleton().removeSession(ctx);

            in.skipBytes(5);
            checkpoint(JS5State.STATE_SELECTOR);
            break;
        case FILE_REQUEST_STARTUP:
            JS5UpdateThread.getSingleton().addSession(ctx);

            in.skipBytes(5);
            checkpoint(JS5State.STATE_SELECTOR);
            break;
        case FILE_REQUEST_XOR:
            ctx.channel().attr(JS5UpdateThread.JS5_XOR_KEY).set(in.readByte());

            in.skipBytes(4);
            checkpoint(JS5State.STATE_SELECTOR);
            break;
        }
    }

}
