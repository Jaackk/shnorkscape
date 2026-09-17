package com.rs.game.player.content.interfaces;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.network.packet.PacketDispatcher;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import java.util.ArrayList;
import java.util.List;

/**
 * ataraxia-server
 * paolo 17/10/2019
 * #Shnek6969
 */
public class NPCDropInterface {

    public static final int INTERFACE_ID = 130;
    public static final int NPC_MODEL_CONTAINER = 27;
    public static final int BIG_NPC_MODEL_CONTAINER = 243;
    public static final int ITEM_CONTAINER = 238;
    public static final int[] DROP_CONTAINERS = {49,54,59,64,69,75,80,85,90,95,100,105,110,115,120,125,130,135,140,145,150,155,160,170,188,193,198,203,208,213,218,223,228,233};
    public static final int[] POSSIBLE_NPC_COMPONENT = {176,178,179,180,181,182,183,184,185,186,244,245,246,247,248,249,250,251,252,253};
    public static final String KEY = "foundnpcs";

    public static void sendInterface(Player player){
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        //sendInfo(player,npcId);
        player.getPackets().sendHideComponents(INTERFACE_ID,true,DROP_CONTAINERS);
        player.getPackets().sendHideComponents(INTERFACE_ID,true,POSSIBLE_NPC_COMPONENT);
        player.getPackets().sendText(INTERFACE_ID,187,"Click 'Find NPC' to start.");
    }

    private static void sendInfo(Player player,int npcId){
        sendBossInfo(player, NPCDefinitions.getNPCDefinitions(npcId));
        sendDrops(player,npcId);
        player.getPackets().sendText(INTERFACE_ID,187, createInfoString(npcId));
    }

    private static final double getPreciseDropRate(final Player player, final NPCDrop[] drops, final int npcId, final int dropId) {
        final double encodedRate = getEncodedDropRate(player, drops, npcId, dropId);
        if (encodedRate == 0)
            return 0;
        int totalRate = 0;
        for (NPCDrop drop : drops) {
            if (drop.getRate() == 100)
                continue;
            totalRate += drop.getRate();
        }
        /**
         * at *100 the drop rates would all be slightly over it.
         * rate * 97 seems to result in the most promising data, although imperfect,
         * still best. This is due to being unable to calculate absolute randomness.
         * CBF explaining why that is.
         */
        return encodedRate * 97 / totalRate;
    }

    private static final double getEncodedDropRate(final Player player, final NPCDrop[] drops, final int npcId, final int dropId) {
        if (drops == null)
            return 0;
        for (NPCDrop drop : drops) {
            if (drop.getItemId() == dropId) {
                double rate = player.getHeart().getDropRate(npcId, drop);
                if (rate < 30)
                    rate *= Settings.getDropQuantityRate(player);
                return rate;
            }
        }
        return 0;
    }

    public static void sendDrops(Player player, int id){
        final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
        player.getPackets().sendHideComponents(INTERFACE_ID,true,DROP_CONTAINERS);
        if(drops == null)
            return;
        int[] items = new int[drops.length];
        for(int i = 0; i< drops.length; i++){
            if(i >= DROP_CONTAINERS.length) {
                PacketDispatcher.sendItemsFull(player, INTERFACE_ID,90, ITEM_CONTAINER, 1,DROP_CONTAINERS.length + 3,  items);
                return;
            }

            NPCDrop drop = drops[i];
            if(drop.getItemId() == 0)
                continue;
            items[i] = (drop.getItemId());
            player.getPackets().sendHideIComponent(INTERFACE_ID,DROP_CONTAINERS[i], false);
            player.getPackets().sendText(INTERFACE_ID,DROP_CONTAINERS[i] +2, ItemDefinitions.getItemDefinitions(drop.getItemId()).getName() +"  "+drop.getMinAmount()+"-"+drop.getMaxAmount()+"  "+Utils.round(getPreciseDropRate(player,drops,id,drop.getItemId()), 1)+"%");
        }
        PacketDispatcher.sendItemsFull(player, INTERFACE_ID,90, ITEM_CONTAINER, 1,DROP_CONTAINERS.length + 3,  items);
    }


