package com.rs.network.handler.message.tail.impl;

import com.rs.game.player.Player;
import com.rs.network.codec.ResultMessage;
import com.rs.network.handler.message.tail.WriteMessageEvent;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class LoginWriteEvent extends WriteMessageEvent {

	private final Player player;
	private final ResultMessage result;

	public LoginWriteEvent(ResultMessage result) {
		this(null, result);
	}

	public LoginWriteEvent(Player player, ResultMessage result) {
		this.player = player;
		this.result = result;
	}

	public Player getPlayer() {
		return player;
	}

	public ResultMessage getResult() {
		return result;
	}

}
