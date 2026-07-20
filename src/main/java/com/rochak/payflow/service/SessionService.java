package com.rochak.payflow.service;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.session.UserSession;

import java.util.Optional;

public interface SessionService {

    UserSession createSession(User user, String device, String ip);

    Optional<UserSession> findBySessionId(String sessionId);

    void deleteSession(String sessionId);

    void updateSession(String sessionId);

    void rotateToken(String sessionId, String newTokenId);
}
