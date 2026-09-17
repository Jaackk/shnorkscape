package com.rs.game.player.actions.firemaking.defs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Log.java | 11:11:55 AM
 *
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum Log {
    NORMAL(1511, 1, 300, 70755, 40, 3098, 6),
    PROTEAN(34528, 1, 900, 96599, 0, 3102, 66),
    ACHEY(2862, 1, 300, 70756, 40, 3098, -1),
    OAK(1521, 15, 450, 70757, 60, 3099, 12),
    WILLOW(1519, 30, 450, 70758, 90, 3101, 18),
    TEAK(6333, 35, 450, 70759, 105, 3099, 25),
    ARCTIC_PINE(10810, 42, 500, 70760, 125, 3099, 30),
    MAPLE(1517, 45, 500, 70761, 135, 3100, 36),
    MAHOGANY(6332, 50, 700, 70762, 157.5, 3099, 40),
    EUCALYPTUS(12581, 58, 700, 70763, 193.5, 3099, 49),
    YEW(1515, 60, 800, 70764, 202.5, 3111, 54),
    MAGIC(1513, 75, 900, 70765, 303.8, 3135, 60),
    CORRUPTED_MAGIC(40338, 75, 900, 70765, 319.0, 3135, 30),
    CURSED_MAGIC(13567, 82, 1000, 70766, 303.8, 3137, 62),
    ELDER(29556, 90, 1100, 70765, 403.5, 3136, 75),
    EVIL_BARK(3239, 95, 1300, 70769, 580.5, 3136, 120);


    public static final ImmutableMap<Integer, Log> ID_TO_LOG;
    public static final ImmutableList<Log> VALUES = ImmutableList.copyOf(values());
    private final int logId;
    private final int level;
    private final int life;
    private final int fireId;
    private final double xp;
    private final int gfxId;
    private final int boostTime;

    static {
        Map<Integer, Log> idToLog = new HashMap<>();
        for (Log log : values()) {
            idToLog.put(log.logId, log);
        }
        ID_TO_LOG = ImmutableMap.copyOf(idToLog);
    }

    Log(int logId, int level, int life, int fireId, double xp, int bonfireGraphicId, int boostTime) {
        this.logId = logId;
        this.level = level;
        this.life = life;
        this.fireId = fireId;
        this.xp = xp;
        this.gfxId = bonfireGraphicId;
        this.boostTime = boostTime;
    }

    public static Log forId(int logsId) {
        return ID_TO_LOG.get(logsId);
    }

    public double getExperience() {
        return xp;
    }

    public int getFireId() {
        return fireId;
    }

    public int getLevel() {
        return level;
    }

    public int getLife() {
        return (life * 600);
    }

    public int getLogId() {
        return logId;
    }

    public int getBonfireGFX() {
        return gfxId;
    }

    public int getBoostTime() {
        return boostTime;
    }
    }