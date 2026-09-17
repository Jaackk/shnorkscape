package com.rs.game.player;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rs.Settings;
import com.rs.game.Graphics;
import com.rs.game.HeadIcon;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.hitbar.HitBar;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.pet.Pet;
import com.rs.network.io.OutputStream;
import com.rs.utils.Utils;

public final class LocalNPCUpdate {

    private final Player player;
    private final LinkedList<NPC> localNPCs;

    public LocalNPCUpdate(Player player) {
        this.player = player;
        localNPCs = new LinkedList<NPC>();
    }

    public void reset() {
        localNPCs.clear();
    }

    public OutputStream createPacketAndProcess() {
        OutputStream stream = new OutputStream();
        OutputStream updateBlockData = new OutputStream();
        stream.writePacketVarShort(player, 186);
        processLocalNPCsInform(stream, updateBlockData);
        stream.writeBytes(updateBlockData.getBuffer(), 0, updateBlockData.getOffset());
        stream.endPacketVarShort();
        return stream;
    }

    public OutputStream updateSlayerMasters(NPC n, int id) {
        player.getSlayer().setSpawnedMasterId(id);
        OutputStream stream = new OutputStream();
        OutputStream updateBlockData = new OutputStream();
        stream.writePacketVarShort(player, 186);
        int renderBits = player.getNPCViewDistanceBits();
        int range = ((1 << renderBits - 1) - 2);
        int check = ((1 << renderBits - 1) - 1);
        int add = 1 << renderBits;
        stream.initBitAccess();
        stream.writeBits(8, localNPCs.size());
        for (Iterator<NPC> it = localNPCs.iterator(); it.hasNext();) {
            NPC npc = it.next();
            if (npc == n || npc.hasFinished() || !(npc.withinDistance(player, range) && player.getMapRegionsIds().contains(n.getRegionId())) || npc.hasTeleported()) {
                stream.writeBits(1, 1);
                stream.writeBits(2, 3);
                it.remove();
                continue;
            }
            boolean needUpdate = npc.needMasksUpdate() || player.getCombatDefinitions().isNeedTargetReticuleUpdate(n);
            boolean walkUpdate = npc.getNextWalkDirection() != -1;
            stream.writeBits(1, needUpdate || walkUpdate ? 1 : 0);
            if (walkUpdate) {
                stream.writeBits(2, npc.getNextRunDirection() == -1 ? 1 : 2);
                if (npc.getNextRunDirection() != -1) {
                    stream.writeBits(1, 1);
                }
                stream.writeBits(3, Utils.getNpcMoveDirection(npc.getNextWalkDirection()));
                if (npc.getNextRunDirection() != -1) {
                    stream.writeBits(3, Utils.getNpcMoveDirection(npc.getNextRunDirection()));
                }
                stream.writeBits(1, needUpdate ? 1 : 0);
            } else if (needUpdate) {
                stream.writeBits(2, 0);
            }
            if (needUpdate) {
                appendUpdateBlock(n, updateBlockData, false);
            }
        }
        for (int regionId : player.getMapRegionsIds()) {
            List<Integer> indexes = World.getRegion(regionId).getNPCsIndexes();
            if (indexes == null) {
                continue;
            }
            for (int npcIndex : indexes) {
                if (localNPCs.size() == Settings.SV_LOCAL_NPCS_LIMIT) {
                    break;
                }
                NPC npc = World.getNPCs().get(npcIndex);
                if (npc == null || npc.hasFinished() || localNPCs.contains(npc) || !(npc.withinDistance(player, range) && player.getMapRegionsIds().contains(n.getRegionId())) || npc.isDead()) {
                    continue;
                }
                stream.writeBits(15, npc.getIndex());
                boolean needUpdate = true;
                int x = n.getX() - player.getX();
                int y = n.getY() - player.getY();
                if (x < check)
                    x += add;
                if (y < check)
                    y += add;
                stream.writeBits(1, n.hasTeleported() ? 1 : 0);
                stream.writeBits(3, (n.getDirection() >> 11) - 4);
                stream.writeBits(renderBits, x);
                stream.writeBits(2, n.getPlane());
                stream.writeBits(15, n.getId());
                stream.writeBits(renderBits, y);
                stream.writeBits(1, needUpdate ? 1 : 0);
                localNPCs.add(npc);
                if (needUpdate) {
                    appendUpdateBlock(npc, updateBlockData, true);
                }
            }
        }
        if (updateBlockData.getOffset() > 0)
            stream.writeBits(15, 32767);
        stream.finishBitAccess();
        stream.writeBytes(updateBlockData.getBuffer(), 0, updateBlockData.getOffset());
        stream.endPacketVarShort();
        return stream;
    }

