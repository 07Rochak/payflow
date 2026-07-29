package com.rochak.payflow.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class DeviceExtractor {
    private static final String USER_AGENT = "User-Agent";
    private static final String UNKNOWN_DEVICE = "UNKNOWN";
    public String extract(HttpServletRequest request){
        String userAgent = request.getHeader(USER_AGENT);

        if(userAgent == null || userAgent.isBlank())
        {
            return UNKNOWN_DEVICE;
        }
        return userAgent.trim();
    }
}
