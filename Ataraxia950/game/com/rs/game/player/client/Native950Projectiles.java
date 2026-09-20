package com.rs.game.player.client;

import com.rs.game.Entity;
import com.rs.game.Projectile;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.npc.NPC;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** One world-tick event batch, frozen before publication to any viewer. No retained actor pointers. */
final class Native950Projectiles {
    private final Thread owner;
    private final List<Flight> flights=new ArrayList<>();
    Native950Projectiles(Thread owner) { this.owner=owner; }

    void add(Projectile projectile) {
        checkOwner();
        if(flights.size()>=4096)throw new IllegalStateException("Native projectile batch capacity exceeded");
        if(projectile.isAdjustFlyingHeight() || projectile.isAdjustSenderHeight() || projectile.getSenderBodyPart()!=0)
            throw new IllegalArgumentException("Special attachment projectile needs a separately verified encoding");
        flights.add(new Flight(projectile));
    }

    void publish(Player player,Consumer<Packet> send) {
        checkOwner();
        WorldTile loaded=player.getLastLoadedMapRegionTile();
        if(loaded==null)return;
        int width=Native950Packets.SCENE_SIZE;
        publish((loaded.getChunkX()-(width>>4))<<3,(loaded.getChunkY()-(width>>4))<<3,player.getPlane(),width,send);
    }

    void publish(int baseX,int baseY,int plane,int size,Consumer<Packet> send) {
        checkOwner();
        for(Flight f:flights) {
            if(f.plane!=plane || f.x<baseX || f.y<baseY || f.x>=baseX+size || f.y>=baseY+size
                    || f.toX<baseX || f.toY<baseY || f.toX>=baseX+size || f.toY>=baseY+size)continue;
            send.accept(Native950Packets.zonePartialFollows((f.x-baseX)>>3,(f.y-baseY)>>3,plane));
            send.accept(f.packet);
        }
    }

    void clear() {checkOwner();flights.clear();}
    int size() {checkOwner();return flights.size();}
    private void checkOwner() {
        if(Thread.currentThread()!=owner)throw new IllegalStateException("Projectiles belong to the native world thread");
    }

    private static int reference(WorldTile tile) {
        if(!(tile instanceof Entity))return 0;
        int index=((Entity)tile).getIndex();
        if(index<0 || index>65535)throw new IllegalArgumentException("Projectile actor index outside950 domain");
        if(tile instanceof NPC)return 0x10000|index;
        if(tile instanceof Player)return 0x20000|index;
        throw new IllegalArgumentException("Unsupported projectile actor kind");
    }

    private static final class Flight {
        final int x,y,toX,toY,plane;
        final Packet packet;
        Flight(Projectile p) {
            WorldTile from=p.getFrom(),to=p.getTo();
            if(from==null || to==null || from.getPlane()!=to.getPlane())throw new IllegalArgumentException("Projectile plane mismatch");
            // Half-tile centres preserve even-size NPC footprints without rounding a tile early.
            int sx=from.getX()*2+(from instanceof Entity?((Entity)from).getSize():1);
            int sy=from.getY()*2+(from instanceof Entity?((Entity)from).getSize():1);
            int tx=to.getX()*2+(to instanceof Entity?((Entity)to).getSize():1);
            int ty=to.getY()*2+(to instanceof Entity?((Entity)to).getSize():1);
            x=sx>>1;y=sy>>1;toX=tx>>1;toY=ty>>1;plane=from.getPlane();
            packet=Native950Packets.projectileHalfSquare(((sx&15)<<4)|(sy&15),tx-sx,ty-sy,
                    reference(from),reference(to),p.getGraphicId(),p.getStartHeight(),p.getEndHeight(),
                    p.getStartTime(),p.getEndTime(),p.getAngle(),p.getSlope());
        }
    }
}
