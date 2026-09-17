package com.rs.network.codec.login;

import java.net.InetSocketAddress;
import java.util.List;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.network.NetworkChannelFilter;
import com.rs.network.codec.ProtocolSet;
import com.rs.network.codec.ResultMessage;
import com.rs.network.handler.message.head.impl.LoginReadEvent;
import com.rs.network.io.ByteBufUtil;
import com.rs.network.io.XTEA;
import com.rs.utils.Encrypt;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.MACBan;
import com.rs.utils.MachineInformation;
import com.rs.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 *         <p>
 *         Created on Nov 2, 2018.
 */
public class LoginDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable() || in.readableBytes() < 3)
            return;
        int id = in.readUnsignedByte();
        if (id != 16 && id != 18 && id != 19) {
            out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
            ctx.channel().close();
            return;
        }

        int size = in.readUnsignedShort();

        if (in.readableBytes() != size) {
            out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
            ctx.channel().close();
            return;
        }
        int revision = in.readInt();
        int update = in.readInt();
        if (revision != ProtocolSet.REVISION || update != ProtocolSet.UPDATE) {
            out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
            return;
        }
        if (id == 19) {// Lobby Login
            if (World.exiting_start != 0) {
                out.add(new LoginReadEvent(ResultMessage.WORLD_UPDATE));
                return;
            }
            int secureLength = in.readUnsignedShort();
            if (secureLength > in.readableBytes()) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            byte[] secureBytes = new byte[secureLength];
            in.readBytes(secureBytes, 0, secureLength);
            ByteBuf securePayload = Unpooled.wrappedBuffer(Utils.cryptRSA(secureBytes, ProtocolSet.LOGIN_EXPONENT, ProtocolSet.LOGIN_MODULUS));
            if (securePayload == null) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            int opcode = securePayload.readUnsignedByte();
            if (opcode != 10) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            int[] seeds = new int[4];
            for (int i = 0; i < seeds.length; i++)
                seeds[i] = securePayload.readInt();

            long hash = securePayload.readLong();

            if (hash != 0L) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.close();
                return;
            }
            securePayload.readByte();
            int unknown = securePayload.readInt();
            @SuppressWarnings("unused")
            boolean usingNXT = unknown != 0L;
            securePayload.readByte();
            String password = ByteBufUtil.getString(securePayload);

            if (password.length() > 30 || password.length() < 3) {
                out.add(ResultMessage.CREDENTIALS_UNSECURE);
                return;
            }

            password = Encrypt.encryptSHA1(password);