    private void processLocalNPCsInform(OutputStream stream, OutputStream updateBlockData) {
        stream.initBitAccess();
        processInScreenNPCs(stream, updateBlockData);
        addInScreenNPCs(stream, updateBlockData);
        if (updateBlockData.getOffset() > 0) {
            stream.writeBits(15, 32767);
        }
        stream.finishBitAccess();
    }

    private void processInScreenNPCs(OutputStream stream, OutputStream updateBlockData) {
        int renderBits = player.getNPCViewDistanceBits();
        int range = ((1 << renderBits - 1) - 2);
        stream.writeBits(8, localNPCs.size());
        for (Iterator<NPC> it = localNPCs.iterator(); it.hasNext();) {
            NPC n = it.next();
            if (n.hasFinished() || !(n.withinDistance(player, range) && (player.getMapRegionsIds().contains(n.getRegionId()))) || n.hasTeleported()) {
                stream.writeBits(1, 1);
                stream.writeBits(2, 3);
                it.remove();
                continue;
            }
            boolean needUpdate = n.needMasksUpdate() || player.getCombatDefinitions().isNeedTargetReticuleUpdate(n) || ((n instanceof Familiar || n instanceof Pet));
            boolean walkUpdate = n.getNextWalkDirection() != -1;
            stream.writeBits(1, (needUpdate || walkUpdate) ? 1 : 0);
            if (walkUpdate) {
                stream.writeBits(2, n.getNextRunDirection() == -1 ? 1 : 2);
                if (n.getNextRunDirection() != -1)
                    stream.writeBits(1, 1);
                stream.writeBits(3, Utils.getNpcMoveDirection(n.getNextWalkDirection()));
                if (n.getNextRunDirection() != -1)
                    stream.writeBits(3, Utils.getNpcMoveDirection(n.getNextRunDirection()));
                stream.writeBits(1, needUpdate ? 1 : 0);
            } else if (needUpdate)
                stream.writeBits(2, 0);
            if (needUpdate)
                appendUpdateBlock(n, updateBlockData, false);
        }
    }

    private void addInScreenNPCs(OutputStream stream, OutputStream updateBlockData) {
        int renderBits = player.getNPCViewDistanceBits();
        int range = ((1 << renderBits - 1) - 2);
        int check = ((1 << renderBits - 1) - 1);
        int add = 1 << renderBits;
        for (int regionId : player.getMapRegionsIds()) {
            List<Integer> indexes = World.getRegion(regionId).getNPCsIndexes();
            if (indexes == null) {
                continue;
            }
            for (int npcIndex : indexes) {
                if (localNPCs.size() == Settings.SV_LOCAL_NPCS_LIMIT)
                    break;
                NPC n = World.getNPCs().get(npcIndex);
                if (n == null || n.hasFinished() || localNPCs.contains(n) || !(n.withinDistance(player, range) && player.getMapRegionsIds().contains(n.getRegionId())) || (!(n instanceof EliteDungeonNPC) && n.isDead())) {
                    continue;
                }
                if ((n instanceof Familiar || n instanceof Pet) && player.isHidePets())
                    continue;
                stream.writeBits(15, n.getIndex());
                boolean needUpdate = true;
                int x = n.getX() - player.getX();
                int y = n.getY() - player.getY();
                if (x < check)
                    x += add;
                if (y < check)
                    y += add;
                stream.writeBits(1, n.hasTeleported() ? 1 : 0);
                stream.writeBits(3, (n.getDirection() >> 11) - 4);
                stream.writeBits(renderBits, x);
                stream.writeBits(2, n.getPlane());
                stream.writeBits(15, n.getId());
                stream.writeBits(renderBits, y);
                stream.writeBits(1, needUpdate ? 1 : 0);
                localNPCs.add(n);
                if (needUpdate) {
                    appendUpdateBlock(n, updateBlockData, true);
                }
            }
        }
    }

