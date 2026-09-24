package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import java.util.*;
import java.util.function.BiConsumer;

/** Owner-scoped stationary combat effects. Tile removal must not clear another active area. */
final class Native950CombatAreas {
    private final Map<Player,Map<Integer,WorldTile>> origins=new IdentityHashMap<>();
    private final BiConsumer<WorldTile,Integer> send;
    Native950CombatAreas(BiConsumer<WorldTile,Integer> send){this.send=send;}
    void start(Player owner,int graphic){
        remove(owner,graphic);
        WorldTile origin=new WorldTile(owner);origins.computeIfAbsent(owner,p->new HashMap<>()).put(graphic,origin);send.accept(origin,graphic);
    }
    void remove(Player owner){Map<Integer,WorldTile> map=origins.get(owner);if(map!=null)for(int graphic:new ArrayList<>(map.keySet()))remove(owner,graphic);}
    void remove(Player owner,int graphic){
        Map<Integer,WorldTile> map=origins.get(owner);if(map==null)return;
        WorldTile origin=map.remove(graphic);if(origin==null)return;if(map.isEmpty())origins.remove(owner);
        for(Map<Integer,WorldTile> other:origins.values())for(WorldTile tile:other.values())if(tile.matches(origin))return;
        send.accept(origin,-1);
    }
}
