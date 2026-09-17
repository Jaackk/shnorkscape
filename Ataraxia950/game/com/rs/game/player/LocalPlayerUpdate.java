package com.rs.game.player;

import java.security.MessageDigest;

import com.rs.Settings;
import com.rs.game.Colour;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.hitbar.HitBar;
import com.rs.game.player.content.dungeoneering.DungeonPartyManager;
import com.rs.network.codec.ProtocolSet;
import com.rs.network.io.OutputStream;

public final class LocalPlayerUpdate {

    /**
     * The maximum amount of local players being added per tick. This is to decrease
     * time it takes to load crowded places (such as home).
     */
    private static final int MAX_PLAYER_ADD = 50;

    private final Player player;

    private final byte[] slotFlags;

    private final Player[] localPlayers;
    private final int[] localPlayersIndexes;
    private int localPlayersIndexesCount;

    private final int[] outPlayersIndexes;
    private int outPlayersIndexesCount;

    private final int[] regionHashes;

    private final byte[][] cachedAppearencesHashes;
    private final byte[][] cachedIconsHashes;
    private int totalRenderDataSentLength;

    /**
     * The amount of local players added this tick.
     */
    private int localAddedPlayers;

    public Player[] getLocalPlayers() {
        return localPlayers;
    }

    public int getLocalPlayersIndexesCount() {
        return localPlayersIndexesCount;
    }

    public int[] getLocalPlayersIndexes() {
        return localPlayersIndexes;
    }

    public boolean needAppearenceUpdate(int index, byte[] hash) {
        if (totalRenderDataSentLength > ((ProtocolSet.PROTOCOL_LIMIT - 500) / 2) || hash == null)
            return false;
        return cachedAppearencesHashes[index] == null || !MessageDigest.isEqual(cachedAppearencesHashes[index], hash);
    }

    public boolean needIconsUpdate(int index, byte[] hash) {
        if (totalRenderDataSentLength > ((ProtocolSet.PROTOCOL_LIMIT - 500) / 2) || hash == null)
            return false;
        return cachedIconsHashes[index] == null || !MessageDigest.isEqual(cachedIconsHashes[index], hash);
    }

    public LocalPlayerUpdate(Player player) {
        this.player = player;
        slotFlags = new byte[2048];
        localPlayers = new Player[2048];
        localPlayersIndexes = new int[Settings.SV_PLAYERS_LIMIT];
        outPlayersIndexes = new int[2048];
        regionHashes = new int[2048];
        cachedAppearencesHashes = new byte[Settings.SV_PLAYERS_LIMIT][];
        cachedIconsHashes = new byte[Settings.SV_PLAYERS_LIMIT][];
    }

    public void init(OutputStream buffer) {
        buffer.initBitAccess();
        buffer.writeBits(30, player.getTileHash());
        localPlayers[player.getIndex()] = player;
        localPlayersIndexes[localPlayersIndexesCount++] = player.getIndex();
        for (int playerIndex = 1; playerIndex < 2048; playerIndex++) {
            if (playerIndex == player.getIndex())
                continue;
            Player player = World.getPlayers().get(playerIndex);
            buffer.writeBits(18, regionHashes[playerIndex] = player == null ? 0 : player.getRegionHash());
            outPlayersIndexes[outPlayersIndexesCount++] = playerIndex;
        }
        buffer.finishBitAccess();
    }

    private boolean needsRemove(Player p) {
        DungeonPartyManager pt = player.getDungeoneeringManager().getParty();
        if (pt != null && pt.getDungeon() != null && pt.getTeam().contains(p))
            return false;
        return p != player && (/*!p.getCutscenesManager().showYourselfToOthers() ||*/ p.hasFinished() || !(player.withinDistance(p, 24)
                && /*
                     * (player. hasLargeSceneView () ||
                     */player.getMapRegionsIds().contains(p.getRegionId())));
    }

