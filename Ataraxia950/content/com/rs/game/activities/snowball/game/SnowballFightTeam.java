package com.rs.game.activities.snowball.game;

import com.rs.game.player.Player;
import com.rs.utils.Colors;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.rs.game.activities.snowball.game.SnowballFightTeamItemConstants.SARADOMIN_CLOAK_ITEM_ID;
import static com.rs.game.activities.snowball.game.SnowballFightTeamItemConstants.ZAMORAK_CLOAK_ITEM_ID;

class SnowballFightTeamItemConstants {
    static final int ZAMORAK_CLOAK_ITEM_ID = 4042;
    static final int SARADOMIN_CLOAK_ITEM_ID = 4041;
}

enum SnowballFightTeamType {
    ZAMORAKIAN(Colors.RED, ZAMORAK_CLOAK_ITEM_ID), SARADOMIN(Colors.CYAN, SARADOMIN_CLOAK_ITEM_ID);

    @Getter
    private final String messageColor;
    @Getter
    private final int itemId;

    SnowballFightTeamType(String messageColor, int itemId) {
        this.messageColor = messageColor;
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(name().toLowerCase().replace("_", " "));
    }
}

public class SnowballFightTeam {
    @Getter
    private final SnowballFightTeamType type;
    @Getter
    private final List<Player> players = new ArrayList<>();
    @Getter
    private final Object2IntOpenHashMap<Player> damageDealt = new Object2IntOpenHashMap<>();

    SnowballFightTeam(SnowballFightTeamType type) {
        this.type = type;
    }

    Player getHighestDamagingPlayer() {
        return Collections.max(damageDealt.object2IntEntrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }
}