    private void appendUpdateBlock(NPC n, OutputStream data, boolean added) {
        int maskData = 0;
        if (n.getNextGraphics4() != null) {
            maskData |= 0x4000000;
        }
        if (n.getNextForceMovement() != null) {
            maskData |= 0x8000;
        }
        if (added || n.isRefreshHeadIcon()) {
            maskData |= 0x20000;
        }
        if (n.getNextTransformation() != null) {
            maskData |= 0x10;
        }
        if (n.getNextGraphics1() != null) {
            maskData |= 0x80;
        }
        if (n.hasChangedRenderAnimation()) {
            maskData |= 0x800;
        }
        if (n.getNextFaceWorldTile() != null && n.getNextRunDirection() == -1 && n.getNextWalkDirection() == -1) {
            maskData |= 0x1;
        }
        if ((n instanceof Familiar || n instanceof Pet) && !player.isHidePets()) {
            maskData |= 0x1000000;
        }
        if (player.getCombatDefinitions().isNeedTargetReticuleUpdate(n)) {
            maskData |= 0x2000000;
        }
        if (n.hasChangedCombatLevel() || added && n.getCustomCombatLevel() >= 0) {
            maskData |= 0x800000;
        }
        if (n.hasChangedModels()) {
            maskData |= 0x400;
        }
        if (n.getNextFaceEntity() != -2 || added && n.getLastFaceEntity() != -1) {
            maskData |= 0x20;
        }
        if (n.getNextAnimation() != null) {
            maskData |= 0x40;
        }
        if (added || n.isNeedTargetInformationUpdate()) {
            maskData |= 0x40000;
        }
        if (n.getNextGraphics2() != null) {
            maskData |= 0x2000;
        }
        if (n.hasChangedName() || added && n.getCustomName() != null) {
            maskData |= 0x10000;
        }
        if (n.getNextGraphics3() != null) {
            maskData |= 0x8000000;
        }
        if (n.getNextForceTalk() != null) {
            maskData |= 0x4;
        }
        if (!n.getNextHits().isEmpty() || !n.getNextHitBars().isEmpty()) {
            maskData |= 0x8;
        }

        if (maskData > 0xff) {
            maskData |= 0x2;
        }
        if (maskData > 0xffff) {
            maskData |= 0x200;
        }
        if (maskData > 0xffffff) {
            maskData |= 0x100000;
        }
        data.writeShort(0);
        data.writeByte(maskData);
        if (maskData > 0xff) {
            data.writeByte(maskData >> 8);
        }
        if (maskData > 0xffff) {
            data.writeByte(maskData >> 16);
        }
        if (maskData > 0xffffff) {
            data.writeByte(maskData >> 24);
        }
        if (n.getNextGraphics4() != null) {
            applyGraphicsMask4(n, data);
        }
        if (n.getNextForceMovement() != null) {
            applyForceMovementMask(n, data);
        }
        if (added || n.isRefreshHeadIcon()) {
            applyIconMask(n, data);
        }
        if (n.getNextTransformation() != null) {
            applyTransformationMask(n, data);
        }
        if (n.getNextGraphics1() != null) {
            applyGraphicsMask1(n, data);
        }
        if (n.hasChangedRenderAnimation()) {
            applyRenderAnimation(n, data);
        }
        if (n.getNextFaceWorldTile() != null && n.getNextRunDirection() == -1 && n.getNextWalkDirection() == -1) {
            applyFaceWorldTileMask(n, data);
        }
        if ((n instanceof Familiar || n instanceof Pet) && !player.isHidePets()) {
            applyHideFamiliarOptionsMask(n, data);
        }
        if (player.getCombatDefinitions().isNeedTargetReticuleUpdate(n)) {
            applyTargetReticuleMask(n, data);
        }
        if (n.hasChangedCombatLevel() || added && n.getCustomCombatLevel() >= 0) {
            applyChangeLevelMask(n, data);
        }
        if (n.hasChangedModels()) {
            applyModelsMask(n, data);
        }
        if (n.getNextFaceEntity() != -2 || added && n.getLastFaceEntity() != -1) {
            applyFaceEntityMask(n, data);
        }
        if (n.getNextAnimation() != null) {
            applyAnimationMask(n, data);
        }
        if (added || n.isNeedTargetInformationUpdate()) {
            applyTargetInformationMask(n, data);
        }
        if (n.getNextGraphics2() != null) {
            applyGraphicsMask2(n, data);
        }
        if (n.hasChangedName() || added && n.getCustomName() != null) {
            applyNameChangeMask(n, data);
        }
        if (n.getNextGraphics3() != null) {
            applyGraphicsMask3(n, data);
        }
        if (n.getNextForceTalk() != null) {
            applyForceTalkMask(n, data);
        }
        if (!n.getNextHits().isEmpty() || !n.getNextHitBars().isEmpty()) {
            applyHitMask(n, data);
        }
    }

