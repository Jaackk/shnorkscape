package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.dungeoneering.Dungeoneering;
import com.rs.game.player.content.dungeoneering.journals.Chronicles;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;

public class DungeoneeringChroniclesD extends Dialogue {

	private Chronicles chronicle;
	private int page;
	@Override
	public void start() {
		chronicle = (Chronicles) parameters[0];
		sendPage(page);
	}
	
	private void sendPage(int page) {
		if (chronicle == null)
			return;
		String[] pages = chronicle.getPages();
		if (page >= pages.length)
			return;
		ArrayList<String> lines = Dungeoneering.generateLines(pages[page], 18);
		player.getPackets().sendIComponentText(959, 52, "Page " + (page * 2 + 1)  + "/" + chronicle.getPages().length * 2);
		player.getPackets().sendIComponentText(959, 53, "Page " + (page * 2 + 2) + "/" + chronicle.getPages().length * 2);
		if (page == 0)
			player.getPackets().sendHideIComponent(959, 28, true);
		if (page == chronicle.getPages().length - 1)
			player.getPackets().sendHideIComponent(959, 29, true);
		player.getInterfaceManager().sendInterface(959);
		for (int i = 30; i < 52; i++)
			player.getPackets().sendIComponentText(959, i, "");
		player.getPackets().sendIComponentText(959, 5, chronicle.getTitle());
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