//            securePayload.readLong();
//            securePayload.readLong();

            byte[] block = new byte[super.actualReadableBytes()];
            in.readBytes(block);
            ByteBuf stream = Unpooled.wrappedBuffer(XTEA.decipher(seeds, block));
            boolean userAsString = (stream.readUnsignedByte() == 1);
            String username = Utils.formatPlayerNameForProtocol(userAsString ? ByteBufUtil.getString(stream) : Utils.longToString(stream.readLong()));
            stream.readUnsignedShort();
            int displayMode = stream.readUnsignedByte();
            stream.readUnsignedShort();// screenWidth
            stream.readUnsignedShort();// screenHeight
            stream.readUnsignedByte();
            stream.skipBytes(24);
            String settings = ByteBufUtil.getString(stream);
            if (!settings.equals(ProtocolSet.SETTINGS)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            stream.skipBytes(stream.readUnsignedByte());
            MachineInformation mInformation = decodeMachineInformation(stream);
            if (mInformation == null) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                return;
            }
            stream.readInt();
            String token = ByteBufUtil.getString(stream);
            if (!token.equals(ProtocolSet.WORLD_TOKEN)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }

            stream.readInt();
            int clientLoginId = stream.readInt();
            if (clientLoginId != ProtocolSet.CLIENT_LOGIN_ID) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            String grabServerToken = ByteBufUtil.getString(stream);
            if (!grabServerToken.equals(ProtocolSet.TOKEN)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            stream.readShort();
            int[] checkIndexes = { 0, 1, 2, 3, 5, 7, 8, 10, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 40, 41, 42, 47, 48, 49, 52, 53, 54, 55, 5 };
            for (int index : checkIndexes) {
                if (index == 32)
                    continue;
                int crc = Cache.STORE.getIndexes()[index] == null ? 0 : Cache.STORE.getIndexes()[index].getCRC();
                int receivedCRC = stream.readInt();
                if (index == 0)
                    usingNXT = receivedCRC != crc;
                if (crc != receivedCRC && index < 31 && index != 0 && index != 5 && index != 7 && index != 10 && index != 24 && index != 25 && index != 30) { // outdated
                    out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                    return;
                }
            }
            String mac = "";
            // String ipaddress = ctx.channel().remoteAddress().toString().substring(1,
            // ctx.channel().remoteAddress().toString().indexOf(":"));
            String address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            ResultMessage filterResult = NetworkChannelFilter.accept(ctx.channel(), address);
            if (filterResult != ResultMessage.LOGIN_GAME_SUCCESS) {
                out.add(new LoginReadEvent(filterResult));
                return;
            }
            if (Utils.invalidAccountName(username) || Utils.badName(username)) {
                out.add(new LoginReadEvent(ResultMessage.CREDENTIALS_PROFANITY));
                return;
            }
            if (World.getPlayers().size() >= ProtocolSet.PLAYER_CAP) {
                out.add(new LoginReadEvent(ResultMessage.WORLD_CAPACITY));
                Logger.getGlobal().info("???");
                return;
            }
            if (!mac.equals("") && MACBan.checkMac(mac)) {
                LoggingSystem.logFailedMac(username, mac, address);
                out.add(new LoginReadEvent(ResultMessage.CREDENTIALS_BLACKLISTED_MACBAN));
                return;
            }
            if (World.containsPlayerLobby(username) || World.containsPlayer(username)) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_ACTIVE));
                return;
            }
            out.add(new LoginReadEvent(username, password, seeds, address, mac, displayMode, usingNXT, ResultMessage.LOGIN_LOBBY_SUCCESS));
        } else if (id == 16 || id == 18) {// GameLogin
            if (World.exiting_start != 0) {
                out.add(new LoginReadEvent(ResultMessage.WORLD_UPDATE));
                return;
            }
            in.readUnsignedByte();
            int secureLength = in.readUnsignedShort();
            if (secureLength > in.readableBytes()) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            byte[] secureBytes = new byte[secureLength];
            in.readBytes(secureBytes, 0, secureLength);
            ByteBuf securePayload = Unpooled.wrappedBuffer(Utils.cryptRSA(secureBytes, ProtocolSet.LOGIN_EXPONENT, ProtocolSet.LOGIN_MODULUS));
            if (securePayload == null) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            int opcode = securePayload.readUnsignedByte();
            if (opcode != 10) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.channel().close();
                return;
            }

            int[] seeds = new int[4];
            for (int i = 0; i < seeds.length; i++)
                seeds[i] = securePayload.readInt();

            long hash = securePayload.readLong();

            if (hash != 0L) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                ctx.close();
                return;
            }
            securePayload.readByte();
            int unknown = securePayload.readInt();
            @SuppressWarnings("unused")
            boolean usingNXT = unknown != 0L;
            securePayload.readByte();
            String password = ByteBufUtil.getString(securePayload);
            if (password.length() > 30 || password.length() < 3) {
                out.add(ResultMessage.CREDENTIALS_UNSECURE);
                return;
            }

            password = Encrypt.encryptSHA1(password);
