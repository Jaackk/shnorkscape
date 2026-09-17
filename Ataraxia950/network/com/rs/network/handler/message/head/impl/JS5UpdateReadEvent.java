package com.rs.network.handler.message.head.impl;

import com.rs.network.codec.update.FileDescriptor;
import com.rs.network.codec.update.JS5Priority;
import com.rs.network.handler.message.head.ReadMessageEvent;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 1, 2018.
 */
public class JS5UpdateReadEvent extends ReadMessageEvent {

	private final FileDescriptor desc;
	private final JS5Priority placement;
	private final JS5Priority priority;

	public JS5UpdateReadEvent(FileDescriptor desc, JS5Priority placement, JS5Priority priority) {
		this.desc = desc;
		this.placement = placement;
		this.priority = priority;
	}

	public FileDescriptor getDescriptor() {
		return desc;
	}

	public JS5Priority getPlacement() {
		return placement;
	}

	public JS5Priority getPriority() {
		return priority;
	}

}
