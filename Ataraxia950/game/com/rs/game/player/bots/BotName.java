package com.rs.game.player.bots;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates RuneScape-style player names. The generator intentionally mixes
 * skill names, places, account archetypes, first names, compact handles, and
 * light number suffixes so server-spawned bots do not look like one roster.
 */
public final class BotName {

    public static final int MAX_NAME_LENGTH = 12;

    private BotName() {
    }

    private static final String[] ACTIVITY_WORDS = {
            "Abyss", "Archer", "Barrage", "Cannon", "Chaos", "Dds",
            "Edge", "Ess", "Farmrun", "Pouch",
            "Quest", "Raid", "Reaper", "Spec", "Tab", "Tele", "Trail",
            "Void", "Whip", "Wildy", "Xp"
    };

    private static final String[] COMBAT_WORDS = {
            "Barrage", "Blitzer", "Bolter", "Dds", "Dh", "Edge", "Hybrid",
            "K0", "Ko", "Pker", "Pking", "Pure", "Ranger", "Risk", "Spec",
            "Tank", "Venge", "Void", "Whip", "Zerk"
    };

    private static final String[] HUMAN_FIRSTS = {
            "Aaron", "Alex", "Amy", "Ari", "Ben", "Blake", "Bob", "Cal",
            "Casey", "Chris", "Cody", "Dan", "Dave", "Drew", "Eli", "Emma",
            "Finn", "Grace", "Hal", "Ian", "Ivy", "Jake", "Jay", "Jess",
            "Jill", "Josh", "Kai", "Kim", "Leo", "Liam", "Liz", "Lou",
            "Max", "Mia", "Mike", "Nate", "Nick", "Noah", "Owen", "Pat",
            "Quinn", "Ray", "Reese", "Riley", "Rose", "Sam", "Sara", "Sean",
            "Sky", "Steve", "Tara", "Theo", "Tim", "Tom", "Tony", "Ty",
            "Vince", "Wes", "Zack", "Zoe"
    };

    private static final String[] HANDLE_BASES = {
            "Aero", "Ashen", "Atlas", "Aven", "Blaze", "Bram", "Cipher",
            "Cloud", "Cobalt", "Comet", "Crux", "Echo", "Ember", "Fable",
            "Frost", "Glint", "Halo", "Haven", "Jinx", "Kairo", "Lumen",
            "Mako", "Nix", "Nova", "Onyx", "Orbit", "Pixel", "Quill",
            "Razor", "Rift", "Riot", "Rune", "Sage", "Shade", "Slate",
            "Spark", "Storm", "Talon", "Tempo", "Tide", "Vanta", "Vex",
            "Violet", "Wisp", "Zen"
    };

    private static final String[] CASUAL_WORDS = {
            "Apricot", "Biscuit", "Blank", "Cereal", "Copper", "Doodle",
            "Flicker", "Hazel", "Jester", "Mango", "Marble", "Minty",
            "Noodle", "Pebble", "Quartz", "Static", "Toast", "Velvet",
            "Wasabi", "Widget"
    };

    private static final String[] LIGHT_RS_WORDS = {
            "Ahrim", "Barrow", "Clue", "Dds", "Edge", "Fally", "Guthix",
            "Karil", "Lumby", "Nex", "Prif", "Rune", "Seers", "Torva",
            "Varrock", "Void", "Whip", "Wildy", "Yew", "Zaros"
    };

    private static final String[] PERSON_SUFFIXES = {
            "Afk", "Alt", "Btw", "Gz", "Irl", "Lad", "Lol", "Main", "Mate",
            "Ok", "Pls", "Rs", "Ty", "Xp"
    };

    private static final String[] MADE_UP_STARTS = {
            "Ael", "Ar", "Bel", "Cor", "Da", "El", "Fen", "Ka", "Kel", "Ky",
            "Lar", "Mal", "Mor", "Ny", "Or", "Ra", "Ren", "Sha", "Tal",
            "Var", "Ven", "Vor", "Za", "Zen"
    };

    private static final String[] MADE_UP_ENDS = {
            "a", "an", "ar", "en", "ia", "in", "is", "ix", "on", "or",
            "os", "ra", "ren", "ryn", "us", "yn"
    };

