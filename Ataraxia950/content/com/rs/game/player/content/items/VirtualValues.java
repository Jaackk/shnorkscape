package com.rs.game.player.content.items;

import com.rs.game.player.actions.herblore.HerbCleaning;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Utils;

public class VirtualValues {

	public static void setValues() {

		/**
		 * We have to increase prices for most items ;s.
		 */
		for (int i = 0; i < 34235; i++) {
			if (HerbCleaning.getHerb(i) != null) {
				if (GrandExchange.getPrice(i) < 4000)
					GrandExchange.setPrice(i, Utils.random(4500, 10000));
			}
			if (GrandExchange.getPrice(i) < 5)
				GrandExchange.setPrice(i, Utils.random(6, 25));
		}

		GrandExchange.setPrice(995, 1);

		// Lucky Armadyl godsword
		GrandExchange.setPrice(23679, GrandExchange.getPrice(11694));
		// Lucky Bandos godsword
		GrandExchange.setPrice(23680, GrandExchange.getPrice(11696));
		// Lucky Saradomin godsword
		GrandExchange.setPrice(23681, GrandExchange.getPrice(11698));
		// Lucky Zamorak godsword
		GrandExchange.setPrice(23682, GrandExchange.getPrice(11700));

		// Lucky Zamorakian spear
		GrandExchange.setPrice(23683, GrandExchange.getPrice(11716));

		// Lucky Armadyl helmet
		GrandExchange.setPrice(23684, GrandExchange.getPrice(11718));
		// Lucky Armadyl chestplate
		GrandExchange.setPrice(23685, GrandExchange.getPrice(11720));
		// Lucky Armadyl chainskirt
		GrandExchange.setPrice(23686, GrandExchange.getPrice(11722));

		// Lucky Bandos chestplate
		GrandExchange.setPrice(23687, GrandExchange.getPrice(11724));
		// Lucky Bandos tassets
		GrandExchange.setPrice(23688, GrandExchange.getPrice(11726));
		// Lucky Bandos boots
		GrandExchange.setPrice(23689, GrandExchange.getPrice(11728));

		// Lucky Saradomin sword
		GrandExchange.setPrice(23690, GrandExchange.getPrice(11730));

		// Lucky abyssal whip
		GrandExchange.setPrice(23691, GrandExchange.getPrice(4151));

		// Lucky dragon full helm
		GrandExchange.setPrice(23692, GrandExchange.getPrice(11335));
		// Lucky dragon platebody
		GrandExchange.setPrice(23693, GrandExchange.getPrice(14479));
		// Lucky dragon chainbody
		GrandExchange.setPrice(23694, GrandExchange.getPrice(3140));
		// Lucky dragon claws
		GrandExchange.setPrice(23695, GrandExchange.getPrice(14484));
		// Lucky dragon 2h sword
		GrandExchange.setPrice(23696, GrandExchange.getPrice(7158));

		// Lucky arcane spirit shield
		GrandExchange.setPrice(23697, GrandExchange.getPrice(13738));
		// Lucky divine spirit shield
		GrandExchange.setPrice(23698, GrandExchange.getPrice(13740));
		// Lucky elysian spirit shield
		GrandExchange.setPrice(23699, GrandExchange.getPrice(13742));
		// Lucky spectral spirit shield
		GrandExchange.setPrice(23700, GrandExchange.getPrice(13744));

		// Fire cape
		GrandExchange.setPrice(6570, 3500000);
		// TokHaar-Kal
		GrandExchange.setPrice(23659, 6000000);
		// Nomad capes
		GrandExchange.setPrice(15432, 65000);
		GrandExchange.setPrice(15433, 65000);

		// Effigy so lootbeams appear
		GrandExchange.setPrice(18778, 2147000000);

		// Abyssal vine whip
		GrandExchange.setPrice(21371, GrandExchange.getPrice(4151) + GrandExchange.getPrice(21369));

		// korasi
		GrandExchange.setPrice(19784, 245000);

		// Dragon fire shield
		GrandExchange.setPrice(11283, GrandExchange.getPrice(11284));

		// arcane stream neck
		GrandExchange.setPrice(18335, 3050000);

		// spirit shard
		GrandExchange.setPrice(12183, 25);
		GrandExchange.setPrice(18016, 25);

		// zanik cbow
		GrandExchange.setPrice(14684, GrandExchange.getPrice(9183));

		// Torsol
		GrandExchange.setPrice(219, 19402);// grimey
		GrandExchange.setPrice(269, 19422);// clean

		// Dwarfweed
		GrandExchange.setPrice(217, 11620);
		GrandExchange.setPrice(267, 11671);

		// lant
		GrandExchange.setPrice(2481, 16202);
		GrandExchange.setPrice(2485, 16231);

		// cadantine
		GrandExchange.setPrice(215, 16820);
		GrandExchange.setPrice(265, 16852);

		// kwaurm
		GrandExchange.setPrice(213, 13521);
		GrandExchange.setPrice(263, 13542);

		// Cleansing crystal
		GrandExchange.setPrice(32615, 110000);
	}
}