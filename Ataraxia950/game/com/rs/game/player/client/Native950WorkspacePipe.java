package com.rs.game.player.client;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

/** Local, fixed-purpose transport. No native process reads/writes; OS ownership queries only. */
final class Native950WorkspacePipe implements AutoCloseable {
    static final String NAME="\\\\.\\pipe\\shnork-workspace-disposable-v5";
    interface Kernel extends StdCallLibrary {
        Pointer CreateNamedPipeW(WString name,int open,int mode,int instances,int out,int in,int timeout,Pointer security);
        boolean ConnectNamedPipe(Pointer pipe,Pointer overlapped);
        boolean DisconnectNamedPipe(Pointer pipe);
        boolean CloseHandle(Pointer handle);
        boolean GetNamedPipeClientProcessId(Pointer pipe,IntByReference pid);
        boolean PeekNamedPipe(Pointer pipe,Pointer buffer,int size,Pointer read,IntByReference available,Pointer left);
        boolean ReadFile(Pointer pipe,byte[] buffer,int count,IntByReference read,Pointer overlapped);
        boolean WriteFile(Pointer pipe,byte[] buffer,int count,IntByReference written,Pointer overlapped);
        Pointer OpenProcess(int access,boolean inherit,int pid);
        boolean QueryFullProcessImageNameW(Pointer process,int flags,char[] path,IntByReference length);
    }
    interface Ip extends StdCallLibrary {int GetExtendedTcpTable(Pointer table,IntByReference size,boolean order,int family,int kind,int reserved);}
    private final Kernel kernel;
    private final Pointer handle;
    Native950WorkspacePipe() throws IOException {
        this(NAME);
    }
    Native950WorkspacePipe(String name) throws IOException {
        kernel=Native.load("kernel32",Kernel.class);
        // FIRST_PIPE_INSTANCE prevents attaching to an existing endpoint. NOWAIT bounds all IO.
        handle=kernel.CreateNamedPipeW(new WString(name),3|0x80000,1|8,1,16384,16384,0,null);
        if(handle==null||Pointer.nativeValue(handle)==-1L)throw new IOException("Cannot create exclusive capture pipe: "+Native.getLastError());
    }
    boolean connect() throws IOException {
        if(kernel.ConnectNamedPipe(handle,null))return true;
        int error=Native.getLastError();
        if(error==535)return true;
        if(error==536)return false;
        throw new IOException("Capture connect failed: "+error);
    }
    int peerPid() throws IOException {
        IntByReference pid=new IntByReference();
        if(!kernel.GetNamedPipeClientProcessId(handle,pid)||pid.getValue()<=0)throw new IOException("No pipe peer PID");
        return pid.getValue();
    }
    boolean ownsGameSocket(int pid,InetSocketAddress remote,InetSocketAddress local) throws IOException {
        Ip ip=Native.load("iphlpapi",Ip.class);IntByReference size=new IntByReference();
        int error=ip.GetExtendedTcpTable(null,size,false,2,5,0);
        if(error!=122||size.getValue()<4||size.getValue()>1048576)throw new IOException("TCP table bounds");
        try(Memory memory=new Memory(size.getValue())) {
            if(ip.GetExtendedTcpTable(memory,size,false,2,5,0)!=0)throw new IOException("TCP ownership changed during query");
            byte[] bytes=memory.getByteArray(0,size.getValue());
            return owns(bytes,pid,remote,local);
        }
    }
    static boolean owns(byte[] table,int pid,InetSocketAddress remote,InetSocketAddress local) throws IOException {
        if(table.length<4)throw new IOException("Truncated TCP table");
        java.nio.ByteBuffer b=java.nio.ByteBuffer.wrap(table).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int count=b.getInt(0);if(count<0||count>16384||4L+24L*count>table.length)throw new IOException("TCP count bounds");
        for(int i=0;i<count;i++) {
            int at=4+i*24;
            if(b.getInt(at)==5&&b.getInt(at+20)==pid
                    &&Arrays.equals(Arrays.copyOfRange(table,at+4,at+8),remote.getAddress().getAddress())
                    &&port(table,at+8)==remote.getPort()
                    &&Arrays.equals(Arrays.copyOfRange(table,at+12,at+16),local.getAddress().getAddress())
                    &&port(table,at+16)==local.getPort())return true;
        }
        return false;
    }
    private static int port(byte[] b,int at){return ((b[at]&255)<<8)|(b[at+1]&255);}
    void verifyImage(int pid,Path expected,String imageHash,String dllHash) throws IOException {
        Pointer process=kernel.OpenProcess(0x1000,false,pid);
        if(process==null)throw new IOException("Cannot query peer image");
        char[] path=new char[32768];IntByReference n=new IntByReference(path.length);
        try {
            if(!kernel.QueryFullProcessImageNameW(process,0,path,n))throw new IOException("Cannot query peer path");
            Path actual=Paths.get(new String(path,0,n.getValue())).toRealPath();
            if(!actual.equals(expected.toRealPath())||!sha(actual,17000000).equals(imageHash)
                    ||!sha(actual.resolveSibling("shnork_workspace_probe_v5.dll"),4000000).equals(dllHash))
                throw new IOException("Capture peer image/DLL mismatch");
        }finally{kernel.CloseHandle(process);}
    }
    static String sha(Path path,int max) throws IOException {
        try(InputStream in=Files.newInputStream(path)) {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] b=new byte[8192];int total=0,n;
            while((n=in.read(b))!=-1){total+=n;if(total>max)throw new IOException("Image length bound");digest.update(b,0,n);}
            StringBuilder s=new StringBuilder();for(byte v:digest.digest())s.append(String.format("%02x",v&255));return s.toString();
        }catch(java.security.NoSuchAlgorithmException e){throw new IOException(e);}
    }
    byte[] read(int count,long deadline,BooleanSupplier live) throws IOException,InterruptedException {
        if(count<0||count>8192)throw new IOException("Capture frame bounds");
        byte[] result=new byte[count];int at=0;
        while(at<count) {
            if(!live.getAsBoolean()||System.nanoTime()>deadline)throw new IOException("Capture session closed/timeout");
            IntByReference available=new IntByReference();
            if(!kernel.PeekNamedPipe(handle,null,0,null,available,null))throw new IOException("Capture peer disconnected");
            if(available.getValue()==0){Thread.sleep(20);continue;}
            byte[] part=new byte[Math.min(count-at,available.getValue())];IntByReference got=new IntByReference();
            if(!kernel.ReadFile(handle,part,part.length,got,null)||got.getValue()<=0||got.getValue()>part.length)throw new IOException("Capture read failed");
            System.arraycopy(part,0,result,at,got.getValue());at+=got.getValue();
        }return result;
    }
    void write(byte[] bytes) throws IOException {
        if(bytes.length>8192)throw new IOException("Capture frame too large");
        IntByReference written=new IntByReference();
        if(!kernel.WriteFile(handle,bytes,bytes.length,written,null)||written.getValue()!=bytes.length)throw new IOException("Capture write incomplete");
    }
    @Override public void close(){kernel.DisconnectNamedPipe(handle);kernel.CloseHandle(handle);}
}
