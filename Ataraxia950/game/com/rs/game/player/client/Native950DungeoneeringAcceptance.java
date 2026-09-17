package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Real950 asset/scene and native entity lifecycle acceptance, without a live account or client. */
public final class Native950DungeoneeringAcceptance {
    private Native950DungeoneeringAcceptance() { }
    public static void main(String[] args) throws Exception {
        require(args.length == 1,"Provide the selected950 flat cache path");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        NPCCombatDefinitionsDataParser.init(); NPCStatsDataParser.init();
        Native950Dungeoneering.verifyCacheBindings();
        verifySceneAreas();
        Native950World.getInstance().execute(() -> { run(); return null; }).get(60,TimeUnit.SECONDS);
        System.out.println("PASS: actual950 ring/entrance/exit pins, all four room footprints, registered native guardians, solo ownership, three credited objectives, exactly-once completion, repeat entry, abort/death/logout cleanup.");
        System.out.println("LIMIT: death notifications are injected after native HP/death-state commit; this does not prove live client controls, rendering, or combat RNG.");
    }
    /** Verify source membership, not merely collision: all sixteen room chunks require area115. */
    static void verifySceneAreas() {
        Native950MapAreas.verify();
        for (int y : Native950Dungeoneering.ROOM_Y) for (int dx : new int[]{0,8}) for (int dy : new int[]{0,8}) {
            int x = 112 + dx, chunkY = y + dy;
            require(Native950MapAreas.resolvedAreaType(x, chunkY) == 115,
                    "Frozen room chunk lacks actual950 area115 membership: " + x + "," + chunkY);
            require(Native950MapAreas.areaTypeFor(x, chunkY, 0) == 115,
                    "Scene rebuild would use the mainland fallback for a frozen room");
        }
        System.out.println("PASS: all16 frozen room chunks resolve area115; index23/3 membership and config2/83 area identities match the selected950 cache.");
    }
    private static void run() {
        require(World.getNPCs().isEmpty() && World.getPlayers().isEmpty(),"Disposable empty world required");
        for (int i = 0; i < 4; i++) Native950Dungeoneering.verifyRoom(i,2);
        for (WorldTile tile : new WorldTile[]{Native950Dungeoneering.outside(),Native950Dungeoneering.ringDestination()}) {
            World.getRegion(tile.getRegionId(),true);
            require(World.isFloorFree(tile.getPlane(),tile.getX(),tile.getY(),1),"Outside arrival is blocked");
        }
        EmbeddedChannel channel = new EmbeddedChannel(), otherChannel = new EmbeddedChannel();
        Player p = Player.createNative950("dungprobe",Native950Dungeoneering.outside(),channel);
        Player other = Player.createNative950("dungother",Native950Dungeoneering.outside(),otherChannel);
        Native950MeleeCombat combat = new Native950MeleeCombat(Thread.currentThread());
        try {
            p.setActive(true); other.setActive(true);
            World.addNative950Player(p,1); World.updateEntityRegion(p); World.addNative950Player(other,2); World.updateEntityRegion(other);
            combat.attach(p); combat.attach(other);
            double before = p.getSkills().getXp(Skills.DUNGEONEERING);
            String first = Native950Dungeoneering.start(p);
            require(Native950Dungeoneering.interrupted(p),"Start did not admit a room: "+first);
            p.setLocation(Native950Dungeoneering.entry(0)); p.resetWalkSteps();
            List<NPC> guardians = new ArrayList<>();
            for (NPC n : World.getNPCs()) if (n.getId() == Native950Dungeoneering.GUARDIAN) guardians.add(n);
            require(guardians.size()==3,"Expected three native guardians");
            for (NPC n : guardians) {
                require(combat.supports(n),"Native combat did not register guardian");
                require(Native950Dungeoneering.attackRefusal(other,n)!=null,"Other player can claim solo guardian");
                require(Native950Dungeoneering.attackRefusal(p,n)==null,"Owner cannot fight guardian");
                Native950Dungeoneering.onNpcDeath(p,n);
                require(Native950Dungeoneering.state(p).claim()==null,"Living guardian awarded completion");
                n.setHitpoints(0); n.setNative950DeathVisible(true);
                Native950Dungeoneering.onNpcDeath(other,n); Native950Dungeoneering.onNpcDeath(p,n); Native950Dungeoneering.onNpcDeath(p,n);
            }
            p.setLocation(113,4994,0);
            WorldObject exit=World.getRegion(p.getRegionId()).getObjectWithType(0,112&63,4994&63,10);
            require(Native950Dungeoneering.object(p,exit,1),"Actual dungeon exit was not routed");
            require(Native950Dungeoneering.completed(p)==1 && Native950Dungeoneering.tokens(p)==15,"Completion reward missing");
            require(p.getSkills().getXp(Skills.DUNGEONEERING)>before,"Completion awarded no XP");
            double after=p.getSkills().getXp(Skills.DUNGEONEERING);
            Native950Dungeoneering.object(p,exit,1);
            require(Native950Dungeoneering.tokens(p)==15 && p.getSkills().getXp(Skills.DUNGEONEERING)==after,"Exit replay duplicated reward");
            require(World.getNPCs().isEmpty(),"Completion leaked guardians");
            p.setLocation(Native950Dungeoneering.outside());
            Native950Dungeoneering.start(p); require(World.getNPCs().size()==3,"Repeat run failed");
            Native950Dungeoneering.onPlayerDeath(p); require(World.getNPCs().isEmpty(),"Death leaked guardians");
            require(Native950Dungeoneering.completed(p)==1 && Native950Dungeoneering.tokens(p)==15,"Abort awarded completion");
            Native950Dungeoneering.onLogout(p); require(!Native950Dungeoneering.interrupted(p),"Logout retained room claim");
            require(Native950Dungeoneering.outside().matches(p),"Logout saved an abandoned chamber position");
        } finally {
            Native950Dungeoneering.onLogout(p); Native950Dungeoneering.onLogout(other);
            combat.detach(p); combat.detach(other); combat.clear();
            World.removeNative950Player(p); World.removeNative950Player(other);
            channel.finishAndReleaseAll(); otherChannel.finishAndReleaseAll();
        }
    }
    private static void require(boolean condition,String message) { if (!condition) throw new AssertionError(message); }
}
