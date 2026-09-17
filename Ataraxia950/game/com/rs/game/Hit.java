package com.rs.game;

import com.rs.game.player.Player;

public final class Hit {

    private Entity source;
    private HitLook look;
    private int damage;
    private boolean critical, special;
    private Hit soaking;
    private final int delay;

    public Hit(int damage, HitLook look) {
        this(null, damage, look, 0);
    }

    public Hit(Entity source, int damage, HitLook look) {
        this(source, damage, look, 0);
    }

    public Hit(Entity source, int damage, HitLook look, boolean special) {
        this(source, damage, look, 0);
        this.special = true;
    }

    public Hit(Entity source, int damage, HitLook look, int delay) {
        this.source = source;
        this.damage = damage;
        this.look = look;
        this.delay = delay;
    }

    public void setSpecial(boolean value) {
        this.special = value;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDelay() {
        return delay;
    }

    public HitLook getLook() {
        return look;
    }

    public int getMark(Player player, Entity victm) {
        if (look == HitLook.MINING_CRITCAL_SWING || look == HitLook.MINING_DAMAGE_MULTIPLIER)
            return look.getMark();
        if (HitLook.HEALED_DAMAGE == look)
            return look.getMark();
        if (damage == 0) {
            return HitLook.MISSED.getMark();
        }
        int mark = look.getMark();
        if (look == HitLook.MELEE_DAMAGE || look == HitLook.RANGE_DAMAGE || look == HitLook.MAGIC_DAMAGE) {
            if (critical)
                mark++;
            mark++;
        }

        if (!interactingWith(player, victm) && look != HitLook.STALL_DAMAGE)
            mark += 17;
        return mark;
    }

    public Hit getSoaking() {
        return soaking;
    }

    public void setLook(HitLook look) {
        this.look = look;
    }

    public void setSoaking(Hit soaking) {
        this.soaking = soaking;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Entity source) {
        this.source = source;
    }

    public boolean interactingWith(Player player, Entity victm) {
        return player == victm || player == source;
    }

    public boolean isSpecialHit() {
        return special;
    }

    public boolean isCriticalHit() {
        return critical;
    }

    public boolean isInstantKill() {
        return (this.getLook() == HitLook.INSTANT_KILL_TYPE);
    }

    public boolean missed() {
        return damage == 0;
    }

    public void setCriticalMark() {
        critical = true;
    }

    public void setHealHit() {
        look = HitLook.HEALED_DAMAGE;
        critical = false;
    }

    // 86 melee critical
    //
    // 126 melee legacy critical
    public enum HitLook {

        MISSED(141), REGULAR_DAMAGE(144), MELEE_DAMAGE(132), RANGE_DAMAGE(135), MAGIC_DAMAGE(138),
        UNBLOCKABLE_MAGIC_DAMAGE(138), REFLECTED_DAMAGE(146), ABSORB_DAMAGE(148), POISON_DAMAGE(142),
        DEATHTOUCHED_DART(54), DESEASE_DAMAGE(142), // rs
        // removed
        // desease
        HEALED_DAMAGE(143), CANNON_DAMAGE(145), VORAGO_SPECIAL_DAMAGE(144), UNBLOCKABLE_REGULAR_DAMAGE(144),
        MINING_DAMAGE_MULTIPLIER(239), MINING_CRITCAL_SWING(243), STALL_DAMAGE(144), INSTANT_KILL_TYPE(54), CRITICAL_DAMAGE(144);

        private int mark;

        HitLook(int mark) {
            this.mark = mark;
        }

        public int getMark() {
            return mark;
        }

        public void setMark(int mark) {
            this.mark = mark;
        }

        public static HitLook getHitLook(String string) {
            for (HitLook l : HitLook.values())
                if (l.name().toLowerCase().equalsIgnoreCase(string))
                    return l;
            return null;
        }
    }
    
    public int getDamageDisplay(Player player) {
        int dmg = damage;
        if (dmg != 0 && dmg < 10 && player.isInLegacyCombatMode())
            dmg = 10;
        if (player.isInLegacyCombatMode() && (critical || (getLook().getMark() == HitLook.REGULAR_DAMAGE.getMark())
                || (getLook().getMark() == HitLook.POISON_DAMAGE.getMark()) || (getLook().getMark() == HitLook.CANNON_DAMAGE.getMark())  
                || (getLook().getMark() == HitLook.HEALED_DAMAGE.getMark()) || (getLook().getMark() == HitLook.ABSORB_DAMAGE.getMark())))
            dmg *= 10;
        if (dmg > Short.MAX_VALUE)
            return Short.MAX_VALUE - 1; // fix client crash
        return dmg;
    }
    
}