package com.rs.game.player.client;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.net.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/** Exercises Windows ABI/peer ownership using this test JVM, never a game client. */
public class Native950WorkspacePipeTest {
    interface Client extends StdCallLibrary {
        Pointer CreateFileW(WString path,int access,int share,Pointer security,int creation,int flags,Pointer template);
        boolean WriteFile(Pointer f,byte[] b,int n,IntByReference written,Pointer overlapped);
        boolean CloseHandle(Pointer handle);
        int GetCurrentProcessId();
    }
    @Test public void actualWindowsPipePidAndTcpOwnership() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        Client kernel=Native.load("kernel32",Client.class);
        String name=Native950WorkspacePipe.NAME+"-test-"+kernel.GetCurrentProcessId();
        try(Native950WorkspacePipe pipe=new Native950WorkspacePipe(name);ServerSocket listen=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"));
                Socket client=new Socket("127.0.0.1",listen.getLocalPort());Socket server=listen.accept()) {
            Pointer peer=kernel.CreateFileW(new WString(name),0xc0000000,0,null,3,0,null);
            assertNotNull(peer);assertNotEquals(-1L,Pointer.nativeValue(peer));
            try {
                assertTrue(pipe.connect());assertEquals(kernel.GetCurrentProcessId(),pipe.peerPid());
                assertTrue(pipe.ownsGameSocket(pipe.peerPid(),(InetSocketAddress)server.getRemoteSocketAddress(),(InetSocketAddress)server.getLocalSocketAddress()));
                assertFalse(pipe.ownsGameSocket(pipe.peerPid()+1,(InetSocketAddress)server.getRemoteSocketAddress(),(InetSocketAddress)server.getLocalSocketAddress()));
                byte[] payload={1,2,3};IntByReference written=new IntByReference();
                assertTrue(kernel.WriteFile(peer,payload,3,written,null));assertEquals(3,written.getValue());
                assertArrayEquals(payload,pipe.read(3,System.nanoTime()+TimeUnit.SECONDS.toNanos(2),()->true));
            }finally{kernel.CloseHandle(peer);}
        }
    }
}