    private static final String[] ADJECTIVES = {
            "Ancient", "Brave", "Bright", "Chill", "Clever", "Cosmic", "Dark",
            "Deadly", "Elder", "Feral", "Fresh", "Frost", "Golden", "Grim",
            "Hidden", "Holy", "Iron", "Lucky", "Magic", "Mighty", "Quiet",
            "Rapid", "Royal", "Rune", "Shadow", "Silent", "Silver", "Swift",
            "Wild"
    };

    private static final String[] NOUNS = {
            "Arrow", "Axe", "Blade", "Bolt", "Boot", "Bow", "Cape", "Claw",
            "Core", "Gem", "Helm", "Herb", "Mage", "Ore", "Pouch", "Ranger",
            "Robes", "Rock", "Rune", "Scroll", "Shield", "Spark", "Staff",
            "Stone", "Sword", "Tab", "Whip", "Yew"
    };

    private static final String[] SHORT_SUFFIXES = {
            "Afk", "Alt", "Btw", "Gz", "Lol", "Main", "Noob", "Ok", "Pls",
            "Ty", "Xp"
    };

    private static final String[] FALLBACK_BASES = {
            "Adventurer", "Banker", "Crafter", "Ranger", "Runner", "Skiller",
            "Trader", "Wanderer"
    };

    private static final String[] COMMON_NUMBERS = {
            "7", "07", "10", "42", "73", "88", "99", "101", "126", "247",
            "365", "420", "777", "2007", "2012", "2277"
    };

    private static final char[] LEET_DIGITS = { '0', '1', '3', '4', '7' };

    public static String generate() {
        return generateDefault(ThreadLocalRandom.current());
    }

    public static String generateFallback(int sequence) {
        String base = FALLBACK_BASES[Math.floorMod(sequence, FALLBACK_BASES.length)];
        return withNumber(base, Math.max(1, sequence), false);
    }

    private static String generateDefault(ThreadLocalRandom random) {
        int pattern = random.nextInt(100);
        if (pattern < 26) return humanLike(random);
        if (pattern < 45) return compactHandle(random);
        if (pattern < 60) return casualHandle(random);
        if (pattern < 72) return wrappedHandle(random);
        if (pattern < 83) return madeUpHandle(random);
        if (pattern < 92) return lightRuneScapeHandle(random);
        if (pattern < 98) return initialsHandle(random);
        return leetSpeak(random);
    }

    private static String humanLike(ThreadLocalRandom random) {
        String first = pick(HUMAN_FIRSTS);
        int suffix = random.nextInt(100);
        if (suffix < 56) {
            return withRandomNumber(first, random.nextInt(2, 5), random.nextInt(4) == 0);
        }
        if (suffix < 78) {
            return join(first, pick(PERSON_SUFFIXES));
        }
        if (suffix < 90) {
            return oldInternetPrefix(first, random);
        }
        return clean(first + repeatLastLetter(first, random));
    }

    private static String casualHandle(ThreadLocalRandom random) {
        String base = random.nextBoolean() ? pick(CASUAL_WORDS) : pick(HANDLE_BASES);
        int pattern = random.nextInt(100);
        if (pattern < 34) {
            return withRandomNumber(base, random.nextInt(2, 5), false);
        }
        if (pattern < 58) {
            return join(base, pick(PERSON_SUFFIXES));
        }
        if (pattern < 76) {
            return oldInternetPrefix(base, random);
        }
        return clean(base);
    }

    private static String wrappedHandle(ThreadLocalRandom random) {
        String base = random.nextBoolean() ? pick(HANDLE_BASES) : pick(CASUAL_WORDS);
        int pattern = random.nextInt(100);
        if (pattern < 34) {
            return wrap("Xx", base, "Xx");
        }
        if (pattern < 56) {
            return wrap("I", base, "I");
        }
        if (pattern < 74) {
            return oldInternetPrefix(base, random);
        }
        if (pattern < 88) {
            return wrap("O", base, "O");
        }
        return withRandomNumber(base, random.nextInt(2, 4), false);
    }

    private static String madeUpHandle(ThreadLocalRandom random) {
        String base = pick(MADE_UP_STARTS) + pick(MADE_UP_ENDS);
        if (random.nextInt(100) < 32) {
            return withRandomNumber(base, random.nextInt(2, 5), false);
        }
        if (random.nextInt(100) < 18) {
            return oldInternetPrefix(base, random);
        }
        return clean(base);
    }

