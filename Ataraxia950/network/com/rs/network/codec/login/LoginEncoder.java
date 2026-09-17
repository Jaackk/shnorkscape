package com.rs.network.codec.login;

import com.rs.Settings;
import com.rs.network.codec.ResultMessage;
import com.rs.network.handler.message.tail.impl.LoginWriteEvent;
import com.rs.network.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class LoginEncoder extends MessageToByteEncoder<LoginWriteEvent> {

    @Override
    protected void encode(ChannelHandlerContext ctx, LoginWriteEvent msg, ByteBuf out) throws Exception {
        if (!out.isWritable())
            return;
        ResultMessage result = msg.getResult();
        if (result != ResultMessage.LOGIN_GAME_SUCCESS && result != ResultMessage.LOGIN_LOBBY_SUCCESS) {
            out.writeByte(result.getType().getId());
            return;
        }
        if (result == ResultMessage.LOGIN_LOBBY_SUCCESS) {
            OutputStream stream = new OutputStream();
            stream.writePacketVarByte(null, 2);
            stream.writeByte(0);
            stream.writeByte(msg.getPlayer().getRights());
            stream.writeByte(0);
            stream.writeByte(0);
            stream.write24BitInteger(0);
            stream.writeByte(0);
            stream.writeByte(0);
            stream.writeByte(0);
            stream.writeLong(-1);
            stream.write5ByteInteger(12);
            stream.writeByte(msg.getPlayer().isBronzeDonor() ? 0x2 : 0);
            stream.writeInt(1);
            stream.writeInt(0);
            stream.writeShort(msg.getPlayer().isPermMuted() ? 0 : 1);
            stream.writeShort(0);
            stream.writeShort(0);// last logged in date
            stream.writeInt(0);
            stream.writeByte(3);
            stream.writeShort(53791);
            stream.writeShort(53791);
            stream.writeByte(0);
            stream.writeVersionedString(msg.getPlayer().getDisplayName());
            stream.writeByte(0);
            stream.writeInt(msg.getPlayer().getRegionKey());
            int ourWorldIndex = 0;
            for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++)
                if (Settings.WORLDS_INFORMATION[i].getId() == Settings.WORLD_ID) {
                    ourWorldIndex = i;
                    break;
                }
            stream.writeShort(Settings.WORLDS_INFORMATION[ourWorldIndex].getId());
            stream.writeVersionedString(Settings.WORLDS_INFORMATION[ourWorldIndex].getIp());
            stream.writeShort(Settings.SERVER_PORT);
            stream.writeShort(Settings.SERVER_PORT);
            stream.writeShort(Settings.SERVER_PORT);
            stream.endPacketVarByte();
            byte[] data = new byte[stream.getOffset()];
            stream.setOffset(0);
            stream.getBytes(data, 0, data.length);
            out.writeBytes(data);
        } else {
            if (msg.getPlayer().getILayoutVars() == null)
                msg.getPlayer().resetILayoutVars();
            /**
             * Ilayout
             */
            OutputStream stream = new OutputStream();
            stream.writePacketVarShort(null, 2);
            stream.writeByte(1);
            for (int id : msg.getPlayer().getILayoutVars().keySet()) {
                Integer value = msg.getPlayer().getILayoutVars().get(id);
                if (value == null || value == 0)
                    continue;
                stream.writeShort(id);
                stream.writeInt(value);
            }
            stream.endPacketVarShort();

            byte[] data = new byte[stream.getOffset()];
            stream.setOffset(0);
            stream.getBytes(data, 0, data.length);
            out.writeBytes(data);

            /**
             * Login Details
             */
            stream = new OutputStream();
            stream.writePacketVarByte(null, 2);
            stream.writeByte(0);
            stream.writeByte(msg.getPlayer().getRights());
            stream.writeByte(0);
            stream.writeByte(0);
            stream.writeByte(0);
            stream.writeByte(1);
            stream.writeByte(0);
            stream.writeShort(msg.getPlayer().getIndex());
            stream.writeByte(1);
            stream.write24BitInteger(0);
            stream.writeByte(1);
            stream.writeShort(0);
            stream.writeLong(0);
            stream.endPacketVarByte();
            data = new byte[stream.getOffset()];
            stream.setOffset(0);
            stream.getBytes(data, 0, data.length);
            out.writeBytes(data);
        }
    }

}
