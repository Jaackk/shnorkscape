package com.rs.game.player.client;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import java.util.*;

/** Single-use selected-component-to-tile claim, completely separate from ability targeting. */
final class Native950DeveloperPlacement {
    static final int SOURCE=(1448<<16)|11,MAX_RANGE=32;
    static final class Request {
        final Native950DeveloperCatalogue.Entry entry;final int slot,amount,type,rotation,plane;final boolean repeat;
        final long expires;final WorldTile origin;
        Request(Native950DeveloperCatalogue.Entry entry,int slot,int amount,int type,int rotation,boolean repeat,WorldTile origin,long now){
            if(entry==null||slot<1||slot>4095||amount<1||amount>50||rotation<0||rotation>3)throw new IllegalArgumentException("Invalid placement configuration");
            this.entry=entry;this.slot=slot;this.amount=amount;this.type=type;this.rotation=rotation;this.repeat=repeat;
            this.origin=new WorldTile(origin);plane=origin.getPlane();expires=now+60000;
        }
        boolean accepts(int hash,int slot,int item,int x,int y,WorldTile player,long now){
            return hash==SOURCE&&slot==this.slot&&item==-1&&now<=expires&&player.getPlane()==plane
                &&Math.abs(player.getX()-origin.getX())<=MAX_RANGE&&Math.abs(player.getY()-origin.getY())<=MAX_RANGE
                &&Math.abs(player.getX()-x)<=MAX_RANGE&&Math.abs(player.getY()-y)<=MAX_RANGE
                &&Native950SavedLocations.validTile(x,y,plane);
        }
    }
    interface Mutations {
        boolean available(WorldTile tile,int size);
        Object npc(int id,WorldTile tile,boolean repeat);
        Object object(int id,int type,int rotation,WorldTile tile);
        void remove(Object entity);
    }
    /** Preflight all NPC footprints, then roll back only entities created by this request on failure. */
    static List<Object> commit(Request request,int x,int y,Mutations world){
        List<Object> created=new ArrayList<>();
        try{
            if(request.entry.kind.equals("NPC")){
                List<WorldTile> tiles=formation(x,y,request.plane,request.entry.width,request.amount);
                for(WorldTile tile:tiles)if(!nearOrigin(request,tile,request.entry.width,request.entry.width)||!world.available(tile,request.entry.width))throw new IllegalArgumentException("A footprint in this formation is blocked or occupied. Choose a clear area.");
                for(WorldTile tile:tiles)created.add(world.npc(request.entry.id,tile,request.repeat));
            }else {
                int w=request.rotation%2==0?request.entry.width:request.entry.height,h=request.rotation%2==0?request.entry.height:request.entry.width;
                WorldTile tile=new WorldTile(x,y,request.plane);
                if(!nearOrigin(request,tile,w,h))throw new IllegalArgumentException("Entire footprint must remain within 32 tiles of the placement origin.");
                created.add(world.object(request.entry.id,request.type,request.rotation,tile));
            }
            return created;
        }catch(RuntimeException failure){for(Object entity:created)world.remove(entity);throw failure;}
    }
    private static boolean nearOrigin(Request r,WorldTile t,int w,int h){return t.getX()>=r.origin.getX()-MAX_RANGE&&t.getY()>=r.origin.getY()-MAX_RANGE&&t.getX()+w-1<=r.origin.getX()+MAX_RANGE&&t.getY()+h-1<=r.origin.getY()+MAX_RANGE;}
    static List<WorldTile> formation(int x,int y,int plane,int size,int amount){
        if(size<1||size>64||amount<1||amount>50)throw new IllegalArgumentException("Invalid NPC formation");
        int columns=(int)Math.ceil(Math.sqrt(amount));List<WorldTile> tiles=new ArrayList<>();
        for(int n=0;n<amount;n++){
            WorldTile tile=new WorldTile(x+(n%columns)*size,y+(n/columns)*size,plane);
            if(!Native950DiagnosticSpawns.fits(tile,size,size))throw new IllegalArgumentException("Formation crosses the world boundary.");tiles.add(tile);
        }return tiles;
    }
    static Mutations world(Player player){return new Mutations(){
        public boolean available(WorldTile tile,int size){return Native950DiagnosticSpawns.spawnTileAvailable(player,tile,size)&&World.canMoveNPC(tile,size);}
        public Object npc(int id,WorldTile tile,boolean repeat){return Native950DiagnosticSpawns.placeNpc(player,id,tile,repeat);}
        public Object object(int id,int type,int rotation,WorldTile tile){
            int slot=Region.OBJECT_SLOTS[type];WorldObject before=World.getObjectWithSlot(tile,slot);
            String result=Native950DiagnosticSpawns.spawnObjectAt(player,id,type,rotation,tile);
            WorldObject after=World.getObjectWithSlot(tile,slot);
            if(after==null||after==before||after.getId()!=id)throw new IllegalArgumentException(result);return after;
        }
        public void remove(Object entity){
            if(entity instanceof NPC){NPC npc=(NPC)entity;if(Native950DiagnosticSpawns.ownedBy(player,npc)&&World.containsNPC(npc))Native950World.getInstance().removeDiagnosticNpc(npc);}
            else if(entity instanceof WorldObject){WorldObject object=(WorldObject)entity;if(World.getObjectWithSlot(object,Region.OBJECT_SLOTS[object.getType()])==object)World.removeObject(object);}
        }
    };}
}
