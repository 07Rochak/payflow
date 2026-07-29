package com.rochak.payflow.service.impl;


import com.rochak.payflow.exception.DeviceMismatchException;
import com.rochak.payflow.exception.IpAddressMismatchException;
import com.rochak.payflow.exception.SessionValidationException;
import com.rochak.payflow.service.SessionValidationService;
import com.rochak.payflow.session.SecurityProperties;
import com.rochak.payflow.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SessionValidationServiceImpl implements SessionValidationService {

    private final SecurityProperties securityProperties;

    @Override
    public void validate(UserSession session, String currentDevice, String currentIp) {
        validateExpiry(session);
        validateSessionVersion(session);
        validateDevice(session, currentDevice);
        validateIp(session, currentIp);
    }

    private void validateExpiry(UserSession session){
        if(session.getExpiresAt() !=null && session.getExpiresAt().isBefore(Instant.now())){
            throw new SessionValidationException("Session has expired");
        }
    }

    private void validateSessionVersion(UserSession session){
        if(session.getSessionVersion() == null || session.getSessionVersion() <= 0) {
            throw new SessionValidationException("Invalid session version");
        }
    }

    private void validateDevice(UserSession session, String currentDevice) {
        if(!securityProperties.isValidateDevice()){
            return;
        }

        if(!Objects.equals(session.getDevice(), currentDevice)){
            throw new DeviceMismatchException(
                    String.format("Device Mismatch. Expected=%s Current=%s", session.getDevice(), currentDevice)
            );
        }

    }

    private void validateIp(UserSession session, String currentIp){
        if(!securityProperties.isValidateIp()){
            return;
        }

        if(!Objects.equals(session.getIp(), currentIp)){
            throw new IpAddressMismatchException(
                    String.format("IP mismatch. Expected=%s, Current=%s", session.getIp(), currentIp)
            );
        }
    }
}
