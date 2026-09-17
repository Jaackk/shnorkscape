package com.rs.network.io;

import io.netty.buffer.ByteBuf;

public class ByteBufUtil {

	public static String getString(ByteBuf buf) {
		if (!buf.isReadable())
			throw new IllegalStateException("Buffer is not readable.");

		StringBuilder bldr = new StringBuilder();
		while (buf.isReadable()) {
			byte read = buf.readByte();
			if(read == 0) {
				break;
			}
			bldr.append((char) read);
		}
		return bldr.toString();
	}

	public static void putString(ByteBuf buf, String string) {
		for (char c : string.toCharArray()) {
			buf.writeByte(c);
		}
		buf.writeByte(0);
	}

	public static int get5Bytes(ByteBuf buf) {
		return ((buf.readByte() & 0xFF) << 32) | ((buf.readByte() & 0xFF) << 24) | ((buf.readByte() & 0xFF) << 16) | ((buf.readByte() & 0xFF) << 8) | (buf.readByte() & 0xFF);
	}

}
