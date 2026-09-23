package com.rs.game.player.client;

import com.rs.game.WorldTile;
import java.util.*;
import java.util.function.BiPredicate;

/** Compact, deterministic free footprints; never substitutes the occupied owner tile. */
final class Native950ConjureFormation {
    private Native950ConjureFormation() { }
    static WorldTile place(WorldTile owner,int ownerSize,int size,List<WorldTile> occupied,
                           List<Integer> sizes,BiPredicate<WorldTile,Integer> clear) {
        for(int radius=1;radius<=12;radius++) {
            List<WorldTile> ring=new ArrayList<>();
            for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++)
                if(Math.max(Math.abs(dx),Math.abs(dy))==radius)ring.add(new WorldTile(owner.getX()+dx,owner.getY()+dy,owner.getPlane()));
            ring.sort(Comparator.comparingInt(t->Math.abs(t.getX()-owner.getX())+Math.abs(t.getY()-owner.getY())));
            for(WorldTile tile:ring) {
                if(overlap(tile,size,owner,ownerSize)||!clear.test(tile,size))continue;
                boolean free=true;
                for(int i=0;i<occupied.size();i++)if(overlap(tile,size,occupied.get(i),sizes.get(i))){free=false;break;}
                if(free)return tile;
            }
        }
        return null;
    }
    static boolean overlap(WorldTile a,int as,WorldTile b,int bs) {
        return a.getPlane()==b.getPlane()&&a.getX()<b.getX()+bs&&b.getX()<a.getX()+as
                &&a.getY()<b.getY()+bs&&b.getY()<a.getY()+as;
    }
}