    private boolean needsAdd(Player p) {
        DungeonPartyManager pt = player.getDungeoneeringManager().getParty();
        if (pt != null && pt.getDungeon() != null && pt.getTeam().contains(p))
            return true;
        return p != null && /*p.getCutscenesManager().showYourselfToOthers() &&*/ p.isRunning() && (player.withinDistance(p, player.hasLargeSceneView() ? 182 : 24)
                && /*
                     * ( player . hasLargeSceneView ( ) ||
                     */player.getMapRegionsIds().contains(p.getRegionId())) && localAddedPlayers < MAX_PLAYER_ADD && p.clientHasLoadedMapRegion();
   }

    private void updateRegionHash(OutputStream stream, int lastRegionHash, int currentRegionHash, int currentMovementType) {
        int lastRegionX = lastRegionHash >> 8;
        int lastRegionY = 0xff & lastRegionHash;
        int lastPlane = lastRegionHash >> 16;
        int currentRegionX = currentRegionHash >> 8;
        int currentRegionY = 0xff & currentRegionHash;
        int currentPlane = currentRegionHash >> 16;
        int planeOffset = currentPlane - lastPlane;
        if (lastRegionX == currentRegionX && lastRegionY == currentRegionY) {
            stream.writeBits(2, 1);
            stream.writeBits(2, planeOffset);
        } else if (Math.abs(currentRegionX - lastRegionX) <= 1 && Math.abs(currentRegionY - lastRegionY) <= 1) {
            int opcode;
            int dx = currentRegionX - lastRegionX;
            int dy = currentRegionY - lastRegionY;
            if (dx == -1 && dy == -1)
                opcode = 0;
            else if (dx == 1 && dy == -1)
                opcode = 2;
            else if (dx == -1 && dy == 1)
                opcode = 5;
            else if (dx == 1 && dy == 1)
                opcode = 7;
            else if (dy == -1)
                opcode = 1;
            else if (dx == -1)
                opcode = 3;
            else if (dx == 1)
                opcode = 4;
            else
                opcode = 6;
            stream.writeBits(2, 2);
            stream.writeBits(5, (planeOffset << 3) + (opcode & 0x7));
        } else {
            int xOffset = currentRegionX - lastRegionX;
            int yOffset = currentRegionY - lastRegionY;
            stream.writeBits(2, 3);
            stream.writeBits(20, (yOffset & 0xff) + ((xOffset & 0xff) << 8) + (planeOffset << 16) + (currentMovementType << 18));
        }
    }

    private void processOutsidePlayers(OutputStream stream, OutputStream updateBlockData, boolean nsn2) {
        stream.initBitAccess();
        int skip = 0;
        localAddedPlayers = 0;
        for (int i = 0; i < outPlayersIndexesCount; i++) {
            if(i >= outPlayersIndexes.length)
                continue;
            int playerIndex = outPlayersIndexes[i];

            if (nsn2 == ((0x1 & slotFlags[playerIndex]) == 0))
                continue;

            if (skip > 0) {
                skip--;
                slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
                continue;
            }
            Player p = World.getPlayers().get(playerIndex);
            if (needsAdd(p)) {
                stream.writeBits(1, 1);
                stream.writeBits(2, 0); // request add
                int hash = p.getRegionHash();
                if (hash == regionHashes[playerIndex])
                    stream.writeBits(1, 0);
                else {
                    stream.writeBits(1, 1);
                    updateRegionHash(stream, regionHashes[playerIndex], hash, p.getMovementType());
                    regionHashes[playerIndex] = hash;
                }
                stream.writeBits(6, p.getXInRegion());
                stream.writeBits(6, p.getYInRegion());
                boolean needAppearenceUpdate = needAppearenceUpdate(p.getIndex(), p.getAppearence().getMD5AppeareanceDataHash(player));
                boolean needIconsUpdate = needIconsUpdate(p.getIndex(), p.getAppearence().getMd5IconsDataHash());
                appendUpdateBlock(p, updateBlockData, needAppearenceUpdate, needIconsUpdate, true);
                stream.writeBits(1, 1);
                localAddedPlayers++;
                localPlayers[p.getIndex()] = p;
                slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
            } else {
                // no need to update hash if player not near
                /*
                 * int hash = p == null ? regionHashes[playerIndex] :
                 * p.getRegionHash(); if (p != null && hash !=
                 * regionHashes[playerIndex]) { stream.writeBits(1, 1);
                 * updateRegionHash(stream, regionHashes[playerIndex], hash, p);
                 * regionHashes[playerIndex] = hash; } else {
                 */
                stream.writeBits(1, 0); // no update needed
                for (int i2 = i + 1; i2 < outPlayersIndexesCount; i2++) {
                    if(i2 >= outPlayersIndexes.length)
                        continue;
                    int p2Index = outPlayersIndexes[i2];

                    if (nsn2 == ((0x1 & slotFlags[p2Index]) == 0))
                        continue;

                    Player p2 = World.getPlayers().get(p2Index);
                    if (needsAdd(p2) || (p2 != null && p2.getRegionHash() != regionHashes[p2Index]))
                        break;
                    skip++;
                }
                skipPlayers(stream, skip);
                slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
                // }
            }
        }
        stream.finishBitAccess();
    }

