package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.game.player.Skills;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;



public class ParamTypeDefinitions {
    private static final ConcurrentHashMap<Integer, ParamTypeDefinitions> paramTypes = new ConcurrentHashMap<Integer, ParamTypeDefinitions>();

    public static void main(String[] args) throws IOException {
        Cache.init();
//        int itemId = 13742;
//        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
//        System.out.println(itemId + " - " + defs.getName() + " - " + paramsToString(defs.clientScriptData));

        GeneralRequirementMap map = GeneralRequirementMap.getMap(28562);
         System.out.println(map.getValues());

    }


    // [ParamId=458, value=20]
    // [ParamId=771, value=20]
    // [ParamId=2697, value=300]
    // 1366
    // [ParamId=7796, value(Dbrow Id)=1369]

    private int id;
    private int key;
    private int unknown1;
    private boolean unknownBoolean = true;
    private String name;

    public static final ParamTypeDefinitions getParamTypeDefinitions(int id) {
        ParamTypeDefinitions defs = paramTypes.get(id);
        if (defs != null)
            return defs;
        if (id == -1)
            return null;
        byte[] data = Cache.STORE.getIndexes()[2].getFile(11, id);
        defs = new ParamTypeDefinitions();
        defs.id = id;
        if (data != null)
            defs.readValueLoop(new InputStream(data));
        paramTypes.put(id, defs);
        return defs;
    }

