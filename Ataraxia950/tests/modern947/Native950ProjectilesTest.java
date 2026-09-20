package com.rs.game.player.client;

import com.rs.game.Projectile;
import com.rs.game.WorldTile;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950ProjectilesTest {
    @Test public void exact950ParserFieldOrderAndEntityKinds() {
        Packet p=Native950Packets.projectileHalfSquare(0x35,6,-4,0x2000a,0x10064,1234,41,16,55,85,255,90);
        assertEquals(154,p.type().opcode());assertEquals(21,p.payload().length);
        ByteBuffer b=ByteBuffer.wrap(p.payload());
        assertEquals(0x35,b.get()&255);assertEquals(0,b.get());assertEquals(6,b.get());assertEquals(-4,b.get());
        assertEquals(2,b.get());assertEquals(10,b.getShort()&65535);
        assertEquals(1,b.get());assertEquals(100,b.getShort()&65535);
        assertEquals(1234,b.getShort()&65535);assertEquals(41,b.get());assertEquals(16,b.get());
        assertEquals(55,b.getShort()&65535);assertEquals(85,b.getShort()&65535);
        assertEquals(255,b.get()&255);assertEquals(90,b.getShort()&65535);assertFalse(b.hasRemaining());
    }

    @Test public void oneFrozenEventReachesBothViewersAndIsClearedAfterAllViews() {
        Native950Projectiles batch=new Native950Projectiles(Thread.currentThread());
        WorldTile from=new WorldTile(3217,3258,0),to=new WorldTile(3220,3256,0);
        batch.add(new Projectile(from,to,false,false,0,1234,41,16,55,85,90,0));
        List<Packet> a=new ArrayList<>(),b=new ArrayList<>();
        batch.publish(3200,3200,0,256,a::add);batch.publish(3208,3208,0,256,b::add);
        assertEquals(2,a.size());assertEquals(2,b.size());assertEquals(1,batch.size());
        assertEquals(96,a.get(0).type().opcode());
        assertArrayEquals(a.get(1).payload(),b.get(1).payload());
        assertEquals(0x35,a.get(1).payload()[0]&255);
        List<Packet> hidden=new ArrayList<>();
        batch.publish(3200,3200,1,256,hidden::add);batch.publish(0,0,0,256,hidden::add);
        assertTrue(hidden.isEmpty());
        batch.clear();a.clear();batch.publish(3200,3200,0,256,a::add);assertTrue(a.isEmpty());
    }

    @Test public void foreignThreadCannotReadOrMutateBatch() throws Exception {
        Native950Projectiles batch=new Native950Projectiles(Thread.currentThread());
        AtomicReference<Throwable> error=new AtomicReference<>();
        Thread t=new Thread(()->{try{batch.clear();}catch(Throwable failure){error.set(failure);}});
        t.start();t.join();assertTrue(error.get() instanceof IllegalStateException);
    }

    @Test public void invalidReferencesTimingAndSignedDeltasAreRefused() {
        for(int[] invalid:new int[][]{{128,0,1,2},{0,0x30001,1,2},{0,0,3,2}}) {
            try {
                Native950Packets.projectileHalfSquare(0,invalid[0],0,invalid[1],0,1,0,0,invalid[2],invalid[3],0,0);
                fail("Invalid native projectile accepted");
            } catch(IllegalArgumentException expected) { }
        }
    }
}
