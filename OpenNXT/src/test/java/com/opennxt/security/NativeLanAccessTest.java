package com.opennxt.security;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Standalone test: no server, sockets, real accounts or real credential files. */
public final class NativeLanAccessTest {
    private static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args)throws Exception {
        for(String address:new String[]{"10.0.0.1","172.16.1.2","172.31.255.254","192.168.1.5"})check(NativeLanAccess.privateIpv4(address));
        for(String address:new String[]{"127.0.0.1","0.0.0.0","8.8.8.8","172.32.0.1","192.168.1.999","010.0.0.1","localhost","::1"})check(!NativeLanAccess.privateIpv4(address));
        NativeLanAccess.initialize("127.0.0.2");
        check(NativeLanAccess.authenticate(new InetSocketAddress("127.0.0.2",1234),"jaxa",""));
        check(!NativeLanAccess.authenticate(new InetSocketAddress("192.168.1.2",1234),"jaxa","anything"));
        check(!NativeLanAccess.authenticate(InetSocketAddress.createUnresolved("localhost",1234),"jaxa","anything"));
        Path file=Files.createTempFile("lan-auth-test-",".properties");
        byte[] salt=new byte[16];Arrays.fill(salt,(byte)37);
        PBEKeySpec spec=new PBEKeySpec("disposable-test-password".toCharArray(),salt,600000,256);
        byte[] hash=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();spec.clearPassword();
        String record=Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(hash);
        String valid="schema=1-pbkdf2-sha256-600000\nnooby="+record+"\n";
        try {
            Files.write(file,valid.getBytes(StandardCharsets.US_ASCII));
            NativeLanAccess p=NativeLanAccess.load("192.168.1.2",file);
            check(p.verify("192.168.1.3","Nooby","disposable-test-password"));
            check(!p.verify("192.168.1.3","nooby","wrong-password"));
            check(!p.verify("192.168.1.3","other","disposable-test-password"));
            check(!p.verify("192.168.1.3","jaxa","disposable-test-password"));
            for(int i=0;i<13;i++)p.verify("192.168.1.3","nooby","bad-password");
            check(!p.verify("192.168.1.3","nooby","disposable-test-password"));
            check(p.verify("192.168.1.4","nooby","disposable-test-password"));
            for(String malformed:new String[]{valid+"nooby="+record+"\n",valid.replace("nooby=","jaxa="),valid.replace("600000","1"),valid.replace(record,"bad:bad")}){
                Files.write(file,malformed.getBytes(StandardCharsets.US_ASCII));
                try{NativeLanAccess.load("192.168.1.2",file);throw new AssertionError("Malformed credentials admitted");}catch(IllegalArgumentException|java.io.IOException expected){}
            }
        }finally{Files.delete(file);}
        System.out.println("PASS: LAN private-address bounds, default denial, local compatibility, account/password binding, rate limit and malformed-store rejection");
    }
}
