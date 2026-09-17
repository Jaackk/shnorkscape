package com.rs.game.player.client;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
/** Reuse original spawn rows for the16 newly actionable gathering NPC types, without enabling whole regions. */
final class Native950SkillNpcPopulation {
 private Native950SkillNpcPopulation(){}
 static boolean candidate(int id){switch(id){
  case 18150:case 18151:case 18153:case 18155:case 18157:case 18159:case 18161:case 18163:case 18165:case 18167:case 18169:case 18171:
  case 5082:case 5083:case 5084:case 5085:return true;
  default: FlyingEntities flying=FlyingEntities.forNPCId(id);return Native950DungeoneeringAssets.tutorCandidate(id)||Native950Slayer.masterCandidate(id)||(flying!=null&&flying.isImpling());
 }}
 static boolean allows(Native950SpawnScope scope,int region,int id){return region>=0&&region<=65535&&(scope.allows(region)||candidate(id));}
 static boolean verified(int id,NPCDefinitions definition){
  if(!candidate(id)||definition==null||definition.decodeFailure!=null||definition.transformTo!=null||definition.models==null||definition.models.length==0
    ||definition.size<1||definition.size>255||!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,id))return false;
  if(Native950DungeoneeringAssets.tutorCandidate(id))return Native950DungeoneeringAssets.verifiedTutor(id,definition);
  if(Native950Slayer.masterCandidate(id))return Native950Slayer.verifiedMaster(id,definition);
  if(id>=18150)return definition.hasOption("Harvest")&&Native950RunecraftingDivinationAssets.verified("npc",id);
  return definition.hasOption("Catch")&&Native950Hunter.supports(FlyingEntities.forNPCId(id));
 }
}
