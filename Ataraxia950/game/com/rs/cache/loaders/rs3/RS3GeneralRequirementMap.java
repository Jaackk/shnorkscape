package com.rs.cache.loaders.rs3;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;



public final class RS3GeneralRequirementMap {

	private HashMap<Long, Object> values;
	private int id;

	private static final ConcurrentHashMap<Integer, RS3GeneralRequirementMap> maps = new ConcurrentHashMap<Integer, RS3GeneralRequirementMap>();

	public static void main(String[] args) throws IOException {
	    
	}

	public static final RS3GeneralRequirementMap getMap(int scriptId) {
		RS3GeneralRequirementMap script = maps.get(scriptId);
		if (script != null)
			return script;

		script = new RS3GeneralRequirementMap();
		script.id = scriptId;

		com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[22];
		byte[] data = null;

		// Safely check if Index 22 exists and grab the file
		if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
			try {
				data = index.getFile(scriptId / 32, scriptId & 31);
			} catch (Exception e) {
				data = null;
			}
		}

		// Only attempt to read if we actually got data
		if (data != null) {
			script.readValueLoop(new InputStream(data));
		} else {
			// We silently return the empty script object.
			// We won't log this one, because CosmeticsManager asks for hundreds of these
			// and it would spam your console instantly.
		}

		maps.put(scriptId, script);
		return script;
	}

	public HashMap<Long, Object> getValues() {
		return values;
	}

	public Object getValue(long key) {
		if (values == null)
			return null;
		return values.get(key);
	}

	public long getKeyForValue(Object value) {
		for (Long key : values.keySet()) {
			if (values.get(key).equals(value))
				return key;
		}
		return -1;
	}

	public int getSize() {
		if (values == null)
			return 0;
		return values.size();
	}

	public int getIntValue(long key) {
		if (values == null)
			return 0;
		Object value = values.get(key);
		if (value == null || !(value instanceof Integer))
			return 0;
		return (Integer) value;
	}

	public String getStringValue(long key) {
		if (values == null)
			return "";
		Object value = values.get(key);
		if (value == null || !(value instanceof String))
			return "";
		return (String) value;
	}

	private void readValueLoop(InputStream stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(stream, opcode);
		}
	}

	private void readValues(InputStream stream, int opcode) {
		if (opcode == 249) {
			int length = stream.readUnsignedByte();
			if (values == null)
				values = new HashMap<Long, Object>(length);
			for (int index = 0; index < length; index++) {
				boolean stringInstance = stream.readUnsignedByte() == 1;
				long key = stream.read24BitInt();
				Object value = stringInstance ? stream.readString() : stream.readInt();
				values.put(key, value);
			}
		}
	}

	public int getId() {
		return id;
	}

	private RS3GeneralRequirementMap() {

	}


}