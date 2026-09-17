package com.rs.network.handler.message.tail;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 31, 2018.
 */
public class WriteMessageEvent {

	protected boolean terminated;

	public void terminate() {
		terminated = true;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean fired) {
		this.terminated = fired;
	}

}
