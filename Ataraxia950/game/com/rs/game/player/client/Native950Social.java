package com.rs.game.player.client;

import com.rs.game.ForceTalk;
import com.rs.game.player.Player;
import java.nio.charset.Charset;

/** Plain public chat uses the verified 950 flagged player-text mask, not 910 MESSAGE_PUBLIC. */
public final class Native950Social {
    private Native950Social() { }

    public static boolean speak(Player player, String input, long now) {
        if (player.isPermMuted() || player.getMuted() > now) return false;
        String text = clean(input);
        if (text.isEmpty()) return false;
        player.setNextForceTalk(new ForceTalk(text, true));
        return true;
    }

    static String clean(String input) {
        if (input == null) return "";
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < input.length() && text.length() < 160; i++) {
            char c = input.charAt(i);
            // Player text cannot inject client markup or string terminators.
            if (c == '<') c = '[';
            if (c == '>') c = ']';
            if (!Character.isISOControl(c)) text.append(c);
        }
        String result = text.toString().trim();
        return Charset.forName("windows-1252").newEncoder().canEncode(result) ? result : "";
    }

    public static boolean receives(Player sender, Player viewer) {
        if (sender == viewer) return true;
        if (viewer == null || viewer.getPublicStatus() == 2) return false;
        if (viewer.getFriendsIgnores().getIgnores().contains(sender.getUsername())) return false;
        return viewer.getPublicStatus() != 1
                || viewer.getFriendsIgnores().getFriends().contains(sender.getUsername());
    }

    public static boolean followable(Player player, Player target) {
        return target != null && target != player && target.isNative950()
                && target.isActive() && !target.hasFinished() && !target.isDead()
                && !player.isDead() && !player.isLocked() && !player.isNative950ForceMovementActive()
                && player.getPlane() == target.getPlane() && player.withinDistance(target, 16);
    }
}
