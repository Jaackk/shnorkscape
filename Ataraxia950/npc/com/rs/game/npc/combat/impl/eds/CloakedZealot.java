package com.rs.game.npc.combat.impl.eds;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class CloakedZealot extends CombatScript {

    @SuppressWarnings("unchecked")
    @Override
    public int attack(NPC n, Entity target) {
        if (!(target instanceof Player))
            return 0;
        if (!(n instanceof EliteDungeonNPC))
            return 0;
        EliteDungeonNPC npc = (EliteDungeonNPC) n;
        if (!npc.hasChangedRenderAnimation())
            npc.setNextRenderAnimation(2688);
        List<EliteDungeonNPC> spawns = npc.getTemporaryAttributtes().get("minions") == null ? new ArrayList<EliteDungeonNPC>() : (ArrayList<EliteDungeonNPC>) npc.getTemporaryAttributtes().get("minions");
        long spawnMinionsDelay = npc.getTemporaryAttributtes().get("specialDelay") == null ? 0 : (long) npc.getTemporaryAttributtes().get("specialDelay");
        if (spawns.size() < 2 && (spawnMinionsDelay == 0 || Utils.currentTimeMillis() > spawnMinionsDelay)) {
            WorldTile tile = new WorldTile(npc);
            WorldTile spawnTile = tile;
            // attemps to randomize tile by 4x4 area
            for (int trycount = 0; trycount < 10; trycount++) {
                if (tile == null)
                    break;
                spawnTile = new WorldTile(tile, 2);
                if (tile == null || spawnTile == null)
                    break;
                if (World.canMoveNPC(tile.getPlane(), spawnTile.getX(), spawnTile.getY(), NPCDefinitions.getNPCDefinitions(25604).size))
                    break;
                spawnTile = tile;
            }
            npc.setNextForceTalk(new ForceTalk("Crassian! I choose you!"));
            EliteDungeonNPC m = new EliteDungeonNPC(25604, spawnTile, npc.getRoom());
            m.spawn();
            m.setNextAnimation(new Animation(23330));
            spawns.add(m);
            npc.getTemporaryAttributtes().put("minions", spawns);
            npc.getTemporaryAttributtes().put("specialDelay", Utils.currentTimeMillis() + 3000L);
        }
        npc.setNextAnimation(new Animation(npc.getAttackEmote()));
        int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
        delayHit(npc, 2, target, getMagicHit(npc, damage));
        World.sendProjectile(npc, target, 2699, 41, 16, 41, 35, 16, 0);
        return npc.getAttackSpeed();
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { "Cloaked zealot" };
    }

}