    private void processLocalPlayers(OutputStream stream, OutputStream updateBlockData, boolean nsn0) {
        stream.initBitAccess();
        int skip = 0;
        for (int i = 0; i < localPlayersIndexesCount; i++) {
            int playerIndex = localPlayersIndexes[i];

            if (nsn0 == ((0x1 & slotFlags[playerIndex]) != 0))
                continue;

            if (skip > 0) {
                skip--;
                slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
                continue;
            }
            Player p = localPlayers[playerIndex];
            if (needsRemove(p)) {
                stream.writeBits(1, 1); // needs update
                stream.writeBits(1, 0); // no masks update needeed
                stream.writeBits(2, 0); // request remove
                regionHashes[playerIndex] = p.getLastWorldTile() == null ? p.getRegionHash() : p.getLastWorldTile().getRegionHash();
                int hash = p.getRegionHash();
                if (hash == regionHashes[playerIndex])
                    stream.writeBits(1, 0);
                else {
                    stream.writeBits(1, 1);
                    updateRegionHash(stream, regionHashes[playerIndex], hash, p.getMovementType());
                    regionHashes[playerIndex] = hash;
                    regionHashes[playerIndex] = hash;
                }
                localPlayers[playerIndex] = null;
            } else {
                boolean needAppearenceUpdate = needAppearenceUpdate(p.getIndex(), p.getAppearence().getMD5AppeareanceDataHash(player));
                boolean needIconsUpdate = needIconsUpdate(p.getIndex(), p.getAppearence().getMd5IconsDataHash());
                boolean needUpdate = p.needMasksUpdate() || needAppearenceUpdate || needIconsUpdate || player.getCombatDefinitions().isNeedTargetReticuleUpdate(p);
                if (needUpdate)
                    appendUpdateBlock(p, updateBlockData, needAppearenceUpdate, needIconsUpdate, false);
                if (p.hasTeleported() || p.getNextWalkDirection() != -1) {
                    stream.writeBits(1, 1); // needs update
                    stream.writeBits(1, needUpdate ? 1 : 0);
                    stream.writeBits(2, 3);
                    int xOffset = p.getX() - p.getLastWorldTile().getX();
                    int yOffset = p.getY() - p.getLastWorldTile().getY();
                    int planeOffset = p.getPlane() - p.getLastWorldTile().getPlane();
                    int movementType = p.hasTeleported() ? 4 : p.getMovementType();
                    if (Math.abs(p.getX() - p.getLastWorldTile().getX()) < 16 // 14
                            && Math.abs(p.getY() - p.getLastWorldTile().getY()) < 16) { // 14
                        stream.writeBits(1, 0);
                        if (xOffset < 0) // viewport used to be 15 now 16
                            xOffset += 32;
                        if (yOffset < 0)
                            yOffset += 32;
                        stream.writeBits(15, yOffset + (xOffset << 5) + ((planeOffset & 0x3) << 10) | movementType << 12);// 4
                                                                                                                          // forces
                                                                                                                          // setmovementtype
                                                                                                                          // to
                                                                                                                          // teleport
                                                                                                                          // for
                                                                                                                          // next
                                                                                                                          // step
                    } else {
                        stream.writeBits(1, 1);
                        stream.writeBits(3, movementType); // 4 forces
                                                           // setmovementtype
                                                           // to teleport for
                                                           // next step
                        stream.writeBits(30, (yOffset & 0x3fff) + ((xOffset & 0x3fff) << 14) + ((planeOffset & 0x3) << 28));
                    }
                    // not needed as teleport handles walk aswell
                    /*
                     * } else if (p.getNextWalkDirection() != -1) { int dx =
                     * Utils.DIRECTION_DELTA_X[p.getNextWalkDirection()]; int dy =
                     * Utils.DIRECTION_DELTA_Y[p.getNextWalkDirection()]; int opcode; if
                     * (p.getNextRunDirection() != -1) { dx +=
                     * Utils.DIRECTION_DELTA_X[p.getNextRunDirection()]; dy +=
                     * Utils.DIRECTION_DELTA_Y[p.getNextRunDirection()]; opcode =
                     * Utils.getPlayerRunningDirection(dx, dy); }else { opcode =
                     * Utils.getPlayerWalkingDirection(dx, dy); } stream.writeBits(1, 1); if ((dx ==
                     * 0 && dy == 0)) { stream.writeBits(1, needUpdate ? 1 : 0);
                     * stream.writeBits(2,p.getNextRunDirection() != -1 ? 2 : 1);
                     * stream.writeBits(p.getNextRunDirection() != -1 ? 4 : 3, opcode);
                     * if(p.getNextRunDirection() == -1) stream.writeBits(1, 0); }
                     */
                } else if (needUpdate) {
                    stream.writeBits(1, 1); // needs update
                    stream.writeBits(1, 1);
                    stream.writeBits(2, 0);
                } else { // skip
                    stream.writeBits(1, 0); // no update needed
                    for (int i2 = i + 1; i2 < localPlayersIndexesCount; i2++) {
                        int p2Index = localPlayersIndexes[i2];
                        if (nsn0 == ((0x1 & slotFlags[p2Index]) != 0))
                            continue;
                        Player p2 = localPlayers[p2Index];
                        if (needsRemove(p2) || p2.hasTeleported() || p2.getNextWalkDirection() != -1 || (p2.needMasksUpdate() || needAppearenceUpdate(p2.getIndex(), p2.getAppearence().getMD5AppeareanceDataHash(player)) || needIconsUpdate(p2.getIndex(), p2.getAppearence().getMd5IconsDataHash())))
                            break;
                        skip++;
                    }
                    skipPlayers(stream, skip);
                    slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
                }

            }
        }
        stream.finishBitAccess();
    }

