package com.rs.cache.loaders;


import com.rs.network.io.InputStream;

public class ObjectData {
	public int rotation;
	public int type;
	public ObjectData_1 aClass448_8204;

	public ObjectData(InputStream class564_sub29) {
		this(class564_sub29, false);
	}

	ObjectData(InputStream class564_sub29, boolean bool) {
		this(class564_sub29, bool, true);
	}

	public ObjectData(InputStream class564_sub29, int i, boolean bool) {
		this(class564_sub29, i, bool, true);
	}

	ObjectData(InputStream class564_sub29, boolean bool, boolean bool_0_) {
		this(class564_sub29, class564_sub29.readUnsignedByte(), bool, bool_0_);
	}

	ObjectData(InputStream buffer, int objectData, boolean bool, boolean bool_1_) {
		boolean bool_2_ = 0 != (objectData & 0x80);
		if (!bool) {
			type = (objectData >> 2 & 0x1f);
			rotation = (objectData & 0x3);
		} else {
			int i_3_ = buffer.readUnsignedByte();
			type = (objectData & 0x7f);
			rotation = i_3_;
		}
		aClass448_8204 = null;
		if (bool_2_)
			aClass448_8204 = new ObjectData_1(buffer, bool_1_);
	}

}
