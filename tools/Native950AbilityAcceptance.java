package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.VarBitDefinitions;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;

/** Isolated real-cache validation, with no connection to the running server or saved characters. */
public final class Native950AbilityAcceptance {
    public static void main(String[] args)throws Exception {
        Cache.initFlatReadOnly(Paths.get("cache"));Native950AbilityAssets.verify();
        int[] types={1,5,6},ids={3,2,3},structures={14682,14664,14727};
        String[] names={"Backhand","Binding Shot","Impact"};
        for(int i=0;i<3;i++){
            int packed=Native950ActionBar.pack(types[i],ids[i]);
            require(Native950ActionBar.struct(packed)==structures[i],"Ability struct");
            require(names[i].equals(Native950ActionBar.name(packed)),"Ability name");
        }
        VarBitDefinitions type=VarBitDefinitions.getClientVarpBitDefinitions(1747),id=VarBitDefinitions.getClientVarpBitDefinitions(1748);
        require(type.baseVar==739&&type.startBit==17&&type.endBit==23,"Type bit slice");
        require(id.baseVar==739&&id.startBit==4&&id.endBit==16,"ID bit slice");
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("ability-test",new WorldTile(3294,10129,0),c);p.setActive(true);
            p.getInterfaceManager().registerNativeOpen(1430,1477,1);
            p.getInterfaceManager().registerNativeOpen(1450,1477,2);
            Native950Actions.DragAction drag=drag(1450,3,3,1430,66);
            require(p.getNative950ActionBar().drag(p,c,drag),"Powers drag handled");
            require(p.nativeSettingsSnapshot().get("actionBar.0")==Native950ActionBar.pack(1,3),"Powers drag saved");
            p.getInterfaceManager().unregisterNativeOpen(1450);
            p.getNative950ActionBar().drag(p,c,drag(1450,3,1,1430,66));
            require(p.nativeSettingsSnapshot().get("actionBar.0")==Native950ActionBar.pack(1,3),"Closed book rejected");
            int[] empty=new int[28];java.util.Arrays.fill(empty,-1);
            Native950Save base=new Native950Save("ability-test",p.getX(),p.getY(),p.getPlane(),empty,new int[28],new int[0],new int[0]);
            java.nio.file.Path directory=java.nio.file.Files.createTempDirectory("native950-actionbar-");
            new Native950SaveStore(directory).save(Native950PlayerBinder.capture(p,base,System.currentTimeMillis()));
            Player restored=Player.createNative950("ability-test",new WorldTile(p),c);
            Native950PlayerBinder.restore(restored,new Native950SaveStore(directory).load("ability-test"));
            require(restored.nativeSettingsSnapshot().get("actionBar.0")==Native950ActionBar.pack(1,3),"Action bar survives disk save and new player restore");
        }finally{c.finishAndReleaseAll();}
        c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("dummy-test",new WorldTile(3294,10129,0),c);p.setActive(true);p.getSkills().set(0,99);p.getSkills().set(2,99);
            NPC dummy=NPC.createNative950Diagnostic(16027,new WorldTile(3295,10129,0));
            final int[] rewardCalls={0};
            Native950MeleeCombat combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
                public void activate(NPC n){}public boolean player(Player v){return v==p;}public boolean npc(NPC n){return n==dummy;}
                public boolean clear(WorldTile t){return true;}public boolean reach(Entity a,Entity b){return true;}
                public boolean approach(Player v,NPC n){return true;}
            },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long d){return true;}public int damage(int max){return max;}},
                v->new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1),new Native950MeleeCombat.Rewards(){
                    public void hit(Player v,NPC n,int damage){rewardCalls[0]++;}public void death(NPC n,Player v){rewardCalls[0]++;}
                });
            combat.attach(p);combat.registerTraining(dummy);int hp=p.getHitpoints();
            require(combat.attack(p,dummy)==null,"Dummy attack");require(combat.ability(p,14682)==null,"Backhand queued");
            combat.beforeMovement();combat.afterMovement();
            require(p.getNextAnimation()!=null&&p.getNextAnimation().getIds()[0]==18154,"Unarmed Backhand uses animation enum, never sprite14212");
            for(int i=1;i<30;i++){combat.beforeMovement();combat.afterMovement();}
            require(dummy.getHitpoints()==100000,"Dummy health restored");require(p.getHitpoints()==hp,"Dummy never retaliates");require(rewardCalls[0]==0,"No dummy XP/loot");
            require(combat.ability(p,14682)==null,"Cooldown expires");combat.detach(p);combat.clear();
        }finally{c.finishAndReleaseAll();}
        for(WorldTile tile:new WorldTile[]{new WorldTile(3294,10129,0),new WorldTile(3107,3298,0)}){
            World.getRegion(tile.getRegionId(),true);
            require(World.isRegionLoaded(tile.getRegionId())&&World.canMoveNPC(tile,1),"Landing tile "+tile.getX()+","+tile.getY());
        }
        System.out.println("PASS: pinned assets, seven definitions, modern varbits, Powers drag and disk-save restore, closed-book rejection, Backhand animation18154, dummy health/no retaliation/no rewards, cooldown expiry and clear hub/exit landing tiles.");
    }
    static Native950Actions.DragAction drag(int sf,int sc,int slot,int tf,int tc){
        int source=sf<<16|sc,target=tf<<16|tc;
        byte[] b={(byte)slot,(byte)(slot>>>8),(byte)(source>>>8),(byte)source,(byte)(source>>>24),(byte)(source>>>16),-1,-1,-1,
            -1,-1,(byte)(target>>>16),(byte)(target>>>24),(byte)target,(byte)(target>>>8),-1,-1,-1};
        return (Native950Actions.DragAction)Native950Actions.decode(12,b);
    }
    static void require(boolean test,String label){if(!test)throw new AssertionError(label);}
}