    private void skipPlayers(OutputStream stream, int amount) {
        stream.writeBits(2, amount == 0 ? 0 : amount > 255 ? 3 : (amount > 31 ? 2 : 1));
        if (amount > 0)
            stream.writeBits(amount > 255 ? 11 : (amount > 31 ? 8 : 5), amount);
    }

    private void appendUpdateBlock(Player p, OutputStream data, boolean needAppearenceUpdate, boolean needIconsUpdate, boolean added) {
        int maskData = 0;
        if (needIconsUpdate)
            maskData |= 0x800;
        if (p.getNextFaceEntity() != -2 || added && p.getLastFaceEntity() != -1)
            maskData |= 0x1;
        if (p.getNextColour() != null)
            maskData |= 0x800000;
        if (needAppearenceUpdate)
            maskData |= 0x4;
        if (!p.getNextHits().isEmpty() || !p.getNextHitBars().isEmpty())
            maskData |= 0x20;
        if (p.getNextForceTalk() != null)
            maskData |= 0x200;
        if (p.getNextAnimation() != null)
            maskData |= 0x80;
        if (p.getNextGraphics1() != null)
            maskData |= 0x2;
        if (p.getNextGraphics2() != null)
            maskData |= 0x100;
        if (p.getNextForceMovement() != null)
            maskData |= 0x8;
        if ((added || p.getNextFaceWorldTile() != null) && (p.getNextRunDirection() == -1 && p.getNextWalkDirection() == -1 && p.getNextForceMovement() == null && p.getNextFaceEntity() < 0 && !(added && p.getLastFaceEntity() != -1)))
            maskData |= 0x10;
        if (p.isRefreshClanIcon() || added)
            maskData |= 0x10000;
        if (p.getNextGraphics3() != null)
            maskData |= 0x2000;
        if (player.getCombatDefinitions().isNeedTargetReticuleUpdate(p))
            maskData |= 0x100000;
        if (p.getNextGraphics4() != null)
            maskData |= 0x400000;

        if (maskData >= 0xff)
            maskData |= 0x40;
        if (maskData >= 0xffff)
            maskData |= 0x1000;

        data.writeShort(0); // rs doesnt use this lol
        data.writeByte(maskData);
        if (maskData >= 0xff)
            data.writeByte(maskData >> 8);
        if (maskData >= 0xffff)
            data.writeByte(maskData >> 16);

        if (needIconsUpdate)
            applyIconsMask(p, data);
        if (p.getNextFaceEntity() != -2 || added && p.getLastFaceEntity() != -1)
            applyFaceEntityMask(p, data);
        if (p.getNextColour() != null)
            applyColourMask(p, data);
        if (needAppearenceUpdate)
            applyAppearanceMask(p, data);
        if (!p.getNextHits().isEmpty() || !p.getNextHitBars().isEmpty())
            applyHitsMask(p, data);
        if (p.getNextForceTalk() != null)
            applyForceTalkMask(p, data);
        if (p.getNextAnimation() != null)
            applyAnimationMask(p, data);
        if (p.getNextGraphics1() != null)
            applyGraphicsMask1(p, data);
        if (p.getNextGraphics2() != null)
            applyGraphicsMask2(p, data);
        if (p.getNextForceMovement() != null)
            applyForceMovementMask(p, data);
        if ((added || p.getNextFaceWorldTile() != null) && (p.getNextRunDirection() == -1 && p.getNextWalkDirection() == -1 && p.getNextForceMovement() == null && p.getNextFaceEntity() < 0 && !(added && p.getLastFaceEntity() != -1)))
            applyFaceDirectionMask(p, data);
        if (p.isRefreshClanIcon() || added)
            applyClanMemberMask(p, data);
        if (p.getNextGraphics3() != null)
            applyGraphicsMask3(p, data);
        if (player.getCombatDefinitions().isNeedTargetReticuleUpdate(p))
            applyTargetReticuleMask(p, data);
        if (p.getNextGraphics4() != null)
            applyGraphicsMask4(p, data);

    }

