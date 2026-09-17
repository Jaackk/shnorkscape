package com.rs.game.activites.multiboss;

import com.rs.game.WorldObject;
import com.rs.game.player.controllers.Controller;

/**
 * @author Noel Multiboss Controller currently a skeleton
 */
public class MultibossController extends Controller {

	/**
	 * Player actions: death, re-logging, teleports, etc
	 */

	private void remove() {
		removeControler();
	}

	@Override
	public boolean login() {
		start();
		return false;
	}

	@Override
	public boolean logout() {
		remove();
		return false;
	}

	@Override
	public boolean sendDeath() {
		remove();
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		remove();
	}

	/**
	 * Items, NPCS, Objects
	 */

	@Override
	public void start() {

	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		switch (object.getId()) {

		case 82899: // elevator controls

			return false;

		case 82895: // grand door - elevator exit
			if (player.getX() > 1893) { // enter

			} else { // exit

			}
			return false;

		default:
			return true;
		}
	}

}
