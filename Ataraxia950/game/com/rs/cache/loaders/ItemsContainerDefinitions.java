package com.rs.cache.loaders;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.filestore.io.OutputStream;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.filestore.utils.Constants;
import com.rs.network.io.InputStream;

public class ItemsContainerDefinitions {
    private static final ConcurrentHashMap<Integer, ItemsContainerDefinitions> cachedItemsContainerDefinitions = new ConcurrentHashMap<Integer, ItemsContainerDefinitions>();

    public static final ItemsContainerDefinitions getItemsContainerDefinitions(int containerId) {
        try {
            ItemsContainerDefinitions defs = cachedItemsContainerDefinitions.get(containerId);
            if (defs != null)
                return defs;
            byte[] data = Cache.STORE.getIndexes()[2].getFile(5, containerId);
            defs = new ItemsContainerDefinitions();
            defs.id = containerId;
            if (data != null)
                defs.readValueLoop(new InputStream(data));
            cachedItemsContainerDefinitions.put(containerId, defs);
            return defs;
        } catch (Throwable t) {
            return null;
        }
    }

    public static void main(String[] args) throws Throwable {
        Cache.init();
        ItemsContainerDefinitions defs = getItemsContainerDefinitions(797);// bank container
        System.out.println(defs.length);
        for (int i = 0; i < Cache.STORE.getIndexes()[2].getValidFilesCount(5); i++) {
            defs = getItemsContainerDefinitions(i);
            if (defs.itemIds != null) {
                System.out.println("containerId=" + i + ", totalSize:" + defs.length);
                System.out.println(Arrays.toString(defs.itemIds));
                System.out.println(Arrays.toString(defs.amounts));
            }
        }
    }

    public int id;
    public int length = 0;
    public int filledLength = 0;
    public int[] itemIds;
    public int[] amounts;

    ItemsContainerDefinitions() {

    }

    void readValueLoop(InputStream var1) {
        while (true) {
            int opcode = var1.readUnsignedByte();
            if (opcode == 0)
                return;
            this.readValues(var1, opcode);
        }
    }

    void readValues(InputStream buffer, int opcode) {
        if (2 == opcode) {
            this.length = buffer.readUnsignedShort();
        } else if (4 == opcode) {
            this.filledLength = buffer.readUnsignedByte();
            this.itemIds = new int[this.filledLength];
            this.amounts = new int[this.filledLength];
            for (int var4 = 0; var4 < this.filledLength; ++var4) {
                this.itemIds[var4] = buffer.readUnsignedShort();
                this.amounts[var4] = buffer.readUnsignedShort();
            }
        }
    }

    public void write(Store store) {
        write(store, false);
    }

    public void write(Store store, boolean rewriteTable) {
        store.getIndexes()[2].putFile(5, id, Constants.GZIP_COMPRESSION, this.encode(), null, rewriteTable, true, -1, -1);
    }

    public byte[] encode() {
        OutputStream stream = new OutputStream();
        if (length != 0) {
            stream.writeByte(2);
            stream.writeShort(length);
        }
        if (filledLength != 0 && itemIds != null && amounts != null) {
            stream.writeByte(4);
            stream.writeByte(filledLength);
            for (int i = 0; i < filledLength; i++) {
                stream.writeShort(itemIds[i]);
                stream.writeShort(amounts[i]);
            }
        }
        // end
        stream.writeByte(0);
        byte[] data = new byte[stream.getOffset()];
        stream.setOffset(0);
        stream.getBytes(data, 0, data.length);
        return data;
    }
}
