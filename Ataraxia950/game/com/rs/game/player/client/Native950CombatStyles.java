package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Hit;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.io.InputStream;
import java.util.*;

/** Basic auto-attacks use current item/weapon structs and the existing910 combat tick/XP loop.
 * Air spells progress automatically; ammunition caps ranged damage. Abilities and projectiles follow later. */
public final class Native950CombatStyles {
    public static final int MELEE=0,RANGED=1,MAGIC=2,NECROMANCY=3;
    private static Object store;
    private static final Map<Integer,Profile> profiles=new HashMap<>();
    private Native950CombatStyles() { }
    public static synchronized Profile profile(int id) {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalArgumentException("The current combat cache is unavailable.");
        if(store!=Cache.STORE){profiles.clear();store=Cache.STORE;}
        if(!profiles.containsKey(id))profiles.put(id,decode(Native950CacheItems.definition(id)));
        Profile p=profiles.get(id);
        if(p==null)throw new IllegalArgumentException("That weapon does not have a supported basic attack yet.");
        return p;
    }
    private static Profile decode(ItemDefinitions d) {
        if(d==null||d.equipSlot!=3||Native950EquipmentTypes.resolve(d.getId())==null)return null;
        int style=classify(d);
        if(style<0)return null;
        int struct=d.getCSOpcode(686,-1);
        if(struct<0)return null;
        byte[] raw=Cache.STORE.getIndexes()[22].getFile(struct>>>5,struct&31);
        Map<Integer,Integer> params=struct(raw);
        int anim=params.getOrDefault(2914,-1),block=params.getOrDefault(2917,425),weaponStyle=params.getOrDefault(2853,-1);
        if(!sequence(anim)||!sequence(block))return null;
        // Current weapon structs distinguish ordinary consumable dart/knife/axe/javelin
        // families from chinchompas and powered weapons. A thrown weapon is its own ammunition.
        int ammo=style==RANGED?(weaponStyle==8?1:weaponStyle==9?2:
                weaponStyle==10&&ordinaryThrown(d,struct)?3:-1):0;
        if(ammo<0)return null;
        int skill=style==RANGED?Skills.RANGE:style==MAGIC?Skills.MAGIC:style==NECROMANCY?28:Skills.ATTACK;
        Map<Integer,Integer> requirements=Native950EquipmentTypes.resolve(d.getId()).requirements;
        int tier=Math.max(1,Math.min(120,d.getCSOpcode(23,requirements.getOrDefault(skill,1))));
        return new Profile(style,skill,tier,Math.max(2,Math.min(10,d.getCSOpcode(14,4))),
                style==MELEE?1:Math.max(2,Math.min(10,d.getCSOpcode(13,6))),anim,block,ammo,d.getCSOpcode(972)==1);
    }
    static int classify(ItemDefinitions d){
        if(d==null)return -1;
        if(d.getCSOpcode(8898)==1)return NECROMANCY;
        if(d.isRangeTypeWeapon())return RANGED;
        if(d.isMagicTypeWeapon())return MAGIC;
        if(d.isMeleeTypeWeapon())return MELEE;
        return -1;
    }
    private static boolean sequence(int id){
        if(id<0)return false;
        byte[] b=Cache.STORE.getIndexes()[20].getFile(id>>>7,id&127);
        if(b==null||b.length==0)return false;
        AnimationDefinitions.decodeStrict947(id,b,null);return true;
    }
    private static Map<Integer,Integer> struct(byte[] raw){
        if(raw==null)throw new IllegalArgumentException("Missing weapon combat profile.");
        InputStream in=new InputStream(raw,true);Map<Integer,Integer> values=new HashMap<>();
        for(int op;(op=in.readUnsignedByte())!=0;){
            if(op!=249)throw new IllegalArgumentException("Unknown weapon profile field.");
            int count=in.readUnsignedByte();
            for(int i=0;i<count;i++){int type=in.readUnsignedByte(),key=in.read24BitInt();
                if(type==0)values.put(key,in.readInt());else if(type==1)in.readString();else throw new IllegalArgumentException("Invalid weapon field.");}
        }
        if(in.getRemaining()!=0)throw new IllegalArgumentException("Trailing weapon profile fields.");return values;
    }
    static Native950MeleeCombat.Loadout loadout(Player p) {
        Profile profile=profile(p.getEquipment().getWeaponId());
        int defence=0;
        for(Item item:p.getEquipment().getItems().getItems())if(item!=null){
            Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(item.getId());
            if(type==null)throw new IllegalArgumentException("An equipped item has invalid current-cache metadata.");
            ItemDefinitions definition=Native950CacheItems.definition(item.getId());
            com.rs.game.player.combat.rs2.ClassicBonuses classic=com.rs.game.player.combat.rs2.RS2BonusDatabase.lookup2009scape(
                    Native950MeleeEquipment.classicName(item.getId(),definition.name));
            if(classic!=null)defence+=classic.crushDef;
            else {
                Integer armour=nativeArmourBonus(definition.clientScriptData);
                if(armour!=null)defence+=armour;
                else if(type.slot!=Equipment.SLOT_WEAPON&&type.requirements.containsKey(Skills.DEFENCE))
                    defence+=com.rs.game.player.combat.rs2.ClassicItemBonusResolver.armourFromTier(type.slot,definition.getType(),0,
                            type.requirements.get(Skills.DEFENCE),false).crushDef;
            }
            for(Map.Entry<Integer,Integer> requirement:type.requirements.entrySet())
                if(p.getSkills().getLevelForXp(requirement.getKey())<requirement.getValue())
                    throw new IllegalArgumentException("You need "+Skills.SKILL_NAME[requirement.getKey()]+" "+requirement.getValue()+" to use "+type.name+".");
        }
        // Cache tiers map to conservative classic bonuses; this is a baseline formula, not EOC balance.
        int damageTier=profile.tier;
        if(profile.style==RANGED&&profile.ammoFamily!=3){
            Item ammo=p.getEquipment().getItem(Equipment.SLOT_ARROWS);
            int supplied=Native950DevelopmentAmmo.supplied(p.getEquipment().getWeaponId(),ammo==null?-1:ammo.getId(),p.isInfiniteAmmunition());
            ItemDefinitions d=supplied>=0?Native950CacheItems.definition(supplied):ammo==null?null:Native950CacheItems.definition(ammo.getId());
            if(matchesAmmo(d,profile.ammoFamily))damageTier=ammunitionDamageTier(profile.tier,d);
        }
        return new Native950MeleeCombat.Loadout(4+profile.tier,4+damageTier,defence,profile.speed,profile.animation,profile.block,profile);
    }
    /** Param2870 is tenths of EoC armour; retain the existing rating-to-classic scale. */
    static Integer nativeArmourBonus(Map<Integer,Object> params) {
        if(params==null||!params.containsKey(2870))return null;
        Object value=params.get(2870);
        if(!(value instanceof Integer)||((Integer)value)<0)
            throw new IllegalArgumentException("Invalid current-cache armour rating.");
        int rating=((Integer)value)/10;
        return (int)Math.round(rating/com.rs.game.player.combat.rs2.Rs2AtaraxiaCacheBonuses.PLAYER_ARMOUR_RATING_DIVISOR);
    }
    public static final class Profile {
        public final int style,skill,tier,speed,range,animation,block,ammoFamily;
        public final boolean airStaff;
        Profile(int style,int skill,int tier,int speed,int range,int animation,int block,int ammoFamily,boolean airStaff){
            this.style=style;this.skill=skill;this.tier=tier;this.speed=speed;this.range=range;
            this.animation=animation;this.block=block;this.ammoFamily=ammoFamily;this.airStaff=airStaff;
        }
        String costRefusal(Player p){
            // Development ammunition is an infinite compatible supply, not a
            // substitute for equipping a ranged weapon/profile in the first place.
            if(style==RANGED&&p.isInfiniteAmmunition())return null;
            if(ammoFamily==3){
                Item held=p.getEquipment().getItem(Equipment.SLOT_WEAPON);
                if(held==null||held.getAmount()<1)return "Equip more thrown weapons before attacking.";
                ItemDefinitions d=Native950CacheItems.definition(held.getId());
                if(!ordinaryThrown(d,d.getCSOpcode(686,-1))||held.getAttributes()!=null||held.getCharges()!=0||held.getInventionData()!=null)
                    return "That thrown weapon needs a depletion policy that is not available yet.";
                return null;
            }
            if(style==MAGIC){
                Native950AutoSpells.Spell spell=Native950AutoSpells.select(p);
                if(p.getSkills().getLevel(Skills.MAGIC)<spell.level)
                    return "You need level "+spell.level+" Magic to cast the selected "+spell.name+".";
                if(!p.isInfiniteCombatRunes()&&!airStaff&&!p.getInventory().containsItem(556,spell.airRunes))
                    return spell.name+" needs "+spell.airRunes+" air rune(s) per cast, or an air-supplying staff.";
            }
            if(style==RANGED){
                Item ammo=p.getEquipment().getItem(Equipment.SLOT_ARROWS);
                if(ammo==null||ammo.getAmount()<1)return "Equip ammunition before attacking.";
                ItemDefinitions d=Native950CacheItems.definition(ammo.getId());
                if(!matchesAmmo(d,ammoFamily))return ammoFamily==1?"This bow needs arrows.":"This crossbow needs bolts.";
                if(p.getEquipment().getWeaponId()==8880&&ammo.getId()!=8882)return "The Dorgeshuun crossbow needs bone bolts.";
            }
            return null;
        }
        boolean consume(Player p){
            if(costRefusal(p)!=null)return false;
            if(style==MAGIC){
                int runes=Native950AutoSpells.select(p).airRunes;
                return p.isInfiniteCombatRunes()||airStaff||Native950Skilling.exchange(p,new Item[]{new Item(556,runes)},new Item[0]);
            }
            if(style==RANGED){
                if(p.isInfiniteAmmunition())return true;
                int slot=ammoFamily==3?Equipment.SLOT_WEAPON:Equipment.SLOT_ARROWS;
                Item ammo=p.getEquipment().getItem(slot);
                Native950Containers c=Native950Skilling.containers(p);
                if(c==null||ammo==null)return false;
                return c.consumeEquipment(slot,ammo.getId(),1);
            }
            return true;
        }
        int maxHit(Player p,int computed){
            if(style!=MAGIC)return computed;
            int level=p.getSkills().getLevel(Skills.MAGIC);
            int effective=Native950AutoSpells.damageTier(level,tier,Native950AutoSpells.select(p));
            // Retain the server's classic damage units/formula, with RS3 progression limits.
            return com.rs.game.player.combat.rs2.Rs2CombatFormula.meleeOrRangedMaxHit(
                    com.rs.game.player.combat.rs2.Rs2CombatFormula.effectiveLevel(effective,0,0,1),4+effective,1);
        }
        Hit.HitLook look(){return style==RANGED?Hit.HitLook.RANGE_DAMAGE:style==NECROMANCY?Hit.HitLook.NECROMANCY_DAMAGE:style==MAGIC?Hit.HitLook.MAGIC_DAMAGE:Hit.HitLook.MELEE_DAMAGE;}
    }
    static boolean ordinaryThrown(ItemDefinitions d,int struct){
        return d!=null&&d.equipSlot==Equipment.SLOT_WEAPON&&d.isStackable()
                &&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0
                &&d.getCSOpcode(2940)>0&&d.getCSOpcode(31)==0
                &&(struct==14943||struct==14944||struct==14945||struct==14946);
    }
    static boolean matchesAmmo(ItemDefinitions d,int family){
        if(d==null||d.equipSlot!=Equipment.SLOT_ARROWS||d.certTemplateId>=0||d.getCSOpcode(2940)<=0)return false;
        String n=d.name.toLowerCase(Locale.ROOT);
        return family==1?n.contains("arrow")&&!n.contains("arrowhead"):family==2&&n.contains("bolt")&&!n.contains("bolt tip");
    }
    static int ammunitionDamageTier(int weaponTier,ItemDefinitions ammo){
        if(ammo==null)return 1;
        Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(ammo.getId());
        int tier=ammo.getCSOpcode(23,type==null?1:type.requirements.getOrDefault(Skills.RANGE,1));
        return Math.max(1,Math.min(weaponTier,Math.min(120,tier)));
    }
}
