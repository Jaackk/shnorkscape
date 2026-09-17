package com.rs.game.player.content.packs.protean;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.map.bossInstance.impl.pz.InstanceType;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 2. okt 2018 : 06:44:31
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class ProteanPackOpenD extends Dialogue {

	private int pageIndex;
	private final int len = ProteanReward.VALUES.length <= 5 ? 5 : 4;
	private final List<String> pages = new ArrayList<String>(5);
	private ProteanPack pack;

	private void sendOptions() {
		pages.clear();
		for (int i = pageIndex * 4; i < ((pageIndex * 4) + len); i++) {
			if (i >= ProteanReward.VALUES.length) {
				continue;
			}
			pages.add(ProteanReward.VALUES[i].toString());
		}
		if (pages.isEmpty()) {
			pageIndex = 0;
			sendOptions();
			return;
		}
		if (len == 4) {
			pages.add("More options...");
		}
		sendOptionsDialogue("Which protean item would you like?", pages.toArray(new String[pages.size()]));
	}
	
	@Override
	public void start() {
		if (parameters == null || parameters.length == 0) {
			return;
		}
		val obj = parameters[0];
		if (!(obj instanceof ProteanPack)) {
			return;
		}
		pack = (ProteanPack) obj;
		sendOptions();
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		val optionIndex = getOrdinal(componentId);
		if (len == 4 && optionIndex == pages.size() - 1) {
			pageIndex++;
			if ((pageIndex * 4) >= InstanceType.VALUES.length) {
				pageIndex = 0;
			}
			sendOptions();
		} else {
			end();
			val type = ProteanReward.VALUES[(pageIndex * 4) + optionIndex];
			if (!player.getInventory().containsItem(pack.getId(), 1)) {
				return;
			}
			player.sendMessage("You open the " + (ItemDefinitions.getItemDefinitions(pack.getId()).getName()) + " and receive " + pack.getAmount() + " x " + (ItemDefinitions.getItemDefinitions(type.getId()).getName()) + ".");
			player.getInventory().deleteItem(pack.getId(), 1);
			player.getInventory().addItem(type.getId(), pack.getAmount());
		}
	}

	@Override
	public void finish() {
		
	}

}
