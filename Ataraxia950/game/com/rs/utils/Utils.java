package com.rs.utils;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.google.common.collect.Range;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.WorldThread;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import lombok.val;

public final class Utils {

    public static final Random RANDOM = new Random();
    public static final byte[] DIRECTION_DELTA_X = new byte[]{-1, 0, 1, -1, 1, -1, 0, 1};
    public static final byte[] DIRECTION_DELTA_Y = new byte[]{1, 1, 1, 0, 0, -1, -1, -1};
    public static final char[] VALID_CHARS = {'_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9'};
    public static final int[] ROTATION_DIR_X = {-1, 0, 1, 0};
    public static final int[] ROTATION_DIR_Y = {0, 1, 0, -1};
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Object ALGORITHM_LOCK = new Object();
    public static char[] aCharArray6385 = {'\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021',
            '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c',
            '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\0', '\u017e',
            '\u0178'};
    private static long timeCorrection;
    private static long lastTimeUpdate;
    public static final byte[][] DIRS = new byte[][]{{0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
            {1, 0}, {1, -1}};

    public static String capitalize(String str) {
        if (!str.isEmpty()) {
            char capital = Character.toUpperCase(str.charAt(0));
            return capital + str.substring(1);
        }
        return str;
    }

    public static String prettyDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours());
        if (hours <= 0 && minutes <= 0) {
            return "1m";
        } else if (hours <= 0) {
            return String.format("%sm", minutes);
        } else {
            return String.format("%sh %sm", hours, minutes);
        }
    }

    public static int secondsToTicks(long secs) {
        double ticks = (secs * 1000) / 600;
        ticks = Math.floor(ticks);
        if (ticks < 1.0) {
            ticks = 1.0;
        }
        return (int) ticks;
    }

    public static int msToTicks(long ms) {
        double ticks = ms / 600;
        ticks = Math.floor(ticks);
        if (ticks < 1.0) {
            ticks = 1.0;
        }
        return (int) ticks;
    }

    public static void createFileIfNotExists(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static String formatTime(final long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;
        final StringBuilder string = new StringBuilder();
        string.append(hours > 9 ? hours : ("0" + hours));
        string.append(":" + (minutes > 9 ? minutes : ("0" + minutes)));
        string.append(":" + (seconds > 9 ? seconds : ("0" + seconds)));
        return string.toString();
    }

    public static final void shuffleIntegerArray(final int[] array) {
        final Random rnd = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            final int index = rnd.nextInt(i + 1);
            final int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    /**
     * Finds the first value in the array that matches the predicate. If none is
     * found, returns null.
     *
     * @param array the array to loop.
     * @param predicate the predicate to test against each value.
     * @return a matching value.
     */
    public static final <T> T findMatching(final T[] array, final Predicate<T> predicate) {
        return findMatching(array, predicate, null);
    }

    /**
     * Finds the first value in the array that matches the predicate. If none is
     * found, returns the default value.
     *
     * @param array the array to loop.
     * @param predicate the predicate to test against each value.
     * @param defaultValue the default value to return if no value matches the
     * predicate.
     * @return a matching value.
     */
    public static final <T> T findMatching(final T[] array, final Predicate<T> predicate, final T defaultValue) {
        for (int i = 0; i < array.length; i++) {
            val object = array[i];
            if (predicate.test(object)) {
                return object;
            }
        }
        return defaultValue;
    }

    /**
     * Rounds a double based on the RoundingMode settings.
     *
     * @param d double to round.
     * @param mode mode to use for rounding.
     * @return rounded integer.
     */
    public static final int round(final double d, final RoundingMode mode) {
        if (mode.equals(RoundingMode.CEILING) || mode.equals(RoundingMode.UP)) {
            return (int) Math.ceil(d);
        } else if (mode.equals(RoundingMode.FLOOR) || mode.equals(RoundingMode.DOWN)
                || mode.equals(RoundingMode.HALF_DOWN)) {
            return (int) Math.floor(d);
        } else if (mode.equals(RoundingMode.HALF_EVEN)) {
            final int ceil = (int) Math.ceil(d);
            if (ceil % 2 == 0) {
                return ceil;
            }
            return (int) Math.floor(d);
        }
        final double dAbs = Math.abs(d);
        final int i = (int) dAbs;
        final double result = dAbs - (double) i;
        if (mode.equals(RoundingMode.UNNECESSARY)) {
            if (result > 0) {
                throw new ArithmeticException();
            }
            return (int) d;
        }
        if (result < 0.5 && d >= 0 || result > 0.5 && d < 0) {
            return (int) Math.floor(d);
        } else {
            return (int) Math.ceil(d);
        }
    }

    /**
     * Gets a point within a chunk based on the input rotation.
     *
     * @param point that will be converted to the new loc.
     * @param widthRadius the width radius of the chunk.
     * @param heightRadius the height radius of the chunk.
     * @param degree to rotate.
     * @returnpoint with the new x/y coordinates.
     */
    public static final Point getPointInChunk(final Point point, final int widthRadius, final int heightRadius,
                                              final int degree) {
        final int derivativeX = Utils.round(
                ((point.getX() - widthRadius) * Math.cos(Math.toRadians(degree)))
                        - ((point.getY() - heightRadius) * Math.sin(Math.toRadians(degree)) - widthRadius),
                RoundingMode.HALF_UP);
        final int derivativeY = Utils.round(
                ((point.getX() - widthRadius) * Math.sin(Math.toRadians(degree)))
                        + ((point.getY() - heightRadius) * Math.cos(Math.toRadians(degree)) + heightRadius),
                RoundingMode.HALF_UP);
        return new Point(derivativeX, derivativeY);
    }

    private static final byte[][] ANGLE_DIRECTION_DELTA = {{0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1},
            {1, 1}, {1, 0}, {1, -1}};

    public static byte[] getDirection(final int angle) {
        final int v = angle >> 11;
        return ANGLE_DIRECTION_DELTA[v];
    }

    public static byte[] getDirByV(int v) {
        return ANGLE_DIRECTION_DELTA[v];
    }
    
    public static int getAngleToForceMovement(int angle) {
        final int v = angle >> 11;
        switch(v) {
        case 0:
            return ForceMovement.SOUTH;
        case 1:
            return ForceMovement.SOUTH_WEST;
        case 2:
            return ForceMovement.WEST;
        case 3:
            return ForceMovement.NORTH_WEST;
        case 4:
            return ForceMovement.NORTH;
        case 5:
            return ForceMovement.NORTH_EAST;
        case 6:
            return ForceMovement.EAST;
        case 7:
            return ForceMovement.SOUTH_EAST;
        }
        return ForceMovement.SOUTH;
    }

    /**
     * Constructs an arraylist of tiles between two points. x1/y1 -> Starting
     * location. x2/y2 -> End location. Draws as straight of a line as possible
     * between the given locations
     */
    public static final List<WorldTile> calculateLine(int x1, int y1, final int x2, final int y2, final int plane) {
        final List<WorldTile> tiles = new ArrayList<WorldTile>();
        final int dx = Math.abs(x2 - x1);
        final int dy = Math.abs(y2 - y1);
        final int sx = (x1 < x2) ? 1 : -1;
        final int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;
        while (true) {
            tiles.add(new WorldTile(x1, y1, plane));
            if (x1 == x2 && y1 == y2) {
                break;
            }
            final int e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }
            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
        return tiles;
    }

    /**
     * Converts a Long into a String.
     *
     * @param amount The amount to convert.
     * @return returns the Long as a formatted String.
     */
    public static String moneyToString(final long amount) {
        if (amount == -1) {
            return "none";
        }
        return (amount <= 999 ? ("" + amount)
                : amount <= 999999 ? ((int) (amount / 1000) + "K (" + amount + ")")
                : amount <= 999999999 ? ((int) (amount / 1000000) + "M (" + amount + ")")
                : amount <= 999999999999L ? ((int) (amount / 1000000000) + "B (" + amount + ")")
                : ((int) (amount / 1000000000000L) + "T (" + amount + ")"));
    }

    public static final int calculateGJString2Length(final String String) {
        final int length = String.length();
        int gjStringLength = 0;
        for (int index = 0; length > index; index++) {
            final char c = String.charAt(index);
            if (c > '\u007f') {
                if (c <= '\u07ff') {
                    gjStringLength += 2;
                } else {
                    gjStringLength += 3;
                }
            } else {
                gjStringLength++;
            }
        }
        return gjStringLength;
    }

    public static byte[] completeQuickMessage(final Player player, final int fileId, byte[] data) {
        if (fileId == 1) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.AGILITY)};
        } else if (fileId == 8) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.ATTACK)};
        } else if (fileId == 13) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.CONSTRUCTION)};
        } else if (fileId == 16) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.COOKING)};
        } else if (fileId == 23) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.CRAFTING)};
        } else if (fileId == 30) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.DEFENCE)};
        } else if (fileId == 34) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.FARMING)};
        } else if (fileId == 41) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.FIREMAKING)};
        } else if (fileId == 47) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.FISHING)};
        } else if (fileId == 55) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.FLETCHING)};
        } else if (fileId == 62) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.HERBLORE)};
        } else if (fileId == 70) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.HITPOINTS)};
        } else if (fileId == 74) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.HUNTER)};
        } else if (fileId == 135) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.MAGIC)};
        } else if (fileId == 127) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.MINING)};
        } else if (fileId == 120) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.PRAYER)};
        } else if (fileId == 116) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.RANGE)};
        } else if (fileId == 111) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.RUNECRAFTING)};
        } else if (fileId == 103) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.SLAYER)};
        } else if (fileId == 96) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.SMITHING)};
        } else if (fileId == 92) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.STRENGTH)};
        } else if (fileId == 85) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.SUMMONING)};
        } else if (fileId == 79) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.THIEVING)};
        } else if (fileId == 142) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.WOODCUTTING)};
        } else if (fileId == 526) {
            final int value = player.getLoyaltyPoints();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 547) {
            // AbstractQuest points
            final int value = 0;
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 821) {
            // Penguins found
            final int value = 0;
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 990) {
            data = new byte[]{(byte) player.getSkills().getLevelForXp(Skills.DUNGEONEERING)};
        } else if (fileId == 965) {
            final int value = player.getHitpoints();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 1108) {
            final int value = player.getDominionTower().getKilledBossesCount();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 1109) {
            final long value = player.getDominionTower().getTotalScore();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 1110) {
            final int value = (int) player.getDominionTower().getTotalScore();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 1111) {
            final int value = player.getDominionTower().getMaxFloorEndurance();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 1134) {
            final int value = player.getCrucibleHighScore();
            data = new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        } else if (fileId == 702) {
            return new byte[]{(byte) player.getSkills().getLevelForXp(Skills.RUNECRAFTING)};
        } else if (fileId == 1203) {
            return new byte[]{(byte) player.getSkills().getLevelForXp(Skills.DIVINATION)};
        }
        return data;
    }

    public static boolean containsInvalidCharacter(final char c) {
        for (final char vc : VALID_CHARS) {
            if (vc == c) {
                return false;
            }
        }
        return true;
    }

    public static boolean randomBool() {
        return RANDOM.nextBoolean();
    }

    public static boolean containsInvalidCharacter(final String name) {
        for (final char c : name.toCharArray()) {
            if (containsInvalidCharacter(c)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("resource")
    public static void copyFile(final File sourceFile, final File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static byte[] cryptRSA(final byte[] data, final BigInteger exponent, final BigInteger modulus) {
        return new BigInteger(data).modPow(exponent, modulus).toByteArray();
    }

    public static synchronized long currentTimeMillis() {
        final long l = System.currentTimeMillis();
        if (l < lastTimeUpdate) {
            timeCorrection += lastTimeUpdate - l;
        }
        lastTimeUpdate = l;
        return l + timeCorrection;
    }

    public static byte[] decipher(final int[] key, final byte[] data) {
        return decipher(key, data, 0, data.length);
    }

    public static byte[] decipher(final int[] key, final byte[] data, final int offset, final int length) {
        final int numBlocks = (length - offset) / 8;
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(offset);
        for (int i = 0; i < numBlocks; i++) {
            int y = buffer.getInt();
            int z = buffer.getInt();
            int sum = -957401312;
            final int delta = -1640531527;
            int numRounds = 32;
            while (numRounds > 0) {
                z -= ((y >>> 5 ^ y << 4) + y ^ sum + key[sum >>> 11 & 0x56c00003]);
                sum -= delta;
                y -= ((z >>> 5 ^ z << 4) - -z ^ sum + key[sum & 0x3]);
                numRounds--;
            }
            buffer.position(buffer.position() - 8);
            buffer.putInt(y);
            buffer.putInt(z);
        }
        return buffer.array();
    }

    public static String decodeBase37(long value) {
        final char[] chars = new char[12];
        int pos = 0;
        while (value != 0) {
            final int remainder = (int) (value % 37);
            value /= 37;

            char c;
            if (remainder >= 1 && remainder <= 26) {
                c = (char) ('a' + remainder - 1);
            } else if (remainder >= 27 && remainder <= 36) {
                c = (char) ('0' + remainder - 27);
            } else {
                c = '_';
            }

            chars[chars.length - pos++ - 1] = c;
        }
        return new String(chars, chars.length - pos, pos);
    }

    public static final byte[] encryptUsingMD5(final byte[] buffer) {
        // prevents concurrency problems with the algorithm
        synchronized (ALGORITHM_LOCK) {
            try {
                final MessageDigest algorithm = MessageDigest.getInstance("MD5");
                algorithm.update(buffer);
                final byte[] digest = algorithm.digest();
                algorithm.reset();
                return digest;
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static List<Class> findClasses(final File directory, final String packageName) {
        final List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        final File[] files = directory.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class
                            .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (final Throwable e) {
                }
            }
        }
        return classes;
    }

    public static String fixChatMessage(final String message) {
        final StringBuilder newText = new StringBuilder();
        boolean wasSpace = true;
        boolean exception = false;
        for (int i = 0; i < message.length(); i++) {
            if (!exception) {
                if (wasSpace) {
                    newText.append(("" + message.charAt(i)).toUpperCase());
                    if (!String.valueOf(message.charAt(i)).equals(" ")) {
                        wasSpace = false;
                    }
                } else {
                    newText.append(("" + message.charAt(i)).toLowerCase());
                }
            } else {
                newText.append(("" + message.charAt(i)));
            }
            if (String.valueOf(message.charAt(i)).contains(":")) {
                exception = true;
            } else if (String.valueOf(message.charAt(i)).contains(".")
                    || String.valueOf(message.charAt(i)).contains("!")
                    || String.valueOf(message.charAt(i)).contains("?")) {
                wasSpace = true;
            }
        }
        return newText.toString();
    }

    public static String formatPlayerNameForDisplay(String name) {
        if (name == null) {
            return "";
        }
        name = name.replaceAll("_", " ");
        name = name.toLowerCase();
        final StringBuilder newName = new StringBuilder();
        boolean wasSpace = true;
        for (int i = 0; i < name.length(); i++) {
            if (wasSpace) {
                newName.append(("" + name.charAt(i)).toUpperCase());
                wasSpace = false;
            } else {
                newName.append(name.charAt(i));
            }
            if (name.charAt(i) == ' ') {
                wasSpace = true;
            }
        }
        return newName.toString();
    }

    public static String formatPlayerNameForProtocol(String name) {
        if (name == null) {
            return "";
        }
        name = name.replaceAll(" ", "_");
        name = name.toLowerCase();
        return name;
    }

    public static String formatAorAn(final Item item) {
        final String name = ItemDefinitions.getItemDefinitions(item.getId()).getName().toLowerCase();
        if (name.contains("boots") || name.contains("gauntlets") || name.contains("goggles") || name.contains("greaves")
                || name.contains("legs") || name.contains("grips") || name.contains("handwraps")
                || name.contains("steadfast") || name.contains("glaiven") || name.contains("ragefire")
                || name.contains("chaps") || name.contains("tassets") || name.contains("gloves")) {
            return "";
        }
        if (name.startsWith("a") || name.startsWith("e") || name.startsWith("i") || name.startsWith("o")
                || name.startsWith("u")) {
            return "an ";
        }
        return "a ";
    }

    public static final int getAnimationDefinitionsSize() {
        final int lastArchiveId = Cache.STORE.getIndexes()[20].getLastArchiveId();
        return lastArchiveId * 128 + Cache.STORE.getIndexes()[20].getValidFilesCount(lastArchiveId);
    }

    public static String getAorAn(final String name) {
        val itemName = name.toLowerCase();
        if (itemName.contains("bones") || itemName.contains("boots") || itemName.contains("gauntlets")
                || itemName.contains("goggles") || itemName.contains("greaves") || itemName.contains("legs")
                || itemName.contains("grips") || itemName.contains("handwraps") || itemName.contains("steadfast")
                || itemName.contains("glaiven") || itemName.contains("ragefire") || itemName.contains("chaps")
                || itemName.contains("tassets") || itemName.contains("gloves")) {
            return "";
        }
        if (itemName.startsWith("a") || itemName.startsWith("e") || itemName.startsWith("i") || itemName.startsWith("o")
                || itemName.startsWith("u")) {
            return "an ";
        }
        return "a ";
    }

    /**
     * Every class under {@code packageName} (recursively) that the context class
     * loader can see. Directory classpath entries are walked exactly as before
     * ({@link #findClasses(File, String)}); when an entry for the package is not
     * a plain directory (a jar, the only case the packaged Java 25 runtime has) the
     * package is scanned with FastClasspathScanner, the same library
     * {@code com.rs.Scanner} already uses for dialogues and commands. Before the
     * fallback a jar classpath silently yielded zero classes, which is how the
     * 947 bootstrap would have booted with no NPC combat scripts.
     *
     * <p>Order within a directory walk is unchanged; jar-discovered classes are
     * appended after the directory ones and de-duplicated by name.
     */
    @SuppressWarnings({"rawtypes"})
    public static Class[] getClasses(final String packageName) throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        final String path = packageName.replace('.', '/');
        final Enumeration<URL> resources = classLoader.getResources(path);
        final List<File> dirs = new ArrayList<File>();
        boolean scanJars = false;
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
            } else {
                scanJars = true;
            }
        }
        final ArrayList<Class> classes = new ArrayList<Class>();
        for (final File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        if (scanJars) {
            final java.util.Set<String> seen = new java.util.HashSet<String>();
            for (final Class found : classes) {
                seen.add(found.getName());
            }
            for (final String name : scanPackageClassNames(packageName, classLoader)) {
                if (!seen.add(name)) {
                    continue;
                }
                try {
                    classes.add(Class.forName(name, true, classLoader));
                } catch (final Throwable e) {
                    // Same leniency as findClasses: a class that cannot be initialised is skipped.
                }
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Names of every class file under {@code packageName} on the class path, jars
     * included, via FastClasspathScanner 3.0.3 with a strict whitelist so classes
     * merely referenced from outside the package are not pulled in.
     */
    public static List<String> scanPackageClassNames(final String packageName, final ClassLoader classLoader) {
        final io.github.lukehutch.fastclasspathscanner.FastClasspathScanner scanner =
                new io.github.lukehutch.fastclasspathscanner.FastClasspathScanner(packageName).strictWhitelist();
        // FastClasspathScanner 3.0.3 predates JDK 9: it can only enumerate a loader it
        // recognises, which in practice means URLClassLoader. From JDK 9 the application
        // loader is jdk.internal.loader.ClassLoaders$AppClassLoader, and overriding with
        // it makes the scanner find NOTHING - measured on this tree with the packaged jar
        // classpath: override=0 classes on Java 25 versus 363 without it, while Java 8
        // returns 363 either way. Without an override the scanner falls back to the
        // java.class.path property, which is correct on both runtimes (this is also why
        // Scanner.scan, which never overrides, always worked). Only override when the
        // loader is one the scanner can actually introspect, so a genuinely custom
        // URLClassLoader is still honoured on Java 8.
        if (classLoader instanceof java.net.URLClassLoader) {
            scanner.overrideClassLoaders(classLoader);
        }
        return scanner.scan().getNamesOfAllClasses();
    }

    public static final int getConfigDefinitionsSize() {
        final int lastArchiveId = Cache.STORE.getIndexes()[22].getLastArchiveId();
        return lastArchiveId * 256 + Cache.STORE.getIndexes()[22].getValidFilesCount(lastArchiveId);
    }

    public static final int[][] getCoordOffsetsNear(final int size) {
        final int[] xs = new int[4 + (4 * size)];
        final int[] xy = new int[xs.length];
        xs[0] = -size;
        xy[0] = 1;
        xs[1] = 1;
        xy[1] = 1;
        xs[2] = -size;
        xy[2] = -size;
        xs[3] = 1;
        xy[2] = -size;
        for (int fakeSize = size; fakeSize > 0; fakeSize--) {
            xs[(4 + ((size - fakeSize) * 4))] = -fakeSize + 1;
            xy[(4 + ((size - fakeSize) * 4))] = 1;
            xs[(4 + ((size - fakeSize) * 4)) + 1] = -size;
            xy[(4 + ((size - fakeSize) * 4)) + 1] = -fakeSize + 1;
            xs[(4 + ((size - fakeSize) * 4)) + 2] = 1;
            xy[(4 + ((size - fakeSize) * 4)) + 2] = -fakeSize + 1;
            xs[(4 + ((size - fakeSize) * 4)) + 3] = -fakeSize + 1;
            xy[(4 + ((size - fakeSize) * 4)) + 3] = -size;
        }
        return new int[][]{xs, xy};
    }

    // 22314

    public static final int getDistance(final int coordX1, final int coordY1, final int coordX2, final int coordY2) {
        final int deltaX = coordX2 - coordX1;
        final int deltaY = coordY2 - coordY1;
        return ((int) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }

    public static final int getDistance(final WorldTile t1, final WorldTile t2) {
        return (t1 == null || t2 == null) ? 0 : getDistance(t1.getX(), t1.getY(), t2.getX(), t2.getY());
    }

    public static final int getFaceDirection(final int xOffset, final int yOffset) {
        return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
    }

    /**
     * Used strictly for Kalphite King's combat script.
     *
     * @param from from WorldTile.
     * @param to to WorldTile.
     * @return direction.
     */
    public static byte[] getOrthogonalDirection(final WorldTile from, final WorldTile to) {
        final int[] dirVector = {to.getX() - from.getX() - 2, to.getY() - from.getY() - 2};
        if (Math.abs(dirVector[0]) > Math.abs(dirVector[1])) {
            if (dirVector[0] > 0) {
                return new byte[]{1, 0};
            } else if (dirVector[0] < 0) {
                return new byte[]{-1, 0};
            }
        } else if (Math.abs(dirVector[0]) < Math.abs(dirVector[1])) {
            if (dirVector[1] > 0) {
                return new byte[]{0, 1};
            } else if (dirVector[1] < 0) {
                return new byte[]{0, -1};
            }
        }
        return new byte[]{1, 0};
    }

    public static final byte[] getFormatedMessage(final String message) {
        final int i_0_ = message.length();
        final byte[] is = new byte[i_0_];
        for (int i_1_ = 0; (i_1_ ^ 0xffffffff) > (i_0_ ^ 0xffffffff); i_1_++) {
            final int i_2_ = message.charAt(i_1_);
            if (((i_2_ ^ 0xffffffff) >= -1 || i_2_ >= 128) && (i_2_ < 160 || i_2_ > 255)) {
                if ((i_2_ ^ 0xffffffff) != -8365) {
                    if ((i_2_ ^ 0xffffffff) == -8219) {
                        is[i_1_] = (byte) -126;
                    } else if ((i_2_ ^ 0xffffffff) == -403) {
                        is[i_1_] = (byte) -125;
                    } else if (i_2_ == 8222) {
                        is[i_1_] = (byte) -124;
                    } else if (i_2_ != 8230) {
                        if ((i_2_ ^ 0xffffffff) != -8225) {
                            if ((i_2_ ^ 0xffffffff) != -8226) {
                                if ((i_2_ ^ 0xffffffff) == -711) {
                                    is[i_1_] = (byte) -120;
                                } else if (i_2_ == 8240) {
                                    is[i_1_] = (byte) -119;
                                } else if ((i_2_ ^ 0xffffffff) == -353) {
                                    is[i_1_] = (byte) -118;
                                } else if ((i_2_ ^ 0xffffffff) != -8250) {
                                    if (i_2_ == 338) {
                                        is[i_1_] = (byte) -116;
                                    } else if (i_2_ == 381) {
                                        is[i_1_] = (byte) -114;
                                    } else if ((i_2_ ^ 0xffffffff) == -8217) {
                                        is[i_1_] = (byte) -111;
                                    } else if (i_2_ == 8217) {
                                        is[i_1_] = (byte) -110;
                                    } else if (i_2_ != 8220) {
                                        if (i_2_ == 8221) {
                                            is[i_1_] = (byte) -108;
                                        } else if ((i_2_ ^ 0xffffffff) == -8227) {
                                            is[i_1_] = (byte) -107;
                                        } else if ((i_2_ ^ 0xffffffff) != -8212) {
                                            if (i_2_ == 8212) {
                                                is[i_1_] = (byte) -105;
                                            } else if ((i_2_ ^ 0xffffffff) != -733) {
                                                if (i_2_ != 8482) {
                                                    if (i_2_ == 353) {
                                                        is[i_1_] = (byte) -102;
                                                    } else if (i_2_ != 8250) {
                                                        if ((i_2_ ^ 0xffffffff) == -340) {
                                                            is[i_1_] = (byte) -100;
                                                        } else if (i_2_ != 382) {
                                                            if (i_2_ == 376) {
                                                                is[i_1_] = (byte) -97;
                                                            } else {
                                                                is[i_1_] = (byte) 63;
                                                            }
                                                        } else {
                                                            is[i_1_] = (byte) -98;
                                                        }
                                                    } else {
                                                        is[i_1_] = (byte) -101;
                                                    }
                                                } else {
                                                    is[i_1_] = (byte) -103;
                                                }
                                            } else {
                                                is[i_1_] = (byte) -104;
                                            }
                                        } else {
                                            is[i_1_] = (byte) -106;
                                        }
                                    } else {
                                        is[i_1_] = (byte) -109;
                                    }
                                } else {
                                    is[i_1_] = (byte) -117;
                                }
                            } else {
                                is[i_1_] = (byte) -121;
                            }
                        } else {
                            is[i_1_] = (byte) -122;
                        }
                    } else {
                        is[i_1_] = (byte) -123;
                    }
                } else {
                    is[i_1_] = (byte) -128;
                }
            } else {
                is[i_1_] = (byte) i_2_;
            }
        }
        return is;
    }

    public static String getFormattedNumber(final int value) {
        return new DecimalFormat("#,###,##0").format(value);
    }

    public static final int getGraphicDefinitionsSize() {
        final int lastArchiveId = Cache.STORE.getIndexes()[21].getLastArchiveId();
        return lastArchiveId * 256 + Cache.STORE.getIndexes()[21].getValidFilesCount(lastArchiveId);
    }

    public static final int getInterfaceDefinitionsComponentsSize(final int interfaceId) {
        return Cache.STORE.getIndexes()[3].getLastFileId(interfaceId) + 1;
    }

    public static int getHashMapSize(int size) {
        size--;
        size |= size >>> -1810941663;
        size |= size >>> 2010624802;
        size |= size >>> 10996420;
        size |= size >>> 491045480;
        size |= size >>> 1388313616;
        return 1 + size;
    }

    public static final int getInterfaceDefinitionsSize() {
        return Cache.STORE.getIndexes()[3].getLastArchiveId() + 1;
    }

    public static final int getItemDefinitionsSize() {
        return getDefinitionsSize(Cache.STORE.getIndexes()[19], Js5ConfigGroup.ObjectsConfigGroup);
    }

    public static int getDefinitionsSize(Index index, Js5ConfigGroup configGroup) {
        if (null != index) {
            if (configGroup.method1306() > 1) {
                int i = index.getLastArchiveId();
                return i * configGroup.method1306() + index.getTable().getArchives()[i].biggestFileId;
            }
            return index.getTable().getArchives()[configGroup.archiveId].biggestFileId;
        }
        return 0;
    }

    public static String formatPrice(int price) {
        if (price < 1_000) {
            return String.valueOf(price);
        }
        if (price < 1_000_000) {
            return price / 1000 + "K";
        }
        return price / 1_000_000 + "M";
    }

    public static List<String> listOfLines(String description) {
        String[] parse = description.split("\n");
        return new ArrayList<>(Arrays.asList(parse));
    }

    public static class Js5ConfigGroup {
        public static Js5ConfigGroup ObjectsConfigGroup = new Js5ConfigGroup(6, 8);
        int groupSize;
        public int archiveId;

        Js5ConfigGroup(int var1, int var2) {
            this.archiveId = var1;
            this.groupSize = var2;
        }

        public int method1305(int var1) {
            return var1 & (1 << this.groupSize) - 1;
        }

        public int method1306() {
            return 1 << this.groupSize;
        }

        public int method1307(int var1) {
            return var1 >>> this.groupSize;
        }
    }

    public static final int getSpritesSize() {
        final int lastArchiveId = Cache.STORE.getIndexes()[8].getLastArchiveId();
        return (lastArchiveId * 256 + Cache.STORE.getIndexes()[8].getValidFilesCount(lastArchiveId));
    }

    public static final boolean isValidQuickChat(final int id) {
        return Cache.STORE.getIndexes()[24].fileExists(1, id);
    }

    public static final int getMoveDirection(final int xOffset, final int yOffset) {
        if (xOffset < 0) {
            if (yOffset < 0) {
                return 5;
            } else if (yOffset > 0) {
                return 0;
            } else {
                return 3;
            }
        } else if (xOffset > 0) {
            if (yOffset < 0) {
                return 7;
            } else if (yOffset > 0) {
                return 2;
            } else {
                return 4;
            }
        } else {
            if (yOffset < 0) {
                return 6;
            } else if (yOffset > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public static final int getNameHash(String name) {
        name = name.toLowerCase();
        int hash = 0;
        for (int index = 0; index < name.length(); index++) {
            hash = method1258(name.charAt(index)) + ((hash << 5) - hash);
        }
        return hash;
    }

    public static final int getNPCDefinitionsSize() {
        try {
            com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[18];

            // 1. Safe check to ensure Index 18 (NPCs) actually loaded
            if (index == null || index.getTable() == null) {
                System.out.println("Warning: Index 18 (NPCs) missing or null. Using fallback size.");
                return 30000; // Safe fallback size for revision 921
            }

            int lastArchiveId = index.getLastArchiveId();

            // 2. Safe check to ensure the last archive actually exists before counting its files
            if (index.getTable().getArchives() == null ||
                    lastArchiveId >= index.getTable().getArchives().length ||
                    index.getTable().getArchives()[lastArchiveId] == null) {

                return lastArchiveId * 128;
            }

            return lastArchiveId * 128 + index.getValidFilesCount(lastArchiveId);

        } catch (Exception e) {
            // 3. If anything goes wrong, catch the error and return a safe size instead of crashing
            System.out.println("Warning: Error calculating NPC size. Using safe fallback.");
            return 30000;
        }
    }

    public static int getNpcMoveDirection(final int dd) {
        if (dd < 0) {
            return -1;
        }
        return getNpcMoveDirection(DIRECTION_DELTA_X[dd], DIRECTION_DELTA_Y[dd]);
    }

    public static int getNpcMoveDirection(final int dx, final int dy) {
        if (dx == 0 && dy > 0) {
            return 0;
        }
        if (dx > 0 && dy > 0) {
            return 1;
        }
        if (dx > 0 && dy == 0) {
            return 2;
        }
        if (dx > 0 && dy < 0) {
            return 3;
        }
        if (dx == 0 && dy < 0) {
            return 4;
        }
        if (dx < 0 && dy < 0) {
            return 5;
        }
        if (dx < 0 && dy == 0) {
            return 6;
        }
        if (dx < 0 && dy > 0) {
            return 7;
        }
        return -1;
    }


    
    public static final int getObjectDefinitionsSize() {
        final int lastArchiveId = Cache.STORE.getIndexes()[16].getLastArchiveId();
        return lastArchiveId * 256 + Cache.STORE.getIndexes()[16].getValidFilesCount(lastArchiveId);
    }

    public static double getPercent(final double percent, final double number) {
        return (percent / 100) * number;
    }

    public static int getPlayerRunningDirection(final int dx, final int dy) {
        if (dx == -2 && dy == -2) {
            return 0;
        }
        if (dx == -1 && dy == -2) {
            return 1;
        }
        if (dx == 0 && dy == -2) {
            return 2;
        }
        if (dx == 1 && dy == -2) {
            return 3;
        }
        if (dx == 2 && dy == -2) {
            return 4;
        }
        if (dx == -2 && dy == -1) {
            return 5;
        }
        if (dx == 2 && dy == -1) {
            return 6;
        }
        if (dx == -2 && dy == 0) {
            return 7;
        }
        if (dx == 2 && dy == 0) {
            return 8;
        }
        if (dx == -2 && dy == 1) {
            return 9;
        }
        if (dx == 2 && dy == 1) {
            return 10;
        }
        if (dx == -2 && dy == 2) {
            return 11;
        }
        if (dx == -1 && dy == 2) {
            return 12;
        }
        if (dx == 0 && dy == 2) {
            return 13;
        }
        if (dx == 1 && dy == 2) {
            return 14;
        }
        if (dx == 2 && dy == 2) {
            return 15;
        }
        return -1;
    }

    /**
     * Walk dirs 0 - South-West 1 - South 2 - South-East 3 - West 4 - East 5 -
     * North-West 6 - North 7 - North-East
     */
    public static int getPlayerWalkingDirection(final int dx, final int dy) {
        if (dx == -1 && dy == -1) {
            return 0;
        }
        if (dx == 0 && dy == -1) {
            return 1;
        }
        if (dx == 1 && dy == -1) {
            return 2;
        }
        if (dx == -1 && dy == 0) {
            return 3;
        }
        if (dx == 1 && dy == 0) {
            return 4;
        }
        if (dx == -1 && dy == 1) {
            return 5;
        }
        if (dx == 0 && dy == 1) {
            return 6;
        }
        if (dx == 1 && dy == 1) {
            return 7;
        }
        return -1;
    }

    public static final int getRandom(final int maxValue) {
        return (int) (Math.random() * (maxValue + 1));
    }

    public static final double getRandomDouble(final double maxValue) {
        return (Math.random() * (maxValue + 1));

    }

    public static final String getUnformatedMessage(final int messageDataLength, final int messageDataOffset,
                                                    final byte[] messageData) {
        final char[] cs = new char[messageDataLength];
        int i = 0;
        for (int i_6_ = 0; i_6_ < messageDataLength; i_6_++) {
            int i_7_ = 0xff & messageData[i_6_ + messageDataOffset];
            if ((i_7_ ^ 0xffffffff) != -1) {
                if ((i_7_ ^ 0xffffffff) <= -129 && (i_7_ ^ 0xffffffff) > -161) {
                    int i_8_ = aCharArray6385[i_7_ - 128];
                    if (i_8_ == 0) {
                        i_8_ = 63;
                    }
                    i_7_ = i_8_;
                }
                cs[i++] = (char) i_7_;
            }
        }
        return new String(cs, 0, i);
    }

    public static int getDirectionBetweenTiles(final WorldTile from, final WorldTile to) {
        if (from.getX() > to.getX() && from.getY() == to.getY()) {
            return 4096;
        } else if (from.getX() == to.getX() && from.getY() < to.getY()) {
            return 8192;
        } else if (from.getX() < to.getX() && from.getY() == to.getY()) {
            return 12288;
        } else if (from.getX() == to.getX() && from.getY() > to.getY()) {
            return 0;
        }
        int multiplier;
        if (from.getX() > to.getX() && from.getY() > to.getY()) {
            multiplier = 1;
        } else if (from.getX() > to.getX() && from.getY() < to.getY()) {
            multiplier = 2;
        } else if (from.getX() < to.getX() && from.getY() < to.getY()
                || from.getX() == to.getX() && from.getY() < to.getY()) {
            multiplier = 3;
        } else {
            multiplier = 4;
        }
        if (((from.getX() - to.getX()) + from.getY() - to.getY()) > 0) {
            return Math.abs(multiplier * 4086 / ((from.getX() - to.getX()) + from.getY() - to.getY()));
        } else {
            return Math.abs(multiplier * 4086);
        }
    }

    public static boolean inCircle(final WorldTile location, final WorldTile center, final int radius) {
        return getDistance(center, location) < radius;
    }

    public static boolean invalidAccountName(final String name) {
        return name.length() < 2 || name.length() > 12 || name.startsWith("_") || name.startsWith("mod ")
                || name.startsWith("owner ") || !name.matches("^[a-zA-Z0-9_ ]{3,12}$") || name.contains("dragonkk")
                || name.endsWith("_") || name.contains("__");
    }

    public static boolean invalidAuthId(final String auth) {
        return auth.length() != 10 || auth.contains("_") || containsInvalidCharacter(auth);
    }

    public static boolean itemExists(final int id) {
        if (id >= getItemDefinitionsSize()) {
            return false;
        }
        return Cache.STORE.getIndexes()[19].fileExists(id >>> 8, 0xff & id);
    }

    public static String getCompleted(final String[] cmd, final int index) {
        final StringBuilder sb = new StringBuilder();
        for (int i = index; i < cmd.length; i++) {
            if (i == cmd.length - 1 || cmd[i + 1].startsWith("+")) {
                return sb.append(cmd[i]).toString();
            }
            sb.append(cmd[i]).append(" ");
        }
        return "null";
    }

    public static final String longToString(long l) {
        if (l <= 0L || l >= 0x5b5b57f8a98a5dd1L) {
            return null;
        }
        if (l % 37L == 0L) {
            return null;
        }
        int i = 0;
        final char[] ac = new char[12];
        while (l != 0L) {
            final long l1 = l;
            l /= 37L;
            ac[11 - i++] = VALID_CHARS[(int) (l1 - l * 37L)];
        }
        return new String(ac, 12 - i, i);
    }

    public static final byte method1258(final char c) {
        byte charByte;
        if (c > 0 && c < '\200' || c >= '\240' && c <= '\377') {
            charByte = (byte) c;
        } else if (c != '\u20AC') {
            if (c != '\u201A') {
                if (c != '\u0192') {
                    if (c == '\u201E') {
                        charByte = -124;
                    } else if (c != '\u2026') {
                        if (c != '\u2020') {
                            if (c == '\u2021') {
                                charByte = -121;
                            } else if (c == '\u02C6') {
                                charByte = -120;
                            } else if (c == '\u2030') {
                                charByte = -119;
                            } else if (c == '\u0160') {
                                charByte = -118;
                            } else if (c == '\u2039') {
                                charByte = -117;
                            } else if (c == '\u0152') {
                                charByte = -116;
                            } else if (c != '\u017D') {
                                if (c == '\u2018') {
                                    charByte = -111;
                                } else if (c != '\u2019') {
                                    if (c != '\u201C') {
                                        if (c == '\u201D') {
                                            charByte = -108;
                                        } else if (c != '\u2022') {
                                            if (c == '\u2013') {
                                                charByte = -106;
                                            } else if (c == '\u2014') {
                                                charByte = -105;
                                            } else if (c == '\u02DC') {
                                                charByte = -104;
                                            } else if (c == '\u2122') {
                                                charByte = -103;
                                            } else if (c != '\u0161') {
                                                if (c == '\u203A') {
                                                    charByte = -101;
                                                } else if (c != '\u0153') {
                                                    if (c == '\u017E') {
                                                        charByte = -98;
                                                    } else if (c != '\u0178') {
                                                        charByte = 63;
                                                    } else {
                                                        charByte = -97;
                                                    }
                                                } else {
                                                    charByte = -100;
                                                }
                                            } else {
                                                charByte = -102;
                                            }
                                        } else {
                                            charByte = -107;
                                        }
                                    } else {
                                        charByte = -109;
                                    }
                                } else {
                                    charByte = -110;
                                }
                            } else {
                                charByte = -114;
                            }
                        } else {
                            charByte = -122;
                        }
                    } else {
                        charByte = -123;
                    }
                } else {
                    charByte = -125;
                }
            } else {
                charByte = -126;
            }
        } else {
            charByte = -128;
        }
        return charByte;
    }

    public static char method2782(final byte value) {
        int byteChar = 0xff & value;
        if (byteChar == 0) {
            throw new IllegalArgumentException(
                    "Non cp1252 character 0x" + Integer.toString(byteChar, 16) + " provided");
        }
        if ((byteChar ^ 0xffffffff) <= -129 && byteChar < 160) {
            int i_4_ = aCharArray6385[-128 + byteChar];
            if ((i_4_ ^ 0xffffffff) == -1) {
                i_4_ = 63;
            }
            byteChar = i_4_;
        }
        return (char) byteChar;
    }

    public static final int next(final int max, final int min) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public static final int packGJString2(final int position, final byte[] buffer, final String String) {
        final int length = String.length();
        int offset = position;
        for (int index = 0; length > index; index++) {
            final int character = String.charAt(index);
            if (character > 127) {
                if (character > 2047) {
                    buffer[offset++] = (byte) ((character | 919275) >> 12);
                    buffer[offset++] = (byte) (128 | ((character >> 6) & 63));
                    buffer[offset++] = (byte) (128 | (character & 63));
                } else {
                    buffer[offset++] = (byte) ((character | 12309) >> 6);
                    buffer[offset++] = (byte) (128 | (character & 63));
                }
            } else {
                buffer[offset++] = (byte) character;
            }
        }
        return offset - position;
    }

    public static final double random(final double min, final double max) {
        final double n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random((int) n));
    }

    public static final int random(final int maxValue) {
        if (maxValue <= 0) {
            return 0;
        }
        return RANDOM.nextInt(maxValue);
    }

    public static final long random(final long maxValue) {
        if (maxValue <= 0) {
            return 0;
        }
        final long randomValue = (long) (RANDOM.nextDouble() * (0 - maxValue));
        return randomValue;
    }

    public static final int random(final Range<Integer> range) {
        if (range.isEmpty()) {
            return range.lowerEndpoint();
        }
        return ThreadLocalRandom.current().nextInt(range.lowerEndpoint(), range.upperEndpoint() + 1);
    }

    public static final int random(final int min, final int max) {
        final int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random(n));
    }

    public static int rand(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    public static final int inclusive(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static <T> T randomFrom(final T... items) {
        return items[random(items.length)];
    }

    public static int randomFrom(final int... items) {
        return items[random(items.length)];
    }

    public static <T> T randomFrom(final List<T> items) {
        return items.get(random(items.size()));
    }

    public static final long stringToLong(final String s) {
        long l = 0L;
        for (int i = 0; i < s.length() && i < 12; i++) {
            final char c = s.charAt(i);
            l *= 37L;
            if (c >= 'A' && c <= 'Z') {
                l += (1 + c) - 65;
            } else if (c >= 'a' && c <= 'z') {
                l += (1 + c) - 97;
            } else if (c >= '0' && c <= '9') {
                l += (27 + c) - 48;
            }
        }
        while (l % 37L == 0L && l != 0L) {
            l /= 37L;
        }
        return l;
    }

    public static boolean colides(final int x1, final int y1, final int size1, final int x2, final int y2,
                                  final int size2) {
        final int distanceX = x1 - x2;
        final int distanceY = y1 - y2;
        return distanceX < size2 && distanceX > -size1 && distanceY < size2 && distanceY > -size1;
    }

    public static boolean isOnRange(final int x1, final int y1, final int size1, final int x2, final int y2,
                                    final int size2, final int maxDistance) {
        final int distanceX = x1 - x2;
        final int distanceY = y1 - y2;
        return distanceX <= size2 + maxDistance && distanceX >= -size1 - maxDistance && distanceY <= size2 + maxDistance
                && distanceY >= -size1 - maxDistance;
    }

    /*
     * dont use this one
     */
    public static boolean isOnRange(final int x1, final int y1, final int x2, final int y2, final int sizeX,
                                    final int sizeY) {
        final int distanceX = x1 - x2;
        final int distanceY = y1 - y2;
        return distanceX <= sizeX && distanceX >= -1 && distanceY <= sizeY && distanceY >= -1;
    }

    public static int fixChatEffects(final String username, final int colorEffect, final int moveEffect) {
        if (colorEffect > 11 || moveEffect > 5) {
            Logger.getGlobal().info("Fake Chat Effects recieved by client (" + username + ").");
            return 0;
        }
        return (colorEffect << 8) | (moveEffect & 0xff);
    }

    public static byte[] getBytesFromFile(final File file) throws IOException {
        final InputStream is = new FileInputStream(file);

        final long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        final byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    public static final double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static boolean badName(final String name) {
        final String n = name.toLowerCase();
        return n.contains("****") || n.contains("mod ") || n.contains("admin") || n.contains("isis");
    }

    public static int get32BitValue(final boolean[] array, final boolean trueCondition) {
        int value = 0;
        for (int index = 1; index < array.length + 1; index++) {
            if (array[index - 1] == trueCondition) {
                value += 1 << index;
            }
        }
        return value;
    }

    public static String getFormattedNumber(final double amount, final char seperator) {
        final String str = new DecimalFormat("#,###,###").format(amount);
        final char[] rebuff = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                rebuff[i] = c;
            } else {
                rebuff[i] = seperator;
            }
        }
        return new String(rebuff);
    }

    public static final int getAngle(final int xOffset, final int yOffset) {
        return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
        }

    public static int getMapArchiveId(final int regionX, final int regionY) {
        return regionX | regionY << 7;
    }

    /**
     * String used to handle displaying and formatting of total time played.
     *
     * @param totalPlayTime The total time played.
     * @return Formatted total time played in String.
     */
    public static String getTimePlayed(final long totalPlayTime) {
        final long tPt = Utils.currentTimeMillis() - totalPlayTime;
        final int sec = (int) (tPt / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? "0" + h : h) + "h:") + ((m < 1) && h < 1 ? "" : (m < 10 ? "0" + m : m) + "m:")
                + ((s < 1) && m < 1 ? "" : (s < 10 ? "0" + s + "s" : s + "s"));
    }

    public static String formatActiveTimer(final long totalPlayTime) {
        final long tPt = totalPlayTime - Utils.currentTimeMillis();
        final int sec = (int) (tPt / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? "0" + h : h) + "h:") + ((m < 1) && h < 1 ? "" : (m < 10 ? "0" + m : m) + "m:")
                + ((s < 1) && m < 1 ? "" : (s < 10 ? "0" + s + "s" : s + "s"));
    }

    public static String formatActiveTimerShort(final long totalPlayTime) {

        final long tPt = totalPlayTime - Utils.currentTimeMillis();
        final int sec = (int) (tPt / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? h : h) + ":") + ((m < 1) && h < 1 ? "" : (m < 10 ? +m : m) + ":")
                + ((s < 1) && m < 1 ? "" : (s < 10 ? s + "" : s + ""));
    }

    public static int getMinutesPlayed(final Player player) {
        final long totalPlayTime = Utils.currentTimeMillis()
                - (player.getTotalPlayTime() + player.getRecordedPlayTime());
        final int sec = (int) (totalPlayTime / 1000), h = sec / 3600;
        int m = sec / 60 % 60;
        for (int i = 0; i < h; i++) {
            m += 60;
        }
        return m;
    }

    public static void clearFile(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearFile(Path path) {
        clearFile(path.toFile());
    }

    public static int getHoursPlayed(final long totalPlayTime) {
        final long tPt = Utils.currentTimeMillis() - totalPlayTime;
        final int sec = (int) (tPt / 1000), h = sec / 3600;
        return (h < 10 ? 0 + h : h);
    }

    /**
     * Converts milliseconds to minutes.
     *
     * @param millisecs The ms to convert.
     * @return minutes.
     */
    public static int millisecsToMinutes(final long millisecs) {
        final int sec = (int) (millisecs / 1000), h = sec / 3600;
        int m = sec / 60 % 60;
        for (int i = 0; i < h; i++) {
            m += 60;
        }
        return m;
    }

    public static WorldTile getFreeTile(final WorldTile center, final int distance) {
        if (center == null) {
            return null;
        }
        WorldTile tile = center;
        for (int i = 0; i < 10; i++) {
            tile = new WorldTile(center, distance);
            if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 1)) {
                return tile;
            }
        }
        return center;
    }

    private static final String[] suffix = new String[]{"", "k", "m", "b", "t"};
    private static final int MAX_LENGTH = 4;

    public static String formatLetter(int number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }
        return r;
    }

    /*
     * world cycles, each is 600ms :). its 100% safe to use :p example of usage well
     * doesnt save with restarts it should work fine for disabled. its bad dont use
     * for things that save ofc good for stuff that doesnt save such as temporary
     * args and delays
     */
    public static long currentWorldCycle() {
        return WorldThread.WORLD_CYCLE;
    }

    public static boolean isOnRange(final Entity entity, final Entity target, final int rangeRatio) {
        return entity.getPlane() == target.getPlane() && isOnRange(entity.getX(), entity.getY(), entity.getSize(),
                target.getX(), target.getY(), target.getSize(), rangeRatio);
    }

    /**
     * @return the number with commas
     */
    public static String formatNumber(final long num) {
        return NumberFormat.getNumberInstance(Locale.UK).format(num);
    }

    public static boolean colides(final Entity entity, final Entity target) {
        return entity.getPlane() == target.getPlane() && colides(entity.getX(), entity.getY(), entity.getSize(),
                target.getX(), target.getY(), target.getSize());
    }

    public static boolean colides(final WorldTile entity, final WorldTile target, final int s1, final int s2) {
        return entity.getPlane() == target.getPlane()
                && colides(entity.getX(), entity.getY(), s1, target.getX(), target.getY(), s2);
    }

    public static int getProjectileTime(final WorldTile startTile, final WorldTile endTile, final int startHeight,
                                        final int endHeight, int speed, final int delay, final int curve, final int startDistanceOffset,
                                        final int creatorSize) {
        final int distance = Utils.getDistance(startTile, endTile) + 1;
        if (speed == 0) {
            // so may round to 0
            speed = 1;
        }
        return (delay * 10) + (distance * ((30 / speed) * 10)); /** Math.cos(Math.toRadians(curve)) */
    }

    public static int getProjectileTimeNew(final WorldTile from, final int fromSizeX, final int fromSizeY,
                                           final WorldTile to, final int toSizeX, final int toSizeY, final double speed) {
        int fromX = from.getX() * 2 + fromSizeX;
        int fromY = from.getY() * 2 + fromSizeY;

        int toX = to.getX() * 2 + toSizeX;
        int toY = to.getY() * 2 + toSizeY;

        fromX /= 2;
        fromY /= 2;
        toX /= 2;
        toY /= 2;

        final int deltaX = fromX - toX;
        final int deltaY = fromY - toY;
        final int sqrt = (int) Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        return (int) (sqrt * (10 / speed));
    }

    public static int getProjectileTimeSoulsplit(final WorldTile from, final int fromSizeX, final int fromSizeY,
                                                 final WorldTile to, final int toSizeX, final int toSizeY) {
        int fromX = from.getX() * 2 + fromSizeX;
        int fromY = from.getY() * 2 + fromSizeY;

        int toX = to.getX() * 2 + toSizeX;
        int toY = to.getY() * 2 + toSizeY;

        fromX /= 2;
        fromY /= 2;
        toX /= 2;
        toY /= 2;

        final int deltaX = fromX - toX;
        final int deltaY = fromY - toY;
        int sqrt = (int) Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        sqrt *= 15;
        sqrt -= sqrt % 30;
        return Math.max(30, sqrt);
    }

    public static int projectileTimeToCycles(final int time) {
        return (time + 29) / 30;
    }

    public static String currentTime(final String dateFormat) {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public static String formatString(String name) {
        if (name == null) {
            return "";
        }
        name = name.replaceAll("_", " ");
        name = name.toLowerCase();
        final StringBuilder newName = new StringBuilder();
        boolean wasSpace = true;
        for (int i = 0; i < name.length(); i++) {
            if (wasSpace) {
                newName.append(("" + name.charAt(i)).toUpperCase());
                wasSpace = false;
            } else {
                newName.append(name.charAt(i));
            }
            if (name.charAt(i) == ' ') {
                wasSpace = true;
            }
        }
        return newName.toString();
    }

    public static String[] splitString(final int lengthRequired, final String message) {
        final int length = message.length();
        if (length > lengthRequired * 2) {
            final String[] splitMessage = message.split(" ");
            if (splitMessage.length == 1) {
                return new String[]{message};
            }
            final String split13 = splitMessage[splitMessage.length / 3];
            final String split23 = splitMessage[splitMessage.length * 2 / 3];
            final String[] newsplitMessage13 = message.split(split13);
            final String[] newsplitMessage23 = newsplitMessage13[1].split(split23);
            if (newsplitMessage23.length == 2) {
                return new String[]{newsplitMessage13[0] + split13, newsplitMessage23[0] + split23,
                        newsplitMessage23[1]};
            }
        }
        if (length > lengthRequired) {
            final String[] splitMessage = message.split(" ");
            if (splitMessage.length == 1) {
                return new String[]{message};
            }
            if (splitMessage.length == 2) {
                return new String[]{splitMessage[0], splitMessage[1]};
            }
            final int splitint = splitMessage.length / 2;
            final String split = splitMessage[splitint] + " " + splitMessage[splitint + 1];
            final String[] newsplitMessage = message.split(split);
            if (newsplitMessage.length == 2) {
                return new String[]{newsplitMessage[0] + split, newsplitMessage[1]};
            }
        }
        return new String[]{message};
    }

    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        try {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isOnRange(final WorldTile entity, final WorldTile target, final int rangeRatio, final int s1,
                                    final int s2) {
        return entity.getPlane() == target.getPlane()
                && isOnRange(entity.getX(), entity.getY(), s1, target.getX(), target.getY(), s2, rangeRatio);
    }

    public static double getProjectileSpeed(final WorldTile startTile, final WorldTile endTile, final int startHeight,
                                            final int endHeight, final long startTime, final long arriveTime) {
        int fromSizeX, fromSizeY;
        if (startTile instanceof Entity) {
            fromSizeX = fromSizeY = ((Entity) startTile).getSize();
        } else if (startTile instanceof WorldObject) {
            final ObjectDefinitions defs = ((WorldObject) startTile).getDefinitions();
            fromSizeX = defs.getSizeX();
            fromSizeY = defs.getSizeY();
        } else {
            fromSizeX = fromSizeY = 1;
        }
        int toSizeX, toSizeY;
        if (endTile instanceof Entity) {
            toSizeX = toSizeY = ((Entity) endTile).getSize();
        } else if (endTile instanceof WorldObject) {
            final ObjectDefinitions defs = ((WorldObject) endTile).getDefinitions();
            toSizeX = defs.getSizeX();
            toSizeY = defs.getSizeY();
        } else {
            toSizeX = toSizeY = 1;
        }
        int fromX = startTile.getX() * 2 + fromSizeX;
        int fromY = startTile.getY() * 2 + fromSizeY;

        int toX = endTile.getX() * 2 + toSizeX;
        int toY = endTile.getY() * 2 + toSizeY;

        fromX /= 2;
        fromY /= 2;
        toX /= 2;
        toY /= 2;

        final int deltaX = fromX - toX;
        final int deltaY = fromY - toY;
        final double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

        final double speed = ((distance * 1000) / (arriveTime - startTime));

        return speed;
    }

    public static int getFaceDirection(final int dir) {
        final int[] dirs = {Utils.getAngle(0, 1), Utils.getAngle(1, 0), Utils.getAngle(0, -1), Utils.getAngle(-1, 0)};
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i] == dir) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isInDirection(final WorldTile entity, final WorldTile target, final int dir) {
        return (dir == ForceMovement.EAST && target.getX() > entity.getX())
                || (dir == ForceMovement.WEST && target.getX() < entity.getX())
                || (dir == ForceMovement.NORTH && target.getY() > entity.getY())
                || (dir == ForceMovement.SOUTH && target.getY() < entity.getY());
    }

    public static WorldTile getFreeTile(final WorldTile tile, final int dir, final int maxSteps, final int size) {
        WorldTile freeTile = tile;
        for (int i = 1; i <= maxSteps; i++) {
            final WorldTile checkTile = getNextWorldTile(tile, dir, i);
            final WorldTile nextTile = getNextWorldTile(tile, dir, i + 1);
            if (World.canMoveNPC(checkTile.getPlane(), checkTile.getX(), checkTile.getY(), size)
                    && World.canMoveNPC(nextTile.getPlane(), nextTile.getX(), nextTile.getY(), size)) {
                freeTile = checkTile;
            }
        }
        return freeTile;
    }

    public static WorldTile getNextWorldTile(final WorldTile tile, final int dir, final int increament) {
        return new WorldTile(
                tile.getX() + (dir == ForceMovement.EAST ? +increament : dir == ForceMovement.WEST ? -increament : 0),
                tile.getY() + (dir == ForceMovement.NORTH ? +increament : dir == ForceMovement.SOUTH ? -increament : 0),
                tile.getPlane());
    }

    public static int getOppositeDirection(final int dir) {
        switch (dir) {
            case ForceMovement.NORTH:
                return ForceMovement.SOUTH;
            case ForceMovement.EAST:
                return ForceMovement.WEST;
            case ForceMovement.SOUTH:
                return ForceMovement.NORTH;
            case ForceMovement.NORTH_EAST:
                return ForceMovement.SOUTH_WEST;
            case ForceMovement.NORTH_WEST:
                return ForceMovement.SOUTH_EAST;
            case ForceMovement.SOUTH_EAST:
                return ForceMovement.NORTH_WEST;
            case ForceMovement.SOUTH_WEST:
                return ForceMovement.NORTH_EAST;
            default:
            case ForceMovement.WEST:
                return ForceMovement.EAST;
        }
    }

    public static long projectileTimeToMiliseconds(Projectile projectile) {
        int time = projectile.getEndTime() + projectile.getStartTime();
        return projectileTimeToMiliseconds(time);
    }

    public static long projectileTimeToMiliseconds(int totalTime) {
        return (long) ((double) totalTime * (double) 16);
    }

    public static long projectileTimeToMiliseconds1(int totalTime) {
        return (long) ((double) totalTime * (double) 20);
    }

    public static boolean isInFaceDirection(Entity entity, WorldTile target) {
        byte[] dir = Utils.getDirection(Utils.getAngle(target.getX() - entity.getX(), target.getY() - entity.getY()));
        byte[] entityDir = Utils.getDirection(entity.getDirection());
        return dir[0] == entityDir[0] && dir[1] == entityDir[1];
    }

    public static String aorAn(String word) {
        return aorAn(word, false);
    }

    public static String aorAn(String word, boolean capital) {
        String vowels = "aeiou";
        boolean vowel = vowels.indexOf(Character.toLowerCase(word.toLowerCase().charAt(0))) != -1;
        return capital ? (vowel ? "An" : "A") : (vowel ? "an" : "a");
    }
    
    public static long currentUTCTimeMillis() {
        return System.currentTimeMillis() - 1587124656470l;
    }
    
    public static int millisecondsToCycles(long time) {
        return (int) ((double) time / 600.00);
    }
    
    public static int script_7439(int arg0, int arg1) {
        if (arg0 < arg1) {
            return arg0;
        }
        return arg0 - arg1 + 1000;
    }
}