    private void applyTargetInformationMask(NPC n, OutputStream data) {
        data.writeByte(1);
        data.writeByte(3);
        data.writeInt(n.getHitpoints());
        data.write24BitIntegerV1(n.getMaxHitpoints());
    }

    private void applyTargetReticuleMask(NPC n, OutputStream data) {
        Graphics reticle = player.getCombatDefinitions().getTargetReticule(n);
        data.writeShortLE128(reticle.getId());
        data.writeInt(reticle.getSettingsHash());
        data.writeByteC(reticle.getSettings2Hash());
    }

    @SuppressWarnings("unused")
    private void applySecondBarMask(NPC n, OutputStream data) {
        data.writeShortLE128(n.getNextSecondaryBar().getTotalUnits());
        data.writeByte(n.getNextSecondaryBar().getBeginningOffset());
        data.write128Byte(n.getNextSecondaryBar().getIncrementalUnits());
    }

    private void applyIconMask(NPC n, OutputStream data) {
        HeadIcon[] icons = n.getIcons();
        int mask = 0;
        for (int i = 0; i < 8; i++)
            mask |= 1 << i;
        data.writeByte128(mask);
        for(int i=0;i<8;i++) {
            HeadIcon icon = i>= icons.length ? null :  icons[i];
            data.writeBigSmart(icon == null ?  (player.isUsingNXT() ? 0 : -1) : icon.getSpriteId());
            data.writeSmart(icon == null ? 0 : icon.getFileId() + 1);
        }
    }

    private void applyForceMovementMask(NPC n, OutputStream data) {
        data.writeByte128(n.getNextForceMovement().getToFirstTile().getX() - n.getX());
        data.writeByte(n.getNextForceMovement().getToFirstTile().getY() - n.getY());
        data.writeByte128(n.getNextForceMovement().getToSecondTile() == null ? 0 : n.getNextForceMovement().getToSecondTile().getX() - n.getX());
        data.writeByte128(n.getNextForceMovement().getToSecondTile() == null ? 0 : n.getNextForceMovement().getToSecondTile().getY() - n.getY());
        data.writeByte(0);
        data.writeByte128(0);
        int firstTickDelay = (n.getNextForceMovement().getFirstTileTicketDelay() * 600) / 20;
        int secondTickDelay = n.getNextForceMovement().getToSecondTile() == null ? 0 : ((n.getNextForceMovement().getSecondTileTicketDelay() * 600) / 20);
        if (secondTickDelay - firstTickDelay == 0)
            secondTickDelay += 1;
        data.writeShort(firstTickDelay);
        data.writeShort(secondTickDelay);
        data.writeShortLE128(n.getNextForceMovement().getDirection());
    }

    private void applyChangeLevelMask(NPC n, OutputStream data) {
        data.writeShort128(n.getCombatLevel());
    }

    private void applyNameChangeMask(NPC npc, OutputStream data) {
        data.writeString(npc.getName());
    }

    private void applyTransformationMask(NPC n, OutputStream data) {
        data.writeBigSmart(n.getNextTransformation().getToNPCId());
    }

    private void applyModelsMask(NPC n, OutputStream data) {
        data.write128Byte(2);
        data.writeShort(0);
        data.write128Byte(n.getChangedModels().length);
        for (int models : n.getChangedModels()) {
            data.writeBigSmart(models);
        }
    }

    private void applyForceTalkMask(NPC n, OutputStream data) {
        data.writeString(n.getNextForceTalk().getText());
    }

    private void applyFaceWorldTileMask(NPC n, OutputStream data) {
        data.writeShort((n.getNextFaceWorldTile().getX() << 1) + 1);
        data.writeShort128((n.getNextFaceWorldTile().getY() << 1) + 1);
    }

