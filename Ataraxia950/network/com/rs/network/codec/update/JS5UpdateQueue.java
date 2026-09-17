package com.rs.network.codec.update;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 *
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateQueue {

	private final FileDescriptor descriptor;
	private final JS5Priority priority;
	private final JS5Priority placementPriority;
	private int sent;

	public JS5UpdateQueue(FileDescriptor descriptor, JS5Priority placementPriority, JS5Priority priority, boolean web) {
		this.descriptor = descriptor;
		this.placementPriority = placementPriority;
		this.priority = priority;
	}

	public ByteBuf create(byte xor) {
		int type = descriptor.getType();
		int file = descriptor.getFile();
		byte[] archive = JS5UpdateHelper.get(type, file, false);
		if (archive == null) {
			return null;
		}

		int length = ((archive[1] & 0xff) << 24) + ((archive[2] & 0xff) << 16) + ((archive[3] & 0xff) << 8) + (archive[4] & 0xff) + 5;
		if (archive[0] != 0)
			length += 4;

		ByteBuf out = Unpooled.buffer(5 + length);

		out.writeByte(descriptor.getType());
		out.writeInt(descriptor.getFile() | (priority == JS5Priority.LOW_PRIORITY ? ~0x7fffffff : 0));

		for (int index = sent; index < length; index++) {
			out.writeByte(archive[index]);
			if (out.writerIndex() == 102400 || index == length - 1) {
				if (xor != 0) {
					for (int i = 0; i < out.writerIndex(); i++)
						out.setByte(i, out.getByte(i) ^ xor);
				}

				sent = (index + 1);
				return out;
			}//open your old client
		}
		return null;
	}

	public FileDescriptor getDescriptor() {
		return descriptor;
	}

	public JS5Priority getPriority() {
		return priority;
	}

	public int getSent() {
		return sent;
	}

	public void setSent(int sent) {
		this.sent = sent;
	}

	public JS5Priority getPlacementPriority() {
		return placementPriority;
	}

}