    private void applyTargetReticuleMask(Player p, OutputStream data) {
        Graphics reticle = player.getCombatDefinitions().getTargetReticule(p);
        data.writeShort128(reticle.getId());
        data.writeIntV1(reticle.getSettingsHash());
        data.writeByteC(reticle.getSettings2Hash());
    }

    private void applyClanMemberMask(Player p, OutputStream data) {
        data.writeByte((player.getClanManager() != null && player.getClanManager().isMemberOnline(p)) ? 1 : 0);
    }

    private void applyIconsMask(Player p, OutputStream data) {
        byte[] renderData = p.getAppearence().getIconsData();
        totalRenderDataSentLength += renderData.length;
        cachedIconsHashes[p.getIndex()] = p.getAppearence().getMd5IconsDataHash();
        data.writeByte128(renderData.length);
        data.writeBytes(renderData);
    }

    private void applyColourMask(Player p, OutputStream data) {
        Colour color = p.getNextColour();
        int colours = color.getColours();
        data.write128Byte(colours & 0xFF);
        data.writeByteC(colours >> 8 & 0xFF);
        data.write128Byte(colours >> 16 & 0xFF);
        data.writeByte(colours >> 24 & 0xFF);
        data.writeShort(color.getDelay());
        data.writeShort(color.getDuration());
    }

