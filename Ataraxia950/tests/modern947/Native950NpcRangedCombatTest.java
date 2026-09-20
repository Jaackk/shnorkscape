package com.rs.game.player.client;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950NpcRangedCombatTest {
    @Test public void rangeAndMagicHitAtImpactNotLaunch() {
        for(int style:new int[]{1,2})try(Fixture f=new Fixture(style,true)) {
            f.combat.attack(f.player,f.npc);f.tick();
            assertEquals(100,f.player.getHitpoints());assertEquals(1,f.projectiles);
            f.tick();f.tick();assertEquals(100,f.player.getHitpoints());
            f.tick();assertEquals(90,f.player.getHitpoints());
            assertEquals(style==1?Hit.HitLook.RANGE_DAMAGE:Hit.HitLook.MAGIC_DAMAGE,
                    f.player.getNextHits().get(0).getLook());
        }
    }
    @Test public void blockedLineOfSightDoesNotLaunchAndStopCancelsInFlightHit() {
        try(Fixture f=new Fixture(1,true)) {
            f.line=false;f.combat.attack(f.player,f.npc);f.tick();
            assertEquals(0,f.projectiles);assertEquals(100,f.player.getHitpoints());
            f.line=true;f.tick();assertEquals(1,f.projectiles);
            f.combat.stop(f.player);for(int i=0;i<6;i++)f.tick();
            assertEquals(100,f.player.getHitpoints());
        }
    }
    @Test public void explicitlyAbsentProjectileStillUsesAuthoredDelayedMagicHit() {
        try(Fixture f=new Fixture(2,false)) {
            f.combat.attack(f.player,f.npc);f.tick();f.tick();
            assertEquals(100,f.player.getHitpoints());assertEquals(0,f.projectiles);
            f.tick();assertEquals(90,f.player.getHitpoints());
        }
    }
    private static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel();
        final Player player=Player.createNative950("npc-style-test",new WorldTile(3217,3258,0),channel);
        final NPC npc=NPC.createNative950(12353,new WorldTile(3222,3258,0),1);
        final Native950MeleeCombat combat;boolean line=true;int projectiles;
        Fixture(int style,boolean projectile) {
            player.setActive(true);player.setIndex(1);npc.setIndex(1);
            Native950CombatStyles.Profile ranged=new Native950CombatStyles.Profile(3,28,1,4,6,-1,-1,0,false);
            combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
                public void activate(NPC n){}public boolean player(Player p){return p==player;}
                public boolean npc(NPC n){return n==npc;}public boolean clear(WorldTile t){return true;}
                public boolean reach(Entity a,Entity b){return false;}
                public boolean rangedReach(Player p,NPC n,int range){return line;}
                public boolean npcRangedReach(NPC n,Player p,int range){assertEquals(7,range);return line;}
                public boolean approach(Player p,NPC n){return true;}
                public void projectile(Projectile p){projectiles++;assertSame(npc,p.getFrom());assertSame(player,p.getTo());assertEquals(72,p.getEndTime());}
            },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long d){return true;}public int damage(int max){return 1;}},
                    p->new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1,ranged));
            combat.attach(player);
            combat.register(npc,new Native950NpcCombatProfile(12353,1,1,1000,1,1,10,10,1,4,-1,-1,-1,0,0)
                    .withAttackStyle(style,projectile?100:-1,-1));
        }
        void tick(){combat.beforeMovement();combat.afterMovement();}
        public void close(){combat.clear();channel.finishAndReleaseAll();}
    }
}
