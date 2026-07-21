package com.rochak.payflow.session;

public final class RedisKeys {

    private RedisKeys() {}

    public static String session(String sessionId) {
        return "session:" + sessionId;
    }

    public static String userSessions(Long userId) {
        return "user:%d:sessions".formatted(userId);
    }

    public static String refreshLock(String sessionId) {
        return "refresh-lock:%s".formatted(sessionId);
    }
}