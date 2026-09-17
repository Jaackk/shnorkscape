package com.rs.game.player.content.easter;

import com.rs.game.activites.EasterEvent;
import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class EasterDialogue extends Dialogue {

	private int pointsRequired;
	private int itemId;
	
	@Override
	public void start() {
		if(!player.startedEasterEvent) {
			String Gender = player.getAppearence().isMale() ? "sir" : "madam";
			sendNPCDialogue(13651, SAD, "Help me kind " + Gender + "! I'm losing my friend.");
			stage = 0;
		} else {
			sendNPCDialogue(13651, SAD, "Welcome back dear friend!");
			stage = 13;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
			case 0:
				sendPlayerDialogue(CALM, "What's wrong?");
				stage = 1;
				break;
			case 1:
				sendNPCDialogue(13651, SAD, "My Easter bunny friend and I were preparing for the essentials of this year's Easter celebration, but suddenly, he got sick.");
				stage = 2;
				break;
			case 2:
				sendNPCDialogue(13651, SAD, "It's getting worse day by day. I don't think he will make it by the end of the easter *cries*");
				stage = 3;
				break;
			case 3:
				sendPlayerDialogue(CALM, "Is there anything I can do to help?");
				stage = 4;
				break;
			case 4:
				sendNPCDialogue(13651, SAD, "I'm glad you asked.. Well, it may seem weird but my friend sustains from easter eggs.");
				stage = 5;
				break;
			case 5:
				sendNPCDialogue(13651, SAD, "But it's not just any type of easter egg. It's a special egg that is produced by Ralf the Easter Bunny.");
				stage = 6;
				break;
			case 6:
				sendPlayerDialogue(CALM, "Sounds pretty easy. So.. where can I find Ralf the Easter Bunny?");
				stage = 7;
				break;
			case 7:
				sendNPCDialogue(13651, SAD, "Well here's the catch... Ralf the Easter Bunny is a really active bunny that wanders around Gielinor.");
				stage = 8;
				break;
			case 8:
				sendNPCDialogue(13651, SAD, "He doesn't like staying in one place so he usually travels to different parts of Gielinor every 10 minutes.");
				stage = 9;
				break;
			case 9:
				sendNPCDialogue(13651, SAD, "I believe he was last seen walking around " + EasterEvent.getDroppedArea() + ".");
				stage = 10;
			case 10:
				sendNPCDialogue(13651, SAD, "Anyways... Once you find the eggs, you must use them on the cannon and fire them towards my friend to heal him.");
				stage = 11;
				break;
			case 11:
				sendNPCDialogue(13651, SAD, "If you have any questions feel free to return to me. We have to save my friend or Easter will be ruined!");
				stage = 12;
				break;
			case 12:
				player.startedEasterEvent = true;
				stage = -1;
				finish();
				break;
			case 13:
				sendPlayerDialogue(CALM, "Hello, I've had some questions which I'd like to ask.");
				stage = 14;
				break;
			case 14:
				sendNPCDialogue(13651, SAD, "Sure, go ahead!");
				stage = 15;
				break;
			case 15:
				sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "What can I do with Easter Eggs?", "Did you see Ralf the Easter Bunny?", "Easter Rewards", "Easter Titles","Nevermind");
				stage = 16;
				break;
			case 16:
				switch(componentId) {
					case OPTION_1:
						sendNPCDialogue(13651, SAD, "Once you find the eggs, you must use them on the cannon and fire them towards my friend to heal him.");
						stage = 18;
						break;
						
					case OPTION_2:
						sendNPCDialogue(13651, SAD, "I believe he was last seen walking around " + EasterEvent.getDroppedArea() + ".");
						stage = 17;
						break;
						
					case OPTION_3:
						sendOptionsDialogue("Easter Points: " + Colors.GREEN + player.easterPoints, "Bunny Ears " + Colors.GREEN + "(5000 Points)",
								"Rubber Chicken " + Colors.GREEN + "(750 Points)", "Easter Carrot " + Colors.GREEN + "(750 Points)", "Chicken Head " + Colors.GREEN + "(200 Points)", "More Options");
						
						stage = 20;
						break;
						
					case OPTION_4:
						sendNPCDialogue(13651, SAD, "You can claim 6 different easter titles by finding ralf the easter bunny and feeding him an easter egg. There are 6 types of eggs that give different titles. Have fun searching!");
						stage = 17;
						break;
						
					case OPTION_5:
						finish();
						break;
				}
				break;
			case 17:
				finish();
				break;
				
			case 18:
				sendNPCDialogue(13651, SAD, "It will take several eggs before he fully recovers so you and your friends have to unite and work together in order to save him.");
				stage = 19;
				break;
			case 19:
				sendNPCDialogue(13651, SAD, "For each egg you fire, you will receive a random amount of points which you can later exchange for several rewards here. However, our main priority is to save my friend before Easter ends!");
				stage = 17;
				break;
			case 20:
				switch(componentId) {
					case OPTION_1:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(1037, 5000);
						stage = 21;
						break;
					case OPTION_2:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(4566, 700);
						stage = 21;
						break;
					case OPTION_3:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(14728, 700);
						stage = 21;
						break;
					case OPTION_4:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(11021, 200);
						stage = 21;
						break;
					case OPTION_5:
						sendOptionsDialogue("Easter Points: " + Colors.GREEN + player.easterPoints, "Chicken Wings " + Colors.GREEN + "(200 Points)", "Chicken Legs " + Colors.GREEN + "(200 Points)",
								"Chicken Feet " + Colors.GREEN + "(200 Points)", "Easter Ring " + Colors.GREEN + "(600 Points)", "Return");
						stage = 22;
						break;
				}
				break;
			case 21:
				switch(componentId) {
					case OPTION_1:
						if(player.easterPoints >= pointsRequired) {
							if(player.getInventory().hasFreeSlots()) {
								player.getInventory().addItem(new Item(itemId, 1));
								player.easterPoints -= pointsRequired;
								player.sendMessage("Enjoy your reward & Happy Easter!");
								finish();
								setPurchaseItem(-1, -1);
							} else {
								player.getBank().addItem(new Item(itemId, 1), true);
								player.easterPoints -= pointsRequired;
								setPurchaseItem(-1, -1);
								finish();
							}
						} else {
							player.sendMessage("You do not have enough easter points to purchase this item");
							setPurchaseItem(-1, -1);
							finish();
						}
						
						break;
					
					case OPTION_2:
						setPurchaseItem(-1, -1);
						finish();
						break;
			}
			
			break;
			
			case 22:
				switch(componentId) {
					case OPTION_1:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(11020, 200);
						stage = 21;
						break;
					case OPTION_2:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(11022, 200);
						stage = 21;
						break;
					case OPTION_3:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(11019, 200);
						stage = 21;
						break;
					case OPTION_4:
						sendOptionsDialogue("Are you sure you'd like to purchase this item?", "Yes", "No");
						setPurchaseItem(7927, 650);
						stage = 21;
						break;
						
					case OPTION_5:
						sendOptionsDialogue("Easter Points: " + Colors.GREEN + player.easterPoints, "Bunny Ears " + Colors.GREEN + "(5000 Points)",
								"Rubber Chicken " + Colors.GREEN + "(750 Points)", "Easter Carrot " + Colors.GREEN + "(750 Points)", "Chicken Head " + Colors.GREEN + "(200 Points)", "More Options");
						
						stage = 20;
						break;
				}
					
		}
	}
	

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

	public void setPurchaseItem(int itemId, int pointsRequired) {
		this.itemId = itemId;
		this.pointsRequired = pointsRequired;
	}
	
}
