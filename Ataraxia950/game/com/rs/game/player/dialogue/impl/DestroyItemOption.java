package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.dialogue.Dialogue;

public class DestroyItemOption extends Dialogue {

	int slotId;
	Item item;

	@Override
	public void start() {
		slotId = (Integer) parameters[0];
		item = (Item) parameters[1];
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 4, item.getName());
		player.getPackets().sendItemOnIComponent(1183, 8, item.getId(), 1);
		player.getPackets().sendIComponentText(1183, 1, "Are you sure you want to destroy this item?");
        player.getPackets().sendHideIComponent(1183, 7, player.getInventory().getAmountOf(item) == 1);
        
		if (item.getId() == Ectoplasmator.ECTOPLASMATOR_ID)
			player.getPackets().sendIComponentText(1183, 1, "You can obtain it again by killing ghostly creatures.");
		else if(item.getId() == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID)
			player.getPackets().sendIComponentText(1183, 1, "You can obtain it again by using 100 ghostly essence on an ectoplasmator."
					+ "<br>Note: All charges will be lost.");
        else if (!player.getDungeoneeringManager().getBindedItems().contains(item) && player.getDungeoneeringManager().getBindedAmmo() != null &&  player.getDungeoneeringManager().getBindedAmmo().getId() != item.getId())
		//if (!player.getDungeoneeringBinds().containsItem(item))
			player.getPackets().sendIComponentText(1183, 1, "You can re-claim this item from the place you got it.");
		else
			player.getPackets().sendIComponentText(1183, 1, "You can re-claim this item from the place you got it."
					+ "<br>Note: Destroying this item will also unbind it.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 1183 && (componentId == 5 || componentId == 7)) {
		    if (componentId == 7)
		        player.getInventory().deleteItem(new Item(item.getId(), player.getInventory().getAmountOf(item), item.getCharges()).setAttributes(item.getAttributes()));
		    else
		        player.getInventory().deleteItem(slotId, new Item(item.getId(), 1, item.getCharges()).setAttributes(item.getAttributes()));
//			player.getCharges().degradeCompletely(item);
//			player.getPackets().sendSound(4500, 0, 1);
			if(item.getId() == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID)
				player.ectoCharges = 0;
			if (player.getDungeoneeringManager().getBindedItems().contains(item))
                player.getDungeoneeringManager().unbind(item);
            else if (player.getDungeoneeringManager().getBindedAmmo() != null && player.getDungeoneeringManager().getBindedAmmo().getId() == item.getId())
                player.getDungeoneeringManager().unbind(item);

			//if (player.getDungeoneeringBinds().containsItem(item))
				//player.getDungeoneeringBinds().unbindItem(item);
			player.getInterfaceManager().closeChatBoxInterface();
			end();
			return;
		}
		player.getInterfaceManager().closeChatBoxInterface();
		end();
	}

	@Override
	public void finish() {
	}
}