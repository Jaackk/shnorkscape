package com.rs.network.codec.game;

import com.rs.network.handler.message.head.impl.GamePacketReadEvent;
import com.rs.network.io.InputStream;
import com.rs.network.packet.PacketRepository;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Nov 2, 2018.
 */
public class GamePacketDecoder extends ReplayingDecoder<GamePacketState> {

    private static final InputStream EMPTY_STREAM = new InputStream(0);
    private final PacketRepository repository;
    private int packetId;
    private int packetSize;

    public GamePacketDecoder(PacketRepository repository) {
        super(GamePacketState.READ_ID);
        this.repository = repository;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_ID:
                packetId = in.readUnsignedByte();
                if (packetId > Byte.MAX_VALUE) {
                    checkpoint(GamePacketState.READ_ID_2);
                } else {
                    checkPacketSize(out);
                }
                break;
            case READ_ID_2:
                // Logically this shouldn't ever be called... but I'm including it just in case so nothing breaks.
                packetId += in.readUnsignedByte();
                checkPacketSize(out);
                break;
            case READ_SIZE:
                if (packetSize == -1)
                    packetSize = in.readUnsignedByte();
                else if (packetSize == -2)
                    packetSize = in.readUnsignedShort();
                else if (packetSize == -3)
                    packetSize = in.readInt();
                else
                    throw new IllegalStateException("Unexpected packet size decode id: " + packetSize);

                if (packetSize > 0) {
                    checkpoint(GamePacketState.READ_PAYLOAD);
                } else if (packetSize == 0) {
                    // No size or payload to decode, ship message right away!
                    out.add(new GamePacketReadEvent(packetId, packetSize, EMPTY_STREAM, repository));
                    checkpoint(GamePacketState.READ_ID);
                } else {
                    throw new IllegalStateException("Packet size <= 0 still.");
                }
                break;
            case READ_PAYLOAD:
                byte[] bytes = new byte[packetSize];
                in.readBytes(bytes, 0, packetSize);
                out.add(new GamePacketReadEvent(packetId, packetSize, new InputStream(bytes), repository));
                checkpoint(GamePacketState.READ_ID);
                break;
        }
    }

    private void checkPacketSize(List<Object> out) {
        packetSize = PacketRepository.PACKET_SIZES[packetId];
        if (packetSize == 0) {
            // No size or payload to decode, ship message right away!
            out.add(new GamePacketReadEvent(packetId, packetSize, EMPTY_STREAM, repository));
            checkpoint(GamePacketState.READ_ID);
        } else if (packetSize > 0) {
            // We know the size, skip to payload.
            checkpoint(GamePacketState.READ_PAYLOAD);
        } else {
            // Otherwise, we need to decode the size.
            checkpoint(GamePacketState.READ_SIZE);
        }
    }

    public PacketRepository getRepository() {
        return repository;
    }

}
