package com.rs.game.player.client;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Shared, bounded developer teleport directory. No character save is modified. */
final class Native950SavedLocations {
    private static final byte[] MAGIC = {'L','O','C','S','9','5','0','1'};
    private static final int MAX_ENTRIES = 256;
    private static final int MAX_BYTES = 32768;
    private static final String FILE_PROPERTY = "ataraxia950.locationsFile";

    static final class Place {
        final String owner, name;
        final int x, y, plane;
        Place(String owner, String name, int x, int y, int plane) {
            this.owner=owner;this.name=name;this.x=x;this.y=y;this.plane=plane;
        }
        String line() { return name+" ("+owner+") - "+x+" "+y+" "+plane; }
    }

    private Native950SavedLocations() { }
    static Path file() { return Paths.get(System.getProperty(FILE_PROPERTY,"server-home/saved-locations950.bin")).toAbsolutePath().normalize(); }
    static boolean validName(String value) {
        return value!=null && value.length()>=2 && value.length()<=40
                && value.matches("[A-Za-z0-9][A-Za-z0-9 _'-]*") && value.trim().length()>=2;
    }
    static boolean validTile(int x,int y,int plane) {
        return x>=0&&x<=16383&&y>=0&&y<=16383&&plane>=0&&plane<=3;
    }
    static synchronized List<Place> list() throws IOException { return read(file()); }
    static synchronized Place save(String owner,String name,int x,int y,int plane) throws IOException {
        owner=Native950Save.canonicalUsername(owner);
        if(!validName(name)||!validTile(x,y,plane))throw new IllegalArgumentException("Invalid location");
        name=name.trim().replaceAll(" +"," ");
        List<Place> places=read(file());
        String keyOwner=owner,keyName=name;
        places.removeIf(place->place.owner.equals(keyOwner)&&place.name.equalsIgnoreCase(keyName));
        if(places.size()>=MAX_ENTRIES)throw new IllegalArgumentException("Location directory is full");
        Place saved=new Place(owner,name,x,y,plane);places.add(saved);
        write(file(),places);
        return saved;
    }
    private static List<Place> read(Path path) throws IOException {
        if(!Files.exists(path))return new ArrayList<>();
        if(!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS))throw new IOException("Location store is not a regular file");
        byte[] file=Files.readAllBytes(path);
        if(file.length<MAGIC.length+4+32||file.length>MAX_BYTES)throw new IOException("Invalid location store size");
        byte[] payload=Arrays.copyOf(file,file.length-32);
        if(!MessageDigest.isEqual(digest(payload),Arrays.copyOfRange(file,file.length-32,file.length)))
            throw new IOException("Location store checksum mismatch");
        try(DataInputStream input=new DataInputStream(new ByteArrayInputStream(payload))){
            byte[] magic=new byte[MAGIC.length];input.readFully(magic);
            if(!Arrays.equals(magic,MAGIC))throw new IOException("Unknown location store version");
            int count=input.readInt();
            if(count<0||count>MAX_ENTRIES)throw new IOException("Invalid location count");
            List<Place> places=new ArrayList<>();Set<String> keys=new HashSet<>();
            for(int i=0;i<count;i++){
                String owner=input.readUTF(),name=input.readUTF();
                int x=input.readInt(),y=input.readInt(),plane=input.readUnsignedByte();
                if(!owner.equals(Native950Save.canonicalUsername(owner))||!validName(name)||!validTile(x,y,plane)
                        ||!keys.add(owner+"\0"+name.toLowerCase(Locale.ROOT)))throw new IOException("Invalid location entry");
                places.add(new Place(owner,name,x,y,plane));
            }
            if(input.available()!=0)throw new IOException("Trailing location data");
            return places;
        }catch(IllegalArgumentException invalid){throw new IOException("Invalid location entry",invalid);}
    }
    private static void write(Path path,List<Place> places) throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(DataOutputStream output=new DataOutputStream(bytes)){
            output.write(MAGIC);output.writeInt(places.size());
            for(Place place:places){
                output.writeUTF(place.owner);output.writeUTF(place.name);
                output.writeInt(place.x);output.writeInt(place.y);output.writeByte(place.plane);
            }
        }
        byte[] payload=bytes.toByteArray();
        if(payload.length+32>MAX_BYTES)throw new IOException("Location store exceeds size limit");
        Path parent=path.getParent();if(parent==null)throw new IOException("Location store has no parent");
        Files.createDirectories(parent);
        Path temp=Files.createTempFile(parent,".locations950-",".tmp");
        try {
            try(OutputStream output=Files.newOutputStream(temp,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)){
                output.write(payload);output.write(digest(payload));output.flush();
            }
            Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temp); }
    }
    private static byte[] digest(byte[] value) {
        try{return MessageDigest.getInstance("SHA-256").digest(value);}
        catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
