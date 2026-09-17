package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Skills;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

/** Current950 equipment contracts derived from definitions and their assets, never an item allow-list. */
public final class Native950EquipmentTypes {
    private static Store store;
    private static final Map<Integer,Type> types=new HashMap<Integer,Type>();
    private static final Map<Integer,Boolean> models=new HashMap<Integer,Boolean>();
    private Native950EquipmentTypes() { }

    public static synchronized Type resolve(int id) {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())
            throw new IllegalStateException("950 equipment requires the selected read-only cache");
        if(store!=Cache.STORE){types.clear();models.clear();store=Cache.STORE;}
        if(!types.containsKey(id))types.put(id,fromDefinition(Native950CacheItems.definition(id),
                Native950EquipmentTypes::modelExists,Native950EquipmentAnimations::resolveBas));
        return types.get(id);
    }

    private static boolean modelExists(int id) {
        if(id<0)return false;
        if(!models.containsKey(id)) {
            boolean present=false;
            try{byte[] bytes=Cache.STORE.getIndexes()[47].getFile(id,0);present=bytes!=null&&bytes.length>0;}
            catch(RuntimeException missing){ /* Missing/corrupt model is not an equipment asset. */ }
            models.put(id,present);
        }
        return models.get(id);
    }

    /** Pure seam uses the same bounded template resolver and validation as live equipment. */
    static Type resolve(int id,IntFunction<byte[]> definitions,IntPredicate models,BasResolver animations) {
        return fromDefinition(Native950CacheItems.resolvedDefinition(id,definitions),models,animations);
    }

    static Type fromDefinition(ItemDefinitions d,IntPredicate models,BasResolver animations) {
        try {
            if(d==null||!d.loaded||d.decodeFailure!=null||d.getId()<0||d.getId()>Native950ItemCatalog.MAX_ITEM_ID
                    ||d.name==null||d.name.trim().isEmpty()||"null".equalsIgnoreCase(d.name)
                    ||d.noted||d.certTemplateId>=0||d.shardTemplateId>=0
                    ||d.equipSlot<0||d.equipSlot>=19||!optionalSlot(d.getEquipType())||!optionalSlot(d.getEquipType2())
                    ||d.stackable<0||d.stackable>2)return null;
            List<Integer> operations=new ArrayList<Integer>();
            if(d.inventoryOptions!=null)for(int i=0;i<Math.min(5,d.inventoryOptions.length);i++)
                if(isWearLabel(d.inventoryOptions[i]))operations.add(i+1);
            if(operations.isEmpty())return null;
            int[] male={d.maleEquip1,d.maleEquip2,d.maleEquipModelId3};
            int[] female={d.femaleEquip1,d.femaleEquip2,d.femaleEquipModelId3};
            // -1 is an absent model, including intentionally invisible and nonvisual equipment.
            // Any actual reference must resolve; never substitute a model from another revision/gender.
            for(int[] gender:new int[][]{male,female})for(int model:gender)
                if(model < -1 || (model>=0&&!models.test(model)))return null;
            Map<Integer,Integer> requirements=requirements(d);
            if(requirements==null)return null;
            boolean hand=d.equipSlot==3||d.equipSlot==5;
            int bas=hand?animations.resolve(d,false):2699,combatBas=hand?animations.resolve(d,true):2688;
            if(bas<0||combatBas<0)return null;
            int[] options=new int[operations.size()];for(int i=0;i<options.length;i++)options[i]=operations.get(i);
            Transform transform=d.bindTemplateId>=0?Transform.BOUND:d.lendTemplateId>=0?Transform.LENT:Transform.NONE;
            int source=transform==Transform.BOUND?d.bindId:transform==Transform.LENT?d.lendId:d.getId();
            return new Type(d.getId(),d.name,d.equipSlot,d.getEquipType(),d.getEquipType2(),d.stackable==1,
                    bas,combatBas,source,transform,options,male,female,requirements);
        }catch(RuntimeException invalid){return null;}
    }

    public static boolean isWearLabel(String label) {
        return "Wear".equalsIgnoreCase(label)||"Wield".equalsIgnoreCase(label)||"Equip".equalsIgnoreCase(label);
    }
    private static boolean optionalSlot(int slot){return slot>=-1&&slot<19;}

    /** Read raw pairs rather than the legacy getter, whose names/IDs rewrite modern requirements. */
    static Map<Integer,Integer> requirements(ItemDefinitions d) {
        Map<Integer,Integer> result=new LinkedHashMap<Integer,Integer>();
        Map<Integer,Object> params=d.clientScriptData;
        if(params==null)return result;
        //950 script929 and PARAM types2/11/749..760 define exactly six STAT/level pairs.
        //761..768 are unrelated INT requirements, not additional skill IDs.
        for(int pair=0;pair<6;pair++) {
            Object rawSkill=params.get(749+pair*2),rawLevel=params.get(750+pair*2);
            if(rawSkill==null&&rawLevel==null)continue;
            if((rawSkill!=null&&!(rawSkill instanceof Integer))||(rawLevel!=null&&!(rawLevel instanceof Integer)))return null;
            // PARAM defaults and script929: absent STAT=-1 skips the pair; absent level=0.
            int skill=rawSkill==null?-1:(Integer)rawSkill,level=rawLevel==null?0:(Integer)rawLevel;
            if(skill==-1)continue;
            if(skill<0||skill>=Skills.SKILL_COUNT||level<0||level>255)return null;
            if(level>1&&Skills.getXPForLevel(skill,level)<=Skills.getXPForLevel(skill,level-1))return null;
            if(level>0)result.put(skill,Math.max(level,result.containsKey(skill)?result.get(skill):0));
        }
        Object capeSkill=params.get(277);
        if(capeSkill!=null) {
            if(!(capeSkill instanceof Integer))return null;
            int skill=(Integer)capeSkill;
            if(skill!=-1) {
                if(skill<0||skill>=Skills.SKILL_COUNT)return null;
                Object master=params.get(4244);
                if(master!=null&&!(master instanceof Integer))return null;
                int level=Integer.valueOf(1).equals(master)?120:99;
                result.put(skill,Math.max(level,result.containsKey(skill)?result.get(skill):0));
            }
        }
        return result;
    }

    interface BasResolver {int resolve(ItemDefinitions definition,boolean combat);}
    public enum Transform {NONE,LENT,BOUND}
    public static final class Type {
        public final int id,slot,hide1,hide2,bas,combatBas,sourceId;
        public final String name;
        public final boolean stackable,twoHanded;
        public final Transform transform;
        public final Map<Integer,Integer> requirements;
        private final int[] options,male,female,conflicts;
        private Type(int id,String name,int slot,int hide1,int hide2,boolean stackable,int bas,int combatBas,
                int sourceId,Transform transform,int[] options,int[] male,int[] female,Map<Integer,Integer> requirements) {
            this.id=id;this.name=name;this.slot=slot;this.hide1=hide1;this.hide2=hide2;this.stackable=stackable;
            this.bas=bas;this.combatBas=combatBas;this.sourceId=sourceId;this.transform=transform;
            this.twoHanded=slot==3&&(hide1==5||hide2==5);
            this.options=options.clone();this.male=male.clone();this.female=female.clone();
            this.requirements=Collections.unmodifiableMap(new LinkedHashMap<Integer,Integer>(requirements));
            Set<Integer> occupied=new LinkedHashSet<Integer>();occupied.add(slot);if(hide1>=0)occupied.add(hide1);if(hide2>=0)occupied.add(hide2);
            conflicts=new int[occupied.size()];int i=0;for(int value:occupied)conflicts[i++]=value;
        }
        public boolean isWearOption(int option){for(int enabled:options)if(enabled==option)return true;return false;}
        public int[] equipOptions(){return options.clone();}
        public int[] conflictSlots(){return conflicts.clone();}
        public int[] maleModels(){return male.clone();}
        public int[] femaleModels(){return female.clone();}
        /** All declared assets are validated; an absent model is a valid empty contribution. */
        public boolean genderSupported(boolean male){return true;}
    }
}