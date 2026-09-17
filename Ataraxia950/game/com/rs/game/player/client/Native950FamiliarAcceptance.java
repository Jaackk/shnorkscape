package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.game.*;import com.rs.game.item.Item;import com.rs.game.npc.NPC;import com.rs.game.player.*;import io.netty.channel.embedded.EmbeddedChannel;import java.nio.file.Paths;import java.util.*;import java.util.concurrent.TimeUnit;
/** Disposable current-cache companion lifecycle, container transactions and owner tests; no account files. */
public final class Native950FamiliarAcceptance {
    static int checks;
    static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    public static void main(String[]a)throws Exception{
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
        Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
        System.out.println("PASS familiar lifecycle: "+checks+" actual-cache checks; no authenticated client or rendered pixels tested.");
    }
    static void run(){
        Native950Familiars.verifyCacheBindings();check(Native950Familiars.profiles().size()==85,"Unexpected ordinary familiar count");
        EmbeddedChannel channel=new EmbeddedChannel(),otherChannel=new EmbeddedChannel();
        Player p=Player.createNative950("fam-probe",new WorldTile(3217,3258,0),channel),other=Player.createNative950("fam-other",new WorldTile(3218,3258,0),otherChannel);
        p.setActive(true);other.setActive(true);World.addNative950Player(p,1);World.addNative950Player(other,2);World.updateEntityRegion(p);World.updateEntityRegion(other);p.loadMapRegions();p.resetMasks();Native950World.installVarpSink(p);
        Native950Containers c=new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops());Native950Skilling.attach(p,c);
        try{
            p.getSkills().setXpWithoutRefresh(Skills.SUMMONING,Skills.getXPForLevel(Skills.SUMMONING,99));p.getSkills().setLevelWithoutRefresh(Skills.SUMMONING,99);
            for(Native950Familiars.Profile profile:Native950Familiars.profiles()){
                p.getInventory().items.set(0,new Item(profile.pouchId,1));p.getSkills().restoreSummoning();
                check(Native950Familiars.summon(p,0,profile)==null,"Cannot summon "+profile.name);
                NPC familiar=Native950Familiars.current(p);check(familiar!=null&&familiar.getId()==profile.npcId&&!familiar.hasMenuOption("Attack")&&World.containsNPC(familiar),"Incorrect or attackable familiar "+profile.name);
                check(p.getInventory().getItem(0)==null&&p.getSkills().getLevel(Skills.SUMMONING)==99-profile.pointCost,"Summon did not charge exactly one pouch and points");
                Native950Familiars.clear(p);check(!World.containsNPC(familiar)&&!Native950Familiars.isFamiliar(familiar),"Dismiss leaked familiar");
            }
            Native950Familiars.Profile wolf=Native950Familiars.profile(12047);check(wolf.npcId==6829&&wolf.durationTicks==1600,"Wolf uses910 PvP id or lifetime");
            p.getInventory().items.set(0,new Item(12047,1));p.getInventory().items.set(1,new Item(12047,1));p.getSkills().setLevelWithoutRefresh(Skills.SUMMONING,0);
            check(Native950Familiars.summon(p,0,wolf)!=null&&p.getInventory().getItem(0)!=null&&p.getInventory().getItem(1)!=null&&World.getNPCs().isEmpty(),"Insufficientpoints consumed or spawned");
            p.getSkills().restoreSummoning();check(Native950Familiars.summon(p,0,wolf)==null,"Wolf summon failed");NPC npc=Native950Familiars.current(p);
            check(!Native950Familiars.controls(other,npc),"Other player controls familiar");Native950Familiars.dismiss(other,npc);check(World.containsNPC(npc),"Other player dismissed familiar");
            check(Native950Familiars.summon(p,0,wolf)!=null&&p.getInventory().getItem(0)==null&&p.getInventory().getItem(1)!=null,"Duplicate summon consumed pouch");
            Native950Familiars.tick(p);int before=Native950Familiars.snapshot(p)[1];
            check(Native950Familiars.renew(p,npc).contains("renew")&&p.getInventory().getItem(1)==null&&Native950Familiars.snapshot(p)[1]>before,"Renew did not consume/refresh");
            int[] snapshot=Native950Familiars.snapshot(p);Native950Familiars.onLogout(p);check(World.getNPCs().isEmpty()&&Native950Familiars.snapshot(p)[1]==snapshot[1],"Logout leaked entity or discarded lifetime");
            Native950Familiars.restore(p,snapshot);snapshot[1]=1;Native950Familiars.tick(p);check(Native950Familiars.current(p)!=null&&Native950Familiars.snapshot(p)[1]>1,"Restore failed or shared callerarray");
            npc=Native950Familiars.current(p);int remaining=Native950Familiars.snapshot(p)[1],points=p.getSkills().getLevel(Skills.SUMMONING);
            p.getControlerManager().startControler(new com.rs.game.player.controllers.Controller(){public void start(){}public boolean canSummonFamiliar(){return false;}});
            Native950Familiars.tick(p);check(Native950Familiars.current(p)==null&&!World.containsNPC(npc)&&Native950Familiars.snapshot(p)[1]==remaining,"Restrictedarea did not suppress and pause familiar");
            Native950Familiars.tick(p);check(Native950Familiars.current(p)==null&&Native950Familiars.snapshot(p)[1]==remaining,"Restrictedarea restored familiar");
            p.getControlerManager().forceStop();Native950Familiars.tick(p);check(Native950Familiars.current(p)!=null&&p.getSkills().getLevel(Skills.SUMMONING)==points,"Leavingrestriction did not restore free of anotherpointcost");
            npc=Native950Familiars.current(p);WorldTile original=new WorldTile(npc);p.setLocation(new WorldTile(3224,3258,0));World.updateEntityRegion(p);p.addWalkSteps(3225,3258,1,true);
            Native950Familiars.tick(p);npc.processNative950Movement();check(!npc.matches(original),"Familiar did not follow a walking player");p.resetWalkSteps();p.resetMasks();npc.resetMasks();
            p.setHitpoints(0);Native950Familiars.tick(p);check(Native950Familiars.current(p)==npc,"Playerdeath destroyed modern familiar");p.setHitpoints(p.getMaxHitpoints());
            p.setLocation(new WorldTile(3217,3258,0));World.updateEntityRegion(p);p.resetMasks();Native950Familiars.clear(p);
            Native950Familiars.Profile bunyip=Native950Familiars.profile(12029);p.getInventory().items.set(0,new Item(12029,1));p.getSkills().restoreSummoning();check(Native950Familiars.summon(p,0,bunyip)==null,"Bunyip failed");
            p.setHitpoints(p.getMaxHitpoints()/2);int hp=p.getHitpoints();for(int i=0;i<25;i++){Native950Familiars.tick(p);NPC n=Native950Familiars.current(p);n.processNative950Movement();n.resetMasks();p.resetMasks();}
            check(p.getHitpoints()==hp+p.getMaxHitpoints()/50,"Bunyip did not heal2percent at25ticks");Native950Familiars.onLogout(p);
            Native950Familiars.restore(p,new int[]{12029,2});Native950Familiars.tick(p);check(Native950Familiars.current(p)!=null,"Shortrestore failed");Native950Familiars.tick(p);check(Native950Familiars.current(p)==null&&World.getNPCs().isEmpty(),"Expiry leaked entity");
            for(int[] bad:new int[][]{{0,1},{1,0},{12047,-1},{12047,14401},{-1,1},{65535,1},{1}}){boolean refused=false;try{Native950Familiars.validateSnapshot(bad);}catch(IllegalArgumentException expected){refused=true;}check(refused,"Malformedtuple admitted");}
            check(World.getNPCs().isEmpty()&&Native950World.getInstance().nativeNpcs().isEmpty(),"Probe leaked NPCregistry");
        }finally{Native950Familiars.clear(p);Native950Familiars.clear(other);Native950Skilling.detach(p);World.removeNative950Player(p);World.removeNative950Player(other);channel.finishAndReleaseAll();otherChannel.finishAndReleaseAll();}
    }
}
