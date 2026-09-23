package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Real-cache, real high-tier equipment, ephemeral accounts only. No live world or save access. */
public final class Native950CombatPassAcceptance {
    static int checks,activations,projectiles;
    static final List<Map<String,Object>> evidence=new ArrayList<>();
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950AbilityAssets.verify();
        com.rs.game.player.client.ui.Native950Bindings bindings=com.rs.game.player.client.ui.Native950Bindings.tryLoad();
        check(bindings!=null,"Exact-cache bindings failed to load");
        Native950IdMap.install(new Native950ActionRouter.IdMapAdapter(bindings.allowListResolver()));
        Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
        Map<String,Object> report=new LinkedHashMap<>();report.put("checks",checks);report.put("activations",activations);
        report.put("projectiles",projectiles);report.put("liveVisual",false);report.put("accountsAccessed",false);report.put("abilities",evidence);
        report.put("prolongedCycles",64);report.put("stylesExercised",Arrays.asList("melee","ranged","magic","necromancy"));
        report.put("limitations","Deterministic scheduler rolls and clear collision seam. Native settings packets are decoded but no client executes their scripts. No physical key or rendered acceptance.");
        Files.write(Paths.get(args[1]),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report).getBytes("UTF-8"));
        System.out.println("COMBAT PASS: "+checks+" checks; "+activations+" activations; "+projectiles+" projectiles; no production writes.");
    }
    static void equip(Player player,int style,boolean shield,boolean twoHanded){
        Native950DeveloperLoadouts.Loadout loadout=Native950DeveloperLoadouts.builtins().get(shield?7:style);
        player.getEquipment().getItems().clear();player.getInventory().items.clear();
        for(int i=0;i<loadout.equipment.length;i++)if(loadout.equipment[i]!=null)player.getEquipment().getItems().set(i,new Item(loadout.equipment[i]));
        for(int i=0;i<loadout.inventory.length;i++)if(loadout.inventory[i]!=null)player.getInventory().items.set(i,new Item(loadout.inventory[i]));
        if(twoHanded){player.getEquipment().getItems().set(3,new Item(16909));player.getEquipment().getItems().set(5,null);}
    }
    static Player player(EmbeddedChannel channel,int index){
        Player player=Player.createNative950("combat-offline-"+index,new WorldTile(3217,3258,0),channel);
        Native950World.installVarpSink(player);
        player.setIndex(index);player.setActive(true);
        for(int skill=0;skill<Skills.SKILL_NAME.length;skill++){
            player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,120));
            player.getSkills().setLevelWithoutRefresh(skill,120);
        }
        player.setHitpoints(player.getMaxHitpoints());
        Native950Skilling.attach(player,new Native950Containers(player,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops()));
        return player;
    }
    static final class Fixture implements AutoCloseable {
        final EmbeddedChannel firstChannel=new EmbeddedChannel(new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread())),
                secondChannel=new EmbeddedChannel(new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread()));
        final Player first=player(firstChannel,1),second=player(secondChannel,2);
        final NPC npc=NPC.createNative950(12353,new WorldTile(3218,3258,0),1);
        final Native950MeleeCombat combat;
        final Set<NPC> companions=Collections.newSetFromMap(new IdentityHashMap<NPC,Boolean>());int nextCompanion=2;
        Fixture(){
            npc.setIndex(1);
            combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
                public void activate(NPC n){}public boolean player(Player p){return p==first||p==second;}
                public boolean npc(NPC n){return n==npc;}public boolean clear(WorldTile t){return true;}
                public boolean reach(Entity a,Entity b){return true;}public boolean approach(Player p,NPC n){return true;}
                public void projectile(Projectile p){projectiles++;}
                public NPC spawnConjure(Player p,int id){NPC actor=NPC.createNative950Conjure(id,new WorldTile(p));actor.setIndex(nextCompanion++);companions.add(actor);return actor;}
                public void removeConjure(NPC n){companions.remove(n);}
            },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long b){return true;}public int damage(int maximum){return maximum/2;}},Native950CombatStyles::loadout);
            combat.attach(first);combat.attach(second);
            combat.register(npc,new Native950NpcCombatProfile(12353,1,1,1000000,1,1,0,100,1,10,-1,-1,-1,0,0));
            npc.setHitpoints(1000000);
        }
        void step(int count){for(int i=0;i<count;i++){first.resetMasks();second.resetMasks();npc.resetMasks();combat.beforeMovement();combat.afterMovement();}}
        void cast(Player player,int id){String result=combat.ability(player,id);check(result==null,"ability "+id+" refused: "+result);activations++;
            Native950PlayerEffects.Result fx=Native950PlayerEffects.append(player,com.rs.network.protocol.modern950.Native950PlayerMasks.builder());
            check(fx.refusedCount()==0,"ability "+id+" caster graphics were silently refused by the native mask gate");
            int[] emitted={npc.getNextGraphics1()==null?-1:npc.getNextGraphics1().getId(),npc.getNextGraphics2()==null?-1:npc.getNextGraphics2().getId()};
            for(int graphic:emitted)check(Native950PlayerEffects.isVerifiedGraphic(graphic),"ability "+id+" impact graphic rejected: "+graphic);
        }
        public void close(){combat.clear();check(companions.isEmpty(),"Conjure actor leaked after combat teardown");Native950Skilling.detach(first);Native950Skilling.detach(second);firstChannel.finishAndReleaseAll();secondChannel.finishAndReleaseAll();}
    }
    static java.util.List<byte[]> drain(EmbeddedChannel channel){
        channel.flushOutbound();java.util.List<byte[]> result=new java.util.ArrayList<>();
        Object value;while((value=channel.readOutbound())!=null){
            check(value instanceof io.netty.buffer.ByteBuf,"Expected framed native output");
            io.netty.buffer.ByteBuf frame=(io.netty.buffer.ByteBuf)value;
            try{byte[] bytes=new byte[frame.readableBytes()];frame.readBytes(bytes);result.add(bytes);}finally{frame.release();}
        }
        return result;
    }
    static void wire(java.util.List<byte[]> frames,int id,int value){
        byte[] expected=(value>=-128&&value<=127?com.rs.network.protocol.modern950.Native950Packets.varpSmall(id,value):
            com.rs.network.protocol.modern950.Native950Packets.varp(id,value)).frame(()->0);
        check(frames.stream().anyMatch(f->java.util.Arrays.equals(f,expected)),"Generated combat varp did not reach transport: "+id+"="+value);
    }
    static void worldCompanionFrames(){
        Native950World world=Native950World.getInstance();
        java.util.List<NPC> actors=new java.util.ArrayList<>();
        EmbeddedChannel a=new EmbeddedChannel(new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread())),
                b=new EmbeddedChannel(new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread()));
        Native950NpcViewport firstView=new Native950NpcViewport(),secondView=new Native950NpcViewport();
        Player first=Player.createNative950("companion-viewer-a",new WorldTile(3217,3258,0),a),
                second=Player.createNative950("companion-viewer-b",new WorldTile(3217,3259,0),b);
        try{
            for(Native950Conjures.Kind k:Native950Conjures.Kind.values()){
                NPC actor=NPC.createNative950Conjure(k.npc,new WorldTile(first));world.addConjure(actor);actors.add(actor);
                check(World.getNPCs().get(actor.getIndex())==actor,"Conjure missing world registry");
                check(World.getRegion(actor.getRegionId()).getNPCsIndexes().contains(actor.getIndex()),"Conjure missing region registry");
                check(actor.getNative950CombatProfile()==null,"Conjure entered hostile NPC AI");
            }
            a.write(firstView.frame(first,7,false,actors));b.write(secondView.frame(second,7,false,actors));
            check(firstView.snapshot().indices.length==4&&secondView.snapshot().indices.length==4,"Conjure omitted from a native player viewport");
            check(!drain(a).isEmpty()&&!drain(b).isEmpty(),"Conjure native frames dropped");
            for(NPC actor:actors)world.removeConjure(actor);
            a.write(firstView.frame(first,7,false,Collections.emptyList()));b.write(secondView.frame(second,7,false,Collections.emptyList()));
            check(firstView.snapshot().indices.length==0&&secondView.snapshot().indices.length==0,"Conjure dismissal retained a viewer actor");
            check(world.nativeNpcs().isEmpty()&&World.getNPCs().isEmpty(),"Conjure dismissal leaked a registry");drain(a);drain(b);
        }finally{for(NPC actor:actors)if(!actor.hasFinished())world.removeConjure(actor);firstView.close();secondView.close();a.finishAndReleaseAll();b.finishAndReleaseAll();}
    }
    static boolean containsBytes(byte[] data,byte[] needle){outer:for(int i=0;i<=data.length-needle.length;i++){for(int j=0;j<needle.length;j++)if(data[i+j]!=needle[j])continue outer;return true;}return false;}
    static void soulWorldFrames(){
        try(Fixture f=new Fixture()){
            equip(f.first,3,false,false);f.first.setRunning(true);f.second.setRunning(true);
            f.first.getEquipment().getItems().set(5,new Item(55482));check(Native950NecromancyResources.soulCap(f.first)==5,"Soulbound lantern cap");
            Native950NecromancyResources resources=new Native950NecromancyResources();Native950EntityFrames frames=new Native950EntityFrames();
            List<Player> roster=Arrays.asList(f.first,f.second);
            for(Player p:roster)for(com.rs.network.protocol.modern950.Native950Packets.Packet packet:frames.admit(p,new Native950World.SceneConfig(p.getX(),p.getY(),0,p.getIndex(),7,0,0,0),roster))p.getRealChannel().write(packet);
            drain(f.firstChannel);drain(f.secondChannel);f.first.resetMasks();f.second.resetMasks();
            for(int count=1;count<=5;count++){
                resources.gainSoul(f.first,count);soulFrameCheck(f,frames,roster,count);
            }
            resources.cast(f.first,48301,6,false);soulFrameCheck(f,frames,roster,0);
            check(f.second.getNative950SoulVisual()==-1,"Another owner gained soul visuals");
        }
    }
    static void soulFrameCheck(Fixture f,Native950EntityFrames frames,List<Player> roster,int count){
        frames.beginFrames(roster);
        byte[] expected=com.rs.network.protocol.modern950.Native950PlayerMasks.encodeWithSkippedPrefix(com.rs.network.protocol.modern950.Native950PlayerMasks.builder().spotanims(
            com.rs.network.protocol.modern950.Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(com.rs.network.protocol.modern950.Native950PlayerMasks.Spotanim.of(4,count==0?-1:7865+count,0,0,0,0,0)))).build());
        for(Player p:roster){
            // Retain resource packets while checking the complete framed PLAYER_INFO path.
            EmbeddedChannel output=(EmbeddedChannel)p.getRealChannel();List<byte[]> before=drain(output);
            frames.encode(new Native950Frames.Frame(p,output,roster,Collections.emptyList(),null,false,7,p.getX(),p.getY(),p.getPlane()));
            List<byte[]> encoded=drain(output);check(encoded.stream().anyMatch(b->(b[0]&255)==com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.PLAYER_INFO.opcode()&&containsBytes(b,expected)),"Soul count "+count+" omitted from viewer "+p.getIndex());
            // Resource publication is independent and only sent to its owner.
            if(p==f.first)wire(before,11035,count);else check(before.isEmpty(),"Soul vars leaked to other player");
        }
    }
    static void run(){
        worldCompanionFrames();soulWorldFrames();
        try(Fixture f=new Fixture()){
            for(int id:new int[]{55526,61355}){
                f.first.getEquipment().getItems().set(5,new Item(id));
                check(Native950NecromancyEquipment.conduit(f.first),"Real950 conduit refused: "+id);
                f.first.getVarsManager().sendVar(11218,0);drain(f.firstChannel);
                Native950NecromancyEquipment.publish(f.first);wire(drain(f.firstChannel),11218,1);
            }
            for(int id:new int[]{-1,61333,13740}){
                f.first.getEquipment().getItems().set(5,id<0?null:new Item(id));
                f.first.getVarsManager().sendVar(11218,1);drain(f.firstChannel);
                Native950NecromancyEquipment.publish(f.first);wire(drain(f.firstChannel),11218,0);
            }
            check(f.second.getVarsManager().getValue(11218)==0,"Conduit availability leaked to another owner");
        }
        try(Fixture f=new Fixture()){
            equip(f.first,3,false,false);f.first.getCombatDefinitions().setSpecialAttackPercentage(100);
            java.util.Map<String,Integer> queueBar=new java.util.HashMap<>();
            queueBar.put("actionBar.0.0",(7<<13|2)|((7<<13|3)<<16));
            f.first.getNative950ActionBar().restore(queueBar);
            f.first.getNative950ActionBar().setActiveBar(f.first,f.firstChannel,0);
            check(f.combat.attack(f.first,f.npc)==null,"Ordinary queue target refused");
            f.cast(f.first,48296);check(f.first.getVarsManager().getValue(10986)==4,"Touch failed to generate Necrosis");
            drain(f.firstChannel);String queuedReply=f.combat.ability(f.first,48297);
            check(queuedReply!=null&&queuedReply.contains("queued"),"Finger did not enter ordinary manual queue: "+queuedReply);
            check(f.first.getVarsManager().getValue(10986)==4,"Queued Finger spent Necrosis before execution");
            int queued=f.first.getVarsManager().getValue(4164);check(queued>0,"Ordinary Finger queue has no native slot marker");
            java.util.List<byte[]> queuedFrames=drain(f.firstChannel);wire(queuedFrames,4164,queued);wire(queuedFrames,5861,1003);
            f.step(2);check(f.first.getVarsManager().getValue(10986)==4,"Finger executed before legitimate GCD ended");
            int hp=f.npc.getHitpoints();f.step(2);
            check(f.first.getVarsManager().getValue(10986)==0,"Queued Finger did not consume Necrosis on execution");
            check(f.npc.getHitpoints()<hp,"Queued Finger produced no damage");
            java.util.List<byte[]> executedFrames=drain(f.firstChannel);wire(executedFrames,4164,0);wire(executedFrames,10986,0);
            int cycle=(int)com.rs.utils.Utils.currentWorldCycle();
            byte[] feedback=com.rs.network.protocol.modern950.Native950Packets.runClientScript(6570,48297,cycle,cycle+Native950AbilityCatalog.get(48297).cooldown,1,1).frame(()->0);
            check(executedFrames.stream().anyMatch(frame->Arrays.equals(frame,feedback)),"Queued execution omitted native activation/cooldown script");
            check(f.second.getVarsManager().getValue(10986)==0,"Queued Finger resource leak");
        }
        try(Fixture f=new Fixture()){
            equip(f.first,3,false,false);f.first.getCombatDefinitions().setSpecialAttackPercentage(100);
            check(f.combat.attack(f.first,f.npc)==null,"Resource wire target admission");drain(f.firstChannel);drain(f.secondChannel);
            f.cast(f.first,48296);wire(drain(f.firstChannel),10986,4);f.step(3);
            f.cast(f.first,48297);wire(drain(f.firstChannel),10986,0);f.step(3);
            f.cast(f.first,48298);wire(drain(f.firstChannel),11035,1);f.step(3);
            f.cast(f.first,48299);wire(drain(f.firstChannel),11035,0);
            check(f.second.getVarsManager().getValue(10986)==0&&f.second.getVarsManager().getValue(11035)==0,"Resource wire ownership leaked");
        }
        try(Fixture f=new Fixture()){
            equip(f.first,0,false,false);f.first.getNative950ActionBar().testBar(f.first,f.firstChannel);
            check(f.combat.attack(f.first,f.npc)==null,"Queue visual target admission");
            Native950BugTest.toggle(f.first);
            try{
                drain(f.firstChannel);String held=f.combat.holdQueueForVisualCheck(f.first,1);
                check(held!=null&&held.contains("7.2"),"Queue diagnostic refused: "+held);
                java.util.List<byte[]> frames=drain(f.firstChannel);wire(frames,5861,1003);wire(frames,4164,1);
                f.step(6);check(f.first.getVarsManager().getValue(4164)==1,"Queue hold cleared before visual observation window");
                f.first.getNative950ActionBar().refreshTransforms(f.first);wire(drain(f.firstChannel),4164,1);
                f.step(7);wire(drain(f.firstChannel),4164,0);check(f.first.getVarsManager().getValue(5861)==0,"Execution retained queued overlay");
                f.step(30);f.combat.holdQueueForVisualCheck(f.first,1);drain(f.firstChannel);
                f.combat.cancelAttack(f.first);wire(drain(f.firstChannel),4164,0);
            }finally{Native950BugTest.close(f.first,"offline-acceptance");}
        }

        try(Fixture fixture=new Fixture()){
            equip(fixture.first,3,false,false);
            int autoAnimation=Native950CombatStyles.loadout(fixture.first).attackAnimation;
            System.out.println("Necromancy auto evidence: animation="+autoAnimation+" projectile="+Native950AbilityCatalog.sequenceParam(autoAnimation,2940)
                    +" caster="+Native950AbilityCatalog.sequenceParam(autoAnimation,2920)+" impact="+Native950AbilityCatalog.sequenceParam(autoAnimation,2933));
            check(fixture.combat.attack(fixture.first,fixture.npc)==null,"Necromancy auto target admission");
            int before=fixture.npc.getHitpoints();fixture.step(1);
            check(fixture.npc.getHitpoints()<before,"Native Necromancy automatic attack failed to impact");
            check(fixture.first.getNextGraphics1()!=null&&fixture.first.getNextGraphics1().getId()==7853,"Native Necromancy caster effect missing");
            check(fixture.npc.getNextGraphics1()!=null&&fixture.npc.getNextGraphics1().getId()==7854,"Native Necromancy impact effect missing");
            fixture.step(7);
            Map<String,Object> row=new LinkedHashMap<>();row.put("struct",48293);row.put("name","Basic Attack");
            row.put("weapon",fixture.first.getEquipment().getWeaponId());row.put("style",3);
            row.put("automated","real-cache automatic Necromancy attack, sequence35449 caster7853/impact7854; explicit book-click route remains pending");
            row.put("projectileEvidence","The exact bound sequence has no param2940; no projectile identity is invented.");
            row.put("executionMode","automatic");row.put("liveVisual","pending");evidence.add(row);
        }
        try(Fixture fixture=new Fixture()){
            Player player=fixture.first;
            player.getInventory().items.clear();
            player.getInventory().items.set(0,new Item(42251));player.getInventory().items.set(1,new Item(42251));
            player.setHitpoints(100);
            fixture.cast(player,44225);
            check(player.getHitpoints()==340,"Sailfish must heal 240 engine HP / 2400 native life points");
            check(player.getInventory().getItem(0)==null&&player.getInventory().getItem(1)!=null,"Sailfish consumption must be slot precise");
            check(fixture.combat.ability(player,44225)!=null&&player.getInventory().getItem(1)!=null,"Food delay must prevent a second consumption");
        }
        for(Native950AbilityCatalog.Definition definition:Native950AbilityCatalog.DEFINITIONS){
            if(definition.effect==Native950AbilityCatalog.Effect.MOVEMENT)continue;
            try(Fixture fixture=new Fixture()){
                Player player=fixture.first;int style=Math.max(0,definition.style());
                equip(player,style,definition.shieldRequired(),definition.twoHandedRequired);
                Native950MeleeCombat.Loadout gear=Native950CombatStyles.loadout(player);
                check(!player.isDevelopmentGodMode()&&!player.isInfiniteAmmunition(),"Harness accidentally enabled almighty");
                player.getCombatDefinitions().setSpecialAttackPercentage(100);
                check(fixture.combat.attack(player,fixture.npc)==null,"Real gear attack rejected: "+definition.name);
                if(definition.struct==48299||definition.struct==48301){
                    fixture.cast(player,48298);fixture.step(9);fixture.cast(player,48298);fixture.step(9);
                }
                if(Native950Conjures.handles(definition.struct)){
                    player.getInventory().items.set(27,new Item(55336,20));
                    if(Native950Conjures.base(definition.struct)!=definition.struct){fixture.cast(player,Native950Conjures.base(definition.struct));fixture.step(3);}
                }
                if(definition.struct==48312||definition.struct==48313){
                    fixture.cast(player,48311);fixture.step(3);
                    if(definition.struct==48313){fixture.cast(player,48312);fixture.step(3);}
                }
                int before=player.getCombatDefinitions().getSpecialAttackPercentage();
                fixture.cast(player,definition.struct);
                check(fixture.firstChannel.isOpen(),definition.name+" closed session");
                check(fixture.combat.pendingHitCount(player)<64,definition.name+" unbounded pending hits");
                fixture.step(Math.max(15,definition.channelTicks()+10));
                check(fixture.firstChannel.isOpen(),definition.name+" delayed execution closed session");
                Map<String,Object> row=new LinkedHashMap<>();row.put("struct",definition.struct);row.put("name",definition.name);
                row.put("weapon",player.getEquipment().getWeaponId());row.put("style",gear.profile.style);
                row.put("animation",Native950AbilityCatalog.animation(player,definition.struct));
                row.put("automated","real-cache admission/execution/channel-completion");row.put("liveVisual","pending");evidence.add(row);
            }
        }
        // Repeated same-target, different-style ownership and resource cycles.
        try(Fixture fixture=new Fixture()){
            fixture.first.getCombatDefinitions().setSpecialAttackPercentage(0);fixture.second.getCombatDefinitions().setSpecialAttackPercentage(0);
            equip(fixture.first,0,false,false);equip(fixture.second,2,false,false);
            check(fixture.combat.attack(fixture.first,fixture.npc)==null,"Primary attack");
            check(fixture.combat.attack(fixture.second,fixture.npc)==null,"Secondary attack");
            int[] basics={14679,14663,14730,48298};
            for(int cycle=0;cycle<64;cycle++){
                int firstStyle=cycle/16,secondStyle=(firstStyle+2)%4;
                if(cycle%16==0){
                    equip(fixture.first,firstStyle,false,false);equip(fixture.second,secondStyle,false,false);
                    fixture.first.getNative950ActionBar().setActiveBar(fixture.first,fixture.firstChannel,firstStyle);
                    fixture.second.getNative950ActionBar().setActiveBar(fixture.second,fixture.secondChannel,secondStyle);
                    Native950Settings settings=new Native950Settings(fixture.first,fixture.firstChannel);
                    check(settings.handle(button(1430,256,-1)),"Native combat settings failed to open");
                    check(settings.handle(button(365,19,10246)),"Native Revolution slider source rejected");
                    check(settings.handle(button(365,20,13)),"Native Revolution range value rejected");
                    check(fixture.first.getNative950ActionBar().revolutionSlots()==14,"Native range not authoritative");
                    check(fixture.second.getNative950ActionBar().revolutionSlots()==9,"Native range leaked to second player");
                    settings.close();
                    check(!settings.handle(button(365,20,0)),"Closed settings retained range authority");
                }
                int otherSouls=fixture.second.getVarsManager().getValue(11035);
                fixture.cast(fixture.first,basics[firstStyle]);
                check(fixture.second.getVarsManager().getValue(11035)==otherSouls,"First player's cast altered second player's souls");
                fixture.cast(fixture.second,basics[secondStyle]);fixture.step(15);
                check(fixture.combat.combatTarget(fixture.first)==fixture.npc&&fixture.combat.combatTarget(fixture.second)==fixture.npc,"Target ownership leaked");
                if(cycle%10==0){fixture.combat.cancelAttack(fixture.first);check(fixture.combat.combatTarget(fixture.second)==fixture.npc,"Primary stop broke secondary");fixture.combat.attack(fixture.first,fixture.npc);}
            }
            fixture.combat.detach(fixture.first);check(fixture.combat.combatTarget(fixture.second)==fixture.npc,"Disconnect broke remaining combat");
            fixture.combat.unregister(fixture.npc);check(fixture.combat.combatTarget(fixture.second)==null,"Removed target retained");
        }
    }
    static com.rs.network.protocol.modern950.Native950Actions.InterfaceAction button(int face,int component,int slot){
        int hash=(face<<16)|component;
        return (com.rs.network.protocol.modern950.Native950Actions.InterfaceAction)com.rs.network.protocol.modern950.Native950Actions.decode(18,
                new byte[]{-1,-1,-1,(byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot});
    }
}
