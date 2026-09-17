package com.rs.tools;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * CachePacker
 * ============================================================
 * Converts a flat-file cache (numbered folders with .dat files)
 * into the traditional sector-based RSPS cache format.
 *
 * Input layout expected:
 *   cache_raw/
 *     0/   0.dat, 1.dat, 2.dat ...   <- index 0, archives 0..N
 *     1/   0.dat, 1.dat ...           <- index 1
 *     ...
 *     60/  ...
 *     255/ 0.dat, 1.dat ...           <- master reference index
 *
 * Output produced:
 *   cache/
 *     main_file_cache.dat2            <- all data in 520-byte sectors
 *     main_file_cache.idx0            <- 6 bytes per archive: [size:3][sector:3]
 *     main_file_cache.idx1
 *     ...
 *     main_file_cache.idx60
 *     main_file_cache.idx255          <- master index
 *
 * Sector layout (520 bytes):
 *   Standard (archiveId < 65536):
 *     [archiveId:2][chunk:2][nextSector:3][indexId:1][data:512]
 *   Extended (archiveId >= 65536):
 *     [archiveId:4][chunk:2][nextSector:3][indexId:1][data:510]
 *
 * No external dependencies — pure Java 8+.
 * Just add this file to your project and run main().
 * ============================================================
 */
public class CachePacker {

    // ── Sector geometry ─────────────────────────────────────
    private static final int SECTOR_SIZE    = 520;
    private static final int STD_DATA_SIZE  = 512;   // 520 - 8  (standard header)
    private static final int EXT_DATA_SIZE  = 510;   // 520 - 10 (extended header for ID > 0xFFFF)

    // ── Paths — edit these to match your project structure ──
    private static final String INPUT_PATH  = "data/cache_raw/";
    private static final String OUTPUT_PATH = "data/cache/";

    // ────────────────────────────────────────────────────────
    public static void main(String[] args) throws IOException {
        new CachePacker().pack(INPUT_PATH, OUTPUT_PATH);
    }

    // ────────────────────────────────────────────────────────
    public void pack(String inputPath, String outputPath) throws IOException {
        File outputDir = new File(outputPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Could not create output directory: " + outputPath);
        }

        File dat2File = new File(outputDir, "main_file_cache.dat2");

        System.out.println("=================================================");
        System.out.println("  RSPS Cache Packer");
        System.out.println("=================================================");
        System.out.println("  Input  : " + new File(inputPath).getAbsolutePath());
        System.out.println("  Output : " + dat2File.getAbsolutePath());
        System.out.println();

        // Archive IDs to process: 0-60, then 255 (master index)
        int[] archiveIds = buildArchiveIdList();

        int currentSector = 0;
        int totalPacked   = 0;

        try (RandomAccessFile dat2 = new RandomAccessFile(dat2File, "rw")) {
            dat2.setLength(0); // wipe any existing file

            for (int archiveId : archiveIds) {
                File folder = new File(inputPath, String.valueOf(archiveId));
                if (!folder.isDirectory()) {
                    continue;
                }

                PackResult result = packArchive(archiveId, folder, dat2, outputDir, currentSector);
                currentSector = result.nextSector;
                totalPacked  += result.filesPacked;
            }
        }

        // ── Final summary ────────────────────────────────────
        long dat2Bytes = dat2File.length();
        System.out.println();
        System.out.println("=================================================");
        System.out.printf ("  Total sectors written : %,d%n",        currentSector);
        System.out.printf ("  Total archives packed : %,d%n",        totalPacked);
        System.out.printf ("  dat2 size             : %,d bytes (%.2f MB)%n",
                dat2Bytes, dat2Bytes / 1_048_576.0);
        System.out.println("=================================================");
        System.out.println("  Done! Cache written to: " + outputPath);
        System.out.println("=================================================");
    }