    private static String lightRuneScapeHandle(ThreadLocalRandom random) {
        String base = pick(LIGHT_RS_WORDS);
        int pattern = random.nextInt(100);
        if (pattern < 38) {
            return withRandomNumber(base, random.nextInt(2, 5), false);
        }
        if (pattern < 62) {
            return join(base, pick(PERSON_SUFFIXES));
        }
        if (pattern < 80) {
            return oldInternetPrefix(base, random);
        }
        return clean(base);
    }

    private static String initialsHandle(ThreadLocalRandom random) {
        char first = (char) ('A' + random.nextInt(26));
        String base = random.nextBoolean() ? pick(HANDLE_BASES) : pick(HUMAN_FIRSTS);
        if (random.nextBoolean()) {
            return join(Character.toString(first), base);
        }
        return clean(base + " " + first);
    }

    private static String oldInternetPrefix(String base, ThreadLocalRandom random) {
        int pattern = random.nextInt(100);
        if (pattern < 38) {
            return clean("I " + base);
        }
        if (pattern < 64) {
            return clean("X " + base);
        }
        if (pattern < 82) {
            return clean("Ii " + base);
        }
        return clean("Xx " + base);
    }

    private static String wrap(String left, String base, String right) {
        String full = cleanSeparators(left + " " + base + " " + right);
        if (full.length() <= MAX_NAME_LENGTH) {
            return clean(full);
        }
        String leftOnly = cleanSeparators(left + " " + base);
        if (leftOnly.length() <= MAX_NAME_LENGTH) {
            return clean(leftOnly);
        }
        String rightOnly = cleanSeparators(base + " " + right);
        if (rightOnly.length() <= MAX_NAME_LENGTH) {
            return clean(rightOnly);
        }
        return clean(base);
    }

    private static String compactHandle(ThreadLocalRandom random) {
        String base = pick(HANDLE_BASES);
        if (random.nextInt(100) < 34) {
            base = join(base, pick(SHORT_SUFFIXES));
        } else if (random.nextInt(100) < 28) {
            base = withRandomNumber(base, random.nextInt(1, 4), false);
        }
        return clean(base);
    }

