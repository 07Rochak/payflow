package com.rochak.payflow.service;

import com.rochak.payflow.entity.User;
import com.rochak.payflow.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.Set;

public interface SessionService {

    UserSession createSession(User user, String device, String ip);

    Optional<UserSession> getRequiredSession(String sessionId);

    void deleteSession(String sessionId);

    Set<String> getUserSessions(Long userId);

    void deleteAllSessions(Long userId);

    UserSession validateAndRotate(String sessionId, String presentedTokenId, HttpServletRequest request);

    void cleanupOrphanSessions();
}
