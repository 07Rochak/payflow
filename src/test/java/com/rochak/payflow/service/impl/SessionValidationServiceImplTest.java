package com.rochak.payflow.service.impl;

import com.rochak.payflow.exception.*;
import com.rochak.payflow.session.SecurityProperties;
import com.rochak.payflow.session.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SessionValidationServiceImplTest {
    private SecurityProperties properties;
    private SessionValidationServiceImpl service;

    @BeforeEach void setUp() {
        properties = new SecurityProperties();
        properties.setValidateDevice(true);
        properties.setValidateIp(true);
        service = new SessionValidationServiceImpl(properties);
    }

    private UserSession session() {
        return UserSession.builder().sessionId("s1").device("Chrome").ip("127.0.0.1")
                .sessionVersion(1).expiresAt(Instant.now().plusSeconds(300)).build();
    }

    @Test void validate_shouldPassValidSession() {
        assertDoesNotThrow(() -> service.validate(session(), "Chrome", "127.0.0.1"));
    }

    @Test void validate_shouldRejectExpiredSession() {
        UserSession s=session(); s.setExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(SessionValidationException.class, () -> service.validate(s,"Chrome","127.0.0.1"));
    }

    @Test void validate_shouldRejectInvalidVersion() {
        UserSession s=session(); s.setSessionVersion(0);
        assertThrows(SessionValidationException.class, () -> service.validate(s,"Chrome","127.0.0.1"));
    }

    @Test void validate_shouldRejectDeviceMismatch() {
        assertThrows(DeviceMismatchException.class, () -> service.validate(session(),"Firefox","127.0.0.1"));
    }

    @Test void validate_shouldRejectIpMismatch() {
        assertThrows(IpAddressMismatchException.class, () -> service.validate(session(),"Chrome","10.0.0.1"));
    }
}
