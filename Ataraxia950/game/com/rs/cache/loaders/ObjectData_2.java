package com.rs.cache.loaders;



/* Class455 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.rs.network.io.InputStream;

public final class ObjectData_2 {
	static int anInt5008;
	public float x;
	static ObjectData_2[] aClass455Array5010 = new ObjectData_2[0];
	static int anInt5011;
	public float y;
	public float z;
	public float aFloat5014;

	public static ObjectData_2 method5594() {
		synchronized (aClass455Array5010) {
			if (anInt5011 == 0) {
				ObjectData_2 class455 = new ObjectData_2();
				return class455;
			}
			aClass455Array5010[--anInt5011].method5606();
			ObjectData_2 class455 = aClass455Array5010[anInt5011];
			return class455;
		}
	}

	static ObjectData_2 method5595(float f, float f_0_, float f_1_, float f_2_) {
		synchronized (aClass455Array5010) {
			if (anInt5011 == 0) {
				ObjectData_2 class455 = new ObjectData_2(f, f_0_, f_1_, f_2_);
				return class455;
			}
			aClass455Array5010[--anInt5011].method5598(f, f_0_, f_1_, f_2_);
			ObjectData_2 class455 = aClass455Array5010[anInt5011];
			return class455;
		}
	}

	public void method5596() {
		synchronized (aClass455Array5010) {
			if (anInt5011 < anInt5008 - 1)
				aClass455Array5010[anInt5011++] = this;
		}
	}

	public ObjectData_2() {
		method5606();
	}

	public ObjectData_2(float f, float f_3_, float f_4_) {
		method5621(f, f_3_, f_4_);
	}

	ObjectData_2(InputStream class564_sub29) {
		x = class564_sub29.readFloat();
		y = class564_sub29.readFloat();
		z = class564_sub29.readFloat();
		aFloat5014 = class564_sub29.readFloat();
	}

	public void method5597(InputStream class564_sub29) {
		x = class564_sub29.readFloat();
		y = class564_sub29.readFloat();
		z = class564_sub29.readFloat();
		aFloat5014 = class564_sub29.readFloat();
	}

	void method5598(float f, float f_5_, float f_6_, float f_7_) {
		x = f;
		y = f_5_;
		z = f_6_;
		aFloat5014 = f_7_;
	}

	public void method5599(ObjectData_2 class455_8_) {
		x = class455_8_.x;
		y = class455_8_.y;
		z = class455_8_.z;
		aFloat5014 = class455_8_.aFloat5014;
	}

	public void method5600(Vector3 class446, float f) {
		method5601(class446.x, class446.y, class446.z, f);
	}

	public void method5601(float f, float f_9_, float f_10_, float f_11_) {
		float f_12_ = (float) Math.sin(f_11_ * 0.5F);
		float f_13_ = (float) Math.cos(f_11_ * 0.5F);
		x = f * f_12_;
		y = f_9_ * f_12_;
		z = f_10_ * f_12_;
		aFloat5014 = f_13_;
	}

	public final void method5602(ObjectData_2 class455_14_, float f) {
		if (method5607(class455_14_) < 0.0F)
			method5603();
		method5612(1.0F - f);
		ObjectData_2 class455_15_ = method5615(class455_14_, f);
		method5608(class455_15_);
		class455_15_.method5596();
		method5605();
	}

	final void method5603() {
		x = -x;
		y = -y;
		z = -z;
		aFloat5014 = -aFloat5014;
	}

	public static final ObjectData_2 method5604(ObjectData_2 class455) {
		ObjectData_2 class455_16_ = method5629(class455);
		class455_16_.method5618();
		return class455_16_;
	}

	public final void method5605() {
		float f = 1.0F / method5609(this);
		x *= f;
		y *= f;
		z *= f;
		aFloat5014 *= f;
	}

	final void method5606() {
		z = 0.0F;
		y = 0.0F;
		x = 0.0F;
		aFloat5014 = 1.0F;
	}

	final float method5607(ObjectData_2 class455_17_) {
		return (x * class455_17_.x + y * class455_17_.y + z * class455_17_.z + aFloat5014 * class455_17_.aFloat5014);
	}

	final void method5608(ObjectData_2 class455_18_) {
		x += class455_18_.x;
		y += class455_18_.y;
		z += class455_18_.z;
		aFloat5014 += class455_18_.aFloat5014;
	}

	static final float method5609(ObjectData_2 class455) {
		return (float) Math.sqrt(method5617(class455, class455));
	}

	public final void method5610(ObjectData_2 class455_19_) {
		method5598(
				(class455_19_.aFloat5014 * x + class455_19_.x * aFloat5014 + class455_19_.y * z - class455_19_.z * y),
				(class455_19_.aFloat5014 * y - class455_19_.x * z + class455_19_.y * aFloat5014 + class455_19_.z * x),
				(class455_19_.aFloat5014 * z + class455_19_.x * y - class455_19_.y * x + class455_19_.z * aFloat5014),
				(class455_19_.aFloat5014 * aFloat5014 - class455_19_.x * x - class455_19_.y * y - class455_19_.z * z));
	}

	static final ObjectData_2 method5611(ObjectData_2 class455, ObjectData_2 class455_20_) {
		ObjectData_2 class455_21_ = method5629(class455);
		class455_21_.method5610(class455_20_);
		return class455_21_;
	}

	final void method5612(float f) {
		x *= f;
		y *= f;
		z *= f;
		aFloat5014 *= f;
	}

	public String method5613() {
		return new StringBuilder().append(x).append(",").append(y).append(",").append(z).append(",").append(aFloat5014)
				.toString();
	}

	public final void method5614() {
		float f = 1.0F / method5609(this);
		x *= f;
		y *= f;
		z *= f;
		aFloat5014 *= f;
	}

	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ObjectData_2) {
			ObjectData_2 class455_22_ = (ObjectData_2) object;
			return (x == class455_22_.x && y == class455_22_.y && z == class455_22_.z
					&& aFloat5014 == class455_22_.aFloat5014);
		}
		return false;
	}

	public String toString() {
		return new StringBuilder().append(x).append(",").append(y).append(",").append(z).append(",").append(aFloat5014)
				.toString();
	}

	static final ObjectData_2 method5615(ObjectData_2 class455, float f) {
		ObjectData_2 class455_23_ = method5629(class455);
		class455_23_.method5612(f);
		return class455_23_;
	}

	public String method5616() {
		return new StringBuilder().append(x).append(",").append(y).append(",").append(z).append(",").append(aFloat5014)
				.toString();
	}

	static final float method5617(ObjectData_2 class455, ObjectData_2 class455_24_) {
		return class455.method5607(class455_24_);
	}

	public final void method5618() {
		x = -x;
		y = -y;
		z = -z;
	}

	public boolean method5619(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ObjectData_2) {
			ObjectData_2 class455_25_ = (ObjectData_2) object;
			return (x == class455_25_.x && y == class455_25_.y && z == class455_25_.z
					&& aFloat5014 == class455_25_.aFloat5014);
		}
		return false;
	}

	public static void method5620(int i) {
		anInt5008 = i;
		aClass455Array5010 = new ObjectData_2[i];
		anInt5011 = 0;
	}

	public ObjectData_2(ObjectData_2 class455_26_) {
		method5599(class455_26_);
	}

	public ObjectData_2(float f, float f_27_, float f_28_, float f_29_) {
		method5598(f, f_27_, f_28_, f_29_);
	}

	public void method5621(float f, float f_30_, float f_31_) {
		method5601(0.0F, 1.0F, 0.0F, f);
		ObjectData_2 class455_32_ = method5594();
		class455_32_.method5601(1.0F, 0.0F, 0.0F, f_30_);
		method5610(class455_32_);
		class455_32_.method5601(0.0F, 0.0F, 1.0F, f_31_);
		method5610(class455_32_);
		class455_32_.method5596();
	}

	public static void method5622(int i) {
		anInt5008 = i;
		aClass455Array5010 = new ObjectData_2[i];
		anInt5011 = 0;
	}

	public final void method5623() {
		x = -x;
		y = -y;
		z = -z;
	}

	final void method5624() {
		z = 0.0F;
		y = 0.0F;
		x = 0.0F;
		aFloat5014 = 1.0F;
	}

	final void method5625() {
		z = 0.0F;
		y = 0.0F;
		x = 0.0F;
		aFloat5014 = 1.0F;
	}

	final void method5626() {
		x = -x;
		y = -y;
		z = -z;
		aFloat5014 = -aFloat5014;
	}

	static {
		new ObjectData_2();
	}

	public final void method5627() {
		x = -x;
		y = -y;
		z = -z;
	}

	public final void method5628() {
		x = -x;
		y = -y;
		z = -z;
	}

	public static ObjectData_2 method5629(ObjectData_2 class455) {
		synchronized (aClass455Array5010) {
			if (anInt5011 == 0) {
				ObjectData_2 class455_33_ = new ObjectData_2(class455);
				return class455_33_;
			}
			aClass455Array5010[--anInt5011].method5599(class455);
			ObjectData_2 class455_34_ = aClass455Array5010[anInt5011];
			return class455_34_;
		}
	}

	public final void method5630() {
		float f = 1.0F / method5609(this);
		x *= f;
		y *= f;
		z *= f;
		aFloat5014 *= f;
	}

	final void method5631() {
		z = 0.0F;
		y = 0.0F;
		x = 0.0F;
		aFloat5014 = 1.0F;
	}

	public String method5632() {
		return new StringBuilder().append(x).append(",").append(y).append(",").append(z).append(",").append(aFloat5014)
				.toString();
	}
}
