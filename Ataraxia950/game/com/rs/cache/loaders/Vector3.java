package com.rs.cache.loaders;



/* Class217 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.rs.network.io.InputStream;

public class Vector3 {
	public float x;
	static int anInt2452;
	static int anInt2453;
	public float z;
	public float y;
	static Vector3[] aClass217Array2456;

	static {
		new Vector3(0.0F, 0.0F, 0.0F);
		aClass217Array2456 = new Vector3[0];
	}

	Vector3(InputStream class564_sub29) {
		x = class564_sub29.readFloat();
		y = class564_sub29.readFloat();
		z = class564_sub29.readFloat();
	}

	public static Vector3 method2004(float f, float f_0_, float f_1_) {
		synchronized (aClass217Array2456) {
			if (anInt2453 == 0) {
				Vector3 class217 = new Vector3(f, f_0_, f_1_);
				return class217;
			}
			aClass217Array2456[--anInt2453].method2007(f, f_0_, f_1_);
			Vector3 class217 = aClass217Array2456[anInt2453];
			return class217;
		}
	}

	public static Vector3 copyOf(Vector3 class217) {
		synchronized (aClass217Array2456) {
			if (anInt2453 == 0) {
				Vector3 class217_2_ = new Vector3(class217);
				return class217_2_;
			}
			aClass217Array2456[--anInt2453].method2013(class217);
			Vector3 class217_3_ = aClass217Array2456[anInt2453];
			return class217_3_;
		}
	}

	public void cache() {
		synchronized (aClass217Array2456) {
			if (anInt2453 < anInt2452 - 1)
				aClass217Array2456[anInt2453++] = this;
		}
	}

	public Vector3(float f, float f_4_, float f_5_) {
		x = f;
		y = f_4_;
		z = f_5_;
	}

	public void method2007(float f, float f_6_, float f_7_) {
		x = f;
		y = f_6_;
		z = f_7_;
	}

	final void method2008() {
		x = -x;
		y = -y;
		z = -z;
	}

	final void method2009(Vector3 class217_8_) {
		x += class217_8_.x;
		y += class217_8_.y;
		z += class217_8_.z;
	}

	public final void method2010(float f, float f_9_, float f_10_) {
		x -= f;
		y -= f_9_;
		z -= f_10_;
	}

	public static final Vector3 method2011(Vector3 class217, Vector3 class217_11_) {
		Vector3 class217_12_ = copyOf(class217);
		class217_12_.method2015(class217_11_);
		return class217_12_;
	}

	public final float method2012() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public Vector3() {
		/* empty */
	}

	Vector3(Vector3 class217_13_) {
		x = class217_13_.x;
		y = class217_13_.y;
		z = class217_13_.z;
	}

	public void method2013(Vector3 class217_14_) {
		method2007(class217_14_.x, class217_14_.y, class217_14_.z);
	}

	public String toString() {
		return new StringBuilder().append(x).append(", ").append(y).append(", ").append(z).toString();
	}

	final void method2015(Vector3 class217_18_) {
		x -= class217_18_.x;
		y -= class217_18_.y;
		z -= class217_18_.z;
	}

	public static void method2017(int i) {
		anInt2452 = i;
		aClass217Array2456 = new Vector3[i];
		anInt2453 = 0;
	}

	public final void method5458(ObjectData_2 class455) {
		ObjectData_2 class455_30_ = ObjectData_2.method5595(x, y, z, 0.0F);
		ObjectData_2 class455_31_ = ObjectData_2.method5604(class455);
		ObjectData_2 class455_32_ = ObjectData_2.method5611(class455_31_, class455_30_);
		class455_32_.method5610(class455);
		method5465(class455_32_.x, class455_32_.y, class455_32_.z);
		class455_30_.method5596();
		class455_31_.method5596();
		class455_32_.method5596();
	}

	public void method5465(float f, float f_37_, float f_38_) {
		x = f;
		y = f_37_;
		z = f_38_;
	}

	public final void method5436(Vector3 class446_22_) {
		x += class446_22_.x;
		y += class446_22_.y;
		z += class446_22_.z;
	}

	final void method5434(Vector3 class446_19_) {
		x *= class446_19_.x;
		y *= class446_19_.y;
		z *= class446_19_.z;
	}

	public boolean method5422(Vector3 class446_7_) {
		return x == class446_7_.x && y == class446_7_.y && z == class446_7_.z;
	}

	public void method5426(Vector3 class446_11_) {
		method5465(class446_11_.x, class446_11_.y, class446_11_.z);
	}

	public final void method5467() {
		x = -x;
		y = -y;
		z = -z;
	}

}
