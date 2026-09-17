package com.rs.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

import com.rs.cache.filestore.util.whirlpool.Whirlpool;
import com.rs.network.codec.ProtocolSet;

import lombok.Getter;
import lombok.Setter;
import lzma.sdk.lzma.Encoder;

public class NXTClientsManager {
    
    public static NXTClient[] clients;
    public static String path = "data/nxt clients/";
    public static int CompressCount;

    public static void main(String[] args) {
        init();
    }

    public static void init() {
        if (!new File(path).exists())
            return;
        
        File[] dirs = new File(path).listFiles();
        clients = new NXTClient[dirs.length];
        for (int i = 0; i < clients.length; i++) {
            File file = dirs[i];
            if (!file.isDirectory() || !file.getName().contains("binary"))
                continue;
            clients[i] = new NXTClient();
            clients[i].setBinaryType(Integer.parseInt(file.getName().replace("binary", "0")));
            clients[i].init();
        }
        Logger.getGlobal().info("Compressed "+ CompressCount +" Files & Loaded " + clients.length + " nxt clients!");
    }

    public static NXTClient getClientByType(int binaryType) {
        for (NXTClient client : clients) {
            if (client.binaryType == binaryType)
                return client;
        }
        return null;
    }

    public static class NXTClient {
        @Getter
        @Setter
        private int binaryType;
        @Getter
        private String[] hashs;
        @Getter
        private long[] Crcs;
        byte[] jav_config;
        byte[][] binaries;
        @Getter
        private int binaryCount = 1;
        private String DllsPath;
        private String[] DllsUNames = null;

        public void init() {
            DllsPath = path + "/binary" + binaryType + "/DLLs/";
            if (new File(DllsPath).exists()) {
                File[] Dlls = new File(DllsPath).listFiles();
                List<String> names = new ArrayList<String>();
                for (File file : Dlls) {
                    if (!file.getName().contains(".dll"))
                        names.add(file.getName());
                }
                DllsUNames = names.toArray(new String[names.size()]);
                binaryCount += DllsUNames.length;
            }
            compressFiles();
            binaries = new byte[binaryCount][];
            hashs = new String[binaryCount];
            Crcs = new long[binaryCount];
            generateHashAndCRC();
            try {
                jav_config = Files.readAllBytes(new File(path + "/binary" + binaryType + "/jav_config.ws").toPath());
                for (int i = 0; i < binaryCount; i++)
                    binaries[i] = DllsUNames == null || i == binaryCount - 1 ? Files.readAllBytes(new File(path + "/binary" + binaryType + "/" + getClientNames()[1]).toPath()) : Files.readAllBytes(new File(DllsPath + DllsUNames[i] + ".dll").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void generateHashAndCRC() {
            for (int i = 0; i < binaryCount; i++) {
                File file = DllsUNames == null || i == binaryCount - 1 ? new File(path + "/binary" + binaryType + "/" + getClientNames()[0]) : new File(DllsPath + DllsUNames[i]);
                if (!file.exists() || file.isDirectory())
                    return;
                try {
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    Crcs[i] = CRC(fileData);
                    hashs[i] = hash(fileData, ProtocolSet.LAUNCHER_MODULUS, ProtocolSet.LAUNCHER_EXPONENT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void compressFiles() {
            for (int i = 0; i < binaryCount; i++) {
                File uncompressedInput = null;
                File compressedOutPut = null;
                try {
                    if (DllsUNames == null || i == binaryCount - 1) {
                        uncompressedInput = new File(path + "/binary" + binaryType + "/" + getClientNames()[0]);
                        compressedOutPut = new File(path + "/binary" + binaryType + "/" + getClientNames()[1]);
                    } else {
                        uncompressedInput = new File(DllsPath + DllsUNames[i]);
                        compressedOutPut = new File(DllsPath + DllsUNames[i] + ".dll");
                    }
                    if(compressedOutPut.exists())
                        continue;
                } catch (Exception e) {
                    throw new RuntimeException("Error while trying to compress binary");
                }
                try {
                    CompressCount++;
                    compressedOutPut.createNewFile();
                    Encoder encoder = new Encoder();
                    encoder.setDictionarySize((1 << 23));
                    LzmaOutputStream out = new LzmaOutputStream(new BufferedOutputStream(new FileOutputStream(compressedOutPut)), encoder, uncompressedInput.length());
                    out.write(Files.readAllBytes(uncompressedInput.toPath()));
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public byte[] getJavConfig() {
            return jav_config;
        }

        public String[] getClientNames() {
            if (binaryType == 3)
                return new String[] { "ataraxiamac.dylib", "ataraxiamac" };
            if(binaryType == 4)
                return new String[] { "ataraxialinux.so", "ataraxialinux"};
            return new String[] { "ancientxclient", "ancientxclient.exe" };
        }

        public String getDownloadName(int index) {
            if (index >= binaries.length)
                throw new RuntimeException("Error while trying to get binary");
            if (index == binaryCount - 1)
                return getClientNames()[1];
            else
                return DllsUNames[index] + ".dll";
        }

        public int getDownloadIndex(String fileName) {
            for (int i = 0; i < binaryCount; i++)
                if (fileName.equalsIgnoreCase(getDownloadName(i)))
                    return i;
            return -1;
        }

        public byte[] getBinary(int index) {
            if (index >= binaries.length)
                throw new RuntimeException("Error while trying to get binary");
            return binaries[index];
        }
    }

    private static String hash(byte[] data, BigInteger modulus, BigInteger exponent) {
        byte[] hash = new byte[65];
        hash[0] = 10;
        System.arraycopy(Whirlpool.getHash(data, 0, data.length), 0, hash, 1, 64);
        hash = new BigInteger(hash).modPow(exponent, modulus).toByteArray();
        return Base64.getEncoder().encodeToString(hash).replaceAll("\\+", "\\*").replaceAll("/", "\\-").replaceAll("=", "");
    }

    private static long CRC(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        return crc.getValue();
    }

}