    private static String leetSpeak(ThreadLocalRandom random) {
        String base = join(random.nextBoolean() ? pick(ADJECTIVES) : pick(ACTIVITY_WORDS),
                random.nextBoolean() ? pick(NOUNS) : pick(COMBAT_WORDS));
        StringBuilder builder = new StringBuilder(base.length() + 1);
        for (int index = 0; index < base.length(); index++) {
            char c = base.charAt(index);
            if (c == ' ' || random.nextInt(4) != 0) {
                builder.append(c);
                continue;
            }
            switch (Character.toLowerCase(c)) {
                case 'a':
                    builder.append('4');
                    break;
                case 'e':
                    builder.append('3');
                    break;
                case 'i':
                    builder.append('1');
                    break;
                case 'o':
                    builder.append('0');
                    break;
                case 's':
                    builder.append('5');
                    break;
                case 't':
                    builder.append('7');
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        if (random.nextBoolean()) {
            builder.append(LEET_DIGITS[random.nextInt(LEET_DIGITS.length)]);
        }
        return clean(builder.toString());
    }

    private static String join(String left, String right) {
        String spaced = cleanSeparators(left + " " + right);
        if (spaced.length() <= MAX_NAME_LENGTH) {
            return clean(spaced);
        }

        String compact = cleanSeparators(left + right);
        if (compact.length() <= MAX_NAME_LENGTH) {
            return clean(compact);
        }

        String slimRight = removeVowelsAfterFirst(right);
        spaced = cleanSeparators(left + " " + slimRight);
        if (spaced.length() <= MAX_NAME_LENGTH) {
            return clean(spaced);
        }

        compact = cleanSeparators(left + slimRight);
        if (compact.length() <= MAX_NAME_LENGTH) {
            return clean(compact);
        }

        return clean(compact);
    }

    private static String withRandomNumber(String base, int maxDigits, boolean spaced) {
        return withNumber(base, rollNumber(maxDigits), spaced);
    }

    private static String withNumber(String base, int number, boolean spaced) {
        return withNumber(base, Integer.toString(number), spaced);
    }

    private static String withNumber(String base, String number, boolean spaced) {
        String cleanNumber = digitsOnly(number);
        if (cleanNumber.isEmpty()) {
            cleanNumber = "1";
        }
        if (cleanNumber.length() >= MAX_NAME_LENGTH) {
            cleanNumber = cleanNumber.substring(cleanNumber.length() - (MAX_NAME_LENGTH - 3));
        }
        String cleanBase = trimEdges(cleanSeparators(base));
        String separator = spaced ? " " : "";
        int maxBaseLength = MAX_NAME_LENGTH - cleanNumber.length() - separator.length();
        if (maxBaseLength < 2 && spaced) {
            separator = "";
            maxBaseLength = MAX_NAME_LENGTH - cleanNumber.length();
        }
        if (maxBaseLength < 2) {
            cleanBase = "Bot";
            maxBaseLength = MAX_NAME_LENGTH - cleanNumber.length();
        }
        if (cleanBase.length() > maxBaseLength) {
            cleanBase = fitBaseForNumber(cleanBase, maxBaseLength);
        }
        if (cleanBase.length() < 2) {
            cleanBase = "Bot";
        }
        return clean(cleanBase + separator + cleanNumber);
    }

    private static String fitBaseForNumber(String base, int maxLength) {
        String cleanBase = trimEdges(base);
        if (cleanBase.length() <= maxLength) {
            return cleanBase;
        }

        String compact = cleanBase.replace(" ", "");
        if (compact.length() <= maxLength) {
            return compact;
        }

        int firstSpace = cleanBase.indexOf(' ');
        if (firstSpace > 1) {
            String firstWord = cleanBase.substring(0, firstSpace);
            if (firstWord.length() <= maxLength) {
                return firstWord;
            }
        }

        String compactShort = removeVowelsAfterFirst(compact);
        if (compactShort.length() <= maxLength) {
            return compactShort;
        }

        return trimEdges(compact.substring(0, Math.max(0, maxLength)));
    }

    private static String rollNumber(int maxDigits) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int digits = Math.max(1, Math.min(maxDigits, 4));
        if (random.nextInt(100) < 28) {
            for (int attempt = 0; attempt < 8; attempt++) {
                String number = pick(COMMON_NUMBERS);
                if (number.length() <= digits) {
                    return number;
                }
            }
        }
        int max = 1;
        for (int index = 0; index < digits; index++) {
            max *= 10;
        }
        return Integer.toString(random.nextInt(1, max));
    }

    private static String removeVowelsAfterFirst(String value) {
        if (value == null || value.length() <= 3) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (index > 0 && isVowel(c)) {
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private static String repeatLastLetter(String value, ThreadLocalRandom random) {
        if (value == null || value.isEmpty() || value.length() >= MAX_NAME_LENGTH) {
            return "";
        }
        char last = value.charAt(value.length() - 1);
        int repeat = random.nextInt(1, Math.min(3, MAX_NAME_LENGTH - value.length()) + 1);
        StringBuilder builder = new StringBuilder(repeat);
        for (int index = 0; index < repeat; index++) {
            builder.append(last);
        }
        return builder.toString();
    }

    private static boolean isVowel(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
            default:
                return false;
        }
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (c >= '0' && c <= '9') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String pick(String[] pool) {
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    private static String clean(String name) {
        String cleaned = trimEdges(cleanSeparators(name));
        if (cleaned.length() > MAX_NAME_LENGTH) {
            cleaned = trimEdges(cleaned.substring(0, MAX_NAME_LENGTH));
        }
        if (cleaned.length() < 3) {
            cleaned = withNumber("Bot", ThreadLocalRandom.current().nextInt(10, 100), false);
        }
        return cleaned;
    }

    private static String cleanSeparators(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(name.length());
        boolean previousSeparator = false;
        for (int index = 0; index < name.length(); index++) {
            char c = name.charAt(index);
            if (isAsciiLetterOrDigit(c)) {
                builder.append(c);
                previousSeparator = false;
            } else if ((c == ' ' || c == '_') && !previousSeparator && builder.length() > 0) {
                builder.append(' ');
                previousSeparator = true;
            }
        }
        return builder.toString();
    }

    private static String trimEdges(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int start = 0;
        int end = value.length();
        while (start < end && isSeparator(value.charAt(start))) {
            start++;
        }
        while (end > start && isSeparator(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    private static boolean isSeparator(char c) {
        return c == ' ' || c == '_';
    }

    private static boolean isAsciiLetterOrDigit(char c) {
        return c >= 'a' && c <= 'z'
                || c >= 'A' && c <= 'Z'
                || c >= '0' && c <= '9';
    }
}