    private void applyHitMask(NPC n, OutputStream data) {
        int hitCount = n.getNextHits().size();
        data.writeByteC(hitCount);
        if (hitCount > 0) {
            for (Hit hit : n.getNextHits()) {
                boolean interactingWith = hit.interactingWith(player, n);
                if (hit.getDamage() < 0) {
                    hit.setDamage(0);
                }
                // Ensure minimum display value of 10 if NPC is alive
                int displayDamage = n.getHitpoints() > 0 ? Math.max(10, hit.getDamageDisplay(player)) : hit.getDamageDisplay(player);
                if (hit.missed() && !interactingWith) {
                    data.writeSmart(32766);
                    data.write128Byte(displayDamage / 10);
                } else {
                    if (hit.getSoaking() != null) {
                        data.writeSmart(32767);
                        data.writeSmart(hit.getMark(player, n));
                        data.writeSmart(Math.min(Math.max(1, displayDamage / 10), Short.MAX_VALUE));
                        data.writeSmart(hit.getSoaking().getMark(player, n));
                        data.writeSmart(Math.min(Math.max(1, hit.getSoaking().getDamageDisplay(player) / 10), Short.MAX_VALUE));
                    } else {
                        data.writeSmart(hit.getMark(player, n));
                        data.writeSmart(Math.min(Math.max(1, displayDamage / 10), Short.MAX_VALUE));
                    }
                }
                data.writeSmart(hit.getDelay());
            }
        }

        data.writeByteC(n.getNextHitBars().size());
        for (HitBar bar : n.getNextHitBars()) {
            data.writeSmart(bar.getType());
            int perc = bar.getPercentage();
            int toPerc = bar.getToPercentage();
            boolean display = bar.display(player);

            // ? Clamp literal display value to at least 10 if NPC is alive and HP < 10
            if (display && n.getHitpoints() > 0 && n.getHitpoints() < 10) {
                perc = 10;
                toPerc = 10;
            }

            // Keep the previous 1% clamping for smooth animation logic
            if (display && n.getHitpoints() > 0) {
                perc = Math.max(perc, 1);
                toPerc = Math.max(toPerc, 1);
            }

            data.writeSmart(display ? perc != toPerc ? 1 : 0 : 32767);
            if (display) {
                data.writeSmart(bar.getDelay());
                data.writeByte(perc);
                if (toPerc != perc)
                    data.write128Byte(toPerc);
            }
        }
    }



    private void applyFaceEntityMask(NPC n, OutputStream data) {
        data.writeShortLE(n.getNextFaceEntity() == -2 ? n.getLastFaceEntity() : n.getNextFaceEntity());
    }

    private void applyAnimationMask(NPC n, OutputStream data) {
        int count = 0;
        for (int id : n.getNextAnimation().getIds()) {
            if (count == 4)
                break;
            data.writeBigSmart(id);
            count++;
        }
        data.writeByte128(n.getNextAnimation().getSpeed());
    }

    private void applyGraphicsMask4(NPC n, OutputStream data) {
        data.writeShortLE128(n.getNextGraphics4().getId());
        data.writeInt(n.getNextGraphics4().getSettingsHash());
        data.writeByte128(n.getNextGraphics4().getSettings2Hash());
    }

    private void applyGraphicsMask3(NPC n, OutputStream data) {
        data.writeShortLE(n.getNextGraphics3().getId());
        data.writeIntV2(n.getNextGraphics3().getSettingsHash());
        data.writeByteC(n.getNextGraphics3().getSettings2Hash());
    }

    private void applyGraphicsMask2(NPC n, OutputStream data) {
        data.writeShort128(n.getNextGraphics2().getId());
        data.writeIntV1(n.getNextGraphics2().getSettingsHash());
        data.write128Byte(n.getNextGraphics2().getSettings2Hash());
    }

    private void applyGraphicsMask1(NPC n, OutputStream data) {
        data.writeShortLE128(n.getNextGraphics1().getId());
        data.writeInt(n.getNextGraphics1().getSettingsHash());
        data.write128Byte(n.getNextGraphics1().getSettings2Hash());
    }

    private void applyRenderAnimation(NPC n, OutputStream data) {
        data.writeShortLE128(n.getNextRenderAnimation());
    }

    private void applyHideFamiliarOptionsMask(NPC n, OutputStream data) {
        if (n.getDefinitions() == null)
            return;
        String[] options = n.getDefinitions().menuOptions;
        if (options == null)
            return;
        int maskValue = 0;
        for (int i = 0; i < options.length; i++)
            maskValue |= (options[i] != null && !options[i].equalsIgnoreCase("examine") ? 1 : 0) << i;
        data.writeByteC(player.isHideFamiliarOptions() ? maskValue : 0);
    }

}
