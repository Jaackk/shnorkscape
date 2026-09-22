package com.rs.game;

public final class ForceTalk {

	private final String text;
	private final boolean publicChat;

	public ForceTalk(String text) {
		this(text, false);
	}

	public ForceTalk(String text, boolean publicChat) {
		this.text = text;
		this.publicChat = publicChat;
	}

	public boolean isPublicChat() { return publicChat; }

	public String getText() {
		return text;
	}
}
