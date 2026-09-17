package com.rs.game.player.client;

import com.rs.game.player.GlobalPlayerUpdater;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.Map;
import java.util.Objects;

/**
 * Moves the schema-3/4 sections of a {@link Native950Save} into the real
 * {@link Player} and back without emitting a single packet.
 *
 * <p>Position, backpack, bank, equipment and the kit flag stay with
 * {@link Native950Containers} (they are container state); this binder owns
 * SKILLS, VITALS, SETTINGS, APPEARANCE, IDENTITY and SKILL_PROGRESS:
 * <ul>
 * <li>SKILLS go through {@link Skills#setLevelWithoutRefresh} /
 *     {@link Skills#setXpWithoutRefresh} (no UPDATE_STAT, no appearance
 *     regeneration; the caller regenerates the body once afterwards).</li>
 * <li>VITALS use {@code Entity.setHitpoints} (marks the target-info flag only),
 *     {@code Prayer.setPrayerpoints}, {@link Player#setRunEnergyWithoutRefresh}
 *     and {@link Player#setRunHidden} (which delegates to {@code Entity.setRun};
 *     {@code Player.setRun} is NOT used - it emits varp 463), all field writes.</li>
 * <li>SETTINGS use {@link Player#applyNativeSettings(Map)}.</li>
 * <li>APPEARANCE uses {@link GlobalPlayerUpdater#restoreLook}.</li>
 * <li>IDENTITY sets the display name and the creation / last-login stamps.</li>
 * <li>SKILL_PROGRESS restores crop, component, excavation and dungeon state without packets.</li>
 * </ul>
 * Every value was validated by {@link Native950Save}; a restored level is
 * additionally clamped to 1..(what the xp table allows + boosts) only by the
 * save's own bounds, exactly as the legacy deserialiser trusts its file.
 *
 * <p>The 947 stat rebalance is absorbed one step earlier, in
 * {@link Native950SaveStore}: a SKILLS section written at the older 27-stat width
 * has every level recomputed from its stored experience with the current per-stat
 * curve ({@link Native950Save.Skills#upgraded}) before it ever reaches this class,
 * so the levels written here are always consistent with the caps and curves the
 * engine holds now. This class therefore stays a plain mover, and the number of
 * stats it moves follows {@link Native950Save#SKILL_COUNT} (29 in the 947 model).
 */
public final class Native950PlayerBinder {

    private Native950PlayerBinder() { }

    /** Applies the six binder-owned sections to the player; packet-free. */
    public static void restore(Player player, Native950Save save) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(save, "save");
        if (!player.isNative950()) throw new IllegalArgumentException("Native character required");
        Skills skills = player.getSkills();
        Native950Save.Skills saved = save.skills();
        for (int skill = 0; skill < Native950Save.SKILL_COUNT; skill++) {
            skills.setLevelWithoutRefresh(skill, saved.level(skill));
            skills.setXpWithoutRefresh(skill, saved.xp(skill));
        }
        Native950Save.Vitals vitals = save.vitals();
        player.setHitpoints(vitals.hitpoints);
        if (player.getPrayer() != null) player.getPrayer().setPrayerpoints(vitals.prayerPoints);
        player.setRunEnergyWithoutRefresh(vitals.runEnergy);
        // setRunHidden, NOT setRun: Player.setRun calls sendRunButtonConfig() ->
        // getPackets().sendConfig(463, ...). restore() runs from the Native950Session
        // constructor, which Native950World.attachOnWorld executes BEFORE the 947
        // transport joins the pipeline, so that write is a counted drop and the
        // restored run state would silently never reach the client. This stays true
        // in M3 even though varp 463 is now bound: restore() must remain packet-free
        // (it runs from the Native950Session constructor, before the transport joins
        // the pipeline). Native950Session.ready() pushes the restored state -
        // sendRunButtonConfig, run energy, hitpoints, prayer, adrenaline and one
        // UPDATE_STAT per modelled stat (Native950Save.SKILL_COUNT of them, 29 in
        // the 947 model) - once the channel can actually carry it.
        player.setRunHidden(vitals.running);
        player.applyNativeSettings(save.settings());
        save.skillProgress().restore(player);
        Native950Save.Appearance look = save.appearance();
        player.getAppearence().restoreLook(look.male, look.colours(), look.bodyKits());
        Native950Save.Identity identity = save.identity();
        if (!identity.displayName.equals(save.username())) player.setDisplayName(identity.displayName);
        if (identity.created > 0) player.setCreationDate(identity.created);
        if (identity.lastLogin > 0) player.setLastLoggedIn(identity.lastLogin);
    }

    /**
     * Reads the six binder-owned sections from the player onto {@code base}
     * (whose containers and position the caller already captured).
     */
    public static Native950Save capture(Player player, Native950Save base, long sessionStarted) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(base, "base");
        Native950Save.Skills skills = new Native950Save.Skills(player.getSkills().getLevelsCopy(), player.getSkills().getXpCopy());
        Native950Save.Vitals vitals = new Native950Save.Vitals(clamp(player.getHitpoints(), 0, 65535),
                player.getPrayer() == null ? 0 : clamp(player.getPrayer().getPrayerpoints(), 0, 65535),
                clamp(player.getRunEnergy(), 0, 100), player.getRun());
        GlobalPlayerUpdater look = player.getAppearence();
        Native950Save.Appearance appearance = new Native950Save.Appearance(look.isMale(), look.getColoursCopy(), look.getBodyStyleCopy());
        long created = player.getCreationDate() > 0 ? player.getCreationDate() : sessionStarted;
        // lastLogin is the session start, not "now": a per-tick timestamp would dirty IDENTITY every checkpoint.
        Native950Save.Identity identity = new Native950Save.Identity(
                player.hasDisplayName() ? player.getDisplayName() : player.getUsername(), created, sessionStarted);
        return base.withSections(skills, vitals, player.nativeSettingsSnapshot(), appearance, identity)
                .withSkillProgress(Native950SkillProgress.capture(player));
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }
}