    private void readValueLoop(InputStream stream) {
        for (;;) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private void readValues(InputStream stream, int opcode) {
        if (opcode == 1) {
            key = (byte) stream.readByte();
        } else if (opcode == 2)
            unknown1 = stream.readInt();
        else if (opcode == 4)
            unknownBoolean = false;
        else if (opcode == 5)
            name = stream.readString();
        else if (opcode == 101) {
            key = (char) stream.readSmart();
            System.out.println(id + " new opcode");
        } else {
            System.out.println("missing param type opcode " + opcode);
        }
    }

    public int getId() {
        return id;
    }

    public int getKey() {
        return key;
    }

    public int getUnknown1() {
        return unknown1;
    }

    public boolean isUnknownBoolean() {
        return unknownBoolean;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ParamTypeDefinitions [id=" + id + ", key=" + key + ", unknown1=" + unknown1 + ", unknownBoolean=" + unknownBoolean + ", name=" + name + "]";
    }

    public enum ScriptVarType {

        INT(0, "INT", 'i', BaseVarType.INTEGER, 0),

        BOOLEAN(1, "BOOLEAN", '1', BaseVarType.INTEGER, 0),

        TYPE_2(2, '2', BaseVarType.INTEGER, -1), // TODO

        QUEST(3, "QUEST", ':', BaseVarType.INTEGER, -1),

        QUESTHELP(4, "QUESTHELP", ',', BaseVarType.INTEGER, -1),

        CURSOR(5, "CURSOR", '@', BaseVarType.INTEGER, -1),

        SEQ(6, "ANIMATION", 'A', BaseVarType.INTEGER, -1),

        COLOUR(7, "COLOUR", 'C', BaseVarType.INTEGER, -1),

        LOC_SHAPE(8, "LOC_SHAPE", 'H', BaseVarType.INTEGER, -1),

        COMPONENT(9, "COMPONENT", 'I', BaseVarType.INTEGER, -1) {
            @Override
            public Object formatForDisplay(int paramId, Object value) {
                return "[" + (paramId != -1 ? "ParamId=" + paramId + ", " : "") + "IntefaceId=" + ((Integer) value >> 16) + ", ComponentId=" + ((Integer) value & 0xFFF) + "]";
            }

        },

        IDKIT(10, "IDKIT", 'K', BaseVarType.INTEGER, -1),

        MIDI(11, "MIDI", 'M', BaseVarType.INTEGER, -1),

        NPC_MODE(12, "NPC_MODE", 'N', BaseVarType.INTEGER, -1),

        TYPE_13(13, 'O', BaseVarType.INTEGER, -1), // TODO

        SYNTH(14, "SYNTH", 'P', BaseVarType.INTEGER, -1),

        TYPE_15(15, 'Q', BaseVarType.INTEGER, -1), // TODO

        AREA(16, "AREA", 'R', BaseVarType.INTEGER, -1),

        STAT(17, "STAT", 'S', BaseVarType.INTEGER, -1) {
            @Override
            public Object formatForDisplay(int paramId, Object value) {
                return "[" + (paramId != -1 ? "ParamId=" + paramId + ", " : "") + ((int) value < Skills.SKILL_NAME.length ? ("SkillName=" + Skills.SKILL_NAME[(int) value] + "]") : "Value=" + value);
            }
        },

        NPC_STAT(18, "NPC_STAT", 'T', BaseVarType.INTEGER, -1),

        WRITEINV(19, "WRITEINV", 'V', BaseVarType.INTEGER, -1),

        MESH(20, "MESH", '^', BaseVarType.INTEGER, -1),

        MAPAREA(21, "MAPAREA", '`', BaseVarType.INTEGER, -1),

        COORDGRID(22, "COORDGRID", 'c', BaseVarType.INTEGER, -1),

        GRAPHIC(23, "GRAPHIC (SPRITE)", 'd', BaseVarType.INTEGER, -1),

        CHATPHRASE(24, "CHATPHRASE", 'e', BaseVarType.INTEGER, -1),

        FONTMETRICS(25, "FONTMETRICS", 'f', BaseVarType.INTEGER, -1),

        ENUM(26, "ENUM", 'g', BaseVarType.INTEGER, -1),

        TYPE_27(27, 'h', BaseVarType.INTEGER, -1), // TODO

        JINGLE(28, "JINGLE", 'j', BaseVarType.INTEGER, -1),

        CHATCAT(29, "CHATCAT", 'k', BaseVarType.INTEGER, -1),

        LOC(30, "LOC (OBJ)", 'l', BaseVarType.INTEGER, -1),

        MODEL(31, "MODEL", 'm', BaseVarType.INTEGER, -1),

        NPC(32, "NPC", 'n', BaseVarType.INTEGER, -1),

        OBJ(33, "OBJ (ITEM)", 'o', BaseVarType.INTEGER, -1),

        PLAYER_UID(34, "PLAYER_UID", 'p', BaseVarType.INTEGER, -1),

        TYPE_35(35, 'r', BaseVarType.LONG, -1L), // TODO

        STRING(36, "STRING", 's', BaseVarType.STRING, ""),

        SPOTANIM(37, "GFX", 't', BaseVarType.INTEGER, -1),

        NPC_UID(38, "NPC_UID", 'u', BaseVarType.INTEGER, -1),

        INV(39, "INV", 'v', BaseVarType.INTEGER, -1),

        TEXTURE(40, "TEXTURE", 'x', BaseVarType.INTEGER, -1),

        CATEGORY(41, "CATEGORY", 'y', BaseVarType.INTEGER, -1),

        CHAR(42, "CHAR", 'z', BaseVarType.INTEGER, -1),

        LASER(43, "LASER", '|', BaseVarType.INTEGER, -1),

        BAS(44, "BAS", '\u20ac', BaseVarType.INTEGER, -1),

        TYPE_45(45, '\u0192', BaseVarType.INTEGER, -1), // TODO

        COLLISION_GEOMETRY(46, "COL_GEO", '\u2021', BaseVarType.INTEGER, -1),

        PHYSICS_MODEL(47, "PHYS_MODEL", '\u2030', BaseVarType.INTEGER, -1),

        PHYSICS_CONTROL_MODIFIER(48, "PHYS_CTRL_MOD", '\u0160', BaseVarType.INTEGER, -1),

        CLANHASH(49, "CLANHASH", '\u0152', BaseVarType.LONG, -1L),

        COORDFINE(50, "COORDFINE", '\u017d', BaseVarType.COORDFINE, null),

        CUTSCENE(51, "CUTSCENE", '\u0161', BaseVarType.INTEGER, -1),

        ITEMCODE(53, "ITEMCODE", '\u00a1', BaseVarType.INTEGER, -1),

        TYPE_54(54, '\u00a2', BaseVarType.INTEGER, -1), // TODO

        MAPSCENEICON(55, "MAPSCENEICON", '\u00a3', BaseVarType.INTEGER, -1),

        CLANFORUMQFC(56, "CLANFORUMQFC", '\u00a7', BaseVarType.LONG, -1L),

        VORBIS(57, "VORBIS", '\u00ab', BaseVarType.INTEGER, -1),

        VERIFY_OBJECT(58, "VERIFY_OBJECT", '\u00ae', BaseVarType.INTEGER, -1),

        MAPELEMENT(59, "MAPELEMENT", '\u00b5', BaseVarType.INTEGER, -1),

        CATEGORYTYPE(60, "CATEGORYTYPE", '\u00b6', BaseVarType.INTEGER, -1),

        SOCIAL_NETWORK(61, "SOCIAL_NETWORK", '\u00c6', BaseVarType.INTEGER, -1),

        HITMARK(62, "HITMARK", '\u00d7', BaseVarType.INTEGER, -1),

        PACKAGE(63, "PACKAGE", '\u00de', BaseVarType.INTEGER, -1),

        PARTICLE_EFFECTOR(64, "PARTICLE_EFFECTOR", '\u00e1', BaseVarType.INTEGER, -1),

        TYPE_65(65, '\u00e6', BaseVarType.INTEGER, -1), // TODO

        PARTICLE_EMITTER(66, "PARTICLE_EMITTER", '\u00e9', BaseVarType.INTEGER, -1),

        PLOGTYPE(67, "PLOGTYPE", '\u00ed', BaseVarType.INTEGER, -1),

        UNSIGNED_INT(68, "UINT", '\u00ee', BaseVarType.INTEGER, -1),

        SKYBOX(69, "SKYBOX", '\u00f3', BaseVarType.INTEGER, -1),

        SKYDECOR(70, "SKYDECOR", '\u00fa', BaseVarType.INTEGER, -1),

        HASH64(71, "HASH64", '\u00fb', BaseVarType.LONG, -1L),

        INPUTTYPE(72, "INPUTTYPE", '\u00ce', BaseVarType.INTEGER, -1),

        STRUCT(73, "STRUCT", 'J', BaseVarType.INTEGER, -1),

        DBROW(74, "DBROW", '\u00d0', BaseVarType.INTEGER, -1),

        TYPE_75(75, '\u00a4', BaseVarType.INTEGER, -1), // TODO

        TYPE_76(76, '\u00a5', BaseVarType.INTEGER, -1), // TODO

        TYPE_77(77, '\u00e8', BaseVarType.INTEGER, -1), // TODO

        TYPE_78(78, '\u00b9', BaseVarType.INTEGER, -1), // TODO

        TYPE_79(79, '\u00b0', BaseVarType.INTEGER, -1), // TODO

        TYPE_80(80, '\u00ec', BaseVarType.INTEGER, -1), // TODO

        TYPE_81(81, '\u00eb', BaseVarType.INTEGER, -1), // TODO

        TYPE_83(83, '\u00fe', BaseVarType.INTEGER, -1), // TODO

        TYPE_84(84, '\u00fd', BaseVarType.INTEGER, -1), // TODO

        TYPE_85(85, '\u00ff', BaseVarType.INTEGER, -1), // TODO

        TYPE_86(86, '\u00f5', BaseVarType.INTEGER, -1), // TODO

        TYPE_87(87, '\u00f4', BaseVarType.INTEGER, -1), // TODO

        TYPE_88(88, '\u00f6', BaseVarType.INTEGER, -1), // TODO

        GWC_PLATFORM(89, "GWC_PLATFORM", '\u00f2', BaseVarType.INTEGER, -1),

        TYPE_90(90, '\u00dc', BaseVarType.INTEGER, -1), // TODO

        TYPE_91(91, '\u00f9', BaseVarType.INTEGER, -1), // TODO

        TYPE_92(92, '\u00ef', BaseVarType.INTEGER, -1), // TODO

        TYPE_93(93, '\u00af', BaseVarType.INTEGER, -1), // TODO

        BUG_TEMPLATE(94, "BUG_TEMPLATE", '\u00ea', BaseVarType.INTEGER, -1),

        BILLING_AUTH_FLAG(95, "BILLING_AUTH_FLAG", '\u00f0', BaseVarType.INTEGER, -1),

        ACCOUNT_FEATURE_FLAG(96, "ACCOUNT_FEATURE_FLAG", '\u00e5', BaseVarType.INTEGER, -1),

        INTERFACE(97, "INTERFACE", 'a', BaseVarType.INTEGER, -1),

        TOPLEVELINTERFACE(98, "TOPLEVELITNERFACE", 'F', BaseVarType.INTEGER, -1),

        OVERLAYINTERFACE(99, "OVERLAYINTERFACE", 'L', BaseVarType.INTEGER, -1),

        CLIENTINTERFACE(100, "CLIENTINTERFACE", '\u00a9', BaseVarType.INTEGER, -1),

        MOVESPEED(101, "MOVESPEED", '\u00dd', BaseVarType.INTEGER, -1),

        MATERIAL(102, "MATERIAL", '\u00ac', BaseVarType.INTEGER, -1),

        SEQGROUP(103, "SEQGROUP", '\u00f8', BaseVarType.INTEGER, -1),

        TEMP_HISCORE(104, "TEMP_HS", '\u00e4', BaseVarType.INTEGER, -1),

        TEMP_HISCORE_LENGTH_TYPE(105, "TEMP_HS_LEN", '\u00e3', BaseVarType.INTEGER, -1),

        TEMP_HISCORE_DISPLAY_TYPE(106, "TEMP_HS_DISPLAY", '\u00e2', BaseVarType.INTEGER, -1),

        TEMP_HISCORE_CONTRIBUTE_RESULT(107, "TEMPH_HS_CONTRIB_RESULT", '\u00e0', BaseVarType.INTEGER, -1),

        AUDIOGROUP(108, "AUDIOGROUP", '\u00c0', BaseVarType.INTEGER, -1),

        AUDIOMIXBUSS(109, "AUDIOMIXBUS", '\u00d2', BaseVarType.INTEGER, -1),

        //LONG(110, "LONG", '�', BaseVarType.LONG, 0L),

        CRM_CHANNEL(111, "CRM_CHANNEL", '\u00cc', BaseVarType.INTEGER, -1),

        HTTP_IMAGE(112, "HTTP_IMAGE", '\u00c9', BaseVarType.INTEGER, -1),

        POP_UP_DISPLAY_BEHAVIOUR(113, "POP_UP_DISPLAY_BEHAVIOUR", '\u00ca', BaseVarType.INTEGER, -1),

        POLL(114, "POLL", '\u00f7', BaseVarType.INTEGER, -1),

        TYPE_115(115, '\u00bc', BaseVarType.LONG, -1L), // TODO

        TYPE_116(116, '\u00bd', BaseVarType.LONG, -1L), // TODO

        POINTLIGHT(117, "POINTLIGHT", '\u2022', BaseVarType.INTEGER, -1),

        PLAYER_GROUP(118, "PLAYER_GROUP", '\u00c2', BaseVarType.LONG, -1L),

        PLAYER_GROUP_STATUS(119, "PLAYER_GROUP_STATUS", '\u00c3', BaseVarType.INTEGER, -1),

        PLAYER_GROUP_INVITE_RESULT(120, "PLAYER_GROUP_INVITE_RESULT", '\u00c5', BaseVarType.INTEGER, -1),

        PLAYER_GROUP_MODIFY_RESULT(121, "PLAYER_GROUP_MODIFY_RESULT", '\u00cb', BaseVarType.INTEGER, -1),

        PLAYER_GROUP_JOIN_OR_CREATE_RESULT(122, "PLAYER_GROU_JOIN_OR_CREATE_RESULT", '\u00cd', BaseVarType.INTEGER, -1),

        PLAYER_GROUP_AFFINITY_MODIFY_RESULT(123, "PLAYER_GROU_AFFINITY_MODIFY_RESULT", '\u00d5', BaseVarType.INTEGER, -1),

        PLAYER_GROUP_DELTA_TYPE(124, "PLAYER_GROUP_DELTA_TYPE", '\u00b2', BaseVarType.INTEGER, -1),

        CLIENT_TYPE(125, "CLIENT_TYPE", '\u00aa', BaseVarType.INTEGER, -1),

        TELEMETRY_INTERVAL(126, "TELEMETRY_INTERVAL", '\0', BaseVarType.INTEGER, 0);

        private static final ConcurrentHashMap<Integer, ScriptVarType> scriptVarTypes = new ConcurrentHashMap<Integer, ScriptVarType>();
        static {
            for (ScriptVarType type : ScriptVarType.values())
                scriptVarTypes.put(type.id, type);
        }
        private final int id;
        private final String name;
        private final char c;
        private final BaseVarType baseType;
        private final Object defaultValue;

        ScriptVarType(int id, String name, char c, BaseVarType base, Object defaultValue) {
            this.id = id;
            this.name = name;
            this.c = c;
            this.baseType = base;
            this.defaultValue = defaultValue;
        }

        ScriptVarType(int id, char c, BaseVarType base, Object defaultValue) {
            this.id = id;
            this.name = "UNKNOWN" + id;
            this.c = c;
            this.baseType = base;
            this.defaultValue = defaultValue;
        }

        public Object formatForDisplay(Object value) {
            return formatForDisplay(-1, value);
        }

        public Object formatForDisplay(int paramId, Object value) {
            return "[" + (paramId != -1 ? "paramId=" + paramId + ", " : "") + Utils.formatPlayerNameForDisplay(name) + "=" + value + "]";
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public char getC() {
            return c;
        }

        public BaseVarType getBaseType() {
            return baseType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public static ScriptVarType getScriptVarTypeById(int id) {
            return scriptVarTypes.get(id);
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public enum BaseVarType {
        INTEGER() {
            public Object decode(InputStream stream) {
                return stream.readInt();
            }
        },
        LONG() {
            public Object decode(InputStream stream) {
                return stream.readLong();
            }
        },
        STRING() {
            public Object decode(InputStream stream) {
                return stream.readString();
            }
        },
        COORDFINE() {
            public Object decode(InputStream stream) {
                int anInt10777 = stream.readUnsignedByte();
                int anInt10780 = stream.readInt();
                int anInt10781 = stream.readInt();
                int anInt10782 = stream.readInt();
                return new CoordFine(anInt10777, anInt10780, anInt10781, anInt10782);
            }

        },
        UNKNOWN_TYPE() {
            public Object decode(InputStream stream) {
                int i_7_ = stream.readUnsignedByte();
                if (0 == i_7_)
                    return null;
                i_7_--;
                stream.setOffset(stream.getOffset() + 1);
                int i_8_ = stream.readInt();
                Object[] objects = new Object[i_7_];
                for (int i_9_ = 0; i_9_ < i_7_; i_9_++) {
                    int i_10_ = stream.readUnsignedByte();
                    if (0 == i_10_)
                        objects[i_9_] = stream.readInt();
                    else if (1 == i_10_)
                        objects[i_9_] = stream.readString();
                    else
                        throw new IllegalStateException(new StringBuilder().append("Unrecognised type ID in deserialise: ").append(i_10_).toString());
                }
                return new unknownType(i_8_, objects);
            }
        };

        public Object decode(InputStream stream) {
            return null;
        }
    }

    public static class unknownType {
        private final int id;
        private final Object[] objects;

        public unknownType(int id, Object[] objects) {
            this.id = id;
            this.objects = objects;
        }

        public int getId() {
            return id;
        }

        public Object[] getObjects() {
            return objects;
        }

    }

    public static class CoordFine {
        public int anInt10777;
        public int anInt10780;
        public int anInt10781;
        public int anInt10782;

        public CoordFine(int anInt10777, int anInt10780, int anInt10781, int anInt10782) {
            this.anInt10777 = anInt10777;
            this.anInt10780 = anInt10780;
            this.anInt10781 = anInt10781;
            this.anInt10782 = anInt10782;
        }

        public int getAnInt10777() {
            return anInt10777;
        }

        public int getAnInt10780() {
            return anInt10780;
        }

        public int getAnInt10781() {
            return anInt10781;
        }

        public int getAnInt10782() {
            return anInt10782;
        }

    }

    public static String getParamTypeValueToString(int paramId, Object value) {
        ParamTypeDefinitions defs = getParamTypeDefinitions(paramId);
        ScriptVarType varType = ScriptVarType.getScriptVarTypeById(defs.key);
        return (String) varType.formatForDisplay(paramId, value);
    }

    public static String getVarTypeName(int id) {
        ScriptVarType varType = ScriptVarType.getScriptVarTypeById(id);
        return Utils.formatPlayerNameForDisplay(varType.getName());
    }

    public static String paramsToString(HashMap<Integer, Object> params) {
        if (params == null)
            return null;
        String[] strings = new String[params.size()];
        int index = 0;
        for (Entry<Integer, Object> e : params.entrySet()) {
            strings[index] = getParamTypeValueToString(e.getKey(), e.getValue());
            index++;
        }
        return Arrays.toString(strings);
    }

    public static String paramsToString1(HashMap<Long, Object> params) {
        if (params == null)
            return null;
        String[] strings = new String[params.size()];
        int index = 0;
        for (Entry<Long, Object> e : params.entrySet()) {
            long key = e.getKey();
            strings[index] = getParamTypeValueToString((int) key, e.getValue());
            index++;
        }
        return Arrays.toString(strings);
    }

}
