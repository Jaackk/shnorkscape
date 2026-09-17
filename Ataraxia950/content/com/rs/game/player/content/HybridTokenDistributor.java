package com.rs.game.player.content;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Xenthium 03/03/2020.
 */

public class HybridTokenDistributor {

    public static void rollForToken(Player player, Activity activity) {
        if (ThreadLocalRandom.current().nextInt(0, activity.getProbability()) == 0) {
            player.getBank().addItem(new Item(48498, 1), true);
            player.sendMessage(Colors.GOLD + "You've received a Hybrid token drop! It has been added to your bank.");
            World.sendNews(player, player.getDisplayName() + " received a Hybrid token drop from " + activity.getFormattedName() + ".", World.WORLD_NEWS);
            QueryExecutor.submit(new News(player, player.getDisplayName() + " received a Hybrid token drop from " + activity.getFormattedName() + "."));
        }
    }


    @AllArgsConstructor
    public enum Activity {
        // Currently configured to take 200-250 hours per armor set.
        NEX(180),
        VORAGO(35),
        ARAXXOR(60),
        LEGIONES(600),
        PEST_CONTROL(250),
        CORPOREAL_BEAST(450),
        QUEEN_BLACK_DRAGON(350) {
            @Override
            public String getFormattedName() {
                return "Queen Black Dragon";
            }
        },
        THE_MAGISTER(600) {
            @Override
            public String getFormattedName() {
                return "The Magister";
            }
        },
        TELOS_THE_WARDEN(75) {
            @Override
            public String getFormattedName() {
                return "Telos the Warden";
            }
        },
        NEX_ANGEL_OF_DEATH(125) {
            @Override
            public String getFormattedName() {
                return "Nex: Angel of Death";
            }
        },
        KALPHITE_KING(180) {
            @Override
            public String getFormattedName() {
                return "Kalphite King";
            }
        },
        BARROWS_RISE_OF_THE_SIX(250) {
            @Override
            public String getFormattedName() {
                return "Barrows: Rise of the Six";
            }
        },
        TEMPLE_OF_AMINISHI(35) {
            @Override
            public String getFormattedName() {
                return "Temple of Aminishi";
            }
        },
        ;

        // The 1 in x chance for a drop, eg: 250 = 1:250 odds.
        private final int probability;

        public int getProbability() {
            return probability;
        }

        public String getFormattedName() {
            return StringUtils.capitalize(this.toString().toLowerCase().replaceAll("_", " "));
        }
    }
}
