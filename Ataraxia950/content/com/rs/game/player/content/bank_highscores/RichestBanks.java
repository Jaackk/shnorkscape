package com.rs.game.player.content.bank_highscores;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class RichestBanks {
    private final String regularUsername;
    private final long regularValue;
    private final String gimUsername;
    private final long gimValue;
    private final String hcimUsername;
    private final long hcimValue;
    private final String imUsername;
    private final long imValue;
}
