package com.rs.network.codec.handshake;

import java.util.List;

import com.rs.Settings;
import com.rs.network.codec.ResultMessage;
import com.rs.network.handler.message.head.impl.HandshakeReadEvent;
import com.rs.network.handler.message.tail.impl.HandshakeWriteEvent;
import com.rs.network.handler.message.tail.impl.HttpWriteEvent;
import com.rs.network.io.ByteBufUtil;
import com.rs.utils.WorldInformation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class HandshakeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable() || in.readableBytes() < 1)
            return;
        in.markReaderIndex();
        int id = in.readUnsignedByte();
        HandshakeType type = HandshakeType.TYPES.get(id);
        if (type != null) {
            switch (type) {
            case UPDATE_CONNECTION_REGULAR:
                if (in.readableBytes() < 1) {
                    in.resetReaderIndex();
                    return;
                }

                int length = in.readUnsignedByte();
                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                    return;
                }

                int revision = in.readInt();
                int update = in.readInt();
                String token = ByteBufUtil.getString(in).trim();
                out.add(new HandshakeReadEvent(revision, update, token));
                break;
            case LOGIN_CONNECTION:
                ctx.channel().writeAndFlush(new HandshakeWriteEvent(type, ResultMessage.SUCCESS));
                break;
            case UPDATE_CONNECTION_WEB:
                in.resetReaderIndex();
                byte[] bytes = new byte[in.readableBytes()];
                in.readBytes(bytes);
                String htmlRequest = new String(bytes);
                if (!htmlRequest.endsWith("\r\n\r\n")) {
                    ctx.channel().close();
                    return;
                }
                try {
                    String location = htmlRequest.substring(htmlRequest.indexOf("GET /") + 5, htmlRequest.indexOf(" HTTP"));
                    String host = htmlRequest.substring(htmlRequest.indexOf("Host: ") + 6, (htmlRequest.contains("Connection") ? (htmlRequest.indexOf("Connection:") - 2) : (htmlRequest.indexOf("Accept:") - 2)));
                    String[] data = (location.contains("?") ? location.substring(location.indexOf("?") + 1) : location).split("&");
                    if (!Settings.DEBUG)
                        Settings.WORLDS_INFORMATION = new WorldInformation[] { new WorldInformation(1, 0, "World 1", 0, 0x2 | 0x8, Settings.SERVER_NAME+" | LIVE", "127.0.0.1") };
                    ctx.channel().writeAndFlush(new HttpWriteEvent(location, host, data));
                } catch (Exception e) {
                    e.printStackTrace();
                    ctx.channel().close();
                }
                break;
            }
        } else
            ctx.channel().close();

    }

}
