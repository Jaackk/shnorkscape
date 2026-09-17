package com.rs.game.player.content;

public class ChatMessage {

	private final String message;
	private String filteredMessage;

	public ChatMessage(String message) {
		this.message = message;
	}

	public String getMessage(boolean filtered) {
		return filtered ? filteredMessage : message;
	}
}