    public static List<Integer> getAllNPCByPrefix(String npcPrefix){
        List<Integer> possibleNpcs = new ArrayList<Integer>();
        for(String s : NPCDefinitions.npcDefinitionsByName.keySet()){
            if(s.toLowerCase().contains(npcPrefix.toLowerCase())){
                if(NPCDropsDataParser.getDrops(NPCDefinitions.npcDefinitionsByName.get(s)) != null)
                    possibleNpcs.add(NPCDefinitions.npcDefinitionsByName.get(s));
            }
        }
        return possibleNpcs;
    }

    private static void sendPossibleNPC(Player player,List<Integer> npcIds){
        player.getPackets().sendHideComponents(INTERFACE_ID,true,POSSIBLE_NPC_COMPONENT);
        player.getTemporaryAttributtes().put(KEY, npcIds);
        for(int i = 0; i < POSSIBLE_NPC_COMPONENT.length; i++){
            if(i >= npcIds.size())
                return;
            player.getPackets().sendHideIComponent(INTERFACE_ID,POSSIBLE_NPC_COMPONENT[i], false);
            player.getPackets().sendIComponentText(INTERFACE_ID, POSSIBLE_NPC_COMPONENT[i], NPCDefinitions.getNPCDefinitions(npcIds.get(i)).getName());
        }
    }


    private static String createInfoString(int npcId){
        NPCDefinitions npcDefinitions = NPCDefinitions.getNPCDefinitions(npcId);
        StringBuilder sb = new StringBuilder();
        NPCCombatDefinition npcCombatDefinition = NPCCombatDefinitionsDataParser.getNPCCombatDefinitions(npcId);
        sb.append("<br>");
        sb.append("CombatLevel: "+npcDefinitions.combatLevel+" <br><br>");
        sb.append("Health: " + npcCombatDefinition.getHitpoints()+" <br><br>");
        sb.append("Max hit: " + npcCombatDefinition.getMaxHit()+" <br><br>");
        sb.append("Style: " + Utils.formatString(npcCombatDefinition.attackStyle)+" <br><br>");
        sb.append("Aggressive: " + (npcCombatDefinition.getAggressivenessType()== 1? "Yes": "No")+" <br><br>");
        return sb.toString();
    }

    public static void sendBossInfo(Player player, NPCDefinitions npcDefinitions){
        player.sm("Size: "+npcDefinitions.size);
        RenderAnimDefinitions renderAnimDefinitions = RenderAnimDefinitions.getRenderAnimDefinitions(npcDefinitions.getRenderAnimation());
        player.sm("render: "+renderAnimDefinitions.walkAnimation);
        player.sm("model length:"+npcDefinitions.models.length);
        player.getPackets().sendIComponentModel(INTERFACE_ID, BIG_NPC_MODEL_CONTAINER , -1);
        player.getPackets().sendIComponentModel(INTERFACE_ID, NPC_MODEL_CONTAINER , -1);
        player.getPackets().sendIComponentModel(INTERFACE_ID, npcDefinitions.size >= 3 ? BIG_NPC_MODEL_CONTAINER : NPC_MODEL_CONTAINER,npcDefinitions.models[0]);
        player.getPackets().sendIComponentAnimation(renderAnimDefinitions.standAnimation, INTERFACE_ID, npcDefinitions.size >= 3 ? BIG_NPC_MODEL_CONTAINER : NPC_MODEL_CONTAINER);
 }
     public static void handleButtons(Player player, int componentId){
        if(componentId == 37) {
            player.sendInputString("NPC Search", new InputStringEvent() {
                @Override
                public void run(Player player) {
                    String prefix = getString();
                    sendPossibleNPC(player, getAllNPCByPrefix(prefix));
                }
            });
            return;
        }
         List<Integer> list = (List<Integer>) player.getTemporaryAttributtes().get(KEY);
        if(list != null) {
            for (int i = 0; i < POSSIBLE_NPC_COMPONENT.length; i++) {
                if (POSSIBLE_NPC_COMPONENT[i] == componentId){
                    sendInfo(player, list.get(i));
                    return;
                }
            }
        }
     }
}
