package com.rs.game.player.client;

import com.google.gson.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Separate bounded workspace sidecar. Caller performs disk work off the world thread. */
final class Native950WorkspaceStore {
    static final int MAX_BYTES=16384;
    private static final int MAGIC=0x57533935,VERSION=1;
    static final String IMAGE="3d4e432e8cb81d5b83cd3cb2064669228d24231779ae997fe78335a81e43364d";
    private final Path directory;
    private final JsonObject schema;
    Native950WorkspaceStore(Path directory,JsonObject schema) {
        this.directory=directory.toAbsolutePath().normalize();this.schema=schema;
    }
    static final class Record {
        final String account;
        final long revision;
        final String image;
        final SortedMap<Integer,Integer> values;
        Record(String account,long revision,Map<Integer,Integer> values) {
            this(account,revision,values,IMAGE);
        }
        Record(String account,long revision,Map<Integer,Integer> values,String image) {
            this.account=Native950Save.canonicalUsername(account);
            if(!IMAGE.equals(image)&&!Native950WorkspaceCapture.IMAGE.equals(image))throw new IllegalArgumentException("Unsupported workspace image");
            this.image=image;
            if(revision<1)throw new IllegalArgumentException("Invalid workspace revision");
            this.revision=revision;this.values=Collections.unmodifiableSortedMap(new TreeMap<>(values));
        }
        JsonObject snapshot() {
            JsonObject o=new JsonObject();o.addProperty("version",4);o.addProperty("status","snapshot-stable");
            o.addProperty("nativeWrites",false);o.addProperty("schemaSha256",Native950LayoutFixture.SCHEMA_SHA);
            JsonArray rows=new JsonArray();
            for(Map.Entry<Integer,Integer> e:values.entrySet()) {
                JsonObject r=new JsonObject();r.addProperty("id",e.getKey());r.addProperty("stable",true);
                r.addProperty("found",e.getValue()!=null);
                if(e.getValue()==null){r.add("variantTag",JsonNull.INSTANCE);r.add("int32",JsonNull.INSTANCE);}
                else{r.addProperty("variantTag",0);r.addProperty("int32",e.getValue());}rows.add(r);
            }
            o.add("items",rows);return o;
        }
    }
    Record capture(String authenticatedAccount,long revision,JsonObject snapshot) {
        Native950LayoutFixture.require(IMAGE.equals(snapshot.get("imageSha256").getAsString()),"Unsupported capture image");
        Native950LayoutFixture.workspacePlan(schema,snapshot);
        Map<Integer,Integer> values=new TreeMap<>();
        for(JsonElement e:snapshot.getAsJsonArray("items")) {
            JsonObject r=e.getAsJsonObject();values.put(r.get("id").getAsInt(),r.get("found").getAsBoolean()?r.get("int32").getAsInt():null);
        }
        return new Record(authenticatedAccount,revision,values);
    }
    Path path(String account) {
        return directory.resolve(hex(hash(Native950Save.canonicalUsername(account).getBytes(StandardCharsets.US_ASCII)))+".workspace950");
    }
    synchronized Record load(String account) throws IOException {
        Path p=path(account);if(!Files.exists(p,LinkOption.NOFOLLOW_LINKS))return null;
        rejectLink(directory);rejectLink(p);return decode(readBounded(p),account);
    }
    synchronized void save(Record record) throws IOException {
        byte[] next=encode(record);
        Files.createDirectories(directory);rejectLink(directory);
        Record previous=load(record.account);
        if(previous!=null&&record.revision<=previous.revision)throw new IOException("Stale workspace revision");
        Path p=path(record.account),prior=p.resolveSibling(p.getFileName()+".previous");
        if(Files.exists(prior,LinkOption.NOFOLLOW_LINKS))rejectLink(prior);
        // Retain a verified generation before replacing the current one. No non-atomic fallback.
        if(previous!=null)replace(prior,encode(previous));
        replace(p,next);
    }
    private static void replace(Path target,byte[] bytes) throws IOException {
        Path tmp=Files.createTempFile(target.getParent(),".workspace-",".tmp");
        try {
            try(FileChannel out=FileChannel.open(tmp,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())out.write(buffer);out.force(true);
            }
            Files.move(tmp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } finally {Files.deleteIfExists(tmp);}
    }
    byte[] encode(Record r) throws IOException {
        Native950LayoutFixture.workspacePlan(schema,r.snapshot());
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeInt(MAGIC);out.writeInt(VERSION);out.writeInt(950);
        out.writeUTF(Native950LayoutFixture.SCHEMA_SHA);out.writeUTF(r.image);out.writeUTF(r.account);
        out.writeLong(r.revision);out.writeShort(r.values.size());
        for(Map.Entry<Integer,Integer> e:r.values.entrySet()) {
            out.writeShort(e.getKey());out.writeByte(e.getValue()==null?0:1);if(e.getValue()!=null)out.writeInt(e.getValue());
        }
        out.flush();byte[] body=bytes.toByteArray();out.write(hash(body));out.flush();
        if(bytes.size()>MAX_BYTES)throw new IOException("Oversized workspace");return bytes.toByteArray();
    }
    Record decode(byte[] bytes,String account) throws IOException {
        if(bytes.length<32||bytes.length>MAX_BYTES)throw new IOException("Invalid workspace length");
        byte[] body=Arrays.copyOf(bytes,bytes.length-32);
        if(!MessageDigest.isEqual(hash(body),Arrays.copyOfRange(bytes,body.length,bytes.length)))throw new IOException("Workspace checksum mismatch");
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(body));
        if(in.readInt()!=MAGIC||in.readInt()!=VERSION||in.readInt()!=950
                ||!in.readUTF().equals(Native950LayoutFixture.SCHEMA_SHA))throw new IOException("Workspace identity/schema mismatch");
        String image=in.readUTF();
        if((!image.equals(IMAGE)&&!image.equals(Native950WorkspaceCapture.IMAGE))
                ||!in.readUTF().equals(Native950Save.canonicalUsername(account)))throw new IOException("Workspace image/account mismatch");
        long revision=in.readLong();int count=in.readUnsignedShort();
        if(count!=912)throw new IOException("Incomplete workspace");
        Map<Integer,Integer> values=new TreeMap<>();int last=-1;
        for(int i=0;i<count;i++) {
            int id=in.readUnsignedShort(),tag=in.readUnsignedByte();
            if(id<=last||tag>1)throw new IOException("Invalid workspace record");last=id;
            values.put(id,tag==0?null:in.readInt());
        }
        if(in.available()!=0)throw new IOException("Trailing workspace bytes");
        try {Record r=new Record(account,revision,values,image);Native950LayoutFixture.workspacePlan(schema,r.snapshot());return r;}
        catch(RuntimeException invalid){throw new IOException("Invalid workspace representation",invalid);}
    }
    private static byte[] readBounded(Path p) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try(InputStream in=Files.newInputStream(p,LinkOption.NOFOLLOW_LINKS)) {
            byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(out.size()+n>MAX_BYTES)throw new IOException("Oversized workspace");out.write(b,0,n);}
        }return out.toByteArray();
    }
    private static void rejectLink(Path p) throws IOException {if(Files.isSymbolicLink(p))throw new IOException("Workspace symlink refused");}
    private static byte[] hash(byte[] b) {try{return MessageDigest.getInstance("SHA-256").digest(b);}catch(Exception e){throw new IllegalStateException(e);}}
    private static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format("%02x",v&255));return s.toString();}
}
