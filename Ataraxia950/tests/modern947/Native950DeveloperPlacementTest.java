package com.rs.game.player.client;

import com.rs.game.WorldTile;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950DeveloperPlacementTest {
    private Native950DeveloperPlacement.Request request(String kind,int size,int amount){
        return new Native950DeveloperPlacement.Request(new Native950DeveloperCatalogue.Entry(new String[]{kind,"1","Test",""+size,"2","10",kind.equals("Object")?"10":""}),7,amount,10,1,false,new WorldTile(3200,3200,0),1000);
    }
    private static class World implements Native950DeveloperPlacement.Mutations {
        int checked,spawned,removed,failAt=-1,blockedAt=-1,rotation=-1;
        public boolean available(WorldTile t,int size){return ++checked!=blockedAt;}
        public Object npc(int id,WorldTile t,boolean repeat){if(++spawned==failAt)throw new IllegalStateException("injected failure");return t;}
        public Object object(int id,int type,int r,WorldTile t){rotation=r;spawned++;return t;}
        public void remove(Object actor){removed++;}
    }
    @Test public void tileClaimChecksIdentityExpiryPlaneAndDistance(){
        Native950DeveloperPlacement.Request r=request("NPC",1,1);WorldTile p=new WorldTile(3200,3200,0);
        assertTrue(r.accepts(Native950DeveloperPlacement.SOURCE,7,-1,3201,3201,p,1001));
        assertFalse(r.accepts(Native950DeveloperPlacement.SOURCE,6,-1,3201,3201,p,1001));
        assertFalse(r.accepts(Native950DeveloperPlacement.SOURCE,7,1,3201,3201,p,1001));
        assertFalse(r.accepts(123,7,-1,3201,3201,p,1001));
        assertFalse(r.accepts(Native950DeveloperPlacement.SOURCE,7,-1,3201,3201,p,61001));
        assertFalse(r.accepts(Native950DeveloperPlacement.SOURCE,7,-1,3201,3201,new WorldTile(3200,3200,1),1001));
        assertFalse(r.accepts(Native950DeveloperPlacement.SOURCE,7,-1,3299,3201,p,1001));
    }
    @Test public void formationPreflightPreventsPartialSpawnAndLargeFootprintOverflow(){
        World w=new World();w.blockedAt=3;
        try{Native950DeveloperPlacement.commit(request("NPC",3,4),3200,3200,w);fail();}catch(IllegalArgumentException expected){}
        assertEquals(0,w.spawned);
        w=new World();try{Native950DeveloperPlacement.commit(request("NPC",16,50),3200,3200,w);fail();}catch(IllegalArgumentException expected){}
        assertEquals(0,w.spawned);
        List<WorldTile> tiles=Native950DeveloperPlacement.formation(3200,3200,0,3,4);
        assertEquals(3203,tiles.get(1).getX());assertEquals(3203,tiles.get(2).getY());
    }
    @Test public void failureRollsBackOnlyItsOwnSuccessfulCreations(){
        World w=new World();w.failAt=3;
        try{Native950DeveloperPlacement.commit(request("NPC",2,4),3200,3200,w);fail();}catch(IllegalStateException expected){}
        assertEquals(4,w.checked);assertEquals(2,w.removed);
    }
    @Test public void objectRotationUsesSameMutationBoundary(){
        World w=new World();assertEquals(1,Native950DeveloperPlacement.commit(request("Object",4,1),3201,3201,w).size());assertEquals(1,w.rotation);
        try{Native950DeveloperPlacement.commit(request("Object",64,1),3201,3201,w);fail();}catch(IllegalArgumentException expected){}
        assertEquals(1,w.spawned);
    }
    @Test public void realCatalogueSearchFindsNamesIdsAndConcreteTypes(){
        List<Native950DeveloperCatalogue.Entry> npcs=Native950DeveloperCatalogue.search("NPC","nex");assertFalse(npcs.isEmpty());
        Native950DeveloperCatalogue.Entry e=npcs.get(0);assertEquals(e.id,Native950DeveloperCatalogue.search("NPC",""+e.id).get(0).id);
        assertEquals(npcs.size(),Native950DeveloperCatalogue.search("NPC","NEX").size());
        List<Native950DeveloperCatalogue.Entry> objects=Native950DeveloperCatalogue.search("Object","bank chest");assertFalse(objects.isEmpty());assertTrue(objects.get(0).types.length>0);
        assertTrue(Native950DeveloperCatalogue.search("NPC","2147483647").isEmpty());
    }
    @Test public void persistentFileExcludesTemporaryAndRejectsMalformedWithoutWriting()throws Exception{
        Path dir=Files.createTempDirectory("devworld-test"),path=dir.resolve("world.tsv");
        try{
            Native950DeveloperWorldEdits.Edit saved=new Native950DeveloperWorldEdits.Edit(UUID.randomUUID().toString(),"jaxa","Object",1,3200,3200,0,10,3,false,true);
            Native950DeveloperWorldEdits.Edit temp=new Native950DeveloperWorldEdits.Edit(UUID.randomUUID().toString(),"nooby","NPC",1,3200,3200,0,-1,0,false,false);
            Native950DeveloperWorldEdits.write(path,Arrays.asList(saved,temp));List<Native950DeveloperWorldEdits.Edit> read=Native950DeveloperWorldEdits.read(path);
            assertEquals(1,read.size());assertEquals("jaxa",read.get(0).owner);assertEquals(3,read.get(0).rotation);
            byte[] valid=Files.readAllBytes(path);Files.write(path,"wrong schema".getBytes("UTF-8"));
            try{Native950DeveloperWorldEdits.read(path);fail();}catch(java.io.IOException expected){}
            assertEquals("wrong schema",new String(Files.readAllBytes(path),"UTF-8"));
            Files.write(path,valid);List<Native950DeveloperWorldEdits.Edit> many=new ArrayList<>();for(int n=0;n<257;n++)many.add(saved);
            try{Native950DeveloperWorldEdits.write(path,many);fail();}catch(java.io.IOException expected){}
            assertArrayEquals(valid,Files.readAllBytes(path));
        }finally{Files.deleteIfExists(path);Files.deleteIfExists(dir);}
    }
}
