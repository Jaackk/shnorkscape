package com.opennxt.security;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Optional, invited guest accounts. Local single-player admission is intentionally unchanged. */
public final class NativeLanAccess {
    private static final int ITERATIONS=600000, MAX_ACCOUNTS=32;
    private static volatile NativeLanAccess active;
    private final String address;
    private final Map<String,byte[][]> accounts;
    private final Map<String,long[]> attempts=new HashMap<>();
    private NativeLanAccess(String address,Map<String,byte[][]> accounts){this.address=address;this.accounts=accounts;}

    public static synchronized void initialize(String localBind) throws Exception {
        if(!InetAddress.getByName(localBind).isLoopbackAddress())
            throw new IllegalStateException("Primary listener must remain loopback; use explicit LAN mode");
        String address=System.getProperty("opennxt.lan.address","").trim();
        active=null;
        NativeLanAccess candidate=address.isEmpty()?null:load(address,Paths.get(System.getProperty("opennxt.lan.credentials","")));
        if(candidate!=null && NetworkInterface.getByInetAddress(InetAddress.getByName(address))==null)
            throw new IllegalStateException("LAN address is not assigned to this host");
        active=candidate;
    }
    static NativeLanAccess load(String address,Path file) throws Exception {
        if(!privateIpv4(address))throw new IllegalArgumentException("LAN address must be an explicit RFC1918 IPv4 address");
        Properties p=read(file);
        Map<String,byte[][]> accounts=new HashMap<>();
        for(String key:p.stringPropertyNames()) {
            if(key.equals("schema"))continue;
            if(!validName(key))throw new IllegalArgumentException("Invalid or protected guest account");
            String[] fields=p.getProperty(key).split(":",-1);
            if(fields.length!=2)throw new IllegalArgumentException("Malformed credential record");
            byte[] salt=Base64.getDecoder().decode(fields[0]),hash=Base64.getDecoder().decode(fields[1]);
            if(salt.length!=16||hash.length!=32)throw new IllegalArgumentException("Malformed credential dimensions");
            accounts.put(key,new byte[][]{salt,hash});
        }
        if(accounts.isEmpty()||accounts.size()>MAX_ACCOUNTS)throw new IllegalArgumentException("LAN requires 1..32 provisioned guests");
        return new NativeLanAccess(address,Collections.unmodifiableMap(accounts));
    }
    public static String address(){NativeLanAccess p=active;return p==null?null:p.address;}
    public static boolean loopback(SocketAddress remote){
        return remote instanceof InetSocketAddress && ((InetSocketAddress)remote).getAddress()!=null
                && ((InetSocketAddress)remote).getAddress().isLoopbackAddress();
    }
    public static boolean allowedPeer(SocketAddress remote){
        if(loopback(remote))return true;
        return active!=null&&remote instanceof InetSocketAddress&&((InetSocketAddress)remote).getAddress()!=null
                &&privateIpv4(((InetSocketAddress)remote).getAddress().getHostAddress());
    }
    public static String hostFor(SocketAddress remote,String local){return loopback(remote)?local:address();}
    public static boolean authenticate(SocketAddress remote,String username,String password){
        return "accepted".equals(authenticateReason(remote,username,password));
    }
    /** Local diagnostic only: never contains a password, verifier, token or raw input. */
    public static String authenticateReason(SocketAddress remote,String username,String password){
        if(loopback(remote))return "accepted";
        NativeLanAccess p=active;
        if(p==null)return "lan-disabled";
        if(!allowedPeer(remote))return "peer-denied";
        return p.verifyReason(((InetSocketAddress)remote).getAddress().getHostAddress(),username,password);
    }
    synchronized boolean verify(String peer,String username,String password){
        return "accepted".equals(verifyReason(peer,username,password));
    }
    synchronized String verifyReason(String peer,String username,String password){
        long now=System.nanoTime();
        long[] window=attempts.get(peer);
        if(window==null){if(attempts.size()>=64)return "peer-limit";window=new long[]{now,0};attempts.put(peer,window);}
        if(now-window[0]>60000000000L){window[0]=now;window[1]=0;}
        if(++window[1]>12)return "attempt-limit";
        if(username==null||password==null||password.length()<8||password.length()>128)return "invalid-input-bounds";
        String canonical=username.toLowerCase(Locale.ROOT);
        if(!validName(canonical))return "invalid-or-protected-name";
        byte[][] credential=accounts.get(canonical);if(credential==null)return "not-invited";
        char[] secret=password.toCharArray();
        try{return MessageDigest.isEqual(credential[1],derive(secret,credential[0]))?"accepted":"password-mismatch";}
        catch(GeneralSecurityException failure){return "verification-error";}
        finally{Arrays.fill(secret,'\0');}
    }
    static boolean privateIpv4(String text){
        if(text==null||!text.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}"))return false;
        String[] parts=text.split("\\.");int[] n=new int[4];
        for(int i=0;i<4;i++){n[i]=Integer.parseInt(parts[i]);if(n[i]>255||!parts[i].equals(Integer.toString(n[i])))return false;}
        return n[0]==10 || n[0]==172&&n[1]>=16&&n[1]<=31 || n[0]==192&&n[1]==168;
    }
    private static boolean validName(String name){
        return name.matches("[a-z][a-z0-9]{2,11}")&&!name.equals("jaxa")&&!name.startsWith("layoutgate")
                &&!name.equals("admin")&&!name.equals("owner");
    }
    private static Properties read(Path file)throws IOException {
        if(!file.isAbsolute()||Files.isSymbolicLink(file)||!Files.isRegularFile(file)||Files.size(file)>16384)
            throw new IOException("Missing, unsafe or oversized LAN credential store");
        Properties p=new Properties(){@Override public synchronized Object put(Object key,Object value){
            if(containsKey(key))throw new IllegalArgumentException("Duplicate credential key");return super.put(key,value);
        }};
        try(InputStream in=Files.newInputStream(file)){p.load(in);}
        if(!"1-pbkdf2-sha256-600000".equals(p.getProperty("schema")))throw new IOException("Unknown credential schema");
        return p;
    }
    private static byte[] derive(char[] secret,byte[] salt)throws GeneralSecurityException {
        PBEKeySpec spec=new PBEKeySpec(secret,salt,ITERATIONS,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        finally{spec.clearPassword();}
    }
    /** Operator-only console provisioning; never receives passwords in argv, chat or logs. */
    static void validatePasswordChange(Properties accounts,String name,boolean change,boolean profileExists,char[] secret,char[] confirm) {
        if(!validName(name))throw new IllegalArgumentException("Invalid or protected guest account");
        if(change?!accounts.containsKey(name):(profileExists||accounts.containsKey(name)||accounts.size()>MAX_ACCOUNTS))
            throw new IllegalStateException("Change requires an existing invitation; new invitations cannot claim existing profiles");
        if(secret==null||confirm==null||secret.length<8||secret.length>16||!Arrays.equals(secret,confirm))
            throw new IllegalArgumentException("Choose matching passwords of 8-16 letters or digits");
        for(char c:secret)if(!(c>='a'&&c<='z'||c>='A'&&c<='Z'||c>='0'&&c<='9'))
            throw new IllegalArgumentException("Use letters and digits only for native client compatibility");
    }
    public static void main(String[] args)throws Exception {
        boolean generated=args.length==5&&args[2].equals("--generate");
        boolean change=args.length==4&&args[2].equals("--change");
        if(args.length!=2&&!generated&&!change)throw new IllegalArgumentException("Usage: NativeLanAccess <absolute credential file> <absolute player directory> [--generate <name> <absolute private handoff file> | --change <name>]");
        Console console=System.console();if(console==null&&!generated)throw new IllegalStateException("Use a normal Windows console for hidden password input");
        Path file=Paths.get(args[0]),players=Paths.get(args[1]);
        if(!file.isAbsolute()||!players.isAbsolute())throw new IllegalArgumentException("Absolute paths required");
        Path handoff=generated?Paths.get(args[4]):null;
        if(generated&&(!handoff.isAbsolute()||Files.exists(handoff)||!handoff.getParent().equals(file.getParent())))
            throw new IllegalArgumentException("Generated handoff must be a new file beside the private credential store");
        String name=(generated||change?args[3]:console.readLine("New guest account (3-12 letters/digits): ")).toLowerCase(Locale.ROOT);
        if(!validName(name))throw new IllegalArgumentException("Invalid or protected guest account");
        byte[] identity=MessageDigest.getInstance("SHA-256").digest(name.getBytes(StandardCharsets.US_ASCII));
        StringBuilder hex=new StringBuilder();for(byte b:identity)hex.append(String.format("%02x",b&255));
        boolean profileExists=Files.exists(players.resolve(hex+".950"));
        if(!change&&profileExists)throw new IllegalStateException("Existing player profile: provisioning may not claim it");
        Properties p=new Properties();if(Files.exists(file))p.putAll(read(file));
        if(change?!p.containsKey(name):(p.containsKey(name)||p.size()>MAX_ACCOUNTS))throw new IllegalStateException("Account missing for change, already exists, or invitation limit reached");
        char[] secret;
        if(generated){secret=new char[16];String alphabet="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";SecureRandom random=new SecureRandom();for(int i=0;i<secret.length;i++)secret[i]=alphabet.charAt(random.nextInt(alphabet.length()));}
        else secret=console.readPassword("New password for %s (8-16 letters/digits, hidden): ",name);
        char[] confirm=generated?secret.clone():console.readPassword("Repeat password: ");
        try {
            validatePasswordChange(p,name,change,profileExists,secret,confirm);
            byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);
            p.setProperty("schema","1-pbkdf2-sha256-600000");
            p.setProperty(name,Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(secret,salt)));
            Files.createDirectories(file.getParent());
            if(generated) {
                try(Writer out=Files.newBufferedWriter(handoff,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW)) {
                    out.write("SHNORKSCAPE private LAN invitation\nUsername: "+name+"\nPassword: ");
                    out.write(secret);out.write("\nShare privately with this guest only. Never include in the client ZIP.\n");
                }
            }
            Path temporary=Files.createTempFile(file.getParent(),"lan-credentials-",".tmp");
            try {
                if(Files.exists(file)) {
                    java.nio.file.attribute.AclFileAttributeView oldAcl=Files.getFileAttributeView(file,java.nio.file.attribute.AclFileAttributeView.class);
                    if(oldAcl!=null)Files.getFileAttributeView(temporary,java.nio.file.attribute.AclFileAttributeView.class).setAcl(oldAcl.getAcl());
                    if(change)Files.copy(file,file.resolveSibling("lan-credentials-before-change-"+UUID.randomUUID()+".properties"),StandardCopyOption.COPY_ATTRIBUTES);
                }
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();p.store(bytes,"Local invited guests; never commit");
                try(FileChannel out=FileChannel.open(temporary,StandardOpenOption.WRITE)){ByteBuffer data=ByteBuffer.wrap(bytes.toByteArray());while(data.hasRemaining())out.write(data);out.force(true);}
                Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            }finally{Files.deleteIfExists(temporary);}
            System.out.println("Guest credential saved. The old invitation password is no longer current. Close clients and restart the LAN server to activate it.");
        }finally{if(secret!=null)Arrays.fill(secret,'\0');if(confirm!=null)Arrays.fill(confirm,'\0');}
    }
}
