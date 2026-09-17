package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BrimhDungeon {

	private static int emoteId;

	public static boolean handleObjects(final Player player, int objectId) {
		if (objectId == 77379) {
			if (player.getX() == 2695 && player.getY() == 9482)
				start(player, 2693, 9482);
			else
				start(player, 2695, 9482);
			return true;
		}
		if (objectId == 77377) {
			if (player.getX() == 2676 && player.getY() == 9479)
				start(player, 2674, 9479);
			else
				start(player, 2676, 9479);
			return true;
		}
		if (objectId == 77375) {
			if (player.getX() == 2672 && player.getY() == 9499)
				start(player, 2674, 9499);
			else
				start(player, 2672, 9499);
			return true;
		}
		if (objectId == 77573) {
			if (player.getSkills().getLevel(Skills.AGILITY) >= 30) {
				player.setNextWorldTile(new WorldTile(2687, 9506, 0));
				return true;
			} else {
				player.sendMessage("You need at least a level of 30 agility to use this shortcut!");
				return true;
			}
		}
	      if (objectId == 77574) {
	            if (player.getSkills().getLevel(Skills.AGILITY) >= 30) {
	                player.setNextWorldTile(new WorldTile(2682, 9506, 0));
	                return true;
	            } else {
	                player.sendMessage("You need at least a level of 30 agility to use this shortcut!");
	                return true;
	            }
	        }
		if (objectId == 77507) {
			player.setNextWorldTile(new WorldTile(2637, 9518, 0));
			return true;
		}
		if (objectId == 77506) {
			player.setNextWorldTile(new WorldTile(2636, 9510, 2));
			return true;
		}
		if (objectId == 77570) {
			if (player.getSkills().getLevel(Skills.AGILITY) >= 12) {
				player.setNextWorldTile(new WorldTile(2647, 9557, 0));
				return true;
			} else {
				player.sendMessage("You need at least a level of 12 agility to use this shortcut!");
				return true;
			}
		}
		if (objectId == 77572) {
			if (player.getSkills().getLevel(Skills.AGILITY) >= 12) {
				player.setNextWorldTile(new WorldTile(2649, 9562, 0));
				return true;
			} else {
				player.sendMessage("You need at least a level of 12 agility to use this shortcut!");
				return true;
			}
		}
		if (objectId == 77423) {
			if (player.getSkills().getLevel(Skills.AGILITY) >= 22) {
				if (player.getX() == 2655)
					player.setNextWorldTile(new WorldTile(2655, 9566, 0));
				else
					player.setNextWorldTile(new WorldTile(2655, 9573, 0));
				return true;
			} else {
				player.sendMessage("You need at least a level of 22 agility to use this shortcut!");
				return true;
			}
		}
		if (objectId == 77508) {
			player.setNextWorldTile(new WorldTile(2643, 9593, 2));
			return true;
		}
		if (objectId == 77509) {
			player.setNextWorldTile(new WorldTile(2649, 9591, 0));
			return true;
		}
		if (objectId == 77373) {
			if (player.getX() == 2683 && player.getY() == 9570)
				start(player, 2683, 9568);
			else
				start(player, 2683, 9570);
			return true;
		}
		if (objectId == 77371) {
			if (player.getX() == 2689 && player.getY() == 9564)
				start(player, 2691, 9564);
			else
				start(player, 2689, 9564);
			return true;
		}
		if (objectId == 77424) {
			if (player.getSkills().getLevel(Skills.AGILITY) >= 77) {
				if (player.getY() >= 9499)
					player.setNextWorldTile(new WorldTile(2698, 9492, 0));
				else
					player.setNextWorldTile(new WorldTile(2698, 9499, 0));
				return true;
			} else {
				player.sendMessage("You need at least a level of 77 agility to use this shortcut!");
				return true;
			}
		}
		return false;
	}

	private static boolean hasAxe(Player player) {
		if (player.getInventory().containsOneItem(1351, 1349, 1353, 1355, 1357, 1361, 1359, 6739, 13661, 32645))
			return true;
		int weaponId = player.getEquipment().getWeaponId();
		if (weaponId == -1)
			return false;
		switch (weaponId) {
		case 1351:// Bronze Axe
		case 1349:// Iron Axe
		case 1353:// Steel Axe
		case 1361:// Black Axe
		case 1355:// Mithril Axe
		case 1357:// Adamant Axe
		case 1359:// Rune Axe
		case 6739:// Dragon Axe
		case 13661: // Inferno adze
		case 32645: // Crystal hatchet
			return true;
		default:
			return false;
		}
	}

	private static boolean start(final Player player, final int moveX, final int moveY) {
		final WorldTile toTile = new WorldTile(moveX, moveY, player.getPlane());
		if (player.isLocked()) {
			player.sendMessage("You cannot do that right now.");
			return true;
		}
		if (!hasAxe(player)) {
			player.sendMessage("You don't have a hatchet to chop away this vine.");
			return true;
		}
		if (!setAxe(player)) {
			player.sendMessage("You don't have the required level to use that axe.");
			return false;
		}
		player.lock(7);
		player.setNextAnimation(new Animation(emoteId));
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(-1));
					player.setNextWorldTile(toTile);
					player.unlock();
					this.stop();
				}
				loop++;
			}
		}, 1, Utils.random(3, 6));
		return false;
	}

	private static boolean setAxe(Player player) {
		int level = player.getSkills().getLevel(8);
		int weaponId = player.getEquipment().getWeaponId();
		if (weaponId != -1) {
			switch (weaponId) {
			case 32645: // crystal axe
				if (level >= 71) {
					emoteId = 2846; // TODO find anim
					return true;
				}
				break;
			case 6739: // dragon axe
				if (level >= 61) {
					emoteId = 2846;
					return true;
				}
				break;
			case 13661: // inferno adze
				if (level >= 61) {
					emoteId = 10251;
					return true;
				}
				break;
			case 1359: // rune axe
				if (level >= 41) {
					return true;
				}
				break;
			case 1357: // adam axe
				if (level >= 31) {
					emoteId = 869;
					return true;
				}
				break;
			case 1355: // mit axe
				if (level >= 21) {
					emoteId = 871;
					return true;
				}
				break;
			case 1361: // black axe
				if (level >= 11) {
					emoteId = 873;
					return true;
				}
				break;
			case 1353: // steel axe
				if (level >= 6) {
					emoteId = 875;
					return true;
				}
				break;
			case 1349: // iron axe
				emoteId = 877;
				return true;
			case 1351: // bronze axe
				emoteId = 879;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(32645)) {
			if (level >= 71) {
				emoteId = 2846; // TODO find anim
				return true;
			}
		}
		if (player.getInventory().containsOneItem(6739)) {
			if (level >= 61) {
				emoteId = 2846;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(13661)) {
			if (level >= 61) {
				emoteId = 10251;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1359)) {
			if (level >= 41) {
				emoteId = 867;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1357)) {
			if (level >= 31) {
				emoteId = 869;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1355)) {
			if (level >= 21) {
				emoteId = 871;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1361)) {
			if (level >= 11) {
				emoteId = 873;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1353)) {
			if (level >= 6) {
				emoteId = 875;
				return true;
			}
		}
		if (player.getInventory().containsOneItem(1349)) {
			emoteId = 877;
			return true;
		}
		if (player.getInventory().containsOneItem(1351)) {
			emoteId = 879;
			return true;
		}
		return false;
	}
}