package com.rs.game.player.client;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.util.Collections;
/** Exact950 residual-soul model family; persistent owner state, delivered once per viewer/change. */
final class Native950SoulVisual {
    static final int SLOT=4;
    static int graphic(int souls){if(souls<0||souls>5)throw new IllegalArgumentException("Souls");return souls==0?-1:7865+souls;}
    static void append(Player player,Native950PlayerInfo.Actor.Builder actor){
        int souls=player.getNative950SoulVisual();if(souls<0)return;
        int graphic=graphic(souls);
        if(!Native950PlayerEffects.isVerifiedGraphic(graphic))throw new IllegalStateException("Unverified residual soul graphic "+graphic);
        actor.persistentSpots(souls+1,Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(
            Native950PlayerMasks.Spotanim.of(SLOT,graphic,0,0,0,0,0))));
    }
}