//            securePayload.readLong();
//            securePayload.readLong();

            byte[] block = new byte[super.actualReadableBytes()];
            in.readBytes(block);
            ByteBuf stream = Unpooled.wrappedBuffer(XTEA.decipher(seeds, block));
            boolean userAsString = (stream.readUnsignedByte() == 1);
            String username = Utils.formatPlayerNameForProtocol(userAsString ? ByteBufUtil.getString(stream) : Utils.longToString(stream.readLong()));
            int displayMode = stream.readUnsignedByte();
            stream.readUnsignedShort();// screenWidth
            stream.readUnsignedShort();// screenHeight
            stream.readUnsignedByte();
            stream.skipBytes(24);
            String settings = ByteBufUtil.getString(stream);
            if (!settings.equals(ProtocolSet.SETTINGS)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            stream.readInt();
            stream.skipBytes(stream.readUnsignedByte());
            MachineInformation mInformation = decodeMachineInformation(stream);
            if (mInformation == null) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_BAD));
                return;
            }
            stream.readInt();
            stream.readLong();
            stream.readLong();
            String token = ByteBufUtil.getString(stream);
            if (!token.equals(ProtocolSet.WORLD_TOKEN)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            if (stream.readUnsignedByte() == 1)
                ByteBufUtil.getString(stream);
            stream.readMedium();
            int clientLoginId = stream.readInt();
            if (clientLoginId != ProtocolSet.CLIENT_LOGIN_ID) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            String grabServerToken = ByteBufUtil.getString(stream);
            if (!grabServerToken.equals(ProtocolSet.TOKEN)) {
                out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                return;
            }
            stream.readByte();
            stream.readShort();
            int[] checkIndexes = { 0, 1, 2, 3, 5, 7, 8, 10, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 40, 41, 42, 47, 48, 49, 52, 53, 54, 55, 5 };
            for (int index : checkIndexes) {
                if (index == 32)
                    continue;
                int crc = Cache.STORE.getIndexes()[index] == null ? 0 : Cache.STORE.getIndexes()[index].getCRC();
                int receivedCRC = stream.readInt();
                if (index == 0)
                    usingNXT = receivedCRC != crc;
                if (crc != receivedCRC && index < 31 && index != 0 && index != 5 && index != 7 && index != 10 && index != 24 && index != 25 && index != 30) { // outdated
                    out.add(new LoginReadEvent(ResultMessage.VERSION_OUTDATED));
                    return;
                }
            }
            String mac = "";
            // String ipaddress = ctx.channel().remoteAddress().toString().substring(1,
            // ctx.channel().remoteAddress().toString().indexOf(":"));
            String address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

            ResultMessage filterResult = NetworkChannelFilter.accept(ctx.channel(), address);
            if (filterResult != ResultMessage.LOGIN_GAME_SUCCESS) {
                out.add(new LoginReadEvent(filterResult));
                return;
            }
            if (Utils.invalidAccountName(username) || Utils.badName(username)) {
                if (username.startsWith("_") || username.endsWith("_")) {
                    out.add(new LoginReadEvent(ResultMessage.CREDENTIALS_INVALID));
                    return;
                }
                out.add(new LoginReadEvent(ResultMessage.CREDENTIALS_PROFANITY));
                return;
            }
            if (World.getPlayers().size() >= ProtocolSet.PLAYER_CAP) {
                out.add(new LoginReadEvent(ResultMessage.WORLD_CAPACITY));
                Logger.getGlobal().info("???");
                return;
            }
            if (!mac.equals("") && MACBan.checkMac(mac)) {
                LoggingSystem.logFailedMac(username, mac, address);
                out.add(new LoginReadEvent(ResultMessage.CREDENTIALS_BLACKLISTED_MACBAN));
                return;
            }
            if (World.containsPlayer(username)) {
                out.add(new LoginReadEvent(ResultMessage.SESSION_ACTIVE));
                return;
            }

            out.add(new LoginReadEvent(username, password, seeds, address, mac, displayMode, usingNXT, ResultMessage.LOGIN_GAME_SUCCESS));
        }

    }

    @SuppressWarnings("unused")
    public MachineInformation decodeMachineInformation(ByteBuf stream) {
        stream.readUnsignedByte();
        int os = stream.readUnsignedByte();
        boolean x64Arch = stream.readUnsignedByte() == 1;
        int osVersion = stream.readUnsignedByte();
        int osVendor = stream.readUnsignedByte();
        int javaVersion = stream.readUnsignedShort();
        int javaVersionBuild = stream.readUnsignedByte();
        int javaVersionBuild2 = stream.readUnsignedByte();
        boolean hasApplet = stream.readUnsignedByte() == 1;
        int heap = stream.readUnsignedShort();
        int availableProcessors = stream.readUnsignedByte();
        int ram = stream.readMedium();
        int cpuClockFrequency = stream.readUnsignedShort();
        stream.readByte();
        String graphicCardManufactor = ByteBufUtil.getString(stream);
        stream.readByte();
        String graphicCardName = ByteBufUtil.getString(stream);
        stream.readByte();
        String empty3 = ByteBufUtil.getString(stream);
        stream.readByte();
        String dxVersion = ByteBufUtil.getString(stream);
        int graphicCardReleaseMonth = stream.readUnsignedByte();
        int graphicCardReleaseYear = stream.readUnsignedShort();
        stream.readByte();
        String cpuManufactor = ByteBufUtil.getString(stream);
        stream.readByte();
        String cpuName = ByteBufUtil.getString(stream);
        int unused3 = stream.readUnsignedByte(); // aspect ratio i think
        int unused4 = stream.readUnsignedByte();// aspect ratio i think
        int[] u = new int[3];
        for (int i = 0; i < u.length; i++)
            u[i] = stream.readInt();
        int unused5 = stream.readInt();
        stream.readByte();
        String empty4 = ByteBufUtil.getString(stream);
        return new MachineInformation(os, x64Arch, osVersion, osVendor, javaVersion, javaVersionBuild, javaVersionBuild2, hasApplet, heap, availableProcessors, ram, cpuClockFrequency, 0, 0, 0); // TODO
        // update
        // this
        // later
    }

}
