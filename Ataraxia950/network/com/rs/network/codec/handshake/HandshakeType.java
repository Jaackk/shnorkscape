package com.rs.network.codec.handshake;

import com.google.common.collect.ImmutableMap;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public enum HandshakeType {

	// login
	LOGIN_CONNECTION,
	// js5
	UPDATE_CONNECTION_REGULAR, 
	// js5_web
	UPDATE_CONNECTION_WEB;

	public static final ImmutableMap<Integer, HandshakeType> TYPES = ImmutableMap.of(
			// login == 14
			14, HandshakeType.LOGIN_CONNECTION,
			// js5 == 15
			15, HandshakeType.UPDATE_CONNECTION_REGULAR,
			
			71, HandshakeType.UPDATE_CONNECTION_WEB);

}
