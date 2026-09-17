package com.rs.network.codec;

public class ResultMessage {

    public static final ResultMessage SUCCESS, LOGIN_GAME_SUCCESS, LOGIN_LOBBY_SUCCESS, VERSION_OUTDATED, WORLD_UPDATE, SESSION_BAD, CREDENTIALS_UNSECURE, CREDENTIALS_INVALID, CREDENTIALS_BLACKLISTED_BAN, CREDENTIALS_BLACKLISTED_MACBAN, CREDENTIALS_PROFANITY, WORLD_CAPACITY, SESSION_ACTIVE, SESSION_LIMIT, PROFILE_BAD, PROFILE_LOCKED;

    static {
        SUCCESS = new ResultMessage(ResultType.SUCCESS);
        LOGIN_GAME_SUCCESS = new ResultMessage(ResultType.LOGIN_SUCCESS);
        LOGIN_LOBBY_SUCCESS = new ResultMessage(ResultType.LOGIN_SUCCESS);
        SESSION_BAD = new ResultMessage(ResultType.BAD_SESSION);
        PROFILE_BAD = new ResultMessage(ResultType.PROFILE_BAD);
        PROFILE_LOCKED = new ResultMessage(ResultType.PROFILE_LOCKED);
        SESSION_ACTIVE = new ResultMessage(ResultType.SESSION_ACTIVE);
        SESSION_LIMIT = new ResultMessage(ResultType.SESSION_LIMIT);
        WORLD_UPDATE = new ResultMessage(ResultType.WORLD_UPDATE);
        WORLD_CAPACITY = new ResultMessage(ResultType.WORLD_CAPACITY);
        VERSION_OUTDATED = new ResultMessage(ResultType.VERSION_OUTDATED);
        CREDENTIALS_INVALID = new ResultMessage(ResultType.CREDENTIALS_INVALID);
        CREDENTIALS_UNSECURE = new ResultMessage(ResultType.CREDENTIALS_UNSECURE);
        CREDENTIALS_PROFANITY = new ResultMessage(ResultType.CREDENTIALS_PROFANITY);
        CREDENTIALS_BLACKLISTED_BAN = new ResultMessage(ResultType.CREDENTIALS_BLACKLISTED_BAN);
        CREDENTIALS_BLACKLISTED_MACBAN = new ResultMessage(ResultType.CREDENTIALS_BLACKLISTED_MACBAN);
    }

    public enum ResultType {
        //
        FUTURE_CLOSE(-1),
        //
        SUCCESS(0),
        //
        LOGIN_SUCCESS(2),
        //
        CREDENTIALS_INVALID(3),
        //
        CREDENTIALS_BLACKLISTED_BAN(4),
        //
        SESSION_ACTIVE(5),
        //
        VERSION_OUTDATED(6),
        //
        WORLD_CAPACITY(7),
        //
        CONNECTION_FAILED_LOGINSERVER(8),
        //
        SESSION_LIMIT(9),
        //
        BAD_SESSION(10),
        //
        CREDENTIALS_UNSECURE(11),
        //
        CREDENTIALS_SUBSCRIPTION(12),
        //
        WORLD_UPDATE(14),
        //
        CREDENTIALS_FAILSAFE(16),
        //
        PROFILE_LOCKED(18),
        //
        PROFILE_BAD(24),
        //
        CREDENTIALS_BLACKLISTED_MACBAN(26),
        //
        CREDENTIALS_PROFANITY(31);

        private final int id;

        ResultType(int result) {
            this.id = result;
        }

        public int getId() {
            return id;
        }
    }

    private final ResultType type;

    private ResultMessage(ResultType type) {
        this.type = type;
    }

    public ResultType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ResultMessage [type=" + type + "]";
    }

}
