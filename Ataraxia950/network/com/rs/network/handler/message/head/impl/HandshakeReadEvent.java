package com.rs.network.handler.message.head.impl;

import com.rs.network.handler.message.head.ReadMessageEvent;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class HandshakeReadEvent extends ReadMessageEvent {

	private final int revision;
	private final int update;
	private final String token;
	
	public HandshakeReadEvent(int revision, int update, String token) {
		this.revision = revision;
		this.update = update;
		this.token = token;
	}
	
    public HandshakeReadEvent() {
        this(-1, -1, null);
    }
    
	public int getRevision() {
		return revision;
	}

	public int getUpdate() {
		return update;
	}

	public String getToken() {
		return token;
	}

}
