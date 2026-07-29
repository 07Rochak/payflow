package com.rochak.payflow.service;

import com.rochak.payflow.session.UserSession;

public interface SessionValidationService {
    void validate(UserSession session, String currentDevice, String currentIp);
}
