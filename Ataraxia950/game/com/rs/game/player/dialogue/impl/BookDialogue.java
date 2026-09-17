package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.Book;
import com.rs.game.player.content.dungeoneering.Dungeoneering;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;

public class BookDialogue extends Dialogue {

	private Book book;
	private int page;
	@Override
	public void start() {
		book = (Book) parameters[0];
		sendPage(page);
	}
	
	private void sendPage(int page) {
		if (book == null)
			return;
		String[] pages = book.getPages();
		if (page >= pages.length)
			return;
		ArrayList<String> lines = Dungeoneering.generateLines(pages[page], 18);
		player.getPackets().sendIComponentText(959, 52, "Page " + (page * 2 + 1)  + "/" + book.getPages().length * 2);
		player.getPackets().sendIComponentText(959, 53, "Page " + (page * 2 + 2) + "/" + book.getPages().length * 2);
		if (page == 0)
			player.getPackets().sendHideIComponent(959, 28, true);
		if (page == book.getPages().length - 1)
			player.getPackets().sendHideIComponent(959, 29, true);
		player.getInterfaceManager().sendInterface(959);
		for (int i = 30; i < 52; i++)
			player.getPackets().sendIComponentText(959, i, "");
		player.getPackets().sendIComponentText(959, 5, book.getTitle());
		for (int i = 30; i < 52; i++) {
			if (lines.size() < i - 29)
				break;
			player.getPackets().sendIComponentText(959, i, (i - 29) > lines.size() ? "" : lines.get(i - 30) == null ? "" : lines.get(i - 30));
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == 28)
			page--;
		else
			page++;
		sendPage(page);
	}

	@Override
	public void finish() {
		
	}

}
