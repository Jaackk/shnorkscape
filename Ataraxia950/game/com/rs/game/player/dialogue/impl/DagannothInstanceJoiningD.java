package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.instances.Instance;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;

public class DagannothInstanceJoiningD extends Dialogue {

	private Instance[] instances;
	
	private String getName(int bossId) {
		switch(bossId) {
		case 2880:
			return "All three Dagannoth kings instance.";
		case 2881:
			return "Dagannoth Supreme instance.";
		case 2882:
			return "Dagannoth Prime instance.";
		default:
			return "Dagannoth Rex instance.";
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		ArrayList<Instance> ins = (ArrayList<Instance>) parameters[0];
		instances = new Instance[ins.size()];
		instances = ins.toArray(instances);
		String[] options = new String[instances.length];
		for (int i = 0; i < instances.length; i++) 
			options[i] = getName(instances[i].getBoss());
		sendOptionsDialogue("This user currently has " + instances.length + " active instances to this boss.<br>Select which you'd like to join.", options);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		switch(componentId) {
		case OPTION_1:
			instances[0].enterInstance(player);
			break;
		case OPTION_2:
			instances[1].enterInstance(player);
			break;
		case OPTION_3:
			instances[2].enterInstance(player);
			break;
			default:
				instances[3].enterInstance(player);
				break;
		}
	}

	@Override
	public void finish() {

	}

}
