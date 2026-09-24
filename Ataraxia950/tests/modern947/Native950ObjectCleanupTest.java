package com.rs.game.player.client;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Native950ObjectCleanupTest {
    private final EmbeddedChannel channel=new EmbeddedChannel();
    private Player a,b;private Path dir,path;
    @Before public void setup()throws Exception{
        a=Player.createNative950("clear-owner",new WorldTile(3200,3200,0),channel);
        b=Player.createNative950("clear-other",new WorldTile(3200,3200,0),channel);
        dir=Files.createTempDirectory("object-clear-");path=dir.resolve("edits.tsv");
    }
    private Native950DeveloperWorldEdits.Edit add(Player p,int x,int plane,boolean saved){
        Native950DeveloperWorldEdits.Edit e=Native950DeveloperWorldEdits.recordObject(p,new WorldObject(1,10,0,x,3200,plane));e.saved=saved;return e;
    }
    @After public void cleanup()throws Exception{
        for(Player p:Arrays.asList(a,b))for(int plane=0;plane<4;plane++)Native950DeveloperWorldEdits.clearObjects(p.getUsername(),new WorldTile(3200,3200,plane),128,path,e->e.actor=null);
        channel.finishAndReleaseAll();try(java.util.stream.Stream<Path> paths=Files.walk(dir)){for(Path p:(Iterable<Path>)paths.sorted(Comparator.reverseOrder())::iterator)Files.delete(p);}
    }
    @Test public void radiusPlaneOwnerAndSavedRecordsAreIndependent()throws Exception{
        Native950DeveloperWorldEdits.Edit near=add(a,3200,0,false),edge=add(a,3216,0,true),far=add(a,3217,0,true),up=add(a,3200,1,false),other=add(b,3200,0,true);
        List<Native950DeveloperWorldEdits.Edit> removed=new ArrayList<>();
        assertEquals(2,Native950DeveloperWorldEdits.clearObjects(a.getUsername(),a,16,path,e->{removed.add(e);e.actor=null;}));
        assertEquals(Arrays.asList(near,edge),removed);assertEquals(Arrays.asList(far,up),Native950DeveloperWorldEdits.owned(a.getUsername()));
        assertEquals(Arrays.asList(other),Native950DeveloperWorldEdits.owned(b.getUsername()));
        List<Native950DeveloperWorldEdits.Edit> disk=Native950DeveloperWorldEdits.read(path);assertEquals(2,disk.size());
        assertTrue(disk.stream().anyMatch(e->e.key.equals(far.key)));assertTrue(disk.stream().anyMatch(e->e.key.equals(other.key)));
        assertEquals(0,Native950DeveloperWorldEdits.clearObjects(a.getUsername(),a,0,path,e->{throw new AssertionError();}));
    }
    @Test public void failedSavedWriteDoesNotRemoveTemporaryOrPersistentActors()throws Exception{
        Native950DeveloperWorldEdits.Edit temp=add(a,3200,0,false),saved=add(a,3201,0,true);
        Path blocked=dir.resolve("not-a-file");Files.createDirectory(blocked);Files.write(blocked.resolve("keep"),new byte[]{1});
        try{Native950DeveloperWorldEdits.clearObjects(a.getUsername(),a,16,blocked,e->{throw new AssertionError("World changed before persistence");});fail();}catch(java.io.IOException expected){}
        assertEquals(Arrays.asList(temp,saved),Native950DeveloperWorldEdits.owned(a.getUsername()));assertNotNull(temp.actor);assertNotNull(saved.actor);
    }
    @Test public void invalidRadiusAndEmptySetNeverWrite()throws Exception{
        add(a,3200,0,false);
        for(int radius:new int[]{-1,129,Integer.MAX_VALUE})try{Native950DeveloperWorldEdits.clearObjects(a.getUsername(),a,radius,path,e->{throw new AssertionError();});fail();}catch(IllegalArgumentException expected){}
        assertFalse(Files.exists(path));assertEquals(1,Native950DeveloperWorldEdits.owned(a.getUsername()).size());
        assertTrue(Native950DevelopmentCommands.isCommand(";;clearobjs 0"));
        Native950DeveloperActions.Action action=Native950DeveloperActions.find("clearobjects");assertTrue(action.confirmation);assertEquals(";;clearobjects 16",action.command(action.defaults()));
    }
}
