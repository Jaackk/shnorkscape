package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.VarBitDefinitions;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Offline real-cache gate. No live sessions, profiles or character files are accessed. */
public final class Native950AbilityPassAcceptance {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));Native950AbilityAssets.verify();
        for(int id:new int[]{36453,36454}){
            VarBitDefinitions v=VarBitDefinitions.getClientVarpBitDefinitions(id);
            check(v.varDomain==0&&v.baseVar==3705&&v.startBit==(id==36453?24:28)&&v.endBit==v.startBit+3,"Book category slice");
        }
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("ability-offline",new WorldTile(3217,3258,0),c);p.setActive(true);
            for(int face:new int[]{1880,1883}){
                int bit=face==1880?36453:36454;
                p.getVarsManager().setVarBit(bit,0);check(Native950ActionBar.bookType(p,face,1)==3,"Defence category");
                p.getVarsManager().setVarBit(bit,1);check(Native950ActionBar.bookType(p,face,1)==4,"Constitution category");
                p.getVarsManager().setVarBit(bit,2);check(Native950ActionBar.bookType(p,face,1)==-1,"Invalid category refused");
            }
            check(Native950ActionBar.struct(Native950ActionBar.pack(3,10))==14719,"Barricade book");
            check(Native950ActionBar.struct(Native950ActionBar.pack(4,10))==24188,"Sacrifice book");
            Map<String,Integer> old=new HashMap<>();old.put("actionBar.0",Native950ActionBar.pack(3,10));old.put("actionBar.1",Native950ActionBar.pack(4,10));
            Native950ActionBar a=new Native950ActionBar();a.restore(old);
            Map<String,Integer> saved=new HashMap<>();a.writeSettings(saved);Native950ActionBar b=new Native950ActionBar();b.restore(saved);
            check(b.slot(0,0)==Native950ActionBar.pack(3,10)&&b.slot(0,1)==Native950ActionBar.pack(4,10),"Defensive bindings survive save format");
        }finally{c.finishAndReleaseAll();}
        int executed=0;
        for(Native950AbilityCatalog.Definition d:Native950AbilityCatalog.DEFINITIONS){
            if(d.effect!=Native950AbilityCatalog.Effect.BUFF)continue;
            c=new EmbeddedChannel();Native950MeleeCombat combat=null;
            try{
                Player p=Player.createNative950("ability-offline",new WorldTile(3217,3258,0),c);p.setActive(true);
                for(int skill=0;skill<Skills.SKILL_NAME.length;skill++)p.getSkills().setLevelWithoutRefresh(skill,99);
                p.getEquipment().getItems().set(5,new Item(13740));
                check(Native950MeleeCombat.hasNativeShield(p)&&Native950MeleeCombat.shieldLevel(p)==75,"Cache-backed spirit shield classification: shield="+Native950MeleeCombat.hasNativeShield(p)+" level="+Native950MeleeCombat.shieldLevel(p));
                p.getCombatDefinitions().setSpecialAttackPercentage(100);
                final int style=Math.max(0,d.style());
                combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
                    public void activate(NPC n){}
                    public boolean player(Player v){return v==p;}public boolean npc(NPC n){return false;}
                    public boolean clear(WorldTile t){return true;}public boolean reach(Entity a,Entity b){return true;}
                    public boolean approach(Player v,NPC n){return false;}
                },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long b){return true;}public int damage(int max){return max;}},
                    v->new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1,new Native950CombatStyles.Profile(style,style==2?6:style==1?4:0,99,4,8,-1,-1,0,true)),
                    new Native950MeleeCombat.Rewards(){public void hit(Player p,NPC n,int damage){}public void death(NPC n,Player p){}});
                combat.attach(p);String result=combat.ability(p,d.struct);check(result==null,d.name+" rejected: "+result);
                combat.beforeMovement();combat.afterMovement();
                check(c.isOpen(),d.name+" execution closed session");
                check(p.getCombatDefinitions().getSpecialAttackPercentage()==100-d.adrenalineCost(),d.name+" cost");
                executed++;
            }finally{if(combat!=null)combat.clear();c.finishAndReleaseAll();}
        }
        System.out.println("ABILITY PASS ACCEPTED: "+Native950AbilityCatalog.DEFINITIONS.size()+" cache definitions, "+executed+" self buffs executed; defensive books/bindings verified. No live writes.");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
