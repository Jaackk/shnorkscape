package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

import java.util.ArrayList;
import java.util.List;

public class BankList extends Dialogue {

	int page = 0;
	int next = OPTION_5;
	boolean mode;
	Bank target;
	
	@Override
	public void start() {
		if(parameters.length > 0)
			mode = (Boolean) parameters[0];
		enforceDefaultBankNames(player);
		menu(true);
		stage = 0;
	}


	private static void enforceDefaultBankNames(Player p) {
		List<Bank> banks = p.getBanks();
		for (int i = 0; i < banks.size(); i++) {
			Bank b = banks.get(i);
			if (b != null) b.setName("Bank " + (i + 1));
		}
	}




	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
			case 0:
				if (componentId == OPTION_5) { // last slot
					finish();
					return;
				}
				action(componentId);
				break;

			case 1:
			switch(componentId) {
			case OPTION_1:
				player.setBank(target);
				sendDialogue("You have set "+Colors.GREEN+Colors.SHAD+target.getName()+"</col></shad> as your default bank!");
				stage = 2;
				break;
			case OPTION_2:
				player.sendInputString("Set bank name: ", new InputStringEvent() {
					@Override
					public void run(Player player) {
						target.setName(getString());
						player.getDialogueManager().startDialogue("BankList", true);
						return;
					}
				});
				finish();
				break;
			case OPTION_3:
				page = 0;
				stage = 0;
				menu(true);
				break;
			}
			break;
		case 2:
			settings();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

	public void menu(boolean start) {
		List<String> banks = new ArrayList<String>();
		int index;
		if(!start)
			page += 1;
		if(page*4 > player.getBanks().size())
			page = 0;
		index = page*4;

		for(int i=0; i < 5; i++) {
			if(player.getBanks().size() <= i+index) {
				if(page > 0) {
					if(i == banks.size())
						banks.add(i, "First page");
					else
						banks.set(i, "First page");
					next = i+12;
				} else
					banks.add(i, "Nevermind");
				break;
			}
			if(player.getBanks().get(index+i) != null)
				banks.add(player.getBanks().get(index+i).getName());
			
			if(i == 4) {
				banks.set(i, "Close");
				//next = OPTION_5;
			}
		}

		sendOptionsDialogue("Choose a bank,"+Colors.WHITE+
				" Page: "+(page+1), banks.toArray(new String[banks.size()-1]));
		stage = 0;
	}
	
	public void settings() {
		sendOptionsDialogue("Editing #"+(player.getBanks().indexOf(target)+1)+
				": "+Colors.WHITE+target.getName(), "Make this my default bank",
				"Change bank name", Colors.RED+"Back to bank list");
		stage = 1;
	}


	public static boolean openBankByIndex(Player player, int index) {
		if (index < 0 || index >= player.getBanks().size() || player.getBanks().get(index) == null) {
			player.sendMessage(Colors.RED + "You don't have that bank yet.");
			return false;
		}
		Bank target = player.getBanks().get(index);
		if (player.getInterfaceManager().containsBankInterface())
			player.getInterfaceManager().removeBankInterface();
		player.setBank(target);
		player.getBank().setPlayer(player);
		player.getBank().openBank();
		return true;
	}



	public void action(int componentId) {
		int option = (componentId - 8) / 5;
		if(player.getBanks().size() > option)
			target = player.getBanks().get(option);
		
		if(target == null) {
			player.setBank(player.getBank());
			finish();
			return;
		}
		
		if(mode)
			settings();
		else {
			if(player.getInterfaceManager().containsBankInterface())
				player.getInterfaceManager().removeBankInterface();
			player.setBank(target);
			player.getBank().setPlayer(player);
			player.getBank().openBank();
		}
	}
}
