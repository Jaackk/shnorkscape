package com.rs.cache.loaders;



/* Class448 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.rs.network.io.InputStream;

public class ObjectData_1 {
	public ObjectData_2 aClass455_4928;
	public Vector3 aClass446_4929;
	Vector3 aClass446_4930;

	public ObjectData_1(ObjectData_1 class448_0_) {
		this();
		method5478(class448_0_);
	}

	public ObjectData_1() {
		aClass455_4928 = new ObjectData_2();
		aClass446_4929 = new Vector3();
		aClass446_4930 = new Vector3(1.0F, 1.0F, 1.0F);
	}

	void method5472(InputStream class564_sub29, boolean bool) {
		if (bool)
			method5473(class564_sub29);
		else {
			aClass455_4928 = new ObjectData_2(class564_sub29);
			aClass446_4929 = new Vector3(class564_sub29);
			aClass446_4930 = new Vector3(class564_sub29);
		}
	}

	void method5473(InputStream class564_sub29) {
		int i = class564_sub29.readUnsignedByte();
		float f = 0.0F;
		float f_1_ = 0.0F;
		float f_2_ = 0.0F;
		float f_3_ = 1.0F;
		if ((i & 0x1) != 0) {
			f = (float) class564_sub29.readShort() / 32768.0F;
			f_1_ = (float) class564_sub29.readShort() / 32768.0F;
			f_2_ = (float) class564_sub29.readShort() / 32768.0F;
			f_3_ = (float) class564_sub29.readShort() / 32768.0F;
		}
		aClass455_4928 = new ObjectData_2(f, f_1_, f_2_, f_3_);
		float f_4_ = 0.0F;
		float f_5_ = 0.0F;
		float f_6_ = 0.0F;
		if ((i & 0x2) != 0)
			f_4_ = (float) class564_sub29.readShort();
		if ((i & 0x4) != 0)
			f_5_ = (float) class564_sub29.readShort();
		if ((i & 0x8) != 0)
			f_6_ = (float) class564_sub29.readShort();
		aClass446_4929 = new Vector3(f_4_, f_5_, f_6_);
		float f_7_ = 1.0F;
		float f_8_ = 1.0F;
		float f_9_ = 1.0F;
		if ((i & 0x10) != 0) {
			float f_10_ = (float) class564_sub29.readShort() / 128.0F;
			f_7_ = f_8_ = f_9_ = f_10_;
		} else {
			if ((i & 0x20) != 0)
				f_7_ = (float) class564_sub29.readShort() / 128.0F;
			if ((i & 0x40) != 0)
				f_8_ = (float) class564_sub29.readShort() / 128.0F;
			if ((i & 0x80) != 0)
				f_9_ = (float) class564_sub29.readShort() / 128.0F;
		}
		aClass446_4930 = new Vector3(f_7_, f_8_, f_9_);
	}

	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ObjectData_1) {
			ObjectData_1 class448_11_ = (ObjectData_1) object;
			return (aClass455_4928.equals(class448_11_.aClass455_4928)
					&& aClass446_4929.method5422(class448_11_.aClass446_4929)
					&& (aClass446_4930.method5422(class448_11_.aClass446_4930)));
		}
		return false;
	}

	public final void method5474(ObjectData_1 class448_12_) {
		aClass455_4928.method5610(class448_12_.aClass455_4928);
		aClass446_4929.method5458(class448_12_.aClass455_4928);
		aClass446_4929.method5436(class448_12_.aClass446_4929);
		aClass446_4930.method5434(class448_12_.aClass446_4930);
	}

	public String toString() {
		return new StringBuilder().append("[").append(aClass455_4928.toString()).append("|")
				.append(aClass446_4929.toString()).append("|").append(aClass446_4930.toString()).append("]").toString();
	}

	public ObjectData_1(InputStream class564_sub29, boolean bool) {
		method5472(class564_sub29, bool);
	}

	public String method5475() {
		return new StringBuilder().append("[").append(aClass455_4928.toString()).append("|")
				.append(aClass446_4929.toString()).append("|").append(aClass446_4930.toString()).append("]").toString();
	}

	public String method5476() {
		return new StringBuilder().append("[").append(aClass455_4928.toString()).append("|")
				.append(aClass446_4929.toString()).append("|").append(aClass446_4930.toString()).append("]").toString();
	}

	public String method5477() {
		return new StringBuilder().append("[").append(aClass455_4928.toString()).append("|")
				.append(aClass446_4929.toString()).append("|").append(aClass446_4930.toString()).append("]").toString();
	}

	public void method5478(ObjectData_1 class448_13_) {
		aClass455_4928.method5599(class448_13_.aClass455_4928);
		aClass446_4929.method5426(class448_13_.aClass446_4929);
		aClass446_4930.method5426(class448_13_.aClass446_4930);
	}

	void method5479(InputStream class564_sub29) {
		int i = class564_sub29.readUnsignedByte();
		float f = 0.0F;
		float f_14_ = 0.0F;
		float f_15_ = 0.0F;
		float f_16_ = 1.0F;
		if ((i & 0x1) != 0) {
			f = (float) class564_sub29.readShort() / 32768.0F;
			f_14_ = (float) class564_sub29.readShort() / 32768.0F;
			f_15_ = (float) class564_sub29.readShort() / 32768.0F;
			f_16_ = (float) class564_sub29.readShort() / 32768.0F;
		}
		aClass455_4928 = new ObjectData_2(f, f_14_, f_15_, f_16_);
		float f_17_ = 0.0F;
		float f_18_ = 0.0F;
		float f_19_ = 0.0F;
		if ((i & 0x2) != 0)
			f_17_ = (float) class564_sub29.readShort();
		if ((i & 0x4) != 0)
			f_18_ = (float) class564_sub29.readShort();
		if ((i & 0x8) != 0)
			f_19_ = (float) class564_sub29.readShort();
		aClass446_4929 = new Vector3(f_17_, f_18_, f_19_);
		float f_20_ = 1.0F;
		float f_21_ = 1.0F;
		float f_22_ = 1.0F;
		if ((i & 0x10) != 0) {
			float f_23_ = (float) class564_sub29.readShort() / 128.0F;
			f_20_ = f_21_ = f_22_ = f_23_;
		} else {
			if ((i & 0x20) != 0)
				f_20_ = (float) class564_sub29.readShort() / 128.0F;
			if ((i & 0x40) != 0)
				f_21_ = (float) class564_sub29.readShort() / 128.0F;
			if ((i & 0x80) != 0)
				f_22_ = (float) class564_sub29.readShort() / 128.0F;
		}
		aClass446_4930 = new Vector3(f_20_, f_21_, f_22_);
	}

	public boolean method5483(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ObjectData_1) {
			ObjectData_1 class448_34_ = (ObjectData_1) object;
			return (aClass455_4928.equals(class448_34_.aClass455_4928)
					&& aClass446_4929.method5422(class448_34_.aClass446_4929)
					&& (aClass446_4930.method5422(class448_34_.aClass446_4930)));
		}
		return false;
	}
}
