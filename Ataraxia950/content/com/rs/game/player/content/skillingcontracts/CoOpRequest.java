package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;

import java.time.LocalTime;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CoOpRequest {
    public final Player requesting;
    public boolean advanced;
    public boolean isShort;
    private LocalTime expired;

    public CoOpRequest(Player requesting) {
        this.requesting = requesting;
    }

    public void start(){
        expired = LocalTime.now().plusMinutes(2);
    }
    public boolean isExpired() {
        if(expired == null)
            return false;
        return LocalTime.now().isAfter(expired);
    }
}