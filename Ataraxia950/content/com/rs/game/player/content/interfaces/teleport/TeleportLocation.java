package com.rs.game.player.content.interfaces.teleport;

import com.rs.game.WorldTile;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Paolo, Discord Shnek#6969 22/01/2019
 */
public class TeleportLocation implements Serializable {

    @Getter private String name;
    @Getter @Setter private WorldTile teleTile;
    @Getter @Setter private String controller;
    @Getter @Setter private boolean isWilderness;
    private static final long serialVersionUID = -2472145743756595742L;

    public TeleportLocation(String name, WorldTile tile, String controller, boolean isWilderness){
        this.name = name;
        this.teleTile = tile;
        this.controller = controller;
        this.isWilderness = isWilderness;
    }

    public TeleportLocation(String name, WorldTile tile){
             this(name,tile,null,false);
    }

    public TeleportLocation(String name, WorldTile tile, boolean isWilderness){ this(name,tile,null,isWilderness); }

    public TeleportLocation(String name, WorldTile tile, String controller){ this(name,tile,controller,false); }

    public void setName(String name) {
        this.name = name;
    }
}