    private void applyForceTalkMask(Player p, OutputStream data) {
        data.writeString(p.getNextForceTalk().getText());
    }

    private void applyHitsMask(Player p, OutputStream data) {
        int count = p.getNextHits().size();
        data.write128Byte(count);
        for (Hit hit : p.getNextHits()) {
            if (hit.getDamage() < 0)
                hit.setDamage(0);
            boolean interactingWith = hit.interactingWith(player, p);
            if (hit.missed() && !interactingWith) {
                data.writeSmart(32766);
                data.writeByteC(hit.getDamageDisplay(player) / 10);
            } else {
                if (hit.getSoaking() != null) {
                    data.writeSmart(32767);
                    data.writeSmart(hit.getMark(player, p));
                    data.writeSmart(Math.min(Math.max(1, hit.getDamageDisplay(player) / 10), Short.MAX_VALUE - 1));
                    data.writeSmart(hit.getSoaking().getMark(player, p));
                    data.writeSmart(Math.min(Math.max(1, hit.getSoaking().getDamageDisplay(player) / 10), Short.MAX_VALUE - 1));
                } else {
                    data.writeSmart(hit.getMark(player, p));
                    data.writeSmart(Math.min(Math.max(1, hit.getDamageDisplay(player) / 10), Short.MAX_VALUE - 1));
                }
            }
            data.writeSmart(hit.getDelay());
        }
        data.write128Byte(p.getNextHitBars().size());
        for (HitBar bar : p.getNextHitBars()) {
            data.writeSmart(bar.getType());
            int perc = bar.getPercentage();
            int toPerc = bar.getToPercentage();
            boolean display = bar.display(player);
            data.writeSmart(display ? perc != toPerc ? 1 : 0 : 32767);
            if (display) {
                data.writeSmart(bar.getDelay());
                data.write128Byte(perc);
                if (toPerc != perc)
                    data.writeByte(toPerc);
            }
        }
    }

    private void applyFaceEntityMask(Player p, OutputStream data) {
        data.writeShortLE128(p.getNextFaceEntity() == -2 ? p.getLastFaceEntity() : p.getNextFaceEntity());
    }

    private void applyFaceDirectionMask(Player p, OutputStream data) {
        data.writeShort128(p.getDirection());
    }

    private void applyGraphicsMask1(Player p, OutputStream data) {
        data.writeShortLE128(p.getNextGraphics1().getId());
        data.writeIntV1(p.getNextGraphics1().getSettingsHash());
        data.write128Byte(p.getNextGraphics1().getSettings2Hash());
    }

    private void applyGraphicsMask2(Player p, OutputStream data) {
        data.writeShort(p.getNextGraphics2().getId());
        data.writeIntLE(p.getNextGraphics2().getSettingsHash());
        data.writeByte(p.getNextGraphics2().getSettings2Hash());
    }

    private void applyGraphicsMask3(Player p, OutputStream data) {
        data.writeShort(p.getNextGraphics3().getId());
        data.writeIntV1(p.getNextGraphics3().getSettingsHash());
        data.writeByteC(p.getNextGraphics3().getSettings2Hash());
    }

    private void applyGraphicsMask4(Player p, OutputStream data) {
        data.writeShort128(p.getNextGraphics4().getId());
        data.writeIntLE(p.getNextGraphics4().getSettingsHash());
        data.writeByte(p.getNextGraphics4().getSettings2Hash());
    }

