package com.rs.cache.filestore.store;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;
/** Independent verification through the production JS5 reader, without cache mutation. */
public final class Native950DeveloperReferenceCheck {
    static void require(boolean ok){if(!ok)throw new AssertionError("JS5 metadata mismatch");}
    static ReferenceTable read(Path p)throws Exception{return new ReferenceTable(new Archive(255,Files.readAllBytes(p),null));}
    public static void main(String[] args)throws Exception{
        Path staged=Paths.get(args[0]);ReferenceTable before=read(Paths.get("cache/255/12.dat")),after=read(staged.resolve("255/12.dat"));
        require(Arrays.equals(after.getValidArchiveIds(),before.getValidArchiveIds()));
        Set<Integer> changed=new HashSet<>(Arrays.asList(8286,21124,21125,21126,21127,21128,21129));
        for(int id:before.getValidArchiveIds()){
            ArchiveReference a=before.getArchives()[id],b=after.getArchives()[id];
            require(a.getNameHash()==b.getNameHash());
            if(!changed.contains(id))require(a.getCRC()==b.getCRC()&&a.getHash()==b.getHash()&&a.getRevision()==b.getRevision()&&a.getCompressed()==b.getCompressed()&&a.getUncompressed()==b.getUncompressed());
            require(Arrays.equals(a.getValidFileIds(),b.getValidFileIds()));
            for(int f:a.getValidFileIds())require(a.getFiles()[f].getNameHash()==b.getFiles()[f].getNameHash());
        }
        for(int id:changed){
            byte[] packed=Files.readAllBytes(staged.resolve("12/"+id+".dat"));Archive archive=new Archive(id,packed,null);ArchiveReference ref=after.getArchives()[id];
            CRC32 crc=new CRC32();crc.update(packed,0,packed.length-2);require((int)crc.getValue()==ref.getCRC());
            crc.reset();crc.update(archive.getData());require((int)crc.getValue()==ref.getHash());
            require(ref.getCompressed()==packed.length-2&&ref.getUncompressed()==archive.getData().length&&ref.getRevision()==before.getArchives()[id].getRevision()+1&&Arrays.equals(new int[]{0},ref.getValidFileIds()));
            require(((packed[packed.length-2]&255)<<8|(packed[packed.length-1]&255))==(ref.getRevision()&65535));
        }
        System.out.println("PASS: unrelated references preserved; seven replacement payloads accepted by production JS5 reader.");
    }
}
