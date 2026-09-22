package com.opennxt.security;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Uses temporary invitations only; never prints generated passwords. */
public final class LanProvisioningAcceptance {
    private static void check(boolean ok){if(!ok)throw new AssertionError();}
    private static void rejected(String[] args)throws Exception {
        try{NativeLanAccess.main(args);throw new AssertionError("Unsafe provisioning accepted");}
        catch(IllegalArgumentException|IllegalStateException expected){}
    }
    public static void main(String[] ignored)throws Exception {
        Path dir=Files.createTempDirectory("lan-provision-test-");
        try {
            Path players=Files.createDirectory(dir.resolve("players")),store=dir.resolve("credentials.properties"),handoff=dir.resolve("invitation.txt");
            String[] args={store.toString(),players.toString(),"--generate","nooby",handoff.toString()};
            for(String name:new String[]{"jaxa","layoutgate2","admin","owner"}) {
                String[] bad=args.clone();bad[3]=name;rejected(bad);
            }
            NativeLanAccess.main(args);
            String text=Files.readString(handoff),password=text.lines().filter(s->s.startsWith("Password: ")).findFirst().orElseThrow().substring(10);
            check(password.length()==24);
            check(!Files.readString(store).contains(password));
            NativeLanAccess access=NativeLanAccess.load("192.168.0.91",store);
            check(access.verify("192.168.0.22","nooby",password));
            check(!access.verify("192.168.0.22","jaxa",password));
            check(!access.verify("192.168.0.22","nooby",password+"wrong"));
            rejected(args);
            StringBuilder id=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest("existing".getBytes(StandardCharsets.US_ASCII)))id.append(String.format("%02x",b&255));
            Files.writeString(players.resolve(id+".950"),"protected-test-profile");
            String[] existing=args.clone();existing[3]="existing";existing[4]=dir.resolve("other.txt").toString();rejected(existing);
            check(!Files.exists(dir.resolve("other.txt")));
            System.out.println("PASS: generated invitation authentication, no secret in store, protected/existing account and overwrite rejection");
        } finally {
            try(var paths=Files.walk(dir)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}
        }
    }
}