    private void applyAnimationMask(Player p, OutputStream data) {
        int count = 0;
        for (int id : p.getNextAnimation().getIds()) {
            if (count == 4)
                break;
            data.writeBigSmart(id);
            count++;
        }
        data.write128Byte(p.getNextAnimation().getSpeed());
    }

    private void applyAppearanceMask(Player p, OutputStream data) {
        byte[] renderData = p.getAppearence().getAppeareanceData(player);
        totalRenderDataSentLength += renderData.length;
        cachedAppearencesHashes[p.getIndex()] = p.getAppearence().getMD5AppeareanceDataHash(player);
        data.writeByteC(renderData.length);
        data.writeBytes(renderData);
    }

    private void applyForceMovementMask(Player p, OutputStream data) {
        if (p.getNextForceMovement().preciseMovement()) {
            applyPreciseForceMovementMask(p, data);
            return;
        }
        data.write128Byte(p.getNextForceMovement().getToFirstTile().getX() - p.getX());
        data.write128Byte(p.getNextForceMovement().getToFirstTile().getY() - p.getY());
        data.writeByte(p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getToSecondTile().getX() - p.getX());
        data.writeByte128(p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getToSecondTile().getY() - p.getY());
        data.write128Byte(0);
        data.writeByte(0);
        int firstTickDelay = p.getNextForceMovement().getFirstTileTicketDelay() * 30;
        int secondTickDelay = p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getSecondTileTicketDelay() * 30;
        if (secondTickDelay - firstTickDelay == 0)
            secondTickDelay +=1;
        data.writeShort(firstTickDelay);
        data.writeShortLE(secondTickDelay);
        data.writeShortLE(p.getNextForceMovement().getDirection());
    }

    private void applyPreciseForceMovementMask(Player p, OutputStream data) {
        data.write128Byte(p.getNextForceMovement().getToFirstTile().getX() - p.getX());
        data.write128Byte(p.getNextForceMovement().getToFirstTile().getY() - p.getY());
        data.writeByte(p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getToSecondTile().getX() - p.getX());
        data.writeByte128(p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getToSecondTile().getY() - p.getY());
        data.write128Byte(0);
        data.writeByte(0);
        int firstTickDelay = p.getNextForceMovement().getFirstTileTicketDelay();
        int secondTickDelay = p.getNextForceMovement().getToSecondTile() == null ? 0 : p.getNextForceMovement().getSecondTileTicketDelay();
        if (secondTickDelay - firstTickDelay == 0)
            secondTickDelay +=1;
        data.writeShort(firstTickDelay);
        data.writeShortLE(secondTickDelay);
        data.writeShortLE(p.getNextForceMovement().getDirection());
    }

    public OutputStream createPacketAndProcess() {
        OutputStream buffer = new OutputStream();
        OutputStream updateBlockData = new OutputStream();
        buffer.writePacketVarShort(player, 122);
        processLocalPlayers(buffer, updateBlockData, true);
        processLocalPlayers(buffer, updateBlockData, false);
        processOutsidePlayers(buffer, updateBlockData, true);
        processOutsidePlayers(buffer, updateBlockData, false);
        buffer.writeBytes(updateBlockData.getBuffer(), 0, updateBlockData.getOffset());
        buffer.endPacketVarShort();
        totalRenderDataSentLength = 0;
        localPlayersIndexesCount = 0;
        outPlayersIndexesCount = 0;
        for (int playerIndex = 1; playerIndex < 2048; playerIndex++) {
            slotFlags[playerIndex] >>= 1;
            Player player = localPlayers[playerIndex];
            if (player == null)
                outPlayersIndexes[outPlayersIndexesCount++] = playerIndex;
            else
                localPlayersIndexes[localPlayersIndexesCount++] = playerIndex;
        }
        return buffer;
    }

}
