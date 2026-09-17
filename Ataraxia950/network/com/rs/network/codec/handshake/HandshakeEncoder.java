package com.rs.network.codec.handshake;

import com.rs.network.codec.ProtocolSet;
import com.rs.network.codec.ResultMessage;
import com.rs.network.codec.login.LoginDecoder;
import com.rs.network.codec.login.LoginEncoder;
import com.rs.network.codec.update.JS5UpdateDecoder;
import com.rs.network.codec.update.JS5UpdateEncoder;
import com.rs.network.handler.message.tail.impl.HandshakeWriteEvent;
import com.rs.network.handler.message.tail.impl.HttpWriteEvent;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class HandshakeEncoder extends MessageToByteEncoder<HandshakeWriteEvent> {

    @Override
    protected void encode(ChannelHandlerContext ctx, HandshakeWriteEvent msg, ByteBuf out) throws Exception {
        if (!out.isWritable())
            return;
        ResultMessage result = msg.getResult();

//		if (Settings.DEBUG)
//			Logger.getGlobal().info("Connection from "+address+", status="+result.getType()+"");
        if (msg.getHandshakeType() != HandshakeType.UPDATE_CONNECTION_WEB)
            out.writeByte(result.getType().getId());

        if (result == ResultMessage.SUCCESS) {
            ChannelPipeline pipeline = ctx.channel().pipeline();
            switch (msg.getHandshakeType()) {
            case UPDATE_CONNECTION_REGULAR:
                for (int key : ProtocolSet.PREFETCH_KEYS)
                    out.writeInt(key);

                pipeline.addAfter("decoder", "update.encoder", new JS5UpdateEncoder());
                pipeline.replace("decoder", "update.decoder", new JS5UpdateDecoder());
                pipeline.remove("encoder");
                break;
            case LOGIN_CONNECTION:
                out.writeLong(0);
                pipeline.addAfter("decoder", "login.encoder", new LoginEncoder());
                pipeline.replace("decoder", "login.decoder", new LoginDecoder());
                pipeline.remove("encoder");
                break;
            case UPDATE_CONNECTION_WEB:
                HttpWriteEvent event = (HttpWriteEvent) msg;
                ByteBuf output = event.getOutPutData();
                if (output != null)
                    out.writeBytes(event.getOutPutData());
                break;
            }
        } else
            ctx.channel().close();
    }

}
