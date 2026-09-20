package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.Projectile;
import com.rs.game.player.Player;

/** Ataraxia ranged item/munition ownership, resolved from the running950 cache. */
final class Native950RangedPresentation {
    final int graphic,start,end;
    Native950RangedPresentation(int graphic,int family,boolean twoHanded) {
        this.graphic=graphic;
        start=family==1?40:family==2?(twoHanded?30:20):23;
        end=start+(family==1?10:7);
    }
    static Native950RangedPresentation resolve(Player player,Native950CombatStyles.Profile profile) {
        if(profile==null||profile.style!=Native950CombatStyles.RANGED)return null;
        ItemDefinitions weapon=Native950CacheItems.definition(player.getEquipment().getWeaponId());
        int ammo=player.getEquipment().getAmmoId();
        ItemDefinitions ammunition=ammo<0?null:Native950CacheItems.definition(ammo);
        int graphic=graphic(weapon,ammunition);
        // Unlike legacy hard-coded effects, this ID comes from the exact950 item.
        // Cache admission already pins that archive; do not require equality with910.
        byte[] definition=graphic<0?null:com.rs.cache.Cache.STORE.getIndexes()[21].getFile(graphic>>>8,graphic&255);
        if(definition==null||definition.length<=1)return null;
        return new Native950RangedPresentation(graphic,profile.ammoFamily,weapon.getEquipType()==5);
    }
    static int graphic(ItemDefinitions weapon,ItemDefinitions ammunition) {
        int own=weapon==null?0:weapon.getCSOpcode(2940);
        int supplied=ammunition==null?0:ammunition.getCSOpcode(2940);
        return own>0?own:supplied>0?supplied:-1;
    }
    Projectile projectile(Entity from,Entity to) {
        // These are absolute 20ms end cycles, not a second duration to add to start.
        Projectile p=new Projectile(from,to,false,false,0,graphic,40,41,start,end,5,0);
        p.setNewProjectile(true);
        return p;
    }
    int hitDelay(){return Math.max(1,(end+29)/30);}
}