    // ────────────────────────────────────────────────────────
    private PackResult packArchive(int archiveId,
                                   File folder,
                                   RandomAccessFile dat2,
                                   File outputDir,
                                   int currentSector) throws IOException {

        // Collect every *.dat file, keyed by its numeric name (= archive/file ID)
        Map<Integer, File> datFiles = new TreeMap<>();
        File[] entries = folder.listFiles();
        if (entries != null) {
            for (File f : entries) {
                String name = f.getName();
                if (name.endsWith(".dat")) {
                    String stem = name.substring(0, name.length() - 4);
                    try {
                        datFiles.put(Integer.parseInt(stem), f);
                    } catch (NumberFormatException ignored) { /* non-numeric filename */ }
                }
            }
        }

        if (datFiles.isEmpty()) {
            System.out.printf("  Index %3d : (no .dat files found, skipped)%n", archiveId);
            return new PackResult(currentSector, 0);
        }

        int maxId      = Collections.max(datFiles.keySet());
        int packed     = 0;
        int nullSlots  = 0;

        // idx file collects 6 bytes per slot: [size:3][firstSector:3]
        ByteArrayOutputStream idxBuf = new ByteArrayOutputStream((maxId + 1) * 6);

        for (int fileId = 0; fileId <= maxId; fileId++) {
            File datFile = datFiles.get(fileId);

            // ── Null / missing slot ──────────────────────────
            if (datFile == null) {
                writeNullIdxEntry(idxBuf);
                nullSlots++;
                continue;
            }

            byte[] data = Files.readAllBytes(datFile.toPath());

            // ── Empty file → treat as null entry ────────────
            if (data.length == 0) {
                writeNullIdxEntry(idxBuf);
                nullSlots++;
                continue;
            }

            // ── Write index entry ────────────────────────────
            int firstSector = currentSector;
            writeInt24(idxBuf, data.length);
            writeInt24(idxBuf, firstSector);

            // ── Write data sectors ───────────────────────────
            boolean extended = (fileId > 0xFFFF);
            int     dataSize = extended ? EXT_DATA_SIZE : STD_DATA_SIZE;

            int chunk  = 0;
            int offset = 0;

            while (offset < data.length) {
                int chunkLen   = Math.min(data.length - offset, dataSize);
                int nextSector = (offset + dataSize < data.length) ? (currentSector + 1) : 0;

                // Header
                if (extended) {
                    dat2.writeInt(fileId);                  // 4 bytes  (extended)
                } else {
                    dat2.writeShort(fileId);                // 2 bytes  (standard)
                }
                dat2.writeShort(chunk);                     // 2 bytes  chunk number
                writeInt24(dat2, nextSector);               // 3 bytes  next sector pointer
                dat2.writeByte(archiveId & 0xFF);           // 1 byte   index/archive ID

                // Data payload — padded to fill the sector data area
                dat2.write(data, offset, chunkLen);
                if (chunkLen < dataSize) {
                    dat2.write(new byte[dataSize - chunkLen]);
                }

                offset        += dataSize;
                currentSector++;
                chunk++;
            }

            packed++;
        }

        // ── Write the idx file ───────────────────────────────
        String idxName = (archiveId == 255)
                ? "main_file_cache.idx255"
                : "main_file_cache.idx" + archiveId;

        File idxFile = new File(outputDir, idxName);
        try (FileOutputStream fos = new FileOutputStream(idxFile)) {
            idxBuf.writeTo(fos);
        }

        System.out.printf("  Index %3d : %5d archives packed | %5d null slots | idx size = %,d bytes%n",
                archiveId, packed, nullSlots, idxBuf.size());

        return new PackResult(currentSector, packed);
    }

    // ── Helpers ──────────────────────────────────────────────

    /** Writes a 6-byte zero entry to the index buffer (no file at this slot). */
    private static void writeNullIdxEntry(OutputStream out) throws IOException {
        out.write(new byte[6]);
    }

    /** Writes a 24-bit (3-byte) big-endian integer to a stream. */
    private static void writeInt24(OutputStream out, int value) throws IOException {
        out.write((value >> 16) & 0xFF);
        out.write((value >>  8) & 0xFF);
        out.write( value        & 0xFF);
    }

    /** Writes a 24-bit (3-byte) big-endian integer to a RandomAccessFile. */
    private static void writeInt24(RandomAccessFile raf, int value) throws IOException {
        raf.writeByte((value >> 16) & 0xFF);
        raf.writeByte((value >>  8) & 0xFF);
        raf.writeByte( value        & 0xFF);
    }

    /** Returns the ordered list of archive IDs to process: 0..60 then 255. */
    private static int[] buildArchiveIdList() {
        int[] ids = new int[62]; // indices 0-60 = 61 entries, +1 for 255
        for (int i = 0; i <= 60; i++) {
            ids[i] = i;
        }
        ids[61] = 255;
        return ids;
    }

    // ── Small result carrier ─────────────────────────────────
    private static final class PackResult {
        final int nextSector;
        final int filesPacked;
        PackResult(int nextSector, int filesPacked) {
            this.nextSector  = nextSector;
            this.filesPacked = filesPacked;
        }
    }
}