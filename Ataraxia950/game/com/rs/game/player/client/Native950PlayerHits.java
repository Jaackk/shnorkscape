package com.rs.game.player.client;

import com.rs.game.Hit;
import com.rs.game.player.Player;

/** Potion/environmental hits must not invoke legacy gear, retaliation or death callbacks. */
public final class Native950PlayerHits {
    private Native950PlayerHits() { }
    public static void process(Player player,Hit hit){
        if(!player.isNative950())throw new IllegalArgumentException("Native player required");
        if(player.isDead()||player.hasFinished())return;
        if(hit.getLook()==Hit.HitLook.HEALED_DAMAGE){
            int before=player.getHitpoints();player.heal(Math.max(0,hit.getDamage()));
            hit.setDamage(player.getHitpoints()-before);
        }else if(hit.getLook()==Hit.HitLook.ABSORB_DAMAGE){
            hit.setDamage(0);
        }else{
            int amount=player.isInvulnerable()?0:Math.max(0,Math.min(player.getHitpoints(),hit.getDamage()));
            hit.setDamage(amount);player.setHitpoints(player.getHitpoints()-amount);
            if(amount>0&&hit.getSource()!=null)player.addReceivedDamage(hit.getSource(),amount);
        }
        player.getNextHits().add(hit);
        if(player.getNextHitBars().isEmpty())player.addHitBars();
        player.refreshHitPoints();
        if(player.isDead()&&player.getNative950Combat()!=null)player.getNative950Combat().playerDied(player);
    }
}
